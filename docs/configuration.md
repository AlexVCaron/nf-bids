# Overview

The configuration file defines how BIDS files are grouped and emitted through the channel. It follows the same format as the original [bids2nf](https://agah.dev/bids2nf/configuration) workflow.

## Configuration Structure

```yaml
# Define which BIDS entities to loop over (outer grouping)
loop_over:
  - subject
  - session
  - run
  - ...

# Define how each data type (suffix) should be grouped
<suffix_name>:
  <set_type>: { ... }
  required: [ ... ]
  additional_extensions: [ ... ]
  suffix_maps_to: "other_suffix"
```

creates :

```groovy
[
  [
    [<sub>, <ses>, <run>, ...],
    {
      data: { ... },
      filePaths: [ ... ],
      bidsParentDir: "/path/to/bids/dir",
      subject: <sub>,
      session: <ses>,
      run: <run>,
      ...
    }
  ],
  ...
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
>- Entities are extracted from filenames ! _Json sidecar parsing to come in the future_
>- Order matters: defines the hierarchy of grouping
>- Missing entities default to "NA" in the output

# Set Types

## Plain Set

Simple 1:1 mapping of files' suffixes to keys in the data map.

```yaml
<suffix>:
  plain_set: {}
```

### Example:

```yaml
loop_over:
- subject

T1w:
  plain_set: {}
```

**Output:**

```groovy
{
  data: {
    T1w: {
      nii: "sub-01/anat/sub-01_T1w.nii.gz",
      json: "sub-01/anat/sub_01_T1w.json"
    }
  },
  ...
}
```

**Use Cases:**
- Structural images (T1w, T2w)
- Single files per subject/session
- Simple anatomical references

---

## Named Set

Group files by specific entities, in named keys under a specific suffix in the data map.

```yaml
<suffix>:
  named_set:
    <key>:
      <entity>: <value>
```

### Example:

```yaml
loop_over:
- subject

dwi:
  named_set:
    ap:
      direction: dir-AP
    pa:
      direction: dir-PA
  required:
  - ap
  - pa
  additional_extensions:
  - bval
  - bvec
```

**Output:**

```groovy
{
  data: {
    dwi: {
      ap: {
        nii: "sub-01/dwi/sub-01_dir-AP_dwi.nii.gz",
        bval: "sub-01/dwi/sub-01_dir-AP_dwi.bval",
        bvec: "sub-01/dwi/sub-01_dir-AP_dwi.bvec",
        json: "sub-01/dwi/sub-01_dir-AP_dwi.json"
      },
      pa: {
        nii: "sub-01/dwi/sub-01_dir-PA_dwi.nii.gz",
        bval: "sub-01/dwi/sub-01_dir-PA_dwi.bval",
        bvec: "sub-01/dwi/sub-01_dir-PA_dwi.bvec",
        json: "sub-01/dwi/sub_01_dir-PA_dwi.json"
      }
    }
  },
  ...
}
```

**Use Cases:**
- Multiple acquisitions (different b-values, directions)
- Field maps with different acquisitions
- Multi-shell DWI data

>[!NOTE]
>Alternatively, the `suffix_maps_to` instruction can be used, for example if the `dwi` suffix is already in use for base mapping :
>```yaml
>dwi_with_reverse:
>  suffix_maps_to: dwi
>  named_set:
>    ...
>```
>
>**Output:**
>
>```groovy
>{
>  data: {
>    dwi_with_reverse: {
>      ...
>    }
>  }
>}
>```

---

## Sequential Set

Order files by an entity (or multiple entities), in arrays under a specific suffix in the data map.

```yaml
<suffix>:
  sequential_set:
    by_entity: <entity>
    by_entities: [ ... ]
    order: <hierarchical|flat>
```

### Example:

```yaml
loop_over:
- subject

megre:
  sequential_set:
    by_entity: echo
```

**Output:**

```groovy
{
  data: {
    megre: {
      nii: [
        "sub-01/anat/sub-01_echo-01_megre.nii.gz",
        "sub-01/anat/sub-01_echo-02_megre.nii.gz",
        ...
      ],
      json: [
        "sub-01/anat/sub-01_echo-01_megre.json",
        "sub-01/anat/sub-01_echo-02_megre.json",
        ...
      ]
    }
  },
  ...
}
```

### Multiple entities:

```yaml
loop_over:
- subject

t1bsrge:
  sequential_set:
    by_entities:
    - flip
    - inversion
    order: hierarchical
```

**Output:**

```groovy
{
  data: {
    t1bsrge: {
      nii: [
        [
          "sub-01/fmap/sub-01_inv-01_flip-01_T1BSRGE.nii.gz"
        ],
        [
          "sub-01/fmap/sub-01_inv-02_flip-02_T1BSRGE.nii.gz"
        ],
        ...
      ],
      json: [
        [
          "sub-01/fmap/sub-01_inv-01_flip-01_T1BSRGE.json"
        ],
        [
          "sub-01/fmap/sub-01_inv-02_flip-02_T1BSRGE.json"
        ],
        ...
      ]
    }
  },
  ...
}
```

**Use Cases:**
- Multi-echo fMRI (ordered by echo)
- Time-series data with ordered runs
- Sequential measurements

---

## Mixed Set

Nested combination of named filtering and sequential ordering.

```yaml
<suffix>:
  mixed_set:
    named_dimension: <entity>
    sequential_dimension: <entity>
    named_groups: { ... }
```

### Example:

```yaml
loop_over:
- subject

mpm:
  mixed_set:
    named_dimension: acquisition
    sequential_dimension: echo
    named_groups:
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
    required:
    - MTw
    - PDw
    - T1w
```

**Output:**

```groovy
{
  data: {
    mpm: {
      MTw: {
        nii: [
          "sub-01/anat/sub-01_acq-MTw_echo-01_flip-1_mt-on_MPM.nii.gz",
          "sub-01/anat/sub-01_acq-MTw_echo-02_flip-1_mt-on_MPM.nii.gz",
          ...
        ],
        json: [
          "sub-01/anat/sub-01_acq-MTw_echo-01_flip-1_mt-on_MPM.json",
          "sub-01/anat/sub-01_acq-MTw_echo-02_flip-1_mt-on_MPM.json",
          ...
        ]
      },
      PDw: {
        nii: [
          "sub-01/anat/sub-01_acq-PDw_echo-01_flip-1_mt-off_MPM.nii.gz",
          "sub-01/anat/sub-01_acq-PDw_echo-02_flip-1_mt-off_MPM.nii.gz",
          ...
        ],
        json: [
          "sub-01/anat/sub-01_acq-PDw_echo-01_flip-1_mt-off_MPM.json",
          "sub-01/anat/sub-01_acq-PDw_echo-02_flip-1_mt-off_MPM.json",
          ...
        ]
      },
      T1w: {
        nii: [
          "sub-01/anat/sub-01_acq-T1w_echo-01_flip-2_mt-off_MPM.nii.gz",
          "sub-01/anat/sub-01_acq-T1w_echo-02_flip-2_mt-off_MPM.nii.gz",
          ...
        ],
        json: [
          "sub-01/anat/sub-01_acq-T1w_echo-01_flip-2_mt-off_MPM.json",
          "sub-01/anat/sub-01_acq-T1w_echo-02_flip-2_mt-off_MPM.json",
          ...
        ]
      },
    }
  },
  ...
}
```

**Use Cases:**
- Field maps with multiple acquisitions and directions
- Complex multi-parametric imaging
- Nested grouping requirements

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

**Output:**

```groovy
{
  data: {
    mrsref: {
      nii: "sub-01/mrs/sub-01_task-baseline_mrsref.nii.gz"
      json: "sub-01/mrs/sub-01_task-baseline_mrsref.json"
    },
    T1w: {
      nii: "sub-01/anat/sub-01_T1w.nii.gz"
      json: "sub-01/anat/sub-01_T1w.json"
    }
  },
  ...
}
```

## Use Cases

- Sharing anatomical references across functional runs
- Field maps used for multiple tasks
- Registration targets for multi-task studies
