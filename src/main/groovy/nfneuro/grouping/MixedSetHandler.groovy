package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
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
     * Get the base name of a file without extension
     */
    private static String getBaseName(String filePath) {
        def filename = new File(filePath).name
        return filename.replaceAll(/\.(nii\.gz|nii|json|tsv|bval|bvec|txt|edf|eeg)$/, '')
    }
    
    /**
     * Get extension type for categorization
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
     * Build nested map for files grouping by extension type
     */
    private static Map<String, List<String>> buildNestedMapForFiles(List<BidsFile> files, List<BidsFile> allFiles) {
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
    
    @Override
    DataflowQueue process(
            List<BidsFile> bidsFiles,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {
        
        BidsLogger.debug("Processing mixed sets with ${bidsFiles.size()} files")
        BidsLogger.debug("Loop-over entities: ${loopOverEntities}")
        
        def results = new DataflowQueue()
        def processedCount = 0
        def filteredCount = 0
        
        // Group files by loop-over entities first
        def filesByGroup = groupFilesByEntities(bidsFiles, loopOverEntities)
        
        // Process each group
        filesByGroup.each { groupKey, filesInGroup ->
            def channelData = processMixedSetGroup(filesInGroup, config, loopOverEntities, suffixMapping)
            
            if (channelData) {
                results.bind(channelData)  // Use .bind() not << to match nf-sqldb pattern
                processedCount++
            } else {
                filteredCount++
            }
        }
        
        // Return unbound queue - will be consumed by transferQueueItems
        logProcessingStats("Mixed set", processedCount, filteredCount)
        
        return results
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
    private BidsChannelData processMixedSetGroup(
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
                BidsLogger.trace("File does not match any named group patterns: ${file.filename}")
                return
            }
            
            // Get sequential dimension entity for ordering
            def sequenceByEntity = getSequenceByEntity(mixedSetConfig)
            if (!sequenceByEntity) {
                BidsLogger.warn("Mixed set config missing sequential dimension for suffix: ${suffix}")
                return
            }
            
            def sequenceValue = file.getEntity(sequenceByEntity)
            if (!sequenceValue) {
                BidsLogger.trace("File missing sequential entity '${sequenceByEntity}': ${file.filename}")
                return
            }
            
            // Apply entity filters if specified
            if (mixedSetConfig.filter) {
                def filterMap = mixedSetConfig.filter as Map
                if (!BidsEntityUtils.matchesPattern(file, filterMap)) {
                    BidsLogger.trace("File filtered by pattern: ${file.filename}")
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
                    BidsLogger.debug("Suffix ${suffix} missing required groups: ${missingGroups}")
                    mixedSets.remove(suffix)
                    return
                }
            }
        }
        
        if (mixedSets.isEmpty()) {
            BidsLogger.debug("No complete mixed sets after required group validation")
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
        def groupingKey = BidsEntityUtils.createGroupingKey(filesInGroup[0], loopOverEntities)
        
        // Create channel data
        def channelData = new BidsChannelData()
        
        // Add suffix data as maps of {groupName -> {extension: [paths]}}
        mixedSets.each { suffix, groups ->
            def groupMap = groups.collectEntries { groupName, files ->
                [groupName, buildNestedMapForFiles(files, allFiles)]
            }
            channelData.addSuffixData(suffix, groupMap)
        }
        
        // Add all file paths (with relative paths)
        allFiles.each { file ->
            channelData.addFilePath(makeRelativePath(file.path))
        }
        
        // Add entities from the first file (all files in group should have same loop-over entities)
        if (filesInGroup) {
            filesInGroup[0].entities.each { k, v ->
                channelData.addEntity(k, v)
            }
        }
        
        BidsLogger.trace("Mixed set emitted with ${mixedSets.size()} suffixes, key: ${groupingKey}")
        
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
            BidsLogger.warn("Mixed set config missing 'named_groups'")
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
                def entityName = normalizeEntityName(configEntityName)
                
                // Get actual entity value from file
                def actualValue = file.getEntity(entityName)
                
                // Check if values match (with normalization)
                if (!entityValuesMatch(actualValue, expectedValue as String)) {
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
     * Compare entity values with normalization
     * 
     * Handles different zero-padding formats: "flip-02" matches "flip-2"
     * Also handles prefix differences: config has "mt-on" while file stores "on"
     * 
     * @param actualValue Value from file entity (without prefix, e.g., "on")
     * @param expectedValue Value from configuration pattern (with prefix, e.g., "mt-on")
     * @return true if values match after normalization
     * 
     * @reference entityValuesMatch function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L47-L53
     */
    private boolean entityValuesMatch(String actualValue, String expectedValue) {
        if (!actualValue || !expectedValue) {
            return actualValue == expectedValue
        }
        
        // Strip prefixes from expected value if present (e.g., "mt-on" -> "on")
        def cleanExpected = expectedValue
        if (expectedValue.contains('-')) {
            def parts = expectedValue.split('-', 2)
            if (parts.length == 2) {
                cleanExpected = parts[1]  // Get part after prefix
            }
        }
        
        return normalizeEntityValue(actualValue) == normalizeEntityValue(cleanExpected)
    }
    
    /**
     * Normalize entity value for comparison
     * 
     * Removes leading zeros from numeric parts: "flip-02" → "flip-2"
     * 
     * @param value Entity value to normalize
     * @return Normalized value
     * 
     * @reference normalizeEntityValue function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L32-L46
     */
    private String normalizeEntityValue(String value) {
        if (!value || !value.contains('-')) {
            return value
        }
        
        def parts = value.split('-', 2)
        if (parts.length != 2) {
            return value
        }
        
        def prefix = parts[0]
        def suffix = parts[1]
        
        // Remove leading zeros: "flip-02" → "flip-2"
        if (suffix.isNumber()) {
            try {
                def numericSuffix = Integer.parseInt(suffix)
                return "${prefix}-${numericSuffix}"
            } catch (NumberFormatException e) {
                return value
            }
        }
        return value
    }
    
    /**
     * Get sequence_by entity from config
     * 
     * Supports 'sequential_dimension', 'sequence_by', 'by_entity', and 'by_entities' field names
     * Priority: sequential_dimension > sequence_by > by_entity > by_entities[0]
     * 
     * @param config Mixed set configuration
     * @return Entity name to sequence by
     */
    private String getSequenceByEntity(Map config) {
        // Priority 1: Check for sequential_dimension (bids2nf.yaml standard)
        if (config.sequential_dimension) {
            return config.sequential_dimension as String
        }
        // Priority 2: Legacy sequence_by
        if (config.sequence_by) {
            return config.sequence_by as String
        }
        // Priority 3: Single entity via by_entity
        if (config.by_entity) {
            return config.by_entity as String
        }
        // Priority 4: First entity from by_entities list
        if (config.by_entities && config.by_entities instanceof List) {
            return (config.by_entities as List)[0] as String
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
            return config.named_dimension as String
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
