```prompt
# Implementation Prompt: Fix Suffix Mapping and Set Type Detection

## Context

You are implementing fixes for two critical bugs in the nf-bids plugin:

1. **Suffix mapping direction is backward** - causing config keys and file suffixes to be confused
2. **Plain set type detection fails for empty configs** - causing files to be silently filtered

## Your Mission

Implement all fixes following the plan in `plan-suffix-mapping-and-set-type-fixes.md` and checklist in `TODO-suffix-mapping-fixes.md`.

## Critical Understanding

### The Core Problem: Config Key ≠ File Suffix

**Config Key** (unique, defined in YAML):
- Example: `dwi_fullreverse`
- This is the top-level key in the configuration file
- Each config key is unique
- This is what should appear in the OUTPUT

**File Suffix** (non-unique, extracted from filename):
- Example: `dwi` (from `sub-01_dwi.nii.gz`)
- Multiple configs can process files with the same suffix
- This is what files are matched by in the INPUT

**The Relationship:**
```
CONFIG FILE                  BIDS FILES
dwi: { plain_set: {} }       sub-01_dwi.nii.gz
    ↓                             ↓
    Uses suffix "dwi"        Has suffix "dwi"
    Output key: "dwi"             ↓
                                  Matches!

dwi_fullreverse:             sub-01_dir-AP_dwi.nii.gz
  suffix_maps_to: "dwi"          ↓
  named_set: { ap: ... }    Has suffix "dwi"
    ↓                             ↓
    Uses suffix "dwi"             Matches!
    Output key: "dwi_fullreverse" (NOT "dwi"!)
```

### What's Wrong Now

1. **SuffixMapper** creates backwards mapping:
   - Current: `mapping["named_set"]["dwi"] = "dwi_fullreverse"` (suffix → config)
   - Problem: Only ONE config can map to each suffix
   - Should be: Track which config uses which suffix differently

2. **Three handlers use wrong output key**:
   - ❌ PlainSetHandler: `channelData.addSuffixData(suffix, ...)` uses file suffix
   - ❌ SequentialSetHandler: `channelData.addSuffixData(suffix, ...)` uses file suffix  
   - ❌ MixedSetHandler: `channelData.addSuffixData(suffix, ...)` uses file suffix
   - ✅ NamedSetHandler: `channelData.addSuffixData(configKey, ...)` uses config key (CORRECT!)

3. **getSetType() fails for empty maps**:
   - `plain_set: {}` becomes `[plain_set: [:]]` in Groovy
   - `if (suffixConfig.plain_set)` is false because empty map is falsy
   - Should use: `if (suffixConfig.containsKey('plain_set'))`

## Implementation Instructions

### Phase 1: Fix getSetType() First (Easiest)

**File:** `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy`
**Lines:** 25-36

Change from truthiness check to key existence check:

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
    return null
}
```

**Test immediately** with validation configs that have `plain_set: {}`.

### Phase 2: Understand Current Data Flow

Before fixing suffix mapping, trace how data flows:

1. **File matching** (`BaseSetHandler.findMatchingGrouping()`):
   - Input: BidsFile with suffix "dwi"
   - Uses: `SuffixMapper.resolveConfigKey(setName, "dwi", suffixMapping)`
   - Returns: Config key like "dwi_fullreverse"
   - Problem: This assumes mapping exists suffix → config

2. **Packing** (`packFileIntoSet()`):
   - Creates index with file suffix: `[suffix: "dwi"]`
   - Stores in `sets[suffix]` using file suffix as key
   - Problem: Config key is lost here

3. **Processing** (`processGroup()`):
   - Receives `sets` with file suffix keys
   - Resolves back to config key using `SuffixMapper.resolveConfigKey()`
   - Some handlers use config key (✓), some use file suffix (✗)

### Phase 3: Fix Suffix Mapping Strategy

**Recommended Approach:** Keep current mapping direction but fix usage

**Why:** Changing the mapping direction requires more extensive refactoring. Instead:
1. Keep `mapping[setType][fileSuffix] = configKey` 
2. Store BOTH file suffix AND config key in the index during packing
3. Use config key for output in all handlers

**Changes needed:**

#### 3.1: Update index structure in getSetIndex()

**Each handler's getSetIndex() should return:**
```groovy
[fileSuffix: file.suffix, configKey: configKey]
```

But wait - handlers don't have access to configKey in getSetIndex()!

**Solution:** Pass configKey to getSetIndex() by updating signature:
```groovy
protected abstract Map getSetIndex(BidsFile file, Map setConfig, String configKey)
```

#### 3.2: Update BaseSetHandler.process() to pass configKey

Around line 60-75 where getSetIndex is called:
```groovy
Map index = getSetIndex(file, setConfig, configKey)
```

The configKey should already be available from findMatchingGrouping() - ensure it's stored!

