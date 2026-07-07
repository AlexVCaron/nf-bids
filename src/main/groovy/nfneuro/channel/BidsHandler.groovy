package nfneuro.plugin.channel

import java.util.concurrent.CompletableFuture

import groovy.lang.GString
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowWriteChannel
import groovyx.gpars.dataflow.DataflowQueue

import nextflow.NF
import nextflow.Global
import nextflow.Session
import nextflow.Channel
import nextflow.extension.CH
import nextflow.file.FileHelper
import nextflow.util.VersionNumber
import nextflow.BuildInfo

import java.nio.file.Path
import java.nio.file.Paths

import nfneuro.plugin.grouping.*
import nfneuro.plugin.model.*
import nfneuro.plugin.parser.BidsParser
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.util.ParticipantsMetadataMerger
import nfneuro.plugin.util.SuffixMapper
import nfneuro.plugin.config.BidsConfigAnalyzer
import nfneuro.plugin.config.BidsConfigLoader

/**
 * Fluent builder that orchestrates the full BIDS-to-channel pipeline.
 *
 * <p>Typical usage (via {@link BidsChannelFactory}):</p>
 * <pre>
 * new BidsHandler()
 *     .withConfig(configPath)
 *     .withBidsDir(bidsDir)
 *     .withOpts(options)
 *     .withParser(new BidsParser(session))
 *     .ignite(session)
 * </pre>
 *
 * <p>Internally the handler:</p>
 * <ol>
 *   <li>Loads and validates the YAML configuration via {@link nfneuro.plugin.config.BidsConfigLoader}.</li>
 *   <li>Parses the BIDS directory into {@link nfneuro.plugin.model.BidsFile} objects via {@link nfneuro.plugin.parser.BidsParser}.</li>
 *   <li>Routes files to the correct {@link nfneuro.plugin.grouping.BaseSetHandler} sub-class
 *       (plain / named / sequential / mixed) depending on the configuration analysis.</li>
 *   <li>Applies cross-modal broadcasting and emits a flat {@code [meta, data…]} map per group.</li>
 * </ol>
 */
@Slf4j
@CompileStatic
class BidsHandler {

    private DataflowWriteChannel target
    private String bidsDir
    private Map options
    private Map config
    private Map configAnalysis
    private List<String> loopOverEntities
    private Map<String, Map<String, String>> suffixMapping
    private BidsParser parser
    private List<Map<String, String>> participantsMetadata = []
    private ParticipantsMetadataMerger participantsMetadataMerger = new ParticipantsMetadataMerger()

    /**
     * Load and analyze the YAML configuration file.
     *
     * @param configPath path to a {@code bids2nf.yaml} configuration file, or {@code null} to use defaults
     * @return {@code this} for method chaining
     */
    BidsHandler withConfig(String configPath) {
        // Load and analyze configuration
        loadConfiguration(configPath)
        return this
    }

    /**
     * Set the path to the BIDS dataset directory.
     *
     * @param bidsDir absolute path to the root of the BIDS dataset
     * @return {@code this} for method chaining
     */
    BidsHandler withBidsDir(String bidsDir) {
        this.bidsDir = bidsDir
        return this
    }

    /**
     * Supply the runtime options map (e.g. {@code libbids_sh}, {@code flatten_output}).
     *
     * @param options map of additional options forwarded from {@code Channel.fromBIDS()}
     * @return {@code this} for method chaining
     */
    BidsHandler withOpts(Map options) {
        this.options = options ?: [:]
        this.participantsMetadataMerger = new ParticipantsMetadataMerger(this.options.entity_aliases_json as String)
        return this
    }

    /**
     * Override the output channel (used internally by {@link #ignite}).
     *
     * @param target the Dataflow write channel to bind results into
     * @return {@code this} for method chaining
     */
    BidsHandler withTarget(DataflowWriteChannel target) {
        this.target = target
        return this
    }

    /**
     * Inject the {@link nfneuro.plugin.parser.BidsParser} to use for dataset parsing.
     *
     * @param parser pre-constructed parser instance
     * @return {@code this} for method chaining
     */
    BidsHandler withParser(BidsParser parser) {
        this.parser = parser
        return this
    }

