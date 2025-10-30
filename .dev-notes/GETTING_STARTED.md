# Getting Started - nf-bids Plugin

## Quick Start (5 minutes)

### 1. Navigate to Plugin Directory

```bash
cd /home/local/USHERBROOKE/vala2004/dev/bids2nf/plugins/nf-bids
```

### 2. Run Quick Test

```bash
./quick-test.sh
```

This will:
- ✓ Check prerequisites (Java, Nextflow)
- ✓ Setup Gradle wrapper if needed
- ✓ Build the plugin
- ✓ Run tests
- ✓ Install to local Maven

**Expected result:** Build succeeds, tests show expected failures (stubs).

### 3. Verify Installation

```bash
# Check JAR was created
ls -lh build/libs/nf-bids-0.1.0.jar

# Check Maven installation
ls -lh ~/.m2/repository/io/github/alexvcaron/nf-bids/0.1.0/
```

---

## What Just Happened?

### Build Process

1. **Compilation**: Groovy classes compiled to bytecode
2. **Packaging**: JAR created with plugin manifest
3. **Testing**: Unit tests executed (will fail - stubs not implemented)
4. **Publishing**: JAR installed to local Maven repository

### Current Status

- ✅ **Plugin Structure**: Complete
- ✅ **Build System**: Working
- ✅ **Class Scaffolding**: Done
- ⚠️ **Implementation**: Methods are stubs (need implementation)
- ⚠️ **Tests**: Fail as expected (no implementation yet)

---

## Next Steps

### Option A: Manual Testing (Recommended First)

Test that the build system works:

```bash
cd /home/local/USHERBROOKE/vala2004/dev/bids2nf/plugins/nf-bids

# Clean build
./gradlew clean

# Build
./gradlew build

# Check output
echo "Build status: $?"
ls -lh build/libs/
```

### Option B: Start Implementation

Follow the implementation guide:

```bash
# Read the guide
cat IMPLEMENTATION_GUIDE.md

# Start with Phase 1: Core Parsing
vim src/main/groovy/nextflow/bids/parser/LibBidsShWrapper.groovy
```

Implement methods in this order:
1. `LibBidsShWrapper.parseBidsToCSV()`
2. `BidsParser.parse()`
3. `BidsConfigLoader.load()`
4. `BidsConfigAnalyzer.analyzeConfiguration()`

### Option C: Explore the Codebase

```bash
# View plugin structure
tree src/main/groovy/nextflow/bids/

# Read class documentation
less src/main/groovy/nextflow/bids/channel/BidsChannelFactory.groovy

# Check reference links
grep -r "@reference" src/main/groovy/nextflow/bids/ | head -10
```

---

## Development Workflow

### Make Changes → Build → Test

```bash
# 1. Edit a file
vim src/main/groovy/nextflow/bids/parser/BidsParser.groovy

# 2. Build
./gradlew build

# 3. Run specific test
./gradlew test --tests BidsParserTest

# 4. If successful, install
./gradlew publishToMavenLocal
```

### Watch Mode (Optional)

```bash
# Terminal 1: Auto-rebuild on changes
./gradlew build --continuous

# Terminal 2: Make edits
vim src/main/groovy/nextflow/bids/...
```

---

## Common Issues

### Issue: "gradlew: command not found"

**Solution**: Setup Gradle wrapper first:

```bash
./setup-gradle.sh
```

Or use system Gradle:

```bash
gradle wrapper --gradle-version 8.4
```

### Issue: Build fails with compilation errors

**Expected**: This is normal - scaffolding has stub methods that may have compilation issues.

**Solution**: Check the error and fix syntax:

```bash
# View detailed error
./gradlew build --stacktrace

# Common fixes:
# - Add missing imports
# - Fix method signatures
# - Implement abstract methods
```

### Issue: "Could not resolve dependencies"

**Solution**: Check internet connection and refresh:

```bash
./gradlew build --refresh-dependencies
```

---

## Understanding the Build Output

### Successful Build

```
BUILD SUCCESSFUL in 5s
7 actionable tasks: 7 executed
```

**What it means:**
- ✅ All Groovy files compiled
- ✅ JAR created
- ✅ Tests ran (may have failed - that's OK)
- ✅ Ready for Maven install

### Failed Build

```
FAILURE: Build failed with an exception.
```

**What to check:**
1. Compilation errors in Groovy files
2. Missing dependencies
3. Syntax errors
4. Check: `/tmp/nf-bids-build.log`

---

## Testing Without Full Implementation

You can test individual components:

```bash
# Test configuration loading
./gradlew test --tests BidsConfigLoaderTest

# Test specific class
./gradlew test --tests BidsParserTest

# Run all tests (will have failures)
./gradlew test

# View test report
xdg-open build/reports/tests/test/index.html
```

---

## File Locations

After successful build:

```
plugins/nf-bids/
├── build/
│   ├── libs/
│   │   └── nf-bids-0.1.0.jar          # Plugin JAR
│   ├── classes/                        # Compiled classes
│   ├── reports/
│   │   └── tests/test/index.html      # Test report
│   └── test-results/                   # Test XML results
│
└── ~/.m2/repository/
    └── io/github/alexvcaron/nf-bids/0.1.0/
        └── nf-bids-0.1.0.jar          # Maven artifact
```

---

## Quick Reference

### Build Commands

```bash
./gradlew clean          # Clean build directory
./gradlew build          # Compile and package
./gradlew test           # Run tests
./gradlew jar            # Create JAR only
./gradlew publishToMavenLocal  # Install to Maven
```

### Development Commands

```bash
./quick-test.sh          # Full build and test
./setup-gradle.sh        # Setup Gradle wrapper
./gradlew tasks          # List all tasks
./gradlew dependencies   # Show dependencies
```

### Useful Flags

```bash
--stacktrace             # Show full error trace
--info                   # Verbose output
--refresh-dependencies   # Force dependency refresh
--continuous             # Watch mode
--tests <TestClass>      # Run specific test
```

---

## Ready to Implement?

### Phase 1 Checklist

- [ ] Read `IMPLEMENTATION_GUIDE.md`
- [ ] Study reference code in original `bids2nf`
- [ ] Implement `LibBidsShWrapper.parseBidsToCSV()`
- [ ] Test: `./gradlew test --tests LibBidsShWrapperTest`
- [ ] Implement `BidsParser.parse()`
- [ ] Test: `./gradlew test --tests BidsParserTest`
- [ ] Continue with remaining classes...

### Resources

- **INSTALL.md** - Detailed installation guide
- **IMPLEMENTATION_GUIDE.md** - Step-by-step implementation
- **ARCHITECTURE.md** - Architecture diagrams
- **README.md** - Plugin overview
- **Original Code** - Reference links in each class

---

## Success Criteria

You're ready to move forward when:

- ✅ `./gradlew build` succeeds
- ✅ JAR file exists
- ✅ Maven artifact installed
- ✅ No compilation errors
- ✅ Ready to implement methods

**Current Status**: ✅ Build system ready, ⚠️ Implementation needed

---

## Need Help?

1. Check **INSTALL.md** for detailed troubleshooting
2. View build logs: `/tmp/nf-bids-build.log`
3. View test logs: `/tmp/nf-bids-test.log`
4. Check Gradle docs: https://gradle.org/
5. Review original code: https://github.com/AlexVCaron/bids2nf

**Next recommended action:** Run `./quick-test.sh` to verify build system
