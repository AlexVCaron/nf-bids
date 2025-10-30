# Configuration Validation

The nf-bids plugin includes comprehensive configuration validation to catch errors early and provide clear, actionable feedback.

## Overview

The `ConfigValidator` class validates entire bids2nf.yaml configuration files and returns detailed error messages for any issues found.

## Usage

### Basic Validation

```groovy
import nfneuro.plugin.config.ConfigValidator

// Load your configuration
def config = /* your parsed YAML config */

// Validate
def result = ConfigValidator.validate(config)

if (!result.isValid()) {
    println "Configuration errors found:"
    println result.toString()
} else {
    println "Configuration is valid!"
}
```

### Convenience Method

```groovy
// Automatically logs results
if (!ConfigValidator.validateAndLog(config)) {
    throw new Exception("Invalid configuration")
}
```

## Validation Rules

### Plain Set Configuration

**Required**: None (all fields optional)

**Optional**:
- `additional_extensions` - Must be a list of strings
- `include_cross_modal` - Should be boolean

**Example Valid Config**:
```yaml
dwi:
  plain_set:
    additional_extensions: ["json", "bval", "bvec"]
    include_cross_modal: true
```

**Common Errors**:
```
Suffix 'dwi' plain_set: additional_extensions must be a list
```

### Named Set Configuration

**Required**:
- At least one named group (e.g., `MTw`, `PDw`)

**Optional**:
- `required` - List of group names (must reference existing groups)

**Example Valid Config**:
```yaml
MTS:
  named_set:
    MTw:
      flip: "flip-1"
      mtransfer: "mt-on"
    PDw:
      flip: "flip-2"
      mtransfer: "mt-off"
    T1w:
      flip: "flip-3"
      mtransfer: "mt-off"
    required: ["MTw", "PDw", "T1w"]
```

**Common Errors**:
```
Suffix 'MTS' named_set: no named groups defined. Must have at least one group (e.g., MTw: {flip: 'flip-1'})
Suffix 'MTS' named_set: required group 'T1w' is not defined
Suffix 'MTS' named_set: 'required' must be a list of group names
```

**Warnings**:
```
Suffix 'MTS' named_set group 'PDw': empty pattern (will match nothing)
Suffix 'MTS' named_set group 'MTw': entity 'flip' has null value
```

### Sequential Set Configuration

**Required** (one of):
- `by_entity` - String (single entity name)
- `by_entities` - List of strings (multiple entities)
- `sequential_dimension` - String (alias for by_entity)

**Optional**:
- `order` - Must be "hierarchical" or "flat"
- `parts` - List of part values (e.g., ["mag", "phase"])
- `additional_extensions` - List of strings
- `filter` - Map of entity filters

**Example Valid Config**:
```yaml
MP2RAGE:
  sequential_set:
    by_entities: ["inversion", "echo"]
    order: "hierarchical"
    parts: ["mag", "phase"]
    additional_extensions: ["json"]
```

**Common Errors**:
```
Suffix 'IRT1' sequential_set: must specify 'by_entity', 'by_entities', or 'sequential_dimension'
Suffix 'MEGRE' sequential_set: 'order' must be 'hierarchical' or 'flat', got 'random'
Suffix 'MP2RAGE' sequential_set: 'by_entities' cannot be empty
Suffix 'IRT1' sequential_set: 'by_entity' must be a string (entity name)
Suffix 'MP2RAGE' sequential_set: 'parts' must be a list of part values
```

**Warnings**:
```
Suffix 'IRT1' sequential_set: 'by_entities' has only one entity, consider using 'by_entity' instead
Suffix 'MP2RAGE' sequential_set: both 'sequential_dimension' and 'by_entity/by_entities' specified. Using 'sequential_dimension'.
Suffix 'MP2RAGE' sequential_set: 'parts' is empty (no effect)
Suffix 'MP2RAGE' sequential_set: 'parts' has only one value (grouping has no effect)
```

### Mixed Set Configuration

**Required**:
- `sequential_dimension` - String (entity name for sequencing)
- `named_groups` - Map of named group patterns (at least one)

**Optional**:
- `loop_over` - String (entity name)
- `named_dimension` - String (entity name)
- `order` - Must be "hierarchical" or "flat"

**Example Valid Config**:
```yaml
MPM:
  mixed_set:
    sequential_dimension: "echo"
    named_groups:
      MTw:
        acquisition: "acq-MTw"
      PDw:
        acquisition: "acq-PDw"
      T1w:
        acquisition: "acq-T1w"
    loop_over: "run"
    named_dimension: "acquisition"
    order: "flat"
```

