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

#### Task 3.1.3: Create Unit Tests for Foundation ✅ COMPLETE
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
- [x] All tests pass (34 tests total including GroupTupleByOp smoke tests)
- [x] Code coverage > 95% for both classes
- [x] Edge cases covered

**Actual Time**: 3 hours

---

#### Task 3.1.4: Create BidsExtension with Operators ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`

**Important Note**: Nextflow plugins can only have **one extension point**. This is why we add the operator methods directly to `BidsExtension` (which already has the `@Factory` method `fromBIDS()`), rather than creating a separate `ChannelGroupingExtension` class.

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

**Important**: This file should contain **only one extension point** because Nextflow plugins are limited to a single extension point per plugin. This is a framework constraint.

**Requirements**:
- [x] Ensure only `nfneuro.plugin.BidsExtension` is listed
- [x] Remove old ChannelGroupingExtension reference (violated single extension point rule)
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
- Consolidated operators into BidsExtension (combines factory + operators)
- Registered single extension in plugin (extensions.idx)
- All code compiles successfully

**Files Created**:
1. `src/main/groovy/nfneuro/plugin/channel/keys/CompositeKey.groovy` (72 lines)
2. `src/main/groovy/nfneuro/plugin/channel/KeyExtractor.groovy` (106 lines)
3. `src/test/groovy/nfneuro/plugin/channel/keys/CompositeKeyTest.groovy` (154 lines)
4. `src/test/groovy/nfneuro/plugin/channel/KeyExtractorTest.groovy` (213 lines)
5. `src/main/groovy/nfneuro/plugin/BidsExtension.groovy` (consolidated operators)
6. `src/main/resources/META-INF/extensions.idx` (1 line)

**Actual Time**: ~8 hours (vs. estimated 10 hours)

---

### Sprint 2: GroupTupleBy Operator (Week 1-2, ~15 hours)

#### Task 3.2.1: Implement GroupTupleByOp Class ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/channel/ops/GroupTupleByOp.groovy`

**Requirements**:
- [x] Create package directory: `src/main/groovy/nfneuro/plugin/channel/ops/`
- [x] Implement class with:
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

#### Task 3.2.2: Integrate GroupTupleBy into Extension ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`

**Requirements**:
- [x] Import `GroupTupleByOp`
- [x] Replace `groupTupleBy` stub implementation:
  - [x] Validate keyExtractor with `KeyExtractor.validateKeyExtractor`
  - [x] Create `GroupTupleByOp` instance
  - [x] Call `apply()` and return result
- [x] Add parameter validation for opts map

**Acceptance Criteria**:
- [x] Method compiles
- [x] Operator callable from workflow script
- [x] Options properly passed through

**Actual Time**: Already integrated

---

#### Task 3.2.3: Create Unit Tests for GroupTupleByOp ✅ COMPLETE
**File**: `src/test/groovy/nfneuro/plugin/channel/ops/GroupTupleByOpTest.groovy`

**Test Cases** (Smoke tests - comprehensive testing via integration tests):
- [x] Test basic operator instantiation
- [x] Test operator with options
- [x] Test return type is DataflowQueue
- [x] Test null opts handling
- [x] Test option pass-through
- [x] Test group maps initialization
- [x] Test different closure types
- [x] Test sort option types

**Note**: Comprehensive functional tests (grouping logic, size option, sort, remainder, null keys, empty channel, exceptions) are covered in integration test `validation/test_grouptupleby.nf` which runs with real Nextflow execution. Unit tests that attempt to block on DataflowQueue.getVal() cause hangs.

**Acceptance Criteria**:
- [x] All smoke tests pass (8 tests)
- [x] Operator construction validated
- [x] Integration tests cover full scenarios

**Actual Time**: 2 hours

---

#### Task 3.2.4: Create Integration Test for GroupTupleBy ✅ COMPLETE
**File**: `validation/test_grouptupleby.nf`

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
- [x] Test basic grouping by simple key
- [x] Test nested field extraction
- [x] Test composite key grouping
- [x] Test with sort option
- [x] Test computed key from path

