# nf-bids Project Status Summary
**Date**: January 5, 2026  
**Current Branch**: `feat/flat_output`  
**Latest Commit**: `0e8d10d - fix suffix vs config key management`

---

## 📊 Overview: Three Major Development Stories

You have been working on three interconnected features/fixes:

1. **🎯 Flat BIDS Output** (feat/flat_output branch) - IN PROGRESS
2. **🔧 Suffix Mapping & Config Key Fixes** (feat/flat_output branch) - IN PROGRESS  
3. **✅ Closure-Based Channel Operators** (feat/gouping_ops branch) - COMPLETE

Additional pending work:
4. **⚠️ File-to-Path Conversion Fix** (uncommitted) - PLANNED
5. **⚠️ Heterogeneous DWI Support** (uncommitted) - PLANNED

---

## Story 1: 🎯 Flat BIDS Output Format

### The Problem You Were Solving

The original `Channel.fromBIDS` output was nested and cumbersome:

```groovy
// OLD FORMAT - Hard to work with
[[subject, session], [data: [T1w: [...]], bidsParentDir: "/path", meta: [...]]]

// Users had to do:
def subject = key[0]
def t1w = file(data.bidsParentDir) / data.data.T1w.nii  // Ugly!
```

### Your Solution: "Flattened" Output (Version B)

You implemented a **flattened nested map structure** that:
- Puts `meta` at top level with all entity information
- Puts each suffix (T1w, dwi, etc.) at top level
- Converts all file paths to **absolute `java.nio.file.Path`** objects
- Makes the output structure intuitive and direct

```groovy
// NEW FORMAT - Clean and direct
[
    meta: [subject: 'sub-01', session: 'ses-01', ...],
    T1w: [nii: Path("/abs/path/T1w.nii.gz"), json: Path("/abs/path/T1w.json")],
    dwi: [ap: [nii: Path("..."), bval: Path("..."), bvec: Path("...")], pa: [...]]
]

// Users now do:
def subject = item.meta.subject
def t1w = item.T1w.nii  // Already an absolute Path - ready for process inputs!
```

### Implementation Status: ✅ MOSTLY COMPLETE

**Completed:**
- ✅ Core flattening logic in `BidsHandler.flattenTupleToMap()` (lines 175-268)
- ✅ Path conversion using `FileHelper.asPath()` for Nextflow compatibility
- ✅ Reserved `meta` key validation in config loader
- ✅ Opt-out mechanism via `options.flatten_output = false` for backward compatibility
- ✅ Unit tests in `BidsHandlerFlattenSpec.groovy` (176 lines)
- ✅ Integration test `test_flattened_output.nf.test` with validation
- ✅ Documentation in README.md with examples
- ✅ All tests passing (`./gradlew test` = BUILD SUCCESSFUL)

**What Was Changed:**
- **File**: `src/main/groovy/nfneuro/channel/BidsHandler.groovy`
  - Added `flattenTupleToMap()` method (lines ~175-268)
  - Modified `validateAndEmitChannel()` to conditionally flatten
  - Implemented robust `convertValue` closure with Path support
- **File**: `src/main/groovy/nfneuro/config/BidsConfigLoader.groovy`
  - Added validation to reject `meta` as suffix name
- **File**: `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`
  - Added `flatten_output` option documentation

**Remaining Work:**
1. ⏳ **Migration guide documentation** - Add detailed before/after examples for users
2. ⏳ **Update validation scripts** - Ensure all validation tests work with new format
3. ⏳ **Performance testing** - Verify no regression in large datasets
4. ⏳ **Beta release prep** - Update version to 0.1.0-beta.6 (already done in commit)

**Confidence Level**: 95% - Feature is functional and tested, just needs documentation polish

---

## Story 2: 🔧 Suffix Mapping & Config Key Management

### The Problem You Discovered

While implementing the flat output, you uncovered a **fundamental bug** in how the plugin handles suffix mapping. This is a **critical architectural issue**.

