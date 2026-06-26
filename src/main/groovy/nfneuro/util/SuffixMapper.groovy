package nfneuro.plugin.util

import groovy.transform.CompileStatic

import nfneuro.plugin.grouping.BaseSetHandler

/**
 * Resolves {@code suffix_maps_to} aliases in the {@code bids2nf.yaml} configuration.
 *
 * <p>When a configuration key (virtual suffix) differs from the actual BIDS file suffix
 * it selects (e.g. {@code dwi_fullreverse} → actual suffix {@code "dwi"}), this class
 * builds a lookup map used by the set handlers to match files to their configuration
 * entry.</p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * dwi_fullreverse:
 *   suffix_maps_to: "dwi"
 *   named_set: ...
 * </pre>
 */
@CompileStatic
class SuffixMapper {

    /**
     * Build suffix mapping from configuration
     *
     * Scans all configuration entries for suffix_maps_to field and creates
     * a mapping of config key -> file suffix (inverted for collision-free lookups)
     *
     * @param config Full configuration map
     * @return Map of config key -> suffix (e.g., "dwi_fullreverse" -> "dwi")
     */
    static Map<String, Map<String, String>> suffixMapping(Map config) {
        Map<String, Map<String, String>> mapping = [:].withDefault { [:] }

        if (!config) {
            return mapping
        }

        config.each { configKey, configValue ->
            if (configValue instanceof Map) {
                Map suffixConfig = configValue as Map

                // Check if this config has suffix_maps_to
                if (suffixConfig.suffix_maps_to) {
                    String targetSuffix = suffixConfig.suffix_maps_to as String
                    String setType = BaseSetHandler.getSetType(suffixConfig)

                    // Map: configuration key -> actual file suffix (INVERTED to prevent collision)
                    mapping[setType][configKey as String] = targetSuffix

                    BidsLogger.logProgress("suffix-mapping", "Suffix mapping for ${setType}: ${configKey} -> ${targetSuffix}")
                }
            }
        }

        return mapping
    }

    /**
     * Resolve configuration key for a given file suffix
     *
     * Searches through the inverted suffix mapping (configKey -> targetSuffix)
     * to find which config key(s) map to the given suffix.
     *
     * @param setType Type of set (e.g., "plain_set", "named_set")
     * @param suffix File suffix from BIDS file
     * @param mapping Suffix mapping from suffixMapping() (configKey -> targetSuffix)
     * @return List of configuration keys that map to this suffix
     */
    static List<String> resolveConfigKeys(String setType, String suffix, Map<String, Map<String, String>> mapping) {
        // Handle null or empty mapping
        if (!mapping || !mapping[setType]) {
            return [suffix]
        }

        // Find all config keys that map to this suffix (inverted search)
        List<String> matchingKeys = mapping[setType]
            .findAll { configKey, targetSuffix -> targetSuffix == suffix }
            .collect { configKey, targetSuffix -> configKey }

        if (matchingKeys) {
            BidsLogger.logProgress("suffix-mapping", "Resolved suffix '${suffix}' to config keys: ${matchingKeys}")
            return matchingKeys
        }

        // If no mapping found, config key is same as suffix
        return [suffix]
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

    /**
     * Get target suffix from inverted mapping for a config key
     *
     * @param setType Type of set (e.g., "plain_set", "named_set")
     * @param configKey Configuration key
     * @param mapping Suffix mapping from suffixMapping() (configKey -> targetSuffix)
     * @return Target suffix, or configKey if no mapping exists
     */
    static String getTargetSuffix(String setType, String configKey, Map<String, Map<String, String>> mapping) {
        if (!mapping || !mapping[setType]) {
            return configKey
        }

        return mapping[setType].get(configKey, configKey)
    }

}
