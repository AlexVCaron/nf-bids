# Research: Nextflow's combine(by:) Operator Behavior

**Date:** 2025-11-21  
**Purpose:** Understanding standard combine(by:) behavior to inform combineBy redesign

## Key Findings

### 1. Output Format

**Standard combine(by:) outputs:**
- `[key, leftItem..., rightItem...]` - **Key is included in output**
- For simple tuples: `[key, leftRemainder, rightRemainder]`
- Key position(s) preserved at the start of the tuple

**Examples from testing:**
```groovy
// Input: ['A', 'left1'] combine ['A', 'right1'] by: 0
// Output: ['A', 'left1', 'right1']  // 3 elements

// Input: ['A', 'X', 'left1'] combine ['A', 'X', 'right1'] by: [0, 1]  
// Output: ['A', 'X', 'left1', 'right1']  // 4 elements
```

### 2. Key Extraction Mechanism

- Uses **index-based key extraction** via `by:` parameter
- `by: 0` - uses first element as key
- `by: [0, 1]` - uses first two elements as composite key
- Keys must match exactly for items to combine

### 3. Cartesian Product Within Groups

**CONFIRMED:** combine(by:) produces full cartesian product within matching keys

**Test Results:**
```
LEFT:  ['KEY1', 'L1'], ['KEY1', 'L2'], ['KEY2', 'L3']
RIGHT: ['KEY1', 'R1'], ['KEY1', 'R2'], ['KEY2', 'R3']

combine(by: 0) produces:
- ['KEY1', 'L1', 'R1']  ← 
- ['KEY1', 'L1', 'R2']  ← KEY1 produces 4 combinations (2x2 cartesian)
- ['KEY1', 'L2', 'R1']  ←
- ['KEY1', 'L2', 'R2']  ←
- ['KEY2', 'L3', 'R3']  ← KEY2 produces 1 combination (1x1 cartesian)
```

### 4. Unmatched Keys Behavior

**Observation:** Unmatched keys are silently dropped (no remainder option visible)

**Test Results:**
```
LEFT:  ['A', 'left1'], ['B', 'left2'], ['C', 'left3']
RIGHT: ['A', 'right1'], ['B', 'right2'], ['D', 'right3']

combine(by: 0) produces only:
- ['A', 'left1', 'right1']
- ['B', 'left2', 'right2']

Keys 'C' and 'D' are dropped (no output)
```

### 5. Empty Channel Behavior

- If either channel is empty, output is empty
- No error, just completes with no emissions

## Design Implications for combineBy

### ✅ What We Should Replicate

1. **Include key in output:** Emit `[key, leftItem, rightItem]`
2. **Cartesian product within groups:** Multiple items with same key produce all combinations
3. **Drop unmatched keys:** Default behavior (no remainder initially)
4. **Key extraction via closure:** But using closure instead of index

### 🔄 What We Should Adapt

1. **Closure-based key extraction:**
    ```groovy
    // Standard: combine(right, by: 0)
    // Our API: combineBy(right, { it[0] })
    ```

2. **Flexible key extraction:**
   - Allow different extractors for left/right channels
   - Support complex key generation (not just index access)

### 📋 Output Format Decision

**Standard combine(by: 0) for simple tuples:**
- Input: `[key, value]` × `[key, value]`
- Output: `[key, leftValue, rightValue]` (3 elements)

**Our combineBy should emit:**
- `[key, leftItem, rightItem]` (3 elements, consistent with joinBy)

**For complex tuples:**
- If items are complex objects/maps: `[key, leftObject, rightObject]`
- Key extractor can generate any object as key

## Test Cases to Implement

1. ✅ Basic key extraction with simple keys
2. ✅ Cartesian product within groups (multiple items per key)
3. ✅ Unmatched keys (verify they're dropped)
4. ✅ Empty channels
5. 🔄 Complex key types (maps, lists, objects)
6. 🔄 Different left/right key extractors
7. 🔄 Null key handling
8. 🔄 High volume (stress test)

## API Design Recommendations

### Recommended Signature

```groovy
DataflowWriteChannel combineBy(
    DataflowReadChannel left,
    DataflowReadChannel right,
    Closure leftKeyExtractor,   // item -> key
    Closure rightKeyExtractor,  // item -> key
    Map options = [:]           // Future: remainder, filter, etc.
)
```

### Example Usage

```groovy
// Extract subject ID from BIDS entities
subjects
    .combineBy(
        sessions,
        { it.subject },      // left key extractor
        { it.subject }       // right key extractor
    )
    .view { key, subj, sess -> 
        "Subject $key: ${subj.session} × ${sess.acquisition}"
    }
```

## Conclusion

Nextflow's `combine(by:)` operator:
- ✅ Includes keys in output (not filtering them out)
- ✅ Produces cartesian product within matching keys
- ✅ Drops unmatched keys silently
- ✅ Uses index-based key specification

Our `combineBy` redesign should:
- ✅ Use closure-based key extraction (more flexible)
- ✅ Emit `[key, leftItem, rightItem]` tuples
- ✅ Maintain cartesian product semantics
- ✅ Align behavior with standard operator
- ✅ Consistency with groupTupleBy and joinBy patterns
