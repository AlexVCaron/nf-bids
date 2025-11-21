# Phase 1.2: Nextflow Plugin Extension Mechanisms

**Research Date**: 2025-01-XX  
**Scope**: How to extend channel operators in Nextflow plugins  
**Goal**: Determine if operators can be overloaded and how to implement extensions

---

## Summary

**Key Findings**:
1. ✅ Plugins **CAN** extend operators using `PluginExtensionPoint` and `@Operator` annotation
2. ❌ Plugins **CANNOT** overload existing core operators - only add **new** operator names
3. ✅ Can add **factory methods** (@Factory) and **functions** (@Function) with same pattern
4. ✅ Extension mechanism is well-defined and tested in production plugins (nf-sqldb)

**Implication for Project**: Must create NEW operator names (e.g., `joinBy`, `groupTupleBy`) instead of extending existing signatures.

---

## Extension Architecture

### Plugin Base Class Structure

```groovy
// From nf-sqldb plugin
class ChannelSqlExtension extends PluginExtensionPoint {
    
    private Session session
    private SqlConfig config
    
    @Override
    protected void init(Session session) {
        this.session = session
        this.config = new SqlConfig((Map) session.config.navigate('sql.db'))
    }
    
    @Factory
    DataflowWriteChannel fromQuery(String query) {
        // Factory implementation
    }
    
    @Operator
    DataflowWriteChannel sqlInsert(DataflowReadChannel source, Map opts) {
        // Operator implementation
    }
    
    @Function
    Map sqlExecute(Map params) {
        // Function implementation
    }
}
```

**Key Components**:
- Extends `PluginExtensionPoint` (abstract base)
- Implements `init(Session)` for plugin initialization
- Uses annotations: `@Factory`, `@Operator`, `@Function`
- Returns `DataflowWriteChannel` for operators/factories
- First parameter MUST be `DataflowReadChannel` for operators

---

## Extension Point Rules

### 1. Factory Methods (@Factory)

**Purpose**: Create channel from data source (like `Channel.fromQuery`)

**Requirements**:
```groovy
@Factory
DataflowWriteChannel factoryName(/* parameters */) {
    // Must return DataflowWriteChannel
    // Can accept any parameters
    // Called as: Channel.factoryName(args)
}
```

**Example from nf-sqldb**:
```groovy
@Factory
DataflowWriteChannel fromQuery(String query) {
    fromQuery(Collections.emptyMap(), query)
}

@Factory
DataflowWriteChannel fromQuery(Map opts, String query) {
    // Implementation with validation
    CheckHelper.checkParams('fromQuery', opts, QUERY_PARAMS)
    return queryToChannel(query, opts)
}
```

**Usage in Pipeline**:
```groovy
include { fromQuery } from 'plugin/nf-sqldb'

channel.fromQuery('SELECT * FROM FOO', db: 'test')
```

---

### 2. Operator Methods (@Operator)

**Purpose**: Transform/consume channel data (like `.sqlInsert()`)

**Requirements**:
```groovy
@Operator
DataflowWriteChannel operatorName(DataflowReadChannel source, /* other params */) {
    // First parameter MUST be DataflowReadChannel
    // Must return DataflowWriteChannel
    // Called as: channel.operatorName(args)
}
```

**Critical Signature Rules** (from PluginExtensionProvider.groovy):
```groovy
// Validation in plugin loading:
if (!Modifier.isPublic(handle.getModifiers()))
    throw new IllegalStateException("Operator extension must be public")
if (Modifier.isStatic(handle.getModifiers()))
    throw new IllegalStateException("Operator cannot be static")
    
final params = handle.getParameterTypes()
if (params.length == 0 || !isReadChannel(params[0])) {
    throw new IllegalStateException("Operator must have DataflowReadChannel as first param")
}
```

**Example from nf-sqldb**:
```groovy
@Operator
DataflowWriteChannel sqlInsert(DataflowReadChannel source, Map opts=null) {
    CheckHelper.checkParams('sqlInsert', opts, INSERT_PARAMS)
    final dataSource = dataSourceFromOpts(opts)
    final target = CH.createBy(source)
    final singleton = target instanceof DataflowExpression
    final insert = new InsertHandler(dataSource, opts)

    final next = { it ->
        insert.perform(it)
        target.bind(it)
    }

    final done = {
        insert.close()
        if (!singleton) target.bind(Channel.STOP)
    }

    DataflowHelper.subscribeImpl(source, [onNext: next, onComplete: done])
    return target
}
```

**Usage in Pipeline**:
```groovy
include { sqlInsert } from 'plugin/nf-sqldb'

channel
    .of([1,'x'], [2,'y'])
    .sqlInsert(into: 'FOO', columns: 'id,name', db: 'test')
```

---

### 3. Function Methods (@Function)

**Purpose**: Standalone functions (not channel operators)

