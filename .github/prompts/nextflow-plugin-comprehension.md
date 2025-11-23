# Nextflow Plugin Comprehension Guide

**Document Purpose**: Provide comprehensive understanding of the nf-bids plugin structure, build system, installation mechanisms, and Groovy project conventions to enable effective development and testing.

---

## ⚠️ CRITICAL TERMINAL COMMAND GUIDELINES ⚠️

### Rule 1: ALWAYS Use Absolute Paths

**NEVER use relative paths in terminal commands:**
- ❌ **FORBIDDEN**: `cd validation/edge_cases` (assumes current directory)
- ❌ **FORBIDDEN**: `rm -f test_debug.nf` (will fail if not in correct directory)
- ❌ **FORBIDDEN**: `./gradlew test` from wrong directory (command not found)

**ALWAYS use absolute paths:**
- ✅ **REQUIRED**: `cd /home/local/USHERBROOKE/vala2004/dev/nf-bids` (explicit project root)
- ✅ **REQUIRED**: `rm /home/local/USHERBROOKE/vala2004/dev/nf-bids/validation/edge_cases/test_debug.nf`
- ✅ **REQUIRED**: `/home/local/USHERBROOKE/vala2004/dev/nf-bids/gradlew test`

**Why this matters:**
- Terminal sessions can be in any directory (validation/, edge_cases/, etc.)
- Relative paths fail silently or operate on wrong files
- Absolute paths work regardless of current working directory
- Prevents "No such file or directory" errors

**Pattern to follow:**
```bash
# ALWAYS start with cd to project root using absolute path
cd /home/local/USHERBROOKE/vala2004/dev/nf-bids

# Then use relative paths if needed, or continue with absolute
./gradlew test
# OR
/home/local/USHERBROOKE/vala2004/dev/nf-bids/gradlew test
```

**Example - BAD:**
```bash
rm validation/edge_cases/test_debug.nf  # Fails if cwd is validation/edge_cases/
cd validation  # Ambiguous - validation of what?
```

**Example - GOOD:**
```bash
rm /home/local/USHERBROOKE/vala2004/dev/nf-bids/validation/edge_cases/test_debug.nf
cd /home/local/USHERBROOKE/vala2004/dev/nf-bids/validation
```

---

### Rule 2: Never Obfuscate CLI Output

**NEVER obfuscate CLI output with pipes that hide information:**
- ❌ **FORBIDDEN**: `| head`, `| tail`, `| grep -A/B/C` (hides context)
- ❌ **FORBIDDEN**: `2>&1 | head -100` (truncates critical error messages)
- ❌ **FORBIDDEN**: Any pipe that prevents seeing the full command output

**ALWAYS show complete output:**
- ✅ **ALLOWED**: `| tee filename` (saves to file while showing output)
- ✅ **ALLOWED**: Full command output without pipes
- ✅ **ALLOWED**: `| grep pattern` only if specifically searching, not limiting output
- ✅ **PREFERRED**: Let commands run completely and show all output naturally

**Why this matters:**
- Build failures need complete stack traces
- Test failures require full error context
- Debugging needs unfiltered information
- User needs to see what's actually happening

**Example - BAD:**
```bash
./gradlew test 2>&1 | head -100  # NEVER DO THIS
```

**Example - GOOD:**
```bash
./gradlew test  # Shows everything
./gradlew test | tee test-output.log  # Save and show
```

---

## Table of Contents

