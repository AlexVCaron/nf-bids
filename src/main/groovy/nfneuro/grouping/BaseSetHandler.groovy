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
        if (suffixConfig.plain_set) {
            return "plain_set"
        } else if (suffixConfig.named_set) {
            return "named_set"
        } else if (suffixConfig.sequential_set) {
            return "sequential_set"
        } else if (suffixConfig.mixed_set) {
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
    DataflowQueue process(
        String datasetRoot,
        List<BidsFile> bidsFiles,
        Map config,
        List<String> loopOverEntities,
        Map<String, Map<String, String>> suffixMapping
    ) {
        BidsLogger.logProgress(getLogGroup(), "Processing mixed sets with ${bidsFiles.size()} files")
        BidsLogger.logProgress(getLogGroup(), "Loop-over entities: ${loopOverEntities}")

        def results = new DataflowQueue()
        def processedCount = 0
        def filteredCount = 0

        // Group files by loop-over entities first
        def filesByGroup = BidsEntityUtils.groupByEntities(bidsFiles, loopOverEntities)

        // Process each group
        filesByGroup.each { groupKey, filesInGroup ->
            Map sets = [:]
            Map allFiles = [:]

            filesInGroup.each { file ->
                BidsLogger.logProgress(getLogGroup(), "Processing file: ${file.path}")
                Map setConfig = findMatchingGrouping(file, config, suffixMapping)
                if (setConfig == null) {
                    return
                }

                String suffix = file.suffix
                BidsLogger.logProgress(getLogGroup(), "Matched file suffix: ${suffix}")

                Map index = getSetIndex(file, setConfig)
                if (!index) {
                    BidsLogger.logProgress(getLogGroup(), "Could not determine set index for file: ${file.path}")
                    return
                }

                def ordering = getOrdering(file, setConfig)

                packFileIntoSet(sets, allFiles, index, file, ordering)
            }

            sets.each { suffix, subset ->
                BidsLogger.logProgress(getLogGroup(), "Set for suffix '${suffix}' has ${subset.size()} items")
                def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(getSetName(), suffix, suffixMapping)

                def suffixConfig = config.get(configKey) as Map
                def setConfig = getSetConfig(suffixConfig)

                // Only validate if requiredGroups is explicitly defined and non-empty
                if (setConfig?.required && !(setConfig.required as List).isEmpty()) {
                    def requiredFields = setConfig.required as List<String>
                    def missingFields = requiredFields.findAll { !subset.containsKey(it) }

                    if (missingFields) {
                        BidsLogger.logProgress(getLogGroup(), "Suffix ${suffix} missing required groups: ${missingFields}")
                        sets.remove(suffix)
                        return
                    }
                }

                // If it survived, sort it, if needed be !
                if (subset.entities) {
                    if (subset.entities.size() == 1) {
                        subset.filter { name, val -> !['entities', 'order'].contains(name) }
                            .each { subName, items ->
                                if (items instanceof List) {
                                    sets[suffix][subName] = items.sort { a, b ->
                                        compareSequenceValues(a.sequenceValues, b.sequenceValues)
                                    }*.file
                                }
                            }
                    } else if (subset.order == 'flat') {
                        sets[suffix] = applyFlatOrdering(subset.files)
                    } else {
                        sets[suffix] = applyHierarchicalOrdering(subset.files)
                    }
                }
            }

            def channelData = processGroup(datasetRoot, sets, allFiles, loopOverEntities)

            if (channelData) {
                results << channelData
                processedCount++
            } else {
                filteredCount++
            }
        }

        // Return unbound queue - will be consumed by transferQueueItems
        logProcessingStats(processedCount, filteredCount)

        return results
    }

    protected List<BidsFile> applyFlatOrdering(Map files) {
        return files.sort { a, b ->
            compareSequenceValues(a.sequenceValues, b.sequenceValues)
        }*.file
    }

    protected List<BidsFile> applyHierarchicalOrdering(Map files) {
        return files.sort { a, b ->
            compareSequenceValues(a.sequenceValues, b.sequenceValues)
        }*.file
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
    protected abstract String getSetName()

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
     * @return Set index
     */
    protected abstract Map getSetIndex(BidsFile file, Map setConfig)

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
     * @param loopOverEntities Entities to group by
     * @return BidsChannelData for the group or null if filtered out
     */
    protected abstract BidsChannelData processGroup(
        String datasetRoot,
        Map plainSets,
        Map allFiles,
        List<String> loopOverEntities,
        Map suffixMapping
    )

    protected String getLogGroup() {
        return "nf-bids-${getSetName()}"
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
        def suffix = file.suffix
        if (!suffix) {
            BidsLogger.logProgress(getLogGroup(), "Skipping file without suffix: ${file.path}")
            return
        }

        // Resolve config key using suffix mapping
        def configKey = SuffixMapper.resolveConfigKey(getSetName(), suffix, suffixMapping)
        BidsLogger.logProgress(getLogGroup(), "Resolved config key for suffix '${suffix}': ${configKey}")

        def suffixConfig = config.get(configKey) as Map
        if (!suffixConfig || !(suffixConfig instanceof Map)) {
            BidsLogger.logProgress(getLogGroup(), "No configuration for suffix: ${suffix} - FILTERED")
            return
        }

        // Check for set type configuration
        def setConfig = getSetConfig(suffixConfig as Map)
        if (setConfig == null) {
            BidsLogger.logProgress(getLogGroup(), "No configuration for suffix: ${suffix} - FILTERED")
            return
        }

        BidsLogger.logProgress(getLogGroup(), "Found configuration for ${suffix}")

        // Validate entity matching if filter specified
        if (setConfig.filter) {
            if (!BidsEntityUtils.entitiesMatch(file.entities, setConfig.filter as List)) {
                BidsLogger.logProgress(getLogGroup(), "File filtered by entity pattern: ${file.path}")
                return
            }
        }

        // Exclude files with specific entities if specified
        if (setConfig.exclude_entities) {
            for (String entityName : setConfig.exclude_entities as List<String>) {
                String normalizedEntity = BidsEntity.normalizeName(entityName)
                if (file.hasEntity(normalizedEntity)) {
                    BidsLogger.logProgress(getLogGroup(), "File excluded by entity ${entityName}: ${file.path}")
                    return
                }
            }
        }

        // Validate required entities are present
        List<String> requiredEntities = setConfig.required_entities as List<String>
        if (requiredEntities && !BidsEntityUtils.hasRequiredEntities(file, requiredEntities)) {
            BidsLogger.logProgress(getLogGroup(), "File missing required entities: ${file.path}")
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
        def sequenceByEntities = getSequenceByEntities(setConfig)
        if (!sequenceByEntities || sequenceByEntities.isEmpty()) {
            BidsLogger.logProgress(getLogGroup(), "Sequence config missing entities for suffix: ${file.suffix}")
            return
        }

        // Extract values for all sequence entities
        // Normalize entity names: config uses long names (inversion),
        // but BidsFile stores short names (inv)
        def sequenceValues = sequenceByEntities.collect { entity ->
            file.getEntityValue(BidsEntity.normalizeName(entity))
        }

        // Skip if any required sequence entity is missing
        if (sequenceValues.any { it == null }) {
            BidsLogger.logProgress(getLogGroup(), "File missing required sequence entities: ${file.filename}")
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
        return files.findAll { it.suffix == suffix }
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

        BidsLogger.logProgress(getLogGroup(), "${processedCount} emitted, ${filteredCount} filtered")

        if (filteredCount > 0) {
            def filterRatio = (filteredCount * 100) / (processedCount + filteredCount)
            BidsLogger.logProgress(getLogGroup(), "Filter ratio: ${filterRatio.round(1)}%")
        }
    }

}
