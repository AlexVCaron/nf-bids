# Implementation Checklist: Phases 3 & 4
## Closure-Based Channel Grouping Operators

**Project**: Extend Nextflow with closure-based key extraction for channel operators  
**Start Date**: 2025-01-20  
**Estimated Duration**: 3-4 weeks  

---

## Phase 3: Implementation

### Sprint 1: Foundation (Week 1, ~10 hours)

#### Task 3.1.1: Create CompositeKey Class ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/channel/keys/CompositeKey.groovy`

**Requirements**:
- [x] Create package directory: `src/main/groovy/nfneuro/plugin/channel/keys/`
- [x] Implement `CompositeKey` class with:
  - [x] `final List<Object> parts` field
  - [x] Constructor accepting `List<Object>`
  - [x] `size()` method
  - [x] `get(int index)` method
  - [x] `@EqualsAndHashCode` annotation
  - [x] `@ToString(includePackage = false)` annotation
  - [x] `@CompileStatic` annotation

**Acceptance Criteria**:
- [x] Class compiles without errors
- [x] Proper equality semantics for list keys
- [x] HashCode consistent with equals

**Actual Time**: 1 hour

---

#### Task 3.1.2: Create KeyExtractor Utility ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/channel/KeyExtractor.groovy`

**Requirements**:
- [x] Create package directory: `src/main/groovy/nfneuro/plugin/channel/`
- [x] Implement `KeyExtractor` utility class with:
  - [x] `static Object extractKey(Object, Closure, String)` method
    - [x] Validate keyExtractor not null
    - [x] Call closure with item
    - [x] Handle null return (log TRACE, return null)
    - [x] Wrap List keys in CompositeKey
    - [x] Catch exceptions with clear error message
  - [x] `static void validateKeyExtractor(Closure, String)` method
    - [x] Check closure not null
    - [x] Check arity >= 1 (getMaximumNumberOfParameters)
    - [x] Throw IllegalArgumentException with helpful message
    - [x] Warn if arity > 2
  - [x] `static boolean keysEqual(Object, Object)` method
    - [x] Handle null keys (return false)
    - [x] Use == for comparison
- [x] Add `@CompileStatic` annotation
- [x] Add `@Slf4j` annotation

**Acceptance Criteria**:
- [x] All methods compile and work correctly
- [x] Error messages are clear and actionable
- [x] Logging at appropriate levels

**Actual Time**: 2 hours

---

#### Task 3.1.3: Create Unit Tests for Foundation
**Files**: 
- `src/test/groovy/nfneuro/plugin/channel/keys/CompositeKeyTest.groovy`
- `src/test/groovy/nfneuro/plugin/channel/KeyExtractorTest.groovy`

**Test Cases for CompositeKey**:
- [x] Test equality for identical lists
- [x] Test inequality for different lists
- [x] Test equality for different list instances with same content
- [x] Test hashCode consistency
- [x] Test with empty lists
- [x] Test with nested structures
- [x] Test toString output
- [x] Test get() and size() methods

**Test Cases for KeyExtractor**:
- [x] Test extractKey with simple closure
- [x] Test extractKey with nested field access
- [x] Test extractKey with computed value
- [x] Test extractKey returning List (becomes CompositeKey)
- [x] Test extractKey returning null
- [x] Test extractKey throwing exception
- [x] Test validateKeyExtractor with null closure
- [x] Test validateKeyExtractor with zero-arity closure
- [x] Test validateKeyExtractor with valid closure
- [x] Test validateKeyExtractor with multi-parameter closure
- [x] Test keysEqual with matching keys
- [x] Test keysEqual with different keys
- [x] Test keysEqual with null keys

**Acceptance Criteria**:
- [x] All tests pass (30 tests)
- [x] Code coverage > 95% for both classes
- [x] Edge cases covered

**Actual Time**: 3 hours

---

