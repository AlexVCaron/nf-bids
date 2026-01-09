# Phase 2.1: Enhanced Operator Signatures Design

**Date**: 2025-01-20  
**Phase**: Design & Architecture  
**Goal**: Define closure-based API signatures for channel grouping operators

---

## Design Principles

### 1. Groovy-Idiomatic
- Use closure syntax familiar to Groovy developers
- Follow conventions from Groovy collections (`groupBy`, `findBy`, `sortBy`)
- Natural reading flow: `channel.groupTupleBy { it.subject }`

### 2. Backward Compatible
- Existing index-based operators remain unchanged
- New operators have distinct names (no conflicts)
- Users can mix old and new approaches in same pipeline

### 3. Type Safety
- Clear return type expectations for closures
- Validation with helpful error messages
- Support for type coercion where sensible

### 4. Consistency
- All operators use same closure parameter naming: `keyExtractor`
- Similar option maps across operators
- Predictable behavior for edge cases

---

## Operator Signatures

### 1. `groupTupleBy` (HIGH PRIORITY)

**Purpose**: Group channel items by dynamically extracted keys

**Signature**:
```groovy
DataflowWriteChannel groupTupleBy(
    DataflowReadChannel source,
    Closure keyExtractor,
    Map opts = [:]
)
```

**Parameters**:
- `source`: The input channel to group
- `keyExtractor`: Closure that receives item and returns grouping key
  - **Input**: Single channel item (any type)
  - **Output**: Key value (any type) or list of keys for multi-key grouping
- `opts`: Optional configuration map
  - `size`: Integer - Expected group size (like current groupTuple)
  - `sort`: Boolean|Closure|Comparator - Sort items within groups
  - `remainder`: Boolean - Emit incomplete groups on completion

**Return**: Channel emitting `[key, [item1, item2, ...]]` tuples

**Usage Examples**:
```groovy
// Single key extraction
channel
    .of(
        [sample: 'A', file: 'a1.txt'],
        [sample: 'A', file: 'a2.txt'],
        [sample: 'B', file: 'b1.txt']
    )
    .groupTupleBy { it.sample }
    // Emits: ['A', [[sample:'A', file:'a1.txt'], [sample:'A', file:'a2.txt']]]
    //        ['B', [[sample:'B', file:'b1.txt']]]

// Nested field access
channel
    .of(
        [metadata: [subject: 'sub-01', session: 'ses-01'], file: 'file1.nii'],
        [metadata: [subject: 'sub-01', session: 'ses-02'], file: 'file2.nii']
    )
    .groupTupleBy { it.metadata.subject }

// Computed key
channel
    .of(
        [path: file('/data/sub-01/anat/T1.nii')],
        [path: file('/data/sub-01/func/bold.nii')],
        [path: file('/data/sub-02/anat/T1.nii')]
    )
    .groupTupleBy { it.path.parent.parent.name }  // Extract subject ID

// Multiple keys (composite key)
channel
    .of(
        [subject: 'sub-01', session: 'ses-01', run: 1],
        [subject: 'sub-01', session: 'ses-01', run: 2],
        [subject: 'sub-01', session: 'ses-02', run: 1]
    )
    .groupTupleBy { [it.subject, it.session] }
    // Groups by [subject, session] combination

// With options
channel
    .from(items)
    .groupTupleBy({ it.key }, [size: 2, sort: true, remainder: false])
```

**Edge Cases**:
- **Null key**: Filter out items returning null (emit warning?)
- **Exception in closure**: Fail fast with clear error
- **Empty channel**: Return empty channel
- **Single item**: Return group of one (if remainder: true)

---

### 2. `joinBy` (HIGH PRIORITY)

**Purpose**: Join two channels by dynamically extracted keys

**Signature**:
```groovy
DataflowWriteChannel joinBy(
    DataflowReadChannel left,
    DataflowReadChannel right,
    Closure leftKeyExtractor,
    Closure rightKeyExtractor = null,
    Map opts = [:]
)
```

**Parameters**:
- `left`: Left input channel
- `right`: Right input channel
- `leftKeyExtractor`: Closure extracting key from left channel items
- `rightKeyExtractor`: Optional closure for right channel (defaults to same as left)
- `opts`: Optional configuration
  - `remainder`: Boolean - Emit unmatched items (like outer join)
  - `failOnDuplicate`: Boolean - Error if duplicate keys in either channel
  - `failOnMismatch`: Boolean - Error if no match found

**Return**: Channel emitting `[leftItem, rightItem]` pairs where keys match

**Usage Examples**:
```groovy
// Simple join with same key extraction
left = Channel.of(
    [id: 'A', value: 1],
    [id: 'B', value: 2]
)
right = Channel.of(
    [id: 'A', data: 'x'],
    [id: 'B', data: 'y']
)
left.joinBy(right) { it.id }
// Emits: [[id:'A', value:1], [id:'A', data:'x']]
//        [[id:'B', value:2], [id:'B', data:'y']]

// Different key extractors
anatomical = Channel.of([subject: 'sub-01', file: 'T1.nii'])
functional = Channel.of([participant: 'sub-01', file: 'bold.nii'])

anatomical.joinBy(
    functional,
    { it.subject },           // Left: extract 'subject'
    { it.participant }        // Right: extract 'participant'
)

// Computed join keys
files.joinBy(metadata) { f -> f.name.split('_')[0] }

// With remainder option (outer join)
left.joinBy(right, { it.id }, [remainder: true])
// Emits matches + [leftItem, null] for unmatched left items
//                 + [null, rightItem] for unmatched right items
```

