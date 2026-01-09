# Plan: Fix Suffix Mapping Bijectivity and Plain Set Type Detection

## Executive Summary

This document outlines critical bugs in the current suffix mapping implementation and the plain set type detection logic. These issues stem from fundamental misunderstandings of the relationship between configuration keys, file suffixes, and output structure.

## Problem Statement

### Problem 1: Non-Bijective Suffix Mapping (CRITICAL)

**Issue 1.1: Mapping Direction is Backward**

The current `SuffixMapper` implementation maps **file suffixes** → **config keys**, but it should map **config keys** → **file suffixes**. This is fundamentally wrong because:

1. **File suffixes are NOT unique** - Multiple configuration entries can map to the same file suffix
   - Example: `dwi`, `dwi_fullreverse`, and `dwi_reverse_only` all map to files with suffix `dwi`
   - Example: `mask` can appear in multiple contexts (anatomical mask, functional mask, dwi mask)
   
2. **Config keys ARE unique** - Each entry in the configuration file has a unique top-level key
   - `dwi_fullreverse` is unique in the config file
   - `epi_fullreverse` is unique in the config file

**Current (WRONG) Implementation:**
```groovy
// In SuffixMapper.suffixMapping()
mapping[setType][targetSuffix] = configKey as String
// Result: Map<String, Map<String, String>> where mapping["named_set"]["dwi"] = "dwi_fullreverse"
// Problem: Only ONE config key can map to "dwi", but multiple configs use "dwi" files!
```

**Expected Behavior:**
The mapping should be: `configKey → fileSuffix`
- `"dwi_fullreverse"` → `"dwi"` (this config uses files with suffix "dwi")
- `"dwi_reverse_only"` → `"dwi"` (this config ALSO uses files with suffix "dwi")
- `"dwi"` → `"dwi"` (identity mapping - no suffix_maps_to)

**Issue 1.2: Output Key is Wrong**

When emitting data, handlers should use the **configuration key** (e.g., `dwi_fullreverse`) as the output key, NOT the file suffix (e.g., `dwi`).

**Current Behavior:**
```groovy
// In NamedSetHandler.processGroup() - LINE 123
channelData.addSuffixData(configKey, groupMap)  // CORRECT! Uses config key
```

```groovy
// In PlainSetHandler.processGroup() - LINE 94
channelData.addSuffixData(suffix, nestedDataMap)  // WRONG! Uses file suffix
```

```groovy
// In SequentialSetHandler.processGroup() - LINE 111
channelData.addSuffixData(suffix, dataMap)  // WRONG! Uses file suffix
```

```groovy
// In MixedSetHandler.processGroup() - LINE 119
channelData.addSuffixData(suffix, groupMap)  // WRONG! Uses file suffix
```

**Why This Matters:**

From the documentation example (docs/configuration.md:301-318):
```yaml
dwi_with_reverse:
  suffix_maps_to: dwi
  named_set:
    ap:
      direction: "dir-AP"
    pa:
      direction: "dir-PA"
```

**Expected Output:**
```groovy
{
  data: {
    dwi_with_reverse: {  // ← Config key name, NOT "dwi"
      ap: { nii: "...", bval: "...", bvec: "..." },
      pa: { nii: "...", bval: "...", bvec: "..." }
    }
  }
}
```

**Current (Wrong) Output:**
```groovy
{
  data: {
    dwi: {  // ← File suffix, NOT config key!
      ap: { nii: "...", bval: "...", bvec: "..." },
      pa: { nii: "...", bval: "...", bvec: "..." }
    }
  }
}
```

This breaks when you have multiple configs using the same file suffix (e.g., both `dwi` and `dwi_fullreverse` would overwrite each other).

### Problem 2: Plain Set Type Detection Returns Null (MEDIUM)

**Issue 2.1: Empty `plain_set: {}` Maps to Null**

From BaseSetHandler.groovy:25-36:
```groovy
final static String getSetType(Map suffixConfig) {
    if (suffixConfig.plain_set) {
        return "plain_set"
    } else if (suffixConfig.named_set) {
        return "named_set"
    } else if (suffixConfig.sequential_set) {
        return "sequential_set"
    } else if (suffixConfig.mixed_set) {
        return "mixed_set"
    }

    return null  // ← PROBLEM: Returns null when plain_set is empty
}
```

**The Problem:**

