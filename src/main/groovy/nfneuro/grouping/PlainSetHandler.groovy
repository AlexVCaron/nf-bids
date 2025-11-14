package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsLogger

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

    @Override
    protected String setName() {
        return "plain_set"
    }

    @Override
    protected List<String> getSequenceByEntities(Map config) {
        // Plain sets do not use sequencing
        return
    }

    @Override
    protected Map getSetIndex(BidsFile file, Map setConfig) {
        // For plain sets, use the suffix as the index
        return [suffix: file.suffix]
    }

    @Override
    protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
        if (file.isPrimaryFile()) {
            sets[index.suffix] = [files: [file: file]]
        } else {
            if (!allFiles.containsKey(index.suffix)) {
                allFiles[index.suffix] = []
            }
            allFiles[index.suffix] << file
        }
    }

    @Override
    protected BidsChannelData processGroup(
            String datasetRoot,
            Map plainSets,
            Map allFiles,
            Map config,
            List<String> loopOverEntities,
            Map suffixMapping) {

        BidsLogger.logProgress(logGroup(), "processGroup called with ${plainSets.size()} plain sets: ${plainSets.keySet()}")
        BidsLogger.logProgress(logGroup(), "allFiles has ${allFiles.size()} suffixes: ${allFiles.keySet()}")

        // Create channel data structure
        BidsChannelData channelData = new BidsChannelData()

        plainSets.each { suffix, setData ->
            BidsLogger.logProgress(logGroup(), "Processing suffix: ${suffix}, setData: ${setData}")
            // Extract the file from the setData structure
            BidsFile file = setData.files?.file as BidsFile
            if (!file) {
                BidsLogger.logProgress(logGroup(), "No primary file found for suffix: ${suffix}, setData.files: ${setData.files}")
                return
            }

            BidsLogger.logProgress(logGroup(), "Emitting plain set for suffix: ${suffix}, file: ${file.path}")

            // Get all related files for this suffix
            List<BidsFile> relatedFiles = allFiles.get(suffix, [])

            // Get parts configuration for this suffix
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(
                setName(), suffix, suffixMapping)
            def suffixConfig = config.get(configKey) as Map
            def partsConfig = suffixConfig ? getSetConfig(suffixConfig)?.parts as List<String> : null

            // Build nested data map grouping files by extension type
            Map nestedDataMap = nestedDataMap(datasetRoot, file, relatedFiles)

            // Apply parts grouping if configured
            if (partsConfig) {
                nestedDataMap = applyPartsGrouping(nestedDataMap, partsConfig, relatedFiles)
            }

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

    /**
     * Build a nested map structure grouping files by extension type
     * e.g., {nii: 'path/to/file.nii.gz', json: 'path/to/file.json', bval: '...', bvec: '...'}
     */
    private Map<String, String> nestedDataMap(String datasetRoot, BidsFile primaryFile, List<BidsFile> allFiles) {
        def baseName = primaryFile.getBasename()
        def nestedMap = [:]

        BidsLogger.logProgress(logGroup(), "Building nested data map for primary file: ${primaryFile.path}")

        // Find all files with the same base name
        allFiles.each { file ->
            BidsLogger.logProgress(logGroup(), "  ├─ Checking associated file: ${file.path}")
            if (file.getBasename() == baseName) {
                nestedMap[file.getType()] = file.relativeTo(datasetRoot)
            }
        }

        nestedMap[primaryFile.getType()] = primaryFile.relativeTo(datasetRoot)

        return nestedMap
    }

}
