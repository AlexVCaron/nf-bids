package nfneuro.plugin.config

import groovy.util.logging.Slf4j

/**
 * Validates bids2nf configuration files
 * 
 * Provides comprehensive validation of YAML configuration structure and values.
 * Returns detailed error messages for troubleshooting.
 */
@Slf4j
class ConfigValidator {
    
    static class ValidationResult {
        List<String> errors = []
        List<String> warnings = []
        
        boolean isValid() { errors.isEmpty() }
        
        String toString() {
            def parts = []
            if (errors) parts << "ERRORS:\n  " + errors.join("\n  ")
            if (warnings) parts << "WARNINGS:\n  " + warnings.join("\n  ")
            parts.join("\n\n")
        }
    }
    
    /**
     * Validate entire configuration
     * @param config Map representing parsed YAML
     * @return ValidationResult with errors and warnings
     */
    static ValidationResult validate(Map config) {
        def result = new ValidationResult()
        
        if (config == null || config.isEmpty()) {
            result.errors << "Configuration is null or empty"
            return result
        }
        
        // Global configuration keys (not suffix definitions)
        def globalKeys = ['loop_over', 'plain_sets', 'named_sets', 'sequential_sets', 'mixed_sets']
        
        // Validate each suffix configuration
        config.each { suffix, suffixConfig ->
            // Skip global configuration keys
            if (suffix in globalKeys) {
                // Validate loop_over specifically if present
                if (suffix == 'loop_over' && !(suffixConfig instanceof List)) {
                    result.errors << "Global 'loop_over' must be a list of entity names"
                }
                return  // Skip further processing for global keys
            }
            
            // Skip if value is not a Map (likely metadata or other non-suffix config)
            if (!(suffixConfig instanceof Map)) {
                return
            }
            
            validateSuffixConfig(suffix, suffixConfig, result)
        }
        
        return result
    }
    
    /**
     * Validate configuration for a single BIDS suffix
     */
    private static void validateSuffixConfig(String suffix, Map suffixConfig, ValidationResult result) {
        if (!suffixConfig) {
            result.errors << "Suffix '${suffix}': configuration is null or empty"
            return
        }
        
        // Check for set type (must have exactly one)
        def setTypes = ['plain_set', 'named_set', 'sequential_set', 'mixed_set']
        def foundTypes = setTypes.findAll { suffixConfig.containsKey(it) }
        
        if (foundTypes.isEmpty()) {
            result.errors << "Suffix '${suffix}': no set type specified (must have one of: ${setTypes.join(', ')})"
        } else if (foundTypes.size() > 1) {
            result.warnings << "Suffix '${suffix}': multiple set types defined (${foundTypes.join(', ')}). Only first will be used."
        }
        
        // Validate each set type
        if (suffixConfig.plain_set != null) {
            validatePlainSet(suffix, suffixConfig.plain_set, result)
        }
        if (suffixConfig.named_set != null) {
            validateNamedSet(suffix, suffixConfig.named_set, result)
        }
        if (suffixConfig.sequential_set != null) {
            validateSequentialSet(suffix, suffixConfig.sequential_set, result)
        }
        if (suffixConfig.mixed_set != null) {
            validateMixedSet(suffix, suffixConfig.mixed_set, result)
        }
        
        // Validate suffix_maps_to
        if (suffixConfig.suffix_maps_to != null) {
            if (!(suffixConfig.suffix_maps_to instanceof String)) {
                result.errors << "Suffix '${suffix}': suffix_maps_to must be a string"
            }
        }
    }
    
    /**
     * Validate plain_set configuration
     */
    private static void validatePlainSet(String suffix, Map config, ValidationResult result) {
        // Plain set has no required fields, just optional ones
        
        // Validate additional_extensions if present
        if (config.additional_extensions != null) {
            if (!(config.additional_extensions instanceof List)) {
                result.errors << "Suffix '${suffix}' plain_set: additional_extensions must be a list"
            }
        }
        
        // Validate include_cross_modal if present
        if (config.include_cross_modal != null) {
            if (!(config.include_cross_modal instanceof Boolean || config.include_cross_modal instanceof List)) {
                result.warnings << "Suffix '${suffix}' plain_set: include_cross_modal should be boolean (true/false) or list of suffixes"
            }
        }
    }
    
    /**
     * Validate named_set configuration
     */
    private static void validateNamedSet(String suffix, Map config, ValidationResult result) {
        // Check for named groups (required - must have at least one group defined)
        def groupCount = config.findAll { k, v -> 
            k != 'required' && k != 'description' && v instanceof Map 
        }.size()
        
        if (groupCount == 0) {
            result.errors << "Suffix '${suffix}' named_set: no named groups defined. Must have at least one group (e.g., MTw: {flip: 'flip-1'})"
        }
        
        // Validate each named group
        config.each { groupName, groupConfig ->
            if (groupName == 'required' || groupName == 'description') {
                return  // Skip special fields
            }
            
            if (!(groupConfig instanceof Map)) {
                result.errors << "Suffix '${suffix}' named_set group '${groupName}': must be a map of entity patterns"
                return
            }
            
            if (groupConfig.isEmpty()) {
                result.warnings << "Suffix '${suffix}' named_set group '${groupName}': empty pattern (will match nothing)"
            }
            
            // Validate each entity pattern
            groupConfig.each { entity, value ->
                if (entity == 'description') return  // Skip description
                
                if (value == null) {
                    result.warnings << "Suffix '${suffix}' named_set group '${groupName}': entity '${entity}' has null value"
                }
            }
        }
        
        // Validate required field if present
        if (config.required != null) {
            if (!(config.required instanceof List)) {
                result.errors << "Suffix '${suffix}' named_set: 'required' must be a list of group names"
            } else {
                def definedGroups = config.findAll { k, v -> 
                    k != 'required' && k != 'description' && v instanceof Map 
                }.keySet()
                
                config.required.each { requiredGroup ->
                    if (!definedGroups.contains(requiredGroup)) {
                        result.errors << "Suffix '${suffix}' named_set: required group '${requiredGroup}' is not defined"
                    }
                }
            }
        }
    }
    