In YAML, `plain_set: {}` is a valid configuration meaning "use plain set with no special options". However:
- In Groovy, `suffixConfig.plain_set` evaluates to an empty Map `[:]`
- Groovy's truthiness: empty Map is **falsy** (`if ([:]){ }` evaluates to false)
- Result: The condition `if (suffixConfig.plain_set)` is false even though `plain_set` key exists!

**Example from validation/configs/config_dwi.yaml:18,30,33,36,39:**
```yaml
T1w:
  plain_set: {}  # ← This exists but evaluates to false in Groovy!

sbref:
  plain_set: {}

T2w:
  plain_set: {}
```

**Current Behavior:**
```groovy
def config = [T1w: [plain_set: [:]]]
BaseSetHandler.getSetType(config.T1w)  // Returns null! Should return "plain_set"
```

**Why This Matters:**

1. `PlainSetHandler` won't process these files (they get filtered out)
2. Files with `plain_set: {}` config are silently dropped
3. No error message - just missing data in output

**Possible Solutions:**

**Option A: Check for key existence (RECOMMENDED)**
```groovy
final static String getSetType(Map suffixConfig) {
    if (suffixConfig.containsKey('plain_set')) {  // Check key, not truthiness
        return "plain_set"
    } else if (suffixConfig.containsKey('named_set')) {
        return "named_set"
    } else if (suffixConfig.containsKey('sequential_set')) {
        return "sequential_set"
    } else if (suffixConfig.containsKey('mixed_set')) {
        return "mixed_set"
    }
    return null
}
```

**Option B: Default to plain_set (RISKY)**
```groovy
final static String getSetType(Map suffixConfig) {
    if (suffixConfig.named_set) {
        return "named_set"
    } else if (suffixConfig.sequential_set) {
        return "sequential_set"
    } else if (suffixConfig.mixed_set) {
        return "mixed_set"
    }
    return "plain_set"  // Default to plain when no other type specified
}
```

**Risk with Option B:** If someone forgets to specify a set type, it defaults to plain_set and might produce unexpected output instead of failing with a clear error. Option A is safer.

## Investigation Results

### Code Flow Analysis

1. **Configuration Loading** (`BidsConfigLoader.groovy`)
   - Loads YAML → Map
   - `plain_set: {}` becomes `[plain_set: [:]]`
   - Empty map `[:]` is falsy in Groovy

2. **Suffix Mapping Construction** (`SuffixMapper.suffixMapping()`)
   - Iterates over all config entries
   - For each entry with `suffix_maps_to`, creates mapping
   - **Current (wrong):** `mapping[setType][fileSuffix] = configKey`
   - **Should be:** A reverse lookup is needed, or store both directions

3. **File Processing** (`BaseSetHandler.process()`)
   - For each file, calls `findMatchingGrouping()`
   - Uses `SuffixMapper.resolveConfigKey()` to find config
   - **Current:** Resolves file suffix → config key (wrong direction)
   - **Should:** Store file suffix in index, use config key for output

4. **Group Processing** (Handler-specific `processGroup()`)
   - Receives `suffix` from file (e.g., "dwi")
   - Calls `resolveConfigKey()` to get config key (e.g., "dwi_fullreverse")
   - **PlainSetHandler (WRONG):** Uses `suffix` for `addSuffixData()`
   - **NamedSetHandler (CORRECT):** Uses `configKey` for `addSuffixData()`
   - **SequentialSetHandler (WRONG):** Uses `suffix` for `addSuffixData()`
   - **MixedSetHandler (WRONG):** Uses `suffix` for `addSuffixData()`

5. **Set Type Detection** (`BaseSetHandler.getSetType()`)
   - Uses Groovy truthiness on map values
   - Empty maps are falsy → `plain_set: {}` returns null
   - Causes files to be filtered out in `findMatchingGrouping()`

### Affected Files

**Critical (Must Fix):**
1. `src/main/groovy/nfneuro/util/SuffixMapper.groovy` - Mapping direction
2. `src/main/groovy/nfneuro/grouping/PlainSetHandler.groovy` - Output key (line 94)
3. `src/main/groovy/nfneuro/grouping/SequentialSetHandler.groovy` - Output key (line 111)
4. `src/main/groovy/nfneuro/grouping/MixedSetHandler.groovy` - Output key (line 119)
5. `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy` - Set type detection (line 25-36)

**Supporting (May Need Updates):**
6. `src/test/groovy/nfneuro/grouping/PlainSetHandlerSpec.groovy` - Test expectations
7. `src/test/groovy/nfneuro/grouping/SequentialSetHandlerSpec.groovy` - Test expectations
8. `src/test/groovy/nfneuro/grouping/MixedSetHandlerSpec.groovy` - Test expectations
9. `validation/configs/*.yaml` - May need test cases for suffix_maps_to with plain_set

