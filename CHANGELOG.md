# Changelog

All notable changes and development history for the nf-bids plugin.

---

## [Unreleased] 0.1.0-beta.9

### ⚠️ BREAKING CHANGES

#### Flat BIDS Output Format

The `Channel.fromBIDS` output structure has been **completely redesigned** for improved usability and type safety.

**Old Format (Nested):**
```groovy
[
  [subject, session, run],  // Grouping key tuple
  [
    data: [T1w: [nii: 'path/to/file.nii.gz', json: 'path/to/file.json']],
    filePaths: ['path/to/file.nii.gz', 'path/to/file.json'],
    bidsParentDir: '/dataset'
  ]
]

// Usage (cumbersome):
def subject = item[0][0]
def t1w = file("${item[1].bidsParentDir}/${item[1].data.T1w.nii}")
```

**New Format (Flat):**
```groovy
[
  meta: [subject: 'sub-01', session: 'ses-01', run: 'NA'],
  T1w: [nii: Path('/dataset/sub-01/ses-01/anat/sub-01_ses-01_T1w.nii.gz'),
        json: Path('/dataset/sub-01/ses-01/anat/sub-01_ses-01_T1w.json')],
  dwi: [ap: [nii: Path('...'), bval: Path('...'), bvec: Path('...')],
        pa: [nii: Path('...'), bval: Path('...'), bvec: Path('...')]]
]

// Usage (clean and direct):
def subject = item.meta.subject
def t1w = item.T1w.nii  // Already an absolute Path - ready for process inputs!
```

**Key Changes:**
- ✅ **Flattened structure:** All data at top level alongside `meta`
- ✅ **Absolute Paths:** All file paths are `java.nio.file.Path` objects (not strings)
- ✅ **Direct access:** No more nested `data` object or path concatenation
- ✅ **Named entities:** `meta.subject`, `meta.session`, etc. instead of tuple indices
- ✅ **Config keys preserved:** Output uses config keys (e.g., `dwi_ap`) not file suffixes (e.g., `dwi`)

**Opt-out Available:**
```groovy
// Use legacy format
Channel.fromBIDS(bidsDir, config, [flatten_output: false])
```