#### Task 3.1.4: Create ChannelGroupingExtension Skeleton ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/channel/ChannelGroupingExtension.groovy`

**Requirements**:
- [x] Create class extending `PluginExtensionPoint`
- [x] Add `@CompileStatic` annotation
- [x] Implement `init(Session)` method
  - [x] Store session reference
- [x] Add method stubs (no implementation yet):
  - [x] `groupTupleBy(DataflowReadChannel, Closure, Map)` with `@Operator`
  - [x] `joinBy(DataflowReadChannel, DataflowReadChannel, Closure, Closure, Map)` with `@Operator`
  - [x] `combineBy(DataflowReadChannel, DataflowReadChannel, Closure, Map)` with `@Operator`
- [x] Each stub should:
  - [x] Validate keyExtractor/predicate
  - [x] Throw `UnsupportedOperationException("Not yet implemented")`

**Acceptance Criteria**:
- [x] Class compiles
- [x] Plugin structure correct
- [x] Ready for operator implementations

**Actual Time**: 2 hours

---

#### Task 3.1.5: Register Extension in Plugin ✅ COMPLETE
**File**: `src/resources/META-INF/extensions.idx`

**Requirements**:
- [x] Add line: `nfneuro.plugin.channel.ChannelGroupingExtension`
- [x] Verify file exists and is properly formatted
- [x] Test plugin loads without errors

**Acceptance Criteria**:
- [x] Plugin loads successfully
- [x] Extension is registered
- [x] No conflicts with existing extensions

**Actual Time**: 30 minutes

---

## ✅ Sprint 1 Complete! Foundation is Ready

**Summary**: 
- Created CompositeKey wrapper for multi-part keys
- Created KeyExtractor utility with validation and error handling
- Implemented 30 comprehensive unit tests (100% pass rate)
- Created ChannelGroupingExtension skeleton with 3 operator stubs
- Registered extension in plugin (extensions.idx)
- All code compiles successfully

**Files Created**:
1. `src/main/groovy/nfneuro/plugin/channel/keys/CompositeKey.groovy` (72 lines)
2. `src/main/groovy/nfneuro/plugin/channel/KeyExtractor.groovy` (106 lines)
3. `src/test/groovy/nfneuro/plugin/channel/keys/CompositeKeyTest.groovy` (154 lines)
4. `src/test/groovy/nfneuro/plugin/channel/KeyExtractorTest.groovy` (213 lines)
5. `src/main/groovy/nfneuro/plugin/channel/ChannelGroupingExtension.groovy` (154 lines)
6. `src/main/resources/META-INF/extensions.idx` (2 lines)

**Actual Time**: ~8 hours (vs. estimated 10 hours)

---

### Sprint 2: GroupTupleBy Operator (Week 1-2, ~15 hours)

#### Task 3.2.1: Implement GroupTupleByOp Class
**File**: `src/main/groovy/nfneuro/plugin/channel/ops/GroupTupleByOp.groovy`

**Requirements**:
- [ ] Create package directory: `src/main/groovy/nfneuro/plugin/channel/ops/`
- [ ] Implement class with:
  - [ ] Private fields:
    - [ ] `DataflowReadChannel source`
    - [ ] `Closure keyExtractor`
    - [ ] `Map opts`
    - [ ] `DataflowWriteChannel target`
    - [ ] `Map<Object, List<Object>> groups`
    - [ ] `Map<Object, Integer> counts`
  - [ ] Constructor accepting `(source, keyExtractor, opts)`
  - [ ] `DataflowWriteChannel apply()` method:
    - [ ] Create target channel with `CH.createBy(source)`
    - [ ] Subscribe to source with `DataflowHelper.subscribeImpl`
    - [ ] Set `onNext: this.&onNext`
    - [ ] Set `onComplete: this.&onComplete`
    - [ ] Return target
  - [ ] `private void onNext(Object item)` method:
    - [ ] Extract key using `KeyExtractor.extractKey`
    - [ ] Skip if key is null
    - [ ] Initialize group if not exists
    - [ ] Add item to group
    - [ ] Increment count
    - [ ] If size option set and count >= size, call `emitGroup(key)`
  - [ ] `private void onComplete()` method:
    - [ ] Check remainder option (default true)
    - [ ] If remainder, emit all remaining groups
    - [ ] Bind `Channel.STOP` to target
  - [ ] `private void emitGroup(Object key)` method:
    - [ ] Get and remove group from map
    - [ ] Return if null/empty
    - [ ] Apply sort if requested:
      - [ ] Boolean true → natural sort
      - [ ] Closure → sort with closure
      - [ ] Comparator → sort with comparator
    - [ ] Bind `[key, items]` tuple to target
