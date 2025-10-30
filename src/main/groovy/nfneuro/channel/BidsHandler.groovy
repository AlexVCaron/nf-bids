package nfneuro.plugin.channel

import java.util.concurrent.CompletableFuture

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowWriteChannel
import groovyx.gpars.dataflow.DataflowQueue

import nextflow.Global
import nextflow.Session
import nextflow.Channel

import nfneuro.plugin.grouping.*
import nfneuro.plugin.model.*
import nfneuro.plugin.parser.BidsParser
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.util.SuffixMapper
import nfneuro.plugin.config.BidsConfigAnalyzer
import nfneuro.plugin.config.BidsConfigLoader

@Slf4j
@CompileStatic
class BidsHandler {

    private DataflowWriteChannel target
    private String bidsDir
    private Map options
    private Map config
    private Map configAnalysis
    private List<String> loopOverEntities
    private Map<String, String> suffixMapping

    private BidsLogger bidsLogger
    private BidsParser parser

    BidsHandler withConfig(String configPath) {
        // Load and analyze configuration
        loadConfiguration(configPath)
        return this
    }

    BidsHandler withBidsDir(String bidsDir) {
        this.bidsDir = bidsDir
        return this
    }

    BidsHandler withOpts(Map options) {
        this.options = options
        return this
    }

    BidsHandler withTarget(DataflowWriteChannel target) {
        this.target = target
        return this
    }

    BidsHandler withParser(BidsParser parser) {
        this.parser = parser
        return this
    }

    BidsHandler perform(boolean async = true) {
        if (async) {
            executeAsync()
        } else {
            execute()
        }
        return this
    }

    private void executeAsync() {
        def future = CompletableFuture.runAsync({ execute() })
        future.exceptionally(this.&handlerException)
    }

    static private void handlerException(Throwable e) {
        final error = e.cause ?: e
        log.error(error.message, error)
        final session = Global.session as Session
        session?.abort(error)
    }

    private void execute() {
        if (config == null) {
            loadConfiguration(null)
        }

        BidsDataset dataset = parser.parseToDataset(bidsDir, options.libbids_sh as String)
        List<BidsFile> bidsFiles = dataset.getFiles()

        // Route to appropriate handlers based on configuration
        DataflowQueue results = processDatasets(bidsFiles, config, configAnalysis, loopOverEntities)

        // Apply cross-modal broadcasting
        DataflowQueue finalResults = applyCrossModalBroadcasting(results, config, loopOverEntities)

        // Validate and return channel
        validateAndEmitChannel(finalResults)
    }

    /**
     * Validate final results and emit channel
     *
     * Ensures at least one data group was processed and logs statistics
     *
     * @param results Final processed results
     *
     * @reference Validation and logging:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L238-L249
     */
    private void validateAndEmitChannel(DataflowQueue results) {
        int count = 0

        // Consume items from results queue and bind each valid one to output
        results.each { item ->
            // CRITICAL: Never allow null into the channel
            if (item != null) {
                target.bind(item)  // Bind each item individually like nf-sqldb
                count++
            }
        }

        if (count == 0) {
            throw new IllegalStateException(
                "├─ ⛔️ ERROR\n" +
                "[bids2nf] └─ No data groups were processed! This could indicate:\n" +
                "  - No files match the configured patterns in bids2nf.yaml\n" +
                "  - Incorrect BIDS directory structure\n" +
                "  - Configuration issues with entity matching\n" +
                "  - Missing required files for complete groupings"
            )
        }

        bidsLogger.logProgress("bids2nf", 
            "├─ ✅ SUCCESS\n" +
            "[bids2nf] └─ Bids2nf workflow complete: ${count} data groups processed")

        // Signal completion with poison pill - this is the ONLY way to close the channel
        target.bind(Channel.STOP)
    }

