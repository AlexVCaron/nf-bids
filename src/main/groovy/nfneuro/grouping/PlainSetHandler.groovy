package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsEntityUtils
import nfneuro.plugin.util.BidsLogger
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Handler for plain BIDS sets
 * 
 * Processes simple 1:1 file mappings without complex grouping.
 * Plain sets emit each file individually with its entities as grouping key.
 * 
 * @reference Plain set implementation: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_plain_sets.nf
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/plain_set_utils.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class PlainSetHandler extends BaseSetHandler {
    
    /**
     * Get the base name of a file without extension
     * e.g., /path/to/sub-01_T1w.nii.gz -> sub-01_T1w
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
     * Check if a file is a primary file (not a sidecar/additional extension)
     * Primary files are: .nii.gz, .nii, .json, .tsv
     * Additional/sidecar files are: .bval, .bvec, .edf, .eeg, .txt
     */
    private static boolean isPrimaryFile(String filePath) {
        return filePath.endsWith('.nii.gz') || filePath.endsWith('.nii') || 
               filePath.endsWith('.json') || filePath.endsWith('.tsv')
    }
    
    /**
     * Build a nested map structure grouping files by extension type
     * e.g., {nii: 'path/to/file.nii.gz', json: 'path/to/file.json', bval: '...', bvec: '...'}
     */
    private Map<String, String> buildNestedDataMap(BidsFile primaryFile, List<BidsFile> allFiles) {
        def baseName = getBaseName(primaryFile.path)
        def nestedMap = [:]
        
        // Find all files with the same base name
        allFiles.each { file ->
            if (getBaseName(file.path) == baseName) {
                def extensionType = getExtensionType(file.path)
                def relativePath = makeRelativePath(file.path)
                nestedMap[extensionType] = relativePath
            }
        }
        
        return nestedMap
    }
    
    /**
     * Convert absolute path to relative path from dataset root
     * Extracts path starting from dataset name (e.g., ds-dwi/sub-01/...)
     */
    private static String makeRelativePath(String absolutePath) {
        // Extract the part starting from the dataset name
        // Paths look like: /home/.../tests/data/custom/ds-dwi/sub-01/...
        // or: /home/.../tests/data/bids-examples/asl001/sub-01/...
        // We want: ds-dwi/sub-01/... or asl001/sub-01/...
        
        def pathParts = absolutePath.split('/')
        
        // Find the dataset name (comes after 'custom' or 'bids-examples')
        def datasetIndex = -1
        for (int i = 0; i < pathParts.length; i++) {
            if (pathParts[i] in ['custom', 'bids-examples']) {
                datasetIndex = i + 1
                break
            }
        }
        
        if (datasetIndex > 0 && datasetIndex < pathParts.length) {
            // Join from dataset name onwards
            return pathParts[datasetIndex..-1].join('/')
        }
        
        // Fallback: return the path as-is if we can't extract relative path
        return absolutePath
    }
    
    @Override
    DataflowQueue process(
            List<BidsFile> bidsFiles,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {
        
        BidsLogger.debug("Processing plain sets with ${bidsFiles.size()} files")
        BidsLogger.debug("Loop-over entities: ${loopOverEntities}")
        
        // DEBUG: Log all unique suffixes in input files
        def uniqueSuffixes = bidsFiles.collect { it.suffix }.unique()
        BidsLogger.info("PlainSetHandler: Input files have suffixes: ${uniqueSuffixes}")
        
        def results = new DataflowQueue()
        def processedCount = 0
        def filteredCount = 0
        
        // Process each file individually (only primary files to avoid duplicates)
        bidsFiles.each { file ->
            // Skip non-primary files (they'll be included via buildNestedDataMap)
            if (!isPrimaryFile(file.path)) {
                BidsLogger.trace("PlainSetHandler: Skipping non-primary file: ${file.filename}")
                filteredCount++
                return
            }
            
            def channelData = processPlainSetFile(file, config, loopOverEntities, suffixMapping, bidsFiles)
            
            if (channelData) {
                // DEBUG: Log successful processing
                BidsLogger.debug("PlainSetHandler: Processed ${file.suffix} file: ${file.filename}")
                results.bind(channelData)  // Use .bind() not << to match nf-sqldb pattern
                processedCount++
            } else {
                // DEBUG: Log filtering with reason
                BidsLogger.debug("PlainSetHandler: Filtered ${file.suffix} file: ${file.filename}")
                filteredCount++
            }
        }
        
        BidsLogger.info("Plain set processing: ${processedCount} emitted, ${filteredCount} filtered")
        
        // Return unbound queue - will be consumed by transferQueueItems
        return results
    }
    
    /**
     * Process a single plain set file
     * 
     * @param file BIDS file to process
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping (for suffix_maps_to)
     * @param allFiles All files for finding associated files
     * @return BidsChannelData or null if filtered
     * 
     * @reference Plain set processing: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_plain_sets.nf#L1-L50
     */
    private BidsChannelData processPlainSetFile(
            BidsFile file,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping,
            List<BidsFile> allFiles) {
        
        def suffix = file.suffix
        if (!suffix) {
            BidsLogger.trace("Skipping file without suffix: ${file.path}")
            return null
        }
        
        // For plain_set, ALWAYS use original suffix (don't use mapped key)
        // This allows both plain_set (epi) and named_set (epi_fullreverse with suffix_maps_to) to process the same files
        def suffixConfig = config.get(suffix) as Map
        if (!suffixConfig) {
            BidsLogger.info("PlainSetHandler: No configuration for suffix: ${suffix} - FILTERED")
            return null
        }
        
        def plainSetConfig = getSetConfig(suffixConfig)
        if (plainSetConfig == null) {
            BidsLogger.info("PlainSetHandler: No plain_set configuration for suffix: ${suffix} - FILTERED")
            return null
        }
        
        BidsLogger.debug("PlainSetHandler: Found plain_set config for ${suffix}")
        
        BidsLogger.debug("PlainSetHandler: Found plain_set config for ${suffix}")
        
        // Apply entity filters if specified
        if (plainSetConfig.filter) {
            def filterMap = plainSetConfig.filter as Map
            if (!BidsEntityUtils.matchesPattern(file, filterMap)) {
                BidsLogger.trace("File filtered by entity pattern: ${file.path}")
                return null
            }
        }
        
        // Exclude files with specific entities if specified
        if (plainSetConfig.exclude_entities) {
            def excludeList = plainSetConfig.exclude_entities as List<String>
            for (String entityName : excludeList) {
                def normalizedEntity = normalizeEntityName(entityName)
                def entityValue = file.getEntity(normalizedEntity)
                if (entityValue && entityValue != "NA") {
                    BidsLogger.trace("File excluded by entity ${entityName}: ${file.path}")
                    return null
                }
            }
        }
        
        // Validate required entities are present
        def requiredEntities = plainSetConfig.required_entities as List<String>
        if (requiredEntities && !BidsEntityUtils.hasRequiredEntities(file, requiredEntities)) {
            BidsLogger.trace("File missing required entities: ${file.path}")
            return null
        }
        
        // Build grouping key from loop-over entities
        def groupingKey = BidsEntityUtils.createGroupingKey(file, loopOverEntities)
        
        // Create channel data structure
        def channelData = new BidsChannelData()
        
        // Build nested data map grouping files by extension type
        def nestedDataMap = buildNestedDataMap(file, allFiles)
        channelData.addSuffixData(suffix, nestedDataMap)
        
        // Add all related files to filePaths list
        allFiles.each { relatedFile ->
            if (getBaseName(relatedFile.path) == getBaseName(file.path)) {
                channelData.addFilePath(makeRelativePath(relatedFile.path))
            }
        }
        
        // Add all entities from the file
        file.entities.each { k, v ->
            channelData.addEntity(k, v)
        }
        
        // Add loop-over entities to ensure grouping key is captured
        loopOverEntities.each { entity ->
            if (!channelData.hasEntity(entity)) {
                channelData.addEntity(entity, file.getEntity(entity))
            }
        }
        
        BidsLogger.trace("Plain set emitted: ${file.filename} with key ${groupingKey}")
        
        return channelData
    }
    
    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.plain_set as Map
    }
    
    /**
     * Build channel tuple for plain set
     * 
     * Format: [grouping_key, file_map]
     * where file_map = [suffix: file_data]
     * 
     * @param file BIDS file
     * @param loopOverEntities Entities for grouping key
     * @return Channel tuple
     * 
     * @reference Tuple structure:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/plain_set_utils.nf#L35-L55
     */
    protected List buildPlainSetTuple(BidsFile file, List<String> loopOverEntities) {
        def groupingKey = BidsEntityUtils.createGroupingKey(file, loopOverEntities)
        
        def fileData = [
            file: file.path,
            entities: file.entities,
            sidecar: file.sidecarPath,
            metadata: file.metadata ?: [:]
        ]
        
        def fileMap = [(file.suffix): fileData]
        
        return [groupingKey, fileMap]
    }
    
    /**
     * Validate plain set configuration
     * 
     * @param config Plain set configuration map
     * @return true if valid
     */
    protected boolean validatePlainSetConfig(Map config) {
        if (!config) {
            return false
        }
        
        // Plain sets don't require specific configuration
        // Just validate optional fields if present
        
        if (config.filter && !(config.filter instanceof Map)) {
            BidsLogger.warn("Plain set filter must be a map")
            return false
        }
        
        if (config.required_entities && !(config.required_entities instanceof List)) {
            BidsLogger.warn("Plain set required_entities must be a list")
            return false
        }
        
        return true
    }
    
    /**
     * Get files for plain set by suffix
     * 
     * @param bidsFiles All BIDS files
     * @param suffix Target suffix
     * @return Filtered files with this suffix
     */
    protected List<BidsFile> getFilesForSuffix(List<BidsFile> bidsFiles, String suffix) {
        return bidsFiles.findAll { it.suffix == suffix }
    }
    
    /**
     * Sort plain set files
     * 
     * Sorts by entities to ensure consistent ordering
     * 
     * @param files Files to sort
     * @param loopOverEntities Entities to sort by
     * @return Sorted files
     */
    protected List<BidsFile> sortPlainSetFiles(
            List<BidsFile> files,
            List<String> loopOverEntities) {
        
        return files.sort { file ->
            BidsEntityUtils.createComparisonKey(file, loopOverEntities)
        }
    }
}
