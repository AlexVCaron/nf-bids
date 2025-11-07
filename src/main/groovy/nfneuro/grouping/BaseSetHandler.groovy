package nfneuro.plugin.grouping

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.util.BidsEntityUtils
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.util.SuffixMapper
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Base handler for BIDS set processing
 *
 * Provides common functionality for plain, named, sequential, and mixed sets.
 * Each handler implements specific grouping and organization logic.
 *
 * @reference Set processing implementation:
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/
 */
// @CompileStatic - TODO: Requires refactoring to align with BidsChannelData model
abstract class BaseSetHandler {

    /**
     * Process BIDS files according to set type
     *
     * @param datasetRoot Root path of BIDS dataset
     * @param bidsFiles List of BIDS files to process
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping (for suffix_maps_to)
     * @return Processed channel data
     */
    DataflowQueue process(
        String datasetRoot,
        List<BidsFile> bidsFiles,
        Map config,
        List<String> loopOverEntities,
        Map<String, String> suffixMapping
    ) {
        BidsLogger.logProgress(getLogGroup(), "Processing mixed sets with ${bidsFiles.size()} files")
        BidsLogger.logProgress(getLogGroup(), "Loop-over entities: ${loopOverEntities}")

        def results = new DataflowQueue()
        def processedCount = 0
        def filteredCount = 0

        // Group files by loop-over entities first
        def filesByGroup = BidsEntityUtils.groupByEntities(bidsFiles, loopOverEntities)

        // Process each group
        filesByGroup.each { groupKey, filesInGroup ->
            def channelData = processGroup(datasetRoot, filesInGroup, config, loopOverEntities, suffixMapping)

            if (channelData) {
                results << channelData
                processedCount++
            } else {
                filteredCount++
            }
        }

        // Return unbound queue - will be consumed by transferQueueItems
        logProcessingStats(processedCount, filteredCount)

        return results
    }

    /**
     * Get set configuration from suffix config
     *
     * @param suffixConfig Configuration for specific suffix
     * @return Set configuration (plain_set, named_set, etc.)
     */
    protected abstract Map getSetConfig(Map suffixConfig)

    /**
     * Get set name
     *
     * @return Name of the set type (e.g., "plain_set", "named_set")
     */
    protected abstract String getSetName()

    /**
     * Process a group of files
     *
     * @param datasetRoot Root path of BIDS dataset
     * @param filesInGroup Files in the current group
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @param suffixMapping Suffix to config key mapping
     * @return BidsChannelData for the group or null if filtered out
     */
    protected abstract BidsChannelData processGroup(
        String datasetRoot,
        List<BidsFile> filesInGroup,
        Map config,
        List<String> loopOverEntities,
        Map<String, String> suffixMapping
    )

    protected String getLogGroup() {
        return "nf-bids-${getSetName()}"
    }

    /**
     * Find matching grouping pattern for given file
     *
     * @param file BIDS file
     * @param config Configuration
     * @param suffixMapping Suffix to config key mapping
     * @return Matching grouping configuration or null
     *
     * @reference findMatchingGrouping function:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L1-L35
     */
    protected Map findMatchingGrouping(BidsFile file, Map config, Map<String, String> suffixMapping) {
        def suffix = file.suffix
        if (!suffix) {
            return null
        }

        // Resolve config key using suffix mapping
        def configKey = SuffixMapper.resolveConfigKey(suffix, suffixMapping ?: [:])

        def suffixConfig = config[configKey]
        if (!suffixConfig || !(suffixConfig instanceof Map)) {
            return null
        }

        // Check for set type configuration
        def setConfig = getSetConfig(suffixConfig as Map)
        if (!setConfig) {
            return null
        }

        // Validate entity matching if filter specified
        def setConfigMap = setConfig as Map
        if (setConfigMap.filter) {
            if (!BidsEntityUtils.entitiesMatch(file.entities, setConfigMap.filter as List)) {
                return null
            }
        }

        return setConfig as Map
    }

    /**
     * Filter files by suffix
     *
     * @param files All BIDS files
     * @param suffix Target suffix
     * @return Filtered files
     */
    protected List<BidsFile> filterBySuffix(List<BidsFile> files, String suffix) {
        return files.findAll { it.suffix == suffix }
    }

    /**
     * Validate required files are present in group
     *
     * @param files File list
     * @param requiredCount Minimum required files
     * @return true if valid
     *
     * @reference validateRequiredFiles function:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/validation_utils.nf#L1-L25
     */
    protected boolean validateRequiredFileCount(List<BidsFile> files, Integer requiredCount) {
        if (!requiredCount) {
            return true
        }

        return files.size() >= requiredCount
    }

    /**
     * Log processing statistics
     *
     * @param handlerName Name of handler
     * @param processedCount Files processed
     * @param filteredCount Files filtered
     */
    protected void logProcessingStats(
            int processedCount,
            int filteredCount) {

        BidsLogger.logProgress(getLogGroup(), "${processedCount} emitted, ${filteredCount} filtered")

        if (filteredCount > 0) {
            def filterRatio = (filteredCount * 100) / (processedCount + filteredCount)
            BidsLogger.logProgress(getLogGroup(), "Filter ratio: ${filterRatio.round(1)}%")
        }
    }

}
