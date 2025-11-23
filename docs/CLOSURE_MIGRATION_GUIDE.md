# Closure-Based Operators Migration Guide

**nf-bids Plugin** - Version 0.1.0-beta.5

Migrate from Nextflow's index-based operators (`groupTuple`, `join`, `combine`) to closure-based alternatives (`groupTupleBy`, `joinBy`, `combineBy`).

---

## Quick Reference

| Index-Based | Closure-Based | Key Benefit |
|-------------|---------------|-------------|
| `groupTuple(by: 0)` | `groupTupleBy { it.field }` | Semantic grouping, works with maps |
| `join(by: [0])` | `joinBy(right) { it.field }` | No tuple restructuring needed |
| `combine(right)` | `combineBy(right)` | Add `.filter()` for conditional pairing |

---

## Why Migrate?

### Index-Based Limitations

```nextflow
// Requires tuple structure, brittle indices
channel
    .of(['sub-01', 'file1.nii'], ['sub-01', 'file2.nii'])
    .groupTuple(by: 0)  // by: 0 is not self-documenting
```

**Problems:**
- ❌ Requires tuples with fixed positions
- ❌ `by: 0` doesn't explain what's being grouped
- ❌ Can't group by computed values
- ❌ Multi-field grouping needs extra `.map()` steps

### Closure-Based Benefits

```nextflow
// Works with any structure, self-documenting
channel
    .of([subject: 'sub-01', file: 'file1.nii'],
        [subject: 'sub-01', file: 'file2.nii'])
    .groupTupleBy { it.subject }  // Crystal clear intent
```

**Advantages:**
- ✅ Semantic field names
- ✅ Works with maps/objects
- ✅ Computed keys: `{ it.value * 2 }`
- ✅ Composite keys: `{ [it.subject, it.session] }`
- ✅ Maintainable code

---

## Installation

**nextflow.config:**
```groovy
plugins {
    id 'nf-bids@0.1.0-beta.5'
}
```

**Workflow script:**
```nextflow
include { groupTupleBy } from 'plugin/nf-bids'
include { joinBy } from 'plugin/nf-bids'
include { combineBy } from 'plugin/nf-bids'
```

---

## groupTuple → groupTupleBy

### Single Field Grouping

**Before:**
```nextflow
channel
    .of(['sub-01', 'file1.nii'], ['sub-01', 'file2.nii'], ['sub-02', 'file3.nii'])
    .groupTuple(by: 0)
// Output: ['sub-01', ['file1.nii', 'file2.nii']]
//         ['sub-02', ['file3.nii']]
```

**After:**
```nextflow
channel
    .of([subject: 'sub-01', file: 'file1.nii'],
        [subject: 'sub-01', file: 'file2.nii'],
        [subject: 'sub-02', file: 'file3.nii'])
    .groupTupleBy { it.subject }
// Output: ['sub-01', [[subject:'sub-01', file:'file1.nii'], 
//                     [subject:'sub-01', file:'file2.nii']]]
//         ['sub-02', [[subject:'sub-02', file:'file3.nii']]]
```

**Key Difference:** Items remain as full maps, not decomposed into separate tuple elements.

---

### Composite Key Grouping

**Before:**
```nextflow
channel
    .of([subject: 'sub-01', session: 'ses-01', file: 'a.nii'])
    .map { item ->
        def key = [item.subject, item.session]  // Create composite key
        return [key, item.file]
    }
    .groupTuple(by: 0)
```

**After:**
```nextflow
channel
    .of([subject: 'sub-01', session: 'ses-01', file: 'a.nii'])
    .groupTupleBy { [it.subject, it.session] }  // Direct composite key
// No .map() step needed!
```

---

### Sorted Groups

**Before:**
```nextflow
channel
    .of(['A', 3, 'c'], ['A', 1, 'a'], ['A', 2, 'b'])
    .groupTuple(by: 0, sort: { a, b -> a[0] <=> b[0] })
```

