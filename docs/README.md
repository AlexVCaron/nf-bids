# nf-bids Documentation

Complete documentation for the nf-bids Nextflow plugin.

---

## Quick Start

- **[QUICKSTART.md](../QUICKSTART.md)** - Get up and running in 5 minutes
- **[README.md](../README.md)** - Project overview and features
- **[CONTRIBUTING.md](../CONTRIBUTING.md)** - How to contribute

---

## User Documentation

### Getting Started
- **[installation.md](installation.md)** - Installation guide
- **[configuration.md](configuration.md)** - YAML configuration reference
- **[examples.md](examples.md)** - Usage examples

### API Reference
- **[api.md](api.md)** - Complete API documentation
  - Channel.fromBIDS() method
  - Data structures
  - Configuration options

---

## Developer Documentation

### Development
- **[development.md](development.md)** - Development setup and workflow
  - Environment setup
  - Build process
  - Testing workflow
  
- **[architecture.md](architecture.md)** - Plugin architecture
  - Component diagram
  - Data flow
  - Package structure

### Implementation
- **[implementation.md](implementation.md)** - Implementation reference
  - Component mapping to original code
  - Reference patterns
  
- **[ASYNC_MIGRATION.md](ASYNC_MIGRATION.md)** - Async execution migration
  - Migration details
  - Pattern changes
  - Type-checking fixes

### Testing
- **[testing.md](testing.md)** - Testing strategies
  - Unit testing
  - Integration testing
  - Test data

- **[TEST_SUITE.md](TEST_SUITE.md)** - Test suite documentation
  - Test coverage
  - Running tests
  - Test philosophy

---

## Project Status

- **[TODO.md](TODO.md)** - Current status and priorities (concise)
- **[NEXT_STEPS.md](NEXT_STEPS.md)** - Detailed roadmap
  - Comprehensive testing plan
  - Feature enhancements
  - Performance optimization

---

## Documentation Map

```
User Journey:
├─ Want to use plugin?
│  ├─ Start: QUICKSTART.md (5 min)
│  ├─ Configure: configuration.md
│  ├─ Examples: examples.md
│  └─ API: api.md
│
├─ Want to contribute?
│  ├─ Start: CONTRIBUTING.md
│  ├─ Setup: development.md
│  ├─ Architecture: architecture.md
│  └─ Tests: TEST_SUITE.md
│
└─ Want to understand internals?
   ├─ Architecture: architecture.md
   ├─ Implementation: implementation.md
   ├─ Async Pattern: ASYNC_MIGRATION.md
   └─ Testing: testing.md
```

---

## Quick Reference

### Build & Test
```bash
./gradlew test                    # Unit tests (29 tests)
cd validation && nextflow run main.nf  # Integration test
```

### Install
```bash
make assemble                     # Build plugin
make install                      # Install to Nextflow
```

### Documentation Updates

When updating documentation:
1. Keep docs concise and cross-linked
2. Update this index if adding new docs
3. Maintain consistent structure
4. Link related documents

---

**Plugin Status**: ✅ Fully Functional (29/29 tests passing)  
**Last Updated**: December 2024