1. [Plugin Overview](#plugin-overview)
2. [Project Structure](#project-structure)
3. [Plugin Entry Points](#plugin-entry-points)
4. [Build System (Gradle)](#build-system-gradle)
5. [Plugin Installation & Testing](#plugin-installation--testing)
6. [Groovy Project Facts](#groovy-project-facts)
7. [Extension System](#extension-system)
8. [Testing Strategy](#testing-strategy)
9. [Development Workflow](#development-workflow)
10. [Common Issues & Solutions](#common-issues--solutions)

---

## Plugin Overview

**Plugin Name**: `nf-bids`  
**Version**: `0.1.0-beta.5`  
**Provider**: `nf-neuro`  
**Nextflow Version**: 24.10.0+  
**Gradle Version**: 8.14  
**Groovy Version**: 4.0.23  

### Purpose
The nf-bids plugin extends Nextflow with BIDS (Brain Imaging Data Structure) dataset handling capabilities and enhanced channel operators for flexible data grouping and manipulation.

### Key Features
- **Channel Factory**: `Channel.fromBIDS()` for parsing BIDS datasets
- **Channel Operators**: `groupTupleBy()`, `joinBy()`, `combineBy()` for closure-based channel manipulation
- **BIDS Parsing**: Integration with libBIDS.sh for BIDS file parsing
- **Data Grouping**: Support for Plain, Named, Sequential, and Mixed set types

---

## Project Structure

```
nf-bids/
├── build.gradle                    # Gradle build configuration
├── settings.gradle                 # Project settings (rootProject.name)
├── Makefile                        # Simplified build/install commands
├── nextflow.config                 # Plugin configuration for testing
├── nf-test.config                  # nf-test framework configuration
│
├── src/main/
│   ├── groovy/nfneuro/
│   │   ├── plugin/                 # Plugin registration & extensions
│   │   │   ├── BidsPlugin.groovy           # Entry point (extends BasePlugin)
│   │   │   ├── BidsExtension.groovy        # Channel factory + operators
│   │   │   ├── BidsFactory.groovy          # Trace observer registration
│   │   │   ├── BidsObserver.groovy         # Workflow event tracing
│   │   │   └── channel/
│   │   │       ├── BidsChannelFactory.groovy       # fromBIDS implementation
│   │   │       ├── KeyExtractor.groovy             # Key extraction logic
│   │   │       ├── keys/
│   │   │       │   └── CompositeKey.groovy         # Multi-field key support
│   │   │       └── ops/
│   │   │           └── GroupTupleByOp.groovy       # Operator implementation
│   │   │
│   │   ├── channel/                # BIDS channel handling
│   │   ├── config/                 # Configuration management
│   │   ├── parser/                 # BIDS parsing (libBIDS.sh wrapper)
│   │   ├── grouping/               # Set handlers (Plain/Named/Sequential/Mixed)
│   │   ├── model/                  # Domain models (BidsFile, BidsDataset, etc.)
│   │   └── util/                   # Utilities (logging, error handling)
│   │
│   └── resources/
│       └── META-INF/
│           └── extensions.idx      # Extension registration index
│
├── src/test/groovy/               # Spock unit tests
│   └── nfneuro/plugin/channel/
│       ├── KeyExtractorTest.groovy
│       ├── keys/CompositeKeyTest.groovy
│       └── ops/GroupTupleByOpTest.groovy
│
├── validation/                    # Integration tests (nf-test)
│   ├── main.nf                         # Base workflow test
│   ├── nextflow.config                 # Plugin config for tests
│   ├── test_datasets.sh                # Test suite runner
│   ├── test_grouptupleby.nf            # GroupTupleBy operator tests
│   ├── comparison_*.nf.test            # nf-test scenarios
│   ├── data/                           # Test datasets
│   └── configs/                        # Test configurations
│
├── libBIDS.sh/                    # Git submodule (BIDS parser)
├── docs/                          # Documentation
├── .github/
│   ├── prompts/                   # Agent comprehension guides
│   └── research/                  # Design research
└── build/                         # Gradle output directory
    ├── classes/groovy/main/       # Compiled .class files
    ├── libs/                      # Built JARs
    ├── resources/main/            # Resources (META-INF)
    └── reports/tests/             # Test reports
```

---

## Plugin Entry Points

### 1. BidsPlugin.groovy
**Path**: `src/main/groovy/nfneuro/plugin/BidsPlugin.groovy`  
**Purpose**: Plugin registration entry point

```groovy
@CompileStatic
class BidsPlugin extends BasePlugin {
    BidsPlugin(PluginWrapper wrapper) {
        super(wrapper)
    }
}
```

- Extends `nextflow.plugin.BasePlugin` (PF4J plugin framework)
- Registered in `build.gradle` via `className = 'nfneuro.plugin.BidsPlugin'`
- No custom initialization required (framework handles extension discovery)

### 2. Extension Registration
**Path**: `src/main/resources/META-INF/extensions.idx`  
**Purpose**: Declares plugin extensions for auto-discovery

```plaintext
nfneuro.plugin.BidsExtension
```

- Each line declares a `PluginExtensionPoint` subclass
- Extensions are automatically discovered by Nextflow at runtime
- Must match `extensionPoints` in `build.gradle` (validation)

**IMPORTANT**: Nextflow plugins can only have **one extension point** per plugin. This is why `BidsExtension` combines both factory methods (`@Factory`) and operator methods (`@Operator`) in a single class. Earlier versions of this plugin had separate `BidsExtension` and `ChannelGroupingExtension` classes, but this violated the single extension point constraint and has been consolidated.

### 3. Extension Overview

#### BidsExtension
- **Type**: Combined Factory + Operator extension
- **Factory Methods**: 
    - `Channel.fromBIDS(bidsDir, configPath, options)` - Create channels from BIDS datasets
        - Note: By default (beta.6+), the plugin emits flattened maps with a `meta` map and top-level suffix keys. To preserve legacy tuple format, set `options.flatten_output = false`.
- **Operator Methods**:
  - `channel.groupTupleBy(keyExtractor, opts)` - Group channel items by dynamic keys
  - `channel.joinBy(right, leftKey, rightKey, opts)` - Join channels by dynamic keys (stub)
    - `channel.combineBy(right, leftKeyExtractor, rightKeyExtractor, opts)` - Combine channels by extracting keys using closures and emitting `[key, leftList, rightList]` tuples. Use a single closure for left-only key extraction or two closures to extract keys from both sides.
- **Purpose**: Provide BIDS dataset parsing and enhanced channel operators

---

## Build System (Gradle)

### ⚠️ CRITICAL: Gradle Build Directory Requirements ⚠️

**YOU MUST BE IN THE PROJECT ROOT DIRECTORY TO RUN GRADLE COMMANDS**

Gradle requires being in the directory containing `build.gradle` and `settings.gradle` to function correctly. Running from subdirectories causes build failures.

**Project Root**: `/home/local/USHERBROOKE/vala2004/dev/nf-bids`

**MANDATORY PRE-BUILD CHECKS:**
1. **ALWAYS check terminal working directory before running gradle**
2. **ALWAYS navigate to project root if not already there**
3. **Use `cd /` first to reset path, then navigate to project root**

**Terminal Working Directory Issues:**
- Terminal sessions cache their working directory
- `cd` alone may not be sufficient if terminal is in a subdirectory
- Use `cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids` to force reset

**WRONG (causes errors):**
```bash
# Terminal is in: /home/local/USHERBROOKE/vala2004/dev/nf-bids/validation
./gradlew compileGroovy
# ERROR: Project directory 'validation' is not part of the build

# Terminal is in: /home/local/USHERBROOKE/vala2004/dev/nf-bids/validation/edge_cases
cd /home/local/USHERBROOKE/vala2004/dev/nf-bids
./gradlew compileGroovy
# May still ERROR if terminal didn't actually change directory!

# Using absolute path from wrong directory
/home/local/USHERBROOKE/vala2004/dev/nf-bids/gradlew compileGroovy
# ERROR: Gradle checks PWD, not where gradlew is located!
```

**CORRECT (guaranteed to work):**
```bash
# STEP 1: Force terminal to reset by going to root first
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids

# STEP 2: Verify you're in the right place
pwd
# Must output: /home/local/USHERBROOKE/vala2004/dev/nf-bids

# STEP 3: Now run gradle
./gradlew compileGroovy

# Or in one command:
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && pwd && ./gradlew compileGroovy
```

**All Gradle Commands Must Follow This Pattern:**
```bash
# Compilation only
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && ./gradlew compileGroovy

# Clean build
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && ./gradlew clean compileGroovy

# Build JAR
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && ./gradlew jar

# Run tests
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && ./gradlew test
```

**Why This Matters:**
- Gradle uses the current working directory (`PWD`) to locate configuration files
- Even with absolute paths to `gradlew`, Gradle still checks `PWD`
- Terminal sessions retain their working directory across commands
- Must explicitly reset terminal location before every gradle command

---

### 🔥 CRITICAL: Installing Plugin into Nextflow 🔥

**AFTER ANY CODE CHANGES, YOU MUST INSTALL THE PLUGIN FOR NEXTFLOW TO USE IT**

Simply compiling (`./gradlew compileGroovy`) or building JARs (`./gradlew jar`) is **NOT ENOUGH**. Nextflow loads plugins from `~/.nextflow/plugins/`, and you must install the plugin there.

**Installation Command:**
```bash
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && make install
```

Or directly with gradle:
```bash
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids && ./gradlew install
```

**Complete Development Workflow:**
```bash
# After editing operator source code:

# 1. Navigate to project root
cd / && cd /home/local/USHERBROOKE/vala2004/dev/nf-bids

# 2. Clean build and install
make clean && make install

# Or with gradle:
./gradlew clean install

# 3. Run tests
cd validation && nextflow run test_joinby.nf
```

**What `make install` / `./gradlew install` Does:**
- Compiles all Groovy source files
- Builds the plugin JAR
- Copies the plugin to `~/.nextflow/plugins/nf-bids-0.1.0-beta.4/`
- Nextflow will load the plugin from this location

**Common Mistake:**
```bash
# ❌ WRONG - Changes won't be used by Nextflow!
./gradlew compileGroovy
nextflow run test_joinby.nf
# Uses OLD cached plugin, not your changes!

# ✅ CORRECT - Install first!
./gradlew install
nextflow run test_joinby.nf
# Uses NEW installed plugin with your changes!
```

**When to Run `make install`:**
- After modifying any `.groovy` file in `src/main/groovy/`
- After changing operator logic
- After updating plugin configuration
- Before running any Nextflow tests that use the plugin

**Quick Reference:**
- `make install` = Compile + Install plugin into Nextflow
- `make clean` = Remove build artifacts and cache
- `make test` = Run unit tests only (not integration tests)

**Why this matters:**
- Gradle looks for `settings.gradle` to determine project root
- Running from subdirectory confuses Gradle's project structure detection
- Error message: "Project directory '...' is not part of the build defined by settings file"
- Solution: ALWAYS `cd` to project root before running any `gradlew` command

**Quick verification:**
```bash
pwd  # Should show: /home/local/USHERBROOKE/vala2004/dev/nf-bids
ls build.gradle settings.gradle  # Both should exist
```

### build.gradle Structure

```gradle
plugins {
    id 'io.nextflow.nextflow-plugin' version '1.0.0-beta.10'
}

version = '0.1.0-beta.4'

nextflowPlugin {
    nextflowVersion = '24.10.0'
    provider = 'nf-neuro'
    className = 'nfneuro.plugin.BidsPlugin'
    extensionPoints = [
        'nfneuro.plugin.BidsExtension'
    ]
    dependencies {
        implementation fileTree('libBIDS.sh') { include '*.sh' }
    }
}
```

**Key Facts**:
- **Plugin Framework**: Uses `io.nextflow.nextflow-plugin` Gradle plugin
  - Provides custom tasks: `installPlugin`, `packagePlugin`, `releasePlugin`
  - Handles plugin packaging, metadata generation, and installation
- **className**: Must point to the `BasePlugin` subclass
- **extensionPoints**: Must list all `PluginExtensionPoint` subclasses (validated at build)
  - **Limitation**: Nextflow plugins support **only one extension point**. All factories and operators must be in the same extension class.
- **dependencies**: Can include non-JAR resources (e.g., bash scripts)

### Available Gradle Tasks

**Build Tasks**:
```bash
./gradlew clean              # Delete build/ directory
./gradlew compileGroovy      # Compile Groovy sources
./gradlew build              # Compile + test + package JAR
./gradlew assemble           # Compile + package (no tests)
./gradlew jar                # Create plugin JAR
```

**Testing Tasks**:
```bash
./gradlew test               # Run Spock unit tests
./gradlew test --tests "*GroupTupleBy*"  # Run specific tests
```

**Plugin Tasks** (from `io.nextflow.nextflow-plugin`):
```bash
./gradlew installPlugin      # Install to ~/.nextflow/plugins/
./gradlew packagePlugin      # Create plugin package
./gradlew releasePlugin      # Publish to registry (requires config)
```

### installPlugin Task Details

**Type**: `PluginInstallTask` (io.nextflow.gradle.PluginInstallTask)  
**Action**: Installs plugin to local Nextflow plugins directory

**Installation Location**:
```
~/.nextflow/plugins/
├── nf-bids-0.1.0-beta.4/
│   ├── nf-bids-0.1.0-beta.4.jar
│   ├── plugin.json              # Plugin metadata
│   └── classes/                 # Extracted classes (if needed)
```

**Process**:
1. Assembles plugin JAR (`build/libs/nf-bids-*.jar`)
2. Generates plugin metadata (`plugin.json`)
3. Copies to `~/.nextflow/plugins/nf-bids-<version>/`
4. Makes plugin available to all local Nextflow runs

**Usage**:
```bash
./gradlew installPlugin
# OR via Makefile
make install
```

After installation, workflows can use:
```nextflow
plugins {
    id 'nf-bids@0.1.0-beta.4'
}
```

---

## Plugin Installation & Testing

### Installation Methods

#### 1. Local Installation (Development)
**Use Case**: Testing plugin changes during development

```bash
# Method 1: Gradle task
./gradlew installPlugin

# Method 2: Makefile wrapper
make install

# Method 3: Build + install
./gradlew build install
```

**Verification**:
```bash
ls -la ~/.nextflow/plugins/nf-bids-0.1.0-beta.4/
```

#### 2. Project-Local Installation (validation/)
**Use Case**: Integration tests in `validation/` directory

The `validation/nextflow.config` uses:
```groovy
plugins {
    id 'nf-bids@0.1.0-beta.4'
}
```

This requires the plugin to be installed via `./gradlew installPlugin` first.

#### 3. Remote Installation (Production)
**Use Case**: Published plugins from registry

```groovy
plugins {
    id 'nf-bids' version '0.1.0-beta.4'
}
```

Nextflow downloads from configured plugin registry.

### Testing Workflow

#### Unit Tests (Spock)
**Location**: `src/test/groovy/`  
**Framework**: Spock Framework  
**Purpose**: Test individual classes in isolation

```bash
# Run all unit tests
./gradlew test

# Run specific test
./gradlew test --tests "*GroupTupleByOpTest"

# View results
open build/reports/tests/test/index.html
```

**Test Structure**:
```groovy
class GroupTupleByOpTest extends Specification {
    def "should create operator instance"() {
        given:
        def source = Mock(DataflowReadChannel)
        def keyExtractor = { it.id }
        
        when:
        def op = new GroupTupleByOp(source, keyExtractor, [:])
        
        then:
        op != null
        op.source == source
    }
}
```

**Limitations**:
- Cannot easily test async DataflowQueue operations
- No access to Nextflow runtime (Session, CH, etc.)
- Suitable for: instantiation, validation, pure logic

#### Integration Tests (nf-test)
**Location**: `validation/`  
**Framework**: nf-test  
**Purpose**: Test operators in real Nextflow workflows

**Prerequisites**:
```bash
# 1. Install plugin
./gradlew installPlugin

# 2. Verify plugin installed
ls ~/.nextflow/plugins/nf-bids-0.1.0-beta.4/
```

**Run Integration Tests**:
```bash
# Manual workflow test
nextflow run validation/test_grouptupleby.nf

# nf-test suite (if configured)
cd validation/
nf-test test comparison_custom_datasets.nf.test
```

**Integration Test Structure**:
```nextflow
#!/usr/bin/env nextflow

nextflow.enable.dsl=2

include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    Channel
        .of([subject: 'sub-01', file: 'a.txt'],
            [subject: 'sub-01', file: 'b.txt'])
        .groupTupleBy { it.subject }
        .view()
}
```

#### Full Validation
```bash
# Run all integration tests
cd validation/
./test_datasets.sh

# Or via Makefile
make test  # (if configured)
```

---

## Groovy Project Facts

### Compilation & Type Checking

#### @CompileStatic Annotation
**Purpose**: Enable static compilation for performance and type safety

```groovy
@CompileStatic
class GroupTupleByOp {
    private DataflowReadChannel source
    private Closure keyExtractor
    
    // Compiler checks types at compile-time
    DataflowWriteChannel apply() {
        return target
    }
}
```

**Benefits**:
- Type checking at compile-time (catches errors early)
- Better performance (no dynamic dispatch)
- IDE support (autocomplete, refactoring)

**Usage Pattern**:
- Use on all classes except where dynamic features required
- Extension classes: Always use `@CompileStatic`
- Test classes: Optional (Spock has dynamic features)

#### Groovy vs Java Differences

**Property Access**:
```groovy
// Groovy auto-generates getters/setters
class BidsFile {
    String path        // Creates getPath() and setPath()
}

// Access: obj.path (calls getPath() internally)
```

**Closures**:
```groovy
// Closures are first-class objects
Closure keyExtractor = { Map item -> item.subject }

// Can be passed as parameters
def result = extractKey(item, keyExtractor)
```

**String Interpolation**:
```groovy
def name = "Alice"
log.info("Hello, ${name}!")  // GString interpolation
```

**Collections**:
```groovy
// List literals
def list = [1, 2, 3]

// Map literals
def map = [key: 'value', num: 42]

// Safe navigation
def result = obj?.property?.nested  // No NPE
```

### Logging with @Slf4j

```groovy
@Slf4j
@CompileStatic
class MyClass {
    void myMethod() {
        log.debug("Debug message")      // if log.level = DEBUG
        log.info("Info message")        // default level
        log.warn("Warning")
        log.error("Error", exception)
    }
}
```

**Log Field**: `@Slf4j` injects `private static final Logger log`

### Extension Annotations

#### @Factory (Channel Factories)
**Purpose**: Register factory methods that create channels

```groovy
@Factory
DataflowWriteChannel fromBIDS(String bidsDir, ...) {
    // Creates Channel.fromBIDS()
    return channel
}
```

**Usage in Workflow**:
```nextflow
Channel.fromBIDS('/data/bids', 'config.yaml')
```

#### @Operator (Channel Operators)
**Purpose**: Register methods that operate on channels

```groovy
@Operator
DataflowWriteChannel groupTupleBy(
    DataflowReadChannel source,
    Closure keyExtractor,
    Map opts
) {
    // Creates channel.groupTupleBy()
    return new GroupTupleByOp(source, keyExtractor, opts).apply()
}
```

**Usage in Workflow**:
```nextflow
channel
    .of([id: 'A', val: 1], [id: 'A', val: 2])
    .groupTupleBy { it.id }
```

### Async Channel Operations (GPars)

**DataflowQueue**: Async message queue for channel items

```groovy
import groovyx.gpars.dataflow.DataflowQueue
import nextflow.extension.DataflowHelper

// Create channel
def target = CH.createBy(source)  // Nextflow helper

// Subscribe to source channel
DataflowHelper.subscribeImpl(source, [
    onNext: { item -> 
        // Process each item asynchronously
        target.bind(item)
    },
    onComplete: {
        // Called when source channel closes
        target.bind(Channel.STOP)
    }
])
```

**Key Concepts**:
- `bind()`: Emit item to channel (async, non-blocking)
- `Channel.STOP`: Special token to close channel
- `subscribeImpl()`: Register async handlers (onNext, onComplete)
- No blocking operations (`.get()`, `.await()`) in operators

---

## Extension System

### Extension Discovery Flow

```
1. Nextflow starts
   │
2. Load plugins from ~/.nextflow/plugins/
   │
3. For each plugin:
   ├─ Read META-INF/extensions.idx
   ├─ Instantiate extension classes
   └─ Scan for @Factory and @Operator methods
   │
4. Register methods in DSL
   ├─ @Factory → Channel.methodName()
   └─ @Operator → channel.methodName()
   │
5. Workflow script must INCLUDE operators explicitly
   include { operatorName } from 'plugin/plugin-id'
```

**CRITICAL**: Even though the plugin is loaded via `nextflow.config`, you MUST use an `include` statement to access operators in your workflow script. This is not optional.

### Extension Base Class Pattern

```groovy
@CompileStatic
class BidsExtension extends PluginExtensionPoint {
    private Session session
    
    @Override
    protected void init(Session session) {
        this.session = session
    }
    
    @Factory
    DataflowWriteChannel fromBIDS(...) {
        // Factory implementation
    }
    
    @Operator
    DataflowWriteChannel groupTupleBy(...) {
        // Operator implementation
    }
}
```

**Required**:
- Extend `PluginExtensionPoint`
- Implement `init(Session)` (can be empty)
- Annotate public methods with `@Factory` or `@Operator`

**Important Constraint**: A Nextflow plugin can only have **one extension point class**. This means:
- All factory methods and operator methods must be in the same extension class
- You cannot split functionality into multiple extension classes (e.g., separate `BidsFactoryExtension` and `BidsOperatorExtension`)
- This is why `BidsExtension` contains both `@Factory` methods (like `fromBIDS`) and `@Operator` methods (like `groupTupleBy`, `joinBy`, `combineBy`)

### Method Signature Rules

#### Factory Methods
```groovy
@Factory
DataflowWriteChannel methodName(
    // Parameters from workflow call
    String param1,
    Map options = [:]  // Optional params with defaults
) {
    // Return a DataflowWriteChannel
}
```

#### Operator Methods
```groovy
@Operator
DataflowWriteChannel methodName(
    DataflowReadChannel source,  // FIRST param: input channel
    // Additional parameters
    Closure closure,
    Map opts = [:]
) {
    // Return a DataflowWriteChannel
}
```

**Key Rule**: First parameter of `@Operator` is always the source channel (implicit in workflow syntax).

### Operator Implementation Pattern

**Extension Method** (thin wrapper):
```groovy
@Operator
DataflowWriteChannel groupTupleBy(
    DataflowReadChannel source,
    Closure keyExtractor,
    Map opts = [:]
) {
    // Validation
    KeyExtractor.validateKeyExtractor(keyExtractor, 'groupTupleBy')
    
    // Delegate to operator class
    def op = new GroupTupleByOp(source, keyExtractor, opts)
    return op.apply()
}
```

**Operator Class** (heavy logic):
```groovy
@CompileStatic
class GroupTupleByOp {
    private DataflowReadChannel source
    private Closure keyExtractor
    private Map opts
    private DataflowWriteChannel target
    private Map<Object, List> groups = [:]
    
    DataflowWriteChannel apply() {
        target = CH.createBy(source)
        DataflowHelper.subscribeImpl(source, [
            onNext: this.&onNext,
            onComplete: this.&onComplete
        ])
        return target
    }
    
    private void onNext(Object item) {
        // Process each item
        def key = extractKey(item)
        groups.computeIfAbsent(key, { [] }).add(item)
    }
    
    private void onComplete() {
        // Emit all groups
        groups.each { key, items ->
            target.bind([key, items])
        }
        target.bind(Channel.STOP)
    }
}
```

---

## Testing Strategy

### Test Type Decision Matrix

| Test Type | Purpose | Framework | Location | When to Use |
|-----------|---------|-----------|----------|-------------|
| **Unit Tests** | Test individual classes | Spock | `src/test/` | Logic, validation, instantiation |
| **Smoke Tests** | Verify basic functionality | Spock | `src/test/` | Operator instantiation, type checking |
| **Integration Tests** | Test in real workflows | nf-test / Nextflow | `validation/` | Operator behavior, channel operations |
| **Validation Tests** | End-to-end scenarios | nf-test | `validation/` | Full plugin workflows |

### Unit Test Guidelines

**What to Test**:
- ✅ Class instantiation
- ✅ Validation logic (throws correct exceptions)
- ✅ Key extraction logic (KeyExtractor)
- ✅ Option parsing and normalization
- ✅ Type checking (returns correct channel types)

**What NOT to Test**:
- ❌ Async channel operations (toList(), subscribe())
- ❌ DataflowQueue item collection
- ❌ Workflow execution
- ❌ Nextflow runtime behavior

**Example**:
```groovy
class GroupTupleByOpTest extends Specification {
    def "should create operator instance"() {
        given:
        def source = Mock(DataflowReadChannel)
        def keyExtractor = { it.id }
        
        when:
        def op = new GroupTupleByOp(source, keyExtractor, [:])
        
        then:
        op != null
        op.source == source
    }
    
    def "should throw on invalid key extractor"() {
        when:
        def op = new GroupTupleByOp(source, null, [:])
        
        then:
        thrown(IllegalArgumentException)
    }
}
```

### Integration Test Guidelines

**Prerequisites**:
1. Install plugin: `./gradlew installPlugin`
2. Create test workflow in `validation/`
3. Configure plugin in `nextflow.config`

**Workflow Structure**:
```nextflow
#!/usr/bin/env nextflow

nextflow.enable.dsl=2

include { groupTupleBy } from 'plugin/nf-bids'

workflow TEST_GROUPTUPLEBY {
    take:
    input_ch
    
    main:
    grouped = input_ch.groupTupleBy { it.subject }
    
    emit:
    grouped
}

workflow {
    // Test data
    data = Channel.of(
        [subject: 'sub-01', file: 'a.txt'],
        [subject: 'sub-01', file: 'b.txt'],
        [subject: 'sub-02', file: 'c.txt']
    )
    
    // Run test
    TEST_GROUPTUPLEBY(data).view()
}
```

**Run Test**:
```bash
nextflow run validation/test_grouptupleby.nf -ansi-log false
```

**Expected Output**:
```
[sub-01, [[subject:sub-01, file:a.txt], [subject:sub-01, file:b.txt]]]
[sub-02, [[subject:sub-02, file:c.txt]]]
```

---

## Development Workflow

### Standard Development Cycle

```bash
# 1. Make code changes
vim src/main/groovy/nfneuro/plugin/channel/ops/MyNewOp.groovy

# 2. Run unit tests
./gradlew test --tests "*MyNewOpTest"

# 3. Build plugin
./gradlew build

# 4. Install to Nextflow
./gradlew installPlugin
# OR
make install

# 5. Test in workflow
nextflow run validation/test_mynewop.nf

# 6. Commit changes
git add .
git commit -m "Add MyNewOp operator"
```

### Makefile Commands

```bash
# Build plugin
make assemble         # Compile + package (no tests)

# Clean build artifacts
make clean            # Remove build/ and .nextflow*

# Run unit tests
make test             # ./gradlew test

# Install to Nextflow
make install          # ./gradlew installPlugin

# Publish plugin (requires config)
make release          # ./gradlew releasePlugin
```

### Incremental Development

**Quick iteration** (no full rebuild):
```bash
# Compile only changed files
./gradlew compileGroovy

# Run specific test
./gradlew test --tests "*GroupTupleByOp*" --no-daemon

# Fast install (skip up-to-date checks)
./gradlew installPlugin --rerun
```

### Performance Benchmarking

**Location**: `validation/benchmark/`

**Structure**:
```
validation/benchmark/
├── nextflow.config              # Plugin config for benchmarks
├── benchmark_grouptuple.nf      # groupTuple vs groupTupleBy
├── benchmark_join.nf            # join vs joinBy  
├── benchmark_combine.nf         # combine vs combineBy
└── BENCHMARK_RESULTS.md         # Detailed analysis
```

**Running Benchmarks**:
```bash
cd validation/benchmark

# Individual benchmarks
nextflow run benchmark_grouptuple.nf
nextflow run benchmark_join.nf
nextflow run benchmark_combine.nf

# Clean between runs
rm -rf .nextflow* work
```

**Benchmark Design Principles**:
1. Test multiple dataset sizes (100, 1K, 10K items)
2. Measure execution time with `System.currentTimeMillis()`
3. Use realistic BIDS-like data structures
4. Test both index-based and semantic key extraction
5. Document results with performance deltas

**Lessons Learned**:
- Simple data structures (2-element tuples) work best for comparing with built-in operators
- Complex tuples (5+ elements) can cause issues with Nextflow's standard operators
- Our closure-based operators handle complex data more robustly
- Performance varies significantly with duplicate key frequency (joinBy issue)

### Debugging Tips

#### 1. Enable Debug Logging
**In workflow** (`nextflow.config`):
```groovy
log.level = 'DEBUG'

plugins {
    id 'nf-bids@0.1.0-beta.4'
}
```

**In code**:
```groovy
@Slf4j
class MyClass {
    void myMethod() {
        log.debug("Entering myMethod with params: ${params}")
    }
}
```

#### 2. Use .view() Operator
```nextflow
channel
    .groupTupleBy { it.subject }
    .view { "Grouped: ${it}" }  // Print to console
```

#### 3. Interactive Groovy Console
```bash
./gradlew groovyConsole
```

```groovy
// In console
import nfneuro.plugin.channel.keys.CompositeKey

def key = new CompositeKey('sub-01', 'ses-01')
println key.toString()  // [sub-01, ses-01]
```

#### 4. Check Compiled Classes
```bash
# Verify class compiled
ls -la build/classes/groovy/main/nfneuro/plugin/channel/ops/GroupTupleByOp.class

# Decompile (if needed)
javap -c build/classes/groovy/main/nfneuro/plugin/channel/ops/GroupTupleByOp.class
```

---

## Common Issues & Solutions

### Issue 0: Filesystem Navigation Confusion
**Symptom**: Commands fail with "No such file or directory" for `gradlew` or other project files

**Cause**: Executing commands from wrong directory (e.g., subdirectory like `validation/`)

**Solution**:
Always verify current directory before running build commands:
```bash
pwd  # Should show: /home/local/USHERBROOKE/vala2004/dev/nf-bids
ls gradlew  # Should exist at project root
cd /home/local/USHERBROOKE/vala2004/dev/nf-bids  # Navigate to root if needed
```

**Key Directories**:
- **Project root**: `/home/local/USHERBROOKE/vala2004/dev/nf-bids` - Contains `gradlew`, `build.gradle`
- **Validation tests**: `validation/` subdirectory - Contains `test_grouptupleby.nf`
- **Source code**: `src/main/groovy/nfneuro/plugin/`
- **Unit tests**: `src/test/groovy/nfneuro/plugin/`

**Best Practice**: Start commands with `cd` to project root, then use relative paths.

### Issue 1: Plugin Not Found
**Error**:
```
ERROR ~ Plugin with id nf-bids not found in any repository
```

**Cause**: Plugin not installed locally

**Solution**:
```bash
cd /home/local/USHERBROOKE/vala2004/dev/nf-bids
./gradlew installPlugin
# Verify
ls ~/.nextflow/plugins/nf-bids-0.1.0-beta.4/
```

### Issue 2: Operator Not Found / Missing Include
**Error**:
```
Missing process or function groupTupleBy(...)
-- OR --
No such method: groupTupleBy for channel
```

**Cause**: Missing `include` statement in workflow script

**Solution**:
**CRITICAL**: You MUST include plugin operators explicitly, even though plugin is loaded in config:
```groovy
include { groupTupleBy } from 'plugin/nf-bids'
```

Without this, operators will not be available.

**Also check**: Extension registered in `src/main/resources/META-INF/extensions.idx`:
```plaintext
nfneuro.plugin.BidsExtension
```

Must match `extensionPoints` in `build.gradle`.

**Note**: If you have multiple extension classes listed, consolidate them into one. Nextflow plugins can only have **one extension point**.

### Issue 3: Unit Tests Hang
**Symptom**: Tests timeout or hang indefinitely

**Cause**: Attempting to block on async DataflowQueue operations

**Bad Code**:
```groovy
def result = op.apply().toList().getVal()  // HANGS!
```

**Solution**: Use smoke tests instead
```groovy
def "should return DataflowQueue"() {
    when:
    def result = op.apply()
    
    then:
    result instanceof DataflowQueue
}
```

**Async Testing**: Use integration tests in `validation/` with real Nextflow execution.

### Issue 4: publishToMavenLocal Not Found
**Error**:
```
Task 'publishToMavenLocal' not found
```

**Cause**: nf-bids uses `io.nextflow.nextflow-plugin` (not Maven publishing)

**Solution**: Use `installPlugin` instead
```bash
./gradlew installPlugin
```

### Issue 5: Version Mismatch
**Error**:
```
Plugin nf-bids@0.1.0-beta.3 found, but 0.1.0-beta.4 required
```

**Cause**: Old plugin version installed

**Solution**:
```bash
# Remove old version
rm -rf ~/.nextflow/plugins/nf-bids-0.1.0-beta.3/

# Install new version
./gradlew clean installPlugin
```

### Issue 6: Gradle Daemon Issues
**Symptom**: Build fails with obscure errors

**Solution**:
```bash
# Stop daemon
./gradlew --stop

# Clean build
./gradlew clean build --no-daemon
```

### Issue 7: Class Not Found at Runtime
**Error**:
```
ClassNotFoundException: nfneuro.plugin.channel.ops.GroupTupleByOp
```

**Cause**: Class not compiled or JAR not rebuilt

**Solution**:
```bash
./gradlew clean build installPlugin
```

---

## Quick Reference

### Most Common Commands

```bash
# Full build + install cycle
./gradlew clean build installPlugin

# Quick test cycle
./gradlew test --tests "*MyTest" --no-daemon

# Integration test
nextflow run validation/test_workflow.nf

# View test results
open build/reports/tests/test/index.html
```

### File Locations

| What | Where |
|------|-------|
| Plugin JAR | `build/libs/nf-bids-0.1.0-beta.4.jar` |
| Compiled classes | `build/classes/groovy/main/` |
| Test reports | `build/reports/tests/test/index.html` |
| Installed plugin | `~/.nextflow/plugins/nf-bids-0.1.0-beta.4/` |
| Extension index | `src/main/resources/META-INF/extensions.idx` |
| Plugin config | `build.gradle` |

### Key Gradle Properties

```gradle
version = '0.1.0-beta.4'              // Plugin version
nextflowVersion = '24.10.0'           // Minimum Nextflow version
className = 'nfneuro.plugin.BidsPlugin'  // Entry point class
extensionPoints = [...]               // Extension classes
```

### Environment Variables

```bash
# Nextflow home (plugin location)
echo $NXF_HOME  # Default: ~/.nextflow

# Plugin installation path
ls $NXF_HOME/plugins/nf-bids-0.1.0-beta.4/

# Java home (for Gradle)
echo $JAVA_HOME
```

---

## Summary

**Key Takeaways**:

1. **Plugin Structure**: `BidsPlugin` (entry) → Extensions (in `extensions.idx`) → Operators/Factories
2. **Build System**: Gradle with `io.nextflow.nextflow-plugin` plugin provides custom tasks
3. **Installation**: `./gradlew installPlugin` → `~/.nextflow/plugins/nf-bids-*/`
4. **Testing**: Unit tests (Spock) for logic, integration tests (nf-test) for workflows
5. **Development**: Edit → Test → Build → Install → Validate cycle
6. **Async Channels**: Use `DataflowHelper.subscribeImpl()`, avoid blocking operations

**Critical Thread-Safety Patterns**:

7. **Race Conditions in Operators**: 
   - ❌ NEVER use `if (!map.containsKey(key)) { map[key] = value }` pattern
   - ✅ ALWAYS use `map.computeIfAbsent(key, { k -> initialValue })`
   - ✅ For counters: use `map[key] = (map[key] ?: 0) + 1` (atomic read-modify-write)
   
8. **Synchronized Methods**:
   - All methods accessing shared mutable state MUST be `synchronized`
   - This includes: `processItem()`, `emitGroup()`, `completeGrouping()`
   - Race conditions manifest as NullPointerExceptions during concurrent access

9. **Testing for Concurrency**:
   - Unit tests may not catch race conditions (single-threaded execution)
   - Integration tests with real Nextflow execution reveal concurrency bugs
   - Benchmark tests with large datasets stress-test thread safety

**Async Testing Patterns**:

10. **Testing with Callbacks**:
    - ❌ NEVER assume workflow blocks until channels complete
    - ✅ ALWAYS use `subscribe(onNext: {...}, onComplete: {...})` pattern
    - ✅ Add `Thread.sleep(timeInMs)` after subscribe to wait for processing
    - ✅ Place assertions in `onComplete` block, not after subscribe call
    - Example:
      ```groovy
      channel.groupTupleBy { it.key }
          .subscribe(
              onNext: { grouped -> /* process */ },
              onComplete: { /* verify assertions here */ }
          )
      Thread.sleep(2000)  // Wait for async processing
      ```

11. **Plugin Import Requirements**:
    - ❌ Workflow config alone is NOT enough: `plugins { id 'nf-bids@...' }`
    - ✅ MUST include operators explicitly in workflow scripts:
      ```groovy
      include { groupTupleBy } from 'plugin/nf-bids'
      include { joinBy } from 'plugin/nf-bids'
      include { combineBy } from 'plugin/nf-bids'
      ```
    - Forgetting include causes "Missing process or function" errors

12. **Operator Output Structures**:
    - `groupTupleBy`: emits `[key, [items]]` (2 elements)
    - `joinBy`: emits `[leftItem, rightItem]` (2 elements, NO key)
      - ❌ WRONG: `joined[1]`, `joined[2]` (assumes key is first)
      - ✅ CORRECT: `joined[0]`, `joined[1]` (left and right items)
        - `combineBy`: emits `[key, leftItem, rightItem]` tuples using key extractors
            - ❌ WRONG: `combineBy(right) { left, right -> predicate }` (filter predicate)
            - ✅ CORRECT: `combineBy(right, { left -> left.key }, { right -> right.key })` - use key extractors, then optionally `.filter { key, left, right -> predicate }`

13. **Dataset Sizing for Tests**:
    - Small (100 items): Logic verification, fast feedback
    - Medium (1-10k items): Performance validation, realistic load
    - Large (50k+ items): Stress testing, use sparingly (long run times)
    - Balance: 10k items is sweet spot for edge case testing

14. **Background Test Execution**:
    - ❌ NEVER inject commands in active terminal (gets prefixed with CTRL+C)
    - ✅ Use `nohup command > log 2>&1 &` for long-running tests
    - ✅ Check status with `cat log` after appropriate wait time
    - Example:
      ```bash
      nohup ./run_all_tests.sh > test_output.log 2>&1 &
      sleep 150  # Wait for tests
      cat test_output.log  # Check results
      ```

15. **Groovy Null-Safe Field Access**:
    - ✅ Use Elvis operator for defaults: `item.field ?: "default"`
    - ✅ Safe navigation: `item?.nested?.field` (no NPE)
    - ✅ Works in closures: `{ it.subject ?: "unknown" }`
    - Gracefully handles missing fields in complex structures

**Essential Commands**:
```bash
make install              # Install plugin to Nextflow
./gradlew test            # Run unit tests
nextflow run validation/  # Run integration tests
./gradlew tasks           # Show all available tasks
```

**Next Steps**:
- Review `docs/development.md` for coding guidelines
- Check `docs/architecture.md` for component overview
- Explore `validation/` for integration test examples
- Read Nextflow plugin docs: https://www.nextflow.io/docs/latest/plugins.html

---

**Document Version**: 1.0  
**Last Updated**: November 2025  
**Maintained By**: nf-bids development team
