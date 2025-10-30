# 🚀 Quickstart Guide

Get up and running with nf-bids in 5 minutes.

---

## ⚡ 5-Minute Setup

### 1️⃣ Prerequisites

```bash
# Check Java (11+)
java -version

# Check Nextflow (25.10.0+)
nextflow -version

# Install Nextflow if needed
curl -s https://get.nextflow.io | bash
sudo mv nextflow /usr/local/bin/
```

### 2️⃣ Clone Repository

```bash
git clone https://github.com/AlexVCaron/bids2nf.git
cd bids2nf/plugins/nf-bids
```

### 3️⃣ Build & Install

```bash
# Build plugin
./gradlew assemble

# Install to Nextflow
make install
# or manually:
# mkdir -p ~/.nextflow/plugins
# cp -r build/plugins/nf-bids-0.1.0 ~/.nextflow/plugins/
```

### 4️⃣ Verify Installation

```bash
# Check installation
ls ~/.nextflow/plugins/nf-bids-0.1.0/

# Should show:
# - nf-bids-0.1.0.jar
# - META-INF/
```

### 5️⃣ Run Test

```bash
cd validation/
nextflow run test.nf --bids_dir ../tests/data/custom/ds-dwi
```

**Success!** 🎉 You should see channels being created.

---

## 📝 First Workflow

Create `my-workflow.nf`:

```groovy
#!/usr/bin/env nextflow

nextflow.enable.dsl = 2

// Include BIDS plugin
plugins {
    id 'nf-bids@0.1.0'
}

workflow {
    // Create BIDS channel
    bids_ch = Channel.fromBIDS(
        bidsDir: params.bids_dir,
        configFile: params.config
    )
    
    // Use the channel
    bids_ch.view { item ->
        def key = item[0]
        def data = item[1]
        "Set: ${key}, Files: ${data.files.size()}"
    }
}
```

Create `bids2nf.yaml`:

```yaml
dataset:
  name: "my-dataset"
  entities:
    - sub
    - ses
    
outputs:
  dwi:
    type: plain_set
    suffixes:
      - dwi
    loop_over:
      - ses
```

Run it:

```bash
nextflow run my-workflow.nf \
    --bids_dir /path/to/bids/dataset \
    --config bids2nf.yaml
```

---

## 🧪 Run Tests

```bash
# All tests
./gradlew test

# View results
open build/reports/tests/test/index.html

# Specific test
./gradlew test --tests BidsFileTest

# Clean and test
./gradlew cleanTest test
```

**Expected**: 29 tests passing ✅

---

## 🔨 Make Changes

### Edit Code

```bash
# 1. Edit source
vim src/main/groovy/nfneuro/plugin/model/BidsFile.groovy

# 2. Run tests
./gradlew test --tests BidsFileTest

# 3. Rebuild & install
./gradlew assemble
make install

# 4. Test with Nextflow
cd validation/
nextflow run test.nf
```

### Quick Iteration

```bash
# One-liner for rapid testing
make clean && ./gradlew test && make install && \
cd validation && nextflow run test.nf && cd ..
```

---

## 📚 Common Tasks

### Build Commands

```bash
./gradlew assemble       # Build plugin
./gradlew test          # Run tests
./gradlew clean         # Clean build
make install            # Install to Nextflow
make clean              # Clean + uninstall
```

### Testing Commands

```bash
# Unit tests
./gradlew test

# Integration test
cd validation/
nextflow run main.nf --bids_dir <path>

# Simple channel test
nextflow run test_simple.nf

# Clean cache
nextflow clean -f
```

### Debug Commands

```bash
# Gradle debug
./gradlew test --debug

# Nextflow debug
nextflow -log .nextflow.log run workflow.nf
tail -f .nextflow.log
```

---

## 🐞 Troubleshooting

### Plugin Not Found

```bash
# Reinstall
make clean install

# Verify
ls ~/.nextflow/plugins/nf-bids-0.1.0/
```

### Tests Fail

```bash
# Clean and retest
./gradlew cleanTest test

# Check Java version
java -version  # Should be 11+
```

### Build Errors

```bash
# Clean everything
./gradlew clean
rm -rf build/

# Rebuild
./gradlew assemble
```

### Nextflow Errors

```bash
# Clean Nextflow cache
nextflow clean -f

# Clear work directory
rm -rf work/

# Reinstall plugin
make install
```

---

## 🎯 Next Steps

1. **Read the docs**:
   - [README.md](README.md) - Complete overview
   - [CONTRIBUTING.md](CONTRIBUTING.md) - How to contribute
   - [docs/development.md](docs/development.md) - Development guide
   - [docs/configuration.md](docs/configuration.md) - YAML config reference

2. **Explore examples**:
   - `validation/main.nf` - Full integration test
   - `validation/test_simple.nf` - Simple channel test
   - `tests/data/custom/` - Test datasets

3. **Run integration tests**:
   ```bash
   cd validation/
   nextflow run main.nf --bids_dir ../tests/data/custom/ds-dwi
   ```

4. **Try different configurations**:
   - Plain sets (1:1 file mapping)
   - Named sets (grouped by entities)
   - Sequential sets (ordered arrays)
   - Mixed sets (nested structures)

5. **Check out the code**:
   - `src/main/groovy/nfneuro/plugin/` - Main source
   - `src/test/groovy/nfneuro/plugin/` - Test suite

---

## 💡 Quick Tips

### Fast Build

```bash
# Skip tests for quick builds
./gradlew assemble -x test
```

### Watch Mode

```bash
# Auto-rebuild on changes (requires entr)
ls src/**/*.groovy | entr -r ./gradlew assemble
```

### IDE Setup

**IntelliJ IDEA** (recommended):
1. Open project
2. Import as Gradle project
3. Set JDK to 11+
4. Enable Groovy support

**VS Code**:
1. Install "Groovy" extension
2. Install "Gradle for Java" extension
3. Open folder

---

## 📞 Get Help

- **Documentation**: [docs/](docs/)
- **Issues**: [GitHub Issues](https://github.com/AlexVCaron/bids2nf/issues)
- **Tests**: Check `src/test/` for examples
- **Examples**: Check `validation/` for working workflows

---

**Happy coding! 🚀**

Now go build amazing BIDS workflows!
