package nfneuro.plugin.parser

// import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * BIDS dataset validator using bids-validator
 * 
 * ⚠️ FUTURE FEATURE - Deferred to v1.1
 * 
 * This is a stub implementation. Full BIDS validation functionality
 * will be implemented in v1.1. For now, users should:
 * 1. Run bids-validator manually before using the plugin, OR
 * 2. Disable validation in configuration (bids_validation: false)
 * 
 * Executes BIDS validation and handles ignore codes
 * 
 * @reference BIDS validation implementation: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/bids_validator.nf
 */
@Slf4j
// @CompileStatic // TODO v1.1: Implement full validation with bids-validator integration
class BidsValidator {
    
    private static final String VALIDATOR_IMAGE = "bids/validator:latest"
    
    /**
     * Validate BIDS dataset structure
     * 
     * ⚠️ STUB IMPLEMENTATION - v1.1 Feature
     * 
     * Currently logs a warning. Full validation will be implemented in v1.1.
     * Runs bids-validator (typically in Docker) with specified ignore codes
     * 
     * @param bidsDir Path to BIDS dataset
     * @param ignoreCodes List of BIDS validation codes to ignore
     * 
     * @reference BIDS_VALIDATOR process: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/bids_validator.nf#L1-L30
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
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/bids_validator.nf#L17-L26
     */
    private String buildValidationCommand(String bidsDir, List<Integer> ignoreCodes) {
        def ignoreFlags = ignoreCodes.collect { "--ignoreWarnings ${it}" }.join(' ')
        
        // Try Docker command first, fall back to local bids-validator if available
        return """
            docker run --rm -v ${bidsDir}:/data:ro ${VALIDATOR_IMAGE} /data ${ignoreFlags}
        """.stripIndent().trim()
    }
    
    /**
     * Perform pre-flight checks before main workflow
     * 
     * Validates that required files and directories exist
     * 
     * @param bidsDir BIDS directory
     * @param configPath Configuration file path
     * @param libBidsPath libBIDS.sh path
     * 
     * @reference preFlightChecks function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/bids_validator.nf#L55-L85
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