    /**
     * Process datasets by routing to appropriate handlers
     *
     * Routes parsed data to named, sequential, mixed, or plain set handlers
     * based on configuration analysis
     *
     * @param bidsFiles List of BidsFile objects from parser
     * @param config Configuration map
     * @param analysis Configuration analysis
     * @param loopOverEntities Entities to group by
     * @return Combined results from all handlers
     *
     * @reference Routing logic:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L69-L92
     */
    private DataflowQueue processDatasets(
            List<BidsFile> bidsFiles,
            Map config,
            Map analysis,
            List<String> loopOverEntities) {

        def combinedResults = new DataflowQueue()

        // Route to appropriate handlers and collect results
        if (analysis.hasNamedSets) {
            bidsLogger.logProgress("bids2nf", "├─ ⑆ Processing named sets >>>")
            NamedSetHandler handler = new NamedSetHandler()
            DataflowQueue namedResults = handler.process(bidsFiles, config, loopOverEntities, suffixMapping)
            transferQueueItems(namedResults, combinedResults)
        }

        if (analysis.hasSequentialSets) {
            bidsLogger.logProgress("bids2nf", "├─ ⑇ Processing sequential sets ...")
            SequentialSetHandler handler = new SequentialSetHandler()
            DataflowQueue sequentialResults = handler.process(bidsFiles, config, loopOverEntities, suffixMapping)
            transferQueueItems(sequentialResults, combinedResults)
        }

        if (analysis.hasMixedSets) {
            bidsLogger.logProgress("bids2nf", "├─ ⑈ Processing mixed sets ...")
            MixedSetHandler handler = new MixedSetHandler()
            DataflowQueue mixedResults = handler.process(bidsFiles, config, loopOverEntities, suffixMapping)
            transferQueueItems(mixedResults, combinedResults)
        }

        if (analysis.hasPlainSets) {
            bidsLogger.logProgress("bids2nf", "├─ ⑉ Processing plain sets ...")
            PlainSetHandler handler = new PlainSetHandler()
            DataflowQueue plainResults = handler.process(bidsFiles, config, loopOverEntities, suffixMapping)
            transferQueueItems(plainResults, combinedResults)
        }

        // Combine all results
        bidsLogger.logProgress("bids2nf", "├─ ⎌ Combining results from all workflow types ...")

        // Don't bind yet - will be consumed by applyCrossModalBroadcasting
        return combinedResults
    }

