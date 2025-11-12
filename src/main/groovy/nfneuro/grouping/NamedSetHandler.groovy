package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsEntityUtils
import nfneuro.plugin.util.BidsLogger
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Handler for named BIDS sets
 *
 * Processes named groupings using pattern-based matching.
 * Files are matched to custom group names (e.g., T1w, MTw, PDw) based on
 * entity value patterns defined in configuration.
 *
 * Named sets create: suffix -> {groupName -> {nii: file, json: file}}
 *
 * Example configuration:
 * MTS:
 *   named_set:
 *     T1w: {flip: "flip-02", mtransfer: "mt-off"}
 *     MTw: {flip: "flip-01", mtransfer: "mt-on"}
 *     PDw: {flip: "flip-01", mtransfer: "mt-off"}
 *   required: ["T1w", "MTw", "PDw"]
 *
 * @reference Named set implementation:
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_named_sets.nf
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
class NamedSetHandler extends BaseSetHandler {

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

    @Override
    protected String getSetName() {
        return "named_set"
    }

    @Override
    protected List<String> getSequenceByEntities(Map config) {
        // Named sets do not use sequencing
        return
    }

    @Override
    protected Map getSetIndex(BidsFile file, Map setConfig) {
        // For named sets, use the suffix, plus grouping entities as index
        def groupName = findMatchingGroupName(file, setConfig)
        return [suffix: file.suffix, group: groupName]
    }

    @Override
    protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
        if (!index.group) {
            BidsLogger.logProgress(getLogGroup(), "No group name found for file: ${file.path}")
            return
        }

        if (!sets[index.suffix]) {
            sets[index.suffix] = [files: [:]]
        }
        sets[index.suffix].files[index.group] = [file: file]

        if (!allFiles.containsKey(index.suffix)) {
            allFiles[index.suffix] = []
        }
        allFiles[index.suffix] << file
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
    protected BidsChannelData processGroup(
            String datasetRoot,
            Map namedSets,
            Map allFiles,
            List<String> loopOverEntities,
            Map suffixMapping) {
        // Create channel data
        def channelData = new BidsChannelData()

        // Add suffix data as maps of {groupName -> {extension: filePath}}
        // Use the resolved config key (virtual suffix) for output, not the file suffix
        namedSets.each { suffix, groups ->
            BidsLogger.logProgress(getLogGroup(), "Emitting named set for suffix: ${suffix}, file: ${file.path}")

            // Resolve to the config key (e.g., "epi" -> "epi_fullreverse")
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(getSetName(), suffix, suffixMapping ?: [:])

            def groupMap = groups.collectEntries { groupName, file ->
                // Build nested file data map by extension type
                [groupName, nestedMapForFile(datasetRoot, file, allFiles)]
            }
            channelData.addSuffixData(configKey, groupMap)  // Use config key, not file suffix

            // Get all related files for this suffix
            List<BidsFile> relatedFiles = allFiles.get(suffix, [])

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

        return channelData
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
