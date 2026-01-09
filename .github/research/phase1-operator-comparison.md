# Phase 1.1: Nextflow Channel Grouping Operators - Comparison Table

**Research Date**: 2025-01-XX  
**Scope**: Operators that group/join channel items (not inner content)  
**Goal**: Identify extension opportunities for closure-based key extraction

---

## Summary

Nextflow provides **5 core grouping operators** that work with tuples/items:
1. `join` - Inner product matching by key
2. `cross` - Outer product (cartesian) filtered by matching key  
3. `combine` - Full cartesian product (optionally filtered by key)
4. `groupTuple` - Group items from single channel by key
5. `groupKey` - Helper to preserve group size through operations

**Current Key Extraction**: Index-based (`by: 0`, `by: [0,2]`) or optional closure for `cross`

---

## Detailed Operator Analysis

### 1. `join` Operator

**Purpose**: Inner product of two channels using matching keys

**Current Signature**:
```groovy
Channel.join(Channel right, Map opts = [:])
// opts: by, failOnDuplicate, failOnMismatch, remainder
```

**Current Grouping Method**:
- `by`: Integer or List<Integer> - zero-based index of tuple elements (default: `[0]`)
- Example: `by: [0, 2]` uses positions 0 and 2 as composite key

**Implementation**: `JoinOp.groovy` (~166 lines)
- Uses `PhaseOp.phaseImpl()` for buffering by key
- Index-based key extraction via `pivot` parameter
- Supports remainder and failOnDuplicate/failOnMismatch

**Extension Opportunity**:
✅ **HIGH PRIORITY** - Could accept closure to extract key from items:
```groovy
ch1.join(ch2, by: { it -> it.sample_id })
ch1.join(ch2, by: { it -> [it.subject, it.session] })
```

**Feasibility**: 
- Requires overloading or extending `by` parameter to accept `Closure<Object>` in addition to `Integer/List<Integer>`
- Closure would be called per item to extract key instead of positional indexing
- Compatible with existing index-based API

---

### 2. `cross` Operator

**Purpose**: Outer product filtered by matching keys (all pairs where keys match)

**Current Signature**:
```groovy
Channel.cross(Channel right, Closure mapper = null)
```

**Current Grouping Method**:
- Default: First element of tuple/list, or value itself
- Optional: Closure mapper to extract key from item
- **Already supports closure-based extraction!** ✅

**Implementation**: `CrossOp.groovy` (~83 lines)
- Uses `PhaseOp.phaseImpl()` with mapper closure
- Mapper defaults to `OperatorImpl.DEFAULT_MAPPING_CLOSURE`

**Extension Opportunity**:
⚠️ **ALREADY IMPLEMENTED** - Cross already accepts optional closure:
```groovy
ch1.cross(ch2) { item -> item.key }
```

**Priority**: LOW - No changes needed, use as reference implementation

---

### 3. `combine` Operator

**Purpose**: Cartesian product of two channels (optionally filtered by key)

**Current Signature**:
```groovy
Channel.combine(Channel/List right, Map opts = [:])
// opts: by
```

**Current Grouping Method**:
- `by`: Integer or List<Integer> - combine only items with matching keys at these indices
- Without `by`: full cartesian product (no filtering)

**Implementation**: `CombineOp.groovy` (~213 lines)
- Uses `setPivot()` to configure key indices
- Calls `makeKey()` to extract key from specified positions
- Flattens and merges matched pairs

**Extension Opportunity**:
✅ **HIGH PRIORITY** - Add closure-based key extraction:
```groovy
ch1.combine(ch2, by: { it -> it.barcode })
ch1.combine(right, by: { l, r -> [l.id, r.category] })
```

**Feasibility**:
- Similar to `join`, extend `by` to accept closures
- Could accept single closure (apply to both) or tuple of closures (left/right)

---

### 4. `groupTuple` Operator

**Purpose**: Group tuples from single channel by key

**Current Signature**:
```groovy
Channel.groupTuple(Map opts = [:])
// opts: by, size, sort, remainder
```

**Current Grouping Method**:
- `by`: Integer or List<Integer> - indices to use as grouping key (default: `[0]`)
- Groups items: `(K, V1, V2)` → `(K, [V1s], [V2s])`

**Implementation**: `GroupTupleOp.groovy` (~236 lines)
- Uses `normalizeKey()` to extract key from indices
- Buffers items in `groups` map by key
- Supports sorting, size constraints, remainder

**Extension Opportunity**:
✅ **HIGH PRIORITY** - Closure-based key extraction:
```groovy
channel.groupTuple(by: { it -> it.subject })
channel.groupTuple(by: { it -> "${it[0]}_${it[2]}" })
```

**Feasibility**:
- Extend `by` parameter to accept `Closure<Object>`
- Call closure to compute key instead of index extraction
- Most complex: handles sorting, sizing, remainder logic

---

### 5. `groupKey` Helper

**Purpose**: Preserve expected group size through flatMap/splitting operations

**Current Signature**:
```groovy
groupKey(Object key, int groupSize)
```

**Current Method**:
- Wraps key with size metadata
- Used with `groupTuple()` to enforce expected group sizes

**Extension Opportunity**:
⚠️ **LOW PRIORITY** - Already works with any key type including computed keys

---

## Other Related Operators (Lower Priority)

### `collect` / `toList` / `toSortedList`
- **Purpose**: Aggregate all items into single collection
- **Current Method**: No grouping - collects everything
- **Extension**: Could add grouping parameter for partial collection
- **Priority**: MEDIUM - useful but less critical

