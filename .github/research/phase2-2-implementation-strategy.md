# Phase 2.2: Implementation Strategy

**Date**: 2025-01-20  
**Phase**: Design & Architecture  
**Goal**: Plan technical implementation approach for closure-based operators

---

## Implementation Architecture

### High-Level Structure

```
nf-bids/
├── src/main/groovy/nfneuro/
│   ├── plugin/
│   │   ├── BidsExtension.groovy              [MODIFY - add operators]
│   │   └── channel/
│   │       ├── BidsChannelFactory.groovy        [EXISTS]
│   │       ├── KeyExtractor.groovy              [NEW]
│   │       ├── ops/
│   │       │   ├── GroupTupleByOp.groovy        [NEW]
│   │       │   ├── JoinByOp.groovy              [NEW]
│   │       │   └── CombineByOp.groovy           [NEW]
│   │       └── keys/
│   │           └── CompositeKey.groovy          [NEW]
│   └── ...
├── src/test/groovy/nfneuro/
│   ├── plugin/channel/
│   │   ├── KeyExtractorTest.groovy              [NEW]
│   │   ├── GroupTupleByOpTest.groovy            [NEW]
│   │   ├── JoinByOpTest.groovy                  [NEW]
│   │   └── CombineByOpTest.groovy               [NEW]
│   └── ...
└── src/resources/META-INF/
    └── extensions.idx                        [NO CHANGE - single extension]
```

**IMPORTANT NOTE**: Nextflow plugins can only have **ONE extension point**. This means we cannot create a separate `ChannelGroupingExtension` class. Instead, all new operators must be added to the existing `BidsExtension` class.

---

## Component Design

### 1. KeyExtractor Utility

**Purpose**: Centralize key extraction logic with error handling and validation

**File**: `src/main/groovy/nfneuro/plugin/channel/KeyExtractor.groovy`

**Class Structure**:
```groovy
package nfneuro.plugin.channel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.channel.keys.CompositeKey

/**
 * Utility for extracting and comparing keys from channel items using closures.
 */
@Slf4j
@CompileStatic
class KeyExtractor {
    
    /**
     * Extract key from item using closure, with error handling
     * 
     * @param item The channel item
     * @param keyExtractor Closure that extracts key
     * @param operatorName Name of calling operator (for error messages)
     * @return Extracted key, or null if extraction failed/returned null
     */
    static Object extractKey(Object item, Closure keyExtractor, String operatorName) {
        if (keyExtractor == null) {
            throw new IllegalArgumentException("${operatorName}: keyExtractor cannot be null")
        }
        
        try {
            def key = keyExtractor.call(item)
            
            if (key == null) {
                log.trace("${operatorName}: item produced null key, skipping: ${item}")
                return null
            }
            
            // Normalize key (wrap lists in CompositeKey for proper equality)
            if (key instanceof List) {
                return new CompositeKey(key)
            }
            
            return key
            
        } catch (Exception e) {
            throw new IllegalStateException(
                "${operatorName}: keyExtractor failed for item [${item}]: ${e.message}",
                e
            )
        }
    }
    
    /**
     * Validate closure has correct arity
     */
    static void validateKeyExtractor(Closure keyExtractor, String operatorName) {
        if (keyExtractor == null) {
            throw new IllegalArgumentException(
                "${operatorName}: keyExtractor closure is required"
            )
        }
        
        int params = keyExtractor.getMaximumNumberOfParameters()
        if (params == 0) {
            throw new IllegalArgumentException(
                "${operatorName}: keyExtractor must accept at least one parameter\n" +
                "  Expected: { item -> item.key }\n" +
                "  Found: { -> ... }"
            )
        }
        
        if (params > 2) {
            log.warn(
                "${operatorName}: keyExtractor has ${params} parameters but only first will be used. " +
                "Did you mean to use a different operator?"
            )
        }
    }
    
    /**
     * Compare two keys for equality
     */
    static boolean keysEqual(Object key1, Object key2) {
        if (key1 == null || key2 == null) {
            return false
        }
        return key1 == key2
    }
}
```

**Complexity**: LOW (2-3 hours)

---

### 2. CompositeKey Class

**Purpose**: Wrap list keys for proper hashCode/equals behavior

**File**: `src/main/groovy/nfneuro/plugin/channel/keys/CompositeKey.groovy`