**After:**
```nextflow
channel
    .of([batch: 'A', run: 3, file: 'c'], 
        [batch: 'A', run: 1, file: 'a'],
        [batch: 'A', run: 2, file: 'b'])
    .groupTupleBy({ it.batch }, [sort: { a, b -> a.run <=> b.run }])
// Sort closure receives full items, not tuple sub-lists
```

---

### Batch Processing (Size Option)

**Before:**
```nextflow
channel
    .of(['batch', 1], ['batch', 2], ['batch', 3], ['batch', 4])
    .groupTuple(by: 0, size: 2, remainder: false)
```

**After:**
```nextflow
channel
    .of([type: 'batch', value: 1], [type: 'batch', value: 2],
        [type: 'batch', value: 3], [type: 'batch', value: 4])
    .groupTupleBy({ it.type }, [size: 2, remainder: false])
```

---

## join → joinBy

### Simple Join (Same Key)

**Before:**
```nextflow
left = channel
    .of([subject: 'sub-01', file: 't1.nii'])
    .map { [it.subject, it] }  // Restructure to [key, item]

right = channel
    .of([subject: 'sub-01', file: 'bold.nii'])
    .map { [it.subject, it] }  // Restructure to [key, item]

left.join(right).view { key, leftItem, rightItem -> ... }
// Output: [key, leftItem, rightItem]
```

**After:**
```nextflow
left = channel.of([subject: 'sub-01', file: 't1.nii'])
right = channel.of([subject: 'sub-01', file: 'bold.nii'])

left.joinBy(right) { it.subject }.view { key, leftItem, rightItem -> ... }
// Output: [key, leftItem, rightItem]  ← SAME AS join!
```

**✅ Compatible:** `joinBy` emits `[key, leftItem, rightItem]`, matching Nextflow's `join` operator

---

### Different Key Fields

**Before:**
```nextflow
scans = channel
    .of([scan_id: 'scan001', file: 'scan.nii'])
    .map { [it.scan_id, it] }

metadata = channel
    .of([participant_id: 'scan001', age: 25])
    .map { [it.participant_id, it] }

scans.join(metadata).view { key, scan, meta -> ... }
```

**After:**
```nextflow
scans = channel.of([scan_id: 'scan001', file: 'scan.nii'])
metadata = channel.of([participant_id: 'scan001', age: 25])

scans
    .joinBy(metadata,
            { it.scan_id },         // Left key
            { it.participant_id })  // Right key
    .view { key, scan, meta -> ... }
// Same output structure as join, no tuple restructuring!
```

---

### Composite Key Join

**Before:**
```nextflow
left = channel
    .of([subj: 'sub-01', sess: 'ses-01', file: 'a.nii'])
    .map { item -> [[item.subj, item.sess], item] }

right = channel
    .of([subj: 'sub-01', sess: 'ses-01', file: 'b.nii'])
    .map { item -> [[item.subj, item.sess], item] }

left.join(right).view()
```

**After:**
```nextflow
left = channel.of([subj: 'sub-01', sess: 'ses-01', file: 'a.nii'])
right = channel.of([subj: 'sub-01', sess: 'ses-01', file: 'b.nii'])

left.joinBy(right) { [it.subj, it.sess] }.view()
// Single line, no .map() needed
```

---

### Outer Join (Remainder)

**Before:**
```nextflow
left = channel
    .of([id: 'A', val: 1], [id: 'B', val: 2])
    .map { [it.id, it] }

right = channel
    .of([id: 'A', val: 10])
    .map { [it.id, it] }

left.join(right, remainder: true).view { key, l, r ->
    "${key}: ${l?.val}, ${r?.val}"
}
// Output includes key
```

**After:**
```nextflow
left = channel.of([id: 'A', val: 1], [id: 'B', val: 2])
right = channel.of([id: 'A', val: 10])

left
    .joinBy(right, { it.id }, { it.id }, [remainder: true])
    .view { l, r ->
        "${l?.id}: ${l?.val}, ${r?.val}"
    }
// Output does NOT include key
```

---

## combine → combineBy

### Cartesian Product