- [ ] Add `@CompileStatic` annotation
- [ ] Add `@Slf4j` annotation

**Acceptance Criteria**:
- [ ] Class compiles
- [ ] Follows pattern from Nextflow's GroupTupleOp
- [ ] All options supported (size, sort, remainder)

**Estimated Time**: 6-8 hours

---

#### Task 3.2.2: Integrate GroupTupleBy into Extension
**File**: `src/main/groovy/nfneuro/plugin/channel/ChannelGroupingExtension.groovy`

**Requirements**:
- [ ] Import `GroupTupleByOp`
- [ ] Replace `groupTupleBy` stub implementation:
  - [ ] Validate keyExtractor with `KeyExtractor.validateKeyExtractor`
  - [ ] Create `GroupTupleByOp` instance
  - [ ] Call `apply()` and return result
- [ ] Add parameter validation for opts map

**Acceptance Criteria**:
- [ ] Method compiles
- [ ] Operator callable from workflow script
- [ ] Options properly passed through

**Estimated Time**: 1 hour

---

#### Task 3.2.3: Create Unit Tests for GroupTupleByOp
**File**: `src/test/groovy/nfneuro/plugin/channel/ops/GroupTupleByOpTest.groovy`

**Test Cases**:
- [ ] Test basic grouping with simple key
  - [ ] Input: `[[id:'A', val:1], [id:'A', val:2], [id:'B', val:3]]`
  - [ ] Expected: `['A', [[id:'A', val:1], [id:'A', val:2]]], ['B', [[id:'B', val:3]]]`
- [ ] Test nested field extraction
  - [ ] Input: `[[meta:[sub:'01'], file:'f1'], [meta:[sub:'01'], file:'f2']]`
  - [ ] Key: `{ it.meta.sub }`
  - [ ] Expected: Grouped by subject
- [ ] Test composite key (multiple fields)
  - [ ] Input: Items with subject and session
  - [ ] Key: `{ [it.subject, it.session] }`
  - [ ] Expected: Grouped by combination
- [ ] Test with size option
  - [ ] Input: Stream of items
  - [ ] Option: `size: 2`
  - [ ] Expected: Emit when group reaches size 2
- [ ] Test with sort option (boolean)
  - [ ] Input: Unsorted items
  - [ ] Option: `sort: true`
  - [ ] Expected: Items sorted within groups
- [ ] Test with sort option (closure)
  - [ ] Input: Items with values
  - [ ] Option: `sort: { it.value }`
  - [ ] Expected: Items sorted by value
- [ ] Test with remainder: false
  - [ ] Input: Incomplete groups
  - [ ] Option: `remainder: false`
  - [ ] Expected: Incomplete groups not emitted
- [ ] Test with null keys
  - [ ] Input: Items where some return null
  - [ ] Expected: Null-key items skipped
- [ ] Test with empty channel
  - [ ] Input: Empty channel
  - [ ] Expected: Empty output
- [ ] Test with closure exception
  - [ ] Input: Item that causes closure to throw
  - [ ] Expected: Clear error message