**Class Structure**:
```groovy
package nfneuro.plugin.channel.keys

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Wrapper for composite (multi-part) keys to ensure proper equality semantics
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class CompositeKey {
    final List<Object> parts
    
    CompositeKey(List<Object> parts) {
        this.parts = parts ? new ArrayList<>(parts) : []
    }
    
    int size() {
        return parts.size()
    }
    
    Object get(int index) {
        return parts[index]
    }
}
```

**Complexity**: LOW (1 hour)

---

### 3. BidsExtension (Modify Existing)

**Purpose**: Single plugin extension point that provides both BIDS parsing and grouping operators

**File**: `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`

**IMPORTANT**: Since Nextflow plugins can only have one extension point, we add the new operators to the existing `BidsExtension` class instead of creating a separate `ChannelGroupingExtension`.

**Class Structure**:
```groovy
package nfneuro.plugin

import groovy.transform.CompileStatic
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Session
import nextflow.plugin.extension.Factory
import nextflow.plugin.extension.Operator
import nextflow.plugin.extension.PluginExtensionPoint
import nfneuro.plugin.channel.BidsChannelFactory
import nfneuro.plugin.channel.KeyExtractor
import nfneuro.plugin.channel.ops.GroupTupleByOp
import nfneuro.plugin.channel.ops.JoinByOp
import nfneuro.plugin.channel.ops.CombineByOp

/**
 * Extends Nextflow with BIDS dataset parsing and closure-based channel grouping operators.
 * Combines both factory and operator methods in a single extension point.
 */
@CompileStatic
class BidsExtension extends PluginExtensionPoint {
    
    private Session session
    
    @Override
    protected void init(Session session) {
        this.session = session
    }
    
    // ========================================================================
    // BIDS Channel Factory (existing functionality)
    // ========================================================================
    
    /**
     * Parse BIDS dataset and return channel
     */
    @Factory
    DataflowWriteChannel fromBIDS(
        String bidsDir,
        String configPath = null,
        Map options = [:]
    ) {
        return new BidsChannelFactory(session).fromBIDS(bidsDir, configPath, options) as DataflowWriteChannel
    }
    
    // ========================================================================
    // Channel Grouping Operators (new functionality)
    // ========================================================================
    
    /**
     * Group channel items by dynamically extracted keys
     * 
     * @param source Input channel
     * @param keyExtractor Closure that extracts grouping key from each item
     * @param opts Options: size, sort, remainder
     * @return Channel emitting [key, [items]] tuples
     */
    @Operator
    DataflowWriteChannel groupTupleBy(
        DataflowReadChannel source,
        Closure keyExtractor,
        Map opts = [:]
    ) {
        KeyExtractor.validateKeyExtractor(keyExtractor, 'groupTupleBy')
        
        def op = new GroupTupleByOp(source, keyExtractor, opts)
        return op.apply()
    }
    
    /**
     * Join two channels by dynamically extracted keys
     * 
     * @param left Left input channel
     * @param right Right input channel
     * @param leftKeyExtractor Closure extracting key from left items
     * @param rightKeyExtractor Optional closure for right items (defaults to leftKeyExtractor)
     * @param opts Options: remainder, failOnDuplicate, failOnMismatch
     * @return Channel emitting [leftItem, rightItem] pairs
     */
    @Operator
    DataflowWriteChannel joinBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure leftKeyExtractor,
        Closure rightKeyExtractor = null,
        Map opts = [:]
    ) {
        KeyExtractor.validateKeyExtractor(leftKeyExtractor, 'joinBy')
        
        // Default right extractor to same as left
        def rightExtractor = rightKeyExtractor ?: leftKeyExtractor
        KeyExtractor.validateKeyExtractor(rightExtractor, 'joinBy')
        
        def op = new JoinByOp(left, right, leftKeyExtractor, rightExtractor, opts)
        return op.apply()
    }
    
    /**
     * Combine two channels with optional filtering predicate
     * 
     * @param left Left input channel
     * @param right Right input channel
     * @param filterPredicate Optional closure to filter combinations
     * @param opts Options (TBD)
     * @return Channel emitting [leftItem, rightItem] for valid combinations
     */
    @Operator
    DataflowWriteChannel combineBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure filterPredicate = null,
        Map opts = [:]
    ) {
        // Validate filter if provided
        if (filterPredicate != null) {
            int params = filterPredicate.getMaximumNumberOfParameters()
            if (params < 2) {
                throw new IllegalArgumentException(
                    "combineBy: filterPredicate must accept 2 parameters [leftItem, rightItem]\n" +
                    "  Expected: { left, right -> ... }\n" +
                    "  Found: closure with ${params} parameter(s)"
                )
            }
        }
        
        def op = new CombineByOp(left, right, filterPredicate, opts)
        return op.apply()
    }
}
```

