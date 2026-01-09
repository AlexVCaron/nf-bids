# Plan: fromBIDS Output Structure Redesign (Version A – List Keys)  
> NOTE: Version A is DEPRECATED in favor of Version B (Nested Maps). Version B provides a more familiar dotted notation and better usability. This file is preserved for archival/reference purposes only. If you must keep the list-key style, use `options.flatten_output:false` to skip flattening and then apply a custom transformation.

Flatten the `fromBIDS` factory output by transforming `[keyMap, enrichedData]` tuples into flat maps `[meta: {...}, ['suffix', 'path', ...]: {...}]` at the final emission point in `validateAndEmitChannel()`. Convert all file paths to absolute using `bidsParentDir` then discard it. All internal processing remains unchanged. Flat format becomes the new default (superseded by Version B's nested maps approach).

## Steps

### 1. Create flattening transformer

**File:** `src/main/groovy/nfneuro/channel/BidsHandler.groovy`

Add `flattenTupleToMap(tuple, loopOverEntities)` method that:
- Recursively walks `enrichedData.data` nested structure
- Converts nested paths to list keys:
  - Plain: `['T1w']`
  - Named: `['dwi', 'ap']`
  - Hierarchical: `['bold', 'run1', 'echo1']`
  - Mixed: `['MPM', 'MTw']` with list values
- Builds `meta` map from loop entities
- Converts all file paths to absolute using `file(bidsParentDir) / relativePath`
- Returns flat map with absolute paths

### 2. Update validateAndEmitChannel()

**File:** `src/main/groovy/nfneuro/channel/BidsHandler.groovy`

- Apply `flattenTupleToMap()` to each `[keyMap, enrichedData]` tuple before `target << item`
- Wrap in try-catch to provide clear error context if flattening fails
- Error message format: `"Failed to flatten BIDS data for entities ${entityValues}: ${e.message}"`
- Emit transformed flat map

### 3. Add meta key validation

**File:** `src/main/groovy/nfneuro/config/BidsConfigLoader.groovy`

In `load()` method:
- Check `config.containsKey('meta')` after YAML parsing
- Throw `IllegalArgumentException("Configuration error: Reserved key 'meta' cannot be used as suffix name. Please rename this suffix in your configuration file.")` if detected

### 4. Rewrite all tests

**Files:** `src/test/` and `validation/`

Changes:
- From: `tuple[1].data.dwi.ap.nii`
- To: `item[['dwi', 'ap']].nii`

Verifications:
- Sequential/mixed have lists: `assert item[['MPM', 'MTw']].nii instanceof List`
- File paths are absolute: `assert item[['T1w']].nii.isAbsolute()`
- Update all 71 tests for new map structure

### 5. Update documentation and migration guide

**Files:** `README.md`, `docs/examples.md`, `docs/configuration.md`, `MIGRATION_GUIDE.md`

Content:
- Show flat map access with `item.meta.subject` and `item[['suffix', 'group']]`
- Document beta.5 → beta.6 breaking change with before/after code
- Explain absolute paths mean no `file(bidsParentDir) /` needed
- Show hierarchical flattening into keys
- Show mixed set list handling

## Output Structure Examples

### Plain Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    ['T1w']: [
        nii: file('/data/bids/sub-01/anat/sub-01_T1w.nii.gz'),
        json: file('/data/bids/sub-01/anat/sub-01_T1w.json')
    ]
]
```

### Named Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    ['dwi', 'ap']: [
        nii: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz'),
        json: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.json'),
        bval: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval'),
        bvec: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec')
    ],
    ['dwi', 'pa']: [
        nii: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz'),
        json: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.json'),
        bval: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval'),
        bvec: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec')
    ]
]
```

### Sequential Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    ['bold']: [
        nii: [
            file('/data/bids/sub-01/func/sub-01_run-01_bold.nii.gz'),
            file('/data/bids/sub-01/func/sub-01_run-02_bold.nii.gz'),
            file('/data/bids/sub-01/func/sub-01_run-03_bold.nii.gz')
        ],
        json: [
            file('/data/bids/sub-01/func/sub-01_run-01_bold.json'),
            file('/data/bids/sub-01/func/sub-01_run-02_bold.json'),
            file('/data/bids/sub-01/func/sub-01_run-03_bold.json')
        ]
    ]
]
```

### Hierarchical Sequential Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    ['bold', 'run-01', 'echo-01']: [
        nii: file('/data/bids/.../sub-01_run-01_echo-01_bold.nii.gz'),
        json: file('/data/bids/.../sub-01_run-01_echo-01_bold.json')
    ],
    ['bold', 'run-01', 'echo-02']: [
        nii: file('/data/bids/.../sub-01_run-01_echo-02_bold.nii.gz'),
        json: file('/data/bids/.../sub-01_run-01_echo-02_bold.json')
    ],
    ['bold', 'run-02', 'echo-01']: [
        nii: file('/data/bids/.../sub-01_run-02_echo-01_bold.nii.gz'),
        json: file('/data/bids/.../sub-01_run-02_echo-01_bold.json')
    ]
    // ... etc
]
```

