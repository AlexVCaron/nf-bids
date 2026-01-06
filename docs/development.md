# Development Guide

Guide for developers working on the nf-bids plugin implementation.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Building the Plugin](#building-the-plugin)
- [Testing](#testing)
- [Implementation Guidelines](#implementation-guidelines)
- [Debugging](#debugging)
- [Resources](#resources)

## Getting Started

**Prerequisites**:
- Java 11 or later
- Gradle 8.14 (included via wrapper)
- [Nextflow 23+](https://nextflow.io) or later
- Bash (for [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh))

### Project Structure

```
nf-bids/
├── src/
│   └── main/
│       └── groovy/nfneuro/
│           ├── plugin/                         # Plugin registration
│           │   ├── BidsPlugin.groovy               # Plugin entry point
│           │   ├── BidsExtension.groovy            # DSL extension
│           │   ├── BidsObserver.groovy             # Nextflow events tracing
│           │   └── BidsFactory.groovy              # Trace observer registration
│           ├── channel/                        # BIDS channel packaging
│           │   ├── BidsChannelFactory.groovy       # Factory Channel.fromBIDS
│           │   └── BIDSHandler.groovy              # Channel packaging and management
│           ├── config/                         # Configuration management
│           │   ├── BidsConfigLoader.groovy         # Load configudation to code
│           │   ├── BidsConfigValidator.groovy      # Validate confguration fields
│           │   └── BidsConfigAnalyzer.groovy       # Configuration summarizing
│           ├── parser/                         # BIDS parsing and validation
│           │   ├── BidsParser.groovy               # BIDS directory parsing handler
│           │   ├── LibBidsShWrapper.groovy         # libBIDS.sh bash script wrapper
│           │   └── BidsValidator.groovy            # BIDS Validator wrapper
│           ├── grouping/                       # Set handlers
│           │   ├── BaseSetHandler.groovy           # Common set properties and methods
│           │   ├── PlainSetHandler.groovy          # Specific Plain Set behaviors
│           │   ├── NamedSetHandler.groovy          # Specific Named Set behaviors
│           │   ├── SequentialSetHandler.groovy     # Specific Sequential Set behaviors
│           │   └── MixedSetHandler.groovy          # Specific Mixed Set behaviors
│           ├── model/                          # Domain models
│           │   ├── BidsEntity.groovy               # BIDS entity representation
│           │   ├── BidsFile.groovy                 # BIDS file representation
│           │   ├── BidsDataset.groovy              # BIDS dataset representation
│           │   └── BidsChannelData.groovy          # BIDS channel item representation
│           └── util/                           # Utilities
│               ├── BidsLogger.groovy               # Nextflow logging utilities
│               ├── BidsErrorHandler.groovy         # Error handling utilities
│               ├── BidsCsvParser.groovy            # libBIDS.sh CSV output conversion
│               ├── BidsEntityUtils.groovy          # BIDS entity management utility
│               └── SuffixMapper.groovy             # BIDS suffix management utility 
├── validation/                                 # Integration and performance tests
│   ├── main.nf                                     # Base integration test with Nextflow
│   ├── nextflow.config                             # Plugin integration configuration
│   ├── test_datasets.sh                            # Utility script to run integration suite
│   ├── comparison_*.nf.test                        # Integration testing suite with nf-test
│   ├── comparison_*.nf.test.snap                   # nf-test snapshot of the integration suite
│   ├── test_heterogeneous_suffix_mapping.nf.test   # Heterogeneous dataset tests
│   ├── data/                                       # BIDS example and custom datasets
│   ├── configs/                                    # Plugin configurations to test
│   ├── edge_cases/                                 # Edge case testing suite
│   │   ├── edge_cases.nf.test                          # nf-test edge case suite
│   │   ├── run_all_tests.sh                            # Run all edge case tests
│   │   ├── test1_large_items.nf                        # Large item test
│   │   ├── test2_many_items.nf                         # Many items test
│   │   ├── test3_nested_structures.nf                  # Nested structure test
│   │   ├── test4_missing_fields.nf                     # Missing fields test
│   │   ├── test5_concurrent.nf                         # Concurrency test
│   │   ├── test6_join_many.nf                          # Join many test
│   │   ├── test7_complex_filter.nf                     # Complex filter test
│   │   ├── test8_combineby_edge_cases.nf               # combineBy edge cases
│   │   └── EDGE_CASE_RESULTS.md                        # Edge case results
│   └── benchmark/                                  # Performance benchmarks
│       ├── benchmark_combine.nf                        # Combine operator benchmark
│       ├── benchmark_combineby_new.nf                  # combineBy operator benchmark
│       ├── benchmark_grouptuple.nf                     # groupTuple benchmark
│       ├── benchmark_join.nf                           # Join operator benchmark
│       └── BENCHMARK_RESULTS.md                        # Benchmark results
├── .dev-notes/                            # Development notes
├── docs/                                  # Official documentation
├── build.gradle                           # Build configuration
├── settings.gradle                        # Project settings
├── Makefile                               # Plugin makefile
└── README.md
```

## Development Setup

### Initial Setup

1. **Run setup script**:
   ```bash
   ./setup.sh
   ```

   This script will:
   - Check for Java 11+
   - Download dependencies
   - Verify build configuration

### Manual Setup

If the setup script fails:

1. **Download dependencies**:
   ```bash
   ./gradlew dependencies
   ```

2. **Verify setup**:
   ```bash
   ./gradlew tasks
   ```

## Building the Plugin

### Quick Build and Install

```bash
# Build plugin
make assemble

# Run tests
make test

# Install to Nextflow
make install
```

This will run:
1. Clean build
2. Run tests
3. Build JAR
4. Install into Nextflow

### Individual Build Steps

**Clean build:**
```bash
make clean
```

**Compile and build:**
```bash
make assemble
```

**Run tests:**
```bash
make test
```

**Install to Nextflow:**
```bash
make install
```

**Publish release:**
```bash
make release
```

### Build Output

Built artifacts are located in:
- **JAR**: `build/libs/nf-bids-0.1.0.jar`
- **Classes**: `build/classes/groovy/main/`
- **Test results**: `build/reports/tests/test/index.html`

## Testing

### Unit Tests

Located in `src/test/groovy/`

**Run unit tests:**
```bash
make test
```

### Integration Tests

Located in `validation/`

**Run all integration tests:**
```bash
cd validation
nf-test test
```

**Run specific test suite:**
```bash
cd validation
nf-test test comparison_plain_sets.nf.test
nf-test test comparison_named_sets.nf.test
nf-test test comparison_sequential_sets.nf.test
nf-test test comparison_mixed_sets.nf.test
nf-test test comparison_custom_datasets.nf.test
nf-test test test_heterogeneous_suffix_mapping.nf.test
```

**Run legacy integration test:**
```bash
cd validation
nextflow run main.nf
```

### Edge Case Tests

Located in `validation/edge_cases/`

**Run all edge case tests:**
```bash
cd validation/edge_cases
./run_all_tests.sh
```

**Run specific edge case:**
```bash
cd validation/edge_cases
nextflow run test1_large_items.nf
```

### Performance Benchmarks

Located in `validation/benchmark/`

**Run benchmarks:**
```bash
cd validation/benchmark
nextflow run benchmark_combine.nf
nextflow run benchmark_combineby_new.nf
nextflow run benchmark_grouptuple.nf
nextflow run benchmark_join.nf
```

### Test Data

All test datasets are located under `validation/data/`:
- `validation/data/bids-examples/` - Official BIDS examples
- `validation/data/custom/` - Custom test datasets

## Implementation Guidelines

### Code Style

Follow Nextflow plugin conventions:

```groovy
package nextflow.bids.channel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session

/**
 * Factory for creating BIDS channels
 *
 * @author Your Name
 */
@Slf4j
@CompileStatic
class BidsChannelFactory {
    
    private Session session
    
    BidsChannelFactory(Session session = null) {
        this.session = session ?: Global.session as Session
    }
    
    // Methods...
}
```

### Error Handling

Use BidsErrorHandler for consistent errors:

```groovy
import nextflow.bids.util.BidsErrorHandler

// Validate input
if (!bidsDir) {
    BidsErrorHandler.handleInvalidBidsDirectory(bidsDir)
}

// Handle parsing errors
try {
    def result = parseBidsFiles(bidsDir)
} catch (Exception e) {
    BidsErrorHandler.handleParsingError(e, bidsDir)
}
```

### Logging

Use `BidsLogger` for consistent logging:

```groovy
import nfneuro.plugin.util.BidsLogger

class MyClass {
    def myMethod() {
        BidsLogger.logDebug("Debug message")
        BidsLogger.logProgress("Info message")
        BidsLogger.logWarning("Warning message")
        BidsLogger.logError("Error message")
        BidsLogger.logSuccess("Success message")
    }
}
```

For context-specific logging:

```groovy
BidsLogger.logProgress("my-context", "Processing started")
BidsLogger.logStats("my-context", ["files": 42, "datasets": 3])
BidsLogger.withTiming("my-context", "parsing operation") {
    // Code to time
}
```

### Reference Original Code

When available, a method should includes `@reference` tags pointing to the original implementation:

```groovy
/**
 * Process BIDS datasets
 *
 * @reference https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L100-L150
 */
def processDatasets(Map params) {
    // Implementation based on original code
}
```

## Debugging

### Enable Debug Logging

In `nextflow.config`:
```groovy
plugins {
    id 'nf-bids@0.1.0'
}

// Enable debug logging
log.level = 'DEBUG'
```

### Interactive Testing

```bash
# Launch Groovy console with plugin classpath
./gradlew groovyConsole
```

```groovy
// In console
import nextflow.bids.channel.BidsChannelFactory

def factory = new BidsChannelFactory()
def channel = factory.fromBIDS('/path/to/bids')
```

## Resources

- [Nextflow Plugin Development](https://www.nextflow.io/docs/latest/plugins.html)
- [Groovy Documentation](https://groovy-lang.org/documentation.html)
- [Spock Testing](https://spockframework.org/)
- [BIDS Specification](https://bids-specification.readthedocs.io/)
- [libBIDS.sh parser](https://github.com/CoBrALab/libBIDS.sh)
- [Original bids2nf](https://github.com/agahkarakuzu/bids2nf)