**Complexity**: LOW (3-4 hours including registration)

---

### 4. GroupTupleByOp

**Purpose**: Implement grouping logic with closure-based key extraction

**File**: `src/main/groovy/nfneuro/plugin/channel/ops/GroupTupleByOp.groovy`

**Class Structure** (pattern adapted from GroupTupleOp.groovy):
```groovy
package nfneuro.plugin.channel.ops

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.extension.CH
import nextflow.extension.DataflowHelper
import nfneuro.plugin.channel.KeyExtractor

/**
 * Groups channel items by closure-extracted keys
 */
@Slf4j
@CompileStatic
class GroupTupleByOp {
    
    private DataflowReadChannel source
    private Closure keyExtractor
    private Map opts
    private DataflowWriteChannel target
    
    // Grouping state
    private Map<Object, List<Object>> groups = new HashMap<>()
    private Map<Object, Integer> counts = new HashMap<>()
    
    GroupTupleByOp(DataflowReadChannel source, Closure keyExtractor, Map opts) {
        this.source = source
        this.keyExtractor = keyExtractor
        this.opts = opts ?: [:]
    }
    
    DataflowWriteChannel apply() {
        target = CH.createBy(source)
        
        DataflowHelper.subscribeImpl(source, [
            onNext: this.&onNext,
            onComplete: this.&onComplete
        ])
        
        return target
    }
    
    private void onNext(Object item) {
        // Extract key
        def key = KeyExtractor.extractKey(item, keyExtractor, 'groupTupleBy')
        if (key == null) {
            return  // Skip items with null keys
        }
        
        // Add to group
        if (!groups.containsKey(key)) {
            groups[key] = []
            counts[key] = 0
        }
        groups[key].add(item)
        counts[key]++
        
        // Check if group is complete
        def expectedSize = opts.size as Integer
        if (expectedSize != null && counts[key] >= expectedSize) {
            emitGroup(key)
        }
    }
    
    private void onComplete() {
        // Emit remaining groups
        def remainder = opts.remainder != null ? opts.remainder : true
        if (remainder) {
            groups.each { key, items ->
                emitGroup(key)
            }
        }
        
        target.bind(Channel.STOP)
    }
    
    private void emitGroup(Object key) {
        def items = groups.remove(key)
        if (items == null || items.isEmpty()) {
            return
        }
        
        // Sort if requested
        def sort = opts.sort
        if (sort) {
            if (sort instanceof Boolean && sort) {
                items = items.sort()
            } else if (sort instanceof Closure) {
                items = items.sort(sort as Closure)
            } else if (sort instanceof Comparator) {
                items = items.sort(sort as Comparator)
            }
        }
        
        // Emit [key, items] tuple
        target.bind([key, items])
    }
}
```

**Complexity**: MEDIUM (6-8 hours including testing)

**Key Challenges**:
- State management (groups map)
- Proper emission timing (size-based vs. completion)
- Sorting logic

---

### 5. JoinByOp

**Purpose**: Implement join logic with closure-based key extraction

**File**: `src/main/groovy/nfneuro/plugin/channel/ops/JoinByOp.groovy`

