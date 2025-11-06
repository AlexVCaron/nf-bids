package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsEntityUtils
import nfneuro.plugin.util.BidsLogger
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Handler for mixed BIDS sets
 *
 * Processes combined named and sequential groupings with nested structures.
 * Mixed sets use pattern-based matching for the named dimension (like NamedSetHandler)
 * and sequential ordering for the sequential dimension.
 * Creates structure: suffix -> {groupName -> [orderedFiles]}
 *
 * Example configuration:
 * MPM:
 *   mixed_set:
 *     named_dimension: "acquisition"
 *     sequential_dimension: "echo"
 *     named_groups:
 *       MTw: {acquisition: "acq-MTw", flip: "flip-1", mtransfer: "mt-on"}
 *       PDw: {acquisition: "acq-PDw", flip: "flip-1", mtransfer: "mt-off"}
 *       T1w: {acquisition: "acq-T1w", flip: "flip-2", mtransfer: "mt-off"}
 *     required: ["MTw", "PDw", "T1w"]
 *
 * @reference Mixed set implementation:
 *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class MixedSetHandler extends BaseSetHandler {

    /**
     * Build nested map for files grouping by extension type
     */
    private static Map<String, List<String>> nestedMapForFiles(
        String datasetRoot,
        List<BidsFile> files,
        List<BidsFile> allFiles
    ) {
        Map nestedMap = [:]

        files.each { file ->
            String baseName = file.getBasename()

            // Find all related files with same base name
            allFiles.each { relatedFile ->
                if (relatedFile.getBasename() == baseName) {
                    String type = relatedFile.getType()
                    String relativePath = relatedFile.relativeTo(datasetRoot)

                    if (!nestedMap[type]) {
                        nestedMap[type] = []
                    }
                    if (!nestedMap[type].contains(relativePath)) {
                        nestedMap[type] << relativePath
                    }
                }
            }
        }

        return nestedMap
    }

    @Override
    protected String getSetName() {
        return "mixed-set"
    }

    /**
     * Process a group for mixed set configuration
     *
     * Handles nested named groups (via pattern matching) containing sequential arrays
     *
     * @param filesInGroup Files in this grouping key group
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData or null
     *
     * @reference Mixed set processing:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L56-L70
     */
    @Override
    protected BidsChannelData processGroup(
            String datasetRoot,
            List<BidsFile> filesInGroup,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {

        // Organize files by suffix -> groupName -> sequential array
        def mixedSets = [:].withDefault { [:].withDefault { [] } }
        def allFiles = []

        filesInGroup.each { file ->
            def suffix = file.suffix
            if (!suffix) return

            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

            def suffixConfig = config.get(configKey) as Map
            if (!suffixConfig) return

            def mixedSetConfig = getSetConfig(suffixConfig)
            if (!mixedSetConfig) return

            // Use pattern matching to find which named group this file belongs to
            def groupName = findMatchingMixedGroupName(file, mixedSetConfig)
            if (!groupName) {
                BidsLogger.logProgress(getLogGroup(), "File does not match any named group patterns: ${file.filename}")
                return
            }

            // Get sequential dimension entity for ordering
            String sequenceByEntity = getSequenceByEntity(mixedSetConfig)
            if (!sequenceByEntity) {
                BidsLogger.logProgress(getLogGroup(), "Mixed set config missing sequential dimension for suffix: ${suffix}")
                return
            }

            String sequenceValue = file.getEntityValue(sequenceByEntity)
            if (!sequenceValue) {
                BidsLogger.logProgress(getLogGroup(), "File missing sequential entity '${sequenceByEntity}': ${file.filename}")
                return
            }

            // Apply entity filters if specified
            if (mixedSetConfig.filter) {
                if (!BidsEntityUtils.entitiesMatch(file.entities, mixedSetConfig.filter as List)) {
                    BidsLogger.logProgress(getLogGroup(), "File filtered by pattern: ${file.filename}")
                    return
                }
            }

            // Add to mixed set structure with sequence value for sorting
            mixedSets[suffix][groupName] << [
                file: file,
                sequenceValue: sequenceValue
            ]
            allFiles << file
        }

        if (mixedSets.isEmpty()) {
            return null
        }

        // Validate required groups are present for each suffix
        mixedSets.each { suffix, groups ->
            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

            def suffixConfig = config.get(configKey) as Map
            def mixedSetConfig = getSetConfig(suffixConfig)

            // Only validate if requiredGroups is explicitly defined and non-empty
            if (mixedSetConfig?.required && !(mixedSetConfig.required as List).isEmpty()) {
                def requiredGroups = mixedSetConfig.required as List<String>
                def missingGroups = requiredGroups.findAll { !groups.containsKey(it) }

                if (missingGroups) {
                    BidsLogger.logProgress(getLogGroup(), "Suffix ${suffix} missing required groups: ${missingGroups}")
                    mixedSets.remove(suffix)
                    return
                }
            }
        }

        if (mixedSets.isEmpty()) {
            BidsLogger.logProgress(getLogGroup(), "No complete mixed sets after required group validation")
            return null
        }

        // Sort each sequential array within each named group
        mixedSets.each { suffix, groups ->
            groups.each { groupName, items ->
                mixedSets[suffix][groupName] = items.sort { a, b ->
                    compareSequenceValues(a.sequenceValue, b.sequenceValue)
                }.collect { it.file }
            }
        }

        // Build grouping key
        def groupingKey = BidsEntityUtils.groupingKey(filesInGroup[0], loopOverEntities)

        // Create channel data
        def channelData = new BidsChannelData()

        // Add suffix data as maps of {groupName -> {extension: [paths]}}
        mixedSets.each { suffix, groups ->
            def groupMap = groups.collectEntries { groupName, files ->
                [groupName, nestedMapForFiles(datasetRoot, files, allFiles)]
            }
            channelData.addSuffixData(suffix, groupMap)
        }

        // Add all file paths (with relative paths)
        allFiles.each { file ->
            channelData.addFilePath(file.relativeTo(datasetRoot))
        }

        // Add entities from the first file (all files in group should have same loop-over entities)
        if (filesInGroup) {
            filesInGroup[0].entities.each { entity ->
                channelData.addEntity(entity.name, entity.value)
            }
        }

        BidsLogger.logProgress(getLogGroup(), "Mixed set emitted with ${mixedSets.size()} suffixes, key: ${groupingKey}")

        return channelData
    }

    /**
     * Find which named group a file belongs to using pattern matching
     *
     * Iterates through named_groups and checks if all entity patterns match.
     * This is identical to named set pattern matching.
     *
     * @param file The BIDS file to match
     * @param mixedSetConfig The mixed_set configuration map
     * @return The matching group name (e.g., "MTw", "PDw", "T1w") or null
     *
     * @reference findMatchingMixedGrouping function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L56-L70
     */
    private String findMatchingMixedGroupName(BidsFile file, Map mixedSetConfig) {
        def namedGroups = mixedSetConfig.named_groups as Map
        if (!namedGroups) {
            BidsLogger.logProgress(getLogGroup(), "Mixed set config missing 'named_groups'")
            return null
        }

        // Check if a specific dimension is configured for named grouping
        def namedDim = getNamedDimension(mixedSetConfig)

        // Iterate through named group definitions (MTw, PDw, T1w, etc.)
        for (def entry : namedGroups.entrySet()) {
            def groupName = entry.key
            def patternMap = entry.value

            // Skip special configuration keys
            if (groupName == 'required' || groupName == 'description' || !(patternMap instanceof Map)) {
                continue
            }

            // If named_dimension is specified, only check that one entity
            // Otherwise, check ALL entity patterns (backward compatible)
            def entitiesToCheck = namedDim ?
                [(namedDim): (patternMap as Map)[namedDim]] :
                (patternMap as Map)

            // Check if all required entity patterns match this file
            boolean allMatch = true
            for (def pattern : entitiesToCheck.entrySet()) {
                def configEntityName = pattern.key
                def expectedValue = pattern.value

                // Skip if entity not in pattern (when filtering by named_dimension)
                if (expectedValue == null) {
                    continue
                }

                // Skip description field (documentation only)
                if (configEntityName == 'description') {
                    continue
                }

                // Normalize entity name: config might use long names (mtransfer),
                // but BidsFile stores short names (mt)
                def entityName = BidsEntity.normalizeName(configEntityName)
                def cleanValue = BidsEntity.sanitizeValue(expectedValue as String)

                // Check if values match (with normalization)
                if (file.getEntity(entityName) != new BidsEntity(entityName, cleanValue)) {
                    allMatch = false
                    break
                }
            }

            if (allMatch) {
                return groupName  // Found matching group!
            }
        }

        return null  // No matching group found
    }

    /**
     * Get sequential_dimension entity from config
     *
     * @param config Mixed set configuration
     * @return Entity name to sequence by
     */
    private String getSequenceByEntity(Map config) {
        if (config.sequential_dimension) {
            if (BidsEntity.longEntityExists(config.sequential_dimension as String)) {
                return config.sequential_dimension as String
            }
            BidsLogger.logProgress(getLogGroup(), "Sequential dimension entity '${config.sequential_dimension}' is not a valid BIDS entity.")
        }
        return null
    }

    /**
     * Get named dimension entity from config
     *
     * The named_dimension specifies which entity should be used for pattern-based
     * grouping in mixed sets. If not specified, pattern matching applies to all entities.
     *
     * @param config Mixed set configuration
     * @return Entity name for named grouping, or null to use all entities
     */
    private String getNamedDimension(Map config) {
        if (config.named_dimension) {
            if (BidsEntity.longEntityExists(config.named_dimension as String)) {
                return config.named_dimension as String
            }
            BidsLogger.logProgress(getLogGroup(), "Named dimension entity '${config.named_dimension}' is not a valid BIDS entity.")
        }
        return null  // Pattern match on all entities
    }

    /**
     * Compare sequence values for sorting
     *
     * @param a First value
     * @param b Second value
     * @return Comparison result
     *
     * @reference Sequence comparison from:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L60-L75
     */
    private int compareSequenceValues(Object a, Object b) {
        try {
            def numA = a as Double
            def numB = b as Double
            return numA <=> numB
        } catch (Exception e) {
            return a.toString() <=> b.toString()
        }
    }

    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.mixed_set as Map
    }

}
