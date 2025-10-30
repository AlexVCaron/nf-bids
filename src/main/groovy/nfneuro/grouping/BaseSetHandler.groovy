package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
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
 *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
abstract class BaseSetHandler {
    
    /**
     * Process BIDS files according to set type
     * 
     * @param bidsFiles List of BIDS files to process
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping (for suffix_maps_to)
     * @return Processed channel data
     */
    abstract DataflowQueue process(
        List<BidsFile> bidsFiles,
        Map config,
        List<String> loopOverEntities,
        Map<String, String> suffixMapping
    )
    
    /**
     * Find matching grouping pattern for given file
     * 
     * @param file BIDS file
     * @param config Configuration
     * @param suffixMapping Suffix to config key mapping
     * @return Matching grouping configuration or null
     * 
     * @reference findMatchingGrouping function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L1-L35
     */
    protected Map findMatchingGrouping(BidsFile file, Map config, Map<String, String> suffixMapping) {
        def suffix = file.suffix
        if (!suffix) {
            return null
        }
        
        // Resolve config key using suffix mapping
        def configKey = SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])
        
        def suffixConfig = config[configKey]
        if (!suffixConfig || !(suffixConfig instanceof Map)) {
            return null
        }
        
        // Check for set type configuration
        def setConfig = getSetConfig(suffixConfig as Map)
        if (!setConfig) {
            return null
        }
        
        // Validate entity matching if filter specified
        def setConfigMap = setConfig as Map
        if (setConfigMap.filter) {
            def filterMap = setConfigMap.filter as Map
            if (!entitiesMatch(file.entities, filterMap)) {
                return null
            }
        }
        
        return setConfig as Map
    }
    
    /**
     * Get set configuration from suffix config
     * 
     * @param suffixConfig Configuration for specific suffix
     * @return Set configuration (plain_set, named_set, etc.)
     */
    protected abstract Map getSetConfig(Map suffixConfig)
    
    /**
     * Check if entities match the required pattern
     * 
     * @param entities Actual entity values
     * @param requiredEntities Required entity pattern
     * @return true if match
     * 
     * @reference Entity matching logic from: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L15-L30
     */
    protected boolean entitiesMatch(Map entities, Map requiredEntities) {
        return requiredEntities.every { key, value ->
            if (value == null || value == 'NA') {
                return true  // Wildcard match
            }
            return entities[key] == value
        }
    }
    
    /**
     * Group files by entity values
     * 
     * @param files List of BIDS files
     * @param groupByEntities Entities to group by
     * @return Map of group key to file list
     * 
     * @reference Entity grouping from:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L75-L110
     */
    protected Map<String, List<BidsFile>> groupFilesByEntities(
            List<BidsFile> files,
            List<String> groupByEntities) {
        
        Map<String, List<BidsFile>> grouped = [:].withDefault { [] as List<BidsFile> }
        
        files.each { file ->
            def key = buildGroupKey(file, groupByEntities)
            grouped[key] << file
        }
        
        return grouped
    }
    
    /**
     * Build group key from file entities
     * 
     * Normalizes entity names from long to short format before lookup.
     * Config uses long names (subject, session) but files store short names (sub, ses).
     * 
     * @param file BIDS file
     * @param entities Entities to include in key (may be long or short names)
     * @return Group key string
     */
    protected String buildGroupKey(BidsFile file, List<String> entities) {
        def key = entities.collect { entity ->
            // Normalize entity name (long->short) before looking up in file
            def shortName = normalizeEntityName(entity)
            file.getEntity(shortName) ?: 'NA'
        }.join('|')
        return key
    }
    
    /**
     * Normalize entity name from long to short format
     * 
     * Config files use long names (subject, session) but BidsFile stores short names (sub, ses).
     * 
     * @param entityName Entity name (long or short)
     * @return Short entity name
     */
    protected String normalizeEntityName(String entityName) {
        // Map of long names to short names (from BIDS specification)
        def longToShort = [
            'subject': 'sub',
            'session': 'ses',
            'acquisition': 'acq',
            'ceagent': 'ce',
            'tracer': 'trc',
            'reconstruction': 'rec',
            'direction': 'dir',
            'modality': 'mod',
            'mtransfer': 'mt',
            'inversion': 'inv',
            'processing': 'proc',
            'hemisphere': 'hemi',
            'segmentation': 'seg',
            'resolution': 'res',
            'density': 'den',
            'nucleus': 'nuc',
            'volume': 'voi'
        ]
        
        return longToShort[entityName] ?: entityName
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
     * Filter files by entity pattern
     * 
     * @param files Files to filter
     * @param entityPattern Required entity values
     * @return Filtered files
     */
    protected List<BidsFile> filterByEntityPattern(
            List<BidsFile> files,
            Map entityPattern) {
        
        return files.findAll { file ->
            entitiesMatch(file.entities, entityPattern)
        }
    }
    
    /**
     * Validate required files are present in group
     * 
     * @param files File list
     * @param requiredCount Minimum required files
     * @return true if valid
     * 
     * @reference validateRequiredFiles function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/validation_utils.nf#L1-L25
     */
    protected boolean validateRequiredFileCount(List<BidsFile> files, Integer requiredCount) {
        if (!requiredCount) {
            return true
        }
        
        return files.size() >= requiredCount
    }
    
    /**
     * Validate that all files have consistent entity values
     * 
     * @param files File list
     * @param consistentEntities Entities that must be same across all files
     * @return true if consistent
     */
    protected boolean validateConsistentEntities(
            List<BidsFile> files,
            List<String> consistentEntities) {
        
        if (files.isEmpty() || !consistentEntities) {
            return true
        }
        
        def referenceFile = files[0]
        
        return files.every { file ->
            consistentEntities.every { entity ->
                file.getEntity(entity) == referenceFile.getEntity(entity)
            }
        }
    }
    
    /**
     * Sort files by entity values
     * 
     * @param files Files to sort
     * @param sortByEntities Entities to sort by (in order)
     * @return Sorted files
     */
    protected List<BidsFile> sortFilesByEntities(
            List<BidsFile> files,
            List<String> sortByEntities) {
        
        if (!sortByEntities) {
            return files
        }
        
        return files.sort { file ->
            sortByEntities.collect { entity ->
                def value = file.getEntity(entity)
                
                // Try numeric sort if value is number
                if (value ==~ /\d+/) {
                    return value.toInteger()
                }
                
                return value ?: 'ZZZ'  // Push nulls to end
            }
        }
    }
    
    /**
     * Create entity string for logging
     * 
     * @param file BIDS file
     * @param entities Entities to include
     * @return Entity string like "sub-01_ses-01"
     */
    protected String createEntityString(BidsFile file, List<String> entities) {
        return entities.collect { entity ->
            def value = file.getEntity(entity)
            if (value && value != 'NA') {
                return "${entity}-${value}"
            }
            return null
        }.findAll { it != null }.join('_')
    }
    
    /**
     * Log processing statistics
     * 
     * @param handlerName Name of handler
     * @param processedCount Files processed
     * @param filteredCount Files filtered
     */
    protected void logProcessingStats(
            String handlerName,
            int processedCount,
            int filteredCount) {
        
        BidsLogger.info("${handlerName}: ${processedCount} emitted, ${filteredCount} filtered")
        
        if (filteredCount > 0) {
            def filterRatio = (filteredCount * 100) / (processedCount + filteredCount)
            BidsLogger.debug("Filter ratio: ${filterRatio.round(1)}%")
        }
    }
}