**Class Structure** (pattern adapted from JoinOp.groovy):
```groovy
package nfneuro.plugin.channel.ops

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.extension.CH
import nextflow.extension.DataflowHelper
import nfneuro.plugin.channel.KeyExtractor

/**
 * Joins two channels by closure-extracted keys
 */
@Slf4j
@CompileStatic
class JoinByOp {
    
    private DataflowReadChannel left
    private DataflowReadChannel right
    private Closure leftKeyExtractor
    private Closure rightKeyExtractor
    private Map opts
    private DataflowWriteChannel target
    
    // Join state
    private Map<Object, List<Object>> leftBuffer = new HashMap<>()
    private Map<Object, List<Object>> rightBuffer = new HashMap<>()
    private Set<Object> matchedKeys = new HashSet<>()
    private boolean leftComplete = false
    private boolean rightComplete = false
    
    JoinByOp(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure leftKeyExtractor,
        Closure rightKeyExtractor,
        Map opts
    ) {
        this.left = left
        this.right = right
        this.leftKeyExtractor = leftKeyExtractor
        this.rightKeyExtractor = rightKeyExtractor
        this.opts = opts ?: [:]
    }
    
    DataflowWriteChannel apply() {
        target = CH.createBy(left)
        
        DataflowHelper.subscribeImpl(left, [
            onNext: { item -> onLeftItem(item) },
            onComplete: { onLeftComplete() }
        ])
        
        DataflowHelper.subscribeImpl(right, [
            onNext: { item -> onRightItem(item) },
            onComplete: { onRightComplete() }
        ])
        
        return target
    }
    
    private synchronized void onLeftItem(Object item) {
        def key = KeyExtractor.extractKey(item, leftKeyExtractor, 'joinBy')
        if (key == null) return
        
        // Buffer left item
        if (!leftBuffer.containsKey(key)) {
            leftBuffer[key] = []
        }
        leftBuffer[key].add(item)
        
        // Try to match with right buffer
        def rightItems = rightBuffer[key]
        if (rightItems) {
            matchedKeys.add(key)
            rightItems.each { rightItem ->
                target.bind([item, rightItem])
            }
        }
    }
    
    private synchronized void onRightItem(Object item) {
        def key = KeyExtractor.extractKey(item, rightKeyExtractor, 'joinBy')
        if (key == null) return
        
        // Buffer right item
        if (!rightBuffer.containsKey(key)) {
            rightBuffer[key] = []
        }
        rightBuffer[key].add(item)
        
        // Try to match with left buffer
        def leftItems = leftBuffer[key]
        if (leftItems) {
            matchedKeys.add(key)
            leftItems.each { leftItem ->
                target.bind([leftItem, item])
            }
        }
    }
    
    private synchronized void onLeftComplete() {
        leftComplete = true
        checkCompletion()
    }
    
    private synchronized void onRightComplete() {
        rightComplete = true
        checkCompletion()
    }
    
    private void checkCompletion() {
        if (!leftComplete || !rightComplete) {
            return
        }
        
        // Emit remainder if requested
        def remainder = opts.remainder as Boolean
        if (remainder) {
            emitRemainder()
        }
        
        target.bind(Channel.STOP)
    }
    
    private void emitRemainder() {
        // Emit unmatched left items
        leftBuffer.each { key, items ->
            if (!matchedKeys.contains(key)) {
                items.each { item ->
                    target.bind([item, null])
                }
            }
        }
        
        // Emit unmatched right items
        rightBuffer.each { key, items ->
            if (!matchedKeys.contains(key)) {
                items.each { item ->
                    target.bind([null, item])
                }
            }
        }
    }
}
```

**Complexity**: HIGH (10-12 hours including testing)

**Key Challenges**:
- Two-channel synchronization
- Buffering both sides
- Cartesian product for duplicate keys
- Remainder handling (outer join)
- Thread safety (synchronized methods)

---

### 6. CombineByOp

**Purpose**: Implement combine with optional filtering

**File**: `src/main/groovy/nfneuro/plugin/channel/ops/CombineByOp.groovy`

**Class Structure** (pattern adapted from CombineOp.groovy):
```groovy
package nfneuro.plugin.channel.ops

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.extension.CH
import nextflow.extension.DataflowHelper

/**
 * Combines two channels with optional filtering predicate
 */
@Slf4j
@CompileStatic
class CombineByOp {
    
    private DataflowReadChannel left
    private DataflowReadChannel right
    private Closure filterPredicate
    private Map opts
    private DataflowWriteChannel target
    
    private List<Object> leftBuffer = []
    private List<Object> rightBuffer = []
    private boolean leftComplete = false
    private boolean rightComplete = false
    
    CombineByOp(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure filterPredicate,
        Map opts
    ) {
        this.left = left
        this.right = right
        this.filterPredicate = filterPredicate
        this.opts = opts ?: [:]
    }
    
    DataflowWriteChannel apply() {
        target = CH.createBy(left)
        
        DataflowHelper.subscribeImpl(left, [
            onNext: { item -> onLeftItem(item) },
            onComplete: { onLeftComplete() }
        ])
        
        DataflowHelper.subscribeImpl(right, [
            onNext: { item -> onRightItem(item) },
            onComplete: { onRightComplete() }
        ])
        
        return target
    }
    
    private synchronized void onLeftItem(Object item) {
        leftBuffer.add(item)
        
        // Combine with existing right items
        rightBuffer.each { rightItem ->
            emitIfValid(item, rightItem)
        }
    }
    
    private synchronized void onRightItem(Object item) {
        rightBuffer.add(item)
        
        // Combine with existing left items
        leftBuffer.each { leftItem ->
            emitIfValid(leftItem, item)
        }
    }
    
    private void emitIfValid(Object leftItem, Object rightItem) {
        // No filter = emit all combinations
        if (filterPredicate == null) {
            target.bind([leftItem, rightItem])
            return
        }
        
        // Apply filter
        try {
            def result = filterPredicate.call(leftItem, rightItem)
            if (result) {
                target.bind([leftItem, rightItem])
            }
        } catch (Exception e) {
            log.error("combineBy: filter predicate failed for [${leftItem}, ${rightItem}]: ${e.message}", e)
            throw new IllegalStateException(
                "combineBy: filter predicate failed: ${e.message}",
                e
            )
        }
    }
    
    private synchronized void onLeftComplete() {
        leftComplete = true
        checkCompletion()
    }
    
    private synchronized void onRightComplete() {
        rightComplete = true
        checkCompletion()
    }
    
    private void checkCompletion() {
        if (leftComplete && rightComplete) {
            target.bind(Channel.STOP)
        }
    }
}
```

