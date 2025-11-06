package nfneuro.plugin.channel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Channel
import nextflow.Session
import nextflow.plugin.extension.Factory
import nfneuro.plugin.parser.BidsParser
import nfneuro.plugin.util.BidsLogger
import groovyx.gpars.dataflow.DataflowWriteChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

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
@CompileStatic
class BidsChannelFactory {

    private final Session session

    BidsChannelFactory(Session session) {
        this.session = session
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
        BidsLogger.logProgress("Starting BIDS dataset parsing: ${bidsDir}")

        // Pre-flight checks
        preFlightChecks(bidsDir, configPath, options)

        // BIDS validation (optional)
        // if (options.bids_validation != false) {
        //     validator.validate(bidsDir, options.ignore_codes ?: [99, 36])
        // } else {
        //     BidsLogger.logProgress("nf-bids",
        //         "---------------------------\n" +
        //         "[nf-bids] ⚠︎⚠︎⚠︎ BIDS validation disabled by configuration ⚠︎⚠︎⚠︎\n" +
        //         "[nf-bids] ---------------------------\n")
        // }
        return new BidsHandler()
            .withConfig(configPath)
            .withBidsDir(bidsDir)
            .withOpts(options)
            .withParser(new BidsParser(session))
            .ignite(session)
    }

    /**
     * Perform pre-flight validation checks
     *
     * Validates BIDS directory, configuration file, and libBIDS.sh availability
     *
     * @param bidsDir Path to BIDS dataset
     * @param configPath Path to configuration file
     * @param options Options map
     */
    private void preFlightChecks(String bidsDir, String configPath, Map options) {
        BidsLogger.logProgress('✈︎✈︎✈︎ Pre-flight checks started')

        // Validate BIDS directory exists
        Path bidsPath = Paths.get(bidsDir)
        if (!Files.exists(bidsPath) || !Files.isDirectory(bidsPath)) {
            throw new IllegalArgumentException("BIDS directory does not exist: ${bidsDir}")
        }

        // Validate configuration file if provided
        if (configPath) {
            Path configFile = Paths.get(configPath)
            if (!Files.exists(configFile)) {
                throw new IllegalArgumentException("Configuration file not found: ${configPath}")
            }
        }

        // Validate libBIDS.sh if required
        if (options.libbids_sh) {
            Path libBidsPath = Paths.get(options.libbids_sh as String)
            if (!Files.exists(libBidsPath)) {
                throw new IllegalArgumentException("libBIDS.sh not found: ${options.libbids_sh}")
            }
        }

        BidsLogger.logProgress('✓ Pre-flight checks completed')
    }

}
