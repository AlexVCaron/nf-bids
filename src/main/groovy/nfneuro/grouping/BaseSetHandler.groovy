/* groovylint-disable DuplicateNumberLiteral, DuplicateStringLiteral, ReturnsNullInsteadOfEmptyCollection */
package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsEntityUtils
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.util.SuffixMapper
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Base handler for BIDS set processing
 *
 * Provides common functionality for plain, named, sequential, and mixed sets.
 * Each handler implements specific grouping and organization logic.
 *
 * @reference Set processing implementation:
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
abstract class BaseSetHandler {

    final static String getSetType(Map suffixConfig) {
        if (suffixConfig.containsKey('plain_set')) {
            return "plain_set"
        } else if (suffixConfig.containsKey('named_set')) {
            return "named_set"
        } else if (suffixConfig.containsKey('sequential_set')) {
            return "sequential_set"
        } else if (suffixConfig.containsKey('mixed_set')) {
            return "mixed_set"
        }

        return null
    }

    /**
     * Process BIDS files according to set type
     *
     * @param datasetRoot Root path of BIDS dataset
     * @param bidsFiles List of BIDS files to process
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping (for suffix_maps_to)
     * @return Processed channel data
     */
    /* groovylint-disable-next-line MethodSize */
    DataflowQueue process(
        String datasetRoot,
        List<BidsFile> bidsFiles,
        Map config,
        List<String> loopOverEntities,
        Map<String, Map<String, String>> suffixMapping
    ) {
        BidsLogger.logProgress(logGroup(), "Processing mixed sets with ${bidsFiles.size()} files")
        BidsLogger.logProgress(logGroup(), "Loop-over entities: ${loopOverEntities}")

        // Start by grouping all files following loop-over entities
        return BidsEntityUtils.groupByEntities(bidsFiles, loopOverEntities)
            .collect { groupKey, filesInGroup ->
                // Once initial sorting is done, look at every file to determine its fit with a set
                filesInGroup.collect { file ->
                    // We first check if a file matches any set configuration
                    BidsLogger.logProgress(logGroup(), "Processing file: ${file.path}")

                    Map setConfig = findMatchingGrouping(file, config, suffixMapping)
                    if (setConfig == null) {
                        return
                    }

                    // Get both file suffix and config key for downstream packing
                    String suffix = file.suffix
                    String configKey = SuffixMapper.resolveConfigKey(setName(), suffix, suffixMapping)
                    BidsLogger.logProgress(logGroup(), "Matched file suffix: ${suffix}, config key: ${configKey}")

                    // We find its index in the current packing scheme (with both suffix and config key)
                    Map index = getSetIndex(file, setConfig, configKey)
                    if (!index) {
                        BidsLogger.logProgress(logGroup(), "Could not determine set index for file: ${file.path}")
                        return
                    }

                    return [
                        file: file,
                        suffix: suffix,
                        index: index,
                        ordering: getOrdering(file, setConfig)
                    ]
                }
                .findAll { file -> file != null }
                .inject([sets: [:], allFiles: [:]]) { acc, data ->
                    packFileIntoSet(acc.sets, acc.allFiles, data.index, data.file, data.ordering)
                    return acc
                }
                .with { packedData ->
                    // Filter and sort sets using functional chain
                    // Note: sets are now keyed by configKey, not file suffix
                    Map filteredSortedSets = packedData.sets.findAll { configKey, subset ->
                        String fileSuffix = subset.fileSuffix ?: configKey
                        Map suffixConfig = config.get(configKey) as Map

                        // Validate required fields/groups are present
                        if (suffixConfig?.required && !(suffixConfig.required as List).isEmpty()) {
                            List<String> required = suffixConfig.required as List<String>
                            Set<String> available = getAvailableKeys(subset, packedData.allFiles.get(fileSuffix, []))
                            /* groovylint-disable-next-line LineLength */
                            BidsLogger.logProgress(logGroup(), "Checking required for ${configKey}: required=${required}, available=${available}")

                            List<String> missing = required.findAll { field -> !available.contains(field) }
                            if (missing) {
                                /* groovylint-disable-next-line LineLength */
                                BidsLogger.logProgress(logGroup(), "Config key ${configKey} missing required fields: ${missing}, filtering out")
                                return false
                            }
                        }
                        return true
                    }
                    .collectEntries { configKey, subset ->
                        if (subset.entities) {
                            /* groovylint-disable-next-line IfStatementCouldBeTernary, Indentation */
                            if (subset.entities.size() == 1) {
                                // Apply sorting to each subfield without mutation
                                /* groovylint-disable-next-line Indentation */
                                return [
                                    /* groovylint-disable-next-line NestedBlockDepth */
                                    (configKey): subset + subset.findAll { name, val ->
                                        return !['entities', 'order', 'fileSuffix'].contains(name)
                                    }
                                    /* groovylint-disable-next-line NestedBlockDepth */
                                    .collectEntries { subName, items ->
                                        return [subName: items instanceof List
                                            /* groovylint-disable-next-line NestedBlockDepth */
                                            ? items.sort { a, b ->
                                                return compareSequenceValues(
                                                    a.sequenceValues,
                                                    b.sequenceValues
                                                )
                                            }
                                            : items
                                        ]
                                    }
                                ]
                            }
                            /* groovylint-disable-next-line Indentation */
                            return [
                                (configKey): subset + (subset.order == 'flat'
                                    ? [files: applyFlatOrdering(subset.files)]
                                    : [files: applyHierarchicalOrdering(subset.files)])
                            ]
                        }

                        return [(configKey): subset]
                    }

                    processGroup(
                        datasetRoot,
                        filteredSortedSets,
                        packedData.allFiles,
                        config,
                        loopOverEntities,
                        suffixMapping
                    )
                }
            }
            .findAll { channelData -> channelData != null }
            .inject(new DataflowQueue()) { queue, channelData ->
                queue << channelData
                return queue
            }
    }