**Acceptance Criteria**:
- [ ] All tests pass
- [ ] Code coverage > 90%
- [ ] Edge cases covered

**Estimated Time**: 4-5 hours

---

#### Task 3.2.4: Create Integration Test for GroupTupleBy
**File**: `tests/plugin/groupTupleBy.nf.test`

**Test Workflow**:
```groovy
include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    Channel.of(
        [sample: 'A', file: 'a1.txt'],
        [sample: 'A', file: 'a2.txt'],
        [sample: 'B', file: 'b1.txt']
    )
    .groupTupleBy { it.sample }
    .view()
}
```

**Test Cases**:
- [ ] Test basic grouping scenario
- [ ] Test with BIDS-like data structure
- [ ] Test with file paths
- [ ] Test size-based emission
- [ ] Verify output format matches expected

**Acceptance Criteria**:
- [ ] Integration test passes
- [ ] Works in real pipeline context
- [ ] Output matches expectations

**Estimated Time**: 3-4 hours

---

### Sprint 3: JoinBy Operator (Week 2, ~22 hours)

#### Task 3.3.1: Implement JoinByOp Class
**File**: `src/main/groovy/nfneuro/plugin/channel/ops/JoinByOp.groovy`

**Requirements**:
- [ ] Implement class with:
  - [ ] Private fields:
    - [ ] `DataflowReadChannel left`
    - [ ] `DataflowReadChannel right`
    - [ ] `Closure leftKeyExtractor`
    - [ ] `Closure rightKeyExtractor`
    - [ ] `Map opts`
    - [ ] `DataflowWriteChannel target`
    - [ ] `Map<Object, List<Object>> leftBuffer`
    - [ ] `Map<Object, List<Object>> rightBuffer`
    - [ ] `Set<Object> matchedKeys`
    - [ ] `boolean leftComplete = false`
    - [ ] `boolean rightComplete = false`
  - [ ] Constructor accepting `(left, right, leftExtractor, rightExtractor, opts)`
  - [ ] `DataflowWriteChannel apply()` method:
    - [ ] Create target channel
    - [ ] Subscribe to left with `onNext: { onLeftItem(it) }`, `onComplete: { onLeftComplete() }`
    - [ ] Subscribe to right with `onNext: { onRightItem(it) }`, `onComplete: { onRightComplete() }`
    - [ ] Return target
  - [ ] `private synchronized void onLeftItem(Object item)`:
    - [ ] Extract key
    - [ ] Skip if null
    - [ ] Buffer left item
    - [ ] Check right buffer for matches
    - [ ] Emit `[leftItem, rightItem]` for each match
    - [ ] Track matched keys
  - [ ] `private synchronized void onRightItem(Object item)`:
    - [ ] Extract key
    - [ ] Skip if null
    - [ ] Buffer right item
    - [ ] Check left buffer for matches
    - [ ] Emit `[leftItem, rightItem]` for each match
    - [ ] Track matched keys
  - [ ] `private synchronized void onLeftComplete()`:
    - [ ] Set leftComplete = true
    - [ ] Call `checkCompletion()`
  - [ ] `private synchronized void onRightComplete()`:
    - [ ] Set rightComplete = true
    - [ ] Call `checkCompletion()`
  - [ ] `private void checkCompletion()`:
    - [ ] Return if either not complete
    - [ ] If remainder option, call `emitRemainder()`
    - [ ] Bind `Channel.STOP`
  - [ ] `private void emitRemainder()`:
    - [ ] Emit unmatched left items as `[item, null]`
    - [ ] Emit unmatched right items as `[null, item]`
- [ ] Add `@CompileStatic` annotation
- [ ] Add `@Slf4j` annotation

**Acceptance Criteria**:
- [ ] Class compiles
- [ ] Thread-safe with synchronized methods
- [ ] Follows pattern from Nextflow's JoinOp
- [ ] Handles cartesian product for duplicate keys

**Estimated Time**: 10-12 hours