**Before:**
```nextflow
subjects = channel.of('sub-01', 'sub-02')
sessions = channel.of('ses-01', 'ses-02')

subjects.combine(sessions).view()
// [sub-01, ses-01], [sub-01, ses-02], [sub-02, ses-01], [sub-02, ses-02]
```

**After:**
```nextflow
subjects = channel.of('sub-01', 'sub-02')
sessions = channel.of('ses-01', 'ses-02')

subjects.combineBy(sessions, { it }) .view()
// Identical output
```

---

### Filtered Combinations

**Before:**
```nextflow
images = channel.of(['img1', 0.95], ['img2', 0.75])
analyses = channel.of(['basic', 0.70], ['advanced', 0.90])

images
    .combine(analyses)
    .filter { imgId, imgQuality, analysisName, minQuality ->
        imgQuality >= minQuality
    }
    .view()
```

**After:**
```nextflow
images = channel.of([id: 'img1', quality: 0.95], [id: 'img2', quality: 0.75])
analyses = channel.of([name: 'basic', min_quality: 0.70], 
                      [name: 'advanced', min_quality: 0.90])

images
    .combineBy(analyses, { 0 })
    .filter { img, analysis ->
        img.quality >= analysis.min_quality
    }
    .view()
// Semantic field access in filter
```

---

### Multi-Condition Filtering

**Before:**
```nextflow
scans = channel.of(['sub-01', 'T1w'], ['sub-02', 'T2w'])
protocols = channel.of(['proto1', 'T1w'], ['proto2', 'T2w'])

scans
    .combine(protocols)
    .filter { subjId, scanMod, protoName, protoMod ->
        scanMod == protoMod
    }
    .view()
```

**After:**
```nextflow
scans = channel.of([subject: 'sub-01', modality: 'T1w'],
                   [subject: 'sub-02', modality: 'T2w'])
protocols = channel.of([name: 'proto1', modality: 'T1w'],
                       [name: 'proto2', modality: 'T2w'])

scans
    .combineBy(protocols, { it.modality }, { it.modality })
    .filter { scan, proto ->
        scan.modality == proto.modality
    }
    .view()
// Self-documenting filter logic
```

---

## Common Scenarios

### BIDS Subject Processing

**Before:**
```nextflow
bids_files = channel
    .of([subject: 'sub-01', file: 'a.nii'],
        [subject: 'sub-01', file: 'b.nii'])
    .map { [it.subject, it] }  // Restructure
    .groupTuple(by: 0)
    .map { subject, items -> [subject: subject, files: items] }  // Unpack
```

**After:**
```nextflow
bids_files = channel
    .of([subject: 'sub-01', file: 'a.nii'],
        [subject: 'sub-01', file: 'b.nii'])
    .groupTupleBy { it.subject }
// Direct, no restructuring needed
```

---

### Metadata Joining

**Before:**
```nextflow
scans = scans_ch.map { [it.id, it] }
metadata = meta_ch.map { [it.scan_id, it] }

scans.join(metadata).map { id, scan, meta -> scan + meta }
```

**After:**
```nextflow
scans_ch
    .joinBy(meta_ch, { it.id }, { it.scan_id })
    .map { scan, meta -> scan + meta }
```

---

### Parameter Sweeps

**Before/After:** (Functionally equivalent)
```nextflow
subjects
    .combineBy(smoothing_values, { it }, { it })
    .combineBy(threshold_values, { it }, { it })

// Same as:
subjects
    .combineBy(smoothing_values)
    .combineBy(threshold_values)
```

---

## Migration Checklist

### Pre-Migration

- [ ] Identify all `groupTuple`, `join`, `combine` uses
- [ ] Document current tuple structures
- [ ] Note complex `.map()` operations
- [ ] Install plugin: `nf-bids@0.1.0-beta.5`

### During Migration

- [ ] Convert tuples to maps where beneficial
- [ ] Add operator `include` statements
- [ ] Replace index keys with closures
- [ ] Remove intermediate `.map()` steps
- [ ] **Update code expecting `[key, left, right]` from join**