#### 3.3: Update packFileIntoSet() to use both keys

**Signature change:**
```groovy
protected abstract void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering)
```

Index now has: `[fileSuffix: "dwi", configKey: "dwi_fullreverse", ...]`

**Update each handler's packFileIntoSet():**
- Store using **configKey** as the map key (not fileSuffix)
- Keep allFiles using fileSuffix (for finding related files)

### Phase 4: Fix Output Keys in Handlers

Once configKey is in the data structures, update processGroup():

#### 4.1: PlainSetHandler.groovy

**Line 94:** Change from:
```groovy
channelData.addSuffixData(suffix, nestedDataMap)
```

To:
```groovy
channelData.addSuffixData(configKey, nestedDataMap)
```

**Where to get configKey:** The `plainSets` map should now be keyed by configKey (from Phase 3)

Update the loop:
```groovy
plainSets.each { configKey, setData ->  // Was: suffix, setData
    // ...existing code...
    // When resolving config, configKey is already known
    channelData.addSuffixData(configKey, nestedDataMap)
}
```

#### 4.2: SequentialSetHandler.groovy

**Line 111:** Change from:
```groovy
channelData.addSuffixData(suffix, dataMap)
```

To:
```groovy
channelData.addSuffixData(configKey, dataMap)
```

Update the loop:
```groovy
sets.each { configKey, setData ->  // Was: suffix, setData
    // Get file suffix from setData if needed for allFiles lookup
    def fileSuffix = setData.fileSuffix ?: configKey  // Fallback for non-mapped
    
    // ...existing code using fileSuffix for allFiles...
    
    channelData.addSuffixData(configKey, dataMap)
}
```

#### 4.3: MixedSetHandler.groovy

**Line 119:** Change from:
```groovy
channelData.addSuffixData(suffix, groupMap)
```

To:
```groovy
channelData.addSuffixData(configKey, groupMap)
```

Update similarly to above.

#### 4.4: Verify NamedSetHandler.groovy

**Line 123:** Already correct!
```groovy
channelData.addSuffixData(configKey, groupMap)  // ✓
```

This is the reference implementation. Check how it gets configKey and replicate.

### Phase 5: Update packFileIntoSet() Implementation

Each handler needs updates:

#### PlainSetHandler.packFileIntoSet()
```groovy
@Override
protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
    String configKey = index.configKey  // Get from index
    String fileSuffix = index.fileSuffix
    
    if (file.isPrimaryFile()) {
        sets[configKey] = [files: [file: file], fileSuffix: fileSuffix]  // Store both
    } else {
        if (!allFiles.containsKey(fileSuffix)) {  // allFiles still uses file suffix
            allFiles[fileSuffix] = []
        }
        allFiles[fileSuffix] << file
    }
}
```

#### SequentialSetHandler.packFileIntoSet()
```groovy
@Override
protected void packFileIntoSet(Map sets, Map allFiles, Map index, BidsFile file, Map ordering) {
    String configKey = index.configKey
    String fileSuffix = index.fileSuffix
    
    if (!sets.containsKey(configKey)) {
        sets[configKey] = [
            files: [],
            entities: ordering.entities,
            order: ordering.order,
            fileSuffix: fileSuffix  // Store for later lookup
        ]
    }
    // ...rest of implementation
}
```

#### MixedSetHandler.packFileIntoSet()
Similar pattern - use configKey for sets, store fileSuffix for allFiles.

#### NamedSetHandler.packFileIntoSet()
Already uses configKey correctly - verify and ensure consistency.

### Phase 6: Update getSetIndex() Signatures

Add configKey parameter to all handlers:

```groovy
// BaseSetHandler.groovy
protected abstract Map getSetIndex(BidsFile file, Map setConfig, String configKey)

// PlainSetHandler.groovy
@Override
protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
    return [fileSuffix: file.suffix, configKey: configKey]
}

// NamedSetHandler.groovy
@Override
protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
    def groupName = findMatchingGroupName(file, setConfig)
    return [fileSuffix: file.suffix, configKey: configKey, group: groupName]
}

// SequentialSetHandler.groovy
@Override
protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
    return [fileSuffix: file.suffix, configKey: configKey]
}

// MixedSetHandler.groovy
@Override
protected Map getSetIndex(BidsFile file, Map setConfig, String configKey) {
    def groupName = findMatchingMixedGroupName(file, setConfig)
    return [fileSuffix: file.suffix, configKey: configKey, group: groupName]
}
```

### Phase 7: Update BaseSetHandler.process() to Pass configKey

In the process() method where getSetIndex is called, pass the configKey:

```groovy
Map setConfig = findMatchingGrouping(file, config, suffixMapping)
if (setConfig == null) {
    return
}

String suffix = file.suffix
String configKey = SuffixMapper.resolveConfigKey(setName(), suffix, suffixMapping)

// ...

Map index = getSetIndex(file, setConfig, configKey)  // Pass configKey here
```

