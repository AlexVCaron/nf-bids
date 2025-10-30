# nf-bids Plugin Refactoring Plan

**Status**: 🔄 Planning Phase  
**Created**: October 29, 2025  
**Priority**: Medium (Post-100% Baseline Alignment)  
**Goal**: Optimize code quality, reduce duplication, improve maintainability

---

## Refactoring Strategy

### Phase 1: Analysis & Planning ✅ (This Document)
**Duration**: 1-2 days  
**Objective**: Comprehensive codebase analysis to identify optimization opportunities

### Phase 2: Investigation & Design
**Duration**: 2-3 days  
**Objective**: Deep dive into identified areas, design refactoring patterns

### Phase 3: Implementation
**Duration**: 1-2 weeks (iterative)  
**Objective**: Execute refactorings with continuous validation

### Phase 4: Validation & Testing
**Duration**: 2-3 days  
**Objective**: Ensure 100% baseline alignment maintained after changes

### Phase 5: Documentation Update
**Duration**: 1 day  
**Objective**: Update architecture docs and inline comments

---

## Initial Codebase Analysis

### Code Complexity Metrics

**Largest Files** (lines of code):
1. `SequentialSetHandler.groovy` - **684 lines** 🔴 High complexity
2. `BidsHandler.groovy` - **581 lines** 🔴 High complexity
3. `MixedSetHandler.groovy` - **496 lines** 🟡 Medium complexity
4. `NamedSetHandler.groovy` - **398 lines** 🟡 Medium complexity
5. `PlainSetHandler.groovy` - **351 lines** 🟡 Medium complexity
6. `BaseSetHandler.groovy` - **319 lines** 🟡 Medium complexity
7. `ConfigValidator.groovy` - **322 lines** 🟡 Medium complexity

**Total Codebase**: ~6,129 lines across 26 Groovy files

### Architecture Overview

```
plugin/
├── BidsPlugin.groovy          (Entry point)
├── BidsExtension.groovy       (@Factory annotation)
├── BidsFactory.groovy         (Factory pattern)
└── BidsObserver.groovy        (Session observer)

channel/
├── BidsChannelFactory.groovy  (Channel creation)
└── BidsHandler.groovy         (Async processing - 581 lines)

grouping/
├── BaseSetHandler.groovy      (Abstract base - 319 lines)
├── PlainSetHandler.groovy     (Plain sets - 351 lines)
├── NamedSetHandler.groovy     (Named sets - 398 lines)
├── SequentialSetHandler.groovy (Sequential - 684 lines)
└── MixedSetHandler.groovy     (Mixed sets - 496 lines)

model/
├── BidsFile.groovy            (File representation)
├── BidsDataset.groovy         (Dataset collection)
├── BidsEntity.groovy          (Entity model)
└── BidsChannelData.groovy     (Channel data structure)

parser/
├── BidsParser.groovy          (Dataset parsing)
├── BidsValidator.groovy       (Validation)
└── LibBidsShWrapper.groovy    (libBIDS.sh integration)

config/
├── BidsConfigLoader.groovy    (YAML loading)
└── BidsConfigAnalyzer.groovy  (Config analysis)

util/
├── BidsCsvParser.groovy       (CSV parsing - 309 lines)
├── BidsEntityUtils.groovy     (Entity utilities)
├── BidsErrorHandler.groovy    (Error handling)
├── BidsLogger.groovy          (Logging)
└── SuffixMapper.groovy        (Suffix mapping)
```

---

## Identified Code Duplication Patterns

### 1. 🔴 **CRITICAL: Duplicate `makeRelativePath()` Method**

**Occurrences**: 4 identical copies
- `PlainSetHandler.groovy:83`
- `NamedSetHandler.groovy:61`
- `SequentialSetHandler.groovy:61`
- `MixedSetHandler.groovy:62`

**Code Duplication**:
```groovy
private static String makeRelativePath(String absolutePath) {
    def pathParts = absolutePath.split('/')
    def datasetIndex = -1
    for (int i = 0; i < pathParts.length; i++) {
        if (pathParts[i] in ['custom', 'bids-examples']) {
            datasetIndex = i + 1
            break
        }
    }
    if (datasetIndex > 0 && datasetIndex < pathParts.length) {
        return pathParts[datasetIndex..-1].join('/')
    }
    return absolutePath
}
```

**Impact**: ~60 lines of duplicate code  
**Solution**: Move to `BaseSetHandler` or utility class

---