**Requirements**:
```groovy
@Function
def functionName(/* parameters */) {
    // Can return any type
    // Not a channel operation
    // Called as: functionName(args)
}
```

**Example from nf-sqldb**:
```groovy
@Function
Map sqlExecute(Map params) {
    final dbName = (params.db ?: 'default') as String
    final statement = params.statement as String
    
    if (!statement)
        return [success: false, error: "Missing parameter 'statement'"]
    
    // Execute SQL and return result map
    try {
        // ... execution logic
        return [success: true, result: affectedRows]
    } catch (Exception e) {
        return [success: false, error: e.message]
    }
}
```

**Usage in Pipeline**:
```groovy
include { sqlExecute } from 'plugin/nf-sqldb'

def result = sqlExecute(db: 'test', statement: 'DROP TABLE FOO')
if (result.success) {
    println "Operation succeeded"
}
```

---

## Overloading vs. New Operators

### Can We Overload Core Operators?

**Answer: NO** ❌

From `PluginExtensionProvider.groovy`:
```groovy
// When loading plugin operators:
final existing = operatorExtensions.get(aliasName)
if (existing.is(OperatorImpl.instance)) {
    throw new IllegalStateException(
        "Operator '$realName' is already defined as a built-in operator - " +
        "Offending plugin '$pluginId'"
    )
}
```

**Key Insight**: If an operator name matches a built-in, plugin loading **FAILS**.

### Implications for Our Project

We CANNOT create:
- ❌ `join(ch, Closure keyExtractor)` - name collision
- ❌ `groupTuple(Closure keyExtractor)` - name collision
- ❌ `combine(ch, Closure keyExtractor)` - name collision

We MUST create:
- ✅ `joinBy(ch, Closure keyExtractor)` - new name
- ✅ `groupTupleBy(Closure keyExtractor)` - new name
- ✅ `combineBy(ch, Closure keyExtractor)` - new name

**Alternative Considered**: Use `by` parameter with Closure
- Would require extending parameter types in existing operators
- Not possible from plugin (would modify core)
- Could be submitted as upstream Nextflow PR (future work)

---

## Implementation Pattern

### Recommended Structure for Our Plugin

```groovy
// src/main/groovy/nfneuro/extension/ChannelGroupingExtension.groovy
package nfneuro.extension

import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Session
import nextflow.plugin.extension.Operator
import nextflow.plugin.extension.PluginExtensionPoint

class ChannelGroupingExtension extends PluginExtensionPoint {
    
    private Session session
    
    @Override
    protected void init(Session session) {
        this.session = session
    }
    
    @Operator
    DataflowWriteChannel groupTupleBy(
        DataflowReadChannel source, 
        Closure keyExtractor,
        Map opts = [:]
    ) {
        // Validate keyExtractor
        if (!keyExtractor) {
            throw new IllegalArgumentException("keyExtractor closure required")
        }
        
        // Implementation using KeyExtractor utility
        def target = CH.createBy(source)
        def handler = new GroupTupleByOp(source, keyExtractor, opts)
        handler.apply(target)
        return target
    }
    
    @Operator
    DataflowWriteChannel joinBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure keyExtractor,
        Map opts = [:]
    ) {
        // NOTE: Second channel must be added to OpCall inputs manually
        // (See JoinOp.groovy pattern in Nextflow core)
        
        def target = CH.create()
        def handler = new JoinByOp(left, right, keyExtractor, opts)
        handler.apply(target)
        return target
    }
    
    @Operator
    DataflowWriteChannel combineBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure keyExtractor,
        Map opts = [:]
    ) {
        def target = CH.create()
        def handler = new CombineByOp(left, right, keyExtractor, opts)
        handler.apply(target)
        return target
    }
}
```

---

## Registration and Loading

### Plugin Descriptor

**File**: `src/resources/META-INF/extensions.idx`
```
nfneuro.extension.ChannelGroupingExtension
```

### Plugin Class

```groovy
class NfBidsPlugin extends BasePlugin {
    NfBidsPlugin(PluginWrapper wrapper) {
        super(wrapper)
    }
}
```

### Manifest

**File**: `src/resources/META-INF/MANIFEST.MF`
```
Manifest-Version: 1.0
Plugin-Class: nfneuro.plugin.NfBidsPlugin
Plugin-Id: nf-bids
Plugin-Provider: Seqera Labs
Plugin-Version: 0.2.0
```

---

## Usage Pattern

### Including Plugin Operators

```groovy
// In nextflow.config
plugins {
    id 'nf-bids@0.2.0'
}

// In workflow script
include { groupTupleBy; joinBy; combineBy } from 'plugin/nf-bids'

workflow {
    // Group by closure instead of index
    channel
        .of(
            [sampleId: 'A', file: 'a.txt'],
            [sampleId: 'A', file: 'b.txt'],
            [sampleId: 'B', file: 'c.txt']
        )
        .groupTupleBy { it.sampleId }
        .view()
    
    // Join by computed key
    ch1.joinBy(ch2) { it.subject + '_' + it.session }
}
```