### Phase 8: Testing Strategy

After each phase, test incrementally:

1. **After Phase 1 (getSetType):**
   ```bash
   ./gradlew test --tests "*BaseSetHandler*"
   ```
   Test with config: `T1w: { plain_set: {} }`

2. **After Phase 3-7 (suffix mapping):**
   ```bash
   ./gradlew test
   ```
   All tests should pass

3. **Integration test:**
   ```bash
   cd validation
   nextflow run test_flattened_output.nf \
     --bids_dir "$PWD/data/bids-examples/asl001" \
     --config "$PWD/configs/config_asl.yaml"
   ```

4. **Test with suffix_maps_to:**
   Create test config:
   ```yaml
   dwi_test:
     suffix_maps_to: "dwi"
     plain_set: {}
   ```
   Verify output has `dwi_test` key, not `dwi` key.

### Phase 9: Documentation Updates

1. **Update docs/configuration.md** - lines 295-325:
   - Clarify that output uses config key
   - Show example: `dwi_with_reverse` appears in output, not `dwi`

2. **Add code comments:**
   - Document config key vs file suffix distinction
   - Explain why we store both in index
   - Comment the output key usage

3. **Update CHANGELOG.md:**
   - Breaking change: suffix_maps_to now works correctly
   - Output structure change for configs with suffix_maps_to
   - Migration: Check your output access patterns

## Common Pitfalls to Avoid

1. **Don't confuse suffix and configKey** - Use clear variable names
2. **Remember allFiles uses fileSuffix** - It's for finding related files by actual suffix
3. **Test with empty plain_set first** - It's the easiest fix to validate
4. **Look at NamedSetHandler** - It's already correct, use as reference
5. **Update abstract method signatures** - All handlers must match

## Success Indicators

✅ `plain_set: {}` files are processed (not filtered)
✅ Output uses config keys: `dwi_fullreverse: {...}` not `dwi: {...}`
✅ Multiple configs can use same file suffix without conflict
✅ All existing tests pass
✅ New tests validate suffix_maps_to behavior
✅ Documentation matches actual behavior

## Implementation Order

1. ✅ Fix getSetType() - quick win
2. ✅ Update getSetIndex() signatures - add configKey parameter
3. ✅ Update BaseSetHandler.process() - pass configKey to getSetIndex()
4. ✅ Update packFileIntoSet() in all handlers - use configKey for sets
5. ✅ Update processGroup() in all handlers - use configKey for output
6. ✅ Test each handler individually
7. ✅ Run full test suite
8. ✅ Manual testing with validation datasets
9. ✅ Update documentation
10. ✅ Update CHANGELOG

## Key Files Reference

**Must modify:**
- `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy` (getSetType, process)
- `src/main/groovy/nfneuro/grouping/PlainSetHandler.groovy` (getSetIndex, packFileIntoSet, processGroup)
- `src/main/groovy/nfneuro/grouping/SequentialSetHandler.groovy` (getSetIndex, packFileIntoSet, processGroup)
- `src/main/groovy/nfneuro/grouping/MixedSetHandler.groovy` (getSetIndex, packFileIntoSet, processGroup)
- `src/main/groovy/nfneuro/grouping/NamedSetHandler.groovy` (verify getSetIndex signature)

**Reference for correct implementation:**
- `src/main/groovy/nfneuro/grouping/NamedSetHandler.groovy:123` (uses configKey correctly)

**Test with:**
- `validation/configs/config_dwi.yaml` (has plain_set: {} and suffix_maps_to examples)
- `validation/configs/config_asl.yaml` (has plain_set: {})

## Build & Install Reminder

**CRITICAL:** After ANY code changes:
```bash
cd /home/local/USHERBROOKE/vala2004/dev/nf-bids
make install
```

Nextflow loads plugins from `~/.nextflow/plugins/` - must reinstall to test changes!

## Expected Output Examples

**Before Fix (wrong):**
```groovy
// Config: dwi_fullreverse: { suffix_maps_to: "dwi", named_set: {...} }
data: {
  dwi: {  // ← Wrong! File suffix used
    ap: {...},
    pa: {...}
  }
}
```

**After Fix (correct):**
```groovy
// Config: dwi_fullreverse: { suffix_maps_to: "dwi", named_set: {...} }
data: {
  dwi_fullreverse: {  // ← Correct! Config key used
    ap: {...},
    pa: {...}
  }
}
```

## Ready to Implement!

Start with Phase 1 (getSetType fix) - it's independent and easy to verify.
Then proceed through phases sequentially, testing after each phase.

Use the TODO checklist (`TODO-suffix-mapping-fixes.md`) to track progress.
Refer to the detailed plan (`plan-suffix-mapping-and-set-type-fixes.md`) for context.

Good luck! 🚀
```