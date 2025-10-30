# Development Guide

Guide for developers working on the nf-bids plugin implementation.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Building the Plugin](#building-the-plugin)
- [Testing](#testing)
- [Implementation Roadmap](#implementation-roadmap)
- [Contributing](#contributing)

## Getting Started

### Prerequisites

- **Java JDK**: 11 or higher
- **Gradle**: 8.4+ (or use included wrapper)
- **Git**: For cloning libBIDS.sh submodule
- **Nextflow**: 23.10.0+ (for testing)
- **Docker**: Optional, for BIDS validator

### Project Structure

```
nf-bids/
├── src/
│   ├── main/
│   │   ├── groovy/nextflow/bids/
│   │   │   ├── BidsPlugin.groovy          # Plugin entry point
│   │   │   ├── BidsExtension.groovy       # DSL extension
│   │   │   ├── channel/                   # Channel factory
│   │   │   │   └── BidsChannelFactory.groovy
│   │   │   ├── config/                    # Configuration
│   │   │   │   ├── BidsConfigLoader.groovy
│   │   │   │   └── BidsConfigAnalyzer.groovy
│   │   │   ├── parser/                    # BIDS parsing
│   │   │   │   ├── BidsParser.groovy
│   │   │   │   ├── LibBidsShWrapper.groovy
│   │   │   │   └── BidsValidator.groovy
│   │   │   ├── grouping/                  # Set handlers
│   │   │   │   ├── BaseSetHandler.groovy
│   │   │   │   ├── PlainSetHandler.groovy
│   │   │   │   ├── NamedSetHandler.groovy
│   │   │   │   ├── SequentialSetHandler.groovy
│   │   │   │   └── MixedSetHandler.groovy
│   │   │   ├── model/                     # Domain models
│   │   │   │   ├── BidsEntity.groovy
│   │   │   │   ├── BidsFile.groovy
│   │   │   │   ├── BidsDataset.groovy
│   │   │   │   └── BidsChannelData.groovy
│   │   │   └── util/                      # Utilities
│   │   │       ├── BidsLogger.groovy
│   │   │       └── BidsErrorHandler.groovy
│   │   └── resources/
│   │       └── META-INF/
│   │           ├── MANIFEST.MF            # Plugin metadata
│   │           └── extensions.idx         # Extension registry
│   └── test/
│       └── groovy/nextflow/bids/
│           ├── BidsPluginTest.groovy
│           ├── BidsExtensionTest.groovy
│           ├── channel/
│           ├── config/
│           ├── parser/
│           ├── grouping/
│           └── model/
├── validation/                             # Integration tests
│   └── test.nf
├── .dev-notes/                            # Development notes
├── docs/                                  # Documentation
├── build.gradle                           # Build configuration
├── settings.gradle                        # Project settings
└── README.md
```

## Development Setup

> **Note**: The plugin is fully functional. All core components are implemented and tested. See [TODO.md](TODO.md) for current priorities.

### Initial Setup

1. **Navigate to plugin directory**:
   ```bash
   cd /path/to/bids2nf/plugins/nf-bids
   ```

2. **Initialize libBIDS.sh submodule** (from repository root):
   ```bash
   cd ../..  # Go to repository root
   git submodule update --init --recursive
   cd plugins/nf-bids  # Return to plugin directory
   ```

3. **Run setup script**:
   ```bash
   ./setup.sh
   ```

   This script will:
   - Check for Java 11+
   - Download and install Gradle wrapper
   - Download dependencies
   - Verify build configuration

### Manual Setup

If the setup script fails:

1. **Install Gradle wrapper manually**:
   ```bash
   ./setup-gradle.sh
   ```

2. **Download dependencies**:
   ```bash
   ./gradlew dependencies
   ```

3. **Verify setup**:
   ```bash
   ./gradlew tasks
   ```

## Building the Plugin

### Quick Build and Install

```bash
# Build with tests
./gradlew build

# Install to Nextflow
make install

# Or quick test script
./quick-test.sh
```

This will run:
1. Clean build
2. Run tests
3. Build JAR
4. Install to Maven local

### Individual Build Steps

**Clean build:**
```bash
./gradlew clean
```

**Compile:**
```bash
./gradlew compileGroovy
```

**Build without tests** (current requirement):
```bash
./gradlew build -x test
```

**Run tests** (will fail until implementation complete):
```bash
./gradlew test
```

**Build JAR:**
```bash
./gradlew jar
```

**Install to Maven local:**
```bash
./gradlew publishToMavenLocal -x test
```

### Build Output

Built artifacts are located in:
- **JAR**: `build/libs/nf-bids-0.1.0.jar`
- **Classes**: `build/classes/groovy/main/`
- **Test results**: `build/reports/tests/test/index.html`

## Testing

### Unit Tests

Located in `src/test/groovy/nextflow/bids/`

**Run all tests:**
```bash
./gradlew test
```

**Run specific test:**
```bash
./gradlew test --tests BidsPluginTest
```

**View test report:**
```bash
open build/reports/tests/test/index.html
```

### Integration Tests

Located in `validation/test.nf`

**Run integration test:**
```bash
cd validation
nextflow run test.nf -plugins nf-bids@0.1.0
```

### Test Data

Use test datasets from the main bids2nf project:
```bash
# Link test data
ln -s ../../tests/data/bids-examples validation/test-data
```

### Writing Tests

Use Spock framework:

```groovy
package nextflow.bids.channel

import spock.lang.Specification
import nextflow.Session

class BidsChannelFactoryTest extends Specification {
    
    def "should create channel from BIDS directory"() {
        given:
        def bidsDir = '/path/to/test/bids'
        def factory = new BidsChannelFactory()
        
        when:
        def channel = factory.fromBIDS(bidsDir)
        
        then:
        channel != null
        // Add assertions
    }
    
    def "should handle missing BIDS directory"() {
        given:
        def bidsDir = '/nonexistent/path'
        def factory = new BidsChannelFactory()
        
        when:
        factory.fromBIDS(bidsDir)
        
        then:
        thrown(IllegalArgumentException)
    }
}
```

## Implementation Roadmap

### Phase 1: Core Infrastructure ✅

- [x] Plugin scaffolding
- [x] Build configuration
- [x] Test framework
- [x] Documentation structure

### Phase 2: Configuration Layer

See `docs/implementation.md` for detailed steps.

**Priority:**
1. **BidsConfigLoader** - Load and validate YAML
2. **BidsConfigAnalyzer** - Parse configuration structure
3. **Unit tests** - Verify configuration handling

**Reference:**
- Original: `modules/utils/config_analyzer.nf`
- Schema: `config/schemas/bids2nf.schema.yaml`

### Phase 3: BIDS Parsing

**Priority:**
1. **LibBidsShWrapper** - Interface with libBIDS.sh
2. **BidsParser** - Parse BIDS files
3. **BidsValidator** - Optional validation
4. **Unit tests** - Verify parsing

**Reference:**
- Original: `modules/parsers/lib_bids_sh_parser.nf`
- Library: `libBIDS.sh/`

### Phase 4: Grouping Logic

**Priority:**
1. **BaseSetHandler** - Base class
2. **PlainSetHandler** - Simple 1:1 mapping
3. **NamedSetHandler** - Entity grouping
4. **SequentialSetHandler** - Ordered arrays
5. **MixedSetHandler** - Nested structures
6. **Unit tests** - Verify each handler

**Reference:**
- Original: `modules/grouping/`
- Templates: `modules/templates/`

### Phase 5: Channel Factory

**Priority:**
1. **BidsChannelFactory.fromBIDS()** - Main entry point
2. **Preflight checks** - Validation
3. **Dataset processing** - Core logic
4. **Cross-modal broadcasting** - Entity sharing
5. **Integration tests** - End-to-end

**Reference:**
- Original: `main.nf` workflow logic
- Subworkflows: `subworkflows/`

### Phase 6: Extension and Plugin

**Priority:**
1. **BidsExtension** - Register factory
2. **BidsPlugin** - Lifecycle management
3. **Integration tests** - Full workflow
4. **Documentation** - Usage examples

### Phase 7: Polish and Release

**Priority:**
1. Error handling improvements
2. Logging and diagnostics
3. Performance optimization
4. Documentation completion
5. Example workflows

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

Use SLF4J logging:

```groovy
@Slf4j
class MyClass {
    def myMethod() {
        log.debug "Debug message"
        log.info "Info message"
        log.warn "Warning message"
        log.error "Error message", exception
    }
}
```

### Reference Original Code

Each method includes `@reference` tags pointing to the original implementation:

```groovy
/**
 * Process BIDS datasets
 *
 * @reference https://github.com/AlexVCaron/bids2nf/blob/main/main.nf#L100-L150
 */
def processDatasets(Map params) {
    // Implementation based on original code
}
```

## Contributing

### Workflow

1. **Create branch** for your feature:
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Implement feature** following guidelines above

3. **Add tests** for new functionality

4. **Run tests**:
   ```bash
   ./gradlew test
   ```

5. **Build and verify**:
   ```bash
   ./quick-test.sh
   ```

6. **Commit changes**:
   ```bash
   git add .
   git commit -m "feat: add my feature"
   ```

7. **Push and create PR**:
   ```bash
   git push origin feature/my-feature
   ```

### Commit Message Format

Follow conventional commits:

```
feat: add new feature
fix: fix bug
docs: update documentation
test: add tests
refactor: refactor code
chore: update build configuration
```

### Code Review Checklist

- [ ] Tests pass (`./gradlew test`)
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] `@reference` tags present
- [ ] Error handling implemented
- [ ] Logging added where appropriate

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

### IntelliJ IDEA Setup

1. Open project in IntelliJ IDEA
2. Import as Gradle project
3. Set JDK to 11+
4. Enable Groovy support
5. Run tests from IDE

## Resources

- **Nextflow Plugin Development**: https://www.nextflow.io/docs/latest/plugins.html
- **PF4J Documentation**: https://pf4j.org/
- **Groovy Documentation**: https://groovy-lang.org/documentation.html
- **Spock Testing**: https://spockframework.org/
- **BIDS Specification**: https://bids-specification.readthedocs.io/
- **Original bids2nf**: https://github.com/AlexVCaron/bids2nf

## Related Documentation

- [Architecture Guide](architecture.md) - Plugin architecture
- [Implementation Guide](implementation.md) - Step-by-step implementation
- [Testing Guide](testing.md) - Testing strategies
- [API Reference](api.md) - API documentation
