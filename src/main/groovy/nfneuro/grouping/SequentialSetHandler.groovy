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
 * Handler for sequential BIDS sets
 *
 * Processes sequential groupings where files are ordered by one or more sequence entities.
 * Supports both single-entity (flat array) and multi-entity (nested arrays) sequencing.
 *
 * Single-entity example (by_entity: "echo"):
 *   VFA -> [echo-1_file, echo-2_file, echo-3_file]
 *
 * Multi-entity hierarchical example (by_entities: [flip, inversion]):
 *   TB1SRGE -> {flip-1: {inv-1: file, inv-2: file}, flip-2: {inv-1: file, inv-2: file}}
 *
 * Multi-entity flat example (by_entities: [echo, flip], order: flat):
 *   TB1EPI -> [[echo-1_flip-1, echo-1_flip-2], [echo-2_flip-1, echo-2_flip-2]]
 *
 * @reference Sequential set implementation:
 *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class SequentialSetHandler extends BaseSetHandler {

    @Override
    protected String getSetName() {
        return "sequential_set"
    }

    /**
     * Process a group for sequential set configuration
     *
     * Orders files by one or more sequence entities (e.g., echo, flip angle, inversion time).
     * Supports hierarchical (nested maps) or flat (nested arrays) output formats.
     *
     * @param filesInGroup Files in this grouping key group
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData or null
     *
     * @reference Sequential set processing:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf#L95-L230
     */
    @Override
    protected BidsChannelData processGroup(
            String datasetRoot,
            List<BidsFile> filesInGroup,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {

        // Organize files by suffix into sequential arrays or hierarchical structures
        def sequentialSets = [:]
        def allFiles = []

        filesInGroup.each { file ->
            def suffix = file.suffix
            if (!suffix) return

            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

            def suffixConfig = config.get(configKey) as Map
            if (!suffixConfig) return

            def sequentialSetConfig = getSetConfig(suffixConfig)
            if (!sequentialSetConfig) return

            // Get the entities to sequence by (single or multiple)
            def sequenceByEntities = getSequenceByEntities(sequentialSetConfig)
            if (!sequenceByEntities || sequenceByEntities.isEmpty()) {
                BidsLogger.logProgress(getLogGroup(), "Sequential set config missing sequence entities for suffix: ${suffix}")
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

            // Apply entity filters if specified
            if (sequentialSetConfig.filter) {
                if (!BidsEntityUtils.entitiesMatch(file.entities, sequentialSetConfig.filter as List)) {
                    BidsLogger.logProgress(getLogGroup(), "File filtered by pattern: ${file.filename}")
                    return
                }
            }

            // Exclude files with specific entities if specified
            if (sequentialSetConfig.exclude_entities) {
                for (String entityName : sequentialSetConfig.exclude_entities as List<String>) {
                    String normalizedEntity = BidsEntity.normalizeName(entityName)
                    String entityValue = file.getEntityValue(normalizedEntity)
                    if (entityValue && entityValue != "NA") {
                        BidsLogger.logProgress(getLogGroup(), "File excluded by entity ${entityName}: ${file.filename}")
                        return
                    }
                }
            }

            // Initialize suffix entry if needed
            if (!sequentialSets.containsKey(suffix)) {
                sequentialSets[suffix] = [
                    files: [],
                    entities: sequenceByEntities,
                    order: sequentialSetConfig.order ?: 'hierarchical'
                ]
            }

            // Add file with its sequence values
            sequentialSets[suffix].files << [
                file: file,
                sequenceValues: sequenceValues
            ]
            allFiles << file
        }

        if (sequentialSets.isEmpty()) {
            return null
        }

        // Build the final structure for each suffix
        sequentialSets.each { suffix, setData ->
            def files = setData.files as List
            def entities = setData.entities as List<String>
            def order = setData.order as String

            // Get parts configuration if specified
            def suffixConfig = config.get(SuffixMapper.resolveConfigKey(suffix, suffixMapping))
            def sequentialSetConfig = getSetConfig(suffixConfig)
            def partsConfig = sequentialSetConfig?.parts as List<String>

            if (entities.size() == 1) {
                // Single-entity: simple sorted array (or parts map)
                if (partsConfig) {
                    sequentialSets[suffix] = sequenceWithParts(datasetRoot, files, partsConfig)
                } else {
                    // Build nested map structure {nii: [...], json: [...]}
                    def sortedFiles = files.sort { a, b ->
                        compareSequenceValues(a.sequenceValues[0], b.sequenceValues[0])
                    }
                    sequentialSets[suffix] = nestedSequenceMap(datasetRoot, sortedFiles*.file, allFiles)
                }
            } else {
                // Multi-entity: hierarchical nested arrays or flat nested arrays
                if (order == 'flat') {
                    sequentialSets[suffix] = flatSequence(datasetRoot, files, partsConfig)
                } else {
                    // Build hierarchical nested arrays grouped by extension
                    sequentialSets[suffix] = hierarchicalSequence(datasetRoot, files, allFiles)
                }
            }
        }

        // Build grouping key
        def groupingKey = BidsEntityUtils.groupingKey(filesInGroup[0], loopOverEntities)

        // Create channel data
        def channelData = new BidsChannelData()

        // Add suffix data (arrays or nested structures)
        sequentialSets.each { suffix, data ->
            channelData.addSuffixData(suffix, data)
        }

        // Add all file paths
        allFiles.each { file ->
            channelData.addFilePath(file.relativeTo(datasetRoot))
        }

        // Add entities from the first file
        if (filesInGroup) {
            filesInGroup[0].entities.each { entity ->
                channelData.addEntity(entity.name, entity.value)
            }
        }

        BidsLogger.logProgress(getLogGroup(), "Sequential set emitted with ${sequentialSets.size()} suffixes, key: ${groupingKey}")

        return channelData
    }

    /**
     * Build nested map structure for sequential files
     * Groups files by extension type: {nii: [...], json: [...]}
     */
    private Map<String, List<String>> nestedSequenceMap(
        String datasetRoot,
        List<BidsFile> files,
        List<BidsFile> allFiles
    ) {
        def nestedMap = [:]

        files.each { file ->
            def baseName = file.getBasename()

            // Find all related files with same base name
            allFiles.each { relatedFile ->
                if (relatedFile.getBasename() == baseName) {
                    def type = relatedFile.getType()
                    def relativePath = relatedFile.relativeTo(datasetRoot)

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

    /**
     * Build sequence with parts for single-entity sequences
     *
     * Example output: {json: ["file1.json", "file2.json"], nii: [{mag: "file1.nii", phase: "file2.nii"}, {mag: "file3.nii", phase: "file4.nii"}]}
     *
     * @param files List of file data
     * @param partsConfig List of part values (e.g., ["mag", "phase"])
     * @param allFiles All files for finding related files
     * @return Map of extension type to list (nii is list of part maps, others are simple lists)
     */
    private Map sequenceWithParts(String datasetRoot, List files, List<String> partsConfig) {
        // Group by sequence value, then by part (only for files WITH parts)
        def grouped = [:]
        def jsonFiles = []

        files.each { item ->
            def file = item.file as BidsFile
            def seqValue = item.sequenceValues[0]
            def partValue = file.getEntityValue("part")?.replaceFirst(/^part-/, '')

            if (partValue && partValue != "NA" && partsConfig.contains(partValue)) {
                // This is a NII file with part
                if (!grouped.containsKey(seqValue)) {
                    grouped[seqValue] = [:]
                }
                grouped[seqValue][partValue] = file
            } else if ((!partValue || partValue == "NA") && file.getExtensionType() == 'json') {
                // This is a JSON file without part - add to json list
                jsonFiles << file.relativeTo(datasetRoot)
            }
        }

        // Filter out incomplete sets and build sorted list of part maps for NII
        def niiSequence = grouped.keySet().sort { a, b ->
            compareSequenceValues(a, b)
        }.collect { seqValue ->
            def partsMap = grouped[seqValue]
            // Only include if all parts present
            if (partsMap.keySet().containsAll(partsConfig)) {
                // Convert absolute paths to relative
                return partsMap.collectEntries { partName, file ->
                    [partName, file.relativeTo(datasetRoot)]
                }
            }
            return null
        }.findAll { it != null }

        // Build result map with extension types
        def result = [:]

        // Add nii with parts structure
        if (!niiSequence.isEmpty()) {
            result.nii = niiSequence
        }

        // Add JSON files (already collected above)
        if (!jsonFiles.isEmpty()) {
            result.json = jsonFiles.sort()
        }

        return result
    }

    /**
     * Build hierarchical nested array structure for multi-entity sequences
     *
     * Baseline format: Nested arrays grouped by extension type
     * Example for [echo, flip]:
     *   {
     *     json: [
     *       ["echo-1_flip-1.json", "echo-1_flip-2.json"],  // First outer entity
     *       ["echo-2_flip-1.json", "echo-2_flip-2.json"]   // Second outer entity
     *     ],
     *     nii: [
     *       ["echo-1_flip-1.nii", "echo-1_flip-2.nii"],
     *       ["echo-2_flip-1.nii", "echo-2_flip-2.nii"]
     *     ]
     *   }
     *
     * @param files List of file data with sequence values (only primary files)
     * @param entities Entity names in order (e.g., [echo, flip])
     * @param partsConfig Optional list of part values to group (e.g., ["mag", "phase"])
     * @param allFiles All files for finding related extensions
     * @return Map of extension type to nested arrays
     *
     * @reference Hierarchical structure building:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf#L145-L255
     */
    private Map hierarchicalSequence(String datasetRoot, List files, List<BidsFile> allFiles) {
        // Group files by outer entity value and collect all related files
        def outerGroups = files.groupBy { item ->
            (item.sequenceValues as List)[0]
        }

        // Sort outer groups
        def sortedOuterKeys = outerGroups.keySet().sort { a, b ->
            compareSequenceValues(a, b)
        }

        // Build structure: {extension: [[outer1_files...], [outer2_files...]]}
        def result = [:].withDefault { [] }

        sortedOuterKeys.each { outerKey ->
            def innerFiles = outerGroups[outerKey]

            // Sort inner files by remaining sequence entities
            def sortedInner = innerFiles.sort { a, b ->
                def aValues = (a.sequenceValues as List)[1..-1]
                def bValues = (b.sequenceValues as List)[1..-1]
                for (int i = 0; i < aValues.size(); i++) {
                    def cmp = compareSequenceValues(aValues[i], bValues[i])
                    if (cmp != 0) return cmp
                }
                return 0
            }

            // Collect unique base names in this group to avoid duplicates
            def baseNamesInGroup = sortedInner.collect { item ->
                (item.file as BidsFile).getBasename()
            }.unique()

            // For each unique base name, find all related files by extension
            def filesInGroup = [:].withDefault { [] }

            baseNamesInGroup.each { baseName ->
                // Find all related files with same base name
                allFiles.each { relatedFile ->
                    if (relatedFile.getBasename() == baseName) {
                        def type = relatedFile.getType()
                        def relativePath = relatedFile.relativeTo(datasetRoot)

                        // Avoid duplicates
                        if (!filesInGroup[type].contains(relativePath)) {
                            filesInGroup[type] << relativePath
                        }
                    }
                }
            }

            // Add this outer group's files to result
            filesInGroup.each { extensionType, paths ->
                result[extensionType] << paths
            }
        }

        return result
    }

    /**
     * Filter out incomplete parts from nested map structure
     *
     * Recursively removes entries that don't have all required parts
     *
     * @param map Nested map structure
     * @param partsConfig Required parts (e.g., ["mag", "phase"])
     * @return Filtered map with only complete parts
     */
    private Map filterIncompleteParts(Map map, List<String> partsConfig) {
        def filtered = [:]

        map.each { key, value ->
            if (value instanceof Map) {
                // Check if this is a parts map (has part keys)
                def isPartsMap = partsConfig.any { value.containsKey(it) }

                if (isPartsMap) {
                    // Validate all parts are present
                    if (partsConfig.every { value.containsKey(it) }) {
                        filtered[key] = value
                    }
                    // Skip if incomplete
                } else {
                    // Recurse deeper
                    def nestedFiltered = filterIncompleteParts(value, partsConfig)
                    if (!nestedFiltered.isEmpty()) {
                        filtered[key] = nestedFiltered
                    }
                }
            } else {
                // Leaf value (file path), keep it
                filtered[key] = value
            }
        }

        return filtered
    }

    /**
     * Recursively sort a hierarchical map by keys
     *
     * @param map Map to sort
     * @return Sorted map
     */
    private Map sortHierarchicalMap(Map map) {
        def sortedMap = map.sort { a, b ->
            compareSequenceValues(a.key, b.key)
        }

        // Recursively sort nested maps
        sortedMap.each { key, value ->
            if (value instanceof Map) {
                sortedMap[key] = sortHierarchicalMap(value as Map)
            }
        }

        return sortedMap
    }

    /**
     * Build flat nested array structure for multi-entity sequences
     *
     * Example for [echo, flip] with order: flat:
     *   [[echo-1_flip-1, echo-1_flip-2], [echo-2_flip-1, echo-2_flip-2]]
     *
     * With parts configuration:
     *   [[{mag: file1, phase: file2}, {mag: file3, phase: file4}], ...]
     *
     * @param files List of file data with sequence values
     * @param partsConfig Optional list of part values to group
     * @return Nested array structure
     *
     * @reference Flat structure building:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf#L190-L210
     */
    private List flatSequence(String datasetRoot, List files, List<String> partsConfig = null) {
        // Group by outer entity first
        def groupedByOuter = files.groupBy { item ->
            (item.sequenceValues as List)[0]
        }

        // Sort outer groups and build nested arrays
        def sortedOuterKeys = groupedByOuter.keySet().sort { a, b ->
            compareSequenceValues(a, b)
        }

        return sortedOuterKeys.collect { outerKey ->
            def innerFiles = groupedByOuter[outerKey]

            if (partsConfig) {
                // Group inner files by remaining sequence values, then by part
                def innerGrouped = [:]
                innerFiles.each { item ->
                    def file = item.file as BidsFile
                    def seqKey = (item.sequenceValues as List)[1..-1].join('_')
                    def partValue = file.getEntityValue("part")?.replaceFirst(/^part-/, '')

                    if (partValue && partsConfig.contains(partValue)) {
                        if (!innerGrouped.containsKey(seqKey)) {
                            innerGrouped[seqKey] = [:]
                        }
                        innerGrouped[seqKey][partValue] = file.relativeTo(datasetRoot)
                    }
                }

                // Sort and filter for complete parts
                return innerGrouped.keySet().sort().collect { seqKey ->
                    def partsMap = innerGrouped[seqKey]
                    if (partsConfig.every { partsMap.containsKey(it) }) {
                        return partsMap
                    }
                    return null
                }.findAll { it != null }
            } else {
                // Sort inner files by remaining entities
                return innerFiles.sort { a, b ->
                    def aValues = (a.sequenceValues as List)[1..-1]
                    def bValues = (b.sequenceValues as List)[1..-1]

                    for (int i = 0; i < aValues.size(); i++) {
                        def cmp = compareSequenceValues(aValues[i], bValues[i])
                        if (cmp != 0) return cmp
                    }
                    return 0
                }.collect { it.file.relativeTo(datasetRoot) }
            }
        }
    }

    /**
     * Get sequence entities from config (single or multiple)
     *
     * @param config Sequential set configuration
     * @return List of entity names to sequence by
     */
    private List<String> getSequenceByEntities(Map config) {
        if (config.by_entities && config.by_entities instanceof List) {
            return config.by_entities as List<String>
        }
        if (config.by_entity) {
            return [config.by_entity as String]
        }
        return []
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
    private int compareSequenceValues(Object a, Object b) {
        // Extract numeric portion from entity values (e.g., "echo-10" -> 10)
        def extractNumber = { val ->
            def str = val.toString()
            // Match pattern like "entity-123" or just "123"
            def matcher = str =~ /(\d+)$/
            if (matcher.find()) {
                try {
                    return matcher.group(1).toInteger()
                } catch (Exception e) {
                    return null
                }
            }
            return null
        }

        def numA = extractNumber(a)
        def numB = extractNumber(b)

        // If both values have numeric components, compare numerically
        if (numA != null && numB != null) {
            return numA <=> numB
        }

        // Otherwise fall back to string comparison
        return a.toString() <=> b.toString()
    }

    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.sequential_set as Map
    }

}
