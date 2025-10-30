package nfneuro.plugin.config

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.config.ConfigValidator

/**
 * Configuration loader for BIDS workflows
 * 
 * Loads and validates YAML configuration files
 * 
 * @reference Configuration loading: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/bids2nf.yaml
 */
@Slf4j
@CompileStatic
class BidsConfigLoader {
    
    private final Yaml yaml
    
    BidsConfigLoader() {
        this.yaml = new Yaml()
    }
    
    /**
     * Load configuration from YAML file
     * 
     * @param configPath Path to bids2nf.yaml configuration file
     * @return Parsed configuration map
     * 
     * @reference Config loading with error handling: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L46-L48
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L35-L48
     */
    Map load(String configPath) {
        try {
            def configFile = new File(configPath)
            if (!configFile.exists()) {
                throw new FileNotFoundException("Configuration file not found: ${configPath}")
            }
            
            def config = yaml.load(new FileReader(configFile)) as Map
            
            // Validate configuration structure and values
            def validationResult = ConfigValidator.validate(config)
            if (!validationResult.isValid()) {
                def errorMsg = "Configuration validation failed for ${configPath}:\n${validationResult}"
                log.error(errorMsg)
                throw new IllegalArgumentException(errorMsg)
            }
            
            // Log warnings if any
            if (validationResult.warnings && !validationResult.warnings.isEmpty()) {
                log.warn("Configuration warnings for ${configPath}:\n${validationResult.warnings.join('\n  ')}")
            }
            
            log.info("Loaded BIDS configuration from: ${configPath}")
            log.info("Configuration keys: ${config.keySet()}")
            config.each { k, v ->
                log.info("  ${k}: ${v?.getClass()?.simpleName} = ${v}")
            }
            return config
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors directly (don't wrap)
            throw e
        } catch (FileNotFoundException e) {
            // Re-throw file not found directly
            throw e
        } catch (Exception e) {
            // Wrap other exceptions
            throw new RuntimeException("Failed to load BIDS configuration from ${configPath}: ${e.message}", e)
        }
    }
    
    /**
     * Load default configuration
     * 
     * Provides sensible defaults when no configuration file is specified.
     * Note: Defaults typically contain only metadata (loop_over, etc.) without
     * suffix configurations, so validation is skipped.
     * 
     * @return Default configuration map
     */
    Map loadDefaults() {
        log.info("Using default BIDS configuration")
        
        def defaultConfig = [
            loop_over: ['subject', 'session', 'run', 'task'],
            // Add other default settings as needed
        ]
        
        // Note: We don't validate defaults because they typically don't contain
        // suffix configurations (plain_set, named_set, etc.), only top-level
        // metadata like loop_over. The validator expects at least one suffix config.
        
        return defaultConfig
    }
}
