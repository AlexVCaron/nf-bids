# nf-bids Plugin - Test Results

**Date**: December 2024  
**Plugin Version**: 0.1.0  
**Nextflow Version**: 25.10.0

---

## Unit Tests

**Status**: ✅ All Passing

```
Test Suite: 29 tests
Passed: 29
Failed: 0
Success Rate: 100%
```

### Test Coverage

| Component | Tests | Status |
|-----------|-------|--------|
| BidsObserver | 1 | ✅ |
| BidsChannelFactory | 2 | ✅ |
| BidsConfigLoader | 4 | ✅ |
| BidsConfigAnalyzer | 5 | ✅ |
| BidsParser | 2 | ✅ |
| BidsFile | 6 | ✅ |
| BidsDataset | 3 | ✅ |
| PlainSetHandler | 3 | ✅ |
| NamedSetHandler | 1 | ✅ |
| SequentialSetHandler | 1 | ✅ |
| MixedSetHandler | 1 | ✅ |

---

## Integration Tests

### Custom Datasets

| Dataset | Modality | Files | Status | Notes |
|---------|----------|-------|--------|-------|
| ds-dwi | DWI | 4 | ✅ | Single subject, basic DWI |
| ds-dwi2 | DWI | 4 | ✅ | With session, direction |
| ds-dwi3 | DWI | 4 | ✅ | Similar to dwi2 |
| ds-dwi4 | DWI | 24 | ✅ | Multi-subject, multi-session, bodypart |
| ds-mrs_fmrs | MRS | - | ✅ | MRS functional data |
| ds-mtsat | MTsat | - | ✅ | Magnetization transfer data |

**Summary**: ✅ 6/6 custom datasets passing

### BIDS Examples

| Dataset | Modality | Status | Notes |
|---------|----------|--------|-------|
| asl001 | ASL | ✅ | Arterial spin labeling |
| asl002 | ASL | ✅ | ASL with sessions |
| qmri_irt1 | qMRI | ✅ | Inversion recovery T1 |
| qmri_megre | qMRI | ✅ | Multi-echo gradient echo |
| qmri_mese | qMRI | ✅ | Multi-echo spin echo |
| qmri_mp2rage | qMRI | ✅ | MP2RAGE |
| qmri_mpm | qMRI | ✅ | Multi-parameter mapping |
| qmri_mtsat | qMRI | ✅ | MT saturation |
| qmri_sa2rage | qMRI | ✅ | SA2RAGE |
| qmri_tb1tfl | qMRI | ✅ | B1 TFL |
| qmri_vfa | qMRI | ✅ | Variable flip angle |
| eeg_cbm | EEG | ✅ | EEG cognitive battery |

**Summary**: ✅ 12/12 BIDS example datasets passing

**Total Integration Tests**: ✅ 18/18 passing

---

## Set Type Testing

### Plain Sets ✅
- ✅ Single file per group
- ✅ Simple suffix mapping
- ✅ Basic DWI datasets

### Named Sets
- ⏳ Testing with multi-acquisition data
- ⏳ Group by acquisition
- ⏳ Group by direction

### Sequential Sets  
- ⏳ Testing with multi-echo data
- ⏳ Ordered arrays
- ⏳ Echo sequencing

### Mixed Sets
- ⏳ Testing with field maps
- ⏳ Nested structures
- ⏳ Acquisition + direction grouping

---

## Performance

### ds-dwi (baseline)
- Parse time: < 1s
- Files processed: 4
- Memory: < 100MB

### ds-dwi4 (complex)
- Parse time: < 2s
- Files processed: 24
- Memory: < 200MB

---

## Known Issues

### Fixed Issues ✅

**Entity Extraction Bug** (Fixed: 2025-10-26)
- **Problem**: Grouping keys showed all "NA" values instead of actual subject/session/run IDs
- **Root Cause**: 
  1. CSV column name mismatch - libBIDS.sh outputs `subject`/`session` but parser expected `sub`/`ses`
  2. Configuration using full names instead of BIDS short names
- **Solution**: 
  1. Added `CSV_TO_ENTITY_MAP` in `BidsCsvParser.groovy` to map full column names to entity short names
  2. Updated config files to use BIDS short names in `loop_over`
- **Files Modified**:
  - `src/main/groovy/nfneuro/util/BidsCsvParser.groovy`
  - `validation/test_config.yaml`
  - `validation/test_datasets.sh`

---

## Next Testing Priorities

1. ✅ ~~BIDS examples datasets (asl, qmri, eeg)~~ - Completed
2. ✅ ~~Plain set validation~~ - Completed
3. ⏳ Named set validation
4. ⏳ Sequential set validation
5. ⏳ Mixed set validation
6. ⏳ Cross-modal broadcasting tests
7. ⏳ Large dataset performance testing

---

**Overall Status**: ✅ Core functionality fully validated  
**Production Ready**: ✅ Yes, for plain sets across all tested modalities  
**Test Coverage**: ✅ 18 integration tests + 29 unit tests, all passing
