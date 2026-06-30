package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsLogger

/**
 * Handler for named BIDS sets.
 *
 * <p>Processes groupings where each file is assigned to a <em>named group</em>
 * (e.g. {@code T1w}, {@code MTw}, {@code PDw}) based on entity-value patterns
 * defined in the configuration.  All entity patterns for a group must match a
 * file for it to be assigned to that group.</p>
 *
 * <p>Produces: {@code suffix → {groupName → {ext: filePath, …}}}.</p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * MTS:
 *   named_set:
 *     T1w: {flip: "flip-02", mtransfer: "mt-off"}
 *     MTw: {flip: "flip-01", mtransfer: "mt-on"}
 *     PDw: {flip: "flip-01", mtransfer: "mt-off"}
 *   required: ["T1w", "MTw", "PDw"]
 * </pre>
 *
 * <p>Corresponds to {@code named_set:} entries in {@code bids2nf.yaml}.</p>
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class NamedSetHandler extends BaseSetHandler {

    @Override
    protected String setName() {
        return "named_set"
    }

    @Override
    protected List<String> getSequenceByEntities(Map config) {
        // Named sets do not use sequencing
        return
    }

    @Override
    protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
        // For named sets, use the suffix, plus grouping entities as index, plus config key
        def groupName = findMatchingGroupName(file, setConfig)
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
            sets[configKey] = [files: [:], fileSuffix: fileSuffix]
        }
        sets[configKey].files[index.group] = [file: file]

        if (!allFiles.containsKey(fileSuffix)) {
            allFiles[fileSuffix] = []
        }
        allFiles[fileSuffix] << file
    }

    /**
     * Process a group of files for named set configuration
     *
     * Uses pattern-based matching to assign files to group names based on entity value patterns.
     *
     * @param filesInGroup Files in this grouping key group
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData or null
     *
     * @reference Named set grouping:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_named_sets.nf#L1-L70
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L20-L60
     */
    @Override
    protected List<BidsChannelData> processGroup(
            String datasetRoot,
            Map namedSets,
            Map allFiles,
            Map config,
            List<String> loopOverEntities,
            Map suffixMapping) {
        // Create channel data
        def channelData = new BidsChannelData()

        // Add suffix data as maps of {groupName -> {extension: filePath}}
        // Use the resolved config key (virtual suffix) for output, not the file suffix
        namedSets.each { configKey, setData ->
            String fileSuffix = setData.fileSuffix ?: configKey  // Get file suffix from setData
            BidsLogger.logProgress(logGroup(), "Emitting named set for config key: ${configKey}, file suffix: ${fileSuffix}")

            // Get all related files for this file suffix
            List<BidsFile> relatedFiles = allFiles.get(fileSuffix, [])

            // Get parts configuration for this suffix
            def suffixConfig = config.get(configKey) as Map
            def partsConfig = suffixConfig ? getSetConfig(suffixConfig)?.parts as List<String> : null

            def groupMap = setData.files.collectEntries { groupName, item ->
                BidsFile file = item.file
                // Build nested file data map by extension type
                def nestedMap = nestedMapForFile(datasetRoot, file, relatedFiles)

                // Apply parts grouping if configured
                if (partsConfig) {
                    nestedMap = applyPartsGrouping(nestedMap, partsConfig, relatedFiles)
                }

                [groupName, nestedMap]
            }
            channelData.addSuffixData(configKey, groupMap)  // Use config key, not file suffix

            // Add all file paths (with relative paths)
            relatedFiles.each { file ->
                channelData.addFilePath(file.relativeTo(datasetRoot))
                if (file.sidecarPath) {
                    channelData.addFilePath(file.sidecarPath.relativeTo(datasetRoot))
                }

                file.entities.each { entity ->
                    channelData.addEntity(entity.name, entity.value)
                }
            }
        }

        return [channelData]
    }

    /**
     * Build nested map for a file grouping by extension type
     */
    private Map<String, String> nestedMapForFile(String datasetRoot, BidsFile file, List<BidsFile> allFiles) {
        def baseName = file.getBasename()
        def nestedMap = [:]

        allFiles.each { relatedFile ->
            if (relatedFile.getBasename() == baseName) {
                nestedMap[relatedFile.getType()] = relatedFile.relativeTo(datasetRoot)
            }
        }

        return nestedMap
    }

    /**
     * Find which named group a file belongs to based on pattern matching.
     * Iterates through group definitions (e.g., T1w, MTw, PDw) and checks if
     * all entity patterns match the file.
     *
     * @param file The BIDS file to match
     * @param namedSetConfig The named_set configuration map
     * @return The matching group name (e.g., "T1w") or null if no match
     *
     * @reference bids2nf entity_grouping_utils.nf findMatchingGrouping
     */
    private String findMatchingGroupName(BidsFile file, Map namedSetConfig) {
        // Iterate through group names (T1w, MTw, PDw, etc.)
        for (def entry : namedSetConfig.entrySet()) {
            def groupName = entry.key
            def patternMap = entry.value

            // Skip special configuration keys
            if (groupName == 'required' || groupName == 'additional_extensions' ||
                groupName == 'filter' || !(patternMap instanceof Map)) {
                continue
            }

            // Check if ALL entity patterns match this file
            boolean allMatch = true
            for (def pattern : (patternMap as Map).entrySet()) {
                def configEntityName = pattern.key
                def expectedValue = pattern.value

                // Skip description field (documentation only)
                if (configEntityName == 'description') {
                    continue
                }

                // Normalize entity name: config might use long names (mtransfer),
                // but BidsFile stores short names (mt)
                def entityName = BidsEntity.normalizeName(configEntityName)
                def sanitizedValue = BidsEntity.sanitizeValue(expectedValue as String)

                // Check if values match (with normalization)
                if (file.getEntity(entityName) != new BidsEntity(entityName, sanitizedValue)) {
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

    @Override
    protected Map getSetConfig(Map suffixConfig) {
        return suffixConfig?.named_set as Map
    }

}
