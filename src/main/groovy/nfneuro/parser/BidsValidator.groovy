package nfneuro.plugin.parser

// import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * BIDS dataset validator — stub implementation (deferred to v1.1).
 *
 * <p>In the current release the {@link #validate} method logs a warning and returns
 * immediately without performing actual validation.  Users should run
 * {@code bids-validator} manually, or set {@code bids_validation: false} in their
 * configuration to suppress the warning.</p>
 *
 * <p>The full Docker-based bids-validator integration is planned for v1.1.</p>
 */
@Slf4j
// @CompileStatic // TODO v1.1: Implement full validation with bids-validator integration
class BidsValidator {
    
    private static final String VALIDATOR_IMAGE = "bids/validator:latest"
    
    /**
     * Validate BIDS dataset structure.
     *
     * <p><strong>Stub — v1.1 feature.</strong>  Currently logs a warning and returns without
     * performing validation.  Full Docker-based bids-validator integration is planned for v1.1.</p>
     *
     * @param bidsDir    path to the BIDS dataset root directory
     * @param ignoreCodes list of BIDS validator error codes to suppress (no-op in current stub)
     */
    void validate(String bidsDir, List<Integer> ignoreCodes = []) {
        log.warn("⚠️ BIDS validation is not yet implemented (v1.1 feature). Skipping validation for: ${bidsDir}")
        log.info("To validate your dataset, please run 'bids-validator' manually or disable validation in config.")
        return  // v1.1: Remove this return and uncomment implementation below
        
        /* v1.1 Implementation:
        log.info("Running BIDS validation on: ${bidsDir}")
        
        try {
            def command = buildValidationCommand(bidsDir, ignoreCodes)
            def process = command.execute()
            
            def output = new StringBuilder()
            def errors = new StringBuilder()
            
            process.consumeProcessOutput(output, errors)
            def exitCode = process.waitFor()
            
            if (exitCode != 0) {
                log.warn("BIDS validation warnings/errors:\n${output}")
                log.warn("BIDS validation stderr:\n${errors}")
                
                // Don't fail on validation errors, just warn
                // Some projects may have acceptable deviations
            } else {
                log.info("BIDS validation passed successfully")
            }
            
        } catch (Exception e) {
            log.warn("BIDS validation could not be executed: ${e.message}")
            // Continue even if validation fails - it's a warning not a fatal error
        }
        */ // End v1.1 implementation
    }

    /**
     * Build validation command with ignore codes
     *
     * ⚠️ v1.1 Feature - Not yet implemented
     *
     * @param bidsDir BIDS directory
     * @param ignoreCodes Codes to ignore
     * @return Command string
     *
     * @reference Validation command:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/parsers/bids_validator.nf#L17-L26
     */
    // private String buildValidationCommand(String bidsDir, List<Integer> ignoreCodes) {
    //     def ignoreFlags = ignoreCodes.collect { "--ignoreWarnings ${it}" }.join(' ')

    //     // Try Docker command first, fall back to local bids-validator if available
    //     return """
    //         docker run --rm -v ${bidsDir}:/data:ro ${VALIDATOR_IMAGE} /data ${ignoreFlags}
    //     """.stripIndent().trim()
    // }

    /**
     * Perform pre-flight checks before the main workflow.
     *
     * <p>Verifies that the BIDS directory exists and is a directory, that
     * {@code dataset_description.json} is present (warns if not), that the
     * configuration file exists (if provided), and that {@code libBIDS.sh}
     * exists (if provided).</p>
     *
     * @param bidsDir     path to the BIDS dataset root directory
     * @param configPath  path to the {@code bids2nf.yaml} configuration file, or {@code null}
     * @param libBidsPath path to the {@code libBIDS.sh} script, or {@code null}
     * @throws FileNotFoundException    if a required path does not exist
     * @throws IllegalArgumentException if {@code bidsDir} is not a directory
     */
    void preFlightChecks(String bidsDir, String configPath, String libBidsPath) {
        log.info("✈︎✈︎✈︎ Pre-flight checks started")
        
        // Check BIDS directory
        def bidsPath = new File(bidsDir)
        if (!bidsPath.exists()) {
            throw new FileNotFoundException("BIDS directory not found: ${bidsDir}")
        }
        if (!bidsPath.isDirectory()) {
            throw new IllegalArgumentException("BIDS path is not a directory: ${bidsDir}")
        }
        
        // Check for dataset_description.json
        def datasetDesc = new File(bidsPath, "dataset_description.json")
        if (!datasetDesc.exists()) {
            log.warn("dataset_description.json not found - may not be a valid BIDS dataset")
        }
        
        // Check configuration file
        if (configPath) {
            def configFile = new File(configPath)
            if (!configFile.exists()) {
                throw new FileNotFoundException("Configuration file not found: ${configPath}")
            }
        }
        
        // Check libBIDS.sh
        if (libBidsPath) {
            def libBidsFile = new File(libBidsPath)
            if (!libBidsFile.exists()) {
                throw new FileNotFoundException("libBIDS.sh not found: ${libBidsPath}")
            }
        }
        
        log.info("✓ Pre-flight checks passed")
    }
}