### Post-Migration

- [ ] Test with small dataset
- [ ] Verify output matches original
- [ ] Test edge cases (nulls, empty channels)
- [ ] Performance test if critical
- [ ] Update documentation

---

## Troubleshooting

### Issue 1: joinBy Output Structure Changed

**Problem:** Code expects `[key, left, right]`

```nextflow
// ❌ WRONG
left.joinBy(right) { it.id }.view { key, l, r -> ... }  // ERROR!

// ✅ CORRECT
left.joinBy(right) { it.id }.view { l, r -> ... }
```

---

### Issue 2: Items Grouped Differently

**groupTuple output:**
```
[key, [val1a, val1b], [val2a, val2b]]  // Tuple decomposed
```

**groupTupleBy output:**
```
[key, [item1, item2]]  // Items preserved
```

**Solution:** Adjust downstream processing:
```nextflow
// After groupTupleBy:
.map { key, items ->
    def files = items.collect { it.file }
    def metadata = items.collect { it.meta }
    ...
}
```

---

### Issue 3: Null Keys Cause Skipping

**Problem:** Items silently skipped

**Solution:** Provide defaults
```nextflow
// ❌ May skip items
.groupTupleBy { it.session }

// ✅ Handles nulls
.groupTupleBy { it.session ?: 'no-session' }
```

---

### Issue 4: Performance Regression

**Diagnosis:**
- Check for duplicate keys in `joinBy` (cartesian product)
- Monitor memory usage

**Solutions:**
1. Filter early: `channel.filter { ... }.groupTupleBy { ... }`
2. For simple tuples, built-in may be faster
3. Ensure mostly unique keys for joins

---

## Best Practices

### 1. Use Maps for Complex Data

```nextflow
// ❌ Hard to maintain
['sub-01', 'ses-01', 'T1w', 'scan.nii', [...]]

// ✅ Self-documenting
[subject: 'sub-01', session: 'ses-01', modality: 'T1w', 
 file: 'scan.nii', metadata: [...]]
```

### 2. Semantic Field Names

```nextflow
// ❌ Unclear
[id1: 'sub-01', id2: 'ses-01']

// ✅ Clear
[subject: 'sub-01', session: 'ses-01']
```

### 3. Safe Navigation

```nextflow
.groupTupleBy { it.session?.id ?: 'unknown' }
```

### 4. Document Composite Keys

```nextflow
// Group by subject + session
.groupTupleBy { [it.subject, it.session] }
```

### 5. Test Incrementally

Migrate one operator at a time, test, then proceed.

---

## When to Use Each Approach

### Use Closure-Based When:

- ✅ Working with maps or complex objects
- ✅ Need semantic field access
- ✅ Computed or composite keys
- ✅ Code readability is priority

### Use Index-Based When:

- ✅ Simple tuples with fixed positions
- ✅ Maximum performance critical (> 100k items)
- ✅ Legacy code compatibility
- ✅ Standard patterns sufficient

---

## Performance Comparison

| Operator | Dataset | Closure-Based vs Built-in |
|----------|---------|---------------------------|
| groupTupleBy | 100 items | **40% faster** ✅ |
| groupTupleBy | 10,000 items | **33% faster** ✅ |
| joinBy | 100 pairs (unique) | **22% faster** ✅ |
| joinBy | 10,000 pairs (duplicates) | **100% slower** ⚠️ |
| combineBy | 100×100 | **3% faster** ✅ |

**Notes:**
- groupTupleBy: Excellent across all sizes
- joinBy: Great for 1:1, slower for many:many
- combineBy: Comparable performance

---

## Resources

- [Channel Operators Documentation](./channel-operators.md) - Full API reference
- [Main Migration Guide](./MIGRATION_GUIDE.md) - bids2nf to nf-bids plugin
- [Nextflow Operators](https://www.nextflow.io/docs/latest/operator.html) - Built-in operators

---

**Version:** 0.1.0-beta.5  
**Last Updated:** November 2025  
**License:** Apache 2.0