    /**
     * Create the output channel and schedule asynchronous execution.
     *
     * <p>In DSL2 mode the work is deferred via {@code session.addIgniter}; in DSL1 it
     * runs immediately.  Either way the returned channel is ready to be consumed by
     * downstream operators before parsing has completed.</p>
     *
     * @param session the active Nextflow session
     * @return the {@code DataflowWriteChannel} that will receive BIDS data items
     */
    DataflowWriteChannel ignite(Session session) {
        DataflowWriteChannel target = this.withTarget(CH.create()).target

        if (Integer.parseInt(new VersionNumber(BuildInfo.version).major) >= 26 || NF.dsl2) {
            session.addIgniter { -> this.perform(true) }
        }
        else {
            this.perform(true)
        }

        return target
    }

    /**
     * Execute the BIDS parsing pipeline, optionally in a background thread.
     *
     * @param async {@code true} to run asynchronously via {@link java.util.concurrent.CompletableFuture} (default);
     *              {@code false} to block the calling thread
     * @return {@code this} for method chaining
     */
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

        BidsDataset dataset = parser.parseToDataset(
            bidsDir,
            options.libbids_sh as String,
            options.use_bidsignore as Boolean,
            options.use_default_ignores as Boolean
        )
        dataset.loadParticipants()
        List<BidsFile> bidsFiles = dataset.getFiles()
        this.participantsMetadata = (dataset.participants ?: []) as List<Map<String, String>>

        // Route to appropriate handlers based on configuration
        DataflowQueue results = processDatasets(getBidsParentDir(), bidsFiles, config, configAnalysis, loopOverEntities)

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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L238-L249
     */
    private void validateAndEmitChannel(DataflowQueue results) {
        int count = 0
        boolean shouldFlatten = true
        if (options?.containsKey('flatten_output')) {
            shouldFlatten = (options.flatten_output as Boolean)
        }

        // Consume items from results queue and bind each valid one to output
        results.each { item ->
            // CRITICAL: Never allow null into the channel
            if (item != null) {
                // If item is a tuple [groupingKey, enrichedData], flatten it
                try {
                    def tuple = item instanceof BidsChannelData ? item.toChannelTuple(loopOverEntities) : item
                    if (!shouldFlatten) {
                        target << item
                    } else {
                        if (tuple instanceof List && tuple.size() >= 2) {
                            def groupingKey = tuple[0]
                            def enrichedData = tuple[1] as Map
                            def entityValues = extractEntityValues(groupingKey, loopOverEntities)
                            def flat = flattenTupleToMap([groupingKey, enrichedData], entityValues)
                            target << flat
                        } else {
                            // Item is already flattened map; emit as-is
                            target << item
                        }
                    }
                } catch (Exception e) {
                    def entityValues = (item instanceof List && item.size() >= 1) ? extractEntityValues(item[0], loopOverEntities) : [:]
                    throw new RuntimeException("Failed to flatten BIDS data for entities ${entityValues}: ${e.message}", e)
                }
                count++
            }
        }

        if (count == 0) {
            throw new IllegalStateException(
                '├─ ⛔️ ERROR\n' +
                '[nf-bids-handler] └─ No data groups were processed! This could indicate:\n' +
                '  - No files match the configured patterns in bids2nf.yaml\n' +
                '  - Incorrect BIDS directory structure\n' +
                '  - Configuration issues with entity matching\n' +
                '  - Missing required files for complete groupings'
            )
        }

        BidsLogger.logProgress('nf-bids-handler', '├─ ✅ SUCCESS')
        BidsLogger.logProgress('nf-bids-handler', "└─ nf-bids job complete: ${count} data groups processed")

        // Signal completion with poison pill - this is the ONLY way to close the channel
        target << Channel.STOP
    }

    /**
     * Recursively deep-copy a data structure to prevent concurrent modification issues.
     * Copies all Maps and Lists, leaving other types as-is (Path, String, Number, etc.).
     *
     * This is necessary because nf-test's convertPathsToStrings function recursively
     * iterates over nested maps using collectEntries, which can cause ConcurrentModificationException
     * if the maps are shared references that could be mutated.
     *
     * @param obj Object to deep-copy
     * @return Deep-copied version of obj
     */
    private Object deepCopy(Object obj) {
        if (obj == null) {
            return null
        }
        if (obj instanceof Map) {
            Map result = obj instanceof LinkedHashMap ? new LinkedHashMap() : [:]
            (obj as Map).each { k, v -> result[k] = deepCopy(v) }
            return result
        }
        if (obj instanceof List) {
            return (obj as List).collect { deepCopy(it) }
        }
        // Return other types as-is (Path, String, Number, Boolean, etc.)
        return obj
    }