**Complexity**: MEDIUM (6-8 hours including testing)

**Key Challenges**:
- Buffering both channels
- Filtering logic
- Thread safety

---

## Plugin Registration

### Extension Index (No Changes Required)

**File**: `src/resources/META-INF/extensions.idx`

**Current content** (no changes needed):
```
nfneuro.plugin.BidsExtension
```

**IMPORTANT**: Do NOT add a second extension. Nextflow plugins can only have one extension point. The new operators are added to the existing `BidsExtension` class.

### Critical Usage Note

**MANDATORY**: To use plugin operators in workflows, you MUST include them explicitly:
```groovy
include { groupTupleBy } from 'plugin/nf-bids'
```

This is required even though the plugin is declared in `nextflow.config`. Without the include statement, operators will not be available and you'll get "Missing process or function" errors.

### Usage (Include Statement Required)

**Plugin configuration in `nextflow.config`**:
```groovy
plugins {
    id 'nf-bids@0.1.0-beta.4'
}
```

**In workflow script - MUST include operators**:
```groovy
include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    Channel
        .of([subject: 'sub-01', file: 'file.nii'])
        .groupTupleBy { it.subject }
}
```

---

## Testing Strategy

### Unit Tests

**KeyExtractorTest.groovy**:
- Test key extraction with various types
- Test null handling
- Test exception handling
- Test composite key creation
- **Complexity**: LOW (2-3 hours)

**CompositeKeyTest.groovy**:
- Test equality for lists
- Test hashCode consistency
- Test with nested lists
- **Complexity**: LOW (1 hour)

### Operator Tests

Each operator test suite:
- Basic functionality
- Edge cases (empty channels, null keys)
- Error handling
- Options validation
- Performance benchmarks

**GroupTupleByOpTest.groovy**:
- Single key grouping
- Composite key grouping
- Size-based emission
- Remainder handling
- Sorting
- **Complexity**: MEDIUM (4-5 hours)

**JoinByOpTest.groovy**:
- Simple join
- Different extractors for left/right
- Duplicate keys (cartesian product)
- Remainder (outer join)
- Empty channels
- **Complexity**: HIGH (6-8 hours)

**CombineByOpTest.groovy**:
- No filter (all combinations)
- With filter predicate
- Filter returning false
- Empty channels
- **Complexity**: MEDIUM (4-5 hours)

### Integration Tests (nf-test)

Create test workflows:
- `test_groupTupleBy.nf.test`
- `test_joinBy.nf.test`
- `test_combineBy.nf.test`

**Complexity**: MEDIUM (8-10 hours for all)

---

## Implementation Order

### Sprint 1: Foundation (Week 1)
1. **CompositeKey** (1 hour)
2. **KeyExtractor** (2-3 hours)
3. **Tests for above** (3-4 hours)
4. **Add operator methods to BidsExtension** (2 hours)

**Total**: ~10 hours

### Sprint 2: GroupTupleBy (Week 1-2)
1. **GroupTupleByOp implementation** (6-8 hours)
2. **Unit tests** (4-5 hours)
3. **Integration tests** (3-4 hours)
4. **Register in extension** (1 hour)

**Total**: ~15 hours

### Sprint 3: JoinBy (Week 2)
1. **JoinByOp implementation** (10-12 hours)
2. **Unit tests** (6-8 hours)
3. **Integration tests** (3-4 hours)

**Total**: ~22 hours

### Sprint 4: CombineBy (Week 2-3)
1. **CombineByOp implementation** (6-8 hours)
2. **Unit tests** (4-5 hours)
3. **Integration tests** (3-4 hours)