### `reduce`
- **Purpose**: Accumulate with custom function
- **Current Method**: Closure with accumulator and item
- **Extension**: Pre-grouping before reduce
- **Priority**: LOW - can chain with `groupTuple`

### `collectFile`
- **Purpose**: Write items to files, optionally grouped
- **Current Method**: Closure returns `[filename, content]` pair
- **Extension**: Already closure-based ✅
- **Priority**: LOW - no changes needed

### `branch` / `multiMap`
- **Purpose**: Route items to multiple output channels
- **Current Method**: Closure-based routing logic
- **Extension**: Not grouping operators (routing, not joining)
- **Priority**: OUT OF SCOPE

---

## Priority Ranking

| Rank | Operator | Current Limitation | Extension Value | Complexity |
|------|----------|-------------------|-----------------|------------|
| 1 | `groupTuple` | Index-only grouping | Very High - single channel grouping is most common | High - most complex implementation |
| 2 | `join` | Index-only matching | High - dual channel matching critical | Medium - similar to cross |
| 3 | `combine` | Index-only filtering | High - common use case | Medium - similar to join |
| 4 | `cross` | ✅ Already has closure | None - reference impl | N/A |
| 5 | `collect` | No grouping | Medium - partial collection useful | Low - new feature |

---

## Implementation Patterns from Source Code

### Key Extraction Pattern (from `cross`)
```groovy
// CrossOp.groovy - Reference implementation
private Closure mapper = OperatorImpl.DEFAULT_MAPPING_CLOSURE

CrossOp setMapper(Closure mapper) {
    this.mapper = mapper ?: OperatorImpl.DEFAULT_MAPPING_CLOSURE
    return this
}

// In apply():
def entries = PhaseOp.phaseImpl(buffer, size, index, it, mapper, true)
```

**Key insight**: `cross` already demonstrates closure-based key extraction pattern!

### Index-Based Extraction Pattern (from `groupTuple`)
```groovy
// GroupTupleOp.groovy
private List<Integer> indices  // e.g., [0] or [0, 2]

private void collect(List tuple) {
    final key = normalizeKey(tuple[indices])  // Extract by positions
    final items = groups.getOrCreate(key) { /* ... */ }
    // ... grouping logic
}

static private List<Integer> getGroupTupleIndices(Map params) {
    if (params?.by == null) return [0]
    if (params.by instanceof List) return params.by as List<Integer>
    if (params.by instanceof Integer) return [params.by as Integer]
    throw new IllegalArgumentException("Invalid by index")
}
```

### Parameter Validation Pattern
```groovy
// JoinOp.groovy
static final private Map JOIN_PARAMS = [
    remainder: Boolean, 
    by: [List, Integer], 
    failOnMismatch: Boolean, 
    failOnDuplicate: Boolean
]

JoinOp(DataflowReadChannel source, DataflowReadChannel target, Map params = null) {
    CheckHelper.checkParams('join', params, JOIN_PARAMS)
    this.pivot = params?.by ? asList(params.by) : [0]
    // ...
}
```

---

## Technical Constraints

### Type System
- `by` parameter currently typed as `[List, Integer]` in param maps
- Need to extend to `[List, Integer, Closure]`
- Groovy dynamic typing makes this straightforward

### Default Behavior
- **Must preserve backwards compatibility**
- Default `by: [0]` must still work
- Closure is optional enhancement

### Closure Signature
- Single-parameter: `{ item -> key }`
- Could support two-parameter for binary ops: `{ left, right -> key }`
- Should support composite keys: `{ it -> [it.subject, it.session] }`

### Key Normalization
- Existing `normalizeKey()` methods handle Lists, GroupKey wrapper
- Should work transparently with closure-extracted keys

---

## Next Steps (Phase 1.2)

1. ✅ Study `cross` implementation as reference for closure support
2. Examine plugin extension points for operator overloading:
   - `PluginExtensionPoint` class
   - `@Operator` annotation
   - `OperatorImpl` extension mechanism
3. Test feasibility of adding closure parameter to existing operators
4. Check if overloading is possible or if new operator names required

---

## Questions for Phase 1.2

1. **Can we overload existing operators** or must we create new names?
   - E.g., `join` vs `joinBy` vs extending `join` signature
   
2. **How do plugins extend core operators?**
   - Do plugins add new operators or modify existing ones?
   - What's the registration mechanism?

3. **Are there examples of operators with multiple parameter type options?**
   - Look for operators that accept different types for same parameter
   - Pattern to follow for `by: Integer | List<Integer> | Closure`

---

## References

**Source Files Examined**:
- `nextflow/extension/JoinOp.groovy` - Join implementation
- `nextflow/extension/CrossOp.groovy` - Cross with closure support ✅
- `nextflow/extension/CombineOp.groovy` - Combine implementation
- `nextflow/extension/GroupTupleOp.groovy` - GroupTuple implementation
- `nextflow/extension/OperatorImpl.groovy` - Base operator class
- `nextflow/plugin/extension/PluginExtensionPoint.groovy` - Plugin extension base
- `nextflow/plugin/extension/Operator.groovy` - Operator annotation
- `docs/reference/operator.md` - Official operator documentation

**Nextflow Repository**: `https://github.com/nextflow-io/nextflow`

**Related Plugins**:
- `nf-sqldb` - Custom operators example
- `nf-validation` - Schema validation operators