    /**
     * Flatten a channel tuple into nested map structure with `meta` and `data` keys.
     *
     * @param tuple A tuple of [groupingKey, enrichedData]
     * @param entityValues Map of loop entity values (used to build meta)
     * @return Map with keys [meta: {...}, data: {...}]
     */
    private Map flattenTupleToMap(List tuple, Map<String, String> entityValues) {
        List groupingKey = tuple[0] as List
        Map enrichedData = tuple[1] as Map

        String bidsParentDir = enrichedData.bidsParentDir as String

        // Build meta map from entity values and enrich with any entity keys present in enrichedData
        Map meta = [:]
        (entityValues ?: [:]).each { k, v -> meta[k] = v }

        // Merge any top-level entries from enrichedData (excluding known control keys)
        List reserved = ['data', 'filePaths', 'bidsParentDir']
        (enrichedData as Map).each { k, v ->
            if (!(k in reserved) && v != null) {
                // Only add scalar or simple entries (strings, numbers) to meta
                if (!(v instanceof Map) && !(v instanceof List)) {
                    meta[(String)k] = v
                }
            }
        }
        participantsMetadataMerger.mergeIntoMeta(meta, participantsMetadata, loopOverEntities)

        // Clone data map so original structure is not mutated; we will move suffixes to top level
        Map dataCopy = [:]

        /**
         * Convert values to Nextflow-compatible types for channel emission.
         * 
         * File paths are converted to java.nio.file.Path objects (not java.io.File)
         * to ensure compatibility with Nextflow process path inputs and to support
         * remote file systems (S3, GCS, Azure).
         * 
         * Uses FileHelper.asPath() which:
         * - Handles local files and remote URIs (s3://, gs://, az://)
         * - Auto-loads required plugins (nf-amazon, nf-google, nf-azure)
         * - Resolves relative paths against bidsParentDir
         * 
         * @param val Value to convert (String path, Path, List, Map, or other)
         * @return Converted value with Path objects for file paths
         */
        boolean unpackJsonSidecar = options?.get('unpack_json_sidecar') as boolean

        Closure convertValue
        convertValue = { Object val ->
            if (val == null) return null
            if (val instanceof Path) return val  // Already a Path, return as-is
            if (val instanceof File) {
                return (val as File).toPath()
            }
            if (val instanceof String || val instanceof GString) {
                String pathStr = val.toString()

                // Use FileHelper.asPath for robust path handling
                // Handles local files, URIs (s3://, gs://, az://), absolute and relative paths
                Path result
                if (pathStr.contains('://') || pathStr.startsWith('/')) {
                    // Absolute path or URI - parse directly
                    result = FileHelper.asPath(pathStr)
                } else {
                    // Relative path - resolve against bidsParentDir
                    String fullPath = bidsParentDir
                        ? Paths.get(bidsParentDir, pathStr).toString()
                        : pathStr
                    result = FileHelper.asPath(fullPath)
                }

                // When unpack_json_sidecar is enabled, parse .json files into maps
                if (unpackJsonSidecar && pathStr.endsWith('.json')) {
                    File jsonFile = result.toFile()
                    if (jsonFile.exists()) {
                        try {
                            return new JsonSlurper().parse(jsonFile) as Map
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse JSON sidecar '${result}': ${e.message}", e)
                        }
                    }
                }

                return result  // Returns java.nio.file.Path
            }
            if (val instanceof List) {
                return (val as List).collect { convertValue.call(it) }
            }
            if (val instanceof Map) {
                Map nested = [:]
                (val as Map).each { k2, v2 -> nested[(String)k2] = convertValue.call(v2) }
                return nested
            }
            return val
        }

        (enrichedData.data as Map).each { suffix, suffixData ->
            dataCopy[(String)suffix] = convertValue.call(suffixData)
        }

        // Build final flattened map and return; embed suffix maps at top level (not under 'data')
        Map<String, Object> flat = new LinkedHashMap<String, Object>()
        flat.put('meta', meta)
        (dataCopy as Map<String, Object>).each { String k, Object v -> flat.put(k, v) }

        // Deep-copy the entire structure to prevent concurrent modification issues when nf-test
        // processes the output with convertPathsToStrings
        return deepCopy(flat) as Map
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L69-L92
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L119-L236
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

            List tupleList = tuple as List
            List groupingKey = tupleList[0]
            Map enrichedData = tupleList[1] as Map
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
            BidsLogger.logProgress("nf-bids-handler", "├─ Applying broadcasting for looping key: ${loopingKey} with ${groupEntries.size()} entries")
            Map<String, Object> availableData = [:]
            crossModalData.findAll { key, value -> loopingKey.contains(key) }
                .each { availableKey, available ->
                    BidsLogger.logProgress("nf-bids-handler", "├─ Applying cross-modal broadcasting for key: ${loopingKey} with available data: ${availableKey} | ${available.keySet().join(', ')}")
                    availableData.putAll(available as Map<String, Object>)
                }

            groupEntries.each { entry ->
                List groupingKey = (entry as List)[0]
                Map enrichedData = (entry as List)[1] as Map
                BidsLogger.logProgress("nf-bids-handler", "├─ Enhancing entry for grouping key: ${groupingKey}")
                Map enhanced = applyIncludeCrossModal(
                    enrichedData,
                    availableData,
                    config
                )

                if (shouldKeepChannel(enhanced, config)) {
                    BidsLogger.logProgress("nf-bids-handler", "├─ Broadcasting enhanced data for key: ${groupingKey} with data: ${(enhanced.data as Map).keySet().join(', ')}")
                    broadcastedResults << [groupingKey, enhanced]
                }
            }
        }

