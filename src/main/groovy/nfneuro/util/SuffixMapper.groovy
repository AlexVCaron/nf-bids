package nfneuro.plugin.util

import groovy.transform.CompileStatic

/**
 * Utility for mapping suffixes via suffix_maps_to configuration
 * 
 * This handles special cases where a configuration name differs from the actual
 * BIDS suffix it processes. For example, dwi_fullreverse maps to "dwi" suffix.
 * 
 * Example configurations:
 *   dwi_fullreverse:
 *     suffix_maps_to: "dwi"
 *     named_set: ...
 * 
 * @reference Special case configurations in bids2nf.yaml:
 *            https://github.com/AlexVCaron/bids2nf/blob/main/bids2nf.yaml#L290-L330
 */
@CompileStatic
class SuffixMapper {
    
    /**
     * Build suffix mapping from configuration
     * 
     * Scans all configuration entries for suffix_maps_to field and creates
     * a mapping of file suffix -> config key
     * 
     * @param config Full configuration map
     * @return Map of suffix -> config key (e.g., "dwi" -> "dwi_fullreverse")
     */
    static Map<String, String> buildSuffixMapping(Map config) {
        def mapping = [:] as Map<String, String>
        
        if (!config) {
            return mapping
        }
        
        config.each { configKey, configValue ->
            if (configValue instanceof Map) {
                def suffixConfig = configValue as Map
                
                // Check if this config has suffix_maps_to
                if (suffixConfig.suffix_maps_to) {
                    def targetSuffix = suffixConfig.suffix_maps_to as String
                    
                    // Map: actual file suffix -> configuration key
                    mapping[targetSuffix] = configKey as String
                    
                    BidsLogger.debug("Suffix mapping: ${targetSuffix} -> ${configKey}")
                }
            }
        }
        
        return mapping
    }
    
    /**
     * Resolve configuration key for a given file suffix
     * 
     * If suffix has a mapping (via suffix_maps_to), returns the mapped config key.
     * Otherwise, returns the suffix itself as the config key.
     * 
     * @param suffix File suffix from BIDS file
     * @param mapping Suffix mapping from buildSuffixMapping()
     * @return Configuration key to look up
     */
    static String resolveConfigKey(String suffix, Map<String, String> mapping) {
        // Handle null or empty mapping
        if (!mapping) {
            return suffix
        }
        
        // If there's a mapping for this suffix, use it
        if (mapping.containsKey(suffix)) {
            def mappedKey = mapping[suffix]
            BidsLogger.trace("Resolving suffix '${suffix}' to config key '${mappedKey}'")
            return mappedKey
        }
        
        // Otherwise, config key is same as suffix
        return suffix
    }
    
    /**
     * Get the actual suffix that should be used in output
     * 
     * For configurations with suffix_maps_to, this returns the target suffix
     * rather than the configuration key.
     * 
     * @param configKey Configuration key (may be like "dwi_fullreverse")
     * @param configValue Configuration value map
     * @return Actual suffix for output (e.g., "dwi")
     */
    static String getOutputSuffix(String configKey, Map configValue) {
        if (configValue?.suffix_maps_to) {
            return configValue.suffix_maps_to as String
        }
        return configKey
    }
}