---

#### Task 3.3.2: Integrate JoinBy into Extension
**File**: `src/main/groovy/nfneuro/plugin/channel/ChannelGroupingExtension.groovy`

**Requirements**:
- [ ] Import `JoinByOp`
- [ ] Replace `joinBy` stub implementation:
  - [ ] Validate leftKeyExtractor
  - [ ] Default rightKeyExtractor to leftKeyExtractor if null
  - [ ] Validate rightKeyExtractor
  - [ ] Create `JoinByOp` instance
  - [ ] Call `apply()` and return result
- [ ] Validate opts map
- [ ] Register right channel as input (for DAG)

**Acceptance Criteria**:
- [ ] Method compiles
- [ ] Both extractors validated
- [ ] Default behavior works

**Estimated Time**: 1-2 hours

---

#### Task 3.3.3: Create Unit Tests for JoinByOp
**File**: `src/test/groovy/nfneuro/plugin/channel/ops/JoinByOpTest.groovy`

**Test Cases**:
- [ ] Test simple join with same key extractor
  - [ ] Left: `[[id:'A', val:1], [id:'B', val:2]]`
  - [ ] Right: `[[id:'A', data:'x'], [id:'B', data:'y']]`
  - [ ] Expected: Matched pairs
- [ ] Test with different extractors
  - [ ] Left: `{ it.subject }`
  - [ ] Right: `{ it.participant }`
  - [ ] Expected: Join on different field names
- [ ] Test duplicate keys (cartesian product)
  - [ ] Left: `[[id:'A', val:1], [id:'A', val:2]]`
  - [ ] Right: `[[id:'A', data:'x'], [id:'A', data:'y']]`
  - [ ] Expected: 4 combinations
- [ ] Test with remainder: true (outer join)
  - [ ] Left has unmatched items
  - [ ] Right has unmatched items
  - [ ] Expected: Emit with null partner
- [ ] Test with empty left channel
  - [ ] Expected: Empty output (unless remainder)
- [ ] Test with empty right channel
  - [ ] Expected: Empty output (unless remainder)
- [ ] Test with null keys
  - [ ] Expected: Items with null keys skipped
- [ ] Test completion synchronization
  - [ ] Verify proper cleanup when both channels complete

**Acceptance Criteria**:
- [ ] All tests pass
- [ ] Thread safety verified
- [ ] Code coverage > 90%

**Estimated Time**: 6-8 hours

---

#### Task 3.3.4: Create Integration Test for JoinBy
**File**: `tests/plugin/joinBy.nf.test`

**Test Workflow**:
```groovy
include { joinBy } from 'plugin/nf-bids'

workflow {
    anatomical = Channel.of(
        [subject: 'sub-01', type: 'T1', file: 't1.nii'],
        [subject: 'sub-02', type: 'T1', file: 't1.nii']
    )
    
    functional = Channel.of(
        [subject: 'sub-01', type: 'bold', file: 'bold.nii'],
        [subject: 'sub-02', type: 'bold', file: 'bold.nii']
    )
    
    anatomical.joinBy(functional) { it.subject }
}
```

**Test Cases**:
- [ ] Test basic join
- [ ] Test with BIDS metadata
- [ ] Test with different field names
- [ ] Test remainder option
- [ ] Verify pair format

**Acceptance Criteria**:
- [ ] Integration test passes
- [ ] Works in pipeline context
- [ ] Handles realistic data

**Estimated Time**: 3-4 hours

---

### Sprint 4: CombineBy Operator (Week 2-3, ~15 hours)

#### Task 3.4.1: Implement CombineByOp Class
**File**: `src/main/groovy/nfneuro/plugin/channel/ops/CombineByOp.groovy`

