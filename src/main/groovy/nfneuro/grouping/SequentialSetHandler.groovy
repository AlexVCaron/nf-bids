package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsLogger

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
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_sequential_sets.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class SequentialSetHandler extends BaseSetHandler {

    @Override
    protected String setName() {
        return "sequential_set"
    }

    @Override
    protected Map getSetIndex(BidsFile file, Map setConfig) {
        // For sequential sets, use the suffix as the index
        return [suffix: file.suffix]
    }

    @Override
    protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
        if (!sets.containsKey(index.suffix)) {
            sets[index.suffix] = [
                files: [],
                entities: ordering.entities,
                order: ordering.order
            ]
        }

        if (sets[index.suffix].entities != ordering.entities) {
            BidsLogger.logProgress(logGroup(), "Warning: Inconsistent sequencing entities for suffix ${index.suffix}")
            return
        }

        if (sets[index.suffix].order != ordering.order) {
            BidsLogger.logProgress(logGroup(), "Warning: Inconsistent sequencing order for suffix ${index.suffix}")
            return
        }

        sets[index.suffix].files << [
            file: file,
            sequenceValues: ordering.values
        ]

        if (!allFiles.containsKey(index.suffix)) {
            allFiles[index.suffix] = []
        }
        allFiles[index.suffix] << file
    }

    @Override
    protected BidsChannelData processGroup(
            String datasetRoot,
            Map sets,
            Map allFiles,
            Map config,
            List<String> loopOverEntities,
            Map<String, Map<String, String>> suffixMapping) {
        // Create channel data
        def channelData = new BidsChannelData()

        // Add suffix data (arrays or nested structures)
        sets.each { suffix, setData ->
            // Get parts configuration for this suffix
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(
                setName(), suffix, suffixMapping)
            BidsLogger.logProgress(logGroup(), "Config key for suffix ${suffix}: ${configKey}")
            def suffixConfig = config.get(configKey) as Map
            BidsLogger.logProgress(logGroup(), "Suffix config: ${suffixConfig}")
            def partsConfig = suffixConfig ? getSetConfig(suffixConfig)?.parts as List<String> : null
            BidsLogger.logProgress(logGroup(), "Parts config for suffix ${suffix}: ${partsConfig}")

            // Use hierarchical mapper which handles both flat and nested structures
            Map dataMap = nestedSequenceMapHierarchical(
                datasetRoot,
                setData.files,
                allFiles.get(suffix, [])
            )

            BidsLogger.logProgress(logGroup(), "Data map before parts grouping: ${dataMap}")

            // Apply parts grouping if configured
            if (partsConfig) {
                BidsLogger.logProgress(logGroup(), "Applying parts grouping with config: ${partsConfig}")
                dataMap = applyPartsGrouping(dataMap, partsConfig, allFiles.get(suffix, []), datasetRoot)
                BidsLogger.logProgress(logGroup(), "Data map after parts grouping: ${dataMap}")
            } else {
                BidsLogger.logProgress(logGroup(), "No parts config, skipping parts grouping")
            }

            channelData.addSuffixData(suffix, dataMap)

            // Get all related files for this suffix
            List<BidsFile> relatedFiles = allFiles.get(suffix, [])

            // Add all file paths
            relatedFiles.each { file ->
                channelData.addFilePath(file.relativeTo(datasetRoot))
                file.entities.each { entity ->
                    channelData.addEntity(entity.name, entity.value)
                }
            }
        }

        BidsLogger.logProgress(logGroup(), "Sequential set emitted with ${sets.size()} suffixes")

        return channelData
    }

    /**
     * Build nested map structure for hierarchical sequential files
     * Recursively handles nested list structures from hierarchical ordering
     *
     * @param datasetRoot Root directory for relative paths
     * @param fileStructure Either flat list of file entries or nested list structure
     * @param allFiles All files for finding related extensions
     * @return Map of extension type to nested arrays preserving hierarchy
     */
    private Map nestedSequenceMapHierarchical(
        String datasetRoot,
        def fileStructure,
        List<BidsFile> allFiles
    ) {
        if (fileStructure.isEmpty()) return [:]

        def first = fileStructure[0]

        // Base case: flat list of file entries [file: BidsFile, sequenceValues: ...]
        if (first instanceof Map && first.containsKey('file')) {
            def files = fileStructure.collect { it.file }
            return nestedSequenceMap(datasetRoot, files, allFiles)
        }

        // Recursive case: nested list structure [[...], [...], ...]
        if (first instanceof List) {
            def result = [:].withDefault { [] }

            fileStructure.each { innerStructure ->
                // Recurse for each group
                def innerMap = nestedSequenceMapHierarchical(
                    datasetRoot,
                    innerStructure,
                    allFiles
                )

                // Add as nested array element for each extension
                innerMap.each { extension, filesOrNested ->
                    result[extension] << filesOrNested
                }
            }

            return result
        }

        return [:]
    }

    /**
     * Get sequence entities from config (single or multiple)
     *
     * @param config Sequential set configuration
     * @return List of entity names to sequence by
     */
    @Override
    protected List<String> getSequenceByEntities(Map config) {
        if (config.by_entities && config.by_entities instanceof List) {
            return config.by_entities as List<String>
        }
        if (config.by_entity) {
            return [config.by_entity as String]
        }
        return []
    }

    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.sequential_set as Map
    }

    /**
     * Build nested map structure for sequential files
     * Groups files by extension type: {nii: [...], json: [...]}
     */
    private Map<String, List<String>> nestedSequenceMap(
        String datasetRoot,
        List<BidsFile> files,
        List<BidsFile> allFiles
    ) {
        def nestedMap = [:]

        files.each { file ->
            def baseName = file.getBasename()

            // Find all related files with same base name
            allFiles.each { relatedFile ->
                if (relatedFile.getBasename() == baseName) {
                    def type = relatedFile.getType()
                    def relativePath = relatedFile.relativeTo(datasetRoot)

                    if (!nestedMap[type]) {
                        nestedMap[type] = []
                    }
                    if (!nestedMap[type].contains(relativePath)) {
                        nestedMap[type] << relativePath
                    }
                }
            }
        }

        return nestedMap
    }

}
