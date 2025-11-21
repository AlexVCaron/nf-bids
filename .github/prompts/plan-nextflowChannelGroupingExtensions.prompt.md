## Plan: Extend Nextflow Channel Grouping Operations with Custom Key Extractors

### Overview
Enhance Nextflow's channel grouping operators by adding closure-based key extraction capabilities, allowing dynamic grouping logic instead of fixed index-based grouping. This will provide more flexible and expressive data manipulation in Nextflow pipelines.

---

### Phase 1: Research & Discovery

#### Step 1.1: Identify All Grouping Operations in Nextflow
**Goal**: Create comprehensive list of operators that perform grouping/joining operations.

**Actions**:
- Review Nextflow documentation for channel operators
- Examine Nextflow source code (likely in `nextflow-io/nextflow` repository)
- Select the exact operators that perform grouping or joining of channel's items, disregarding the operator that only act on the inner content of those items :
  - **Join operators**: `join`, `cross`, `combine`
  - **Grouping operators**: `groupTuple`, `groupKey`
  - **Collection operators**: `collect`, `toList`, `toSortedList`
  - **Aggregation operators**: `reduce`, `collectFile`
  - **Splitting operators**: `branch`, `multiMap`
- Document each selected operator's:
  - Current signature and parameters
  - Existing key extraction mechanism (index, by, etc.)
  - Return value structure
  - Use cases and limitations

**Deliverable**: Markdown table with columns: `Operator`, `Current Grouping Method`, `Extension Opportunity`, `Priority`

#### Step 1.2: Study Nextflow Plugin Extension Points
**Goal**: Understand how to extend channel operators in plugins.

**Actions**:
- Examine existing plugins that extend operators (check `nextflow-io/plugins`)
- Study Nextflow's operator extension API:
  - `OperatorEx` interface/class usage
  - How operators are registered in plugins
  - Method interception vs. new operator creation
- Review `nf-bids` plugin structure for patterns already used
- Investigate if operators can be "overridden" or only "added"
- Check if there's a way to add overloads to existing operators

**Key Questions**:
- Can we add overloaded versions of existing operators?
- How are operator parameters validated and parsed?
- What's the lifecycle of operator registration?

**Deliverable**: Technical notes document with code examples from existing plugins

#### Step 1.3: Analyze Current Key Extraction Patterns
**Goal**: Understand existing patterns and their limitations.

**Actions**:
- Document how `join` uses positional indices
- Document how `groupTuple` uses `by` parameter (numeric indices)
- Examine how `groupKey` works with explicit key objects
- Identify common use cases where current approach is limiting:
  - Grouping by computed values
  - Grouping by nested structure fields
  - Conditional grouping logic

**Deliverable**: Use case comparison document showing current vs. desired syntax

---

### Phase 2: Design & Architecture

#### Step 2.1: Define Enhanced Operator Signatures
**Goal**: Design closure-based API that's intuitive and Groovy-idiomatic.

**Design Considerations**:
- Closure should receive channel item as input
- Should return key(s) for grouping
- Should be backwards compatible with existing index-based approach
- Consider multi-key extraction for complex joins

**Example Proposed Syntax**:
```groovy
// Current: join by index 0
channel1.join(channel2, by: 0)

// Proposed: join by closure
channel1.join(channel2) { it.sampleId }
channel1.join(channel2, by: { it.metadata.subject })

// Multiple keys
channel1.join(channel2) { [it.subject, it.session] }
```

**Actions**:
- Draft API signature for each prioritized operator
- Define closure parameter naming convention
- Specify return type expectations
- Handle edge cases (null keys, missing fields, type mismatches)

**Deliverable**: API design document with signature examples for each operator

#### Step 2.2: Plan Implementation Strategy
**Goal**: Determine technical approach for each operator.

**Options to Evaluate**:
1. **Create new operators** (e.g., `joinBy`, `groupTupleBy`) - safest, clearest
2. **Overload existing operators** - best UX if possible
3. **Add `keyExtractor` parameter** - backwards compatible

**Recommended Approach**: Start confirming feasibility of overloading and adding a parameter. If not feasible, create new operators.

**Actions**:
- Map each operator to implementation complexity (High/Medium/Low)
- Identify shared utility code for key extraction
- Plan testing strategy for each operator
- Consider performance implications

**Deliverable**: Implementation roadmap with effort estimates

---

### Phase 3: Implementation

#### Step 3.1: Create Plugin Infrastructure
**Actions**:
- Create base utility class for closure-based key extraction:
  ```groovy
  class KeyExtractor {
      static def extractKey(def item, Closure keyExtractor)
      static def extractMultiKey(def item, Closure keyExtractor)
      static boolean keysMatch(def key1, def key2)
  }
  ```
- Implement key normalization and comparison logic
- Add comprehensive error handling for closure failures
- Write unit tests for KeyExtractor utility