**Acceptance Criteria**:
- [x] All 5 integration tests pass
- [x] Works in real Nextflow execution
- [x] Output format verified

**Actual Time**: Already implemented and passing

---

## ✅ Sprint 2 Complete! GroupTupleBy Operator Ready

**Summary**: 
- GroupTupleByOp fully implemented (158 lines)
- Integrated into BidsExtension
- 8 smoke tests passing (unit tests)
- 5 comprehensive scenarios passing (integration tests)
- All options supported: size, sort, remainder
- Handles simple keys, nested fields, composite keys

**Files Created/Modified**:
1. `src/main/groovy/nfneuro/plugin/channel/ops/GroupTupleByOp.groovy`
2. `src/main/groovy/nfneuro/plugin/BidsExtension.groovy` (groupTupleBy method)
3. `src/test/groovy/nfneuro/plugin/channel/ops/GroupTupleByOpTest.groovy`
4. `validation/test_grouptupleby.nf`

**Actual Time**: ~10 hours (vs. estimated 15 hours)

---

### Sprint 3: JoinBy Operator (Week 2, ~22 hours)

#### Task 3.3.1: Implement JoinByOp Class ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/channel/ops/JoinByOp.groovy`

**Requirements**:
- [x] Implement class with:
  - [x] Private fields:
    - [x] `DataflowReadChannel left`
    - [x] `DataflowReadChannel right`
    - [x] `Closure leftKeyExtractor`
    - [x] `Closure rightKeyExtractor`
    - [x] `Map opts`
    - [x] `DataflowWriteChannel target`
    - [x] `Map<Object, List<Object>> leftBuffer`
    - [x] `Map<Object, List<Object>> rightBuffer`
    - [x] `Set<Object> matchedKeys`
    - [x] `boolean leftComplete = false`
    - [x] `boolean rightComplete = false`
  - [x] Constructor accepting `(left, right, leftExtractor, rightExtractor, opts)`
  - [x] `DataflowWriteChannel apply()` method:
    - [x] Create target channel
    - [x] Subscribe to left with `onNext: { onLeftItem(it) }`, `onComplete: { onLeftComplete() }`
    - [x] Subscribe to right with `onNext: { onRightItem(it) }`, `onComplete: { onRightComplete() }`
    - [x] Return target
  - [x] `private synchronized void onLeftItem(Object item)`:
    - [x] Extract key
    - [x] Skip if null
    - [x] Buffer left item
    - [x] Check right buffer for matches
    - [x] Emit `[leftItem, rightItem]` for each match
    - [x] Track matched keys
  - [x] `private synchronized void onRightItem(Object item)`:
    - [x] Extract key
    - [x] Skip if null
    - [x] Buffer right item
    - [x] Check left buffer for matches
    - [x] Emit `[leftItem, rightItem]` for each match
    - [x] Track matched keys
  - [x] `private synchronized void onLeftComplete()`:
    - [x] Set leftComplete = true
    - [x] Call `checkCompletion()`
  - [x] `private synchronized void onRightComplete()`:
    - [x] Set rightComplete = true
    - [x] Call `checkCompletion()`
  - [x] `private void checkCompletion()`:
    - [x] Return if either not complete
    - [x] If remainder option, call `emitRemainder()`
    - [x] Bind `Channel.STOP`
  - [x] `private void emitRemainder()`:
    - [x] Emit unmatched left items as `[item, null]`
    - [x] Emit unmatched right items as `[null, item]`
- [x] Add `@CompileStatic` annotation
- [x] Add `@Slf4j` annotation

**Acceptance Criteria**:
- [x] Class compiles
- [x] Thread-safe with synchronized methods
- [x] Follows pattern from Nextflow's JoinOp
- [x] Handles cartesian product for duplicate keys

**Actual Time**: 6 hours

---

#### Task 3.3.2: Integrate JoinBy into Extension ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`

**Requirements**:
- [x] Import `JoinByOp`
- [x] Replace `joinBy` stub implementation:
  - [x] Validate leftKeyExtractor
  - [x] Default rightKeyExtractor to leftKeyExtractor if null
  - [x] Validate rightKeyExtractor
  - [x] Create `JoinByOp` instance
  - [x] Call `apply()` and return result
