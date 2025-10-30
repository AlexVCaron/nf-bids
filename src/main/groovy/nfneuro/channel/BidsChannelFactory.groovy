package nfneuro.plugin.channel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.NF
import nextflow.Channel
import nextflow.Session
import nextflow.extension.CH
import nextflow.plugin.extension.Factory
import nfneuro.plugin.parser.BidsParser
import nfneuro.plugin.util.BidsLogger
import groovyx.gpars.dataflow.DataflowWriteChannel

/**
 * Channel factory for creating BIDS-structured Nextflow channels
 *
 * Main entry point for Channel.fromBIDS() functionality
 * Orchestrates BIDS parsing, grouping, and channel emission
 *
 * @reference Main workflow implementation:
 *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf
 */
@Slf4j
// @CompileStatic - TODO: Complex dynamic features require significant refactoring:
//   - Multiple assignment destructuring (def (a,b) = list)
//   - Dynamic Map property access (map.property vs map['property'])
//   - Dynamic list operations (groupingKey[index])
//   - Collection type inference challenges
//   Recommend refactoring to use explicit data classes instead of dynamic Maps
class BidsChannelFactory {

    private final Session session
    private String currentBidsDir
    private BidsLogger bidsLogger

    BidsChannelFactory(Session session) {
        this.session = session
        this.bidsLogger = new BidsLogger()
    }

    /**
     * Create a channel from a BIDS dataset
     *
     * Main factory method that implements the complete bids2nf workflow
     *
     * @param bidsDir Path to BIDS dataset directory
     * @param configPath Path to bids2nf YAML configuration (optional)
     * @param options Additional options map (validation, libbids_sh path, etc.)
     * @return DataflowWriteChannel containing structured BIDS data
     *
     * @reference Workflow orchestration:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L20-L56
     */
    @Factory
    DataflowWriteChannel fromBIDS(String bidsDir, String configPath = null, Map options = [:]) {
        bidsLogger.logProgress("bids2nf", "Starting BIDS dataset parsing: ${bidsDir}")

        // Pre-flight checks
        preFlightChecks(bidsDir, configPath, options)

        // BIDS validation (optional)
        // if (options.bids_validation != false) {
        //     validator.validate(bidsDir, options.ignore_codes ?: [99, 36])
        // } else {
        //     bidsLogger.logProgress("bids2nf",
        //         "---------------------------\n" +
        //         "[bids2nf] ⚠︎⚠︎⚠︎ BIDS validation disabled by configuration ⚠︎⚠︎⚠︎\n" +
        //         "[bids2nf] ---------------------------\n")
        // }

        // Store bidsDir for later use
        this.currentBidsDir = bidsDir

        def target = CH.create()
        final handler = new BidsHandler()
            .withConfig(configPath)
            .withBidsDir(bidsDir)
            .withOpts(options)
            .withParser(new BidsParser(session))
            .withTarget(target)

        if (NF.dsl2) {
            session.addIgniter{ -> handler.perform(true) }
        }
        else {
            handler.perform(true)
        }

        return target
    }

    /**
     * Perform pre-flight validation checks
     * 
     * Validates BIDS directory, configuration file, and libBIDS.sh availability
     * 
     * @param bidsDir Path to BIDS dataset
     * @param configPath Path to configuration file
     * @param options Options map
     * 
     * @reference Validation logic: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/bids_validator.nf#L55-L85
     */
    private void preFlightChecks(String bidsDir, String configPath, Map options) {
        bidsLogger.logProgress("bids2nf", "✈︎✈︎✈︎ Pre-flight checks started")
        
        // Validate BIDS directory exists
        File bidsPath = new File(bidsDir)
        if (!bidsPath.exists() || !bidsPath.isDirectory()) {
            throw new IllegalArgumentException("BIDS directory does not exist: ${bidsDir}")
        }
        
        // Validate configuration file if provided
        if (configPath) {
            def configFile = new File(configPath)
            if (!configFile.exists()) {
                throw new IllegalArgumentException("Configuration file not found: ${configPath}")
            }
        }
        
        // Validate libBIDS.sh if required
        if (options.libbids_sh) {
            def libBidsPath = new File(options.libbids_sh as String)
            if (!libBidsPath.exists()) {
                throw new IllegalArgumentException("libBIDS.sh not found: ${options.libbids_sh}")
            }
        }
        
        bidsLogger.logProgress("bids2nf", "✓ Pre-flight checks completed")
    }

}