**Edge Cases**:
- **No matches**: Empty channel (unless remainder: true)
- **Duplicate keys**: Cartesian product of matches (or fail if failOnDuplicate: true)
- **Null keys**: Filter out (log warning)
- **One empty channel**: Empty result (unless remainder: true)

---

### 3. `combineBy` (HIGH PRIORITY)

**Purpose**: Combine channels with optional key-based filtering

**Signature**:
```groovy
DataflowWriteChannel combineBy(
    DataflowReadChannel left,
    DataflowReadChannel right,
    Closure filterPredicate = null,
    Map opts = [:]
)
```

**Parameters**:
- `left`: Left input channel
- `right`: Right input channel
- `filterPredicate`: Optional closure receiving `[leftItem, rightItem]` and returning boolean
  - Returns `true` to emit the combination
  - Returns `false` to skip the combination
- `opts`: Optional configuration (TBD based on current combine options)

**Return**: Channel emitting `[leftItem, rightItem]` for all valid combinations

**Usage Examples**:
```groovy
// All combinations (current behavior)
left.combineBy(right)
// Same as: left.combine(right)

// Filtered combinations
subjects = Channel.of('sub-01', 'sub-02', 'sub-03')
sessions = Channel.of('ses-01', 'ses-02')

subjects.combineBy(sessions) { subj, sess ->
    // Only combine if session number <= subject number
    sess.split('-')[1] <= subj.split('-')[1]
}
// For 'sub-01': only 'ses-01'
// For 'sub-02': 'ses-01', 'ses-02'
// For 'sub-03': 'ses-01', 'ses-02'

// Combine based on metadata compatibility
files1.combineBy(files2) { f1, f2 ->
    f1.metadata.modality != f2.metadata.modality  // Different modalities only
}

// Key-based filtering (group-aware combine)
anatomical = Channel.of(
    [subject: 'sub-01', type: 'T1'],
    [subject: 'sub-02', type: 'T1']
)
functional = Channel.of(
    [subject: 'sub-01', type: 'bold'],
    [subject: 'sub-02', type: 'bold']
)
anatomical.combineBy(functional) { anat, func ->
    anat.subject == func.subject  // Only combine matching subjects
}
```

**Alternative Design** (if filter is too broad):
```groovy
// Stricter: combine only items with matching keys
DataflowWriteChannel combineBy(
    DataflowReadChannel left,
    DataflowReadChannel right,
    Closure leftKeyExtractor,
    Closure rightKeyExtractor = null,
    Map opts = [:]
)
// Would produce cartesian product WITHIN each key group
```

**Edge Cases**:
- **No filter**: Behaves like regular combine (all pairs)
- **All filtered out**: Empty channel
- **Filter throws exception**: Fail fast with context

---

### 4. `crossBy` (MEDIUM PRIORITY)

**Note**: `cross` already supports closure mapper! From research:
```groovy
cross(source, mapper)  // mapper is a closure
```

**Decision**: 
- **Option A**: Skip (already exists)
- **Option B**: Create `crossBy` as alias with clearer naming
- **Option C**: Extend current `cross` if limitations exist

**Recommended**: Skip or create simple alias if needed for consistency.

---

## Closure Parameter Design

### Input/Output Contract

**Closure Signature**:
```groovy
Closure<KeyType> keyExtractor = { ItemType item -> 
    // Return single key
    return key
    
    // OR return composite key (for multi-key grouping)
    return [key1, key2, key3]
}
```

**Type Expectations**:
- **Input**: Receives the full channel item (could be any type)
- **Output**: 
  - Single value (primitive, String, Object) → simple key
  - List/Array → composite key (multiple dimensions)
  - Null → item is filtered out (with warning?)

### Validation Rules

**At operator invocation**:
```groovy
private static void validateKeyExtractor(Closure keyExtractor) {
    if (keyExtractor == null) {
        throw new IllegalArgumentException(
            "keyExtractor closure is required"
        )
    }
    
    // Check arity (parameter count)
    int params = keyExtractor.getMaximumNumberOfParameters()
    if (params == 0) {
        throw new IllegalArgumentException(
            "keyExtractor must accept at least one parameter (the channel item)"
        )
    }
    
    if (params > 1) {
        log.warn(
            "keyExtractor has ${params} parameters but only first will be used. " +
            "Did you mean to use 'combineBy' for pair filtering?"
        )
    }
}
```

**During execution**:
```groovy
private static def safeExtractKey(Closure keyExtractor, item, String operatorName) {
    try {
        def key = keyExtractor.call(item)
        if (key == null) {
            log.trace("${operatorName}: item produced null key, filtering out: ${item}")
            return null
        }
        return key
    } catch (Exception e) {
        throw new IllegalStateException(
            "${operatorName}: keyExtractor failed for item ${item}: ${e.message}",
            e
        )
    }
}
```

