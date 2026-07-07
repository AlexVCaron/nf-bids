package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsLogger

/**
 * Handler for sequential BIDS sets.
 *
 * <p>Groups files that are ordered along one or more BIDS entities (e.g. echo, flip,
 * inversion).  Produces a flat or hierarchical array of files sorted by the
 * sequence entity values.</p>
 *
 * <ul>
 *   <li>Single-entity ({@code by_entity: echo}) → flat array:
 *       {@code [echo-1_file, echo-2_file, echo-3_file]}</li>
 *   <li>Multi-entity hierarchical ({@code by_entities: [flip, inversion]}) →
 *       nested map: {@code {flip-1: {inv-1: file, inv-2: file}, …}}</li>
 *   <li>Multi-entity flat ({@code by_entities: [echo, flip], order: flat}) →
 *       2-D array: {@code [[echo-1_flip-1, echo-1_flip-2], …]}</li>
 * </ul>
 *
 * <p>Corresponds to {@code sequential_set:} entries in {@code bids2nf.yaml}.</p>
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class SequentialSetHandler extends BaseSetHandler {

    @Override
    protected String setName() {
        return "sequential_set"
    }

    @Override
    protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
        // For sequential sets, use the suffix as the index, plus config key
        return [fileSuffix: file.suffix, configKey: configKey]
    }

    @Override
    protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
        String configKey = index.configKey
        String fileSuffix = index.fileSuffix
        
        if (!sets.containsKey(configKey)) {
            sets[configKey] = [
                files: [],
                entities: ordering.entities,
                order: ordering.order,
                fileSuffix: fileSuffix
            ]
        }

        if (sets[configKey].entities != ordering.entities) {
            BidsLogger.logProgress(logGroup(), "Warning: Inconsistent sequencing entities for config key ${configKey}")
            return
        }

        if (sets[configKey].order != ordering.order) {
            BidsLogger.logProgress(logGroup(), "Warning: Inconsistent sequencing order for config key ${configKey}")
            return
        }

        sets[configKey].files << [
            file: file,
            sequenceValues: ordering.values
        ]

        if (!allFiles.containsKey(fileSuffix)) {
            allFiles[fileSuffix] = []
        }
        allFiles[fileSuffix] << file
    }

    @Override
    protected List<BidsChannelData> processGroup(
            String datasetRoot,
            Map sets,
            Map allFiles,
            Map config,
            List<String> loopOverEntities,
            Map<String, Map<String, String>> suffixMapping) {
        // Create channel data
        def channelData = new BidsChannelData()

        // Entities consumed as the sequence dimension (by_entity/by_entities) must
        // be kept out of the emitted meta so the item can fuse with the other
        // results: their value varies across the sequenced files.
        Set<String> consumedEntities = consumedEntities(config)

        // Add suffix data (arrays or nested structures)
        sets.each { configKey, setData ->
            String fileSuffix = setData.fileSuffix ?: configKey  // Get file suffix from setData
            BidsLogger.logProgress(logGroup(), "Config key: ${configKey}, file suffix: ${fileSuffix}")
            
            // Get parts configuration using config key
            def suffixConfig = config.get(configKey) as Map
            BidsLogger.logProgress(logGroup(), "Suffix config: ${suffixConfig}")
            def partsConfig = suffixConfig ? getSetConfig(suffixConfig)?.parts as List<String> : null
            BidsLogger.logProgress(logGroup(), "Parts config for config key ${configKey}: ${partsConfig}")

            // Use hierarchical mapper which handles both flat and nested structures
            Map dataMap = nestedSequenceMapHierarchical(
                datasetRoot,
                setData.files,
                allFiles.get(fileSuffix, [])
            )

            BidsLogger.logProgress(logGroup(), "Data map before parts grouping: ${dataMap}")

            // Apply parts grouping if configured
            if (partsConfig) {
                BidsLogger.logProgress(logGroup(), "Applying parts grouping with config: ${partsConfig}")
                dataMap = applyPartsGrouping(dataMap, partsConfig, allFiles.get(fileSuffix, []), datasetRoot)
                BidsLogger.logProgress(logGroup(), "Data map after parts grouping: ${dataMap}")
            } else {
                BidsLogger.logProgress(logGroup(), "No parts config, skipping parts grouping")
            }

            channelData.addSuffixData(configKey, dataMap)

            // Get all related files for this file suffix
            List<BidsFile> relatedFiles = allFiles.get(fileSuffix, [])

            // Add all file paths
            relatedFiles.each { file ->
                channelData.addFilePath(file.relativeTo(datasetRoot))
                file.entities.each { entity ->
                    // Skip entities consumed as the sequence dimension: their value
                    // varies across the sequenced files, so they must not appear in
                    // the grouping key (otherwise the item cannot fuse with the
                    // other results).
                    if (!consumedEntities.contains(entity.name)) {
                        channelData.addEntity(entity.name, entity.value)
                    }
                }
            }
        }

        BidsLogger.logProgress(logGroup(), "Sequential set emitted with ${sets.size()} suffixes")

        return [channelData]
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
