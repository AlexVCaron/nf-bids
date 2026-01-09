# Phase 1 Research Summary

**Completed**: 2025-01-XX  
**Duration**: Research phase completed  
**Status**: ✅ Ready for Phase 2 (Design & Architecture)

---

## Executive Summary

Phase 1 research has determined the **feasibility** and **technical approach** for extending Nextflow channel grouping operators with closure-based key extraction. The project is **viable** but requires creating **new operator names** rather than overloading existing ones.

---

## Key Decisions Made

### 1. Implementation Strategy

**Decision**: Create new operators with `By` suffix instead of overloading existing operators

**Rationale**:
- **Technical Constraint**: Nextflow plugins cannot overload core operator names
- **Clear Intent**: `groupTupleBy` vs `groupTuple` makes purpose obvious
- **Convention**: Follows Groovy/Kotlin naming (`sortBy`, `groupBy`, `findBy`)
- **Backward Compatible**: Existing code continues to work unchanged

**Operators to Implement**:
1. `groupTupleBy(Closure)` - Single channel grouping with closure
2. `joinBy(Closure)` - Dual channel join with closure
3. `combineBy(Closure)` - Dual channel combine with closure

---

### 2. Technical Architecture

**Plugin Extension Pattern**:
```groovy
class BidsExtension extends PluginExtensionPoint {
    @Override
    protected void init(Session session) { /* ... */ }
    
    @Factory
    DataflowWriteChannel fromBIDS(...) { /* ... */ }
    
    @Operator
    DataflowWriteChannel groupTupleBy(
        DataflowReadChannel source,
        Closure keyExtractor,
        Map opts = [:]
    ) { /* ... */ }
}
```

**Key Requirements**:
- Extend `PluginExtensionPoint` base class
- Use `@Factory` or `@Operator` annotations
- First parameter of operators MUST be `DataflowReadChannel`
- Return `DataflowWriteChannel`
- Public, non-static methods only
- **CRITICAL**: Nextflow plugins can only have **ONE extension point** - all factories and operators must be in the same class

---

## Research Deliverables

### Phase 1.1: Operator Comparison Table

**File**: `.github/research/phase1-operator-comparison.md`

**Key Findings**:
- **5 core grouping operators** identified:
  1. `groupTuple` (priority: HIGH)
  2. `join` (priority: HIGH)
  3. `combine` (priority: HIGH)
  4. `cross` (already has closure support ✅)
  5. `groupKey` (helper, low priority)

- **Current Limitations**:
  - All use positional indexing: `by: 0` or `by: [0, 2]`
  - Cannot extract from nested structures
  - Cannot compute keys dynamically
  - Not intuitive for map-based data

- **Reference Implementation**: `cross` operator already uses closure-based extraction

**Priority Ranking**:
| Rank | Operator | Value | Complexity |
|------|----------|-------|------------|
| 1 | `groupTuple` | Very High | High |
| 2 | `join` | High | Medium |
| 3 | `combine` | High | Medium |

---

### Phase 1.2: Plugin Extension Mechanisms

**File**: `.github/research/phase1-2-plugin-extension-mechanisms.md`

**Key Findings**:
- ❌ **Cannot overload** core operators (name collision = plugin load failure)
- ✅ **Can add** new operators with different names
- ✅ **Production example** exists: `nf-sqldb` plugin with `@Factory` and `@Operator`
- ✅ **Extension pattern** is straightforward and well-tested

**Critical Patterns**:
1. **Operator Signature**: 
   ```groovy
   @Operator
   DataflowWriteChannel name(DataflowReadChannel source, ...)
   ```

2. **Multi-Channel Operators**:
   ```groovy
   // May need to register second channel manually
   OpCall.current.get()?.inputs?.add(rightChannel)
   ```

3. **Validation**:
   ```groovy
   CheckHelper.checkParams('operatorName', opts, PARAMS_MAP)
   ```

---

### Phase 1.3: Current Patterns Analysis

**Completed**: Analysis of existing key extraction patterns

**Current State**:
```groovy
// Positional indexing only
channel.groupTuple(by: 0)           // First element
channel.groupTuple(by: [0, 2])      // Composite key from positions
channel.join(other, by: [1, 3])     // Join on positions 1 and 3
```

**Limitations**:
- Requires knowing exact positions
- Breaks when tuple structure changes
- Cannot access map keys: `it.sampleId`
- Cannot compute keys: `"${it.subject}_${it.session}"`
- Not self-documenting code

**Desired State**:
```groovy
// Closure-based extraction
channel.groupTupleBy { it.sampleId }
channel.groupTupleBy { [it.subject, it.session] }
channel.joinBy(other) { it.metadata.bids.subject }
channel.combineBy(other) { item -> item.type == 'anat' ? item.subject : null }
```

---

## Use Case Examples

### Example 1: BIDS Subject Grouping

**Current Approach** (brittle):
```groovy
// Must know exact tuple structure
channel
    .map { file -> [file.entities.subject, file] }  // Restructure
    .groupTuple(by: 0)                              // Group by position 0
    .map { subject, files -> ... }                   // Destructure
```

**With `groupTupleBy`** (intuitive):
```groovy
channel
    .groupTupleBy { it.entities.subject }
    .map { key, files -> ... }
```

### Example 2: Multi-Key Join

**Current Approach** (complex):
```groovy
ch1
    .map { it -> [it.subject + '_' + it.session, it] }  // Create composite key
    .join(
        ch2.map { it -> [it.subject + '_' + it.session, it] },
        by: 0
    )
    .map { key, left, right -> ... }  // Remove artificial key
```

**With `joinBy`** (clean):
```groovy
ch1.joinBy(ch2) { [it.subject, it.session] }
```

