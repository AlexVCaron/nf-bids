package nfneuro.plugin.channel

import java.util.concurrent.CompletableFuture

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowWriteChannel
import groovyx.gpars.dataflow.DataflowQueue

import nextflow.NF
import nextflow.Global
import nextflow.Session
import nextflow.Channel
import nextflow.extension.CH

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

    DataflowWriteChannel ignite(Session session) {
        DataflowWriteChannel target = this.withTarget(CH.create()).target

        if (NF.dsl2) {
            session.addIgniter { -> this.perform(true) }
        }
        else {
            this.perform(true)
        }

        return target
    }

    BidsHandler perform(boolean async = true) {
        if (async) {
            this.executeAsync()
        } else {
            this.execute()
        }
        return this
    }

    static private void handlerException(Throwable e) {
        final error = e.cause ?: e
        log.error(error.message, error)
        final Session session = Global.session as Session
        session?.abort(error)
    }

    private void executeAsync() {
        CompletableFuture<Void> future = CompletableFuture.runAsync { execute() }
        future.exceptionally(this.&handlerException)
    }

    private void execute() {
        if (config == null) {
            loadConfiguration(null)
        }

        BidsDataset dataset = parser.parseToDataset(bidsDir, options.libbids_sh as String)
        List<BidsFile> bidsFiles = dataset.getFiles()

        // Route to appropriate handlers based on configuration
        DataflowQueue results = processDatasets(new File(bidsDir).parent, bidsFiles, config, configAnalysis, loopOverEntities)

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
                target << item
                count++
            }
        }

        if (count == 0) {
            throw new IllegalStateException(
                "├─ ⛔️ ERROR\n" +
                "[nf-bids-handler] └─ No data groups were processed! This could indicate:\n" +
                "  - No files match the configured patterns in bids2nf.yaml\n" +
                "  - Incorrect BIDS directory structure\n" +
                "  - Configuration issues with entity matching\n" +
                "  - Missing required files for complete groupings"
            )
        }

        BidsLogger.logProgress("nf-bids-handler", "├─ ✅ SUCCESS")
        BidsLogger.logProgress("nf-bids-handler", "└─ nf-bids job complete: ${count} data groups processed")

        // Signal completion with poison pill - this is the ONLY way to close the channel
        target << Channel.STOP
    }

    /**
     * Process datasets by routing to appropriate handlers
     *
     * Routes parsed data to named, sequential, mixed, or plain set handlers
     * based on configuration analysis
     *
     * @param datasetRoot Root path of BIDS dataset
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
            String datasetRoot,
            List<BidsFile> bidsFiles,
            Map config,
            Map analysis,
            List<String> loopOverEntities) {

        DataflowQueue combinedResults = new DataflowQueue()

        List<BaseSetHandler> handlers = []

        // Route to appropriate handlers and collect results
        if (analysis.hasNamedSets) {
            handlers << new NamedSetHandler()
        }

        if (analysis.hasSequentialSets) {
            handlers << new SequentialSetHandler()
        }

        if (analysis.hasMixedSets) {
            handlers << new MixedSetHandler()
        }

        if (analysis.hasPlainSets) {
            handlers << new PlainSetHandler()
        }

        handlers.each { handler ->
            BidsLogger.logProgress("nf-bids-handler", "├─ ⎌ Running handler: ${handler.getClass().simpleName} ...")
            DataflowQueue handlerResults = handler.process(
                datasetRoot,
                bidsFiles,
                config,
                loopOverEntities,
                suffixMapping
            )

            transferQueueItems(handlerResults, combinedResults)
        }

        return combinedResults
    }

    /**
     * Apply demand-driven cross-modal broadcasting
     *
     * Implements the cross-modal data inclusion logic where channels can request data from
     * others based on configuration
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

            BidsLogger.logProgress("nf-bids-handler", "├─ Processing item for cross-modal broadcasting: ${tuple}")

            if (tuple == null) {
                BidsLogger.logProgress("nf-bids-handler", "├─ Null tuple encountered in applyCrossModalBroadcasting, skipping item: ${item}")
                return
            }

            if (!(tuple instanceof List) || ((List)tuple).size() < 2) {
                BidsLogger.logProgress("nf-bids-handler", "├─ Invalid tuple format in applyCrossModalBroadcasting: ${tuple} (type: ${tuple?.getClass()?.name}), skipping")
                return
            }

            List tupleList = (List)tuple
            def groupingKey = tupleList[0]
            def enrichedData = tupleList[1] as Map
            Map<String, String> entityValues = extractEntityValues(groupingKey, loopOverEntities)

            String loopingKey = constructLoopingKey(entityValues, loopOverEntities)

            // Collect cross-modal data
            if (!crossModalData.containsKey(loopingKey)) {
                crossModalData[loopingKey] = [:]
            }

            Map enrichedDataMap = enrichedData as Map
            (enrichedDataMap.data as Map).each { suffix, suffixData ->
                crossModalData[loopingKey][(String)suffix] = suffixData
            }

            // Group all data
            if (!groupedData.containsKey(loopingKey)) {
                groupedData[loopingKey] = []
            }
            groupedData[loopingKey] << [groupingKey, enrichedData]
        }

        // Apply demand-driven broadcasting and create result queue
        DataflowQueue broadcastedResults = new DataflowQueue()

        groupedData.each { loopingKey, groupEntries ->
            def hasBroadcasted = false
            BidsLogger.logProgress("nf-bids-handler", "├─ Applying broadcasting for looping key: ${loopingKey} with ${groupEntries.size()} entries")
            crossModalData.findAll { key, value -> loopingKey.contains(key) }
                .each { availableKey, available ->
                    if (!hasBroadcasted) {
                        BidsLogger.logProgress("nf-bids-handler", "├─ Applying cross-modal broadcasting for key: ${loopingKey} with available data: ${availableKey} | ${available.keySet().join(', ')}")
                        groupEntries //.findAll { entry ->
                        //     def enrichedData = (entry as List)[1] as Map

                        //     return loopingKey != availableKey || (enrichedData.data as Map<String, Object>).keySet() != available.keySet()
                        // }
                        .each { entry ->
                            def groupingKey = (entry as List)[0]
                            def enrichedData = (entry as List)[1] as Map
                            BidsLogger.logProgress("nf-bids-handler", "├─ Enhancing entry for grouping key: ${groupingKey}")
                            def enhanced = applyIncludeCrossModal(
                                enrichedData,
                                available,
                                config
                            )

                            if (shouldKeepChannel(enhanced, config)) {
                                BidsLogger.logProgress("nf-bids-handler", "├─ Broadcasting enhanced data for key: ${groupingKey} with data: ${(enhanced.data as Map).keySet().join(', ')}")
                                broadcastedResults << [groupingKey, enhanced]
                                hasBroadcasted = true
                            }
                        }
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

            BidsLogger.logProgress("nf-bids-handler", "├─ Unifying item: ${tuple}")

            if (tuple == null) {
                BidsLogger.logProgress("nf-bids-handler", "├─ Null tuple encountered in unifyResults, skipping item: ${item}")
                return
            }

            if (!(tuple instanceof List) || ((List)tuple).size() < 2) {
                BidsLogger.logProgress("nf-bids-handler", "├─ Invalid tuple format in unifyResults: ${tuple} (type: ${tuple?.getClass()?.name}), skipping")
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
                Map dataMap = enrichedMap.data as Map
                List<String> filePaths = enrichedMap.filePaths as List

                dataMap.each { suffix, suffixData ->
                    mergedDataMap[(String)suffix] = suffixData
                }

                allFilePaths.addAll(filePaths as List<String>)
            }

            Map enrichedData = [
                data: mergedDataMap,
                filePaths: allFilePaths.unique(),
                bidsParentDir: getBidsParentDir()
            ]

            // Add entity values
            entityValues.each { entity, value ->
                enrichedData[entity] = value
            }

            unifiedQueue << [groupingKey, enrichedData]
        }

        // Return unbound queue - will be consumed by applyCrossModalBroadcasting
        return unifiedQueue
    }

    /**
     * Apply include_cross_modal configuration
     *
     * Adds requested cross-modal data
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
            Map available,
            Map config) {

        Map enhanced = new LinkedHashMap(enrichedData)
        Map originalData = enrichedData.data as Map
        enhanced.data = new LinkedHashMap(originalData)

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

        return enhanced
    }

    /**
     * Determine if a channel should be kept in final results
     *
     * Channels are only kept if they contain data not requested
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
    private boolean shouldKeepChannel(Map enrichedData, Map config) {
        // Only keep if has non-requested data
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
    private String constructLoopingKey(Map<String, String> entityValues, List<String> loopOverEntities) {
        return loopOverEntities.collect { entity -> entityValues[entity] ?: "NA" }
            .findAll { value -> value != "NA" }
            .join('_')
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
            BidsLogger.logProgress("nf-bids-handler", "├─ Built suffix mappings: ${suffixMapping}")
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
        BidsLogger.logProgress("nf-bids-handler", "┌─ ✓ Configuration analysis complete:")
        BidsLogger.logProgress("nf-bids-handler", "├─ ↬ Loop over entities: ${loopOverEntities.join(', ')}")

        Map namedSets = summary.namedSets as Map
        Map sequentialSets = summary.sequentialSets as Map
        Map mixedSets = summary.mixedSets as Map
        Map plainSets = summary.plainSets as Map

        BidsLogger.logProgress("nf-bids-handler", "├─ ⑆ Named sets: ${namedSets.count} patterns (${(namedSets.suffixes as List).join(', ')})")
        BidsLogger.logProgress("nf-bids-handler", "├─ ⑇ Sequential sets: ${sequentialSets.count} patterns (${(sequentialSets.suffixes as List).join(', ')})")
        BidsLogger.logProgress("nf-bids-handler", "├─ ⑈ Mixed sets: ${mixedSets.count} patterns (${(mixedSets.suffixes as List).join(', ')})")
        BidsLogger.logProgress("nf-bids-handler", "├─ ⑉ Plain sets: ${plainSets.count} patterns (${(plainSets.suffixes as List).join(', ')})")
        BidsLogger.logProgress("nf-bids-handler", "├─ = TOTAL patterns: ${summary.totalPatterns}")
    }

}
