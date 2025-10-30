package nfneuro.plugin.config

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.util.BidsLogger

/**
 * Analyzes BIDS configuration to determine channel emission strategy
 * 
 * Determines how to group and emit BIDS files based on configuration
 * 
 * @reference Configuration analysis: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/config_analyzer.nf
 */
@Slf4j
@CompileStatic
class BidsConfigAnalyzer {
    
    /**
     * Analyze configuration to determine required workflows
     * 
     * Scans configuration for different set types and returns analysis
     * 
     * @param config Configuration map
     * @return Analysis map with hasNamedSets, hasSequentialSets, hasMixedSets, hasPlainSets
     * 
     * @reference analyzeConfiguration function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/config_analyzer.nf#L1-L40
     */
    Map analyzeConfiguration(Map config) {
        def hasNamedSets = false
        def hasSequentialSets = false
        def hasMixedSets = false
        def hasPlainSets = false
        
        log.info("Analyzing configuration with keys: ${config.keySet()}")
        config.each { key, value ->
            log.info("  Checking ${key}: ${value?.getClass()?.simpleName}")
            if (value instanceof Map) {
                log.info("    Value is a Map with keys: ${value.keySet()}")
                if (value.containsKey('named_set')) {
                    log.info("    Found named_set!")
                    hasNamedSets = true
                }
                if (value.containsKey('sequential_set')) {
                    log.info("    Found sequential_set!")
                    hasSequentialSets = true
                }
                if (value.containsKey('mixed_set')) {
                    log.info("    Found mixed_set!")
                    hasMixedSets = true
                }
                if (value.containsKey('plain_set')) {
                    log.info("    Found plain_set!")
                    hasPlainSets = true
                }
            }
        }
        
        log.info("Analysis results: named=${hasNamedSets}, sequential=${hasSequentialSets}, mixed=${hasMixedSets}, plain=${hasPlainSets}")
        
        return [
            hasNamedSets: hasNamedSets,
            hasSequentialSets: hasSequentialSets,
            hasMixedSets: hasMixedSets,
            hasPlainSets: hasPlainSets
        ]
    }
    
    /**
     * Extract loop_over entities from configuration
     * 
     * @param config Configuration map
     * @return List of entity names to loop over
     * 
     * @reference getLoopOverEntities function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/config_analyzer.nf#L42-L55
     */
    List<String> getLoopOverEntities(Map config) {
        def loopOver = config.loop_over
        
        if (loopOver instanceof List) {
            return loopOver as List<String>
        } else if (loopOver instanceof String) {
            return [loopOver as String]
        }
        
        // Default entities if not specified
        return ['subject', 'session', 'run', 'task']
    }
    
    /**
     * Generate configuration summary for logging
     * 
     * Creates a summary of all set types and their suffixes
     * 
     * @param config Configuration map
     * @return Summary map with counts and suffix lists
     * 
     * @reference getConfigurationSummary function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/config_analyzer.nf#L57-L100
     */
    Map getConfigurationSummary(Map config) {
        def namedSets = []
        def sequentialSets = []
        def mixedSets = []
        def plainSets = []
        
        config.each { suffix, value ->
            if (value instanceof Map) {
                if (value.containsKey('named_set')) {
                    namedSets << suffix
                }
                if (value.containsKey('sequential_set')) {
                    sequentialSets << suffix
                }
                if (value.containsKey('mixed_set')) {
                    mixedSets << suffix
                }
                if (value.containsKey('plain_set')) {
                    plainSets << suffix
                }
            }
        }
        
        return [
            namedSets: [
                count: namedSets.size(),
                suffixes: namedSets
            ],
            sequentialSets: [
                count: sequentialSets.size(),
                suffixes: sequentialSets
            ],
            mixedSets: [
                count: mixedSets.size(),
                suffixes: mixedSets
            ],
            plainSets: [
                count: plainSets.size(),
                suffixes: plainSets
            ],
            totalPatterns: namedSets.size() + sequentialSets.size() + 
                          mixedSets.size() + plainSets.size()
        ]
    }
}