### Example 3: Conditional Grouping

**Current Approach** (not possible):
```groovy
// Cannot filter keys in built-in operators
channel
    .filter { it.type == 'anat' }     // Pre-filter required
    .groupTuple(by: 0)
```

**With `groupTupleBy`** (flexible):
```groovy
channel.groupTupleBy { 
    it.type == 'anat' ? it.subject : null  // Null keys filtered
}
```

---

## Technical Constraints Identified

### 1. Operator Naming

**Constraint**: Cannot reuse core operator names

**Impact**: 
- Users must learn new names
- Migration guide required
- Documentation critical

**Mitigation**:
- Use intuitive `By` suffix
- Provide migration examples
- Add aliases in docs

---

### 2. Closure Validation

**Constraint**: Runtime validation only (Groovy dynamic typing)

**Impact**:
- Cannot enforce closure signature at compile time
- Need clear error messages for wrong signatures

**Mitigation**:
- Document expected closure signature
- Validate arity (parameter count) at runtime
- Provide helpful error messages

---

### 3. Multi-Channel Tracking

**Constraint**: May not have access to `OpCall.current` from plugins

**Impact**:
- DAG visualization might not show both channels for `joinBy`/`combineBy`

**Mitigation**:
- Test with actual plugin loading
- Document known limitation if present
- File upstream issue if needed

---

### 4. Performance Overhead

**Constraint**: Closure invocation adds overhead vs. direct indexing

**Impact**:
- Slightly slower than positional indexing
- Negligible for most use cases

**Mitigation**:
- Profile performance
- Document expected overhead (~5-10%)
- Optimize hot paths if needed

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Users confused by new names | **High** | Medium | Clear docs, migration guide, examples |
| Performance overhead | Low | Low | Profile and optimize if needed |
| Plugin loading issues | Low | **High** | Follow nf-sqldb pattern exactly |
| Closure serialization | Low | Medium | Document limitations, test thoroughly |
| Breaking Nextflow updates | Medium | **High** | Pin to version, monitor releases |

---

## Success Criteria Validation

### From Original Plan

- [x] At least 4 grouping operators identified
- [x] Extension mechanism understood
- [x] Implementation pattern documented
- [x] Limitations clearly defined
- [x] Use cases validated

### Additional Validation

- [x] Production plugin example found (nf-sqldb)
- [x] Reference implementation identified (cross)
- [x] Naming convention decided
- [x] Architecture design ready

---

## Recommendations for Phase 2

### 1. Design Priorities

**Start with**: `groupTupleBy`
- Highest user impact (single channel grouping most common)
- Simpler than multi-channel operators
- Can validate pattern before scaling

**Then**: `joinBy` and `combineBy`
- Apply learned lessons
- Similar implementation pattern
- Parallel development possible

---

### 2. API Design Considerations

**Closure Signature**:
```groovy
{ item -> key }              // Single key
{ item -> [key1, key2] }     // Composite key
{ item -> null }             // Filter out (don't group)
```

**Parameter Options** (from core operators):
```groovy
groupTupleBy(closure, [
    size: Integer,           // Expected group size
    sort: Boolean/Closure,   // Sort within groups
    remainder: Boolean       // Emit incomplete groups
])
```

---

### 3. Testing Strategy

**Unit Tests** (Spock):
- Test closure invocation
- Test key extraction
- Test error handling
- Test parameter validation

**Integration Tests** (nf-test):
- Real workflow scenarios
- Complex data structures
- Edge cases (empty channels, null values)
- Performance benchmarks

---

### 4. Documentation Needs

**User Documentation**:
- Migration guide (old → new)
- API reference with examples
- Tutorial with realistic use cases
- Performance considerations

**Developer Documentation**:
- Architecture diagrams
- Implementation notes
- Testing guidelines
- Future enhancement ideas

---

## Phase 2 Readiness Checklist

- [x] Technical feasibility confirmed
- [x] Implementation approach validated
- [x] Naming convention decided
- [x] Architecture pattern selected
- [x] Reference implementations identified
- [x] Constraints documented
- [x] Risks assessed
- [x] Success criteria defined

**Status**: ✅ **READY TO PROCEED TO PHASE 2**

---

## Timeline Update

**Original Estimate**: 3-5 days for research  
**Actual**: Research phase completed efficiently  
**Remaining Phases**:
- Phase 2 (Design): 2-3 days
- Phase 3 (Implementation): 1-2 weeks
- Phase 4 (Testing/Docs): 1 week
- Phase 5 (Validation): 1 week

**Total Remaining**: ~3 weeks to MVP

---

## Next Actions

### Immediate (Phase 2.1)

1. **Design API Signatures**:
   - Define closure parameter types
   - Specify return value expectations
   - Document error conditions

2. **Create KeyExtractor Utility**:
   - Key extraction logic
   - Key normalization
   - Error handling

3. **Design Operator Classes**:
   - `GroupTupleByOp`
   - `JoinByOp`
   - `CombineByOp`

### Near-Term (Phase 2.2)

1. **Implementation Roadmap**:
   - Prioritize operators
   - Define milestones
   - Estimate effort

2. **Testing Plan**:
   - Unit test scenarios
   - Integration test cases
   - Performance benchmarks

---

## References

- Phase 1.1 Deliverable: `.github/research/phase1-operator-comparison.md`
- Phase 1.2 Deliverable: `.github/research/phase1-2-plugin-extension-mechanisms.md`
- Nextflow Core: `nextflow-io/nextflow` repository
- Reference Plugin: `nextflow-io/nf-sqldb`
- nf-bids Plugin: Current project structure