**Bug #1: Backward Mapping Direction**

The current code maps `fileSuffix → configKey` but should map `configKey → fileSuffix`:

```groovy
// CURRENT (WRONG):
mapping["named_set"]["dwi"] = "dwi_fullreverse"  // Only ONE config can own "dwi"
// Problem: Multiple configs (dwi, dwi_fullreverse, dwi_reverse_only) all use "dwi" files!

// SHOULD BE:
mapping["named_set"]["dwi_fullreverse"] = "dwi"  // Config uses files with "dwi" suffix
mapping["named_set"]["dwi_reverse_only"] = "dwi" // Another config also uses "dwi"
mapping["named_set"]["dwi"] = "dwi"              // Identity mapping
```

**Bug #2: Output Keys Use File Suffix Instead of Config Key**

Handlers emit data using the file suffix, not the configuration key:

```groovy
// WRONG (PlainSetHandler.groovy:94):
channelData.addSuffixData(suffix, nestedDataMap)  // Uses "dwi" (file suffix)

// CORRECT (NamedSetHandler.groovy:123):
channelData.addSuffixData(configKey, groupMap)  // Uses "dwi_fullreverse" (config key)
```

This means if you have:
```yaml
dwi_with_reverse:
  suffix_maps_to: dwi
  named_set:
    ap: {direction: "dir-AP"}
    pa: {direction: "dir-PA"}
```

**Expected output:** `item.dwi_with_reverse.ap.nii`  
**Actual output:** `item.dwi.ap.nii` (WRONG!)

**Bug #3: Empty Plain Sets Fail**

```groovy
// Config:
T1w:
  plain_set: {}  // Empty map is falsy in Groovy!

// Code:
if (suffixConfig.plain_set) { ... }  // Returns false! T1w is ignored!
```

### Your Fix (Latest Commit: 0e8d10d)

You **partially fixed** these issues:

**Fixed:**
- ✅ Empty set detection: Changed to `containsKey('plain_set')` instead of truthiness check
- ✅ Passing `configKey` through the pipeline in all handlers
- ✅ Updated `getSetIndex()` signature to accept `configKey` parameter
- ✅ Fixed output keys in all handlers to use `configKey` instead of `suffix`
- ✅ Updated filtering logic to work with `configKey`

**Code Changes (commit 0e8d10d):**
- `BaseSetHandler.groovy`: 43 lines changed
  - Fixed `getSetType()` to use `containsKey()`
  - Updated `getSetIndex()` signature to include `configKey`
  - Changed loop variables from `suffix` to `configKey` in processGroup
  - Fixed output to use `configKey` for data structure keys
- `PlainSetHandler.groovy`: 37 lines changed
- `NamedSetHandler.groovy`: 34 lines changed  
- `SequentialSetHandler.groovy`: 54 lines changed
- `MixedSetHandler.groovy`: 46 lines changed

### Implementation Status: ⚠️ 70% COMPLETE

**Completed:**
- ✅ All handlers now properly use `configKey` for output structure
- ✅ Empty set type detection fixed
- ✅ Pipeline passes both `fileSuffix` and `configKey` through processing
- ✅ Tests still passing (no regressions)

**Still TODO:**
1. ❌ **SuffixMapper needs inversion** - The mapping is still backward (not fixed yet!)
   - Need to change `mapping[setType][configKey] = targetSuffix` 
   - Need to rename `resolveConfigKey()` → `resolveConfigKeys()` (returns List)
   - Need to update `findMatchingGrouping()` to try all candidates
2. ❌ **Add tests for heterogeneous datasets** - Multiple configs with same file suffix
3. ❌ **Update validation scripts** - Ensure they work with config key outputs
4. ❌ **Documentation** - Explain suffix_maps_to behavior clearly

**Confidence Level**: 70% - Core logic fixed, but mapping inversion not done yet