### 2. 🔴 **CRITICAL: Duplicate `exclude_entities` Logic**

**Occurrences**: 2 nearly identical implementations
- `PlainSetHandler.groovy:207-221`
- `SequentialSetHandler.groovy:188-210`

**Code Pattern**:
```groovy
if (sequentialSetConfig.exclude_entities) {
    def excludeList = sequentialSetConfig.exclude_entities as List<String>
    for (String entityName : excludeList) {
        def normalizedEntity = normalizeEntityName(entityName)
        def entityValue = file.getEntity(normalizedEntity)
        if (entityValue && entityValue != "NA") {
            BidsLogger.trace("File excluded by entity ${entityName}: ${file.filename}")
            return  // Skip this file
        }
    }
}
```

**Impact**: ~30 lines of duplicate code  
**Solution**: Extract to `BaseSetHandler.shouldExcludeFile(file, config)` method

---

### 3. 🟡 **HIGH: Common Entity Normalization Pattern**

**Occurrences**: Throughout all handlers
- Used in `buildGroupKey()`, entity matching, value extraction
- Pattern: `normalizeEntityName(entity)` before `file.getEntity()`

**Current Implementation**: `BaseSetHandler.normalizeEntityName()` exists but could be optimized

**Potential Improvements**:
- Cache normalized entity names (static map)
- Consider bidirectional mapping (short→long, long→short)
- Consolidate with `BidsEntityUtils`

---

### 4. 🟡 **HIGH: File Extension Categorization**

**Occurrences**: Multiple handlers have similar logic
- `PlainSetHandler.getExtensionType()`
- `PlainSetHandler.isPrimaryFile()`
- Similar logic scattered in other handlers

**Impact**: ~40 lines across multiple files  
**Solution**: Centralize in utility class `FileTypeClassifier`

---

### 5. 🟡 **MEDIUM: Entity Value Matching Logic**

**Pattern**: Checking entity values against config patterns
- `NamedSetHandler.entityValuesMatch()`
- `MixedSetHandler.entityValuesMatch()`
- Similar logic in `BaseSetHandler.entitiesMatch()`

**Potential Improvements**:
- Consolidate into single `EntityMatcher` utility
- Support wildcards, regex, value lists
- Improve prefix handling (mt-on → on)

---

### 6. 🟡 **MEDIUM: Parts Grouping Logic**

**Occurrences**: 
- `SequentialSetHandler.buildSequenceWithParts()` (special handling)
- Parts detection: `file.getEntity('part')` checks

**Code Pattern**:
```groovy
def partValue = file.getEntity('part')
if ((!partValue || partValue == "NA") && file.path.endsWith('.json')) {
    // JSON file without part entity
    jsonFiles << makeRelativePath(file.path)
}
```

**Solution**: Extract to `PartsGroupingStrategy` class

---

### 7. 🟢 **LOW: Logging Patterns**

**Occurrences**: Consistent logging throughout handlers
- Progress logging
- Debug traces
- Warning messages

**Current State**: Already centralized in `BidsLogger`  
**Potential Improvement**: Add log levels, structured logging

---

## Refactoring Priorities

### P0: Critical Refactorings (Must-Have)
**Impact**: Significant code reduction, improved maintainability

1. **Extract `makeRelativePath()` to Utility Class**
   - Target: `BidsPathUtils.makeRelativePath()`
   - Impact: Removes 60+ lines of duplication
   - Risk: Low (pure function, well-tested)

2. **Extract `exclude_entities` Logic to Base Class**
   - Target: `BaseSetHandler.shouldExcludeFile()`
   - Impact: Removes 30+ lines, centralizes filtering
   - Risk: Low (clear behavior, covered by tests)

3. **Consolidate File Type Classification**
   - Target: `FileTypeClassifier` utility class
   - Methods: `getExtensionType()`, `isPrimaryFile()`, `isSidecarFile()`
   - Impact: Removes 40+ lines across handlers
   - Risk: Low (deterministic logic)

---

### P1: High Priority Refactorings (Should-Have)
**Impact**: Code quality and performance improvements

4. **Optimize Entity Name Normalization**
   - Add static caching for frequently accessed mappings
   - Consider merging with `BidsEntityUtils`
   - Impact: Performance gain, cleaner API
   - Risk: Low (backwards compatible)

5. **Extract Entity Matching Logic**
   - Target: `EntityMatcher` utility class
   - Consolidate `entitiesMatch()`, `entityValuesMatch()` variations
   - Support advanced patterns (wildcards, lists)
   - Impact: More flexible matching, reduced duplication
   - Risk: Medium (needs careful testing)