**Migration Required:** All workflows must be updated. See [Migration Guide](#flat-output-migration) below.

#### Suffix Mapping & Config Key Fixes

Fixed fundamental bugs in how suffix mapping and config keys are handled. This enables **heterogeneous datasets** where multiple configs use the same file suffix.

**What Was Fixed:**
1. **Mapping direction corrected:** Now maps `configKey → fileSuffix` (was backwards)
2. **Output keys fixed:** Channel output uses config keys, not file suffixes
3. **Multiple configs per suffix:** Multiple configs can now map to same `suffix_maps_to`
4. **Empty set detection:** Fixed falsy check that ignored empty config maps

**Example - Heterogeneous DWI:**
```yaml
# Different subjects can have different phase-encoding schemes
dwi:
  plain_set:
    exclude_entities: [direction]  # Only files WITHOUT direction
  additional_extensions: [bvec, bval]

dwi_ap:  # Files WITH dir-AP/dir-PA
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  suffix_maps_to: "dwi"  # Maps to same suffix

dwi_rl:  # Files WITH dir-RL/dir-LR  
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  suffix_maps_to: "dwi"  # No collision!
```

**Output:**
```groovy
// Subject with AP/PA phase encoding
[meta: [...], dwi_ap: [ap: [...], pa: [...]]]  // Config key, not "dwi"

// Subject with RL/LR phase encoding  
[meta: [...], dwi_rl: [rl: [...], lr: [...]]]  // Different config key
```

**Critical Pattern:** When using `suffix_maps_to`, plain configs MUST exclude distinguishing entities to prevent files matching multiple configs.

### Added
- Flat output structure in `BidsHandler.flattenTupleToMap()`
- `flatten_output` option (default: `true`) for backward compatibility
- `exclude_entities` validation to prevent double-matching
- Comprehensive heterogeneous dataset test suite
- New test workflow: `validation/main_flat.nf`
- Test dataset: `validation/data/custom/ds-dwi4` with heterogeneous data
- Test suite: `validation/test_heterogeneous_suffix_mapping_flat.nf.test`

### Changed
- **SuffixMapper:** Inverted mapping to `configKey → targetSuffix`
- **SuffixMapper.resolveConfigKeys():** Now returns `List<String>` of all matching configs
- **BaseSetHandler.findMatchingGrouping():** Tries all candidate config keys
- **All handlers:** Updated to use `configKey` in output structure
- **BaseSetHandler.getSetType():** Uses `containsKey()` instead of truthiness
- **BidsConfigLoader:** Validates against reserved `meta` key name

### Fixed
- **Critical:** Suffix mapping direction (was backwards)
- **Critical:** Output keys now use config keys, not file suffixes  
- **Critical:** Empty plain sets now detected correctly
- **Critical:** Multiple configs can now share same `suffix_maps_to`
- File path compatibility with Nextflow process `path` inputs
- Type safety: All paths are `java.nio.file.Path` objects

### Performance
- No regressions: All 78 unit tests passing
- All 25+ integration tests passing
- Comprehensive heterogeneous dataset tests (4/4 passing)

---

## [Unreleased] 0.1.0-beta.5

### ⚠️ BREAKING CHANGES

#### Java File to Path Conversion

Channel outputs now use `java.nio.file.Path` instead of `java.io.File` for all file paths to ensure compatibility with Nextflow process `path` inputs and support for remote file systems.

**Impact:**
- ✅ **Fixed:** Processes can now consume plugin output with `path` inputs (previously failed with "Unexpected path value: [java.io.File]")
- ✅ **Improved:** Full support for cloud storage (S3, GCS, Azure Blob Storage)
- ✅ **Enhanced:** Richer Path API for file operations

**What Changed:**
- All file paths in channel output are now `java.nio.file.Path` objects
- Uses `FileHelper.asPath()` for robust path handling
- Supports local files, URIs (s3://, gs://, az://), and relative paths

**Migration:** 
Most users won't need to change anything - Nextflow processes automatically handle Path objects. If you were using File-specific methods:
```groovy
// Old: def file = item.T1w; file.listFiles()
// New: def path = item.T1w; path.toFile().listFiles()  // or Files.list(path)
```

### Fixed
- **Critical:** File path compatibility with Nextflow process path inputs
- Channel outputs now emit Path objects compatible with process staging

### ⚠️ BREAKING CHANGES

#### combineBy Operator Redesign

The `combineBy` operator has been completely redesigned to use **key extraction** instead of **filter predicates**, aligning with `groupTupleBy` and `joinBy` patterns.

**Old API (0.1.0-beta.4):**
```nextflow
subjects.combineBy(sessions) { subj, sess ->
    subj.id == sess.subject  // Filter predicate
}
.view { subj, sess -> "${subj} with ${sess}" }  // 2 elements
```

**New API (0.1.0-beta.5+):**
```nextflow
subjects.combineBy(
    sessions,
    { it.id },      // Left key extractor
    { it.subject }  // Right key extractor
)
.view { key, subj, sess -> "${key}: ${subj} with ${sess}" }  // 3 elements
```

**Changes:**
- ❌ Removed: Filter predicate parameter
- ✅ Added: Dual key extractors (left and right)
- ✅ Changed: Output from `[left, right]` to `[key, left, right]`
- ✅ Feature: Cartesian product within matching key groups
- ✅ Consistency: Aligns with Nextflow's `combine(by:)` operator

**Migration Required:** All existing `combineBy` usage must be updated. See the [Closure-Based Channel Operators](https://nf-neuro.github.io/nf-bids/concepts/channel-operators.html) chapter for the migration guide.

### Added
- Key-based combination logic in `CombineByOp.groovy`
- Support for different key extractors for left/right channels
- Cartesian product generation within matching key groups
- Comprehensive test suite for new combineBy behavior
- Research documentation for Nextflow's `combine(by:)` operator
- API design specification for combineBy redesign

### Changed
- `CombineByOp`: Replaced `List` buffers with `Map<Object, List>` for key-based storage
- `BidsExtension.combineBy()`: Updated signature to accept dual key extractors
- Documentation: Complete rewrite of combineBy section in channel-operators.md
- Tests: Updated `test_combineby.nf` with 8 new test cases
- Benchmark: Added `benchmark_combineby_new.nf` for performance validation

### Fixed
- Operator consistency: All three operators (`groupTupleBy`, `joinBy`, `combineBy`) now use key extraction
- Output structure: `combineBy` now includes key in output (matches `joinBy` pattern)

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

See the [BIDS Migration Guide](https://nf-neuro.github.io/nf-bids/appendices/migration-bids.html) for instructions on migrating from the original bids2nf implementation.

---

## Known Issues

None! All validation datasets pass with 100% baseline alignment. 🎉

If you encounter any issues with your specific datasets, please report them via GitHub issues.

---

## Contributors

- Development team following best practices from Nextflow plugin ecosystem
- Testing validated against bids-examples repository
- Community feedback incorporated throughout development