    /**
     * Validate sequential_set configuration
     */
    private static void validateSequentialSet(String suffix, Map config, ValidationResult result) {
        // Must have either by_entity or by_entities or sequential_dimension
        def hasSequenceSpec = config.by_entity || config.by_entities || config.sequential_dimension
        
        if (!hasSequenceSpec) {
            result.errors << "Suffix '${suffix}' sequential_set: must specify 'by_entity', 'by_entities', or 'sequential_dimension'"
        }
        
        // Validate by_entity
        if (config.by_entity != null) {
            if (!(config.by_entity instanceof String)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'by_entity' must be a string (entity name)"
            }
        }
        
        // Validate by_entities
        if (config.by_entities != null) {
            if (!(config.by_entities instanceof List)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'by_entities' must be a list of entity names"
            } else if (config.by_entities.isEmpty()) {
                result.errors << "Suffix '${suffix}' sequential_set: 'by_entities' cannot be empty"
            } else if (config.by_entities.size() == 1) {
                result.warnings << "Suffix '${suffix}' sequential_set: 'by_entities' has only one entity, consider using 'by_entity' instead"
            }
        }
        
        // Validate sequential_dimension (alias for by_entity)
        if (config.sequential_dimension != null) {
            if (!(config.sequential_dimension instanceof String)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'sequential_dimension' must be a string (entity name)"
            }
            
            if (config.by_entity || config.by_entities) {
                result.warnings << "Suffix '${suffix}' sequential_set: both 'sequential_dimension' and 'by_entity/by_entities' specified. Using 'sequential_dimension'."
            }
        }
        
        // Validate order
        if (config.order != null) {
            if (!(config.order instanceof String)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'order' must be a string"
            } else if (config.order !in ['hierarchical', 'flat']) {
                result.errors << "Suffix '${suffix}' sequential_set: 'order' must be 'hierarchical' or 'flat', got '${config.order}'"
            }
        }
        
        // Validate parts
        if (config.parts != null) {
            if (!(config.parts instanceof List)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'parts' must be a list of part values"
            } else if (config.parts.isEmpty()) {
                result.warnings << "Suffix '${suffix}' sequential_set: 'parts' is empty (no effect)"
            } else if (config.parts.size() == 1) {
                result.warnings << "Suffix '${suffix}' sequential_set: 'parts' has only one value (grouping has no effect)"
            }
        }
        
        // Validate additional_extensions
        if (config.additional_extensions != null) {
            if (!(config.additional_extensions instanceof List)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'additional_extensions' must be a list"
            }
        }
    }
    
    /**
     * Validate mixed_set configuration
     */
    private static void validateMixedSet(String suffix, Map config, ValidationResult result) {
        // Must have sequential_dimension
        if (!config.sequential_dimension) {
            result.errors << "Suffix '${suffix}' mixed_set: must specify 'sequential_dimension'"
        } else if (!(config.sequential_dimension instanceof String)) {
            result.errors << "Suffix '${suffix}' mixed_set: 'sequential_dimension' must be a string (entity name)"
        }
        
        // Must have named_groups
        if (!config.named_groups) {
            result.errors << "Suffix '${suffix}' mixed_set: must specify 'named_groups'"
        } else if (!(config.named_groups instanceof Map)) {
            result.errors << "Suffix '${suffix}' mixed_set: 'named_groups' must be a map"
        } else if (config.named_groups.isEmpty()) {
            result.errors << "Suffix '${suffix}' mixed_set: 'named_groups' cannot be empty"
        } else {
            // Validate each named group
            config.named_groups.each { groupName, groupConfig ->
                if (!(groupConfig instanceof Map)) {
                    result.errors << "Suffix '${suffix}' mixed_set group '${groupName}': must be a map of entity patterns"
                } else if (groupConfig.isEmpty()) {
                    result.warnings << "Suffix '${suffix}' mixed_set group '${groupName}': empty pattern (will match nothing)"
                }
            }
        }
        
        // Validate loop_over if present
        if (config.loop_over != null) {
            if (!(config.loop_over instanceof String)) {
                result.errors << "Suffix '${suffix}' mixed_set: 'loop_over' must be a string (entity name)"
            }
        }
        
        // Validate named_dimension if present
        if (config.named_dimension != null) {
            if (!(config.named_dimension instanceof String)) {
                result.errors << "Suffix '${suffix}' mixed_set: 'named_dimension' must be a string (entity name)"
            }
        }
        
        // Validate order
        if (config.order != null) {
            if (!(config.order instanceof String)) {
                result.errors << "Suffix '${suffix}' mixed_set: 'order' must be a string"
            } else if (config.order !in ['hierarchical', 'flat']) {
                result.errors << "Suffix '${suffix}' mixed_set: 'order' must be 'hierarchical' or 'flat', got '${config.order}'"
            }
        }
    }
    
    /**
     * Convenience method to validate and log results
     */
    static boolean validateAndLog(Map config) {
        def result = validate(config)
        
        if (!result.isValid()) {
            log.error("Configuration validation failed:\n${result}")
            return false
        }
        
        if (result.warnings) {
            log.warn("Configuration validation warnings:\n${result}")
        }
        
        return true
    }
}
