```prompt
# Plan: fromBIDS Output Structure Redesign (Version B - Nested Maps)

This document records the final design and implementation details for Version B (Nested Maps) of the `Channel.fromBIDS` flat output.

Summary of final decisions:
- The factory will optionally flatten the emitted tuples of `[groupingKey, enrichedData]` into simplified maps. The flattened output puts `meta` under the `meta` key and places suffix groupings at the top level of the emitted map (e.g., `item.T1w.nii`, not `item.data.T1w.nii`).
- File paths are resolved into absolute `File` instances during flattening using `bidsParentDir` and the `bidsParentDir` is not present in final output.
- Flattening occurs at `validateAndEmitChannel()` and is enabled by default. Consumers requiring the original tuple format can disable flattening at runtime via `options.flatten_output = false`.
- The `meta` key is reserved and cannot be used as a suffix name in configuration (validated by the loader); enrichedData top-level entity keys are merged into `meta` to preserve entity context.
- Internal processing of enrichedData and `enrichedData.data` structure is unchanged; flattening only affects the final emitted item format.

Why this design?
- Backwards compatibility: It maintains original tuple emission when users opt out via `options.flatten_output`. The validation scripts and tests that relied on the old tuple format can continue to opt-out for the time being.
- Usability: The flattened nested map structure (top-level suffixes + `meta`) offers a more familiar dotted-style access for users (e.g., `item.T1w.nii`) while still preserving hierarchical and mixed set structures.

## Implementation details (what we did)

1) Add flattening transformer

File: `src/main/groovy/nfneuro/channel/BidsHandler.groovy`

- Added a new method `flattenTupleToMap(List tuple, Map entityValues)` which:
    - Accepts a `[groupingKey, enrichedData]` tuple, where `enrichedData.data` contains suffix structures.
    - Builds `meta` using `entityValues`, and also merges important top-level enrichedData entity keys into `meta` (excluding control keys such as `data`, `filePaths`, `bidsParentDir`).
    - Walks `enrichedData.data` recursively and converts path values to absolute `File` objects using `bidsParentDir`.
    - Preserves existing nested map structure inside suffix values — named sets remain grouped (e.g., `item.dwi.ap`), hierarchical sequential structures remain nested maps, and sequential lists remain `List<File>`.
    - Returns a `Map` which contains `meta` and all suffixes at the top-level (e.g., `flat.T1w`, `flat.dwi`, `flat.bold`).

2) Apply flattening in `validateAndEmitChannel()` (conditionally)

File: `src/main/groovy/nfneuro/channel/BidsHandler.groovy`

- At emission (`validateAndEmitChannel`), the handler now checks the `options.flatten_output` flag to decide whether to flatten the tuple or emit the original `[groupingKey, enrichedData]` item.
    - Default: `options.flatten_output` is true (or missing). Flattening is performed.
    - If `options.flatten_output` is `false`: Behaves like legacy code and emits the unmodified tuple.
- Added robust error handling with a descriptive error message:
    - "Failed to flatten BIDS data for entities ${entityValues}: ${e.message}"
- Emission now returns either:
    - Flattened map (top-level suffixes + `meta`) when `flatten_output:true`, or
    - Original `[groupingKey, enrichedData]` tuple when `flatten_output:false`.

3) Reserved `meta` key validation

File: `src/main/groovy/nfneuro/config/BidsConfigLoader.groovy`

- The loader now throws an `IllegalArgumentException` if the top-level YAML configuration contains `meta` as a suffix key. The message clarifies that's a reserved key name (used by the plugin to emit metadata in flattened results).

4) Tests and test suite updates

- Updated unit tests to assert the new output structure and file path resolution:
    - Added `BidsHandlerFlattenSpec` to test `flattenTupleToMap`, `validateAndEmitChannel` emission, absolute path conversion, list conversion, and merging of entity keys into `meta`.
    - Reworked operator and extension tests to account for combineBy change (closure-based key extractors), and included `overloads` on `BidsExtension.combineBy` to accept both 1-closure and 2-closure signatures.
    - Updated the validation test suite to explicitly opt-out of flattening by adding `flatten_output:false` to the `options` map.
    - All unit tests and `./gradlew test` passes locally.

5) Documentation & migration guidance (recommendations)

- The prompt authoring and documentation updates still needed (README, MIGRATION_GUIDE, docs/examples) should reflect the final output format and the presence of `options.flatten_output`. Suggested migration examples:
    - Before:
        Channel.fromBIDS(params.bids_dir, 'config.yaml')
            .map { key, data ->
                def subject = key[0]
                def dwi_ap = file(data.bidsParentDir) / data.data.dwi.ap.nii
            }
    - After (flat):
        Channel.fromBIDS(params.bids_dir, 'config.yaml')
            .map { item ->
                def subject = item.meta.subject
                def dwi_ap = item.dwi.ap.nii  // absolute File
            }
    - Or disable flattening for legacy code: `Channel.fromBIDS(bidsDir, config, [flatten_output: false])`

## Dev notes (noteworthy details)
- The `meta` field is the only nested map included in the flattened output.
- We intentionally do not change the internal format in the processing pipeline — only the final emitted items are transformed.
- The `data` key was removed from final output; suffix groupings (T1w, dwi, bold, etc.) are top-level in the final map.
- If a user passes `flatten_output:false`, we always emit the unmodified inline tuple so validation scripts and other consumers that depend on the old format continue to work.

## Future work (optional)
- Update examples and update docs (`README.md`, `docs/examples.md`, `MIGRATION_GUIDE.md`) to include new outputs and migration notes.
- Add additional integration tests in `validation/` that assert the flattened result in real nextflow runs (opt-in), and optimize flattening for performance if needed.

## Example outputs (final)

### Plain Set (flattened)
```
{
    meta: [subject: 'sub-01', session: 'ses-01'],
    T1w: { nii: file('/.../sub-01_T1w.nii.gz'), json: file('/.../sub-01_T1w.json') }
}
```

### Named Set (flattened)
```
{
    meta: [subject: 'sub-01', session: 'ses-01'],
    dwi: {
        ap: { nii: file('/.../dir-AP_dwi.nii.gz'), bval: file('/.../dir-AP_dwi.bval') },
        pa: { nii: file('/.../dir-PA_dwi.nii.gz') }
    }
}
```

### Sequential Set
```
{
    meta: [subject: 'sub-01', session: 'ses-01'],
    bold: { nii: [file('/.../run-1_bold.nii.gz'), file('/.../run-2_bold.nii.gz')], json: [...] }
}
```


## Steps

### 1. Create flattening transformer

**File:** `src/main/groovy/nfneuro/channel/BidsHandler.groovy`

Add `flattenTupleToMap(tuple, loopOverEntities)` method that:
- Recursively walks `enrichedData.data` nested structure
- Preserves nested map structure using dotted notation access:
  - Plain: `data.T1w`
  - Named: `data.dwi.ap`
  - Hierarchical: `data.bold.run1.echo1`
  - Mixed: `data.MPM.MTw` with list values
- Builds `meta` map from loop entities
- Converts all file paths to absolute using `file(bidsParentDir) / relativePath`
- Returns flat map with absolute paths and nested data structure

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
- To: `item.data.dwi.ap.nii`

Verifications:
- Sequential/mixed have lists: `assert item.data.MPM.MTw.nii instanceof List`
- File paths are absolute: `assert item.data.T1w.nii.isAbsolute()`
- Update all 71 tests for new map structure

### 5. Update documentation and migration guide

**Files:** `README.md`, `docs/examples.md`, `docs/configuration.md`, `MIGRATION_GUIDE.md`

Content:
- Show flat map access with `item.meta.subject` and `item.data.suffix.group`
- Document beta.5 → beta.6 breaking change with before/after code
- Explain absolute paths mean no `file(bidsParentDir) /` needed
- Show hierarchical nesting preserved
- Show mixed set list handling

## Output Structure Examples

### Plain Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    data: [
        T1w: [
            nii: file('/data/bids/sub-01/anat/sub-01_T1w.nii.gz'),
            json: file('/data/bids/sub-01/anat/sub-01_T1w.json')
        ]
    ]
]
```