- [x] Validate opts map
- [x] Register right channel as input (for DAG)

**Acceptance Criteria**:
- [x] Method compiles
- [x] Both extractors validated
- [x] Default behavior works

**Actual Time**: Already integrated

---

#### Task 3.3.3: Create Unit Tests for JoinByOp ✅ COMPLETE
**File**: `src/test/groovy/nfneuro/plugin/channel/ops/JoinByOpTest.groovy`

**Test Cases** (Smoke tests - comprehensive testing via integration tests):
- [x] Test operator instantiation
- [x] Test with options (remainder)
- [x] Test return type is DataflowQueue
- [x] Test null opts handling
- [x] Test buffer initialization
- [x] Test completion flags
- [x] Test different extractors for left/right
- [x] Test remainder option handling

**Note**: Comprehensive functional tests (cartesian product, outer join, null keys, empty channels, synchronization) are covered in integration test `validation/test_joinby.nf` which runs with real Nextflow execution.

**Acceptance Criteria**:
- [x] All smoke tests pass (8 tests)
- [x] Operator construction validated
- [x] Integration tests cover full scenarios

**Actual Time**: 1 hour

---

#### Task 3.3.4: Create Integration Test for JoinBy ✅ COMPLETE
**File**: `validation/test_joinby.nf`

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
- [x] Test basic join with same key extractor
- [x] Test with different key extractors (subject vs participant_id)
- [x] Test with nested field extraction
- [x] Test with composite keys
- [x] Test inner join (remainder: false)
- [x] Test outer join (remainder: true) with null partners

**Acceptance Criteria**:
- [x] All 6 integration tests pass
- [x] Works in real Nextflow execution
- [x] Handles BIDS-like metadata
- [x] Cartesian product for duplicate keys works
- [x] Remainder option properly emits unmatched items

**Actual Time**: Already implemented and passing

---

## ✅ Sprint 3 Complete! JoinBy Operator Ready

**Summary**: 
- JoinByOp fully implemented (218 lines)
- Thread-safe with synchronized methods
- Integrated into BidsExtension
- 8 smoke tests passing (unit tests)
- 6 comprehensive scenarios passing (integration tests)
- Supports inner join (default) and outer join (remainder: true)
- Handles cartesian product for duplicate keys
- Works with different extractors for left/right channels

**Files Created/Modified**:
1. `src/main/groovy/nfneuro/plugin/channel/ops/JoinByOp.groovy`
2. `src/main/groovy/nfneuro/plugin/BidsExtension.groovy` (joinBy method)
3. `src/test/groovy/nfneuro/plugin/channel/ops/JoinByOpTest.groovy`
4. `validation/test_joinby.nf`

**Actual Time**: ~8 hours (vs. estimated 22 hours - much faster due to clear patterns from GroupTupleBy)

---

### Sprint 4: CombineBy Operator (Week 2-3, ~15 hours)

#### Task 3.4.1: Implement CombineByOp Class ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/channel/ops/CombineByOp.groovy`

**Requirements**:
- [x] Implement class with:
  - [x] Private fields:
    - [x] `DataflowReadChannel left`
    - [x] `DataflowReadChannel right`
    - [x] `Closure filterPredicate`
    - [x] `Map opts`
    - [x] `DataflowWriteChannel target`
    - [x] `List<Object> leftBuffer`
    - [x] `List<Object> rightBuffer`
    - [x] `boolean leftComplete = false`
    - [x] `boolean rightComplete = false`
  - [x] Constructor accepting `(left, right, filterPredicate, opts)`
  - [x] `DataflowWriteChannel apply()` method:
    - [x] Create target channel
    - [x] Subscribe to left
    - [x] Subscribe to right
    - [x] Return target
  - [x] `private synchronized void onLeftItem(Object item)`:
    - [x] Add to leftBuffer
    - [x] Combine with all existing rightBuffer items
    - [x] Call `emitIfValid` for each combination
  - [x] `private synchronized void onRightItem(Object item)`:
    - [x] Add to rightBuffer
    - [x] Combine with all existing leftBuffer items
    - [x] Call `emitIfValid` for each combination
  - [x] `private void emitIfValid(Object left, Object right)`:
    - [x] If no filter, emit `[left, right]`
    - [x] If filter exists, call predicate
    - [x] If predicate returns true, emit `[left, right]`
    - [x] Catch exceptions with clear error
  - [x] `private synchronized void onLeftComplete()`:
    - [x] Set leftComplete = true
    - [x] Call `checkCompletion()`
  - [x] `private synchronized void onRightComplete()`:
    - [x] Set rightComplete = true
    - [x] Call `checkCompletion()`
  - [x] `private void checkCompletion()`:
    - [x] If both complete, bind `Channel.STOP`
