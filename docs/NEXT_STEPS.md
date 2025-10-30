# Next Steps for nf-bids Plugin

**Current Status**: ✅ Plugin Fully Functional  
**Date**: December 2024

See [TODO.md](TODO.md) for current component status.

---

## Priority 1: Comprehensive Testing 🧪

**Goal**: Validate plugin with diverse BIDS datasets

### Test Coverage Expansion

**Available Test Datasets** (in `tests/data/bids-examples/`):
- [ ] asl001, asl002 - Arterial Spin Labeling
- [ ] ds-mrs_fmrs - MR Spectroscopy  
- [ ] ds-mtsat - Magnetization Transfer
- [ ] eeg_cbm - EEG data
- [ ] qmri_* datasets - Quantitative MRI variants
  - [ ] qmri_irt1 - Inversion Recovery T1
  - [ ] qmri_megre - Multi-Echo Gradient Echo
  - [ ] qmri_mese - Multi-Echo Spin Echo
  - [ ] qmri_mp2rage - MP2RAGE
  - [ ] qmri_mpm - Multi-Parametric Mapping
  - [ ] qmri_mtsat - MT Saturation
  - [ ] qmri_sa2rage - SA2RAGE
  - [ ] qmri_tb1tfl - B1+ mapping
  - [ ] qmri_vfa - Variable Flip Angle

### Set Type Testing

**Current**: Only plain sets tested with ds-dwi

**Need to test**:
- [ ] **Named Sets** - Grouping by entity (e.g., acquisition)
  - Test with: Multi-acquisition datasets
  - Config: `group_by: "acq"`
  - Expected: `{acq1: file1, acq2: file2}`

- [ ] **Sequential Sets** - Ordering by entity (e.g., echo)
  - Test with: Multi-echo datasets (qmri_megre)
  - Config: `sequence_by: "echo"`
  - Expected: `[echo1, echo2, echo3]`

- [ ] **Mixed Sets** - Nested grouping + sequencing
  - Test with: Field mapping datasets
  - Config: `group_by: "acq"`, `sequence_by: "dir"`
  - Expected: `{acq1: [dir-AP, dir-PA], acq2: [...]}`

### Cross-Modal Broadcasting

**Status**: Implemented but untested

**Test Scenarios**:
- [ ] Anatomical (anat) broadcast to functional (func)
- [ ] T1w available for all sessions → broadcast to dwi
- [ ] Field maps broadcast to EPI sequences

**Expected Behavior**:
```
Input: sub-01_T1w.nii.gz (single anatomical)
       sub-01_ses-01_dwi.nii.gz
       sub-01_ses-02_dwi.nii.gz

Output: [sub-01_ses-01, [anat: T1w, dwi: dwi-ses01]]
        [sub-01_ses-02, [anat: T1w, dwi: dwi-ses02]]
```

### Output Validation

**Goal**: Compare plugin output with expected outputs

**Location**: `tests/expected_outputs/`

**Datasets with Expected Outputs**:
- [ ] asl001
- [ ] asl002
- [ ] ds-dwi (current test)
- [ ] ds-dwi2, ds-dwi3, ds-dwi4
- [ ] ds-mrs_fmrs
- [ ] ds-mtsat
- [ ] eeg_cbm
- [ ] All qmri_* variants

**Comparison Tool**: `tests/compare_json_dirs.sh`

**Commands**:
```bash
# Run plugin on dataset
nextflow run validation/main.nf --bids_dir tests/data/custom/ds-dwi

# Compare with expected
./tests/compare_json_dirs.sh \
  actual_output/ \
  tests/expected_outputs/ds-dwi/
```

---

## Priority 2: Code Quality & CI/CD ⚙️

**Add JavaDoc/GroovyDoc**:
- [ ] BidsHandler class and methods
- [ ] BidsChannelFactory.fromBIDS()
- [ ] All set handler classes
- [ ] Type-casting patterns (why needed)

---

## Priority 4: Code Quality & CI/CD ⚙️

### Static Analysis

**Tool**: CodeNarc

```bash
./gradlew codenarcMain
./gradlew codenarcTest
```

**Fix**:
- [ ] Unused imports
- [ ] Dead code
- [ ] Code style violations
- [ ] Complexity warnings

### Performance Profiling

**Areas to Profile**:
- [ ] libBIDS.sh parsing (I/O bound)
- [ ] Entity grouping operations
- [ ] Channel emission speed
- [ ] Memory usage with large datasets

**Optimization Ideas**:
- Cache parsed datasets
- Parallelize file processing
- Stream large CSV outputs
- Optimize regex patterns

### CI/CD Setup

**Platform**: GitHub Actions

**Workflow**:
```yaml
name: nf-bids CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Java
        uses: actions/setup-java@v2
      - name: Run tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v2
```

**Checks**:
- [ ] Unit tests passing
- [ ] Code coverage >80%
- [ ] Static analysis passing
- [ ] Build artifacts created

---

## Priority 5: Feature Enhancements 🚀

### BIDS Derivatives Support

**Goal**: Support BIDS derivatives directory structure

**Example**:
```
derivatives/
  fmriprep/
    sub-01/
      anat/
        sub-01_space-MNI152_T1w.nii.gz
```

**Implementation**:
- [ ] Add derivatives path parameter
- [ ] Parse derivatives with libBIDS.sh
- [ ] Handle space-* entity
- [ ] Support desc-* entity
- [ ] Merge with raw BIDS data

### Custom Entities

**Goal**: Support non-standard BIDS entities

**Current**: Only standard BIDS entities supported

**Enhancement**:
- [ ] Load custom entity definitions
- [ ] Validate custom patterns
- [ ] Support in grouping logic

**Config Example**:
```yaml
custom_entities:
  - name: "contrast"
    pattern: "contrast-[a-zA-Z0-9]+"
  - name: "model"
    pattern: "model-[a-zA-Z0-9]+"
```

### Progress Reporting

**Goal**: Show progress for large datasets

**Implementation**:
```groovy
log.info "Parsing BIDS dataset: ${bidsDir}"
log.info "Found ${totalFiles} files"
log.info "Processing ${currentFile}/${totalFiles}..."
log.info "Emitted ${channelItems} channel items"
```

**Features**:
- [ ] File count estimation
- [ ] Progress percentage
- [ ] Estimated time remaining
- [ ] Summary statistics

### Incremental Updates

**Goal**: Re-parse only changed files

**Implementation**:
- [ ] Cache parsed dataset metadata
- [ ] Detect file changes (mtime)
- [ ] Merge cached + new data
- [ ] Invalidate cache on config change

**Use Case**: Large datasets with frequent updates

---

## Quick Reference

For detailed guides, see:
- [QUICKSTART.md](../QUICKSTART.md) - 5-minute setup
- [development.md](development.md) - Development workflow  
- [testing.md](testing.md) - Testing strategies
- [TODO.md](TODO.md) - Current priorities

---

**Plugin Status**: ✅ Fully Functional  
**Test Coverage**: 29/29 tests passing  
**Last Updated**: December 2024
