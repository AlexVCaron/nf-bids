package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsEntityUtils
import nfneuro.plugin.util.BidsLogger
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Handler for named BIDS sets
 * 
 * Processes named groupings using pattern-based matching.
 * Files are matched to custom group names (e.g., T1w, MTw, PDw) based on 
 * entity value patterns defined in configuration.
 * 
 * Named sets create: suffix -> {groupName -> {nii: file, json: file}}
 * 
 * Example configuration:
 * MTS:
 *   named_set:
 *     T1w: {flip: "flip-02", mtransfer: "mt-off"}
 *     MTw: {flip: "flip-01", mtransfer: "mt-on"}
 *     PDw: {flip: "flip-01", mtransfer: "mt-off"}
 *   required: ["T1w", "MTw", "PDw"]
 * 
 * @reference Named set implementation: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_named_sets.nf
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class NamedSetHandler extends BaseSetHandler {
    
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
     * Build nested map for a file grouping by extension type
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
        
        BidsLogger.debug("Processing named sets with ${bidsFiles.size()} files")
        BidsLogger.logProgress("Loop-over entities: ${loopOverEntities}")
        BidsLogger.logProgress("Loop-over entities (normalized): ${loopOverEntities.collect { normalizeEntityName(it) }}")
        
        def results = new DataflowQueue()
        def processedCount = 0
        def filteredCount = 0
        
        // Group files by loop-over entities first
        def filesByGroup = groupFilesByEntities(bidsFiles, loopOverEntities)
        BidsLogger.logProgress("Created ${filesByGroup.size()} groups from ${bidsFiles.size()} files with keys: ${filesByGroup.keySet()}")
        
        // Process each group
        filesByGroup.each { groupKey, filesInGroup ->
            def channelData = processNamedSetGroup(filesInGroup, config, loopOverEntities, suffixMapping)
            
            if (channelData) {
                results.bind(channelData)  // Use .bind() not << to match nf-sqldb pattern
                processedCount++
            } else {
                filteredCount++
            }
        }
        
        logProcessingStats("Named set", processedCount, filteredCount)
        
        return results
    }
    
    /**
     * Process a group of files for named set configuration
     * 
     * Uses pattern-based matching to assign files to group names based on entity value patterns.
     * 
     * @param filesInGroup Files in this grouping key group
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData or null
     * 
     * @reference Named set grouping: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_named_sets.nf#L1-L70
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L20-L60
     */
    private BidsChannelData processNamedSetGroup(
            List<BidsFile> filesInGroup,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {
        
        // Organize files by suffix and group name using pattern matching
        def namedSets = [:].withDefault { [:] }
        def allFiles = []
        
        filesInGroup.each { file ->
            def suffix = file.suffix
            BidsLogger.logProgress("Processing file: ${file.filename} with suffix: ${suffix}")
            if (!suffix) return
            
            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])
            
            def suffixConfig = config.get(configKey) as Map
            BidsLogger.logProgress("Suffix config (key: ${configKey}): ${suffixConfig}")
            if (!suffixConfig) return
            
            def namedSetConfig = getSetConfig(suffixConfig)
            BidsLogger.logProgress("Named set config: ${namedSetConfig}")
            if (!namedSetConfig) return
            
            // Use pattern matching to find matching group name
            // Find which named group this file belongs to
            def groupName = findMatchingGroupName(file, namedSetConfig)
            BidsLogger.logProgress("Matched group name: ${groupName}")
            if (!groupName) {
                BidsLogger.trace("No matching group pattern for file: ${file.filename}")
                return
            }
            
            // Add to named set structure (only one file per group for named sets)
            if (!namedSets[suffix]) {
                namedSets[suffix] = [:]
            }
            namedSets[suffix][groupName] = file
            allFiles << file
        }
        
        if (namedSets.isEmpty()) {
            return null
        }
        
        // Validate required groups are present for each suffix
        def validSuffixes = [:]
        
        namedSets.each { suffix, groups ->
            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])
            
            def suffixConfig = config.get(configKey) as Map
            def namedSetConfig = getSetConfig(suffixConfig)
            // required is at suffix config level, not inside named_set
            def requiredGroups = suffixConfig?.required as List<String>
            
            // Only validate if requiredGroups is explicitly defined and non-empty
            if (requiredGroups != null && !requiredGroups.isEmpty()) {
                def foundGroups = groups.keySet()
                def hasAllRequired = requiredGroups.every { foundGroups.contains(it) }
                
                if (hasAllRequired) {
                    validSuffixes[suffix] = groups
                } else {
                    def missing = requiredGroups - foundGroups
                    def entityMap = [:]
                    loopOverEntities.eachWithIndex { entity, index ->
                        entityMap[entity] = filesInGroup[0]?.getEntity(entity) ?: "NA"
                    }
                    def entityDesc = loopOverEntities.collect { entity -> "${entity}: ${entityMap[entity]}" }.join(", ")
                    BidsLogger.warn("Entities ${entityDesc}, Suffix ${suffix}: Missing required groups: ${missing}. Found: ${foundGroups}")
                }
            } else {
                // No required groups specified, accept all groups
                validSuffixes[suffix] = groups
            }
        }
        
        if (validSuffixes.isEmpty()) {
            return null
        }
        
        // Build grouping key
        def groupingKey = BidsEntityUtils.createGroupingKey(filesInGroup[0], loopOverEntities)
        
        // Create channel data
        def channelData = new BidsChannelData()
        
        // Add suffix data as maps of {groupName -> {extension: filePath}}
        // Use the resolved config key (virtual suffix) for output, not the file suffix
        validSuffixes.each { suffix, groups ->
            // Resolve to the config key (e.g., "epi" -> "epi_fullreverse")
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])
            
            def groupMap = groups.collectEntries { groupName, file ->
                // Build nested file data map by extension type
                [groupName, buildNestedMapForFile(file, allFiles)]
            }
            channelData.addSuffixData(configKey, groupMap)  // Use config key, not file suffix
        }
        
        // Add all file paths (with relative paths)
        allFiles.each { file ->
            channelData.addFilePath(makeRelativePath(file.path))
            if (file.sidecarPath) {
                channelData.addFilePath(makeRelativePath(file.sidecarPath))
            }
        }
        
        // Add entities from the first file (all files in group should have same loop-over entities)
        if (filesInGroup) {
            filesInGroup[0].entities.each { k, v ->
                channelData.addEntity(k, v)
            }
        }
        
        BidsLogger.trace("Named set emitted with ${validSuffixes.size()} suffixes, key: ${groupingKey}")
        
        return channelData
    }
    
    /**
     * Find which named group a file belongs to based on pattern matching.
     * Iterates through group definitions (e.g., T1w, MTw, PDw) and checks if
     * all entity patterns match the file.
     * 
     * @param file The BIDS file to match
     * @param namedSetConfig The named_set configuration map
     * @return The matching group name (e.g., "T1w") or null if no match
     * 
     * @reference bids2nf entity_grouping_utils.nf findMatchingGrouping
     */
    private String findMatchingGroupName(BidsFile file, Map namedSetConfig) {
        // Iterate through group names (T1w, MTw, PDw, etc.)
        for (def entry : namedSetConfig.entrySet()) {
            def groupName = entry.key
            def patternMap = entry.value
            
            // Skip special configuration keys
            if (groupName == 'required' || groupName == 'additional_extensions' || 
                groupName == 'filter' || !(patternMap instanceof Map)) {
                continue
            }
            
            // Check if ALL entity patterns match this file
            boolean allMatch = true
            for (def pattern : (patternMap as Map).entrySet()) {
                def configEntityName = pattern.key
                def expectedValue = pattern.value
                
                // Skip description field (documentation only)
                if (configEntityName == 'description') {
                    continue
                }
                
                // Normalize entity name: config might use long names (mtransfer),
                // but BidsFile stores short names (mt)
                def entityName = normalizeEntityName(configEntityName)
                
                // Get actual entity value from file (stored without prefix)
                def actualValue = file.getEntity(entityName)
                
                // Check if values match (with normalization)
                if (!entityValuesMatch(actualValue, expectedValue as String, entityName)) {
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
     * The config patterns have full entity-value format (e.g., "flip-02", "mt-off"),
     * but BidsFile stores values without prefixes (e.g., "02", "off").
     * This method adds the prefix back to the file value before comparing.
     * 
     * Also handles zero-padding: "flip-02" matches "flip-2"
     * 
     * @param actualValue Value from file entity (without prefix, e.g., "02")
     * @param expectedValue Value from configuration pattern (with prefix, e.g., "flip-02")
     * @param entityName Entity short name (e.g., "flip", "mt")
     * @return true if values match after normalization
     */
    private boolean entityValuesMatch(String actualValue, String expectedValue, String entityName) {
        if (!actualValue || !expectedValue) {
            return actualValue == expectedValue
        }
        
        // Add prefix back to actual value for comparison
        def actualWithPrefix = "${entityName}-${actualValue}"
        
        return normalizeEntityValue(actualWithPrefix) == normalizeEntityValue(expectedValue)
    }
    
    /**
     * Normalize entity value for comparison
     * 
     * Handles:
     * 1. Removes entity prefix if present: "flip-02" → "02"
     * 2. Removes leading zeros from numeric parts: "02" → "2"
     * 
     * This allows matching file entities (stored without prefix) against
     * config patterns (which include prefix).
     * 
     * @param value Entity value to normalize (may or may not have prefix)
     * @return Normalized value without prefix and without leading zeros
     * 
     * @reference Normalization implementation:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L1-L18
     */
    private String normalizeEntityValue(String value) {
        if (!value) {
            return value
        }
        
        def normalizedValue = value
        
        // Strip prefix if present (e.g., "flip-02" -> "02")
        if (value.contains('-')) {
            def parts = value.split('-', 2)
            if (parts.length == 2) {
                normalizedValue = parts[1]
            }
        }
        
        // If value is numeric, remove leading zeros
        if (normalizedValue.isNumber()) {
            try {
                def numericValue = Integer.parseInt(normalizedValue)
                return "${numericValue}"
            } catch (NumberFormatException e) {
                return normalizedValue
            }
        }
        
        return normalizedValue
    }
    
    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.named_set as Map
    }
}
