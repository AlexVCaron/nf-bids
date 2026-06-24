# Quick Reference - nf-bids Plugin Documentation

Last Updated: October 24, 2024

## Documentation Structure

```
nf-bids/
├── README.md                          # ← START HERE
├── docs/                              # Detailed documentation
│   ├── installation.md                # Plugin installation
│   ├── configuration.md               # YAML configuration
│   ├── api.md                         # API reference
│   ├── examples.md                    # Usage examples
│   ├── development.md                 # Developer setup
│   ├── architecture.md                # Architecture guide
│   ├── implementation.md              # Implementation roadmap
│   └── testing.md                     # Testing guide
└── .dev-notes/                        # Archived notes
    ├── DOCUMENTATION_REORGANIZATION.md
    ├── OVERVIEW.txt
    ├── START_HERE.md
    ├── SCAFFOLDING_SUMMARY.md
    ├── GETTING_STARTED.md
    └── README-old.md
```

## For End-Users (Pipeline Developers)

**Getting Started:**
1. 📖 [README.md](../README.md) - Overview and quick start
2. 🔧 [docs/installation.md](../docs/installation.md) - Install plugin
3. ⚙️ [docs/configuration.md](../docs/configuration.md) - Configure YAML
4. 💡 [docs/examples.md](../docs/examples.md) - Copy examples
5. 📚 [docs/api.md](../docs/api.md) - API reference

**Quick Links:**
- Install plugin: `docs/installation.md`
- Basic usage: `README.md#basic-usage`
- Configuration: `docs/configuration.md`
- Examples: `docs/examples.md`
- API docs: `docs/api.md`

## For Plugin Developers

**Getting Started:**
1. 📖 [README.md](../README.md) - Project overview
2. 🛠️ [docs/development.md](../docs/development.md) - Setup environment
3. 🏗️ [docs/architecture.md](../docs/architecture.md) - Understand structure
4. 📋 [docs/implementation.md](../docs/implementation.md) - Implementation steps
5. 🧪 [docs/testing.md](../docs/testing.md) - Write tests

**Quick Links:**
- Setup dev env: `docs/development.md#development-setup`
- Architecture: `docs/architecture.md`
- Implementation roadmap: `docs/implementation.md`
- Testing guide: `docs/testing.md`

## Common Tasks

### Use the Plugin

```groovy
// In your nextflow.config
plugins {
    id 'nf-bids@0.1.0'
}

// In your workflow
Channel.fromBIDS(params.bids_dir, params.config)
```

See: `docs/examples.md`

### Configure BIDS Channel

```yaml
# config.yaml
loop_over:
  - subject
  - session

T1w:
  plain_set:
    entities:
      suffix: T1w
```

See: `docs/configuration.md`

### Build the Plugin

```bash
./setup.sh           # First time setup
./quick-test.sh      # Build, test, install
```

See: `docs/development.md#building-the-plugin`

### Run Tests

```bash
./gradlew test                    # Unit tests
cd validation && nextflow run test.nf  # Integration tests
```

See: `docs/testing.md`

### Implement a Feature

1. Read: `docs/implementation.md` - Find the feature
2. Check: `@reference` tags in Groovy files
3. Write: Implementation based on original code
4. Test: Add unit + integration tests
5. Document: Update relevant docs

See: `docs/development.md#implementation-roadmap`

## Documentation Index

### End-User Docs

| Document | Purpose | Key Topics |
|----------|---------|------------|
| [README.md](../README.md) | Quick start | Installation, basic usage, features |
| [installation.md](../docs/installation.md) | Setup guide | Requirements, plugin installation |
| [configuration.md](../docs/configuration.md) | YAML config | Set types, entities, broadcasting |
| [api.md](../docs/api.md) | API reference | Channel.fromBIDS(), parameters, output |
| [examples.md](../docs/examples.md) | Usage examples | T1w, fMRI, DWI, multi-modal |

### Developer Docs

| Document | Purpose | Key Topics |
|----------|---------|------------|
| [development.md](../docs/development.md) | Dev guide | Setup, building, contributing |
| [architecture.md](../docs/architecture.md) | Architecture | Plugin structure, components, flow |
| [implementation.md](../docs/implementation.md) | Implementation | Roadmap, phases, steps |
| [testing.md](../docs/testing.md) | Testing | Unit, integration, validation |

### Archived Docs

| Document | Purpose |
|----------|---------|
| DOCUMENTATION_REORGANIZATION.md | Reorganization summary |
| OVERVIEW.txt | Original project overview |
| START_HERE.md | Original orientation |
| SCAFFOLDING_SUMMARY.md | Scaffolding creation log |
| GETTING_STARTED.md | Original getting started |
| README-old.md | Original README |

## Search Tips

**Find configuration examples:**
```bash
grep -r "loop_over" docs/
```

**Find API usage:**
```bash
grep -r "Channel.fromBIDS" docs/
```

**Find implementation steps:**
```bash
grep -r "Phase [0-9]" docs/
```

## Quick Command Reference

```bash
# Build and test
./setup.sh                  # Initial setup
./quick-test.sh             # Build, test, install
./gradlew clean build       # Clean build
./gradlew test              # Run unit tests

# Development
./gradlew compileGroovy     # Compile only
./gradlew publishToMavenLocal  # Install locally

# Testing
cd validation
nextflow run test.nf -plugins nf-bids@0.1.0

# Documentation
cd docs
grep -r "keyword" .         # Search docs
```

## Need Help?

- **Installation issues**: See `docs/installation.md`
- **Configuration errors**: See `docs/configuration.md`
- **API questions**: See `docs/api.md`
- **Development setup**: See `docs/development.md`
- **Architecture questions**: See `docs/architecture.md`
- **Testing issues**: See `docs/testing.md`

## Status

- ✅ Plugin scaffolding complete
- ✅ Documentation reorganized
- ⏭️ Implementation in progress (see `docs/implementation.md`)
- ⏭️ Testing framework ready

---

**For the latest updates, always start with [README.md](../README.md)**