    /**
     * Apply demand-driven cross-modal broadcasting
     *
     * Implements the cross-modal data inclusion logic where task-specific
     * channels can request data from task="NA" channels based on configuration
     *
     * @param results Combined results from all handlers
     * @param config Configuration map
     * @param loopOverEntities Entities to group by
     * @return Channel with cross-modal data included
     *
     * @reference Broadcasting implementation:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L119-L236
     */
    private DataflowQueue applyCrossModalBroadcasting(
            DataflowQueue results,
            Map config,
            List<String> loopOverEntities) {

        // First, unify results by grouping key
        DataflowQueue unifiedData = unifyResults(results, loopOverEntities)

        // Group data by non-task entities for cross-modal broadcasting
        Map<String, List<Object>> groupedData = [:].withDefault { [] }
        Map<String, Map<String, Object>> crossModalData = [:].withDefault { [:] }

        unifiedData.each { item ->
            // Convert BidsChannelData to tuple format
            def tuple = item instanceof BidsChannelData ? 
                item.toChannelTuple(loopOverEntities) : item

            bidsLogger.logProgress("Processing item for cross-modal broadcasting: ${tuple}")

            if (tuple == null) {
                bidsLogger.warn("Null tuple encountered in applyCrossModalBroadcasting, skipping item: ${item}")
                return
            }

            if (!(tuple instanceof List) || ((List)tuple).size() < 2) {
                bidsLogger.warn("Invalid tuple format in applyCrossModalBroadcasting: ${tuple} (type: ${tuple?.getClass()?.name}), skipping")
                return
            }

            List tupleList = (List)tuple
            def groupingKey = tupleList[0]
            def enrichedData = tupleList[1] as Map
            Map<String, String> entityValues = extractEntityValues(groupingKey, loopOverEntities)
            String nonTaskKey = constructNonTaskKey(entityValues, loopOverEntities)

            // Collect cross-modal data (task="NA")
            if (entityValues.task == "NA") {
                if (!crossModalData.containsKey(nonTaskKey)) {
                    crossModalData[nonTaskKey] = [:]
                }
                Map enrichedDataMap = enrichedData as Map
                (enrichedDataMap.data as Map).each { suffix, suffixData ->
                    crossModalData[nonTaskKey][(String)suffix] = suffixData
                }
            }

            // Group all data
            if (!groupedData.containsKey(nonTaskKey)) {
                groupedData[nonTaskKey] = []
            }
            groupedData[nonTaskKey] << [groupingKey, enrichedData, entityValues]
        }

        // Apply demand-driven broadcasting and create result queue
        DataflowQueue broadcastedResults = new DataflowQueue()

        groupedData.each { nonTaskKey, groupEntries ->
            Map<String, Object> available = crossModalData[nonTaskKey] ?: [:]

            groupEntries.each { entry ->
                List entryList = (List)entry
                def groupingKey = entryList[0]
                def enrichedData = entryList[1] as Map
                def entityValues = entryList[2] as Map
                def enhanced = applyIncludeCrossModal(
                    enrichedData,
                    entityValues,
                    available,
                    config
                )

                if (shouldKeepChannel(enhanced, entityValues, config)) {
                    broadcastedResults.bind([groupingKey, enhanced])  // Use .bind() not <<
                }
            }
        }

        // Return unbound queue - will be bound only in validateAndEmitChannel
        return broadcastedResults
    }

    /**
     * Unify results by grouping and merging data maps
     *
     * @param results Combined channel results
     * @param loopOverEntities Entities to group by
     * @return Unified channel with merged data
     *
     * @reference Unification logic:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L94-L117
     */
    private DataflowQueue unifyResults(DataflowQueue results, List<String> loopOverEntities) {
        // Group by grouping key
        Map<Object, List<Object>> grouped = [:].withDefault { [] }
        results.each { item ->
            // Convert BidsChannelData to tuple format
            def tuple = item instanceof BidsChannelData ?
                item.toChannelTuple(loopOverEntities) : item

            bidsLogger.logProgress("Unifying item: ${tuple}")

            if (tuple == null) {
                bidsLogger.warn("Null tuple encountered in unifyResults, skipping item: ${item}")
                return
            }

            if (!(tuple instanceof List) || ((List)tuple).size() < 2) {
                bidsLogger.warn("Invalid tuple format in unifyResults: ${tuple} (type: ${tuple?.getClass()?.name}), skipping")
                return
            }

            List tupleList = (List)tuple
            def groupingKey = tupleList[0]
            def enrichedData = tupleList[1]

            if (!grouped.containsKey(groupingKey)) {
                grouped[groupingKey] = []
            }
            grouped[groupingKey] << enrichedData
        }

        // Create unified queue with merged data
        DataflowQueue unifiedQueue = new DataflowQueue()

        grouped.each { groupingKey, dataList ->
            Map<String, String> entityValues = extractEntityValues(groupingKey, loopOverEntities)

            // Merge all enriched data maps and file paths
            Map<String, Object> mergedDataMap = [:]
            List<String> allFilePaths = []

            dataList.each { enrichedData ->
                Map enrichedMap = enrichedData as Map
                def dataMap = enrichedMap.data as Map
                def filePaths = enrichedMap.filePaths as List

                dataMap.each { suffix, suffixData ->
                    mergedDataMap[(String)suffix] = suffixData
                }

                allFilePaths.addAll(filePaths as List<String>)
            }

            def enrichedData = [
                data: mergedDataMap,
                filePaths: allFilePaths.unique(),
                bidsParentDir: getBidsParentDir()
            ]

            // Add entity values
            entityValues.each { entity, value ->
                enrichedData[entity] = value
            }

            unifiedQueue.bind([groupingKey, enrichedData])  // Use .bind() not <<
        }

        // Return unbound queue - will be consumed by applyCrossModalBroadcasting
        return unifiedQueue
    }