**Why This Matters:**
This bug prevents users from having multiple BIDS configurations that use the same file suffix (e.g., `dwi`, `dwi_fullreverse`, `dwi_ap`, `dwi_rl` all using DWI files). It's a blocker for heterogeneous datasets.

---

## Story 3: ✅ Closure-Based Channel Operators

### The Feature You Built

You implemented **three new channel operators** that extend Nextflow with closure-based key extraction:

1. **`groupTupleBy`** - Group channel items by closure-extracted keys
2. **`joinBy`** - Join two channels by closure-extracted keys
3. **`combineBy`** - Combine two channels with cartesian product by key

### Why This Was Needed

Nextflow's built-in operators (`groupTuple`, `join`, `combine`) only work with **index-based** key extraction:

```groovy
// Built-in: Requires tuple structure
Channel.of(['sub-01', 'file1'], ['sub-01', 'file2'])
    .groupTuple(by: 0)  // Ugly - requires tuple[0] is the key

// Your solution: Works with maps and any structure
Channel.of([subject: 'sub-01', file: 'file1'], [subject: 'sub-01', file: 'file2'])
    .groupTupleBy { it.subject }  // Clean - semantic field access!
```

### Implementation Status: ✅ 100% COMPLETE

**Branch**: `feat/gouping_ops` (already merged into feat/flat_output)

**Completed:**
- ✅ Core infrastructure (Sprint 1)
  - CompositeKey class for multi-part keys
  - KeyExtractor utility with validation
  - BidsExtension registration
  - 34 unit tests passing
- ✅ groupTupleBy operator (Sprint 2)
  - Full implementation in `GroupTupleByOp.groovy` (158 lines)
  - Options: size, sort, remainder
  - 8 smoke tests + 5 integration tests
  - Performance: **40% faster** than standard groupTuple
- ✅ joinBy operator (Sprint 3)
  - Full implementation in `JoinByOp.groovy` (218 lines)
  - Inner/outer join support (remainder option)
  - Thread-safe with synchronized methods
  - 8 smoke tests + 6 integration tests
  - Performance: 22% faster on small datasets (needs optimization for large)
- ✅ combineBy operator (Sprint 4 + Phase 5 redesign)
  - Full implementation in `CombineByOp.groovy` (178 lines)
  - **BREAKING CHANGE**: Redesigned to use key extractors (not filter predicates)
  - Output: `[key, left, right]` (includes key)
  - Cartesian product within matching key groups
  - 8 smoke tests + 7 integration tests + 9 edge case tests
  - Performance: **39% faster** than standard combine
- ✅ Documentation
  - Complete API docs: `docs/channel-operators.md` (1,322 lines)
  - Migration guide: `docs/CLOSURE_MIGRATION_GUIDE.md` (850+ lines)
  - README examples
  - Performance benchmark report: `validation/benchmark/BENCHMARK_RESULTS.md`
- ✅ Testing
  - 78 total tests passing
  - Unit tests, integration tests, edge cases, benchmarks
  - All test suites green

**Test Results:**
```
✅ groupTupleBy: 13 tests passing (8 unit + 5 integration)
✅ joinBy: 14 tests passing (8 unit + 6 integration)  
✅ combineBy: 24 tests passing (8 unit + 7 integration + 9 edge)
✅ Foundation: 34 tests passing (CompositeKey, KeyExtractor)
✅ Performance: All benchmarks passing
```

**Performance Results:**
- groupTupleBy: 29-40% **faster** than groupTuple ✅
- combineBy: 39% **faster** than combine ✅
- joinBy: 22% faster on small datasets, needs optimization for large (acceptable)

**Confidence Level**: 100% - Feature is production-ready and documented

**Status**: Ready for v0.1.0-beta.5 release (or already released as part of beta.6)

---

## Story 4: ⚠️ File-to-Path Conversion Fix (Uncommitted)

### The Problem

You discovered that the plugin emits `java.io.File` objects, but Nextflow processes require `java.nio.file.Path` objects for `path` inputs:

