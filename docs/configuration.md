# BIDS Configuration Guide

The configuration file defines how BIDS files are grouped and emitted through the channel. It follows the same format as the original [bids2nf](https://agah.dev/bids2nf/configuration) workflow.

## Table of Contents

1. [Configuration File Structure](#configuration-structure)
2. [Runtime Options](#runtime-options)
3. [Loop Over Entities](#loop-over-entities)
4. [Set Types](#set-types)
   - [Plain Sets](#plain-sets)
   - [Named Sets](#named-sets)
   - [Sequential Sets](#sequential-sets)
   - [Mixed Sets](#mixed-sets)
   - [Heterogeneous Datasets](#advanced-heterogeneous-datasets)
5. [Global Options Reference](#global-options-reference)
   - [additional_extensions](#additional_extensions)
   - [include_cross_modal](#include_cross_modal)
   - [exclude_entities](#exclude_entities)
   - [suffix_maps_to](#suffix_maps_to)
   - [required](#required)
6. [Set-Specific Options Reference](#set-specific-options-reference)
   - [parts](#parts)
7. [Cross-Modal Broadcasting](#cross-modal-broadcasting)

---

# Runtime Options

`Channel.fromBIDS()` accepts an optional `options` map for runtime configuration:

```groovy
Channel.fromBIDS(
    params.bids_dir, 
    'config.yaml',
    [
        flatten_output: true,        // default
        unpack_json_sidecar: false,  // default
        libbids_sh: '/custom/path',  // optional
        validate: false              // not implemented
    ]
)
```

## Available Options

### `flatten_output` (Boolean)

**Since:** 0.1.0-beta.6 (opt-in), **Default** since 0.1.0-beta.9

**Default:** `true`

Controls the output format of `Channel.fromBIDS()`:

**When `true` (default in beta.9+):**
```groovy
// Flattened map with meta + top-level suffixes
[
    meta: [subject: 'sub-01', session: 'ses-01', run: 'NA'],
    dwi: [
        nii: Path("/abs/path/to/sub-01_ses-01_dwi.nii.gz"),
        json: Path("/abs/path/to/sub-01_ses-01_dwi.json"),
        bval: Path("/abs/path/to/sub-01_ses-01_dwi.bval"),
        bvec: Path("/abs/path/to/sub-01_ses-01_dwi.bvec")
    ],
    T1w: [
        nii: Path("/abs/path/to/sub-01_ses-01_T1w.nii.gz"),
        json: Path("/abs/path/to/sub-01_ses-01_T1w.json")
    ]
]
```

**Benefits:**
- 🎯 **Semantic access:** `item.meta.subject`, `item.dwi.nii`
- ✅ **Absolute paths:** All files are `java.nio.file.Path` objects, ready to use
- 🔧 **Type-safe:** IDE autocomplete works with named fields
- 🚀 **Operator-friendly:** Use `groupTupleBy { it.meta.subject }` directly
- 📦 **Cloud-ready:** Path objects work with S3, GCS, Azure storage

**When `false` (legacy):**
```groovy
// Original tuple format for backward compatibility
[
    ["sub-01", "ses-01", "NA", "NA"],  // grouping key
    [
        bidsParentDir: "/path/to/bids",
        subject: "sub-01",
        session: "ses-01",
        data: [
            dwi: [
                nii: "sub-01/ses-01/dwi/sub-01_ses-01_dwi.nii.gz"  // relative
            ]
        ]
    ]
]
```

**Use legacy format when:**
- Migrating from baseline bids2nf
- Existing workflows expect tuple structure
- Gradual migration preferred

**Example usage:**
```groovy
// New workflows (recommended)
Channel.fromBIDS(params.bids_dir, 'config.yaml')
    .map { item -> 
        [item.meta.subject, item.dwi.nii, item.dwi.bval]
    }

// Legacy workflows
Channel.fromBIDS(params.bids_dir, 'config.yaml', [flatten_output: false])
    .map { key, data ->
        [key[0], file(data.bidsParentDir) / data.data.dwi.nii]
    }
```

### `libbids_sh` (String)

**Optional:** Path to custom libBIDS.sh parsing script

```groovy
Channel.fromBIDS(
    params.bids_dir,
    'config.yaml',
    [libbids_sh: '/path/to/custom/libBIDS.sh']
)
```

Use when:
- Testing libBIDS.sh modifications
- Using custom BIDS parsing logic
- Pinning specific libBIDS.sh version

### `unpack_json_sidecar` (Boolean)

**Default:** `false`

When enabled, `.json` sidecars are parsed and emitted as Groovy maps (dictionary-like objects) instead of path values.

**When `false` (default):**
```groovy
item.T1w.json == Path('/abs/path/to/sub-01_T1w.json')
```

**When `true`:**
```groovy
item.T1w.json == [RepetitionTime: 2.0, TaskName: 'rest']
```

Works with both flattened output (`flatten_output: true`) and legacy tuple output (`flatten_output: false`).

### `validate` (Boolean)

**Not implemented** - Reserved for future BIDS validator integration

### `validator_version` (String)

**Not implemented** - Reserved for specifying validator version

### `ignore_codes` (String)

**Not implemented** - Reserved for ignoring specific validation error codes

---

# Overview

## Configuration Structure

```yaml
# Define which BIDS entities to loop over (outer grouping)
loop_over:
  - subject
  - session
  - run

# Define how each data type (suffix) should be grouped
# Options are at the config key level:
<suffix_name>:
  <set_type>: { ... }                     # Set-specific config
  additional_extensions: [.bval, .bvec]   # Global option
  include_cross_modal: [T1w]              # Global option
  suffix_maps_to: "dwi"                   # Global option
  exclude_entities: [direction]            # Global option
  required: [ap, pa]                      # Global option (named/mixed sets only)
```

**Output Format (beta.9+):**

```groovy
[
  meta: [
    subject: 'sub-01',
    session: 'ses-01',
    run: 'NA'
  ],
  dwi: [
    nii: Path('/abs/path/to/sub-01_ses-01_dwi.nii.gz'),
    json: Path('/abs/path/to/sub-01_ses-01_dwi.json'),
    bval: Path('/abs/path/to/sub-01_ses-01_dwi.bval'),
    bvec: Path('/abs/path/to/sub-01_ses-01_dwi.bvec')
  ],
  T1w: [
    nii: Path('/abs/path/to/sub-01_ses-01_T1w.nii.gz'),
    json: Path('/abs/path/to/sub-01_ses-01_T1w.json')
  ]
]
```

---

# Loop Over Entities

The `loop_over` section defines the outer grouping structure:

```yaml
loop_over:
  - subject
  - session
  - run
  - ...
```

**Valid Entities:**
- `subject` - BIDS subject identifier (sub-01, sub-02, etc.)
- `session` - Session identifier (ses-01, ses-baseline, etc.)
- `run` - Run number (run-01, run-02, etc.)
- `task` - Task name (task-rest, task-nback, etc.)
- `acquisition` - Acquisition type (acq-highres, acq-lowres, etc.)
- `direction` - Phase encoding direction (dir-AP, dir-PA, etc.)
- `echo` - Echo number (echo-1, echo-2, etc.)
- Any other valid [BIDS entity](https://bids-specification.readthedocs.io/en/stable/appendices/entities.html)

>[!NOTE]
>- Entities are extracted from filenames (JSON sidecar unpacking is optional via `unpack_json_sidecar`)
>- Order matters: defines the hierarchy of grouping
>- Missing entities default to "NA" in the output

# Set Types

Sets define how BIDS files are grouped and organized. **Configuration options** (`additional_extensions`, `suffix_maps_to`, `required`, `exclude_entities`, `include_cross_modal`) are specified **at the config key level** (outside the set type definition).

## Plain Set

Simple 1:1 mapping of files' suffixes to keys in the data map.

```yaml
<suffix>:
  plain_set: {}  # Set-specific config (usually empty for plain sets)
  # Options at config key level:
  additional_extensions: [.bval, .bvec]
  include_cross_modal: [T1w]
  exclude_entities: [direction]
```

### Example:

```yaml
loop_over:
- subject

T1w:
  plain_set: {}
```

**Output (flat format, default):**

```groovy
[
  meta: [subject: 'sub-01', session: 'NA', run: 'NA'],
  T1w: [
    nii: Path("/path/to/bids/sub-01/anat/sub-01_T1w.nii.gz"),
    json: Path("/path/to/bids/sub-01/anat/sub-01_T1w.json")
  ]
]
```

### Available Options:

**At config key level:**
- **`additional_extensions`** - Include non-NIfTI files (e.g., `.bval`, `.bvec`)
- **`include_cross_modal`** - Include files from other suffixes
- **`exclude_entities`** - Exclude files with specific entities
- **`suffix_maps_to`** - Use files with different suffix

**Inside `plain_set`:**
- **`parts: [type1, ...]`** - Filter by file parts (e.g., `["mag", "phase"]`) - rarely used

**Use Cases:**
- Structural images (T1w, T2w, FLAIR)
- Single files per subject/session
- Simple anatomical references

**See:** [Global Options](#global-options-reference) for detailed option documentation.

---

## Named Set

Group files by specific entities, in named keys under a specific suffix in the data map.

```yaml
<suffix>:
  named_set:  # Named groups are INSIDE named_set
    <key>:
      <entity>: <value>
  # Options at config key level (OUTSIDE named_set):
  required: [key1, key2]
  additional_extensions: [.bval, .bvec]
  suffix_maps_to: "other_suffix"
```

### Example:

```yaml
loop_over:
- subject

dwi:
  named_set:  # Named groups INSIDE
    ap:
      direction: dir-AP
    pa:
      direction: dir-PA
  # Options OUTSIDE at config key level:
  required: [ap, pa]
  additional_extensions: [.bval, .bvec]
```

**Output (flat format, default):**

```groovy
[
  meta: [subject: 'sub-01', session: 'NA'],
  dwi: [
    ap: [
      nii: Path("/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz"),
      bval: Path("/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval"),
      bvec: Path("/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec"),
      json: Path("/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.json")
    ],
    pa: [
      nii: Path("/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz"),
      bval: Path("/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval"),
      bvec: Path("/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec"),
      json: Path("/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.json")
    ]
  ]
]
```

### Available Options:

**Inside `named_set`:** (set-specific)
- **Named groups** - Map group names to entity patterns (required)
- **`parts: [type1, ...]`** - Filter by file parts within each group

**At config key level:** (global options)
- **`required: [group1, group2]`** - Enforce presence of specific named groups
- **`additional_extensions`** - Include extra file types  
- **`include_cross_modal`** - Include files from other suffixes
- **`suffix_maps_to`** - Use different file suffix
- **`exclude_entities`** - Filter by entity (since beta.6)

**Use Cases:**
- Phase-encoding directions (AP/PA, RL/LR)
- Multiple b-values in DWI (b1000, b2000, b3000)
- Field maps with different acquisitions
- Multi-flip angle imaging

**See:** [Global Options](#global-options-reference) for detailed option documentation.

>[!NOTE]
>Alternatively, the `suffix_maps_to` instruction can be used, for example if the `dwi` suffix is already in use for base mapping :
>```yaml
>dwi_with_reverse:
>  suffix_maps_to: dwi
>  named_set:
>    ...
>```
>
>**Output (0.1.0-beta.9+):**
>
>```groovy
>{
>  meta: { subject: 'sub-01', ... },
>  dwi_with_reverse: {  // Config key, not "dwi"
>    ap: { nii: Path(...), json: Path(...) },
>    pa: { nii: Path(...), json: Path(...) }
>  }
>}
>```

### Advanced: Heterogeneous Datasets

**New in 0.1.0-beta.9:** Multiple configurations can now use the same `suffix_maps_to` value, enabling support for heterogeneous datasets where different subjects have different acquisition schemes.

#### Example: Multiple Phase-Encoding Directions

Some subjects may have AP/PA phase encoding, others have RL/LR, and some have IS/SI:

```yaml
loop_over:
  - subject
  - session

# Plain DWI (no phase encoding direction)
dwi:
  plain_set: {}  # No set-specific config
  exclude_entities: [direction]  # ← CRITICAL: Option at config key level
  additional_extensions: [bvec, bval]  # ← Option at config key level

# AP/PA phase encoding
dwi_ap:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: ["ap", "pa"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"  # Same suffix as plain dwi

# RL/LR phase encoding  
dwi_rl:
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  required: ["rl", "lr"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"  # No collision!

# IS/SI phase encoding
dwi_is:
  named_set:
    is: {direction: dir-IS}
    si: {direction: dir-SI}
  required: ["is", "si"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"  # All three can coexist
```

**Output for subject with AP/PA:**
```groovy
{
  meta: { subject: 'sub-01', session: 'ses-01' },
  dwi_ap: {  // Config key preserved
    ap: { nii: Path(...), bval: Path(...), bvec: Path(...) },
    pa: { nii: Path(...), bval: Path(...), bvec: Path(...) }
  }
}
```

**Output for subject with RL/LR:**
```groovy
{
  meta: { subject: 'sub-02', session: 'ses-01' },
  dwi_rl: {  // Different config key
    rl: { nii: Path(...), bval: Path(...), bvec: Path(...) },
    lr: { nii: Path(...), bval: Path(...), bvec: Path(...) }
  }
}
```

#### Critical Pattern: `exclude_entities`

**When using `suffix_maps_to`, you MUST use `exclude_entities` in plain configs** to prevent files from matching multiple configurations:

```yaml
# ❌ WRONG: Files with dir-AP will match BOTH configs
dwi:
  plain_set: {}  # Matches ALL dwi files
  
dwi_ap:
  named_set:
    ap: {direction: dir-AP}
  suffix_maps_to: "dwi"  # Files with dir-AP match this too!

# ✅ CORRECT: Files only match one config
dwi:
  plain_set: {}
  exclude_entities: [direction]  # \u2190 Option at config key level
    
dwi_ap:
  named_set:
    ap: {direction: dir-AP}
  suffix_maps_to: "dwi"  # \u2190 Option at config key level
```

Without `exclude_entities`, the same file will appear in BOTH `dwi` and `dwi_ap` outputs (duplicate data).

#### How It Works

1. **SuffixMapper** creates a mapping: `{dwi_ap: "dwi", dwi_rl: "dwi", dwi_is: "dwi", dwi: "dwi"}`
2. **File matching** tries each config key for files with `dwi` suffix
3. **Entity filtering** determines which config matches:
   - File with `dir-AP` → matches `dwi_ap` (has `direction: dir-AP` filter)
   - File with `dir-RL` → matches `dwi_rl` (has `direction: dir-RL` filter)
   - File without `dir-*` → matches `dwi` (excluded by others)
4. **Output** uses the matched config key: `dwi_ap`, `dwi_rl`, `dwi_is`, or `dwi`

---

## Sequential Set

Order files by an entity (or multiple entities), in arrays under a specific suffix in the data map.

```yaml
<suffix>:
  sequential_set:  # Set-specific config INSIDE
    by_entity: <entity>
    # OR
    by_entities: [entity1, entity2]
    order: hierarchical  # optional, for by_entities only
  # Options at config key level (OUTSIDE):
  additional_extensions: [.bval]
  exclude_entities: [reconstruction]
```

### Example:

```yaml
loop_over:
- subject

megre:
  sequential_set:  # Set-specific config INSIDE
    by_entity: echo
```

**Output (flat format, default):**

```groovy
[
  meta: [subject: 'sub-01', session: 'NA'],
  megre: [
    nii: [
      Path("/path/to/bids/sub-01/anat/sub-01_echo-01_megre.nii.gz"),
      Path("/path/to/bids/sub-01/anat/sub-01_echo-02_megre.nii.gz"),
      Path("/path/to/bids/sub-01/anat/sub-01_echo-03_megre.nii.gz")
    ],
    json: [
      Path("/path/to/bids/sub-01/anat/sub-01_echo-01_megre.json"),
      Path("/path/to/bids/sub-01/anat/sub-01_echo-02_megre.json"),
      Path("/path/to/bids/sub-01/anat/sub-01_echo-03_megre.json")
    ]
  ]
]
```

### Multiple Entities (Hierarchical):

```yaml
loop_over:
- subject

vfa:  # Variable Flip Angle
  sequential_set:
    by_entities:
    - flip
    - echo
    order: hierarchical
```

**Output (flat format, hierarchical):**

```groovy
[
  meta: [subject: 'sub-01', session: 'NA'],
  vfa: [
    nii: [
      [  // flip-1
        Path("/path/to/bids/sub-01/anat/sub-01_flip-1_echo-1_vfa.nii.gz"),
        Path("/path/to/bids/sub-01/anat/sub-01_flip-1_echo-2_vfa.nii.gz")
      ],
      [  // flip-2
        Path("/path/to/bids/sub-01/anat/sub-01_flip-2_echo-1_vfa.nii.gz"),
        Path("/path/to/bids/sub-01/anat/sub-01_flip-2_echo-2_vfa.nii.gz")
      ]
    ],
    json: [...] // Same hierarchical structure
  ]
]
```

### Available Options:

**Inside `sequential_set`:** (set-specific)
- **`by_entity: "entity"`** - Sequence by single entity (required for single)
- **`by_entities: [e1, e2]`** - Sequence by multiple entities (required for multiple)
- **`order: hierarchical|flat`** - Nesting strategy (default: hierarchical, for by_entities only)
- **`parts: [type1, ...]`** - Filter by file parts (e.g., `["mag", "phase"]`)

**At config key level:** (global options)
- **`additional_extensions`** - Include extra file types
- **`exclude_entities`** - Filter by entity (since beta.6)
- **`include_cross_modal`** - Include files from other suffixes

**Use Cases:**
- Multi-echo acquisitions (GRE, MEGRE)
- Multi-flip angle sequences (VFA, AFI)
- Inversion recovery (MP2RAGE, SA2RAGE)
- Time-series with ordered runs

**See:** [Global Options](#global-options-reference) for detailed option documentation.

---

## Mixed Set

Nested combination of named filtering and sequential ordering.

```yaml
<suffix>:
  mixed_set:  # Set-specific config INSIDE
    named_dimension: <entity>
    sequential_dimension: <entity>
    named_groups:  # Named groups INSIDE mixed_set
      <group_name>:
        <entity>: <value>
  # Options at config key level (OUTSIDE):
  required: [group1, group2]
  additional_extensions: [.ext]
```

### Example:

```yaml
loop_over:
- subject

mpm:
  mixed_set:  # Set-specific config INSIDE
    named_dimension: acquisition
    sequential_dimension: echo
    named_groups:  # Named groups INSIDE
      MTw:
        acquisition: acq-MTw
        flip: flip-1
        mtransfer: mt-on
      PDw:
        acquisition: acq-PDw
        flip: flip-1
        mtransfer: mt-off
      T1w:
        acquisition: acq-T1w
        flip: flip-2
        mtransfer: mt-off
  # Options OUTSIDE at config key level:
  required: [MTw, PDw, T1w]
```

**Output (flat format, default):**

```groovy
[
  meta: [subject: 'sub-01', session: 'NA'],
  mpm: [
    MTw: [
      nii: [
        Path("/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-01_flip-1_mt-on_MPM.nii.gz"),
        Path("/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-02_flip-1_mt-on_MPM.nii.gz")
      ],
      json: [
        Path("/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-01_flip-1_mt-on_MPM.json"),
        Path("/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-02_flip-1_mt-on_MPM.json")
      ]
    ],
    PDw: [
      nii: [
        Path("/path/to/bids/sub-01/anat/sub-01_acq-PDw_echo-01_flip-1_mt-off_MPM.nii.gz"),
        Path("/path/to/bids/sub-01/anat/sub-01_acq-PDw_echo-02_flip-1_mt-off_MPM.nii.gz")
      ],
      json: [...]
    ],
    T1w: [
      nii: [
        Path("/path/to/bids/sub-01/anat/sub-01_acq-T1w_echo-01_flip-2_mt-off_MPM.nii.gz"),
        Path("/path/to/bids/sub-01/anat/sub-01_acq-T1w_echo-02_flip-2_mt-off_MPM.nii.gz")
      ],
      json: [...]
    ]
  ]
]
```

### Available Options:

**Inside `mixed_set`:** (set-specific)
- **`named_dimension: "entity"`** - Entity for named grouping (required)
- **`sequential_dimension: "entity"`** - Entity for array ordering (required)
- **`named_groups: {...}`** - Named group definitions (required)
- **`parts: [type1, ...]`** - Filter by file parts within groups

**At config key level:** (global options)
- **`required: [group1, ...]`** - Enforce presence of named groups
- **`additional_extensions`** - Include extra file types
- **`exclude_entities`** - Exclude files with specific entities
- **`include_cross_modal`** - Include files from other suffixes

**Use Cases:**
- Multi-parametric mapping (MPM, qMRI)
- Multiple acquisitions each with multiple echoes
- Field maps with acquisitions and directions
- Complex nested protocols

**See:** [Global Options](#global-options-reference) for detailed option documentation.

---

# Global Options Reference

These options can be applied to any set type (plain, named, sequential, or mixed).

## `additional_extensions`

**Since:** Baseline / v0.1.0  
**Applies to:** All set types

Include non-NIfTI files with matching basenames:

```yaml
dwi:
  plain_set: {}
  additional_extensions:
    - .bval
    - .bvec
```

**Behavior:**
- Searches for files with same basename but different extensions
- Added to output under extension key (e.g., `bval`, `bvec`)
- JSON sidecars (`.json`) are automatically included

**Example:**
```
sub-01_dwi.nii.gz  ← Primary file
sub-01_dwi.bval    ← Matched by additional_extensions
sub-01_dwi.bvec    ← Matched by additional_extensions  
sub-01_dwi.json    ← Always included automatically
```

**Output:**
```groovy
dwi: [
  nii: Path('/.../sub-01_dwi.nii.gz'),
  bval: Path('/.../sub-01_dwi.bval'),
  bvec: Path('/.../sub-01_dwi.bvec'),
  json: Path('/.../sub-01_dwi.json')
]
```

---

## `include_cross_modal`

**Since:** Baseline / v0.1.0  
**Applies to:** All set types

Include files from other suffixes in the output:

```yaml
bold:
  plain_set: {}
  include_cross_modal:
    - T1w  # Include T1w anatomical reference
    - fmap # Include field maps
```

**Behavior:**
- Cross-modal files added at top level alongside primary suffix
- Only includes files that exist for the grouping key
- Multiple cross-modal suffixes can be specified

**Output:**
```groovy
[
  meta: [subject: 'sub-01', session: 'ses-01', task: 'rest'],
  bold: [
    nii: Path('/.../sub-01_ses-01_task-rest_bold.nii.gz'),
    json: Path('/.../sub-01_ses-01_task-rest_bold.json')
  ],
  T1w: [  // Cross-modal inclusion
    nii: Path('/.../sub-01_ses-01_T1w.nii.gz'),
    json: Path('/.../sub-01_ses-01_T1w.json')
  ],
  fmap: [  // Cross-modal inclusion
    nii: Path('/.../sub-01_ses-01_fmap.nii.gz')
  ]
]
```

**Use Cases:**
- Anatomical references for functional runs
- Field maps shared across modalities
- Registration targets

**See Also:** [Cross-Modal Broadcasting](#cross-modal-broadcasting) section below

---

## `exclude_entities`

**Since:** 0.1.0-beta.6 (Nov 2025) 🆕  
**Applies to:** All set types

Exclude files that have specific entities in their filenames:

```yaml
dwi:
  plain_set:
    exclude_entities:
      - direction       # Exclude sub-01_dir-AP_dwi.nii.gz
      - reconstruction  # Exclude sub-01_rec-dis2d_dwi.nii.gz

MP2RAGE:
  sequential_set:
    by_entity: inversion
    exclude_entities:
      - reconstruction  # Exclude rec-dis2d files from sequencing
```

**Behavior:**
- Files with ANY of the listed entities are filtered OUT
- Case-insensitive entity name matching
- Uses BIDS short entity names (e.g., `dir` for direction, `rec` for reconstruction)

**Example:**
```
Files in dataset:
  sub-01_dwi.nii.gz              ← Matched (no direction entity)
  sub-01_dir-AP_dwi.nii.gz       ← Excluded (has direction entity)
  sub-01_dir-PA_dwi.nii.gz       ← Excluded (has direction entity)
  sub-01_rec-dis2d_dwi.nii.gz    ← Excluded (has reconstruction entity)
```

**Critical for Heterogeneous Datasets:**

When using `suffix_maps_to`, **you MUST use `exclude_entities`** to prevent double-matching:

```yaml
# ❌ WRONG: Files match BOTH configs
dwi:
  plain_set: {}
  
dwi_ap:
  suffix_maps_to: "dwi"
  named_set:
    ap: {direction: dir-AP}

# ✅ CORRECT: Files match only one config
dwi:
  plain_set: {}
  exclude_entities: [direction]  # \u2190 Option at config key level
    
dwi_ap:
  suffix_maps_to: "dwi"  # \u2190 Option at config key level
  named_set:
    ap: {direction: dir-AP}
```

**See Also:** [Heterogeneous Datasets](#advanced-heterogeneous-datasets) section below

---

## `suffix_maps_to`

**Since:** Baseline / v0.1.0  
**Fixed in:** 0.1.0-beta.6 (output now uses config key)  
**Applies to:** All set types

Use files with a different suffix than the config key:

```yaml
dwi_with_reverse:
  suffix_maps_to: "dwi"  # Look for *_dwi.nii.gz files
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
```

**Output (beta.6+):**
```groovy
[
  meta: [...],
  dwi_with_reverse: [  // Config key preserved (not "dwi")
    ap: [nii: Path('/.../sub-01_dir-AP_dwi.nii.gz'), ...],
    pa: [nii: Path('/.../sub-01_dir-PA_dwi.nii.gz'), ...]
  ]
]
```

**Breaking Change in beta.6:**
- **Before beta.6:** Output used file suffix (`dwi`)
- **Since beta.6:** Output uses config key (`dwi_with_reverse`)

**Multiple Configs, Same Suffix:**

Since beta.6, multiple configurations can map to the same suffix:

```yaml
dwi:
  plain_set: {}
  exclude_entities: [direction]  # \u2190 Option at config key level
    
dwi_ap:
  suffix_maps_to: "dwi"  # \u2190 Option at config key level
  named_set: ...
  
dwi_rl:
  suffix_maps_to: "dwi"  # All map to same suffix!
  named_set: ...
```

**Critical Pattern:** Always use `exclude_entities` with `suffix_maps_to` in plain configs!

**See Also:** [Heterogeneous Datasets](#advanced-heterogeneous-datasets) section below

---

## `required`

**Since:** Baseline / v0.1.0  
**Applies to:** Named sets, Mixed sets only

Enforce presence of named groups:

```yaml
dwi:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: [ap, pa]  # BOTH must exist or no emission
```

**Behavior:**
- If ANY required group is missing, entire set is skipped (no channel emission)
- Useful for protocols requiring paired acquisitions
- Without `required`, partial sets are emitted

**Example:**
```
Subject 1: Has both ap and pa → ✅ Emitted
Subject 2: Has only ap       → ❌ Skipped (pa missing)
Subject 3: Has neither       → ❌ Skipped (no groups)
```

**Use Cases:**
- Paired phase-encoding directions (require both AP and PA)
- Multi-shell DWI (require all b-value shells)
- Multi-parametric imaging (require all contrasts)

---

# Set-Specific Options Reference

These options are configured **inside** the set type definition (e.g., `plain_set: {...}`, `named_set: {...}`), unlike global options which are specified at the config key level.

## `parts`

**Since:** Baseline / v0.1.0  
**Applies to:** All set types  
**Location:** Inside set type definition

Filter files by their BIDS part type (e.g., `mag`, `phase`, `real`, `imag`).

### Syntax

```yaml
<suffix>:
  <set_type>:
    parts: [type1, type2, ...]
```

### Behavior

- Only files with matching `part-<type>` entity are included
- Multiple parts can be specified
- Files without a `part-` entity are excluded when `parts` is specified
- Each part type becomes a separate key in the output

### Example: Plain Set with Parts

```yaml
MP2RAGE:
  plain_set:
    parts: ["mag", "phase"]
```

**Output:**
```groovy
[
  meta: [subject: 'sub-01', ...],
  MP2RAGE: [
    mag: [
      nii: Path('/.../sub-01_part-mag_MP2RAGE.nii.gz'),
      json: Path('/.../sub-01_part-mag_MP2RAGE.json')
    ],
    phase: [
      nii: Path('/.../sub-01_part-phase_MP2RAGE.nii.gz'),
      json: Path('/.../sub-01_part-phase_MP2RAGE.json')
    ]
  ]
]
```

### Example: Sequential Set with Parts

```yaml
MP2RAGE:
  sequential_set:
    by_entity: inversion
    parts: ["mag", "phase"]
```

**Output:**
```groovy
[
  meta: [subject: 'sub-01', ...],
  MP2RAGE: [
    mag: [
      nii: [
        Path('/.../sub-01_inv-1_part-mag_MP2RAGE.nii.gz'),
        Path('/.../sub-01_inv-2_part-mag_MP2RAGE.nii.gz')
      ],
      json: [...]
    ],
    phase: [
      nii: [
        Path('/.../sub-01_inv-1_part-phase_MP2RAGE.nii.gz'),
        Path('/.../sub-01_inv-2_part-phase_MP2RAGE.nii.gz')
      ],
      json: [...]
    ]
  ]
]
```

### Example: Named Set with Parts

```yaml
TB1TFL:
  named_set:
    anat:
      acquisition: acq-anat
    famp:
      acquisition: acq-famp
    parts: ["mag", "phase"]  # Applied to each named group
```

**Output:**
```groovy
[
  meta: [subject: 'sub-01', ...],
  TB1TFL: [
    anat: [
      mag: [nii: Path('/.../sub-01_acq-anat_part-mag_TB1TFL.nii.gz'), ...],
      phase: [nii: Path('/.../sub-01_acq-anat_part-phase_TB1TFL.nii.gz'), ...]
    ],
    famp: [
      mag: [nii: Path('/.../sub-01_acq-famp_part-mag_TB1TFL.nii.gz'), ...],
      phase: [nii: Path('/.../sub-01_acq-famp_part-phase_TB1TFL.nii.gz'), ...]
    ]
  ]
]
```

### Use Cases

- **Complex-valued MRI data** - Separate magnitude and phase images
- **MP2RAGE sequences** - Magnitude and phase for each inversion time
- **Quantitative imaging** - Real and imaginary components
- **B1 mapping** - Magnitude and phase for flip angle maps

### Notes

- If no files match the specified parts, the entire set is filtered out
- Parts filtering happens after entity matching and before grouping
- Common part types: `mag`, `phase`, `real`, `imag`

---

# Cross-Modal Broadcasting

Cross-modal broadcasting is used to join files from different suffixes together for filtering and grouping in a set.

## Configuration

```yaml
include_cross_modal: [ ... ]
```

### Example:

```yaml
loop_over:
- subject
- task

mrsref:
  plain_set:
    include_cross_modal:
    - T1w
```

**Output (flat format, default):**

```groovy
[
  meta: [subject: 'sub-01', session: 'NA', task: 'baseline'],
  mrsref: [
    nii: Path("/path/to/bids/sub-01/mrs/sub-01_task-baseline_mrsref.nii.gz"),
    json: Path("/path/to/bids/sub-01/mrs/sub-01_task-baseline_mrsref.json")
  ],
  T1w: [  // Cross-modal inclusion
    nii: Path("/path/to/bids/sub-01/anat/sub-01_T1w.nii.gz"),
    json: Path("/path/to/bids/sub-01/anat/sub-01_T1w.json")
  ]
]
```

**Behavior:**
- Cross-modal files are added at top level alongside primary suffix
- Files are only included if they exist for the grouping key
- Multiple cross-modal suffixes can be specified

## Use Cases

- Sharing anatomical references across functional runs
- Field maps used for multiple tasks
- Registration targets for multi-task studies
- Reference images for spectroscopy (MRS)

**See:** [include_cross_modal](#include_cross_modal) in Global Options for more details.