### Named Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    data: [
        dwi: [
            ap: [
                nii: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz'),
                json: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.json'),
                bval: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval'),
                bvec: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec')
            ],
            pa: [
                nii: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz'),
                json: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.json'),
                bval: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval'),
                bvec: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec')
            ]
        ]
    ]
]
```

### Sequential Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    data: [
        bold: [
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
]
```

### Hierarchical Sequential Set
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    data: [
        bold: [
            'run-01': [
                'echo-01': [
                    nii: file('/data/bids/.../sub-01_run-01_echo-01_bold.nii.gz'),
                    json: file('/data/bids/.../sub-01_run-01_echo-01_bold.json')
                ],
                'echo-02': [
                    nii: file('/data/bids/.../sub-01_run-01_echo-02_bold.nii.gz'),
                    json: file('/data/bids/.../sub-01_run-01_echo-02_bold.json')
                ]
            ],
            'run-02': [
                'echo-01': [
                    nii: file('/data/bids/.../sub-01_run-02_echo-01_bold.nii.gz'),
                    json: file('/data/bids/.../sub-01_run-02_echo-01_bold.json')
                ]
            ]
            // ... etc
        ]
    ]
]
```

### Mixed Set (Named + Sequential)
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    data: [
        MPM: [
            MTw: [
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
            PDw: [
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
            T1w: [
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
    ]
]
```