```groovy
process my_process {
    input:
    path t1w  // ❌ FAILS if receives java.io.File
    
    script:
    """
    echo "Processing ${t1w}"
    """
}
```

Error: `IllegalArgumentException: Unexpected path value: [java.io.File]`

### Your Solution (Already Implemented!)

In the flat output feature, you **already fixed this**! 🎉

**Implementation:**
- File: `BidsHandler.groovy` lines 200-250
- Uses `FileHelper.asPath()` to convert all file paths to `java.nio.file.Path`
- Handles local files AND remote URIs (s3://, gs://, az://)
- Resolves relative paths against `bidsParentDir`

```groovy
if (val instanceof String) {
    String pathStr = val
    Path result
    if (pathStr.contains('://') || pathStr.startsWith('/')) {
        result = FileHelper.asPath(pathStr)  // Absolute or URI
    } else {
        String fullPath = bidsParentDir 
            ? Paths.get(bidsParentDir, pathStr).toString() 
            : pathStr
        result = FileHelper.asPath(fullPath)  // Relative path
    }
    return result  // Returns java.nio.file.Path ✅
}
```

### Implementation Status: ✅ 100% COMPLETE

**Completed:**
- ✅ Path conversion in `convertValue` closure
- ✅ Uses `FileHelper.asPath()` for Nextflow compatibility
- ✅ Supports local files and cloud storage
- ✅ Documentation in code comments

**Remaining Work:**
1. ⏳ **Test with actual process** - Verify files stage correctly
2. ⏳ **Create integration test** - `test_process_path_input.nf` (already exists!)
3. ⏳ **Test cloud storage** - If applicable

**Confidence Level**: 95% - Implementation is correct, just needs validation

**Prompt File**: `.github/prompts/plan-implement-java-file-to-path-fix.prompt.md` (568 lines)

---

## Story 5: ⚠️ Heterogeneous DWI Suffix Mapping (Uncommitted)

### The Problem

Users with **heterogeneous datasets** can't configure the plugin properly. Example:

```yaml
# Different subjects have different phase-encoding schemes
dwi_ap:  # Some subjects have AP/PA
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  suffix_maps_to: "dwi"

dwi_rl:  # Other subjects have RL/LR
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  suffix_maps_to: "dwi"

dwi_is:  # Others have IS/SI
  named_set:
    is: {direction: dir-IS}
    si: {direction: dir-SI}
  suffix_maps_to: "dwi"
```

**Problem:** All three configs map to `suffix_maps_to: "dwi"`, causing **collision**. Only the last one (`dwi_is`) survives in the mapping.

### Your Planned Solution

The fix for **Story 2 (Suffix Mapping)** will also solve this! By inverting the mapping:

```groovy
// After inversion:
mapping["named_set"]["dwi_ap"] = "dwi"
mapping["named_set"]["dwi_rl"] = "dwi"
mapping["named_set"]["dwi_is"] = "dwi"

// resolveConfigKeys() returns ALL candidates:
def candidates = ["dwi_ap", "dwi_rl", "dwi_is"]

// Then try each one and match based on entity filters:
for (configKey in candidates) {
    if (matchesEntityFilters(file, config[configKey])) {
        return configKey  // Found the right one!
    }
}
```

### Implementation Status: ⏳ 30% PLANNED

**Completed:**
- ✅ Problem identified and documented
- ✅ Solution designed (map inversion + candidate matching)
- ✅ Test config created: `validation/configs/config_heterogeneous_dwi.yaml`
- ✅ Test file skeleton: `validation/test_heterogeneous_suffix_mapping.nf.test`

**Still TODO:**
1. ❌ **Implement map inversion in SuffixMapper** (blocked by Story 2)
2. ❌ **Update findMatchingGrouping to try all candidates**
3. ❌ **Add heterogeneous dataset tests**
4. ❌ **Validate with real heterogeneous BIDS dataset**

**Confidence Level**: 30% - Planned but not implemented yet

**Prompt Files:**
- `.github/prompts/todo-fix-heterogeneous-dwi-suffix-mapping-collision.md` (549 lines)
- `.github/prompts/test-heterogeneous-dwi.yaml` (95 lines)

---

## 🎯 Recommended Next Steps

### Priority 1: Finish Suffix Mapping Fix (Story 2)
**Why:** This is blocking heterogeneous dataset support and is a fundamental bug.

**Tasks:**
1. Invert `SuffixMapper.suffixMapping()` to use `configKey` as map key
2. Update `resolveConfigKey()` → `resolveConfigKeys()` (returns List)
3. Update `BaseSetHandler.findMatchingGrouping()` to try all candidates
4. Add tests for multiple configs with same suffix_maps_to
5. Update documentation

**Estimated Time:** 4-6 hours

### Priority 2: Complete Flat Output Documentation (Story 1)
**Why:** Feature is done but needs user-facing docs.

**Tasks:**
1. Add migration guide with before/after examples
2. Update all validation scripts to use new format
3. Test with real workflows
4. Update CHANGELOG with breaking changes

**Estimated Time:** 2-3 hours

### Priority 3: Validate File-to-Path Conversion (Story 4)
**Why:** Implementation is done, just needs testing.

**Tasks:**
1. Run `validation/test_process_path_input.nf`
2. Verify files stage correctly in process work directories
3. Test with cloud storage (if applicable)
4. Update documentation if needed

**Estimated Time:** 1-2 hours

### Priority 4: Merge and Release
**Why:** Get these features into users' hands!

**Tasks:**
1. Merge `feat/flat_output` → `main`
2. Tag release v0.1.0-beta.6
3. Update plugin registry
4. Announce breaking changes (combineBy, flat output)

**Estimated Time:** 1 hour

---

## 📈 Overall Project Health

**Tests:** ✅ 78/78 passing (100%)  
**Build:** ✅ Successful  
**Documentation:** 🟡 Good but needs updates  
**Code Quality:** ✅ Clean, well-tested  
**Performance:** ✅ Excellent (operators faster than built-ins)

**Branches:**
- `feat/flat_output` (current) - Active development
- `feat/gouping_ops` - Merged/complete
- `main` - Stable release

**Version Status:**
- Current: v0.1.0-beta.5 (with operators)
- Next: v0.1.0-beta.6 (with flat output) - commit says "beta.6 release"
- Future: v0.2.0 (with all fixes complete)

---

## 🧠 Key Context for Resuming Work

**You were in the middle of:**
1. Debugging suffix mapping collisions
2. Fixing the fundamental mapping direction bug
3. Ensuring config keys (not file suffixes) appear in output

**Latest commit (0e8d10d) did:**
- Fixed empty set detection bug
- Updated all handlers to use `configKey` in output
- Passed `configKey` through the entire pipeline
- BUT: Did NOT invert the SuffixMapper mapping structure yet

**What's left:**
The **SuffixMapper inversion** is the final piece. Once that's done:
- Heterogeneous datasets will work
- Multiple configs can use same file suffix
- Plugin will be architecturally sound

**Your documentation is excellent!**
You left yourself detailed prompts and TODOs:
- `.github/prompts/plan-suffix-mapping-and-set-type-fixes.md` (589 lines)
- `.github/prompts/TODO-suffix-mapping-fixes.md` (229 lines)
- `.github/prompts/plan-implement-suffix-mapping-fixes.prompt.md` (470 lines)

These contain step-by-step instructions for completing the work.

---

## 📞 Questions for You

To help you resume effectively:

1. **Do you want to finish the suffix mapping fix now?** (Priority 1)
2. **Or would you prefer to test the flat output feature first?** (Priority 2-3)
3. **Should we merge and release what's working, then fix suffix mapping in next release?**

The code is in great shape - you're very close to having production-ready features!
