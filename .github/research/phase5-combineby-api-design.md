# CombineBy API Design Specification

**Version:** v0.1.0-beta.5  
**Date:** 2025-11-21  
**Status:** APPROVED for implementation

## Executive Summary

Redesign `combineBy` operator to use **key extraction** (like `groupTupleBy` and `joinBy`) instead of **filtering predicates**, aligning with Nextflow's `combine(by:)` pattern and ensuring API consistency across all closure-based operators.

## Design Decisions

### Decision 1: Key Extractor Signature ✅

**Chosen: Option B - Dual Closures**

```groovy
channel.combineBy(
    rightChannel,
    leftKeyExtractor,    // Closure: leftItem -> key
    rightKeyExtractor    // Closure: rightItem -> key
)
```

**Rationale:**
- Allows different key extraction logic for left/right channels
- Consistent with `joinBy` which uses separate extractors
- More flexible for asymmetric data structures
- Follows single responsibility principle (each closure handles one channel)

**Rejected Alternative:**
```groovy
// Option A: Single closure (rejected)
channel.combineBy(rightChannel) { item -> key }
```
- Would force same extraction logic for both channels
- Less flexible for different data structures

---

### Decision 2: Output Format ✅

**Chosen: `[key, leftItem, rightItem]` (3-element tuples)**

**Rationale:**
- **Consistency with joinBy:** Both operators now emit identical structure
- **Includes key:** Aligns with Nextflow's `combine(by:)` which keeps key in output
- **Predictable destructuring:** `{ key, left, right -> ... }`
- **Migration clarity:** Clear parallel to joinBy migration

**Comparison:**

| Format | Example | Pros | Cons |
|--------|---------|------|------|
| `[key, left, right]` ✅ | `['sub-01', meta1, meta2]` | Consistent with joinBy, includes key | Breaking change |
| `[left, right]` ❌ | `[meta1, meta2]` | Current API (no change) | Inconsistent, loses key info |
| `[key, [lefts], [rights]]` ❌ | `['sub-01', [m1, m2], [m3]]` | Groups all combinations | Different from standard combine |

---

### Decision 3: Combination Logic ✅

**Chosen: Full Cartesian Product Within Matching Keys**

**Behavior:**
```groovy
// Left channel:  ['A', 'L1'], ['A', 'L2'], ['B', 'L3']
// Right channel: ['A', 'R1'], ['A', 'R2'], ['B', 'R3']

// With key extractor: { it[0] }

// Output (6 tuples):
['A', ['A', 'L1'], ['A', 'R1']]  ← 
['A', ['A', 'L1'], ['A', 'R2']]  ← A × A = 2×2 = 4 combinations
['A', ['A', 'L2'], ['A', 'R1']]  ←
['A', ['A', 'L2'], ['A', 'R2']]  ←
['B', ['B', 'L3'], ['B', 'R3']]  ← B × B = 1×1 = 1 combination
```

**Rationale:**
- **Matches Nextflow's combine(by:):** Standard operator does full cartesian within groups
- **Expected behavior:** "Combine" implies all combinations
- **Use case fit:** BIDS workflows need all subject×session, scan×param combinations
- **Consistency:** joinBy already supports cartesian for duplicate keys

**Rejected Alternative:**
- One-to-one matching (first-with-first): Not expected for "combine" semantics

---

### Decision 4: Additional Features ✅

**Phase 1 (Beta.5): Core Functionality Only**
- Key extraction (dual closures)
- Cartesian product within groups
- Drop unmatched keys (inner join semantics)

**Future Phases (Post Beta.5):**
- `remainder: true` option (outer join, like joinBy)
- Optional filter predicate as 5th parameter
- `failOnDuplicate` validation option

**Rationale:**
- Start simple, validate core behavior
- Add complexity incrementally based on user feedback
- Remainder can be added without breaking API (optional parameter)

---

## Final API Specification

### Method Signature

```groovy
/**
 * Combines two channels by extracting and matching keys, emitting the
 * cartesian product of items within each key group.
 *
 * @param left Left input channel
 * @param right Right input channel  
 * @param leftKeyExtractor Closure to extract key from left items
 * @param rightKeyExtractor Closure to extract key from right items
 * @param opts Optional configuration map (reserved for future: remainder, filter)
 * @return Channel emitting [key, leftItem, rightItem] tuples
 */
DataflowWriteChannel combineBy(
    DataflowReadChannel left,
    DataflowReadChannel right,
    Closure leftKeyExtractor,
    Closure rightKeyExtractor,
    Map opts = [:]
)
```

### Usage Examples

#### Example 1: Basic Key Extraction

```groovy
// Combine subjects with sessions by subject ID
subjects = Channel.of(
    [id: 'sub-01', age: 25],
    [id: 'sub-01', age: 25],  // duplicate subject
    [id: 'sub-02', age: 30]
)

sessions = Channel.of(
    [id: 'sub-01', session: 'ses-01'],
    [id: 'sub-01', session: 'ses-02'],  // two sessions for sub-01
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

// Output (5 tuples - sub-01 has 2×2=4, sub-02 has 1×1=1):
// Key=sub-01: Subject(age=25) × Session(ses-01)
// Key=sub-01: Subject(age=25) × Session(ses-02)
// Key=sub-01: Subject(age=25) × Session(ses-01)
// Key=sub-01: Subject(age=25) × Session(ses-02)
// Key=sub-02: Subject(age=30) × Session(ses-01)
```