### Cross-Modal Broadcasting
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    data: [
        dwi: [
            ap: [
                nii: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz'),
                bval: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval'),
                bvec: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec'),
                json: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.json')
            ],
            pa: [
                nii: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz'),
                bval: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval'),
                bvec: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec'),
                json: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.json')
            ]
        ],
        epi: [  // ← Cross-modal included from config
            nii: file('/data/bids/sub-01/fmap/sub-01_epi.nii.gz'),
            json: file('/data/bids/sub-01/fmap/sub-01_epi.json')
        ]
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

### After (beta.6 - Version B)
```groovy
Channel.fromBIDS(params.bids_dir, 'config.yaml')
    .map { item ->
        def subject = item.meta.subject
        def session = item.meta.session
        def dwi_ap = item.data.dwi.ap.nii  // Already absolute
        def bval = item.data.dwi.ap.bval
        def bvec = item.data.dwi.ap.bvec
        
        [subject, dwi_ap, bval, bvec]
    }
```

## Key Design Decisions

1. **Flat format as default:** No opt-in flag, breaking change for beta.6
2. **Reserved `meta` key:** Validated at config load time with clear error
3. **Absolute paths:** All file paths resolved immediately, `bidsParentDir` discarded
4. **Nested maps preserved:** Dotted notation access `item.data.suffix.group` instead of list keys
5. **Mixed sets:** Named dimension nested, sequential dimension in lists
6. **Error handling:** Clear error messages for flattening failures, downstream Nextflow handles path validation
7. **No helper methods:** Keep API simple, users access via dotted notation (returns null if not found)
8. **Performance:** Not optimized yet, benchmark at end if needed

## Comparison with Version A

**Version A (List Keys):**
- Access: `item[['dwi', 'ap']].nii`
- Pro: More explicit nested indexing with `getAt` operator
- Con: Less readable, unfamiliar syntax

**Version B (Nested Maps - THIS VERSION):**
- Access: `item.data.dwi.ap.nii`
- Pro: Familiar dotted notation, more readable
- Con: Less explicit about nesting depth

```