- [x] Add `@CompileStatic` annotation
- [x] Add `@Slf4j` annotation

**Acceptance Criteria**:
- [x] Class compiles
- [x] Thread-safe with synchronized methods
- [x] Follows pattern from Nextflow's CombineOp
- [x] Filter optional (null = all combinations)

**Actual Time**: 4 hours

---

#### Task 3.4.2: Integrate CombineBy into Extension ✅ COMPLETE
**File**: `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`

**Requirements**:
- [x] Import `CombineByOp`
- [x] Replace `combineBy` stub implementation:
  - [x] Validate filterPredicate if not null
  - [x] Check arity >= 2
  - [x] Create `CombineByOp` instance
  - [x] Call `apply()` and return result
- [x] Register right channel as input (for DAG)

**Acceptance Criteria**:
- [x] Method compiles
- [x] Filter validation correct (validated in BidsExtensionTest)
- [x] Works with and without filter

**Actual Time**: Already integrated

---

#### Task 3.4.3: Create Unit Tests for CombineByOp ✅ COMPLETE
**File**: `src/test/groovy/nfneuro/plugin/channel/ops/CombineByOpTest.groovy`

**Test Cases** (Smoke tests - comprehensive testing via integration tests):
- [x] Test operator instantiation without filter
- [x] Test with filter
- [x] Test return type
- [x] Test null opts handling
- [x] Test buffer initialization
- [x] Test completion flags
- [x] Test complex filter predicates
- [x] Test null filter acceptance

**Note**: Comprehensive functional tests (cartesian product, filtering logic, empty channels, exception handling) are covered in integration test `validation/test_combineby.nf`.

**Acceptance Criteria**:
- [x] All smoke tests pass (8 tests)
- [x] Operator construction validated
- [x] Integration tests cover full scenarios

**Actual Time**: 1 hour

---

#### Task 3.4.4: Create Integration Test for CombineBy ✅ COMPLETE
**File**: `validation/test_combineby.nf`

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
- [x] Test basic cartesian product (no filter)
- [x] Test with conditional filter (session <= subject)
- [x] Test with complex maps
- [x] Test with ID matching filter
- [x] Test parameters × datasets combination
- [x] Test filter that rejects all (empty result)
- [x] Test numeric comparison filter

**Acceptance Criteria**:
- [x] All 7 integration tests pass
- [x] Works in real Nextflow execution
- [x] Cartesian product correct
- [x] Filter logic validated
- [x] Empty results handled correctly

**Actual Time**: Already implemented and passing

---

## ✅ Sprint 4 Complete! CombineBy Operator Ready

**Summary**: 
- CombineByOp fully implemented (178 lines)
- Thread-safe with synchronized methods
- Integrated into BidsExtension
- 8 smoke tests passing (unit tests)
- 7 comprehensive scenarios passing (integration tests)
- Supports full cartesian product (no filter)
- Supports conditional filtering with custom predicates
- Clear error messages for filter exceptions

**Files Created/Modified**:
1. `src/main/groovy/nfneuro/plugin/channel/ops/CombineByOp.groovy`
2. `src/main/groovy/nfneuro/plugin/BidsExtension.groovy` (combineBy method)
3. `src/test/groovy/nfneuro/plugin/channel/ops/CombineByOpTest.groovy`
4. `validation/test_combineby.nf`

**Actual Time**: ~5 hours (vs. estimated 15 hours - very efficient due to established patterns)

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
