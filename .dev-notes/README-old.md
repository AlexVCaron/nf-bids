# nf-bids - Nextflow BIDS Plugin

A Nextflow plugin that provides native BIDS dataset support through the `Channel.fromBIDS()` factory method.

## Overview

This plugin reimplements the [bids2nf](https://github.com/AlexVCaron/bids2nf) workflow as a native Nextflow plugin, providing seamless BIDS dataset integration.

## Project Structure

### Source Code Organization

```
plugins/nf-bids/
├── src/main/groovy/nextflow/bids/
│   ├── BidsPlugin.groovy              # Main plugin entry point
│   ├── BidsExtension.groovy           # DSL extension
│   ├── channel/
│   │   └── BidsChannelFactory.groovy  # Main channel factory
│   ├── config/
│   │   ├── BidsConfigLoader.groovy    # Configuration loader
│   │   └── BidsConfigAnalyzer.groovy  # Configuration analyzer
│   ├── parser/
│   │   ├── BidsParser.groovy          # Main BIDS parser
│   │   ├── LibBidsShWrapper.groovy    # Bash library wrapper
│   │   └── BidsValidator.groovy       # BIDS validator
│   ├── grouping/
│   │   ├── BaseSetHandler.groovy      # Base handler class
│   │   ├── PlainSetHandler.groovy     # Plain set handler
│   │   ├── NamedSetHandler.groovy     # Named set handler
│   │   ├── SequentialSetHandler.groovy # Sequential set handler
│   │   └── MixedSetHandler.groovy     # Mixed set handler
│   ├── model/
│   │   ├── BidsEntity.groovy          # BIDS entity model
│   │   ├── BidsFile.groovy            # BIDS file model
│   │   ├── BidsDataset.groovy         # BIDS dataset model
│   │   └── BidsChannelData.groovy     # Channel data model
│   └── util/
│       ├── BidsLogger.groovy          # Logging utilities
│       └── BidsErrorHandler.groovy    # Error handling
├── src/test/groovy/nextflow/bids/
│   └── BidsChannelFactoryTest.groovy  # Unit tests
├── build.gradle                        # Build configuration
└── settings.gradle                     # Gradle settings
```

### Code References

All classes include docstring references to the original bids2nf codebase implementation:

- **Main Workflow**: `main.nf`
- **Parsers**: `modules/parsers/`
- **Grouping**: `modules/grouping/`
- **Subworkflows**: `subworkflows/`
- **Utilities**: `modules/utils/`

## Setup Instructions

### 1. Clone Template Repository

The official Nextflow plugin template is `nf-hello`:

```bash
# Fork or clone the template
git clone https://github.com/nextflow-io/nf-hello nf-bids-template
cd nf-bids-template
```

### 2. Initialize Plugin Structure

Copy the scaffolding from this project:

```bash
# Copy plugin structure to template
cp -r plugins/nf-bids/* /path/to/nf-bids-template/plugins/nf-bids/
```

### 3. Build the Plugin

```bash
cd plugins/nf-bids
./gradlew build
```

### 4. Test the Plugin

```bash
./gradlew test
```

## Usage

### Basic Usage

```groovy
// In your Nextflow workflow
plugins {
    id 'nf-bids'
}

workflow {
    Channel.fromBIDS(
        '/path/to/bids/dataset',
        '/path/to/bids2nf.yaml'
    ) | processData
}
```

### Advanced Options

```groovy
Channel.fromBIDS(
    bidsDir: '/path/to/bids/dataset',
    config: '/path/to/bids2nf.yaml',
    options: [
        bids_validation: true,
        ignore_codes: [99, 36],
        libbids_sh: '/path/to/libBIDS.sh'
    ]
)
```

## Implementation Status

### ✅ Completed Scaffolding

- [x] Plugin structure
- [x] Core classes with method signatures
- [x] Docstring references to original code
- [x] Build configuration
- [x] Test framework setup

### 🚧 To Implement

Each class contains method stubs with comprehensive docstrings linking to the reference implementation. You need to implement:

1. **BidsParser** - CSV parsing logic
2. **LibBidsShWrapper** - Bash script execution
3. **Set Handlers** - Data grouping logic
4. **Cross-modal Broadcasting** - Data inclusion logic
5. **Channel Emission** - Nextflow channel creation

## Key Features to Implement

### 1. Configuration-Driven Routing

Reference: [`main.nf#L69-L92`](https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L69-L92)

```groovy
// Dynamically route to handlers based on config
if (analysis.hasNamedSets) { ... }
if (analysis.hasSequentialSets) { ... }
```

### 2. Cross-Modal Broadcasting

Reference: [`main.nf#L119-L236`](https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L119-L236)

```groovy
// Task-specific channels request cross-modal data
if (setCfg?.include_cross_modal) { ... }
```

### 3. Entity Grouping

Reference: [`modules/grouping/entity_grouping_utils.nf`](https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf)

```groovy
// Group files by BIDS entities
def groupingKey = loopOverEntities.collect { entity -> ... }
```

## Dependencies

- **Nextflow**: 23.10.0+
- **Java**: 11+
- **Groovy**: 3.0.17
- **PF4J**: 3.9.0 (plugin framework)
- **SnakeYAML**: 2.0 (configuration parsing)
- **GPars**: 1.2.1 (dataflow)

## External Dependencies

- **libBIDS.sh**: Bash library for BIDS parsing (git submodule)
- **bids-validator**: Docker container for BIDS validation

## Testing

Reference tests from original project:
- [`tests/integration/`](https://github.com/AlexVCaron/bids2nf/tree/main/tests/integration)
- [`tests/expected_outputs/`](https://github.com/AlexVCaron/bids2nf/tree/main/tests/expected_outputs)

## Contributing

1. Implement method stubs following docstring references
2. Add unit tests for each component
3. Test with real BIDS datasets from `tests/data/`
4. Ensure cross-modal broadcasting works correctly

## License

MIT License (same as bids2nf)

## References

- **Original Project**: https://github.com/AlexVCaron/bids2nf
- **BIDS Specification**: https://bids-specification.readthedocs.io/
- **Nextflow Plugins**: https://www.nextflow.io/docs/latest/plugins.html
- **Plugin Template**: https://github.com/nextflow-io/nf-hello

## Next Steps

1. Review each class and its reference links
2. Implement core parsing logic in `BidsParser` and `LibBidsShWrapper`
3. Implement set handlers starting with `PlainSetHandler`
4. Add comprehensive unit tests
5. Test with bids2nf example datasets
6. Document API and usage patterns
