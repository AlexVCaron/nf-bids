package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
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
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_plain_sets.nf
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/plain_set_utils.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class PlainSetHandler extends BaseSetHandler {

    /**
     * Build a nested map structure grouping files by extension type
     * e.g., {nii: 'path/to/file.nii.gz', json: 'path/to/file.json', bval: '...', bvec: '...'}
     */
    private Map<String, String> nestedDataMap(String datasetRoot, BidsFile primaryFile, List<BidsFile> allFiles) {
        def baseName = primaryFile.getBasename()
        def nestedMap = [:]

        BidsLogger.logProgress(getLogGroup(), "Building nested data map for primary file: ${primaryFile.path}")

        // Find all files with the same base name
        allFiles.each { file ->
            BidsLogger.logProgress(getLogGroup(), "  ├─ Checking associated file: ${file.path}")
            if (file.getBasename() == baseName) {
                nestedMap[file.getType()] = file.relativeTo(datasetRoot)
            }
        }

        nestedMap[primaryFile.getType()] = primaryFile.relativeTo(datasetRoot)

        return nestedMap
    }

    // @Override
    // DataflowQueue process(
    //         String datasetRoot,
    //         List<BidsFile> bidsFiles,
    //         Map config,
    //         List<String> loopOverEntities,
    //         Map<String, String> suffixMapping) {

    //     BidsLogger.logProgress("nf-bids-plain-set", "Processing plain sets with ${bidsFiles.size()} files")
    //     BidsLogger.logProgress("nf-bids-plain-set", "Loop-over entities: ${loopOverEntities}")

    //     // DEBUG: Log all unique suffixes in input files
    //     def uniqueSuffixes = bidsFiles.collect { it.suffix }.unique()
    //     BidsLogger.logProgress("nf-bids-plain-set", "Input files have suffixes: ${uniqueSuffixes}")

    //     def results = new DataflowQueue()
    //     def processedCount = 0
    //     def filteredCount = 0

    //     // Process each file individually (only primary files to avoid duplicates)
    //     bidsFiles.each { file ->
    //         // Skip non-primary files (they'll be included via nestedDataMap)
    //         if (!file.isPrimaryFile()) {
    //             BidsLogger.logProgress("nf-bids-plain-set", "Skipping non-primary file: ${file.filename}")
    //             filteredCount++
    //             return
    //         }

    //         def channelData = processPlainSetFile(datasetRoot, file, config, loopOverEntities, bidsFiles)

    //         if (channelData) {
    //             // DEBUG: Log successful processing
    //             BidsLogger.logProgress("nf-bids-plain-set", "Processed ${file.suffix} file: ${file.filename}")
    //             BidsLogger.logProgress("nf-bids-plain-set", "  ├─ Channel data: ${channelData}")
    //             results << channelData
    //             processedCount++
    //         } else {
    //             // DEBUG: Log filtering with reason
    //             BidsLogger.logProgress("nf-bids-plain-set", "Filtered ${file.suffix} file: ${file.filename}")
    //             filteredCount++
    //         }
    //     }

    //     BidsLogger.logProgress("nf-bids-plain-set", "Plain set processing: ${processedCount} emitted, ${filteredCount} filtered")

    //     // Return unbound queue - will be consumed by transferQueueItems
    //     return results
    // }

    @Override
    protected String getSetName() {
        return "plain_set"
    }

    /**
     * Process a single plain set file
     *
     * @param file BIDS file to process
     * @param config Configuration map
     * @param loopOverEntities entities to group by
     * @param suffixMapping Suffix to config key mapping (for suffix_maps_to)
     * @param allFiles All files for finding associated files
     * @return BidsChannelData or null if filtered
     *
     * @reference Plain set processing:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_plain_sets.nf#L1-L50
     */
    @Override
    protected BidsChannelData processGroup(
            String datasetRoot,
            List<BidsFile> filesInGroup,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {

        Map plainSets = [:].withDefault { [:] }
        Map allFiles = [:]

        filesInGroup.each{ file ->
            String suffix = file.suffix
            if (!suffix) {
                BidsLogger.logProgress(getLogGroup(), "Skipping file without suffix: ${file.path}")
                return
            }

            // For plain_set, ALWAYS use original suffix (don't use mapped key)
            // This allows both plain_set (epi) and named_set (epi_fullreverse with suffix_maps_to)
            // to process the same files
            def suffixConfig = config.get(suffix) as Map
            if (!suffixConfig) {
                BidsLogger.logProgress(getLogGroup(), "No configuration for suffix: ${suffix} - FILTERED")
                return
            }

            def plainSetConfig = getSetConfig(suffixConfig)
            if (plainSetConfig == null) {
                BidsLogger.logProgress(getLogGroup(), "No plain_set configuration for suffix: ${suffix} - FILTERED")
                return
            }

            BidsLogger.logProgress(getLogGroup(), "Found plain_set config for ${suffix}")

            // Apply entity filters if specified
            if (plainSetConfig.filter) {
                if (!BidsEntityUtils.entitiesMatch(file.entities, plainSetConfig.filter as List)) {
                    BidsLogger.logProgress(getLogGroup(), "File filtered by entity pattern: ${file.path}")
                    return
                }
            }

            // Exclude files with specific entities if specified
            if (plainSetConfig.exclude_entities) {
                for (String entityName : plainSetConfig.exclude_entities as List<String>) {
                    String normalizedEntity = BidsEntity.normalizeName(entityName)
                    String entityValue = file.getEntityValue(normalizedEntity)
                    if (entityValue && entityValue != "NA") {
                        BidsLogger.logProgress(getLogGroup(), "File excluded by entity ${entityName}: ${file.path}")
                        return
                    }
                }
            }

            // Validate required entities are present
            def requiredEntities = plainSetConfig.required_entities as List<String>
            if (requiredEntities && !BidsEntityUtils.hasRequiredEntities(file, requiredEntities)) {
                BidsLogger.logProgress(getLogGroup(), "File missing required entities: ${file.path}")
                return
            }

            if (file.isPrimaryFile()) {
                plainSets[suffix] = file
            } else {
                if (!allFiles.containsKey(suffix)) {
                    allFiles[suffix] = []
                }
                allFiles[suffix] << file
            }
        }

        // Create channel data structure
        BidsChannelData channelData = new BidsChannelData()

        plainSets.each { suffix, file ->
            BidsLogger.logProgress(getLogGroup(), "Emitting plain set for suffix: ${suffix}, file: ${file.path}")

            // Get all related files for this suffix
            List<BidsFile> relatedFiles = allFiles.get(suffix, [])

            // Build nested data map grouping files by extension type
            Map nestedDataMap = nestedDataMap(datasetRoot, file, relatedFiles)
            channelData.addSuffixData(suffix, nestedDataMap)

            // Add all related files to filePaths list
            relatedFiles.each { relatedFile ->
                if (relatedFile.getBasename() == file.getBasename()) {
                    channelData.addFilePath(relatedFile.relativeTo(datasetRoot))
                }
            }

            // Load file into channel
            channelData.addFilePath(file.relativeTo(datasetRoot))

            // Add all entities from the file
            file.entities.each { entity ->
                channelData.addEntity(entity.name, entity.value)
            }

            // Add loop-over entities to ensure grouping key is captured
            loopOverEntities.each { entityName ->
                if (!channelData.hasEntity(entityName)) {
                    channelData.addEntity(entityName, file.getEntityValue(BidsEntity.normalizeName(entityName)))
                }
            }
        }

        return channelData
    }

    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.plain_set as Map
    }

}
