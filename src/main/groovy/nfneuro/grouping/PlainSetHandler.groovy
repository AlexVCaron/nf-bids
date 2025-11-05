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
     * Build a nested map structure grouping files by extension type
     * e.g., {nii: 'path/to/file.nii.gz', json: 'path/to/file.json', bval: '...', bvec: '...'}
     */
    private Map<String, String> buildNestedDataMap(String datasetRoot, BidsFile primaryFile, List<BidsFile> allFiles) {
        def baseName = primaryFile.getBasename()
        def nestedMap = [:]

        // Find all files with the same base name
        allFiles.each { file ->
            if (file.getBasename() == baseName) {
                def extensionType = file.getExtensionType()
                def relativePath = file.relativeTo(datasetRoot)
                nestedMap[extensionType] = relativePath
            }
        }

        return nestedMap
    }

    @Override
    DataflowQueue process(
            String datasetRoot,
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
            if (!file.isPrimaryFile()) {
                BidsLogger.trace("PlainSetHandler: Skipping non-primary file: ${file.filename}")
                filteredCount++
                return
            }

            def channelData = processPlainSetFile(datasetRoot, file, config, loopOverEntities, suffixMapping, bidsFiles)

            if (channelData) {
                // DEBUG: Log successful processing
                BidsLogger.debug("PlainSetHandler: Processed ${file.suffix} file: ${file.filename}")
                results << channelData
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
            String datasetRoot,
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
                def normalizedEntity = BidsEntityUtils.normalizeEntityName(entityName)
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
        def nestedDataMap = buildNestedDataMap(datasetRoot, file, allFiles)
        channelData.addSuffixData(suffix, nestedDataMap)

        // Add all related files to filePaths list
        allFiles.each { relatedFile ->
            if (relatedFile.getBaseName() == file.getBaseName()) {
                channelData.addFilePath(relatedFile.relativeTo(datasetRoot))
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

}