---

## Key Comparison Strategy

### Simple Keys (Primitives, Strings)
```groovy
static boolean keysMatch(def key1, def key2) {
    return key1 == key2
}
```

### Composite Keys (Lists)
```groovy
static boolean keysMatch(def key1, def key2) {
    if (key1 instanceof List && key2 instanceof List) {
        if (key1.size() != key2.size()) return false
        return key1.equals(key2)  // Element-wise comparison
    }
    return key1 == key2
}
```

### Hash Code for Grouping
```groovy
static int keyHashCode(def key) {
    if (key instanceof List) {
        // Compute hash for composite key
        return key.hashCode()
    }
    return key?.hashCode() ?: 0
}
```

**Storage**: Use `Map<KeyWrapper, List<Item>>` where `KeyWrapper` handles equality/hashCode correctly for lists.

---

## Error Handling Strategy

### User-Friendly Error Messages

**Bad closure arity**:
```
Error in groupTupleBy: keyExtractor must accept exactly 1 parameter (the channel item)
  Found: { a, b -> a + b }
  Expected: { item -> item.key }
```

**Closure throws exception**:
```
Error in joinBy: keyExtractor failed for item [subject: 'sub-01', session: 'ses-01']
  Caused by: No such property: subjectId
  
  Closure:
    { it.subjectId }  // Should be: { it.subject }
```

**Null key returned**:
```
Warning in groupTupleBy: 3 items produced null keys and were filtered out
  Set log level to TRACE to see filtered items
```

**Type mismatch in join**:
```
Error in joinBy: Key types don't match
  Left channel key type: Integer (from { it.id })
  Right channel key type: String (from { it.id })
  
  Consider explicit type conversion:
    { it.id.toString() }
```

---

## Backward Compatibility Notes

### No Breaking Changes
- Existing operators untouched: `join`, `groupTuple`, `combine`, etc.
- New operator names: `joinBy`, `groupTupleBy`, `combineBy`
- Users opt-in by using new operators

### Migration Path
```groovy
// OLD (index-based)
channel.groupTuple(by: 0)

// NEW (closure-based)
channel.groupTupleBy { it[0] }  // Equivalent

// NEW (semantic)
channel.groupTupleBy { it.subject }  // Better!
```

### Deprecation Strategy (Future)
Not planned initially, but if Nextflow core adopts closure support:
1. Mark `*By` operators as aliases
2. Update docs to prefer core operators
3. Eventually deprecate (3+ major versions later)

---

## Performance Considerations

### Closure Overhead
- **Concern**: Closure invocation slower than direct indexing
- **Mitigation**: 
  - Profile with realistic data volumes
  - Consider caching closure results if pure function
  - Accept minor overhead for expressiveness gain

### Memory Usage
- **Concern**: Grouping requires holding items in memory
- **Mitigation**: Same as current operators (inherent limitation)

### Key Comparison
- **Concern**: Composite key comparison slower than single key
- **Mitigation**: 
  - Implement efficient `KeyWrapper` class
  - Use hash-based lookups (O(1) average case)

---

## Implementation Checklist

### Phase 2.1 Complete When:
- [x] All operator signatures defined
- [x] Closure parameter contracts specified
- [x] Edge cases documented
- [x] Error handling strategy designed
- [x] Usage examples provided for each operator
- [x] Review with stakeholders (if applicable)
- [x] Ready to proceed to Phase 2.2 (Implementation Strategy)

---

## Next Steps (Phase 2.2)

1. **Create implementation roadmap**:
   - Estimate complexity for each operator
   - Identify shared utility code
   - Plan test strategy

2. **Design KeyExtractor utility class**:
   - Key extraction with error handling
   - Key comparison/hashing
   - Composite key support

3. **Map operator priorities**:
   - `groupTupleBy` → HIGH (most requested)
   - `joinBy` → HIGH (common pattern)
   - `combineBy` → HIGH (useful for filtering)
   - `crossBy` → LOW (already supported?)

---

## Questions to Resolve

1. **Null key behavior**: Filter out silently or error?
   - **Recommendation**: Filter with TRACE log (avoid cluttering output)

2. **Type coercion**: Auto-convert keys (e.g., Integer → String)?
   - **Recommendation**: Strict equality (user handles conversion)

3. **Composite key syntax**: List vs. Tuple vs. custom class?
   - **Recommendation**: List (simplest, familiar)

4. **Should combineBy filter or key-match**?
   - **Recommendation**: Start with predicate filter (more flexible)
   - Can add key-based variant later if needed

5. **Performance targets**: What's acceptable overhead?
   - **Recommendation**: Within 20% of index-based operators

---

## References

- Phase 1 Research: `.github/research/phase1-*.md`
- Current Groovy patterns: `groupBy`, `collectEntries`, `findAll { }`
- Nextflow operators: `GroupTupleOp.groovy`, `JoinOp.groovy`, `CrossOp.groovy`
- nf-sqldb plugin: `ChannelSqlExtension.groovy` (implementation reference)