    /**
     * Apply include_cross_modal configuration
     *
     * Adds requested cross-modal data to task-specific channels
     *
     * @param enrichedData Current channel data
     * @param entityValues Entity values for this channel
     * @param available Available cross-modal data
     * @param config Configuration map
     * @return Enhanced data with cross-modal includes
     *
     * @reference Cross-modal inclusion:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L166-L183
     */
    private Map applyIncludeCrossModal(
            Map enrichedData,
            Map entityValues,
            Map available,
            Map config) {

        Map enhanced = new LinkedHashMap(enrichedData)
        Map originalData = enrichedData.data as Map
        enhanced.data = new LinkedHashMap(originalData)

        // For task-specific channels, check include_cross_modal requests
        if (entityValues.task != "NA") {
            (enrichedData.data as Map).each { suffix, suffixData ->
                def suffixConfig = config[suffix]
                if (suffixConfig instanceof Map) {
                    Map suffixCfgMap = suffixConfig as Map
                    def setCfg = suffixCfgMap.plain_set ?: suffixCfgMap.named_set ?: 
                               suffixCfgMap.sequential_set ?: suffixCfgMap.mixed_set

                    Map setCfgMap = setCfg as Map
                    if (setCfgMap?.include_cross_modal) {
                        (setCfgMap.include_cross_modal as List).each { requestedSuffix ->
                            if (available.containsKey(requestedSuffix)) {
                                (enhanced.data as Map)[(String)requestedSuffix] = available[(String)requestedSuffix]
                            }
                        }
                    }
                }
            }
        }

        return enhanced
    }

    /**
     * Determine if a channel should be kept in final results
     *
     * Task="NA" channels are only kept if they contain data not requested
     * by other channels via include_cross_modal
     *
     * @param enrichedData Channel data
     * @param entityValues Entity values
     * @param config Configuration map
     * @return true if channel should be emitted
     *
     * @reference Channel filtering:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L185-L218
     */
    private boolean shouldKeepChannel(Map enrichedData, Map entityValues, Map config) {
        // Always keep non-task channels
        if (entityValues.task != "NA") {
            return true
        }

        // For task="NA", only keep if has non-requested data
        boolean hasNonRequestedData = (enrichedData.data as Map).any { suffix, suffixData ->
            def wasRequested = false
            config.each { otherSuffix, otherSuffixConfig ->
                if (otherSuffix != suffix && otherSuffixConfig instanceof Map) {
                    def otherSetCfg = otherSuffixConfig.plain_set ?: 
                                     otherSuffixConfig.named_set ?: 
                                     otherSuffixConfig.sequential_set ?: 
                                     otherSuffixConfig.mixed_set

                    Map otherSetCfgMap = otherSetCfg as Map
                    if (otherSetCfgMap?.include_cross_modal && (otherSetCfgMap.include_cross_modal as List).contains(suffix)) {
                        wasRequested = true
                    }
                }
            }
            return !wasRequested
        }

        return hasNonRequestedData
    }

    /**
     * Extract entity values from grouping key
     *
     * @param groupingKey Tuple key from channel
     * @param loopOverEntities List of entity names
     * @return Map of entity name to value
     *
     * @reference Entity extraction:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L98-L101
     */
    private Map<String, String> extractEntityValues(Object groupingKey, List<String> loopOverEntities) {
        def entityValues = [:]
        loopOverEntities.eachWithIndex { entity, index ->
            List groupingKeyList = groupingKey as List
            entityValues[(String)entity] = groupingKeyList[index] as String ?: "NA"
        }
        return entityValues as Map<String, String>
    }