**Documentation:**
10. `docs/configuration.md` - Clarify output structure for suffix_maps_to
11. `docs/examples.md` - Add examples showing config key vs file suffix

### Test Coverage Gaps

1. **No tests for `suffix_maps_to` with `plain_set`**
   - Only tested with `named_set` currently
   - Need to verify: `dwi_plain: { suffix_maps_to: "dwi", plain_set: {} }`

2. **No tests for `plain_set: {}`** (empty configuration)
   - Current tests probably use `plain_set: { additional_extensions: [...] }`
   - Need explicit test with truly empty `{}`

3. **No tests for multiple configs mapping to same suffix**
   - Example: Both `dwi` and `dwi_fullreverse` using files with suffix "dwi"
   - Should verify they don't overwrite each other

4. **No tests for `SuffixMapper.getOutputSuffix()`**
   - This method exists but is never called in the codebase
   - Either remove it or use it properly

## Implementation Plan

### Phase 1: Investigation & Understanding (COMPLETE)

✅ **Task 1.1:** Read and understand suffix mapping code flow
✅ **Task 1.2:** Read and understand set type detection logic
✅ **Task 1.3:** Identify all locations where suffix vs config key is used
✅ **Task 1.4:** Review documentation for expected behavior
✅ **Task 1.5:** Check test coverage for suffix_maps_to
✅ **Task 1.6:** Create comprehensive investigation document

### Phase 2: Fix Suffix Mapping Bijectivity

**Task 2.1: Clarify Mapping Purpose**
- **File:** `src/main/groovy/nfneuro/util/SuffixMapper.groovy`
- **Action:** Add comprehensive documentation explaining:
  - Config keys are unique (bijective domain)
  - File suffixes are not unique (non-bijective codomain)
  - Purpose: Map config keys to file suffixes for lookup
  - Direction: configKey → fileSuffix (one-to-many is OK)
  - Reverse lookup: fileSuffix → [configKey1, configKey2, ...] (if needed)

**Task 2.2: Decide on Mapping Strategy**

**Option A: Keep current direction, fix usage**
- Keep `mapping[setType][fileSuffix] = configKey`
- Accept that only ONE config per (setType, fileSuffix) pair
- Document this as a limitation
- Add validation error if multiple configs try to use same (setType, fileSuffix)

**Option B: Reverse the mapping (RECOMMENDED)**
- Change to `mapping[setType][configKey] = fileSuffix`
- Update `resolveConfigKey()` to `resolveFileSuffix()`
- In file matching: Store configKey in index, look up fileSuffix from config
- This allows multiple configs to use same file suffix

**Option C: Bidirectional mapping**
- Store both directions: `[forward: [configKey → fileSuffix], reverse: [fileSuffix → [configKeys]]]`
- More complex but most flexible
- Allows checking for conflicts

**Recommendation: Option B** - It's cleaner and matches the semantic relationship

**Task 2.3: Update SuffixMapper Implementation**
- If Option B: Reverse mapping direction in `suffixMapping()`
- Update method name from `resolveConfigKey()` to `resolveFileSuffix()`
- Update documentation and parameter names
- Update logging messages

**Task 2.4: Update BaseSetHandler.findMatchingGrouping()**
- **Current:** Uses file suffix to look up config key
- **New:** Use config key directly when known, or iterate through configs
- **Key insight:** We need to check ALL configs that might match this file's suffix
- May need to change approach: Instead of mapping, iterate configs and check `suffix_maps_to`

**Task 2.5: Update BaseSetHandler.getSetIndex()**
- Ensure index includes both file suffix AND config key
- Example: `[fileSuffix: 'dwi', configKey: 'dwi_fullreverse', group: 'ap']`
- This allows proper tracking through the pipeline

**Task 2.6: Fix Output Keys in All Handlers**

Update these specific lines:

**PlainSetHandler.groovy:94**
```groovy
// BEFORE:
channelData.addSuffixData(suffix, nestedDataMap)

// AFTER:
channelData.addSuffixData(configKey, nestedDataMap)
```

**SequentialSetHandler.groovy:111**
```groovy
// BEFORE:
channelData.addSuffixData(suffix, dataMap)

// AFTER:
channelData.addSuffixData(configKey, dataMap)
```

**MixedSetHandler.groovy:119**
```groovy
// BEFORE:
channelData.addSuffixData(suffix, groupMap)

// AFTER:
channelData.addSuffixData(configKey, groupMap)
```