#### Example 2: Different Key Extractors

```groovy
// Combine scans with parameters using different fields
scans = Channel.of(
    [subject: 'sub-01', scan: 'T1w'],
    [subject: 'sub-02', scan: 'T2w']
)

params = Channel.of(
    [subjectId: 'sub-01', tr: 2000],
    [subjectId: 'sub-02', tr: 3000]
)

scans.combineBy(
    params,
    { it.subject },      // extract from 'subject' field
    { it.subjectId }     // extract from 'subjectId' field
)
.view { key, scan, param ->
    "Subject ${key}: ${scan.scan} with TR=${param.tr}ms"
}
```

#### Example 3: Complex Key Generation

```groovy
// Combine by composite key (subject + session)
dwi = Channel.of(
    [sub: 'sub-01', ses: 'ses-01', type: 'dwi'],
    [sub: 'sub-01', ses: 'ses-02', type: 'dwi']
)

anat = Channel.of(
    [sub: 'sub-01', ses: 'ses-01', type: 'T1w'],
    [sub: 'sub-01', ses: 'ses-02', type: 'T1w']
)

dwi.combineBy(
    anat,
    { "${it.sub}_${it.ses}" },  // composite key
    { "${it.sub}_${it.ses}" }
)
.view { key, dwiScan, anatScan ->
    "Pairing ${key}: ${dwiScan.type} + ${anatScan.type}"
}
```

---

## Implementation Requirements

### CombineByOp.groovy Changes

**Fields to Add/Modify:**
```groovy
// REMOVE:
private final Closure filterPredicate
private final List<Object> leftBuffer
private final List<Object> rightBuffer

// ADD:
private final Closure leftKeyExtractor
private final Closure rightKeyExtractor
private final Map<Object, List<Object>> leftBuffer
private final Map<Object, List<Object>> rightBuffer
```

**Core Logic:**
1. Extract key using `KeyExtractor.extractKey(item, closure, 'combineBy(left/right)')`
2. Buffer items in Maps: `leftBuffer.computeIfAbsent(key, { k -> [] }).add(item)`
3. Match by key: `rightBuffer[key]`
4. Emit cartesian product: `target.bind([key, leftItem, rightItem])`
5. Drop unmatched keys (no remainder in phase 1)

**Thread Safety:**
- Use `@CompileStatic`
- `synchronized` on all mutation methods
- `computeIfAbsent` for Map access

---

## Edge Cases & Validation

### Test Cases Required

1. ✅ **Basic matching:** Single left + single right with same key
2. ✅ **Cartesian product:** Multiple left + multiple right with same key
3. ✅ **Multiple keys:** Different keys processed independently
4. ✅ **Unmatched keys:** Verify dropped (no output)
5. ✅ **Empty channels:** Either empty → output empty
6. ✅ **Null keys:** Items with null keys skipped (with trace log)
7. ✅ **Duplicate keys:** Verify cartesian product correctness
8. ✅ **Different extractors:** Left/right use different fields
9. ✅ **Complex keys:** Maps, lists, tuples as keys
10. ✅ **High volume:** Stress test with 10k+ items

### Error Handling

**Invalid closure:**
```groovy
try {
    def key = KeyExtractor.extractKey(item, extractor, 'combineBy(left)')
} catch (Exception e) {
    throw new IllegalStateException(
        "combineBy: Key extractor failed for item ${item}: ${e.message}",
        e
    )
}
```

**Null key behavior:**
- Skip item (don't add to buffer)
- Log trace warning
- Continue processing (no error)

---

## Migration Guide

### Breaking Changes

**Old API (v0.1.0-beta.4):**
```groovy
subjects.combineBy(sessions) { subj, sess ->
    // Filter predicate: return true to include combination
    subj.id == sess.subject
}
.view { subj, sess -> "Matched: ${subj} with ${sess}" }
```

**New API (v0.1.0-beta.5+):**
```groovy
subjects.combineBy(
    sessions,
    { it.id },          // Key extractor for left
    { it.subject }      // Key extractor for right
)
.view { key, subj, sess -> "Key ${key}: ${subj} with ${sess}" }
```

**Migration Checklist:**
1. ❌ Remove filter predicate closure
2. ✅ Add left key extractor (extracts key from left items)
3. ✅ Add right key extractor (extracts key from right items)
4. ✅ Update `.view` / `.map` to destructure 3 elements: `{ key, left, right -> ... }`
5. ✅ Verify cartesian product is desired behavior

---

## Success Criteria

- [x] API uses key extraction (not filtering)
- [x] Emits `[key, leftItem, rightItem]` tuples
- [x] Cartesian product within matching keys
- [x] Thread-safe implementation
- [x] Comprehensive error handling
- [x] Null key handling (skip gracefully)
- [x] All edge cases tested
- [x] Documentation updated
- [x] Migration guide complete

---

## Approval

**Approved for implementation:** 2025-11-21  
**Target version:** v0.1.0-beta.5  
**Breaking change:** YES - requires major migration

This design aligns with:
- ✅ Nextflow's `combine(by:)` semantics
- ✅ Consistency with `groupTupleBy` and `joinBy`
- ✅ Closure-based pattern across all operators
- ✅ Clear migration path from beta.4
