package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsLogger

/**
 * Handler for plain BIDS sets.
 *
 * <p>Processes simple 1:1 file-per-suffix mappings.  For each loop-over entity group,
 * each matching primary file (e.g. {@code .nii.gz}) is emitted together with its
 * sidecar files (JSON, bval/bvec, …) grouped by extension type into a nested map:
 * {@code {nii: path, json: path, …}}.</p>
 *
 * <p>Corresponds to {@code plain_set:} entries in {@code bids2nf.yaml}.</p>
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
    protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
        // For plain sets, use the suffix as the index, plus config key for output
        return [fileSuffix: file.suffix, configKey: configKey]
    }

    @Override
    protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
        String configKey = index.configKey  // Use config key for sets
        String fileSuffix = index.fileSuffix  // Use file suffix for allFiles lookup

        if (file.isPrimaryFile()) {
            // Collect all primary files to support outer-join across configKeys
            if (!sets.containsKey(configKey)) {
                sets[configKey] = [primaryFiles: [], fileSuffix: fileSuffix]
            }
            (sets[configKey].primaryFiles as List) << file
        } else {
            if (!allFiles.containsKey(fileSuffix)) {
                allFiles[fileSuffix] = []
            }
            allFiles[fileSuffix] << file
        }
    }

    @Override
    protected List<BidsChannelData> processGroup(
            String datasetRoot,
            Map plainSets,
            Map allFiles,
            Map config,
            List<String> loopOverEntities,
            Map suffixMapping) {

        BidsLogger.logProgress(logGroup(), "processGroup called with ${plainSets.size()} plain sets: ${plainSets.keySet()}")
        BidsLogger.logProgress(logGroup(), "allFiles has ${allFiles.size()} suffixes: ${allFiles.keySet()}")

        List<BidsChannelData> result = []

        plainSets.keySet().toList().sort().each { configKey ->
            def setData = plainSets[configKey]
            String fileSuffix = setData.fileSuffix ?: configKey
            BidsLogger.logProgress(logGroup(), "Processing config key: ${configKey}, file suffix: ${fileSuffix}")

            List<BidsFile> primaryFiles = ((setData.primaryFiles ?: []) as List<BidsFile>)
                .sort { normalizedPath(it) }

            if (primaryFiles.isEmpty()) {
                BidsLogger.logProgress(logGroup(), "No primary files found for config key: ${configKey}")
                return
            }

            // Get all related sidecar files for this suffix
            List<BidsFile> relatedFiles = (allFiles.get(fileSuffix, []) as List<BidsFile>)
                .toList()
                .sort { a, b -> normalizedPath(a) <=> normalizedPath(b) }

            // Get parts configuration using config key
            def suffixConfig = config.get(configKey) as Map
            def partsConfig = suffixConfig ? getSetConfig(suffixConfig)?.parts as List<String> : null

            // Emit one BidsChannelData per primary file (outer-join support)
            primaryFiles.each { file ->
                BidsLogger.logProgress(logGroup(), "Emitting plain set for config key: ${configKey}, file: ${file.path}")

                BidsChannelData channelData = new BidsChannelData()

                // Build nested data map grouping files by extension type
                Map nestedDataMap = nestedDataMap(datasetRoot, file, relatedFiles)

                // Apply parts grouping if configured
                if (partsConfig) {
                    nestedDataMap = applyPartsGrouping(nestedDataMap, partsConfig, relatedFiles, datasetRoot)
                }

                channelData.addSuffixData(configKey, nestedDataMap)

                // Add sidecar file paths for this primary file only
                relatedFiles.each { relatedFile ->
                    if (relatedFile.getBasename() == file.getBasename()) {
                        channelData.addFilePath(relatedFile.relativeTo(datasetRoot))
                    }
                }
                channelData.addFilePath(file.relativeTo(datasetRoot))

                // Add all entities from this file
                file.entities.each { entity ->
                    channelData.addEntity(entity.name, entity.value)
                }

                // Ensure loop-over entities are captured
                loopOverEntities.each { entityName ->
                    if (!channelData.hasEntity(entityName)) {
                        channelData.addEntity(entityName, file.getEntityValue(BidsEntity.normalizeName(entityName)))
                    }
                }

                result << channelData
            }
        }

        return result
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

    private static String normalizedPath(BidsFile file) {
        return file.path?.toString()?.replace('\\', '/') ?: ''
    }

}