**Task 2.7: Pass Config Key Through Pipeline**
- Review `packFileIntoSet()` in each handler
- Ensure config key is preserved alongside file suffix
- May need to update `index` structure in all handlers

**Task 2.8: Update Variable Names for Clarity**
- Rename variables to be clear about config key vs file suffix
- Example: `suffix` → `fileSuffix`, add `configKey` variable
- Update comments to clarify distinction

### Phase 3: Fix Plain Set Type Detection

**Task 3.1: Fix BaseSetHandler.getSetType()**
- **File:** `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy`
- **Lines:** 25-36
- **Change:** Use `containsKey()` instead of truthiness check
- **Implementation:**
```groovy
final static String getSetType(Map suffixConfig) {
    if (suffixConfig.containsKey('plain_set')) {
        return "plain_set"
    } else if (suffixConfig.containsKey('named_set')) {
        return "named_set"
    } else if (suffixConfig.containsKey('sequential_set')) {
        return "sequential_set"
    } else if (suffixConfig.containsKey('mixed_set')) {
        return "mixed_set"
    }
    return null  // Only null if NO set type specified
}
```

**Task 3.2: Add Validation for Missing Set Type**
- **File:** `src/main/groovy/nfneuro/config/BidsConfigValidator.groovy`
- **Location:** Around line 75-85 (in validateSuffixConfig)
- **Current:** Checks if set type exists, warns if multiple
- **Verify:** Error message is clear when set type is missing
- **Already present:** Looks good, validation exists

**Task 3.3: Handle Null Set Type in findMatchingGrouping**
- **File:** `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy`
- **Location:** Line ~350 in `findMatchingGrouping()`
- **Current:** Returns null if `getSetConfig()` returns null
- **Add:** Better error message showing which suffix has no valid set config
- **Implementation:**
```groovy
Map setConfig = getSetConfig(suffixConfig as Map)
if (setConfig == null) {
    BidsLogger.logProgress(logGroup(), 
        "No ${setName()} configuration for suffix: ${suffix}. Config has set types: ${suffixConfig.keySet()} - FILTERED")
    return
}
```

### Phase 4: Testing & Validation

**Task 4.1: Unit Tests for SuffixMapper**
- Create `SuffixMapperSpec.groovy` if it doesn't exist
- Test cases:
  - Single config with `suffix_maps_to`
  - Multiple configs mapping to same suffix (if supported)
  - Config without `suffix_maps_to` (identity mapping)
  - Empty configuration
  - Null configuration

**Task 4.2: Unit Tests for getSetType()**
- Create test in `BaseSetHandlerSpec.groovy`
- Test cases:
  - `plain_set: {}` → should return "plain_set"
  - `plain_set: { parts: ["mag", "phase"] }` → should return "plain_set"
  - `named_set: { ... }` → should return "named_set"
  - No set type → should return null
  - Multiple set types → should return first one found (document order)

**Task 4.3: Integration Tests for suffix_maps_to with plain_set**
- Create test config: `dwi_simple: { suffix_maps_to: "dwi", plain_set: {} }`
- Verify output has `dwi_simple` key, not `dwi` key
- Test with multiple configs using same file suffix

**Task 4.4: Integration Tests for plain_set: {}**
- Use configs from `validation/configs/config_dwi.yaml`
- Verify T1w, sbref, T2w, etc. are processed correctly
- Should not be filtered out

**Task 4.5: Regression Tests**
- Run all existing tests to ensure no breakage
- Focus on tests using:
  - NamedSetHandler (should still work - it was correct)
  - PlainSetHandler (may need updates)
  - SequentialSetHandler (may need updates)
  - MixedSetHandler (may need updates)

**Task 4.6: Manual Testing with Real Data**
- Test with validation datasets:
  - `validation/data/bids-examples/asl001` (has plain sets)
  - Any dataset with multiple configs using same suffix
- Verify output structure matches documentation
- Check flattened output format

### Phase 5: Documentation & Cleanup

**Task 5.1: Update docs/configuration.md**
- Clarify that output key is config key, not file suffix
- Add clear examples showing difference:
  - `dwi: { plain_set: {} }` → output has `dwi` key
  - `dwi_reverse: { suffix_maps_to: "dwi", named_set: {...} }` → output has `dwi_reverse` key
- Explain when to use `suffix_maps_to`
- Document limitation if multiple configs can't use same suffix

**Task 5.2: Update docs/examples.md**
- Add example showing multiple configs for same file type
- Show how `suffix_maps_to` affects output structure
- Migration guide from incorrect assumptions