**Common Errors**:
```
Suffix 'MPM' mixed_set: must specify 'sequential_dimension'
Suffix 'MPM' mixed_set: must specify 'named_groups'
Suffix 'MPM' mixed_set: 'named_groups' cannot be empty
Suffix 'MPM' mixed_set: 'sequential_dimension' must be a string (entity name)
Suffix 'MPM' mixed_set group 'MTw': must be a map of entity patterns
```

**Warnings**:
```
Suffix 'MPM' mixed_set group 'PDw': empty pattern (will match nothing)
```

### Suffix Mapping

**Optional**:
- `suffix_maps_to` - String (target suffix name)

**Example Valid Config**:
```yaml
MP2RAGE:
  suffix_maps_to: "MP2RAGE"
  sequential_set:
    by_entity: "inversion"
```

**Common Errors**:
```
Suffix 'MP2RAGE': suffix_maps_to must be a string
```

## Error Messages

### Format

The validator returns a `ValidationResult` object with:
- `errors` - List of error messages (configuration invalid)
- `warnings` - List of warning messages (configuration valid but suspicious)
- `isValid()` - Boolean (true if no errors)
- `toString()` - Formatted output

### Example Output

**Multiple Errors**:
```
ERRORS:
  Suffix 'broken1' named_set: no named groups defined. Must have at least one group (e.g., MTw: {flip: 'flip-1'})
  Suffix 'broken2' sequential_set: must specify 'by_entity', 'by_entities', or 'sequential_dimension'
  Suffix 'broken2' sequential_set: 'order' must be 'hierarchical' or 'flat', got 'invalid'
  Suffix 'broken3' mixed_set: must specify 'sequential_dimension'
  Suffix 'broken3' mixed_set: must specify 'named_groups'
```

**Warnings Only**:
```
WARNINGS:
  Suffix 'dwi': multiple set types defined (plain_set, named_set). Only first will be used.
  Suffix 'MP2RAGE' sequential_set: 'parts' has only one value (grouping has no effect)
```

## Test Coverage

The validator is tested with 30 comprehensive test cases:

### Valid Configurations (6 tests)
- Minimal valid plain_set
- Minimal valid named_set
- Minimal valid sequential_set (by_entity)
- Minimal valid sequential_set (by_entities)
- Minimal valid mixed_set
- Configuration with all optional fields

### Invalid Configurations (16 tests)
- Null/empty configuration
- Suffix without set type
- Named set without groups
- Named set with invalid required reference
- Sequential set without dimension
- Sequential set with invalid order
- Sequential set with empty by_entities
- Mixed set without sequential_dimension
- Mixed set without named_groups
- Mixed set with empty named_groups
- Wrong type for suffix_maps_to
- Wrong type for required field
- Wrong type for additional_extensions
- Wrong type for parts

### Warnings (7 tests)
- Multiple set types in same config
- Single entity in by_entities list
- Empty parts array
- Single value in parts
- Empty named group pattern
- Null entity value in pattern
- Conflicting dimension specifications

### Edge Cases (1 test)
- Complex realistic configuration
- Multiple errors in single config

## Integration

### In Plugin Initialization

```groovy
class BidsPlugin {
    void init() {
        // Load config
        def config = loadYamlConfig()
        
        // Validate
        def result = ConfigValidator.validate(config)
        if (!result.isValid()) {
            throw new RuntimeException("Invalid configuration:\n${result}")
        }
        
        if (result.warnings) {
            log.warn("Configuration warnings:\n${result}")
        }
    }
}
```

### In Workflow

```groovy
workflow {
    // Validate config early
    if (!ConfigValidator.validateAndLog(params.config)) {
        exit 1, "Configuration validation failed"
    }
    
    // Continue with validated config
    processData(params.config)
}
```

## Benefits

1. **Early Detection**: Catch configuration errors before processing starts
2. **Clear Messages**: Detailed, actionable feedback for users
3. **Type Safety**: Ensures correct types for all fields
4. **Reference Checking**: Validates cross-references (e.g., required groups exist)
5. **Value Validation**: Ensures valid enum values (e.g., order)
6. **Comprehensive**: Covers all configuration options
7. **Production Ready**: Tested with 30 test cases

## Future Enhancements

Potential improvements for validation:
- BIDS entity name validation (check against official BIDS spec)
- File existence validation (check that referenced files exist)
- Pattern consistency checking (ensure patterns don't overlap)
- Performance profiling (warn about potentially slow configs)
- Suggestion system (recommend fixes for common errors)

## See Also

- [Configuration Options](configuration.md) - Detailed config documentation
- [Test Suite](../src/test/groovy/nfneuro/plugin/config/ConfigValidatorTest.groovy) - Full test coverage
- [ConfigValidator Source](../src/main/groovy/nfneuro/plugin/config/ConfigValidator.groovy) - Implementation