    /**
     * Get set configuration from suffix config
     *
     * @param suffixConfig Configuration for specific suffix
     * @return Set configuration (plain_set, named_set, etc.)
     */
    protected abstract Map getSetConfig(Map suffixConfig)

    /**
     * Get set name
     *
     * @return Name of the set type (e.g., "plain_set", "named_set")
     */
    protected abstract String setName()

    /**
     * Get sequence by entities from set config
     *
     * @param setConfig Set configuration
     * @return List of entities to sequence by
     */
    protected abstract List<String> getSequenceByEntities(Map setConfig)

    /**
     * Get set index for a file based on set configuration
     *
     * @param file BIDS file
     * @param setConfig Set configuration
     * @param configKey Configuration key (for suffix_maps_to support)
     * @return Set index containing both fileSuffix and configKey
     */
    protected abstract Map getSetIndex(BidsFile file, Map setConfig, String configKey)

    /**
     * Pack file into set structure
     *
     * @param sets Set structure to populate
     * @param allFiles Map of associated files
     * @param index Set index for the file
     * @param file BIDS file to pack
     * @param ordering Ordering information for sequencing
     */
    protected abstract void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering)

    /**
     * Process a group of files
     *
     * @param datasetRoot Root path of BIDS dataset
     * @param plainSets Map of plain sets
     * @param allFiles Map of associated files
     * @param config Full configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData for the group or null if filtered out
     */
    /* groovylint-disable-next-line ParameterCount */
    protected abstract BidsChannelData processGroup(
        String datasetRoot,
        Map plainSets,
        Map allFiles,
        Map config,
        List<String> loopOverEntities,
        Map suffixMapping
    )

    protected List applyFlatOrdering(Map files) {
        // Handle Map input
        return applyFlatOrdering(files.values().collect())
    }

    protected List applyFlatOrdering(List files) {
        // Handle List input
        return files.sort { a, b ->
            compareSequenceValues(a.sequenceValues, b.sequenceValues)
        }
    }

    protected List applyHierarchicalOrdering(Map files) {
        // Handle Map input
        return applyHierarchicalOrdering(files.values().collect())
    }

    protected List applyHierarchicalOrdering(List files) {
        if (files.isEmpty()) { return [] }

        // Determine nesting depth from first file's sequenceValues
        if (files[0].sequenceValues.size() == 1) {
            // Base case: single entity remaining, return sorted flat list
            return files.sort { a, b ->
                compareSequenceValues(a.sequenceValues[0], b.sequenceValues[0])
            }
        }

        // Recursive case: group by first entity value
        return files.groupBy { entry -> entry.sequenceValues[0] }
            .sort { a, b -> compareSequenceValues(a.key, b.key) }
            .collect { key, groupList ->
                applyHierarchicalOrdering(groupList.collect { fileEntry ->
                    [
                        file: fileEntry.file,
                        sequenceValues: fileEntry.sequenceValues[1..-1]
                    ]
                })
            }
    }

    /**
     * Compare sequence values for sorting
     *
     * Attempts numeric comparison first by extracting numbers from entity values.
     * For example, "echo-10" and "echo-2" will be compared as 10 and 2 (not alphabetically).
     * Falls back to string comparison if values are not numeric.
     *
     * @param a First value to compare
     * @param b Second value to compare
     * @return Negative if a < b, positive if a > b, zero if equal
     */
    protected int compareSequenceValues(String a, String b) {
        // Extract numeric portion from entity values (e.g., "echo-10" -> 10)
        Closure extractNumber = { val ->
            // Match pattern like "entity-123" or just "123"
            try {
                return (val as String =~ /(\d+)$/).group(1)?.toInteger()
            } catch (IllegalStateException e) {
                /* groovylint-disable-next-line ReturnNullFromCatchBlock */
                return
            }
        }

        Integer numA = extractNumber(a)
        Integer numB = extractNumber(b)

        // If both values have numeric components, compare numerically
        if (numA != null && numB != null) {
            return numA <=> numB
        }

        // Otherwise fall back to string comparison
        return a.toString() <=> b.toString()
    }

    protected int compareSequenceValues(List a, List b) {
        return a.toString() <=> b.toString()
    }

    protected String logGroup() {
        return "nf-bids-${setName()}"
    }

    /**
     * Find matching grouping pattern for given file
     *
     * @param file BIDS file
     * @param config Configuration
     * @param suffixMapping Suffix to config key mapping
     * @return Matching grouping configuration or null
     *
     * @reference findMatchingGrouping function:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L1-L35
     */
    protected Map findMatchingGrouping(BidsFile file, Map config, Map<String, Map<String, String>> suffixMapping) {
        String suffix = file.suffix
        if (!suffix) {
            BidsLogger.logProgress(logGroup(), "Skipping file without suffix: ${file.path}")
            return
        }

        // Resolve config key using suffix mapping
        String configKey = SuffixMapper.resolveConfigKey(setName(), suffix, suffixMapping)
        BidsLogger.logProgress(logGroup(), "Resolved config key for suffix '${suffix}': ${configKey}")

        Map suffixConfig = config.get(configKey) as Map
        if (!suffixConfig || !(suffixConfig instanceof Map)) {
            BidsLogger.logProgress(logGroup(), "No configuration for suffix: ${suffix} - FILTERED")
            return
        }

        // Check for set type configuration
        Map setConfig = getSetConfig(suffixConfig as Map)
        if (setConfig == null) {
            BidsLogger.logProgress(logGroup(), "No configuration for suffix: ${suffix} - FILTERED")
            return
        }

        BidsLogger.logProgress(logGroup(), "Found configuration for ${suffix}")

        // Validate entity matching if filter specified
        if (setConfig.filter) {
            if (!BidsEntityUtils.entitiesMatch(file.entities, setConfig.filter as List)) {
                BidsLogger.logProgress(logGroup(), "File filtered by entity pattern: ${file.path}")
                return
            }
        }

        // Exclude files with specific entities if specified
        if (setConfig.exclude_entities) {
            for (String entityName : setConfig.exclude_entities as List<String>) {
                String normalizedEntity = BidsEntity.normalizeName(entityName)
                if (file.hasEntity(normalizedEntity)) {
                    BidsLogger.logProgress(logGroup(), "File excluded by entity ${entityName}: ${file.path}")
                    return
                }
            }
        }

        // Validate required entities are present
        List<String> requiredEntities = setConfig.required_entities as List<String>
        if (requiredEntities && !BidsEntityUtils.hasRequiredEntities(file, requiredEntities)) {
            BidsLogger.logProgress(logGroup(), "File missing required entities: ${file.path}")
            return
        }

        return setConfig as Map
    }

    /**
     * Get sequence by entities from set config
     *
     * @param file BIDS file
     * @param setConfig Set configuration
     * @return List of entities to sequence by
     */
    protected Map<String, List<String>> getOrdering(BidsFile file, Map setConfig) {
        // Get the entities to sequence by (single or multiple)
        List sequenceByEntities = getSequenceByEntities(setConfig)
        if (!sequenceByEntities || sequenceByEntities.isEmpty()) {
            BidsLogger.logProgress(logGroup(), "Sequence config missing entities for suffix: ${file.suffix}")
            return
        }

        // Extract values for all sequence entities
        // Normalize entity names: config uses long names (inversion),
        // but BidsFile stores short names (inv)
        List sequenceValues = sequenceByEntities.collect { entity ->
            file.getEntityValue(BidsEntity.normalizeName(entity))
        }

        // Skip if any required sequence entity is missing
        if (sequenceValues.any { val -> val == null }) {
            BidsLogger.logProgress(logGroup(), "File missing required sequence entities: ${file.filename}")
            return
        }

        return [
            entities: sequenceByEntities,
            values: sequenceValues,
            order: setConfig.order ?: 'flat'
        ]
    }

    /**
     * Filter files by suffix
     *
     * @param files All BIDS files
     * @param suffix Target suffix
     * @return Filtered files
     */
    protected List<BidsFile> filterBySuffix(List<BidsFile> files, String suffix) {
        return files.findAll { file -> file.suffix == suffix }
    }

    /**
     * Get available keys for required field validation
     *
     * For named sets: returns keys from subset.files (named groups like 'ap', 'pa')
     * For sequential/plain sets: returns file types/extensions (like 'nii', 'json', 'bval')
     * For mixed sets: returns both named groups and file types
     *
     * @param subset The set data structure
     * @param allFiles All files for this suffix
     * @return Set of available keys
     */
    protected Set<String> getAvailableKeys(Map subset, List<BidsFile> allFiles) {
        // For named sets: check keys in subset.files (the named groups)
        if (subset.containsKey('files') && subset.files instanceof Map) {
            return subset.files.keySet()
        }

        // For sequential/plain sets: check file types/extensions available in allFiles
        if (allFiles) {
            return allFiles*.getType().toSet()
        }

        return [] as Set
    }

    /**
     * Validate required files are present in group
     *
     * @param files File list
     * @param requiredCount Minimum required files
     * @return true if valid
     *
     * @reference validateRequiredFiles function:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/validation_utils.nf#L1-L25
     */
    protected boolean validateRequiredFileCount(List<BidsFile> files, Integer requiredCount) {
        if (!requiredCount) {
            return true
        }

        return files.size() >= requiredCount
    }

    /**
     * Log processing statistics
     *
     * @param handlerName Name of handler
     * @param processedCount Files processed
     * @param filteredCount Files filtered
     */
    protected void logProcessingStats(
            int processedCount,
            int filteredCount) {

        BidsLogger.logProgress(logGroup(), "${processedCount} emitted, ${filteredCount} filtered")

        if (filteredCount > 0) {
            BigDecimal filterRatio = (filteredCount * 100) / (processedCount + filteredCount)
            BidsLogger.logProgress(logGroup(), "Filter ratio: ${filterRatio.round(1)}%")
        }
    }

    /**
     * Apply parts grouping to a nested data map
     *
     * Transforms file lists/values grouped by extension to also group by BIDS 'part' entity.
     * Parts represent subcomponents of complex MRI data (e.g., mag/phase, real/imag).
     *
     * Input structure examples:
     *   {nii: ["path1", "path2"]} or {nii: "path"}
     * Output structure with parts:
     *   {nii: [{mag: "path1_mag", phase: "path1_phase"}, {mag: "path2_mag", phase: "path2_phase"}]}
     *
     * @param nestedMap Map of extension type to file path(s) - {nii: [...], json: [...]}
     * @param partsConfig List of part values to group by (e.g., ["mag", "phase"])
     * @param allFiles All BidsFile objects for extracting part entity values
     * @param datasetRoot Root path for computing relative paths
     * @return Transformed map with parts grouping applied where applicable
     */
    protected Map applyPartsGrouping(
        Map nestedMap,
        List<String> partsConfig,
        List<BidsFile> allFiles,
        String datasetRoot
    ) {
        if (!partsConfig || partsConfig.isEmpty()) {
            return nestedMap
        }

        Map basenameToFile = allFiles.groupBy { file -> file.getBasename(['part']) }
            .withDefault { [] }
            .values()
            .flatten()
            .collectEntries { file -> [(file.relativeTo(datasetRoot)): file] }

        return nestedMap.collectEntries { ftype, value ->
            [(ftype): BidsFile.typeAllowsParts(ftype)
                ? groupValueByParts(value, partsConfig, basenameToFile)
                : value]
        }
    }

    /**
     * Recursively group a value (string, list, or nested list) by parts
     */
    /* groovylint-disable-next-line UnusedMethodParameter */
    protected List groupValueByParts(String value, List<String> partsConfig, Map basenameToFile) {
        return value
    }

    protected List groupValueByParts(List value, List<String> partsConfig, Map basenameToFile) {
        if (value.isEmpty()) {
            return value
        }

        return value[0] instanceof String
            ? groupPathsByParts(value, partsConfig, basenameToFile)
            : value.collect { val -> groupValueByParts(val, partsConfig, basenameToFile) }
    }

    /**
     * Group a list of file paths by their part entity values
     *
     * Uses the basenameToFile lookup map to find properly parsed BidsFile objects.
     * Groups files by their basename (excluding part entity) and creates part-grouped maps.
     *
     * @param paths List of relative file paths to group
     * @param partsConfig List of part values (e.g., ["mag", "phase"])
     * @param basenameToFile Map of basename to BidsFile objects
     */
    protected List groupPathsByParts(List<String> paths, List<String> partsConfig, Map basenameToFile) {
        /* groovylint-disable-next-line DuplicateListLiteral, DuplicateStringLiteral */
        return paths.groupBy { path -> basenameToFile[path]?.getBasename(['part']) ?: path }
            .values()
            *.collectEntries { path ->
                String partValue = basenameToFile[path]?.getEntityValue('part')
                return (partValue && partsConfig.contains(partValue)) ? [(partValue): path] : [:]
            }
            .findAll { parts -> !parts.isEmpty() }
    }

}