---

## Multi-Channel Operators Pattern

### Challenge: Operators with Multiple Input Channels

From `JoinOp.groovy` in Nextflow core:
```groovy
DataflowWriteChannel join(DataflowReadChannel left, Map opts, right) {
    if (right == null) 
        throw new IllegalArgumentException("join argument cannot be null")
    if (!(right instanceof DataflowReadChannel)) 
        throw new IllegalArgumentException("Invalid join argument")
    
    // CRITICAL: Add second channel to inputs tracking
    OpCall.current.get().inputs.add(right)
    
    def target = new JoinOp(left, right, opts).apply()
    return target
}
```

**Key Pattern**: When operator has multiple channel inputs, must manually register them with `OpCall`.

### Our Implementation Pattern

```groovy
@Operator
DataflowWriteChannel joinBy(
    DataflowReadChannel left,
    DataflowReadChannel right,
    Closure keyExtractor,
    Map opts = [:]
) {
    // Validate inputs
    if (right == null) {
        throw new IllegalArgumentException("Right channel cannot be null")
    }
    if (!(right instanceof DataflowReadChannel)) {
        throw new IllegalArgumentException("Right operand must be a channel")
    }
    
    // IMPORTANT: Register second channel for DAG tracking
    // (This may not be accessible from plugin - needs testing)
    // OpCall.current.get()?.inputs?.add(right)
    
    def target = CH.create()
    def handler = new JoinByOp(left, right, keyExtractor, opts)
    handler.apply(target)
    return target
}
```

**Testing Note**: Need to verify if `OpCall.current` is accessible from plugins or core-only.

---

## Parameter Validation Pattern

### CheckHelper Utility (Core)

```groovy
import nextflow.util.CheckHelper

static final Map OPERATOR_PARAMS = [
    size: Integer,
    sort: [Boolean, String, Closure, Comparator],
    remainder: Boolean
]

CheckHelper.checkParams('groupTupleBy', opts, OPERATOR_PARAMS)
```

### Custom Validation for Closures

```groovy
private void validateKeyExtractor(Closure keyExtractor) {
    if (keyExtractor == null) {
        throw new IllegalArgumentException(
            "keyExtractor closure is required"
        )
    }
    
    // Check closure arity (parameter count)
    int params = keyExtractor.getMaximumNumberOfParameters()
    if (params != 1) {
        throw new IllegalArgumentException(
            "keyExtractor must accept exactly one parameter (the channel item)"
        )
    }
}
```

---

## Async Execution Pattern

### From nf-sqldb's Async Query

```groovy
protected DataflowWriteChannel queryToChannel(String query, Map opts) {
    final channel = CH.create()
    final dataSource = dataSourceFromOpts(opts)
    final handler = new QueryHandler()
            .withDataSource(dataSource)
            .withStatement(query)
            .withTarget(channel)
            .withOpts(opts)
    
    if (NF.dsl2) {
        // DSL2: Use igniter for async execution
        session.addIgniter {-> handler.perform(true) }
    }
    else {
        // DSL1: Execute directly
        handler.perform(true)
    }
    
    return channel
}
```

**Key Insights**:
- Use `session.addIgniter` for async operations in DSL2
- Prevents blocking workflow execution
- Channel is returned immediately, populated later

### Our Operators Don't Need Async

**Why**: Our grouping operators process items as they arrive (streaming), not bulk operations.

Pattern from `GroupTupleOp.groovy`:
```groovy
DataflowWriteChannel apply() {
    if (target == null)
        target = CH.create()
    
    // Subscribe to source with handlers
    DataflowHelper.subscribeImpl(channel, [
        onNext: this.&collect, 
        onComplete: this.&finalise
    ])
    
    return target
}
```

---

## Testing Pattern

### Unit Test Structure

```groovy
import groovy.util.logging.Slf4j
import nextflow.Channel
import nextflow.Session
import spock.lang.Specification

@Slf4j
class ChannelGroupingExtensionTest extends Specification {
    
    def setupSpec() {
        new Session()
    }
    
    def 'should group by closure key extractor'() {
        given:
        def ext = new ChannelGroupingExtension()
        ext.init(Mock(Session))
        
        def channel = Channel.of(
            [id: 1, name: 'A'],
            [id: 1, name: 'B'],
            [id: 2, name: 'C']
        )
        
        when:
        def result = ext.groupTupleBy(channel) { it.id }
        
        then:
        result.val == [1, [[id: 1, name: 'A'], [id: 1, name: 'B']]]
        result.val == [2, [[id: 2, name: 'C']]]
        result.val == Channel.STOP
    }
}
```