        // Return unbound queue - will be bound only in validateAndEmitChannel
        return broadcastedResults
    }

    /**
     * Unify results by performing an outer join across items that belong to the same
     * loop-over group.
     *
     * <p>Items that share a loop-over {@code groupingKey} but carry <em>different</em>
     * data-key sets (fingerprints) originate from different set handlers or configKey
     * streams and must be combined.  Items that share both the {@code groupingKey}
     * <em>and</em> their fingerprint are <em>alternatives</em> for the same slot (e.g.
     * multiple runs of the same fmap suffix) and must be kept separate.</p>
     *
     * <p>The result is the cross-product across distinct fingerprint groups, so that
     * every alternative in one stream is paired with every alternative in every other
     * stream (outer join behaviour).</p>
     *
     * @param results Combined channel results
     * @param loopOverEntities Entities to group by
     * @return Unified channel with outer-joined data
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

        // Create unified queue using outer join (cross-product) across fingerprint groups
        DataflowQueue unifiedQueue = new DataflowQueue()

        grouped.each { groupingKey, dataList ->
            Map<String, String> entityValues = extractEntityValues(groupingKey, loopOverEntities)

            // Group items by their data-key fingerprint (set of suffix/configKey names).
            // Items with the same fingerprint are alternatives for the same slot;
            // items with different fingerprints come from independent streams and
            // must be cross-product joined.
            Map fingerprintGroups = new LinkedHashMap()
            dataList.each { enrichedData ->
                def fingerprint = ((enrichedData as Map).data as Map).keySet()
                if (!fingerprintGroups.containsKey(fingerprint)) {
                    fingerprintGroups[fingerprint] = []
                }
                (fingerprintGroups[fingerprint] as List) << (enrichedData as Map)
            }

            BidsLogger.logProgress("nf-bids-handler",
                "├─ Outer-joining ${fingerprintGroups.size()} fingerprint group(s) for key ${groupingKey}")

            // Compute cross-product across the fingerprint streams
            List combinations = computeOuterJoin(fingerprintGroups.values().toList())

            combinations.each { combo ->
                Map<String, Object> mergedDataMap = [:]
                List<String> allFilePaths = []

                (combo as List).each { enrichedData ->
                    ((enrichedData as Map).data as Map).each { suffix, suffixData ->
                        mergedDataMap[(String)suffix] = suffixData
                    }
                    allFilePaths.addAll((enrichedData as Map).filePaths as List<String>)
                }

                Map enrichedDataOut = [
                    data: mergedDataMap,
                    filePaths: allFilePaths.unique(),
                    bidsParentDir: getBidsParentDir()
                ]

                // Add loop-over entity values
                entityValues.each { entity, value ->
                    enrichedDataOut[entity] = value
                }

                unifiedQueue << [groupingKey, enrichedDataOut]
            }
        }

        // Return unbound queue - will be consumed by applyCrossModalBroadcasting
        return unifiedQueue
    }

    /**
     * Compute the cross-product (outer join) of a list of alternative streams.
     *
     * <p>Each element of {@code streams} is a list of alternative items for one
     * "slot".  The result contains one entry for every combination that picks
     * exactly one item from each slot.</p>
     *
     * @param streams List of alternative-item lists, one per fingerprint group
     * @return All combinations, each being a list of one item per stream
     */
    private List computeOuterJoin(List streams) {
        if (!streams) {
            return [[]]
        }
        List first = streams[0] as List
        List rest = computeOuterJoin(streams.subList(1, streams.size()))
        List result = []
        first.each { item ->
            rest.each { combo ->
                result << ([item] + (combo as List))
            }
        }
        return result
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L166-L183
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L185-L218
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L98-L101
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L133-L141
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
        return bidsDir ? new File(bidsDir).parent : ''
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L46-L48
     */
    protected void loadConfiguration(String configPath) {
        BidsConfigLoader configLoader = new BidsConfigLoader()
        if (configPath) {
            this.config = configLoader.load(configPath)
        } else {
            this.config = configLoader.loadDefaults()
        }

        // Build suffix mapping from configuration
        this.suffixMapping = SuffixMapper.suffixMapping(config)
        if (suffixMapping && !suffixMapping.isEmpty()) {
            BidsLogger.logProgress("nf-bids-handler", "├─ Built suffix mappings: ${suffixMapping}")
        }

        BidsConfigAnalyzer configAnalyzer = new BidsConfigAnalyzer()
        this.configAnalysis = configAnalyzer.analyzeConfiguration(config)
        this.loopOverEntities = configAnalyzer.getLoopOverEntities(config)

        Map summary = configAnalyzer.getConfigurationSummary(config)
        logConfigurationSummary(summary, loopOverEntities)
    }

    /**
     * Log configuration summary with pretty formatting
     *
     * @param config Configuration map
     * @param loopOverEntities List of entities to loop over
     *
     * @reference Logging format:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L61-L67
     */
    private void logConfigurationSummary(Map summary, List<String> loopOverEntities) {
        final String handler = 'nf-bids-handler'
        final String separator = ', '

        BidsLogger.logProgress(handler, '┌─ ✓ Configuration analysis complete:')
        BidsLogger.logProgress(handler, "├─ ↬ Loop over entities: ${loopOverEntities.join(separator)}")

        Map namedSets = summary.namedSets as Map
        Map sequentialSets = summary.sequentialSets as Map
        Map mixedSets = summary.mixedSets as Map
        Map plainSets = summary.plainSets as Map

        String namedSetMsg = "├─ ⑆ Named sets: ${namedSets.count} patterns " +
            "(${(namedSets.suffixes as List).join(separator)})"
        BidsLogger.logProgress(handler, namedSetMsg)

        String seqSetMsg = "├─ ⑇ Sequential sets: ${sequentialSets.count} patterns " +
            "(${(sequentialSets.suffixes as List).join(separator)})"
        BidsLogger.logProgress(handler, seqSetMsg)

        String mixedSetMsg = "├─ ⑈ Mixed sets: ${mixedSets.count} patterns " +
            "(${(mixedSets.suffixes as List).join(separator)})"
        BidsLogger.logProgress(handler, mixedSetMsg)

        String plainSetMsg = "├─ ⑉ Plain sets: ${plainSets.count} patterns " +
            "(${(plainSets.suffixes as List).join(separator)})"
        BidsLogger.logProgress(handler, plainSetMsg)

        BidsLogger.logProgress(handler, "├─ = TOTAL patterns: ${summary.totalPatterns}")
    }

}
