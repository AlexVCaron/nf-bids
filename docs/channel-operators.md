# Channel Operators - Closure-Based Extensions

**nf-bids Plugin** - Version 0.1.0-beta.5

This document provides comprehensive documentation for the closure-based channel grouping operators provided by the nf-bids plugin. These operators extend Nextflow's built-in channel operators with flexible, closure-based key extraction for complex data structures.

---

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Quick Start](#quick-start)
4. [Operators](#operators)
   - [groupTupleBy](#grouptupleby)
   - [joinBy](#joinby)
   - [combineBy](#combineby)
5. [Advanced Topics](#advanced-topics)
6. [Performance Characteristics](#performance-characteristics)
7. [Troubleshooting](#troubleshooting)

---

## Overview

### Why Closure-Based Operators?

Nextflow's built-in channel operators (`groupTuple`, `join`, `combine`) work with **index-based** key extraction, requiring channel items to be tuples where the grouping key is at a specific position:

```nextflow
// Built-in operators require tuple structure
channel
    .of(['sub-01', 'file1.nii'], ['sub-01', 'file2.nii'])
    .groupTuple(by: 0)  // Group by first element (index 0)
```

This works well for simple data but becomes limiting when working with:
- **Complex structures** (maps, nested data, BIDS datasets)
- **Computed keys** (derived from multiple fields)
- **Semantic grouping** (by field names instead of indices)

### Closure-Based Solution

The nf-bids plugin provides **closure-based** alternatives that extract keys dynamically using Groovy closures:

```nextflow
// Closure-based operators work with any structure
channel
    .of([subject: 'sub-01', file: 'file1.nii'],
        [subject: 'sub-01', file: 'file2.nii'])
    .groupTupleBy { it.subject }  // Group by field name
```

**Benefits:**
- ✅ Works with maps, objects, and any data structure
- ✅ Semantic field access (`it.subject` vs `it[0]`)
- ✅ Supports computed keys (multiple fields, transformations)
- ✅ More readable and maintainable workflows
- ✅ Type-safe with IDE autocomplete

---

## Installation

### Prerequisites

- Nextflow 24.10.0 or later
- Java 11 or later

### Plugin Configuration

Add the plugin to your `nextflow.config`:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.4'
}
```

### Importing Operators

In your workflow script, explicitly import the operators you need:

```nextflow
#!/usr/bin/env nextflow
nextflow.enable.dsl=2

include { groupTupleBy } from 'plugin/nf-bids'
include { joinBy } from 'plugin/nf-bids'
include { combineBy } from 'plugin/nf-bids'

workflow {
    // Use operators here
}
```

**Important:** The `include` statement is required even though the plugin is loaded in the config. Without it, you'll get "Missing process or function" errors.

---

## Quick Start

### Example 1: Group BIDS Files by Subject

```nextflow
include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    Channel
        .of(
            [subject: 'sub-01', modality: 'T1w', file: 'anat1.nii'],
            [subject: 'sub-01', modality: 'T2w', file: 'anat2.nii'],
            [subject: 'sub-02', modality: 'T1w', file: 'anat3.nii']
        )
        .groupTupleBy { it.subject }
        .view()
}

// Output:
// [sub-01, [[subject:sub-01, modality:T1w, file:anat1.nii], 
//           [subject:sub-01, modality:T2w, file:anat2.nii]]]
// [sub-02, [[subject:sub-02, modality:T1w, file:anat3.nii]]]
```

### Example 2: Join Anatomical and Functional Scans

```nextflow
include { joinBy } from 'plugin/nf-bids'

workflow {
    anatomical = Channel.of(
        [subject: 'sub-01', file: 't1.nii'],
        [subject: 'sub-02', file: 't1.nii']
    )
    
    functional = Channel.of(
        [subject: 'sub-01', file: 'bold.nii'],
        [subject: 'sub-02', file: 'bold.nii']
    )
    
    anatomical
        .joinBy(functional) { it.subject }
        .view()
}

// Output:
// [[subject:sub-01, file:t1.nii], [subject:sub-01, file:bold.nii]]
// [[subject:sub-02, file:t1.nii], [subject:sub-02, file:bold.nii]]
```

### Example 3: Match Subjects with Their Sessions

```nextflow
include { combineBy } from 'plugin/nf-bids'

workflow {
    // Match subjects with their sessions by subject ID
    subjects = Channel.of(
        [subject: 'sub-01', age: 25],
        [subject: 'sub-02', age: 30],
        [subject: 'sub-03', age: 28]
    )
    sessions = Channel.of(
        [subject: 'sub-01', session: 'ses-01'],
        [subject: 'sub-01', session: 'ses-02'],
        [subject: 'sub-02', session: 'ses-01'],
        [subject: 'sub-03', session: 'ses-01']
    )
    
    subjects
        .combineBy(sessions, { it.subject })
        .view { key, subj, sess -> 
            "Subject ${key} (age ${subj.age}): session ${sess.session}" 
        }
}

// Output:
// Subject sub-01 (age 25): session ses-01
// Subject sub-01 (age 25): session ses-02
// Subject sub-02 (age 30): session ses-01
// Subject sub-03 (age 28): session ses-01
```

---

## Operators

## groupTupleBy

Group channel items by dynamically extracted keys using a closure.

### Signature

```groovy
channel.groupTupleBy(keyExtractor)
channel.groupTupleBy(keyExtractor, options)
```

**Parameters:**
- `keyExtractor` (Closure): Function that receives an item and returns its grouping key
- `options` (Map, optional): Configuration options

**Options:**
- `size` (Integer): Emit group when it reaches this size (default: collect all)
- `sort` (Boolean|Closure|Comparator): Sort items in each group
  - `true`: Natural sort order
  - Closure: Custom sort logic `{ a, b -> ... }`
  - Comparator: Java comparator
- `remainder` (Boolean): Emit incomplete groups at end (default: `true`)

**Returns:** Channel emitting `[key, [items]]` tuples

### Basic Usage

#### Example 1: Group by Single Field

```nextflow
Channel
    .of(
        [subject: 'sub-01', session: 'ses-01', file: 'a.nii'],
        [subject: 'sub-01', session: 'ses-02', file: 'b.nii'],
        [subject: 'sub-02', session: 'ses-01', file: 'c.nii']
    )
    .groupTupleBy { it.subject }
    .view()

// Output:
// [sub-01, [[subject:sub-01, session:ses-01, file:a.nii], 
//           [subject:sub-01, session:ses-02, file:b.nii]]]
// [sub-02, [[subject:sub-02, session:ses-01, file:c.nii]]]
```

#### Example 2: Group by Multiple Fields (Composite Key)

```nextflow
Channel
    .of(
        [subject: 'sub-01', session: 'ses-01', run: 1, file: 'a.nii'],
        [subject: 'sub-01', session: 'ses-01', run: 2, file: 'b.nii'],
        [subject: 'sub-01', session: 'ses-02', run: 1, file: 'c.nii']
    )
    .groupTupleBy { [it.subject, it.session] }  // Returns list = composite key
    .view()

// Output:
// [[sub-01, ses-01], [[subject:sub-01, session:ses-01, run:1, file:a.nii],
//                     [subject:sub-01, session:ses-01, run:2, file:b.nii]]]
// [[sub-01, ses-02], [[subject:sub-01, session:ses-02, run:1, file:c.nii]]]
```

**Note:** When the closure returns a List, it's automatically wrapped in a `CompositeKey` for proper equality semantics.

#### Example 3: Group by Computed Key

```nextflow
Channel
    .of(
        [subject: 'sub-01', value: 15],
        [subject: 'sub-02', value: 25],
        [subject: 'sub-03', value: 35],
        [subject: 'sub-04', value: 18]
    )
    .groupTupleBy { 
        // Group into age ranges
        it.value < 20 ? 'young' : it.value < 30 ? 'adult' : 'senior'
    }
    .view()

// Groups subjects into age categories
```

### Advanced Usage

#### Example 4: Sort Items Within Groups

```nextflow
Channel
    .of(
        [subject: 'sub-01', run: 3, file: 'c.nii'],
        [subject: 'sub-01', run: 1, file: 'a.nii'],
        [subject: 'sub-01', run: 2, file: 'b.nii']
    )
    .groupTupleBy({ it.subject }, [sort: { a, b -> a.run <=> b.run }])
    .view()

// Output (sorted by run number):
// [sub-01, [[subject:sub-01, run:1, file:a.nii],
//           [subject:sub-01, run:2, file:b.nii],
//           [subject:sub-01, run:3, file:c.nii]]]
```

#### Example 5: Emit Groups at Fixed Size

```nextflow
Channel
    .of(
        [batch: 'A', item: 1],
        [batch: 'A', item: 2],
        [batch: 'A', item: 3],
        [batch: 'A', item: 4],
        [batch: 'A', item: 5]
    )
    .groupTupleBy({ it.batch }, [size: 2])
    .view()

// Output (emits when group reaches size 2):
// [A, [[batch:A, item:1], [batch:A, item:2]]]
// [A, [[batch:A, item:3], [batch:A, item:4]]]
// [A, [[batch:A, item:5]]]  // remainder emitted at end
```

#### Example 6: Discard Incomplete Groups

```nextflow
Channel
    .of(
        [batch: 'A', item: 1],
        [batch: 'A', item: 2],
        [batch: 'A', item: 3],
        [batch: 'B', item: 1]  // Only 1 item in batch B
    )
    .groupTupleBy({ it.batch }, [size: 2, remainder: false])
    .view()

// Output (incomplete batch B discarded):
// [A, [[batch:A, item:1], [batch:A, item:2]]]
```

### Use Cases

**1. Group BIDS Files by Subject**
```nextflow
bids_files.groupTupleBy { it.subject }
```

**2. Group by Subject and Session**
```nextflow
bids_files.groupTupleBy { [it.subject, it.session] }
```

**3. Group by Acquisition Parameters**
```nextflow
scans.groupTupleBy { "${it.modality}_${it.resolution}" }
```

**4. Group QC Results by Status**
```nextflow
qc_results.groupTupleBy { it.passed ? 'passed' : 'failed' }
```

**5. Batch Processing**
```nextflow
large_dataset
    .groupTupleBy({ 'batch' }, [size: 100])
    .map { key, items -> process_batch(items) }
```

### Error Handling

**Null Keys:**
Items that produce `null` keys are automatically skipped:

```nextflow
Channel
    .of(
        [subject: 'sub-01', file: 'a.nii'],
        [subject: null, file: 'b.nii'],  // Skipped
        [subject: 'sub-02', file: 'c.nii']
    )
    .groupTupleBy { it.subject }
    .view()

// Only sub-01 and sub-02 groups are emitted
```

**Exception in Closure:**
If the keyExtractor throws an exception, the workflow fails with a clear error:

```nextflow
// This will fail if 'subject' field is missing
channel.groupTupleBy { it.subject.toUpperCase() }
```

Use safe navigation to handle missing fields:
```nextflow
channel.groupTupleBy { it.subject?.toUpperCase() ?: 'UNKNOWN' }
```

---

## joinBy

Join two channels by dynamically extracted keys using closures.

### Signature

```groovy
leftChannel.joinBy(rightChannel, keyExtractor)
leftChannel.joinBy(rightChannel, keyExtractor, options)
leftChannel.joinBy(rightChannel, leftKeyExtractor, rightKeyExtractor)
leftChannel.joinBy(rightChannel, leftKeyExtractor, rightKeyExtractor, options)
```

**Parameters:**
- `rightChannel` (DataflowReadChannel): The right channel to join with
- `keyExtractor` (Closure): Key extractor for both channels
- `leftKeyExtractor` (Closure): Key extractor for left channel items
- `rightKeyExtractor` (Closure): Key extractor for right channel items (defaults to `leftKeyExtractor`)
- `options` (Map, optional): Configuration options

**Options:**
- `remainder` (Boolean): Emit unmatched items with null partner (default: `false`)
  - `false`: Inner join (only matched pairs)
  - `true`: Outer join (all items, nulls for unmatched)

**Returns:** Channel emitting `[key, leftItem, rightItem]` tuples

**Important:** Like Nextflow's standard `join` operator, `joinBy` emits **3-element tuples** `[key, left, right]`. The key is included as the first element to maintain functional compatibility with the base operator.

### Basic Usage

#### Example 1: Simple Join (Same Key for Both Channels)

```nextflow
anatomical = Channel.of(
    [subject: 'sub-01', type: 'T1w', file: 't1.nii'],
    [subject: 'sub-02', type: 'T1w', file: 't1.nii']
)

functional = Channel.of(
    [subject: 'sub-01', type: 'BOLD', file: 'bold.nii'],
    [subject: 'sub-02', type: 'BOLD', file: 'bold.nii']
)

anatomical
    .joinBy(functional) { it.subject }
    .view { key, left, right -> 
        "Matched ${key}: ${left.type} with ${right.type}"
    }

// Output:
// Matched sub-01: T1w with BOLD
// Matched sub-02: T1w with BOLD
```

#### Example 2: Join with Different Key Extractors

```nextflow
scans = Channel.of(
    [scan_id: 'scan001', subject: 'sub-01', file: 'scan.nii'],
    [scan_id: 'scan002', subject: 'sub-02', file: 'scan.nii']
)

metadata = Channel.of(
    [participant_id: 'sub-01', age: 25, sex: 'F'],
    [participant_id: 'sub-02', age: 30, sex: 'M']
)

scans
    .joinBy(metadata, 
            { it.subject },           // Left: extract 'subject'
            { it.participant_id })    // Right: extract 'participant_id'
    .view { key, scan, meta ->
        "Scan ${scan.scan_id} (subject ${key}): ${meta.age}yo ${meta.sex}"
    }

// Output:
// Scan scan001: 25yo F
// Scan scan002: 30yo M
```

#### Example 3: Join by Composite Key

```nextflow
acquisitions = Channel.of(
    [subject: 'sub-01', session: 'ses-01', file: 'acq.nii'],
    [subject: 'sub-01', session: 'ses-02', file: 'acq.nii']
)

derivatives = Channel.of(
    [subject: 'sub-01', session: 'ses-01', file: 'deriv.nii'],
    [subject: 'sub-01', session: 'ses-02', file: 'deriv.nii']
)

acquisitions
    .joinBy(derivatives) { [it.subject, it.session] }
    .view()

// Matches based on both subject AND session
```

### Advanced Usage

#### Example 4: Cartesian Product with Duplicate Keys

When multiple items share the same key, `joinBy` produces the **cartesian product** (all combinations):

```nextflow
images = Channel.of(
    [subject: 'sub-01', run: 1, file: 'run1.nii'],
    [subject: 'sub-01', run: 2, file: 'run2.nii']
)

masks = Channel.of(
    [subject: 'sub-01', type: 'brain', file: 'brain_mask.nii'],
    [subject: 'sub-01', type: 'tissue', file: 'tissue_mask.nii']
)

images
    .joinBy(masks) { it.subject }
    .view { key, img, mask ->
        "Apply ${mask.type} mask to run ${img.run} (subject: ${key})"
    }

// Output (4 combinations):
// Apply brain mask to run 1
// Apply brain mask to run 2
// Apply tissue mask to run 1
// Apply tissue mask to run 2
```

**Performance Note:** With many duplicates, the cartesian product can be large. For 1:1 joins, ensure keys are unique.

#### Example 5: Outer Join (Include Unmatched Items)

```nextflow
left = Channel.of(
    [id: 'A', val: 1],
    [id: 'B', val: 2],
    [id: 'C', val: 3]  // No match in right
)

right = Channel.of(
    [id: 'A', val: 10],
    [id: 'B', val: 20],
    [id: 'D', val: 40]  // No match in left
)

left
    .joinBy(right, { it.id }, [remainder: true])
    .view { l, r ->
        def leftId = l?.id ?: 'null'
        def rightId = r?.id ?: 'null'
        "Joined: left=${leftId}, right=${rightId}"
    }

// Output:
// Joined: left=A, right=A     (matched)
// Joined: left=B, right=B     (matched)
// Joined: left=C, right=null  (unmatched left)
// Joined: left=null, right=D  (unmatched right)
```

### Use Cases

**1. Join Scans with Clinical Data**
```nextflow
scans.joinBy(participants) { it.subject }
```

**2. Match Anatomical with Functional**
```nextflow
t1w.joinBy(bold) { [it.subject, it.session] }
```

**3. Combine Images with Segmentations**
```nextflow
images.joinBy(segmentations, 
              { it.scan_id }, 
              { it.reference_scan_id })
```

**4. Link Derivatives to Raw Data**
```nextflow
raw_data.joinBy(derivatives) { 
    "${it.subject}_${it.modality}" 
}
```

### Performance Characteristics

**Time Complexity:**
- **Best case (unique keys)**: O(n + m) where n = left size, m = right size
- **Worst case (all duplicate keys)**: O(n × m) cartesian product

**Memory Usage:**
- Buffers **all items** from both channels in memory
- For large datasets with many duplicates, consider filtering first

**Recommendations:**
- ✅ Optimal for typical BIDS workflows (low duplicate rate)
- ⚠️ Use with caution for 1:many or many:many joins with high duplication
- ✅ Consider `groupTupleBy` + `join` for complex join logic

---

## combineBy

> **⚠️ BREAKING CHANGE in v0.1.0-beta.5:** API changed from filter predicates to key extraction.

Combine two channels by extracting and matching keys, emitting the cartesian product of items within each key group.

Similar to Nextflow's `combine(by:)` operator but uses **closures for key extraction** instead of tuple indices.

### Signature

```groovy
// Single-key extractor (applies to both channels)
leftChannel.combineBy(rightChannel, keyExtractor)
leftChannel.combineBy(rightChannel, keyExtractor, options)

// Asymmetric extractors (left/right closures)
leftChannel.combineBy(rightChannel, leftKeyExtractor, rightKeyExtractor)
leftChannel.combineBy(rightChannel, leftKeyExtractor, rightKeyExtractor, options)
```

**Parameters:**
- `rightChannel` (DataflowReadChannel): The right channel to combine with
- `leftKeyExtractor` (Closure): Extracts key from left channel items (`leftItem -> key`)
- `rightKeyExtractor` (Closure): Extracts key from right channel items (`rightItem -> key`). If omitted, it defaults to `leftKeyExtractor` (i.e. the same extractor will be applied to both channels).
- `options` (Map, optional): Configuration options (reserved for future: `remainder`)

**Returns:** Channel emitting `[key, leftItem, rightItem]` tuples (includes the matching key)

**Key Features:**
- ✅ Extracts keys using closures (supports computed/composite keys)
- ✅ Emits **[key, leftItem, rightItem]** tuples (consistent with `joinBy`)
- ✅ Produces **full cartesian product** within matching key groups
- ✅ Drops unmatched keys (inner join semantics)
 - ⚠️ If you intend to produce a full cartesian product (all pairs) regardless of keys, prefer Nextflow's built-in `cross` operator for clarity: `left.cross(right)`.

### Basic Usage

#### Example 1: Combine Subjects with Sessions

```nextflow
subjects = Channel.of(
    [id: 'sub-01', age: 25],
    [id: 'sub-02', age: 30]
)

sessions = Channel.of(
    [id: 'sub-01', session: 'ses-01'],
    [id: 'sub-01', session: 'ses-02'],
    [id: 'sub-02', session: 'ses-01']
)

subjects.combineBy(
    sessions,
    { it.id },      // extract subject ID from left
    { it.id }       // extract subject ID from right
)
.view { key, subj, sess -> 
    "Key=${key}: Subject(age=${subj.age}) × Session(${sess.session})"
}

// Output:
// Key=sub-01: Subject(age=25) × Session(ses-01)
// Key=sub-01: Subject(age=25) × Session(ses-02)
// Key=sub-02: Subject(age=30) × Session(ses-01)
```

#### Example 2: Cartesian Product Within Groups

```nextflow
scans = Channel.of(
    [subject: 'sub-01', scan: 'T1w'],
    [subject: 'sub-01', scan: 'T2w'],  // 2 scans for sub-01
    [subject: 'sub-02', scan: 'dwi']
)

params = Channel.of(
    [subject: 'sub-01', tr: 2000],
    [subject: 'sub-01', tr: 3000],     // 2 TR values for sub-01
    [subject: 'sub-02', tr: 2500]
)

scans.combineBy(
    params,
    { it.subject }
)
.view { key, scan, param ->
    "Subject ${key}: ${scan.scan} with TR=${param.tr}ms"
}

// Output (sub-01 produces 2×2=4 combinations):
// Subject sub-01: T1w with TR=2000ms
// Subject sub-01: T1w with TR=3000ms
// Subject sub-01: T2w with TR=2000ms
// Subject sub-01: T2w with TR=3000ms
// Subject sub-02: dwi with TR=2500ms
```

**Note:** Items with the same key produce a **cartesian product**. In the example above, `sub-01` has 2 scans and 2 TR values, resulting in 2 × 2 = 4 output combinations for that subject.

### Advanced Usage

#### Example 3: Conditional Pairing

```nextflow
scans = Channel.of(
    [subject: 'sub-01', modality: 'T1w', file: 't1.nii'],
    [subject: 'sub-02', modality: 'T1w', file: 't1.nii'],
    [subject: 'sub-03', modality: 'T2w', file: 't2.nii']
)

contrasts = Channel.of(
    [name: 'GM', requires: 'T1w'],
    [name: 'WM', requires: 'T1w'],
    [name: 'CSF', requires: 'T2w']
)

scans
    .combineBy(contrasts, { it.modality }, { it.requires })
    .view { key, scan, contrast ->
        "Extract ${contrast.name} from ${scan.subject} ${scan.modality}"
    }

// Output (only modality-compatible combinations):
// Extract GM from sub-01 T1w
// Extract WM from sub-01 T1w
// Extract GM from sub-02 T1w
// Extract WM from sub-02 T1w
// Extract CSF from sub-03 T2w
```

#### Example 4: Multi-Condition Filtering

```nextflow
datasets = Channel.of(
    [id: 'ds001', subjects: 50, sessions: 2],
    [id: 'ds002', subjects: 100, sessions: 1],
    [id: 'ds003', subjects: 25, sessions: 3]
)

pipelines = Channel.of(
    [name: 'quick', max_subjects: 60, max_sessions: 2],
    [name: 'full', max_subjects: 150, max_sessions: 5]
)

datasets
    // Group datasets by a simple size class (small/large) and match pipelines accordingly
    .combineBy(
        pipelines,
        { ds -> ds.subjects < 60 ? 'small' : 'large' },
        { p -> p.max_subjects <= 60 ? 'small' : 'large' }
    )
    .view { key, ds, pipe ->
        "Run ${pipe.name} pipeline on ${ds.id} (group=${key})"
    }

// Output (only feasible combinations):
// Run quick on ds001
// Run full on ds001
// Run full on ds002
// Run quick on ds003
// Run full on ds003
```

#### Example 5: Asymmetric Combinations

```nextflow
// Asymmetric combinations: if you need to apply parameter sets to specific dataset types,
// prefer `combineBy` with a meaningful key extractor that maps parameters to dataset classes.
parameters = Channel.of(
    [smoothing: 4, min_size: 10],
    [smoothing: 6, min_size: 50]
)

datasets = Channel.of(
    [name: 'validation', size: 10],
    [name: 'training', size: 100],
    [name: 'testing', size: 25]
)

parameters
    .combineBy(datasets,
        { p -> p.min_size <= 20 ? 'small' : 'large' },
        { d -> d.size <= 20 ? 'small' : 'large' }
    )
    .view { key, params, dataset ->
        "Test smoothing=${params.smoothing} on ${dataset.name} (group=${key})"
    }
```

### Use Cases

**1. All Subjects × All Analysis Types**
```nextflow
 subjects.combineBy(analysis_types, { it })
```

**2. Parameter Grid Search**
```nextflow
learning_rates
    .combineBy(batch_sizes, { it })
    .combineBy(optimizers, { it })
```

**3. Quality-Based Processing**
```nextflow
images
    .combineBy(pipelines, { it.modality })
    .filter { key, img, pipe -> img.quality >= pipe.min_quality }
```

**4. Cross-Dataset Validation**
```nextflow
train_sets
    .combineBy(test_sets,
        { train -> train.size <= 50 ? 'small' : 'large' },
        { test -> test.size <= 50 ? 'small' : 'large' }
    )
    .filter { key, train, test -> train.id != test.id }
```

**5. Conditional Workflows**
```nextflow
scans
    .combineBy(protocols, { it.modality })
    .filter { key, scan, proto -> 
        scan.modality == proto.modality &&
        scan.resolution >= proto.min_resolution
    }
```

### Performance Characteristics

**Time Complexity:** O(n × m) where n = left size, m = right size

**Memory Usage:**
- Buffers **all items** from both channels
- Emits combinations on-the-fly as items arrive
- Peak memory: size of both complete channels

**Recommendations:**
- ✅ Excellent for parameter sweeps, grid searches
- ✅ Use filters to reduce output size
- ⚠️ Be cautious with very large channels (e.g., 1000 × 1000 = 1M combinations)
- ✅ Consider generating combinations upstream if possible

---

## Advanced Topics

### Composite Keys

All three operators support **composite keys** (multi-field grouping/joining):

```nextflow
// Group by subject AND session
channel.groupTupleBy { [it.subject, it.session] }

// Join by subject AND modality
left.joinBy(right) { [it.subject, it.modality] }
```

**How It Works:**
- When a closure returns a **List**, it's automatically wrapped in a `CompositeKey`
- `CompositeKey` provides correct `equals()` and `hashCode()` for list-based keys
- Keys are compared element-by-element: `['sub-01', 'ses-01'] == ['sub-01', 'ses-01']`

**Example: Three-Part Key**
```nextflow
channel.groupTupleBy { 
    [it.subject, it.session, it.acquisition] 
}
```

### Null Handling

**Null Keys:**
- Items producing `null` keys are **silently skipped**
- Logged at TRACE level: `groupTupleBy: item produced null key, skipping: [...]`

**Safe Navigation:**
Use Groovy's safe navigation (`?.`) to handle missing fields:

```nextflow
// If 'session' is missing, returns null → item skipped
channel.groupTupleBy { it.session }

// Provide default for missing fields
channel.groupTupleBy { it.session ?: 'no-session' }

// Safe navigation through nested fields
channel.groupTupleBy { it.metadata?.acquisition?.date }
```

**Null in Join Results (Outer Join):**
```nextflow
left.joinBy(right, { it.id }, [remainder: true])
    .view { l, r ->
        def leftVal = l?.value ?: 'NONE'
        def rightVal = r?.value ?: 'NONE'
        "Left: ${leftVal}, Right: ${rightVal}"
    }
```

### Exception Handling

**Closure Exceptions:**
If a key extractor throws an exception, the workflow **fails immediately** with a detailed error:

```nextflow
// This will fail if 'subject' field is missing
channel.groupTupleBy { it.subject.toUpperCase() }

// Error:
// groupTupleBy: keyExtractor failed for item [{file:'a.nii'}]: 
//   No such property: subject for class: java.util.LinkedHashMap
```

**Best Practice:**
Use defensive coding for complex extractors:

```nextflow
channel.groupTupleBy { item ->
    try {
        return extractComplexKey(item)
    } catch (Exception e) {
        log.warn("Failed to extract key from ${item}: ${e.message}")
        return null  // Skip this item
    }
}
```

### Sorting Options

`groupTupleBy` supports three sorting modes:

**1. Natural Sort (Boolean)**
```nextflow
channel.groupTupleBy({ it.subject }, [sort: true])
// Sorts items by natural order (toString() comparison)
```

**2. Custom Sort (Closure)**
```nextflow
channel.groupTupleBy({ it.subject }, [
    sort: { a, b -> a.run <=> b.run }
])
// Sorts by custom logic (Groovy spaceship operator)
```

**3. Comparator (Java Comparator)**
```nextflow
import java.util.Comparator

def byDate = Comparator.comparing { it.date }

channel.groupTupleBy({ it.subject }, [sort: byDate])
```

### Size-Based Grouping

Emit groups when they reach a specific size:

```nextflow
channel.groupTupleBy({ it.batch }, [
    size: 10,          // Emit when group has 10 items
    remainder: true    // Emit incomplete groups at end
])
```

**Use Cases:**
- Batch processing (process 100 items at a time)
- Sliding windows
- Fixed-size chunks

**Behavior:**
- Groups emit **immediately** when reaching `size`
- If `remainder: false`, incomplete groups are **discarded**
- If `remainder: true` (default), incomplete groups emit at channel end

### Thread Safety

All three operators are **thread-safe** for concurrent channel processing:

**Synchronized Methods:**
- All state-modifying operations use `synchronized` blocks
- Atomic operations for counter increments: `counts[key] = (counts[key] ?: 0) + 1`
- Thread-safe map operations: `map.computeIfAbsent(key, { [] })`

**What This Means:**
- ✅ Safe to use with `parallel`, `splitFastq`, and other concurrent operators
- ✅ Handles high-volume data streams (tested up to 10,000 items)
- ✅ No race conditions or data corruption under concurrent load

---

## Performance Characteristics

### groupTupleBy

| Dataset Size | Execution Time | vs groupTuple |
|--------------|----------------|---------------|
| 100 items    | ~260ms         | **40% faster** ✅ |
| 1,000 items  | ~320ms         | **29% faster** ✅ |
| 10,000 items | ~600ms         | **33% faster** ✅ |

**Memory:** O(n) - stores all items until grouping complete

**Best For:**
- Small to medium datasets (< 100k items)
- Complex data structures (maps, objects)
- Semantic field access

### joinBy

| Dataset Size | Execution Time | vs join | Notes |
|--------------|----------------|---------|-------|
| 100 pairs    | ~420ms         | **22% faster** ✅ | Unique keys |
| 1,000 pairs  | ~1,080ms       | **40% slower** ⚠️ | Some duplicates |
| 10,000 pairs | ~3,150ms       | **100% slower** ⚠️ | Many duplicates |

**Memory:** O(n + m) - buffers both channels completely

**Performance Notes:**
- ✅ **Excellent** for typical BIDS workflows (mostly 1:1 joins)
- ⚠️ **Degrades** with high key duplication (cartesian product overhead)
- 🔧 **Future optimization** planned for duplicate-heavy scenarios

**Best For:**
- BIDS subject matching (low duplication)
- 1:1 or 1:few relationships
- Complex key extraction logic

### combineBy

| Dataset Size | Execution Time | vs combine | Output Size |
|--------------|----------------|------------|-------------|
| 10 × 10      | ~170ms         | **39% faster** ✅ | 100 pairs |
| 30 × 30      | ~340ms         | **1% faster** ✅ | 900 pairs |
| 100 × 100    | ~640ms         | **3% faster** ✅ | 10,000 pairs |

**Memory:** O(n + m) - buffers both channels, emits combinations on-the-fly

**Output Size:** O(n × m) - all combinations (before filtering)

**Best For:**
- Parameter sweeps
- Grid searches
- Moderate-sized channel combinations (< 1000 × 1000)

### General Recommendations

**When to Use Closure-Based Operators:**
- ✅ Working with maps, objects, or complex structures
- ✅ Need semantic field access (`it.subject` vs `it[0]`)
- ✅ Computed or composite keys
- ✅ Code readability and maintainability

**When to Use Built-In Operators:**
- ✅ Simple tuple structures with fixed positions
- ✅ Performance-critical sections with very large datasets (> 100k items)
- ✅ Join operations with extremely high key duplication

**Optimization Tips:**
1. **Filter early:** Reduce data before grouping/joining
2. **Use indices when possible:** For simple cases, built-ins may be faster
3. **Monitor memory:** Large cartesian products can consume significant memory
4. **Consider alternatives:** For massive joins, use `groupTupleBy` + standard `join`

---

## Troubleshooting

### Common Issues

#### Issue 1: "Missing process or function"

**Error:**
```
Missing process or function groupTupleBy(...)
```

**Cause:** Forgot to include operator from plugin

**Solution:**
```nextflow
include { groupTupleBy } from 'plugin/nf-bids'
```

**Note:** The `include` statement is **required** even if plugin is loaded in config.

---

#### Issue 2: "keyExtractor must accept at least one parameter"

**Error:**
```
groupTupleBy: keyExtractor must accept at least one parameter
  Expected: { item -> item.key }
  Found: { -> ... }
```

**Cause:** Closure has no parameters

**Solution:**
```nextflow
// ❌ WRONG
channel.groupTupleBy { }

// ✅ CORRECT
channel.groupTupleBy { it.key }
```

---

#### Issue 3: Incorrect Join Output Access

**Error:**
```
groovy.lang.MissingPropertyException: No such property
```

**Cause:** Trying to access `joined[1]` and `joined[2]` expecting `[key, left, right]`

**Solution:**
`joinBy` emits `[key, leftItem, rightItem]` (3 elements), matching Nextflow's standard `join`:

```nextflow
// ❌ WRONG (2-element destructuring)
left.joinBy(right) { it.id }.view { left, right ->
    println left   // This gets the KEY, not left item!
    println right  // This gets left item, not right!
}

// ✅ CORRECT (array access)
left.joinBy(right) { it.id }.view { joined ->
    println joined[0]  // key
    println joined[1]  // left
    println joined[2]  // right
}

// ✅ BETTER (3-element destructuring)
left.joinBy(right) { it.id }.view { key, left, right ->
    println key
    println left
    println right
}
```

---

#### Issue 4: Items Silently Skipped

**Symptom:** Fewer items in output than expected

**Cause:** Key extractor returning `null` for some items

**Debug:**
Enable TRACE logging in `nextflow.config`:
```groovy
log.level = 'TRACE'
```

Look for:
```
groupTupleBy: item produced null key, skipping: [...]
```

**Solution:**
Provide defaults for missing fields:
```nextflow
// ❌ May produce nulls
channel.groupTupleBy { it.session }

// ✅ Provides default
channel.groupTupleBy { it.session ?: 'no-session' }
```

---

#### Issue 5: combineBy Incorrect Destructuring

**Error:**
```
No such property: session for class: java.lang.String
```

**Cause:** Forgetting that `combineBy` emits 3-element tuples `[key, left, right]`

**Solution:**
Use 3-element destructuring:

```nextflow
// ❌ WRONG (2-element destructuring)
subjects.combineBy(sessions, { it.id })
    .view { subj, sess -> "${subj.age}" }  // subj is actually the KEY!

// ✅ CORRECT (3-element destructuring)
subjects.combineBy(sessions, { it.id })
    .view { key, subj, sess -> "${subj.age}" }

// ✅ ALSO CORRECT (array access)
subjects.combineBy(sessions, { it.id })
    .view { tuple ->
        "Key=${tuple[0]}, Subject=${tuple[1]}, Session=${tuple[2]}"
    }
```

---

#### Issue 6: Out of Memory

**Symptom:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Cause:** Large cartesian product or buffering many items

**Solutions:**

1. **Increase memory:**
```bash
export NXF_OPTS="-Xms2g -Xmx8g"
nextflow run workflow.nf
```

2. **Filter earlier:**
```nextflow
// ❌ BAD: Combine then filter (using a constant key with combineBy is less explicit)
large_channel1
    .combineBy(large_channel2, { it.key })
    .filter { ... }

// ✅ GOOD: Filter then combineBy or cross (filter upstream to reduce the size of the cartesian product).
// For combineBy, ensure meaningful key extractors exist before combining.
large_channel1.filter { ... }
    .combineBy(large_channel2.filter { ... }, { it.key })
```

3. **Use standard operators:**
For very large datasets, consider built-in operators or chunking strategies.

---

### Debugging Tips

**1. Use .view() to Inspect Data**
```nextflow
channel
    .groupTupleBy { it.subject }
    .view { key, items -> 
        "Key: ${key}, Count: ${items.size()}"
    }
```

**2. Log Key Extraction**
```nextflow
channel.groupTupleBy { item ->
    def key = extractKey(item)
    log.info("Item ${item} → Key ${key}")
    return key
}
```

**3. Test with Small Data First**
```nextflow
Channel
    .of([subject: 'sub-01', file: 'a.nii'])
    .groupTupleBy { it.subject }
    .view()
```

**4. Check Nextflow Log**
```bash
tail -f .nextflow.log
```

**5. Use --dump-channels**
```bash
nextflow run workflow.nf --dump-channels
```

---

## Summary

### Quick Reference

| Operator | Purpose | Output Format | Performance |
|----------|---------|---------------|-------------|
| `groupTupleBy` | Group by key | `[key, [items]]` | ✅ Excellent (30-40% faster than groupTuple) |
| `joinBy` | Join by key | `[key, leftItem, rightItem]` | ✅ Good for 1:1, ⚠️ Slower for many:many |
| `combineBy` | Cartesian product by matching keys | `[key, leftItem, rightItem]` | ✅ Excellent (1-39% faster than combine) |

### When to Use Each Operator

**groupTupleBy:**
- Consolidate items by subject, session, or other attributes
- Batch processing
- Collecting runs, echoes, or repetitions

**joinBy:**
- Match data across channels (anatomical + functional)
- Link metadata to files
- Combine derivatives with raw data

**combineBy:**
- Parameter sweeps
- Grid searches
- Cross-product of configurations

---

## See Also

- [Migration Guide](./MIGRATION_GUIDE.md) - Converting from index-based to closure-based operators
- [Plugin Documentation](../README.md) - Full plugin documentation
- [Nextflow Documentation](https://www.nextflow.io/docs/latest/) - Official Nextflow docs
- [BIDS Specification](https://bids-specification.readthedocs.io/) - Brain Imaging Data Structure

---

**Version:** 0.1.0-beta.5  
**Last Updated:** November 2025  
**License:** Apache 2.0