    /**
     * Create non-task grouping key for cross-modal broadcasting
     *
     * @param entityValues Map of entity values
     * @param loopOverEntities List of entities
     * @return Non-task key string
     *
     * @reference Key creation:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L133-L141
     */
    private String constructNonTaskKey(Map<String, String> entityValues, List<String> loopOverEntities) {
        List<String> nonTaskEntities = loopOverEntities.findAll { it != 'task' } as List<String>
        List<String> nonTaskKey = nonTaskEntities.collect { entity ->
            entityValues[entity] ?: "NA"
        }
        return nonTaskKey.join('_')
    }

    /**
     * Get BIDS parent directory from session
     *
     * @return Parent directory path
     */
    private String getBidsParentDir() {
        return bidsDir ? new File(bidsDir).parent : ""
    }

    /**
     * Transfer items from one DataflowQueue to another
     *
     * @param source Source queue
     * @param destination Destination queue
     */
    private void transferQueueItems(DataflowQueue source, DataflowQueue destination) {
        source.each { item ->
            destination << item
        }
    }

    /**
     * Load and analyze configuration from file or use defaults
     *
     * @param configPath Path to YAML configuration file
     * @return Parsed configuration map
     *
     * @reference Config loading:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L46-L48
     */
    protected void loadConfiguration(String configPath) {
        BidsConfigLoader configLoader = new BidsConfigLoader()
        if (configPath) {
            this.config = configLoader.load(configPath)
        } else {
            this.config = configLoader.loadDefaults()
        }

        // Build suffix mapping from configuration
        this.suffixMapping = SuffixMapper.buildSuffixMapping(config)
        if (suffixMapping && !suffixMapping.isEmpty()) {
            bidsLogger.logProgress("bids2nf", "Built suffix mappings: ${suffixMapping}")
        }

        BidsConfigAnalyzer configAnalyzer = new BidsConfigAnalyzer()
        this.configAnalysis = configAnalyzer.analyzeConfiguration(config)
        this.loopOverEntities = configAnalyzer.getLoopOverEntities(config)

        Map summary = configAnalyzer.getConfigurationSummary(config)
        logConfigurationSummary(config, summary, loopOverEntities)
    }

    /**
     * Log configuration summary with pretty formatting
     *
     * @param config Configuration map
     * @param loopOverEntities List of entities to loop over
     *
     * @reference Logging format:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L61-L67
     */
    private void logConfigurationSummary(Map config, Map summary, List<String> loopOverEntities) {
        bidsLogger.logProgress("bids2nf", "┌─ ✓ Configuration analysis complete:")
        bidsLogger.logProgress("bids2nf", "├─ ↬ Loop over entities: ${loopOverEntities.join(', ')}")
        
        Map namedSets = summary.namedSets as Map
        Map sequentialSets = summary.sequentialSets as Map
        Map mixedSets = summary.mixedSets as Map
        Map plainSets = summary.plainSets as Map
        
        bidsLogger.logProgress("bids2nf", "├─ ⑆ Named sets: ${namedSets.count} patterns (${(namedSets.suffixes as List).join(', ')})")
        bidsLogger.logProgress("bids2nf", "├─ ⑇ Sequential sets: ${sequentialSets.count} patterns (${(sequentialSets.suffixes as List).join(', ')})")
        bidsLogger.logProgress("bids2nf", "├─ ⑈ Mixed sets: ${mixedSets.count} patterns (${(mixedSets.suffixes as List).join(', ')})")
        bidsLogger.logProgress("bids2nf", "├─ ⑉ Plain sets: ${plainSets.count} patterns (${(plainSets.suffixes as List).join(', ')})")
        bidsLogger.logProgress("bids2nf", "├─ = TOTAL patterns: ${summary.totalPatterns}")
    }

}
