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
            List<BidsFile> filesInGroup,
            Map config,
            List<String> loopOverEntities,
            Map<String, String> suffixMapping) {

        // Organize files by suffix and group name using pattern matching
        def namedSets = [:].withDefault { [:] }
        def allFiles = []

        filesInGroup.each { file ->
            def suffix = file.suffix
            BidsLogger.logProgress(getLogGroup(), "Processing file: ${file.filename} with suffix: ${suffix}")
            if (!suffix) return

            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

            def suffixConfig = config.get(configKey) as Map
            BidsLogger.logProgress(getLogGroup(), "Suffix config (key: ${configKey}): ${suffixConfig}")
            if (!suffixConfig) return

            def namedSetConfig = getSetConfig(suffixConfig)
            BidsLogger.logProgress(getLogGroup(), "Named set config: ${namedSetConfig}")
            if (!namedSetConfig) return

            // Use pattern matching to find matching group name
            // Find which named group this file belongs to
            def groupName = findMatchingGroupName(file, namedSetConfig)
            BidsLogger.logProgress(getLogGroup(), "Matched group name: ${groupName}")
            if (!groupName) {
                BidsLogger.logProgress(getLogGroup(), "No matching group pattern for file: ${file.filename}")
                return
            }

            // Add to named set structure (only one file per group for named sets)
            if (!namedSets[suffix]) {
                namedSets[suffix] = [:]
            }
            namedSets[suffix][groupName] = file
            allFiles << file
        }

        if (namedSets.isEmpty()) {
            return null
        }

        // Validate required groups are present for each suffix
        def validSuffixes = [:]

        namedSets.each { suffix, groups ->
            // Resolve config key using suffix mapping
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

            def suffixConfig = config.get(configKey) as Map
            // required is at suffix config level, not inside named_set
            def requiredGroups = suffixConfig?.required as List<String>

            // Only validate if requiredGroups is explicitly defined and non-empty
            if (requiredGroups != null && !requiredGroups.isEmpty()) {
                def foundGroups = groups.keySet()
                def hasAllRequired = requiredGroups.every { foundGroups.contains(it) }

                if (hasAllRequired) {
                    validSuffixes[suffix] = groups
                } else {
                    def missing = requiredGroups - foundGroups
                    def entityMap = [:]
                    loopOverEntities.eachWithIndex { entity, index ->
                        entityMap[entity] = filesInGroup[0]?.getEntityValue(entity) ?: "NA"
                    }
                    def entityDesc = loopOverEntities.collect { entity -> "${entity}: ${entityMap[entity]}" }.join(", ")
                    BidsLogger.logProgress(getLogGroup(), "Entities ${entityDesc}, Suffix ${suffix}: Missing required groups: ${missing}. Found: ${foundGroups}")
                }
            } else {
                // No required groups specified, accept all groups
                validSuffixes[suffix] = groups
            }
        }

        if (validSuffixes.isEmpty()) {
            return null
        }

        // Build grouping key
        def groupingKey = BidsEntityUtils.groupingKey(filesInGroup[0], loopOverEntities)

        // Create channel data
        def channelData = new BidsChannelData()

        // Add suffix data as maps of {groupName -> {extension: filePath}}
        // Use the resolved config key (virtual suffix) for output, not the file suffix
        validSuffixes.each { suffix, groups ->
            // Resolve to the config key (e.g., "epi" -> "epi_fullreverse")
            def configKey = nfneuro.plugin.util.SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

            def groupMap = groups.collectEntries { groupName, file ->
                // Build nested file data map by extension type
                [groupName, nestedMapForFile(datasetRoot, file, allFiles)]
            }
            channelData.addSuffixData(configKey, groupMap)  // Use config key, not file suffix
        }

        // Add all file paths (with relative paths)
        allFiles.each { file ->
            channelData.addFilePath(file.relativeTo(datasetRoot))
            if (file.sidecarPath) {
                channelData.addFilePath(file.sidecarPath.relativeTo(datasetRoot))
            }
        }

        // Add entities from the first file (all files in group should have same loop-over entities)
        if (filesInGroup) {
            filesInGroup[0].entities.each { entity ->
                channelData.addEntity(entity.name, entity.value)
            }
        }

        BidsLogger.logProgress(getLogGroup(), "Named set emitted with ${validSuffixes.size()} suffixes, key: ${groupingKey}")

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
