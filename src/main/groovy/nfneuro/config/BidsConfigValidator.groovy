package nfneuro.plugin.config

import groovy.util.logging.Slf4j

/**
 * Validates {@code bids2nf.yaml} configuration maps.
 *
 * <p>Performs structural and semantic validation of the parsed YAML configuration,
 * returning a {@link ValidationResult} that accumulates errors and warnings.
 * Validation covers: set-type presence (exactly one of {@code plain_set},
 * {@code named_set}, {@code sequential_set}, {@code mixed_set} per suffix),
 * required named groups, sequential ordering, and the global {@code loop_over} key.</p>
 */
@Slf4j
class BidsConfigValidator {

    /**
     * Holds the outcome of a configuration validation pass.
     *
     * <p>Errors indicate conditions that will prevent correct operation.
     * Warnings indicate non-fatal issues that may produce unexpected behaviour.</p>
     */
    static class ValidationResult {

        List<String> errors = []
        List<String> warnings = []

        boolean isValid() { return errors.isEmpty() }

        String toString() {
            def parts = []
            if (errors) parts << "ERRORS:\n  " + errors.join("\n  ")
            if (warnings) parts << "WARNINGS:\n  " + warnings.join("\n  ")
            return parts.join("\n\n")
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
        def globalKeys = ['loop_over']

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
            validateNamedSet(suffix, suffixConfig.named_set, suffixConfig.required, result)
        }
        if (suffixConfig.sequential_set != null) {
            validateSequentialSet(suffix, suffixConfig.sequential_set, result)
        }
        if (suffixConfig.mixed_set != null) {
            validateMixedSet(suffix, suffixConfig.mixed_set, suffixConfig.required, result)
        }

        // Validate suffix_maps_to
        if (suffixConfig.suffix_maps_to != null) {
            if (!(suffixConfig.suffix_maps_to instanceof String)) {
                result.errors << "Suffix '${suffix}': suffix_maps_to must be a string"
            }
        }

        // Validate additional extensions
        if (suffixConfig.additional_extensions != null) {
            if (!(suffixConfig.additional_extensions instanceof List)) {
                result.errors << "Suffix '${suffix}': additional_extensions must be a list"
            }
        }

        // Validate cross-modal inclusion
        if (suffixConfig.include_cross_modal != null) {
            if (!(suffixConfig.include_cross_modal instanceof List)) {
                result.warnings << "Suffix '${suffix}': include_cross_modal should be a list of suffixes"
            }
        }
    }

    /**
     * Validate plain_set configuration
     */
    private static void validatePlainSet(String suffix, Map config, ValidationResult result) {
        // Plain set has no required fields, just optional ones

        // Validate parts
        validateParts(suffix, config, "plain_set", result)
    }

    /**
     * Validate named_set configuration
     */
    private static void validateNamedSet(String suffix, Map config, List required, ValidationResult result) {
        // Check for named groups (required - must have at least one group defined)
        if (config.size() == 0) {
            result.errors << "Suffix '${suffix}' named_set: no named groups defined. Must have at least one group (e.g., MTw: {flip: 'flip-1'})"
        }

        // Validate each named group
        config.each { groupName, groupConfig ->
            if (!(groupConfig instanceof Map)) {
                result.errors << "Suffix '${suffix}' named_set group '${groupName}': must be a map of entity patterns"
                return
            }

            validateNamedGroup(groupName, groupConfig, suffix, "named_set", result)
        }

        // Validate required field if present
        validateNamedRequirements(suffix, config, required, "named_set", result)
    }

    /**
     * Validate sequential_set configuration
     */
    private static void validateSequentialSet(String suffix, Map config, ValidationResult result) {
        // Must have either by_entity or by_entities
        def hasSequenceSpec = config.by_entity || config.by_entities

        if (!hasSequenceSpec) {
            result.errors << "Suffix '${suffix}' sequential_set: must specify 'by_entity' or 'by_entities'"
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

        // Validate order
        if (config.order != null) {
            if (!(config.order instanceof String)) {
                result.errors << "Suffix '${suffix}' sequential_set: 'order' must be a string"
            } else if (config.order !in ['hierarchical', 'flat']) {
                result.errors << "Suffix '${suffix}' sequential_set: 'order' must be 'hierarchical' or 'flat', got '${config.order}'"
            }

            if (config.by_entity != null) {
                result.warnings << "Suffix '${suffix}' sequential_set: 'order' is ignored when using 'by_entity'"
            }
        }

        // Validate parts
        validateParts(suffix, config, "sequential_set", result)
    }

    /**
     * Validate mixed_set configuration
     */
    private static void validateMixedSet(String suffix, Map config, List required, ValidationResult result) {
        // Must have sequential_dimension
        if (!config.sequential_dimension) {
            result.errors << "Suffix '${suffix}' mixed_set: must specify 'sequential_dimension'"
        } else if (!(config.sequential_dimension instanceof String)) {
            result.errors << "Suffix '${suffix}' mixed_set: 'sequential_dimension' must be a string (entity name)"
        }

        // Must have named_dimension
        if (!config.named_dimension) {
            result.errors << "Suffix '${suffix}' mixed_set: must specify 'named_dimension'"
        } else if (!(config.named_dimension instanceof String)) {
            result.errors << "Suffix '${suffix}' mixed_set: 'named_dimension' must be a string (entity name)"
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
                } else {
                    validateNamedGroup(groupName, groupConfig, suffix, "mixed_set", result)
                }
            }

            validateNamedRequirements(suffix, config.named_groups, required, "mixed_set", result)
        }
    }

    /**
     * Validate a named group within any set type
     */
    private static void validateNamedGroup(String groupName, Map groupConfig, String suffix, String setType, ValidationResult result) {
        if (groupConfig.isEmpty()) {
            result.warnings << "Suffix '${suffix}' ${setType} group '${groupName}': empty pattern (will match nothing)"
        }

        def nonEntityKeys = ['parts']

        // Validate each entity pattern
        groupConfig.each { entity, value ->
            if (nonEntityKeys.contains(entity)) return  // Skip non-entity keys

            if (value == null) {
                result.warnings << "Suffix '${suffix}' ${setType} group '${groupName}': entity '${entity}' has null value"
            }
        }

        validateParts(suffix, groupConfig, "${setType} group '${groupName}'", result)
    }

    /**
     * Validate required field within named sets
     */
    private static void validateNamedRequirements(String suffix, Map config, List required, String setType, ValidationResult result) {
        if (required != null) {
            if (!(required instanceof List)) {
                result.errors << "Suffix '${suffix}' ${setType}: 'required' must be a list of group names"
            } else {
                required.each { requiredGroup ->
                    if (!config.containsKey(requiredGroup)) {
                        result.errors << "Suffix '${suffix}' ${setType}: required group '${requiredGroup}' is not defined"
                    }
                }
            }
        }
    }

    /**
     * Validate parts field within a group or a set
     */
    private static void validateParts(String suffix, Map setOrGroupConfig, String setOrGroupId, ValidationResult result) {
        if (setOrGroupConfig.parts != null) {
            if (!(setOrGroupConfig.parts instanceof List)) {
                result.errors << "Suffix '${suffix}' ${setOrGroupId}: 'parts' must be a list"
            } else if (setOrGroupConfig.parts.isEmpty()) {
                result.warnings << "Suffix '${suffix}' ${setOrGroupId}: 'parts' is empty (no effect)"
            } else if (setOrGroupConfig.parts.size() == 1) {
                result.warnings << "Suffix '${suffix}' ${setOrGroupId}: 'parts' has only one value (grouping has no effect)"
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