**Files to Create**:
- `src/main/groovy/nfneuro/channel/KeyExtractor.groovy`
- `src/test/groovy/nfneuro/channel/KeyExtractorTest.groovy`

**Important Note**: Nextflow plugins can only have **one extension point** per plugin. All operators must be added to the same extension class (e.g., `BidsExtension`), not split into separate extension classes.

#### Step 3.2: Implement High-Priority Operators
**Recommended Order** (most impactful first):
1. `groupTupleBy` - extends groupTuple with closure
2. `joinBy` - extends join with closure
3. `crossBy` - extends cross with closure
4. `combineBy` - extends combine with closure

**For Each Operator**:
- Extend appropriate Nextflow operator base class
- Implement closure parameter handling
- Add validation for closure return types
- Write integration tests with realistic data
- Document with examples

**Example Implementation Skeleton**:
```groovy
@CompileStatic
class BidsExtension extends PluginExtensionPoint {
    
    // Note: This class combines both @Factory and @Operator methods
    // because Nextflow plugins can only have ONE extension point
    
    @Factory
    DataflowWriteChannel fromBIDS(...) {
        // Existing factory method
    }
    
    @Operator
    DataflowWriteChannel groupTupleBy(
        DataflowReadChannel source,
        Closure keyExtractor,
        Map opts = [:]
    ) {
        // Extract keys for each item
        // Group by extracted keys
        // Return grouped channel
    }
}
```

#### Step 3.3: Add Plugin Registration
**Actions**:
- Register new operators in plugin manifest
- Add operator documentation strings
- Configure operator visibility and namespace
- Update plugin version and changelog

**Important**: Ensure `META-INF/extensions.idx` contains **only one extension point** (e.g., `nfneuro.plugin.BidsExtension`). Nextflow enforces a single extension point per plugin.

**Files to Modify**:
- `src/resources/META-INF/extensions.idx` (should have only ONE line)
- `build.gradle` (extensionPoints should have only ONE entry)
- Version configuration

---

### Phase 4: Testing & Documentation

#### Step 4.1: Comprehensive Testing
**Test Categories**:
- **Unit tests**: KeyExtractor utility functions
- **Integration tests**: Each operator with various data types
- **Performance tests**: Compare with built-in operators
- **Edge case tests**: Empty channels, null values, closure exceptions

**Test Data Scenarios**:
- Simple tuples: `[id, file]`
- Complex maps: `[metadata: [subject: ..., session: ...], files: [...]]`
- Nested structures from BIDS data
- Large channel volumes (performance)

**Actions**:
- Create nf-test scenarios for each operator
- Add comparison tests vs. current index-based approach
- Test error handling and meaningful error messages

#### Step 4.2: Documentation
**Deliverables**:
- **README section**: Overview of new operators with quick examples
- **API documentation**: Detailed parameter descriptions, return types
- **Tutorial**: Step-by-step guide with real-world examples
- **Migration guide**: How to convert from index-based to closure-based
- **Examples directory**: Complete working pipelines demonstrating each operator

**Example Structure**:
```
docs/
  channel-extensions/
    README.md
    groupTupleBy.md
    joinBy.md
    examples/
      basic-grouping.nf
      bids-subject-grouping.nf
      multi-key-join.nf
```

---

### Phase 5: Validation & Iteration

#### Step 5.1: Real-World Testing
**Actions**:
- Apply new operators to existing nf-bids workflows
- Gather feedback from pipeline developers
- Measure performance impact
- Identify missing features or edge cases

#### Step 5.2: Refinement
**Actions**:
- Address feedback and bug reports
- Optimize performance bottlenecks
- Add requested features
- Improve error messages based on user confusion

---

### Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Nextflow doesn't allow operator overloading | High | Use new operator names (e.g., `joinBy`) |
| Performance overhead from closures | Medium | Profile and optimize; add caching for key extraction |
| Breaking changes in Nextflow API | High | Pin to specific Nextflow version; monitor releases |
| Closure serialization issues | Medium | Document limitations; validate closure types early |

---

### Success Criteria

- [ ] At least 4 grouping operators extended with closure support
- [ ] All tests passing (unit, integration, performance)
- [ ] Documentation complete with 5+ real-world examples
- [ ] Performance within 10% of built-in operators
- [ ] Zero breaking changes to existing nf-bids functionality
- [ ] Positive feedback from at least 3 external users

---

### Timeline Estimate

- **Phase 1 (Research)**: 3-5 days
- **Phase 2 (Design)**: 2-3 days
- **Phase 3 (Implementation)**: 1-2 weeks
- **Phase 4 (Testing/Docs)**: 1 week
- **Phase 5 (Validation)**: 1 week

**Total**: 3-4 weeks for MVP with 4 operators
