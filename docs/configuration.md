# Configuration Guide

This guide covers the YAML configuration format used by the nf-bids plugin.

## Overview

The configuration file defines how BIDS files should be grouped and emitted through the channel. It follows the same format as the original [bids2nf](https://github.com/AlexVCaron/bids2nf) workflow.

## Configuration Structure

```yaml
# Define which BIDS entities to loop over (outer grouping)
loop_over:
  - subject
  - session
  - run
  - task

# Define how each data type (suffix) should be grouped
<suffix_name>:
  <set_type>:
    entities:
      <entity_key>: <entity_value>
    # Additional options depending on set_type
```

## Loop Over Entities

The `loop_over` section defines the outer grouping structure:

```yaml
loop_over:
  - subject
  - session
  - run
  - task
```

**Valid Entities:**
- `subject` - BIDS subject identifier (sub-01, sub-02, etc.)
- `session` - Session identifier (ses-01, ses-baseline, etc.)
- `run` - Run number (run-01, run-02, etc.)
- `task` - Task name (task-rest, task-nback, etc.)
- `acquisition` - Acquisition type (acq-highres, acq-lowres, etc.)
- `direction` - Phase encoding direction (dir-AP, dir-PA, etc.)
- `echo` - Echo number (echo-1, echo-2, etc.)
- Any other valid BIDS entity

**Notes:**
- Order matters: defines the hierarchy of grouping
- Missing entities default to "NA" in the output
- Cross-modal broadcasting uses `task` entity specially

## Set Types

### Plain Set

Simple 1:1 mapping of files to data slots.

```yaml
T1w:
  plain_set:
    entities:
      suffix: T1w
      acquisition: highres  # Optional: filter by entity
```

**Output:**
```groovy
enrichedData.data.T1w = '/path/to/sub-01_T1w.nii.gz'
```

**Use Cases:**
- Structural images (T1w, T2w)
- Single files per subject/session
- Simple anatomical references

---

### Named Set

Group files by a specific entity, creating a map.

```yaml
dwi:
  named_set:
    entities:
      suffix: dwi
    group_by: acquisition
```

**Output:**
```groovy
enrichedData.data.dwi = [
    'acq01': '/path/to/sub-01_acq-acq01_dwi.nii.gz',
    'acq02': '/path/to/sub-01_acq-acq02_dwi.nii.gz'
]
```

**Use Cases:**
- Multiple acquisitions (different b-values, directions)
- Field maps with different acquisitions
- Multi-shell DWI data

---

### Sequential Set

Order files by an entity, creating an array.

```yaml
bold:
  sequential_set:
    entities:
      suffix: bold
      task: rest
    sequence_by: echo
```

**Output:**
```groovy
enrichedData.data.bold = [
    '/path/to/sub-01_task-rest_echo-1_bold.nii.gz',
    '/path/to/sub-01_task-rest_echo-2_bold.nii.gz',
    '/path/to/sub-01_task-rest_echo-3_bold.nii.gz'
]
```

**Use Cases:**
- Multi-echo fMRI (ordered by echo)
- Time-series data with ordered runs
- Sequential measurements

---

### Mixed Set

Nested combination of named and sequential sets.

```yaml
fmap:
  mixed_set:
    entities:
      suffix: epi
    group_by: acquisition
    sequence_by: direction
```

**Output:**
```groovy
enrichedData.data.fmap = [
    'acq01': [
        '/path/to/sub-01_acq-acq01_dir-AP_epi.nii.gz',
        '/path/to/sub-01_acq-acq01_dir-PA_epi.nii.gz'
    ],
    'acq02': [
        '/path/to/sub-01_acq-acq02_dir-LR_epi.nii.gz',
        '/path/to/sub-01_acq-acq02_dir-RL_epi.nii.gz'
    ]
]
```

**Use Cases:**
- Field maps with multiple acquisitions and directions
- Complex multi-parametric imaging
- Nested grouping requirements

## Cross-Modal Broadcasting

### Configuration

```yaml
cross_modal_broadcasting:
  - T1w
  - T2w
  - flair
```

### Behavior

Files with `task="NA"` (task-independent) are shared across all task-specific channels:

**Example:**
- Subject has: `T1w` (task=NA), `bold` (task=rest), `bold` (task=nback)
- Output: Both `task=rest` and `task=nback` channels include the same T1w

**Requirements:**
- `task` must be in `loop_over`
- Data type must be listed in `cross_modal_broadcasting`
- Source files must have no task entity (`_task-` not in filename)

### Use Cases

- Sharing anatomical references across functional runs
- Field maps used for multiple tasks
- Registration targets for multi-task studies

## Complete Example

```yaml
# Grouping hierarchy
loop_over:
  - subject
  - session
  - run
  - task

# Enable cross-modal broadcasting
cross_modal_broadcasting:
  - T1w
  - T2w
  - fmap

# Anatomical images (task-independent)
T1w:
  plain_set:
    entities:
      suffix: T1w

T2w:
  plain_set:
    entities:
      suffix: T2w

# Field maps (grouped by acquisition, sequenced by direction)
fmap:
  mixed_set:
    entities:
      suffix: epi
    group_by: acquisition
    sequence_by: direction

# Functional data (multi-echo, sequenced)
bold:
  sequential_set:
    entities:
      suffix: bold
    sequence_by: echo

# Diffusion data (multi-shell, grouped by acquisition)
dwi:
  named_set:
    entities:
      suffix: dwi
    group_by: acquisition
```

## Validation

The plugin validates configurations against a JSON schema. Common errors:

**Missing Required Fields:**
```yaml
# ERROR: No set type specified
T1w:
  entities:
    suffix: T1w
```

**Invalid Set Type:**
```yaml
# ERROR: 'custom_set' is not a valid set type
T1w:
  custom_set:
    entities:
      suffix: T1w
```

**Missing Required Parameters:**
```yaml
# ERROR: named_set requires 'group_by'
dwi:
  named_set:
    entities:
      suffix: dwi
```

**Invalid Entity Names:**
```yaml
# ERROR: 'modality' is not a BIDS entity
loop_over:
  - modality  # Should use 'suffix' or valid BIDS entity
```

## Schema Location

The configuration schema is defined in:
```
../../config/schemas/bids2nf.schema.yaml
```

This schema is shared with the original bids2nf workflow for consistency.

## Related Documentation

- [API Reference](api.md) - Channel factory API
- [Examples](examples.md) - Configuration examples
- [Implementation Guide](implementation.md) - Developer guide