### Mixed Set (Named + Sequential)
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    ['MPM', 'MTw']: [
        nii: [
            file('/data/bids/.../sub-01_acq-MTw_echo-01_MPM.nii.gz'),
            file('/data/bids/.../sub-01_acq-MTw_echo-02_MPM.nii.gz'),
            file('/data/bids/.../sub-01_acq-MTw_echo-03_MPM.nii.gz')
        ],
        json: [
            file('/data/bids/.../sub-01_acq-MTw_echo-01_MPM.json'),
            file('/data/bids/.../sub-01_acq-MTw_echo-02_MPM.json'),
            file('/data/bids/.../sub-01_acq-MTw_echo-03_MPM.json')
        ]
    ],
    ['MPM', 'PDw']: [
        nii: [
            file('/data/bids/.../sub-01_acq-PDw_echo-01_MPM.nii.gz'),
            file('/data/bids/.../sub-01_acq-PDw_echo-02_MPM.nii.gz'),
            file('/data/bids/.../sub-01_acq-PDw_echo-03_MPM.nii.gz')
        ],
        json: [
            file('/data/bids/.../sub-01_acq-PDw_echo-01_MPM.json'),
            file('/data/bids/.../sub-01_acq-PDw_echo-02_MPM.json'),
            file('/data/bids/.../sub-01_acq-PDw_echo-03_MPM.json')
        ]
    ],
    ['MPM', 'T1w']: [
        nii: [
            file('/data/bids/.../sub-01_acq-T1w_echo-01_MPM.nii.gz'),
            file('/data/bids/.../sub-01_acq-T1w_echo-02_MPM.nii.gz'),
            file('/data/bids/.../sub-01_acq-T1w_echo-03_MPM.nii.gz')
        ],
        json: [
            file('/data/bids/.../sub-01_acq-T1w_echo-01_MPM.json'),
            file('/data/bids/.../sub-01_acq-T1w_echo-02_MPM.json'),
            file('/data/bids/.../sub-01_acq-T1w_echo-03_MPM.json')
        ]
    ]
]
```

### Cross-Modal Broadcasting
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    ['dwi', 'ap']: [
        nii: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz'),
        bval: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval'),
        bvec: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec'),
        json: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.json')
    ],
    ['dwi', 'pa']: [
        nii: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz'),
        bval: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval'),
        bvec: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec'),
        json: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.json')
    ],
    ['epi']: [  // ← Cross-modal included from config
        nii: file('/data/bids/sub-01/fmap/sub-01_epi.nii.gz'),
        json: file('/data/bids/sub-01/fmap/sub-01_epi.json')
    ]
]
```

## Migration Examples

### Before (beta.5)
```groovy
Channel.fromBIDS(params.bids_dir, 'config.yaml')
    .map { key, data ->
        def subject = key[0]
        def session = key[1]
        def dwi_ap = file(data.bidsParentDir) / data.data.dwi.ap.nii
        def bval = file(data.bidsParentDir) / data.data.dwi.ap.bval
        def bvec = file(data.bidsParentDir) / data.data.dwi.ap.bvec
        
        [subject, dwi_ap, bval, bvec]
    }
```

### After (beta.6)
```groovy
Channel.fromBIDS(params.bids_dir, 'config.yaml')
    .map { item ->
        def subject = item.meta.subject
        def session = item.meta.session
        def dwi_ap = item[['dwi', 'ap']].nii  // Already absolute
        def bval = item[['dwi', 'ap']].bval
        def bvec = item[['dwi', 'ap']].bvec
        
        [subject, dwi_ap, bval, bvec]
    }
```

## Key Design Decisions

1. **Flat format as default:** No opt-in flag, breaking change for beta.6
2. **Reserved `meta` key:** Validated at config load time with clear error
3. **Absolute paths:** All file paths resolved immediately, `bidsParentDir` discarded
4. **Hierarchical flattening:** All nested levels become list keys (not optimal, but V1 acceptable)
5. **Mixed sets:** Named dimension in key, sequential dimension in lists
6. **Error handling:** Clear error messages for flattening failures, downstream Nextflow handles path validation
7. **No helper methods:** Keep API simple, users access via `item[['key']]` (returns null if not found)
8. **Performance:** Not optimized yet, benchmark at end if needed