**Task 5.3: Add Code Comments**
- Document the config key vs file suffix distinction in all handlers
- Add comments explaining the mapping direction
- Document why we use configKey for output

**Task 5.4: Update CHANGELOG.md**
- Add breaking change notice for suffix_maps_to behavior
- Explain output structure change
- Provide migration guide

**Task 5.5: Remove Unused Code**
- Investigate `SuffixMapper.getOutputSuffix()` - is it needed?
- If not used, remove it
- If needed, document where and why

## Edge Cases & Considerations

### Edge Case 1: Multiple Configs, Same Suffix, Same Set Type
**Scenario:**
```yaml
dwi:
  plain_set: {}

dwi_alternative:
  suffix_maps_to: "dwi"
  plain_set: { additional_extensions: ["bval", "bvec"] }
```

**Question:** Should this be allowed?

**Current Behavior:** Only one will be processed (whichever is mapped)

**Recommendation:** 
- Either: Allow it, emit both `dwi` and `dwi_alternative` in output
- Or: Validation error - conflicting configs for same (suffix, setType)

### Edge Case 2: Cross-Set-Type Suffix Reuse
**Scenario:**
```yaml
dwi:
  plain_set: {}

dwi_fullreverse:
  suffix_maps_to: "dwi"
  named_set:
    ap: { direction: "dir-AP" }
    pa: { direction: "dir-PA" }
```

**Question:** Can same file suffix be used in different set types?

**Answer:** YES - this should work because:
- PlainSetHandler processes files differently than NamedSetHandler
- Files with dir-AP/PA will match named_set
- Files without direction will match plain_set
- Different set types have different filtering logic

**Current Support:** Should work if output keys are correct

### Edge Case 3: Nested suffix_maps_to
**Scenario:**
```yaml
dwi:
  plain_set: {}

dwi_alias:
  suffix_maps_to: "dwi"
  plain_set: {}

dwi_alias_alias:
  suffix_maps_to: "dwi_alias"  # Maps to alias, not original
  plain_set: {}
```

**Question:** Should this chain?

**Answer:** NO - Keep it simple
- `suffix_maps_to` should always refer to actual file suffix
- No transitive resolution
- Add validation to detect and error on this

## Success Criteria

1. ✅ All handlers use config key (not file suffix) for output
2. ✅ `plain_set: {}` is correctly detected and processed
3. ✅ Multiple configs can use same file suffix with different set types
4. ✅ Output structure matches documentation examples
5. ✅ All unit tests pass
6. ✅ All integration tests pass
7. ✅ Manual testing with validation datasets succeeds
8. ✅ Documentation clearly explains config key vs file suffix
9. ✅ CHANGELOG documents breaking changes
10. ✅ No regression in existing functionality

## Timeline Estimate

- **Phase 1:** Complete (investigation & planning)
- **Phase 2:** 4-6 hours (suffix mapping fixes)
- **Phase 3:** 1-2 hours (set type detection)
- **Phase 4:** 3-4 hours (testing)
- **Phase 5:** 2-3 hours (documentation)
- **Total:** ~10-15 hours of focused development

## References

### Key Files
- `src/main/groovy/nfneuro/util/SuffixMapper.groovy` - Mapping implementation
- `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy` - Set type detection
- `src/main/groovy/nfneuro/grouping/PlainSetHandler.groovy` - Plain set processing
- `src/main/groovy/nfneuro/grouping/NamedSetHandler.groovy` - Named set processing (correct implementation)
- `src/main/groovy/nfneuro/grouping/SequentialSetHandler.groovy` - Sequential set processing
- `src/main/groovy/nfneuro/grouping/MixedSetHandler.groovy` - Mixed set processing
- `docs/configuration.md` - Configuration documentation
- `validation/configs/config_dwi.yaml` - Example configurations

### Key Concepts
- **Config Key:** Unique top-level key in YAML configuration (e.g., "dwi_fullreverse")
- **File Suffix:** BIDS suffix from filename (e.g., "dwi" from "sub-01_dwi.nii.gz")
- **Set Type:** Type of grouping (plain_set, named_set, sequential_set, mixed_set)
- **suffix_maps_to:** Configuration directive mapping config key to file suffix
- **Output Key:** Key used in channel data structure (should be config key)

### Groovy Gotchas
- Empty map `[:]` is falsy in boolean context
- Use `containsKey()` to check for presence regardless of value
- `map.key` returns null if key doesn't exist OR if value is null
