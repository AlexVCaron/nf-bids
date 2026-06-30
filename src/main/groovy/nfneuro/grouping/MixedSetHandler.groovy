package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsLogger

/**
 * Handler for mixed BIDS sets.
 *
 * <p>Combines named and sequential grouping dimensions.  Files are first matched
 * to named groups (like {@link NamedSetHandler}) using entity-value patterns for
 * the {@code named_dimension}, then ordered along the {@code sequential_dimension}
 * entity.  Produces: {@code suffix → {groupName → [orderedFiles]}}.</p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * MPM:
 *   mixed_set:
 *     named_dimension: "acquisition"
 *     sequential_dimension: "echo"
 *     named_groups:
 *       MTw: {acquisition: "acq-MTw", flip: "flip-1", mtransfer: "mt-on"}
 *       PDw: {acquisition: "acq-PDw", flip: "flip-1", mtransfer: "mt-off"}
 *       T1w: {acquisition: "acq-T1w", flip: "flip-2", mtransfer: "mt-off"}
 *     required: ["MTw", "PDw", "T1w"]
 * </pre>
 *
 * <p>Corresponds to {@code mixed_set:} entries in {@code bids2nf.yaml}.</p>
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class MixedSetHandler extends BaseSetHandler {

    @Override
    protected String setName() {
        return "mixed-set"
    }

    @Override
    protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
        // For mixed sets, use the suffix and the named_group, plus config key
        def groupName = findMatchingMixedGroupName(file, setConfig)
        return [fileSuffix: file.suffix, configKey: configKey, group: groupName]
    }

    @Override
    protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
        if (!index.group) {
            BidsLogger.logProgress(logGroup(), "No group name found for file: ${file.path}")
            return
        }

        String configKey = index.configKey
        String fileSuffix = index.fileSuffix
        
        if (!sets[configKey]) {
            sets[configKey] = [
                files: [:],
                entities: ordering.entities,
                order: 'hierarchical',
                fileSuffix: fileSuffix
            ]
        }

        if (!sets[configKey].files[index.group]) {
            sets[configKey].files[index.group] = []
        }
        sets[configKey].files[index.group] << [
            file: file,
            sequenceValues: ordering.values
        ]

        if (!allFiles.containsKey(fileSuffix)) {
            allFiles[fileSuffix] = []
        }
        allFiles[fileSuffix] << file
    }

    /**
     * Process a group for mixed set configuration
     *
     * Handles nested named groups (via pattern matching) containing sequential arrays
     *
     * @param filesInGroup Files in this grouping key group
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData or null
     *
     * @reference Mixed set processing:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L56-L70
     */
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

        // Add suffix data as maps of {groupName -> {extension: [paths]}}
        sets.each { configKey, setData ->
            String fileSuffix = setData.fileSuffix ?: configKey  // Get file suffix from setData
            
            // Get parts configuration using config key
            def suffixConfig = config.get(configKey) as Map
            def partsConfig = suffixConfig ? getSetConfig(suffixConfig)?.parts as List<String> : null

            def groupMap = setData.files.collectEntries { groupName, items ->
                List<BidsFile> files = items.collect { it.file }
                def nestedMap = nestedMapForFiles(datasetRoot, files, allFiles.get(fileSuffix, []))

                // Apply parts grouping if configured
                if (partsConfig) {
                    nestedMap = applyPartsGrouping(nestedMap, partsConfig, allFiles.get(fileSuffix, []))
                }

                [groupName, nestedMap]
            }
            channelData.addSuffixData(configKey, groupMap)

            // Get all related files for this file suffix
            List<BidsFile> relatedFiles = allFiles.get(fileSuffix, [])

            // Add all file paths (with relative paths)
            relatedFiles.each { file ->
                channelData.addFilePath(file.relativeTo(datasetRoot))
                file.entities.each { entity ->
                    channelData.addEntity(entity.name, entity.value)
                }
            }
        }

        BidsLogger.logProgress(logGroup(), "Mixed set emitted with ${sets.size()} suffixes")

        return [channelData]
    }

    /**
     * Get sequential_dimension entity from config
     *
     * @param config Mixed set configuration
     * @return Entity name to sequence by
     */
    @Override
    protected List<String> getSequenceByEntities(Map config) {
        if (config.sequential_dimension) {
            if (BidsEntity.longEntityExists(config.sequential_dimension as String)) {
                return [config.sequential_dimension as String]
            }
            BidsLogger.logProgress(logGroup(), "Sequential dimension entity '${config.sequential_dimension}' is not a valid BIDS entity.")
        }
        return
    }

    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.mixed_set as Map
    }

    /**
     * Build nested map for files grouping by extension type
     */
    private static Map<String, List<String>> nestedMapForFiles(
        String datasetRoot,
        List<BidsFile> files,
        List<BidsFile> allFiles
    ) {
        Map nestedMap = [:]

        files.each { file ->
            String baseName = file.getBasename()

            // Find all related files with same base name
            allFiles.each { relatedFile ->
                if (relatedFile.getBasename() == baseName) {
                    String type = relatedFile.getType()
                    String relativePath = relatedFile.relativeTo(datasetRoot)

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

    /**
     * Find which named group a file belongs to using pattern matching
     *
     * Iterates through named_groups and checks if all entity patterns match.
     * This is identical to named set pattern matching.
     *
     * @param file The BIDS file to match
     * @param mixedSetConfig The mixed_set configuration map
     * @return The matching group name (e.g., "MTw", "PDw", "T1w") or null
     *
     * @reference findMatchingMixedGrouping function:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L56-L70
     */
    private String findMatchingMixedGroupName(BidsFile file, Map mixedSetConfig) {
        def namedGroups = mixedSetConfig.named_groups as Map
        if (!namedGroups) {
            BidsLogger.logProgress(logGroup(), "Mixed set config missing 'named_groups'")
            return null
        }

        // Check if a specific dimension is configured for named grouping
        def namedDim = getNamedDimension(mixedSetConfig)

        // Iterate through named group definitions (MTw, PDw, T1w, etc.)
        for (def entry : namedGroups.entrySet()) {
            def groupName = entry.key
            def patternMap = entry.value

            // Skip special configuration keys
            if (groupName == 'required' || groupName == 'description' || !(patternMap instanceof Map)) {
                continue
            }

            // If named_dimension is specified, only check that one entity
            // Otherwise, check ALL entity patterns (backward compatible)
            def entitiesToCheck = namedDim ?
                [(namedDim): (patternMap as Map)[namedDim]] :
                (patternMap as Map)

            // Check if all required entity patterns match this file
            boolean allMatch = true
            for (def pattern : entitiesToCheck.entrySet()) {
                def configEntityName = pattern.key
                def expectedValue = pattern.value

                // Skip if entity not in pattern (when filtering by named_dimension)
                if (expectedValue == null) {
                    continue
                }

                // Skip description field (documentation only)
                if (configEntityName == 'description') {
                    continue
                }

                // Normalize entity name: config might use long names (mtransfer),
                // but BidsFile stores short names (mt)
                def entityName = BidsEntity.normalizeName(configEntityName)
                def cleanValue = BidsEntity.sanitizeValue(expectedValue as String)

                // Check if values match (with normalization)
                if (file.getEntity(entityName) != new BidsEntity(entityName, cleanValue)) {
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
     * Get named dimension entity from config
     *
     * The named_dimension specifies which entity should be used for pattern-based
     * grouping in mixed sets. If not specified, pattern matching applies to all entities.
     *
     * @param config Mixed set configuration
     * @return Entity name for named grouping, or null to use all entities
     */
    private String getNamedDimension(Map config) {
        if (config.named_dimension) {
            if (BidsEntity.longEntityExists(config.named_dimension as String)) {
                return config.named_dimension as String
            }
            BidsLogger.logProgress(logGroup(), "Named dimension entity '${config.named_dimension}' is not a valid BIDS entity.")
        }
        return null  // Pattern match on all entities
    }

}