**Requirements**:
- [ ] Implement class with:
  - [ ] Private fields:
    - [ ] `DataflowReadChannel left`
    - [ ] `DataflowReadChannel right`
    - [ ] `Closure filterPredicate`
    - [ ] `Map opts`
    - [ ] `DataflowWriteChannel target`
    - [ ] `List<Object> leftBuffer`
    - [ ] `List<Object> rightBuffer`
    - [ ] `boolean leftComplete = false`
    - [ ] `boolean rightComplete = false`
  - [ ] Constructor accepting `(left, right, filterPredicate, opts)`
  - [ ] `DataflowWriteChannel apply()` method:
    - [ ] Create target channel
    - [ ] Subscribe to left
    - [ ] Subscribe to right
    - [ ] Return target
  - [ ] `private synchronized void onLeftItem(Object item)`:
    - [ ] Add to leftBuffer
    - [ ] Combine with all existing rightBuffer items
    - [ ] Call `emitIfValid` for each combination
  - [ ] `private synchronized void onRightItem(Object item)`:
    - [ ] Add to rightBuffer
    - [ ] Combine with all existing leftBuffer items
    - [ ] Call `emitIfValid` for each combination
  - [ ] `private void emitIfValid(Object left, Object right)`:
    - [ ] If no filter, emit `[left, right]`
    - [ ] If filter exists, call predicate
    - [ ] If predicate returns true, emit `[left, right]`
    - [ ] Catch exceptions with clear error
  - [ ] `private synchronized void onLeftComplete()`:
    - [ ] Set leftComplete = true
    - [ ] Call `checkCompletion()`
  - [ ] `private synchronized void onRightComplete()`:
    - [ ] Set rightComplete = true
    - [ ] Call `checkCompletion()`
  - [ ] `private void checkCompletion()`:
    - [ ] If both complete, bind `Channel.STOP`
- [ ] Add `@CompileStatic` annotation
- [ ] Add `@Slf4j` annotation

**Acceptance Criteria**:
- [ ] Class compiles
- [ ] Thread-safe with synchronized methods
- [ ] Follows pattern from Nextflow's CombineOp
- [ ] Filter optional (null = all combinations)

**Estimated Time**: 6-8 hours

---

#### Task 3.4.2: Integrate CombineBy into Extension
**File**: `src/main/groovy/nfneuro/plugin/channel/ChannelGroupingExtension.groovy`

**Requirements**:
- [ ] Import `CombineByOp`
- [ ] Replace `combineBy` stub implementation:
  - [ ] Validate filterPredicate if not null
  - [ ] Check arity >= 2
  - [ ] Create `CombineByOp` instance
  - [ ] Call `apply()` and return result
- [ ] Register right channel as input (for DAG)

**Acceptance Criteria**:
- [ ] Method compiles
- [ ] Filter validation correct
- [ ] Works with and without filter

**Estimated Time**: 1 hour

---

#### Task 3.4.3: Create Unit Tests for CombineByOp
**File**: `src/test/groovy/nfneuro/plugin/channel/ops/CombineByOpTest.groovy`

**Test Cases**:
- [ ] Test without filter (all combinations)
  - [ ] Left: `['A', 'B']`
  - [ ] Right: `[1, 2]`
  - [ ] Expected: `[['A',1], ['A',2], ['B',1], ['B',2]]`
- [ ] Test with filter (selective combinations)
  - [ ] Filter: `{ l, r -> l == r }`
  - [ ] Expected: Only matching combinations
- [ ] Test with filter returning false for all
  - [ ] Expected: Empty output
- [ ] Test with empty left channel
  - [ ] Expected: Empty output
- [ ] Test with empty right channel
  - [ ] Expected: Empty output
- [ ] Test filter exception handling
  - [ ] Filter throws exception
  - [ ] Expected: Clear error message
- [ ] Test complex filter logic
  - [ ] Filter based on multiple conditions
  - [ ] Expected: Correct filtering

**Acceptance Criteria**:
- [ ] All tests pass
- [ ] Thread safety verified
- [ ] Code coverage > 90%

