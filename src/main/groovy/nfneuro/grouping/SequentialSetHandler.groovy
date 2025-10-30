package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
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
    
    /**
     * Get the base name of a file without extension
     * e.g., /path/to/sub-01_echo-01_T1w.nii.gz -> sub-01_echo-01_T1w
     */
    private static String getBaseName(String filePath) {
        def filename = new File(filePath).name
        return filename.replaceAll(/\.(nii\.gz|nii|json|tsv|bval|bvec|txt|edf|eeg)$/, '')
    }
    
    /**
     * Get extension type for categorization
     * e.g., .nii.gz -> 'nii', .json -> 'json', .bval -> 'bval'
     */
    private static String getExtensionType(String filePath) {
        if (filePath.endsWith('.nii.gz')) return 'nii'
        if (filePath.endsWith('.nii')) return 'nii'
        if (filePath.endsWith('.json')) return 'json'
        if (filePath.endsWith('.tsv')) return 'tsv'
        if (filePath.endsWith('.bval')) return 'bval'
        if (filePath.endsWith('.bvec')) return 'bvec'
        if (filePath.endsWith('.txt')) return 'txt'
        if (filePath.endsWith('.edf')) return 'edf'
        if (filePath.endsWith('.eeg')) return 'eeg'
        return 'other'
    }
    
    /**
     * Convert absolute path to relative path from dataset root
     */
    private static String makeRelativePath(String absolutePath) {
        def pathParts = absolutePath.split('/')
        def datasetIndex = -1
        for (int i = 0; i < pathParts.length; i++) {
            if (pathParts[i] in ['custom', 'bids-examples']) {
                datasetIndex = i + 1
                break
            }
        }
        if (datasetIndex > 0 && datasetIndex < pathParts.length) {
            return pathParts[datasetIndex..-1].join('/')
        }
        return absolutePath
    }
    
    /**
     * Build nested map for a single file grouping by extension type
     */
    private Map<String, String> buildNestedMapForFile(BidsFile file, List<BidsFile> allFiles) {
        def baseName = getBaseName(file.path)
        def nestedMap = [:]
        
        allFiles.each { relatedFile ->
            if (getBaseName(relatedFile.path) == baseName) {
                def extensionType = getExtensionType(relatedFile.path)
                def relativePath = makeRelativePath(relatedFile.path)
                nestedMap[extensionType] = relativePath
            }
        }
        
        return nestedMap
    }
    
    @Override
    DataflowQueue process(
            List<BidsFile> bidsFiles,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {
        
        BidsLogger.debug("Processing sequential sets with ${bidsFiles.size()} files")
        BidsLogger.debug("Loop-over entities: ${loopOverEntities}")
        
        def results = new DataflowQueue()
        def processedCount = 0
        def filteredCount = 0
        
        // Group files by loop-over entities first
        def filesByGroup = groupFilesByEntities(bidsFiles, loopOverEntities)
        
        // Process each group
        filesByGroup.each { groupKey, filesInGroup ->
            def channelData = processSequentialSetGroup(filesInGroup, config, loopOverEntities, suffixMapping)
            
            if (channelData) {
                results.bind(channelData)  // Use .bind() not << to match nf-sqldb pattern
                processedCount++
            } else {
                filteredCount++
            }
        }
        
        logProcessingStats("Sequential set", processedCount, filteredCount)
        
        return results
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
    private BidsChannelData processSequentialSetGroup(
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
                BidsLogger.warn("Sequential set config missing sequence entities for suffix: ${suffix}")
                return
            }
            
            // Extract values for all sequence entities
            // Normalize entity names: config uses long names (inversion), 
            // but BidsFile stores short names (inv)
            def sequenceValues = sequenceByEntities.collect { entity ->
                def normalizedEntity = normalizeEntityName(entity)
                file.getEntity(normalizedEntity)
            }
            
            // Skip if any required sequence entity is missing
            if (sequenceValues.any { it == null }) {
                BidsLogger.trace("File missing required sequence entities: ${file.filename}")
                return
            }
            
            // Apply entity filters if specified
            if (sequentialSetConfig.filter) {
                def filterMap = sequentialSetConfig.filter as Map
                if (!BidsEntityUtils.matchesPattern(file, filterMap)) {
                    BidsLogger.trace("File filtered by pattern: ${file.filename}")
                    return
                }
            }
            
            // Exclude files with specific entities if specified
            if (sequentialSetConfig.exclude_entities) {
                def excludeList = sequentialSetConfig.exclude_entities as List<String>
                for (String entityName : excludeList) {
                    def normalizedEntity = normalizeEntityName(entityName)
                    def entityValue = file.getEntity(normalizedEntity)
                    if (entityValue && entityValue != "NA") {
                        BidsLogger.trace("File excluded by entity ${entityName}: ${file.filename}")
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
                    sequentialSets[suffix] = buildSequenceWithParts(files, partsConfig, allFiles)
                } else {
                    // Build nested map structure {nii: [...], json: [...]}
                    def sortedFiles = files.sort { a, b ->
                        compareSequenceValues(a.sequenceValues[0], b.sequenceValues[0])
                    }
                    sequentialSets[suffix] = buildNestedSequenceMap(sortedFiles.collect { it.file }, allFiles)
                }
                
            } else {
                // Multi-entity: hierarchical nested arrays or flat nested arrays
                if (order == 'flat') {
                    sequentialSets[suffix] = buildFlatSequence(files, entities, partsConfig)
                } else {
                    // Build hierarchical nested arrays grouped by extension
                    sequentialSets[suffix] = buildHierarchicalSequence(files, entities, partsConfig, allFiles)
                }
            }
        }
        
        // Build grouping key
        def groupingKey = BidsEntityUtils.createGroupingKey(filesInGroup[0], loopOverEntities)
        
        // Create channel data
        def channelData = new BidsChannelData()
        
        // Add suffix data (arrays or nested structures)
        sequentialSets.each { suffix, data ->
            channelData.addSuffixData(suffix, data)
        }
        
        // Add all file paths
        allFiles.each { file ->
            channelData.addFilePath(makeRelativePath(file.path))
        }
        
        // Add entities from the first file
        if (filesInGroup) {
            filesInGroup[0].entities.each { k, v ->
                channelData.addEntity(k, v)
            }
        }
        
        BidsLogger.trace("Sequential set emitted with ${sequentialSets.size()} suffixes, key: ${groupingKey}")
        
        return channelData
    }
    
    /**
     * Build nested map structure for sequential files
     * Groups files by extension type: {nii: [...], json: [...]}
     */
    private Map<String, List<String>> buildNestedSequenceMap(List<BidsFile> files, List<BidsFile> allFiles) {
        def nestedMap = [:]
        
        files.each { file ->
            def baseName = getBaseName(file.path)
            
            // Find all related files with same base name
            allFiles.each { relatedFile ->
                if (getBaseName(relatedFile.path) == baseName) {
                    def extensionType = getExtensionType(relatedFile.path)
                    def relativePath = makeRelativePath(relatedFile.path)
                    
                    if (!nestedMap[extensionType]) {
                        nestedMap[extensionType] = []
                    }
                    if (!nestedMap[extensionType].contains(relativePath)) {
                        nestedMap[extensionType] << relativePath
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
    private Map buildSequenceWithParts(List files, List<String> partsConfig, List<BidsFile> allFiles) {
        // Group by sequence value, then by part (only for files WITH parts)
        def grouped = [:]
        def jsonFiles = []
        
        files.each { item ->
            def file = item.file as BidsFile
            def seqValue = item.sequenceValues[0]
            def partValue = file.getEntity("part")?.replaceFirst(/^part-/, '')
            
            if (partValue && partValue != "NA" && partsConfig.contains(partValue)) {
                // This is a NII file with part
                if (!grouped.containsKey(seqValue)) {
                    grouped[seqValue] = [:]
                }
                grouped[seqValue][partValue] = file.path
            } else if ((!partValue || partValue == "NA") && file.path.endsWith('.json')) {
                // This is a JSON file without part - add to json list
                jsonFiles << makeRelativePath(file.path)
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
                return partsMap.collectEntries { partName, path ->
                    [partName, makeRelativePath(path)]
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
    private Map buildHierarchicalSequence(List files, List<String> entities, List<String> partsConfig, List<BidsFile> allFiles) {
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
                getBaseName((item.file as BidsFile).path)
            }.unique()
            
            // For each unique base name, find all related files by extension
            def filesInGroup = [:].withDefault { [] }
            
            baseNamesInGroup.each { baseName ->
                // Find all related files with same base name
                allFiles.each { relatedFile ->
                    if (getBaseName(relatedFile.path) == baseName) {
                        def extensionType = getExtensionType(relatedFile.path)
                        def relativePath = makeRelativePath(relatedFile.path)
                        
                        // Avoid duplicates
                        if (!filesInGroup[extensionType].contains(relativePath)) {
                            filesInGroup[extensionType] << relativePath
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
     * @param entities Entity names in order
     * @param partsConfig Optional list of part values to group
     * @return Nested array structure
     * 
     * @reference Flat structure building:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf#L190-L210
     */
    private List buildFlatSequence(List files, List<String> entities, List<String> partsConfig = null) {
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
                    def partValue = file.getEntity("part")?.replaceFirst(/^part-/, '')
                    
                    if (partValue && partsConfig.contains(partValue)) {
                        if (!innerGrouped.containsKey(seqKey)) {
                            innerGrouped[seqKey] = [:]
                        }
                        innerGrouped[seqKey][partValue] = file.path
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
                }.collect { it.file.path }
            }
        }
    }
    
    /**
     * Get sequence entities from config (single or multiple)
     * 
     * Supports 'sequential_dimension', 'sequence_by', 'by_entity', and 'by_entities' field names
     * Priority: by_entities > sequential_dimension > sequence_by > by_entity
     * 
     * @param config Sequential set configuration
     * @return List of entity names to sequence by
     */
    private List<String> getSequenceByEntities(Map config) {
        // Priority 1: Multiple entities via by_entities
        if (config.by_entities && config.by_entities instanceof List) {
            return config.by_entities as List<String>
        }
        // Priority 2: sequential_dimension (bids2nf.yaml standard for single entity)
        if (config.sequential_dimension) {
            return [config.sequential_dimension as String]
        }
        // Priority 3: Legacy sequence_by
        if (config.sequence_by) {
            return [config.sequence_by as String]
        }
        // Priority 4: by_entity
        if (config.by_entity) {
            return [config.by_entity as String]
        }
        return []
    }
    
    /**
     * Compare sequence values for sorting
     * 
     * Handles numeric and string sequence values
     * 
     * @param a First sequence value
     * @param b Second sequence value
     * @return Comparison result
     * 
     * @reference Sequence sorting logic from: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf#L40-L60
     */
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