### Integration Test with nf-test

```groovy
// tests/groupTupleBy.nf.test
nextflow_process {
    
    name "Test groupTupleBy operator"
    
    script "test_groupTupleBy.nf"
    
    test("Should group by subject ID") {
        
        when {
            params {
                input = "test_data.csv"
            }
        }
        
        then {
            assert workflow.success
            assert workflow.trace.tasks().size() == 3
        }
    }
}
```

---

## Comparison with Core Operators

### Core `groupTuple` Implementation

**File**: `GroupTupleOp.groovy`

```groovy
class GroupTupleOp {
    private List<Integer> indices  // Key positions
    
    private void collect(List tuple) {
        final key = normalizeKey(tuple[indices])  // Extract by index
        final items = groups.getOrCreate(key) { /* ... */ }
        // ... accumulate items
    }
}
```

**Limitations**:
- Index-based only: `by: [0, 2]`
- Cannot extract from maps: `{ it.sample_id }`
- Cannot compute keys: `{ it.subject + '_' + it.session }`

### Our `groupTupleBy` Implementation (Concept)

```groovy
class GroupTupleByOp {
    private Closure keyExtractor  // User-provided closure
    
    private void collect(def item) {
        // Call closure to get key
        final key = keyExtractor.call(item)
        final items = groups.getOrCreate(key) { /* ... */ }
        // ... accumulate items
    }
}
```

**Advantages**:
- ✅ Works with maps: `{ it.sample_id }`
- ✅ Computed keys: `{ [it.subject, it.session] }`
- ✅ Conditional logic: `{ it.type == 'anat' ? it.subject : null }`
- ✅ Nested access: `{ it.metadata.bids.subject }`

---

## Next Steps (Phase 2.1)

### Questions Answered

1. ✅ **Can we overload existing operators?**
   - **No** - name collisions cause plugin loading failure
   
2. ✅ **How do plugins extend operators?**
   - Extend `PluginExtensionPoint`
   - Use `@Operator` annotation
   - First param must be `DataflowReadChannel`
   
3. ✅ **Examples of multi-parameter type support?**
   - Not in overloading sense
   - Can use method overloading (multiple signatures)
   - Example: `fromQuery(String)` and `fromQuery(Map, String)`

### Remaining Decisions for Phase 2

1. **Naming Convention**:
   - Option A: `groupTupleBy`, `joinBy`, `combineBy` (clear intent)
   - Option B: `groupTupleClosure`, `joinClosure` (verbose)
   - Option C: `closureGroupTuple`, `closureJoin` (awkward)
   - **Recommendation**: Option A (matches Ruby/Kotlin conventions)

2. **Closure Signature**:
   - Single-param: `{ item -> key }`
   - Support list keys: `{ item -> [key1, key2] }`
   - Handle null keys? (filter out or error?)

3. **Backward Compatibility**:
   - Keep index-based `by` parameter?
   - Make closure and index mutually exclusive?
   - Default behavior when neither provided?

4. **Multi-Channel Operators**:
   - Can we access `OpCall.current` from plugins?
   - If not, document limitation or find workaround

---

## Key Takeaways

### What We Learned

1. **Plugin Extension is Straightforward**:
   - Well-documented pattern
   - Production-tested in nf-sqldb
   - Clear rules and validation

2. **Cannot Overload Core Operators**:
   - Must use new names
   - Naming is critical for discoverability
   - Users will need migration guidance

3. **Implementation Pattern Exists**:
   - `CrossOp` already shows closure-based key extraction
   - Can adapt pattern for our operators
   - DataflowHelper provides utilities

4. **Testing is Standard**:
   - Spock for unit tests
   - nf-test for integration
   - Same patterns as core operators

### Architecture Decision

**Chosen Approach**: Create new operators with `By` suffix
- `groupTupleBy(Closure)` - closure-based groupTuple
- `joinBy(Closure)` - closure-based join
- `combineBy(Closure)` - closure-based combine

**Rationale**:
- Cannot overload (technical constraint)
- Clear naming convention (user clarity)
- Follows Groovy/Kotlin patterns (`sortBy`, `groupBy`)
- Backward compatible (old operators still work)

---

## References

**Source Files**:
- `nextflow/plugin/extension/PluginExtensionPoint.groovy` - Base class
- `nextflow/plugin/extension/Operator.groovy` - Annotation
- `nextflow/plugin/extension/PluginExtensionProvider.groovy` - Loading/validation
- `nf-sqldb/ChannelSqlExtension.groovy` - Complete example
- `nextflow/extension/CrossOp.groovy` - Closure-based reference

**Documentation**:
- [Nextflow Plugin Development](https://www.nextflow.io/docs/latest/plugins.html)
- [nf-sqldb Plugin](https://github.com/nextflow-io/nf-sqldb)