6. **Extract Parts Grouping Strategy**
   - Target: `PartsGroupingStrategy` class
   - Encapsulate magnitude/phase pairing logic
   - Impact: Cleaner sequential handler, reusable logic
   - Risk: Medium (complex behavior)

---

### P2: Medium Priority Refactorings (Nice-to-Have)
**Impact**: Code organization and clarity

7. **Reduce Handler Size**
   - Break down large handlers into smaller focused classes
   - Target: `SequentialSetHandler` (684 lines → ~400 lines)
   - Extract strategies: FlatSequencing, HierarchicalSequencing
   - Impact: Better SRP compliance, easier testing
   - Risk: Medium (behavioral preservation critical)

8. **Improve Configuration Validation**
   - `ConfigValidator` (322 lines) could be split
   - Separate validators per set type
   - Impact: More modular validation logic
   - Risk: Low (validation isolated)

9. **Standardize Error Handling**
   - Review `BidsErrorHandler` usage consistency
   - Add context-aware error messages
   - Impact: Better debugging experience
   - Risk: Low (non-functional improvement)

---

### P3: Low Priority Refactorings (Future)
**Impact**: Polish and optimization

10. **Add @CompileStatic Where Possible**
    - Currently disabled in handlers (see TODO comments)
    - Requires model alignment work
    - Impact: Performance, type safety
    - Risk: High (significant refactoring)

11. **Optimize Channel Emission**
    - Review `BidsHandler` (581 lines) for optimization
    - Consider batching strategies
    - Impact: Performance for large datasets
    - Risk: Medium (async behavior)

12. **Improve Test Coverage**
    - Add integration tests for refactored components
    - Stress test with large datasets
    - Impact: Confidence in refactorings
    - Risk: Low (testing improvement)

---

## Investigation Plan

### Investigation 1: Handler Commonality Analysis
**Duration**: 4 hours  
**Goal**: Identify common patterns across all 4 handlers

**Tasks**:
1. ✅ Map all methods in each handler (completed in analysis)
2. Create method comparison matrix
3. Identify candidates for extraction to base class
4. Document behavioral differences

**Deliverable**: Method comparison spreadsheet

---

### Investigation 2: Entity Normalization Deep Dive
**Duration**: 3 hours  
**Goal**: Optimize entity name handling throughout codebase

**Tasks**:
1. Trace all `normalizeEntityName()` call sites
2. Measure frequency of calls (profiling)
3. Design caching strategy
4. Evaluate merge with `BidsEntityUtils`

**Deliverable**: Entity normalization optimization design doc

---

### Investigation 3: Parts Grouping Analysis
**Duration**: 4 hours  
**Goal**: Understand parts handling for extraction

**Tasks**:
1. Document all parts-related logic locations
2. Trace magnitude/phase pairing algorithm
3. Identify JSON sidecar association logic
4. Design `PartsGroupingStrategy` interface

**Deliverable**: Parts grouping strategy design

---

### Investigation 4: Sequential Handler Complexity
**Duration**: 6 hours  
**Goal**: Break down 684-line handler into smaller components

**Tasks**:
1. Create control flow diagram
2. Identify natural seams for splitting
3. Document hierarchical vs flat sequencing differences
4. Design extraction strategy

**Deliverable**: Sequential handler refactoring design

---

### Investigation 5: Performance Profiling
**Duration**: 4 hours  
**Goal**: Identify performance bottlenecks

**Tasks**:
1. Profile plugin with large datasets (>100 subjects)
2. Measure time spent in each handler
3. Identify hot paths
4. Document optimization opportunities

**Deliverable**: Performance profile report

---

### Investigation 6: Test Impact Analysis
**Duration**: 3 hours  
**Goal**: Ensure refactorings won't break baseline alignment

**Tasks**:
1. Review test coverage for each refactoring target
2. Identify gaps in test coverage
3. Design regression test strategy
4. Plan snapshot validation after refactorings

**Deliverable**: Test validation strategy

---

## Implementation Guidelines

### Before Starting Any Refactoring

1. **Create Feature Branch**
   ```bash
   git checkout -b refactor/<component-name>
   ```

2. **Run Baseline Tests**
   ```bash
   cd plugins/nf-bids
   make test
   nf-test test validation/*.nf.test
   ```

3. **Snapshot Current State**
   ```bash
   cd ../../tests
   bash compare_baseline_plugin.sh
   # Verify: 18/18 identical
   ```