**Total**: ~15 hours

### Sprint 5: Polish & Docs (Week 3)
1. **Documentation** (8-10 hours)
2. **Examples** (4-6 hours)
3. **Performance testing** (4-6 hours)
4. **Bug fixes** (buffer time)

**Total**: ~20 hours

---

## Effort Estimates

### By Component

| Component | Complexity | Estimate | Priority |
|-----------|-----------|----------|----------|
| KeyExtractor | LOW | 2-3 hours | HIGH |
| CompositeKey | LOW | 1 hour | HIGH |
| BidsExtension (add operators) | LOW | 3-4 hours | HIGH |
| GroupTupleByOp | MEDIUM | 6-8 hours | HIGH |
| JoinByOp | HIGH | 10-12 hours | HIGH |
| CombineByOp | MEDIUM | 6-8 hours | HIGH |
| Unit Tests (all) | MEDIUM | 15-20 hours | HIGH |
| Integration Tests | MEDIUM | 8-10 hours | MEDIUM |
| Documentation | MEDIUM | 8-10 hours | MEDIUM |
| Examples | LOW | 4-6 hours | MEDIUM |

**Total Implementation**: 65-85 hours (~2-3 weeks full-time)

### By Phase

| Phase | Estimate |
|-------|----------|
| Foundation | 10 hours |
| groupTupleBy | 15 hours |
| joinBy | 22 hours |
| combineBy | 15 hours |
| Polish & Docs | 20 hours |
| **TOTAL** | **82 hours** |

---

## Risk Assessment

### Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Thread safety issues in buffering | MEDIUM | HIGH | Synchronize critical sections; extensive testing |
| Memory issues with large channels | MEDIUM | HIGH | Document limitations; test with large data |
| Closure serialization problems | LOW | MEDIUM | Validate early; document restrictions |
| Performance degradation | MEDIUM | MEDIUM | Benchmark; optimize hot paths |

### Schedule Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| JoinBy more complex than estimated | MEDIUM | MEDIUM | Start early; allocate buffer time |
| Testing uncovers major issues | LOW | HIGH | Test incrementally; start testing early |
| Nextflow API changes | LOW | HIGH | Pin Nextflow version; monitor releases |

---

## Success Criteria

### Functional Requirements
- [ ] All three operators implemented: `groupTupleBy`, `joinBy`, `combineBy`
- [ ] All unit tests passing (>95% coverage)
- [ ] All integration tests passing
- [ ] Error messages are clear and helpful
- [ ] Documented with examples

### Non-Functional Requirements
- [ ] Performance within 20% of index-based operators
- [ ] No memory leaks with large channels
- [ ] Thread-safe operation
- [ ] Graceful error handling

### Documentation Requirements
- [ ] API documentation complete
- [ ] Usage examples for each operator
- [ ] Migration guide from index-based
- [ ] Performance characteristics documented

---

## Next Steps (Phase 3)

Ready to begin implementation:

1. **Create branch**: `feature/closure-based-grouping`
2. **Start with foundation**: KeyExtractor + CompositeKey
3. **Implement operators in order**: groupTupleBy → joinBy → combineBy
4. **Test incrementally**: Don't wait until end
5. **Document as you go**: Better than retroactive docs

---

## Questions for Review

1. **Thread Safety**: Current approach uses synchronized methods. Is this sufficient or should we use more sophisticated concurrency primitives?
   - **Recommendation**: Start with synchronized, profile if issues arise

2. **Memory Management**: Should we add limits to buffer sizes?
   - **Recommendation**: Document limitation, add optional warnings

3. **Error Recovery**: Should failed key extraction stop pipeline or skip item?
   - **Current design**: Stop pipeline (fail fast)
   - **Alternative**: Skip with warning (more forgiving)

4. **API Flexibility**: Should we support both closure and method reference?
   - **Current**: Closure only
   - **Example**: `.groupTupleBy(Item::getSubject)` vs `.groupTupleBy { it.subject }`
   - **Recommendation**: Closure only (simpler, Groovy idiomatic)

---

## References

- Phase 1: `.github/research/phase1-*.md`
- Phase 2.1: `.github/research/phase2-1-operator-signatures.md`
- Nextflow operators: `GroupTupleOp.groovy`, `JoinOp.groovy`, `CombineOp.groovy`
- DataflowHelper: `nextflow.extension.DataflowHelper`
- Channel utilities: `nextflow.extension.CH`
