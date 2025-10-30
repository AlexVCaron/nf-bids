# Changelog

All notable changes and development history for the nf-bids plugin.

---

## [0.2.0] - 2025-10-29 🎉 100% BASELINE ALIGNMENT

### 🏆 Achievement: Production Ready

The plugin now produces **identical outputs** to the original bids2nf codebase across all 18 test datasets.

**Progress**: 67% → 72% → 83% → 94% → **100%** ✅

### Added
- Entity normalization between long/short entity names (inversion↔inv, reconstruction↔rec)
- `exclude_entities` configuration option for filtering files by entity presence
- `sbref_fullreverse` named set configuration for paired reference images
- Comprehensive validation test suite with 18 datasets

### Fixed
- **Entity Normalization** (16→17/18): Long entity names from config now properly mapped to short names for file matching
- **JSON Parts Grouping** (qmri_mp2rage): JSON files now correctly collected when `getEntity('part')` returns "NA" string
- **Exclude Entities** (17→18/18): Implemented file filtering to exclude specific entity variants (e.g., rec-dis2d)
- **Hierarchical Sequential Sets** (qmri_mpm, qmri_sa2rage): Fixed multi-level sequencing (flip→echo, flip→inversion)
- **Events.tsv Collection** (ds-mrs_fmrs): Added events suffix support for MRS datasets
- **Mixed Set Emission** (qmri_mpm): Fixed entity value matching to strip prefixes (mt-on→on, flip-1→1)
- **Sequential Set Structure** (ds-dwi3, ds-dwi4): Added proper epi_fullreverse and dwi_fullreverse configurations

### Changed
- Consolidated documentation into STATUS.md and CHANGELOG.md
- Updated test suite to reflect 100% baseline alignment
- Cleaned up temporary development documentation files

---

## [0.1.0] - 2024-12-XX - Initial Plugin Implementation

### Added
- Core plugin infrastructure using Nextflow plugin API
- `Channel.fromBIDS()` channel factory with `@Factory` annotation
- Async execution pattern using `session.addIgniter()`
- BIDS dataset parsing and entity extraction
- Configuration file support (YAML)
- File grouping handlers:
  - PlainSetHandler - Basic file-per-suffix grouping
  - NamedSetHandler - Entity-based dimension grouping
  - SequentialSetHandler - Ordered arrays by entity
  - MixedSetHandler - Nested grouping + sequencing
- Configuration validation system
- Comprehensive test suite infrastructure

### Development History

#### Phase 1: Core Implementation
- Implemented BidsExtension with @Factory annotation
- Created BidsHandler for async execution
- Built BidsFile model with entity extraction
- Implemented ConfigValidator for configuration validation

#### Phase 2: File Grouping
- Created PlainSetHandler for simple suffix grouping
- Implemented NamedSetHandler for entity-based dimensions
- Built SequentialSetHandler for ordered sequences
- Added MixedSetHandler for complex nested structures

#### Phase 3: Testing Infrastructure
- Created baseline test suite (12 datasets)
- Added custom dataset validation (6 datasets)
- Implemented nf-test integration
- Built snapshot comparison tooling

#### Phase 4: Baseline Alignment
See version 0.2.0 above for detailed fixes.

### Technical Details

**Architecture**:
- Plugin entry point: `BidsExtension.groovy`
- Async processing: `BidsHandler.groovy`
- File model: `BidsFile.groovy`
- Grouping logic: `*SetHandler.groovy` classes
- Configuration: `ConfigValidator.groovy`

**Key Design Decisions**:
- Used `@Factory` annotation for channel factory registration
- Implemented async execution to prevent workflow hanging
- Created modular handler system for different set types
- Built comprehensive validation for early error detection

---

## Development Timeline

### October 2025 - Baseline Alignment Sprint
- **Oct 25**: Investigation of @Factory channel registration pattern
- **Oct 26**: Created 18-dataset validation test suite
- **Oct 27**: Built comparison infrastructure and tooling
- **Oct 28**: Fixed hierarchical structure and cross-modal inclusion (13→15/18)
- **Oct 29 AM**: Fixed epi_fullreverse validation (15→16/18)
- **Oct 29 PM**: Fixed entity normalization (16→17/18)
- **Oct 29 PM**: Fixed JSON parts grouping and exclude_entities (17→18/18) 🎉

### December 2024 - Initial Development
- Core plugin infrastructure
- BIDS parsing and entity extraction
- File grouping handlers
- Configuration system
- Test infrastructure

---

## Migration from Baseline

See [docs/MIGRATION_GUIDE.md](docs/MIGRATION_GUIDE.md) for instructions on migrating from the original bids2nf implementation.

---

## Known Issues

None! All validation datasets pass with 100% baseline alignment. 🎉

If you encounter any issues with your specific datasets, please report them via GitHub issues.

---

## Contributors

- Development team following best practices from Nextflow plugin ecosystem
- Testing validated against bids-examples repository
- Community feedback incorporated throughout development