### During Refactoring

1. **Small, Incremental Changes**
   - One refactoring per commit
   - Keep commits atomic and reversible

2. **Continuous Validation**
   ```bash
   # After each change:
   make test                              # Unit tests
   nf-test test validation/*.nf.test      # Integration tests
   bash compare_baseline_plugin.sh        # Baseline validation
   ```

3. **Document Changes**
   - Update inline comments
   - Document behavioral changes (if any)
   - Update architecture.md if structure changes

### After Refactoring

1. **Full Validation Suite**
   ```bash
   # Unit tests
   ./gradlew test
   
   # Integration tests
   nf-test test validation/*.nf.test
   
   # Baseline comparison
   cd ../../tests
   bash compare_baseline_plugin.sh
   cat comparison_reports/COMPARISON_INDEX.md
   ```

2. **Performance Validation**
   - Run with large dataset
   - Compare execution time before/after
   - Document any performance changes

3. **Code Review Checklist**
   - [ ] All tests passing
   - [ ] 18/18 baseline alignment maintained
   - [ ] No performance regression
   - [ ] Documentation updated
   - [ ] Code follows existing patterns
   - [ ] @CompileStatic compliance (where applicable)

---

## Risk Mitigation

### Critical Success Factors

1. **Preserve 100% Baseline Alignment**
   - Any refactoring that breaks alignment is rolled back
   - Validation after every change

2. **No Behavioral Changes**
   - Refactorings are code structure only
   - Output must remain identical

3. **Incremental Approach**
   - One component at a time
   - Easy to rollback if issues arise

### Rollback Strategy

If refactoring causes issues:

1. **Immediate Rollback**
   ```bash
   git reset --hard HEAD~1
   ```

2. **Root Cause Analysis**
   - Identify what broke
   - Document lesson learned
   - Adjust refactoring approach

3. **Re-attempt with Better Strategy**
   - Smaller steps
   - More validation points
   - Different extraction approach

---

## Success Metrics

### Quantitative Goals

- **Code Reduction**: Reduce total LOC by ~10-15% (600-900 lines)
- **Duplication**: Eliminate 90%+ of identified duplications
- **Handler Size**: Reduce largest handler from 684 → ~400 lines
- **Test Coverage**: Maintain or improve current coverage
- **Performance**: No regression (±5% acceptable variance)

### Qualitative Goals

- **Maintainability**: Easier to understand and modify handlers
- **Extensibility**: Simpler to add new set types
- **Testability**: More granular unit testing possible
- **Documentation**: Clearer code structure reflected in docs

---

## Timeline Estimate

**Total Duration**: 3-4 weeks (part-time, iterative)

### Week 1: Investigation Phase
- Complete 6 investigations
- Design refactoring approach
- Get stakeholder approval

### Week 2-3: Implementation Phase
- Execute P0 refactorings (critical)
- Execute P1 refactorings (high priority)
- Continuous validation

### Week 4: Validation & Polish
- Execute P2 refactorings (nice-to-have)
- Final validation suite
- Update documentation
- Code review and merge

---

## Next Steps

1. **Review This Plan** with stakeholders
2. **Prioritize Investigations** based on feedback
3. **Start with Investigation 1** (Handler Commonality Analysis)
4. **Begin P0 Refactorings** once investigations complete

---

## Appendix: Quick Wins

### Immediate Actions (No Investigation Needed)

1. **Move `makeRelativePath()` to Utils**
   - Effort: 30 minutes
   - Risk: Minimal
   - Impact: -60 lines duplication

2. **Extract `shouldExcludeFile()` to Base**
   - Effort: 1 hour
   - Risk: Low
   - Impact: -30 lines duplication

3. **Create `FileTypeClassifier` Utility**
   - Effort: 2 hours
   - Risk: Low
   - Impact: -40 lines duplication

**Quick Win Total**: ~130 lines removed in <4 hours work

These can be done immediately to show value before deeper refactorings.

---

## References

- **Current Status**: [STATUS.md](STATUS.md)
- **Architecture**: [docs/architecture.md](docs/architecture.md)
- **Development History**: [docs/DEVELOPMENT_HISTORY.md](docs/DEVELOPMENT_HISTORY.md)
- **Test Suite**: [docs/TEST_SUITE.md](docs/TEST_SUITE.md)

---

**Document Status**: ✅ Ready for Review  
**Next Action**: Stakeholder review and investigation prioritization