**Estimated Time**: 4-5 hours

---

#### Task 3.4.4: Create Integration Test for CombineBy
**File**: `tests/plugin/combineBy.nf.test`

**Test Workflow**:
```groovy
include { combineBy } from 'plugin/nf-bids'

workflow {
    subjects = Channel.of('sub-01', 'sub-02', 'sub-03')
    sessions = Channel.of('ses-01', 'ses-02')
    
    // Only combine if session number <= subject number
    subjects.combineBy(sessions) { subj, sess ->
        sess.split('-')[1] <= subj.split('-')[1]
    }
}
```

**Test Cases**:
- [ ] Test without filter
- [ ] Test with filter
- [ ] Test with BIDS-like matching
- [ ] Verify combination logic

**Acceptance Criteria**:
- [ ] Integration test passes
- [ ] Works in pipeline context
- [ ] Filter logic correct

**Estimated Time**: 3-4 hours

---

## Phase 4: Testing & Documentation

### Sprint 5: Comprehensive Testing (Week 3, ~10 hours)

#### Task 4.1.1: Performance Benchmarks
**File**: `tests/plugin/performance/benchmark.nf.test`

**Requirements**:
- [ ] Create benchmark comparing:
  - [ ] `groupTuple(by: 0)` vs `groupTupleBy { it[0] }`
  - [ ] `join(by: 0)` vs `joinBy { it[0] }`
- [ ] Test with various data volumes:
  - [ ] 100 items
  - [ ] 1,000 items
  - [ ] 10,000 items
- [ ] Measure:
  - [ ] Execution time
  - [ ] Memory usage
- [ ] Document results

**Acceptance Criteria**:
- [ ] Performance within 20% of index-based operators
- [ ] No memory leaks detected
- [ ] Results documented

**Estimated Time**: 4-6 hours

---

#### Task 4.1.2: Edge Case Testing
**File**: `tests/plugin/edge_cases.nf.test`

**Test Cases**:
- [ ] Test with very large items (megabytes each)
- [ ] Test with many small items (100k+)
- [ ] Test with complex nested structures
- [ ] Test with closure accessing non-existent fields
- [ ] Test with closure modifying items (should work)
- [ ] Test with serialization of closures
- [ ] Test concurrent execution (parallel processes)

**Acceptance Criteria**:
- [ ] All edge cases handled gracefully
- [ ] Error messages clear
- [ ] No crashes or hangs

**Estimated Time**: 4-6 hours

---

### Sprint 5: Documentation (Week 3, ~10 hours)

#### Task 4.2.1: API Documentation
**File**: `docs/channel-extensions/README.md`

**Requirements**:
- [ ] Create documentation structure:
  ```
  docs/channel-extensions/
  ├── README.md          (overview)
  ├── groupTupleBy.md    (detailed)
  ├── joinBy.md          (detailed)
  ├── combineBy.md       (detailed)
  └── examples/
      ├── basic-grouping.nf
      ├── bids-subject-grouping.nf
      ├── multi-key-join.nf
      └── filtered-combine.nf
  ```

**Content for Each Operator Doc**:
- [ ] Overview and purpose
- [ ] Signature and parameters
- [ ] Usage examples (3-5 per operator)
- [ ] Options documentation
- [ ] Edge cases and gotchas
- [ ] Performance notes
- [ ] Comparison with index-based approach

**Acceptance Criteria**:
- [ ] All operators documented
- [ ] Examples are runnable
- [ ] Clear and beginner-friendly

**Estimated Time**: 6-8 hours

---

#### Task 4.2.2: Migration Guide
**File**: `docs/channel-extensions/MIGRATION_GUIDE.md`

**Requirements**:
- [ ] Create guide showing:
  - [ ] When to use new operators vs old
  - [ ] Side-by-side comparisons
  - [ ] Common patterns converted
  - [ ] Benefits of closure-based approach
