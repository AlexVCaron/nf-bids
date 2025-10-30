package nfneuro.plugin.model

import groovy.transform.CompileStatic

/**
 * Represents channel data structure for BIDS processing
 * 
 * Contains grouped and organized BIDS data ready for Nextflow channels
 * 
 * @reference Channel data structure from: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L98-L117
 */
@CompileStatic
class BidsChannelData {
    
    // Mapping from short entity names to long CSV column names
    private static final Map<String, String> ENTITY_LONG_NAMES = [
        'sub': 'subject',
        'ses': 'session',
        'acq': 'acquisition',
        'ce': 'ceagent',
        'trc': 'tracer',
        'rec': 'reconstruction',
        'dir': 'direction',
        'mod': 'modality',
        'inv': 'inversion',
        'mt': 'mtransfer',
        'proc': 'processing',
        'hemi': 'hemisphere',
        'seg': 'segmentation',
        'res': 'resolution',
        'den': 'density',
        'desc': 'description',
        'nuc': 'nucleus',
        'voi': 'volume'
    ]
    
    Map<String, Object> data
    List<String> filePaths
    String bidsParentDir
    Map<String, String> entities
    
    BidsChannelData() {
        this.data = [:]
        this.filePaths = []
        this.entities = [:]
    }
    
    /**
     * Add suffix data
     * 
     * @param suffix BIDS suffix
     * @param suffixData Data for this suffix (can be String path, Map, or List)
     */
    void addSuffixData(String suffix, Object suffixData) {
        if (suffix && suffixData != null) {
            data[suffix] = suffixData
        }
    }
    
    /**
     * Get suffix data
     * 
     * @param suffix BIDS suffix
     * @return Suffix data or null
     */
    Object getSuffixData(String suffix) {
        return data[suffix]
    }
    
    /**
     * Check if suffix exists in data
     * 
     * @param suffix BIDS suffix
     * @return true if suffix data exists
     */
    boolean hasSuffix(String suffix) {
        return data.containsKey(suffix)
    }
    
    /**
     * Get all suffixes in this channel data
     * 
     * @return List of suffix names
     */
    List<String> getSuffixes() {
        return new ArrayList<>(data.keySet())
    }
    
    /**
     * Add file path
     * 
     * @param filePath File path to add
     */
    void addFilePath(String filePath) {
        if (filePath && !filePaths.contains(filePath)) {
            filePaths << filePath
        }
    }
    
    /**
     * Add multiple file paths
     * 
     * @param paths List of file paths
     */
    void addFilePaths(List<String> paths) {
        paths.each { path ->
            addFilePath(path)
        }
    }
    
    /**
     * Add entity value
     * 
     * @param entityName Entity name
     * @param entityValue Entity value
     */
    void addEntity(String entityName, String entityValue) {
        if (entityName && entityValue) {
            entities[entityName] = entityValue
        }
    }
    
    /**
     * Get entity value
     * 
     * @param entityName Entity name
     * @return Entity value or "NA"
     */
    String getEntity(String entityName) {
        return entities.getOrDefault(entityName, "NA")
    }
    
    /**
     * Check if entity exists
     * 
     * @param entityName Entity name
     * @return true if entity exists and is not "NA"
     */
    boolean hasEntity(String entityName) {
        def value = entities[entityName]
        return value && value != "NA"
    }
    
    /**
     * Validate that channel data has required content
     * 
     * @return true if valid (has data and entities)
     */
    boolean isValid() {
        return !data.isEmpty() && !entities.isEmpty()
    }
    
    /**
     * Check if channel data is empty
     * 
     * @return true if no data
     */
    boolean isEmpty() {
        return data.isEmpty()
    }
    
    /**
     * Convert to channel tuple format
     * 
     * @param loopOverEntities Entities to use for grouping key (also determines which entities appear in enriched data)
     * @return Tuple [groupingKey, enrichedData]
     */
    Object toChannelTuple(List<String> loopOverEntities) {
        def groupingKey = loopOverEntities.collect { entity ->
            getEntityWithPrefix(entity)
        }
        
        def enrichedData = [
            data: data,
            filePaths: filePaths,
            bidsParentDir: bidsParentDir
        ]
        
        // Add ONLY the loop_over entities to enriched data using LONG names with prefix
        // This matches baseline behavior where only loop_over entities appear in the enriched data
        // e.g., "subject": "sub-Sub103" (not "sub": "sub-Sub103")
        // NOTE: loopOverEntities may contain long OR short names, so normalize first
        loopOverEntities.each { entityName ->
            // Normalize to short name for lookup in entities map
            def shortName = normalizeEntityName(entityName)
            // Get corresponding long name for output
            def longName = ENTITY_LONG_NAMES.getOrDefault(shortName, shortName)
            
            def value = entities.getOrDefault(shortName, 'NA')
            if (value == 'NA') {
                enrichedData[longName] = 'NA'
            } else {
                enrichedData[longName] = "${shortName}-${value}"
            }
        }
        
        return [groupingKey, enrichedData]
    }
    
    /**
     * Normalize entity name from long to short format
     * 
     * @param entityName Entity name (long or short)
     * @return Short entity name
     */
    private String normalizeEntityName(String entityName) {
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
     * Get entity value with BIDS prefix
     * 
     * Converts entity value to full BIDS format (e.g., "Sub103" -> "sub-Sub103")
     * Normalizes entity name to short form before lookup.
     * 
     * @param entityName Entity name (may be long or short)
     * @return Entity value with prefix or "NA"
     */
    private String getEntityWithPrefix(String entityName) {
        // Normalize to short name for lookup
        def shortName = normalizeEntityName(entityName)
        def value = getEntity(shortName)
        if (value == "NA") {
            return "NA"
        }
        return "${shortName}-${value}"
    }
    
    @Override
    String toString() {
        def entityStr = entities.collect { k, v -> "${k}=${v}" }.join(', ')
        def suffixStr = data.keySet().join(', ')
        return "BidsChannelData[entities=[${entityStr}], suffixes=[${suffixStr}], files=${filePaths.size()}]"
    }
    
    @Override
    boolean equals(Object obj) {
        if (this.is(obj)) return true
        if (!(obj instanceof BidsChannelData)) return false
        BidsChannelData other = (BidsChannelData) obj
        return entities == other.entities && data == other.data
    }
    
    @Override
    int hashCode() {
        return Objects.hash(entities, data)
    }
}