- [ ] Examples:
  ```groovy
  // OLD
  channel.groupTuple(by: 0)
  
  // NEW
  channel.groupTupleBy { it[0] }           // Index
  channel.groupTupleBy { it.subject }      // Semantic
  ```

**Acceptance Criteria**:
- [ ] Clear conversion examples
- [ ] Addresses common use cases
- [ ] Explains benefits

**Estimated Time**: 2-3 hours

---

#### Task 4.2.3: Update Main Plugin README
**File**: `README.md`

**Requirements**:
- [ ] Add section: "Channel Grouping Extensions"
- [ ] Brief overview of new operators
- [ ] Quick example for each
- [ ] Link to detailed docs
- [ ] Update changelog

**Acceptance Criteria**:
- [ ] README updated
- [ ] Examples clear
- [ ] Links work

**Estimated Time**: 1-2 hours

---

## Final Checklist

### Code Quality
- [ ] All code has `@CompileStatic` annotation
- [ ] All code has appropriate logging (`@Slf4j`)
- [ ] No compiler warnings
- [ ] Code follows project style guide
- [ ] No TODOs or FIXMEs left in code

### Testing
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Code coverage > 95% for utilities
- [ ] Code coverage > 90% for operators
- [ ] Performance benchmarks completed
- [ ] Edge cases tested

### Documentation
- [ ] All operators documented
- [ ] API docs complete
- [ ] Usage examples provided
- [ ] Migration guide written
- [ ] README updated
- [ ] Changelog updated

### Plugin Integration
- [ ] Extension registered in `extensions.idx`
- [ ] Plugin loads without errors
- [ ] Operators callable from workflows
- [ ] No conflicts with existing functionality
- [ ] Version number updated

### Pre-Release Validation
- [ ] Test with simple workflow
- [ ] Test with complex BIDS workflow
- [ ] Test with existing nf-bids features
- [ ] Performance acceptable
- [ ] Error messages helpful
- [ ] Documentation accurate

---

## Estimated Total Time

### Phase 3: Implementation
| Sprint | Tasks | Estimated Time |
|--------|-------|----------------|
| Sprint 1: Foundation | 5 tasks | ~10 hours |
| Sprint 2: GroupTupleBy | 4 tasks | ~15 hours |
| Sprint 3: JoinBy | 4 tasks | ~22 hours |
| Sprint 4: CombineBy | 4 tasks | ~15 hours |
| **Total** | **17 tasks** | **~62 hours** |

### Phase 4: Testing & Documentation
| Sprint | Tasks | Estimated Time |
|--------|-------|----------------|
| Sprint 5: Testing | 2 tasks | ~10 hours |
| Sprint 5: Documentation | 3 tasks | ~10 hours |
| **Total** | **5 tasks** | **~20 hours** |

### Grand Total
**22 tasks across 5 sprints = ~82 hours (10-11 working days)**

---

## Success Criteria

### Functional
✅ All three operators implemented and working  
✅ All options supported (size, sort, remainder, etc.)  
✅ Error handling comprehensive  
✅ Thread-safe operation  

### Quality
✅ Code coverage > 90%  
✅ Performance within 20% of built-in operators  
✅ No memory leaks  
✅ Clear error messages  

### Documentation
✅ Complete API documentation  
✅ 5+ working examples  
✅ Migration guide  
✅ README updated  

### Integration
✅ Works with existing nf-bids features  
✅ No breaking changes  
✅ Plugin loads correctly  
✅ Operators usable in workflows  

---

## Notes

- **Work incrementally**: Don't move to next task until current is complete
- **Test as you go**: Don't accumulate untested code
- **Commit frequently**: Small, focused commits
- **Document decisions**: Add comments explaining non-obvious choices
- **Ask for review**: Check complex implementations with team

---

**Last Updated**: 2025-01-20  
**Status**: Ready to begin Sprint 1
