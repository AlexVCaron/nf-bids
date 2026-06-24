# TODO: Fix Heterogeneous DWI Suffix Mapping Collision

## ✅ FIX IMPLEMENTED (Latest Update)

**Date:** 2024-12-06  
**Status:** ✅ COLLISION BUG FIXED - Inverted map structure solution implemented

**Solution Chosen:** Map Inversion (Better than list-based approach)
- **Key insight:** configKey is unique, targetSuffix is not
- **Old structure:** `Map<setType, Map<targetSuffix, configKey>>` → collision
- **New structure:** `Map<setType, Map<configKey, targetSuffix>>` → no collision!

**Implementation Summary:**
- ✅ Inverted `SuffixMapper.suffixMapping()` to use configKey as key
- ✅ Updated `resolveConfigKeys()` to return List<String> by searching values
- ✅ Added `getTargetSuffix()` helper for reverse lookups
- ✅ Updated `BaseSetHandler.findMatchingGrouping()` to try all candidates
- ✅ Returns both `configKey` and `setConfig` from matching logic
- ✅ All unit tests pass (77 tests)
- ✅ All integration tests pass (nf-test)

**Technical Changes:**
```groovy
// Before: mapping[setType][targetSuffix] = configKey
// After:  mapping[setType][configKey] = targetSuffix

// Before: resolveConfigKey() returns single String
// After:  resolveConfigKeys() returns List<String>

// Before: findMatchingGrouping() returns setConfig only
// After:  findMatchingGrouping() returns [configKey: ..., setConfig: ...]
```

**Files Modified:**
1. `src/main/groovy/nfneuro/util/SuffixMapper.groovy`
2. `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy`

**Testing:**
- ✅ Compiles without errors
- ✅ Unit tests pass
- ✅ Integration tests pass (named_set comparison test)
- ⏳ Needs validation with actual heterogeneous DWI dataset

---

## Problem Summary

The nf-bids plugin fails to correctly handle heterogeneous DWI datasets where different subjects have different phase-encoding directions (AP/PA, RL/LR, IS/SI). Multiple named sets with `suffix_maps_to: "dwi"` are colliding, causing only the last one (`dwi_is`) to be used.

## Problems Identified from Log Analysis

### 🔴 CRITICAL: Suffix Mapping Collision

**Log Evidence:**
```
Dec-05 01:57:08.480 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: dwi -> dwi_ap
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: dwi -> dwi_rl
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: dwi -> dwi_is
Dec-05 01:57:08.483 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Built suffix mappings: [plain_set:[T1w:T1w, mask:aparc_aseg], named_set:[dwi:dwi_is, sbref:sbref_is]]
```

**Problem:** All three DWI named sets (`dwi_ap`, `dwi_rl`, `dwi_is`) map to the same suffix `"dwi"`, but only `dwi_is` is retained in the final suffix mapping. This is a **Map key collision** - later entries overwrite earlier ones.

**Impact:**
- Subjects with AP/PA encoding are ignored
- Subjects with RL/LR encoding are ignored  
- Only subjects with IS/SI encoding are processed

---

### 🟡 PROBLEM: Incorrect Configuration Pattern for Heterogeneous Data

**Current Configuration:**
```yaml
dwi_ap:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: ["ap", "pa"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"  # ❌ COLLISION

dwi_rl:
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  required: ["rl", "lr"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"  # ❌ COLLISION

dwi_is:
  named_set:
    is: {direction: dir-IS}
    si: {direction: dir-SI}
  required: ["is", "si"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"  # ❌ COLLISION
```

**Problem:** The configuration assumes all subjects have the same phase-encoding scheme, but the dataset is heterogeneous.

---

### 🟡 PROBLEM: Same Issue with sbref

**Log Evidence:**
```
[suffix-mapping] Suffix mapping for named_set: sbref -> sbref_ap
[suffix-mapping] Suffix mapping for named_set: sbref -> sbref_rl
[suffix-mapping] Suffix mapping for named_set: sbref -> sbref_is
Built suffix mappings: [..., named_set:[..., sbref:sbref_is]]
```

Same collision problem exists for `sbref_ap`, `sbref_rl`, `sbref_is`.

---

### 🟡 PROBLEM: Multiple Mask Types Collision

**Log Evidence:**
```
[suffix-mapping] Suffix mapping for plain_set: mask -> lesion
[suffix-mapping] Suffix mapping for plain_set: mask -> wmparc
[suffix-mapping] Suffix mapping for plain_set: mask -> aparc_aseg
Built suffix mappings: [plain_set:[T1w:T1w, mask:aparc_aseg], ...]
```

Three different masks (`lesion`, `wmparc`, `aparc_aseg`) all map to `"mask"`, but only `aparc_aseg` survives. The other two are lost.

---

## Root Cause Analysis

### Technical Root Cause

The plugin builds suffix mappings using a `Map` data structure where:
- **Key** = The suffix (e.g., `"dwi"`, `"mask"`, `"sbref"`)
- **Value** = The configuration key name (e.g., `"dwi_is"`, `"aparc_aseg"`)

When multiple configurations use `suffix_maps_to` with the same suffix, they overwrite each other in the Map:

```groovy
// Pseudocode of what's happening
suffixMap = [:]
suffixMap['dwi'] = 'dwi_ap'  // First registration
suffixMap['dwi'] = 'dwi_rl'  // Overwrites dwi_ap
suffixMap['dwi'] = 'dwi_is'  // Overwrites dwi_rl
// Result: only dwi_is survives
```

### Design Flaw

The plugin's suffix mapping system was designed for homogeneous datasets where:
- Each modality has one configuration
- Each subject has the same data structure

It **cannot handle**:
- Heterogeneous phase-encoding schemes across subjects
- Multiple mutually-exclusive variants of the same modality
- Multiple anatomical masks that should coexist

---

## TODO Tasks to Fix

### Task 1: ✅ CODE INVESTIGATION COMPLETE

**Status:** CONFIRMED - No recent fixes exist, collision issue is present in current code

**Files investigated:**
- `src/main/groovy/nfneuro/util/SuffixMapper.groovy` - Core suffix mapping logic
- `src/main/groovy/nfneuro/channel/BidsHandler.groovy` - Uses SuffixMapper
- `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy` - Config resolution

**Current Implementation (SuffixMapper.groovy, line 42-43):**
```groovy
mapping[setType][targetSuffix] = configKey as String
```

**Data Structure:**
```groovy
Map<String, Map<String, String>> suffixMapping
// Outer key: setType (e.g., "plain_set", "named_set")
// Inner key: targetSuffix (e.g., "dwi", "mask", "sbref")
// Inner value: configKey (e.g., "dwi_is", "aparc_aseg")
```

**CONFIRMED COLLISION:** When multiple configs map to same suffix within same set type:
```groovy
// What happens with dwi_ap, dwi_rl, dwi_is:
mapping['named_set']['dwi'] = 'dwi_ap'  // First
mapping['named_set']['dwi'] = 'dwi_rl'  // Overwrites
mapping['named_set']['dwi'] = 'dwi_is'  // Final value (only survivor)
```

**Resolution Logic (SuffixMapper.resolveConfigKey, line 62-75):**
```groovy
static String resolveConfigKey(String setType, String suffix, Map<String, Map<String, String>> mapping) {
    if (mapping[setType].containsKey(suffix)) {
        String mappedKey = mapping[setType][suffix]  // Returns single String
        return mappedKey
    }
    return suffix
}
```

**CONCLUSION:**
- ❌ No list-based mapping exists
- ❌ No priority system exists
- ❌ No conflict detection exists
- ❌ Recent fixes have NOT addressed this issue
- ✅ Root cause confirmed: Simple string value in nested map causes collision
- 🎯 **Action Required:** Implement Option A (List-Based Suffix Mapping)

---

### Task 2: Design Solution Architecture

**Three Possible Solutions:**

#### Option A: List-Based Suffix Mapping (Recommended)
**Change:** Allow one suffix to map to multiple configuration keys
```groovy
// Before (collision):
suffixMap = [dwi: 'dwi_is', mask: 'aparc_aseg']

// After (multi-valued):
suffixMap = [
    dwi: ['dwi_ap', 'dwi_rl', 'dwi_is'],
    mask: ['lesion', 'wmparc', 'aparc_aseg']
]
```

**Logic:** For each suffix, try all configurations and match based on entity filters.

**Pros:**
- ✅ Supports heterogeneous datasets
- ✅ All configurations are considered
- ✅ Backwards compatible

**Cons:**
- ⚠️ More complex matching logic
- ⚠️ Need to handle ambiguous matches

---

#### Option B: Make suffix_maps_to Optional
**Change:** Don't require `suffix_maps_to` for variant configurations
```yaml
# Base configuration
dwi:
  plain_set: {}
  additional_extensions: [bvec, bval]
  # No suffix_maps_to needed

# Variant configurations (AP/PA subjects)
dwi_ap:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: ["ap", "pa"]
  additional_extensions: [bvec, bval]
  # No suffix_maps_to - uses config key "dwi_ap" as output suffix

# Variant configurations (RL/LR subjects)
dwi_rl:
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  required: ["rl", "lr"]
  additional_extensions: [bvec, bval]
  # No suffix_maps_to - uses config key "dwi_rl" as output suffix
```

**Output would be:**
```groovy
item.dwi_ap   // For subjects with AP/PA
item.dwi_rl   // For subjects with RL/LR
item.dwi_is   // For subjects with IS/SI
```

**Pros:**
- ✅ Simple fix
- ✅ No collision possible
- ✅ Clear which variant matched

**Cons:**
- ❌ Breaking change for existing pipelines
- ❌ Output suffix name different from BIDS suffix
- ❌ Users need to check multiple possible keys

---

#### Option C: Priority-Based Resolution
**Change:** Add `priority` field to handle collisions
```yaml
dwi_ap:
  named_set: {...}
  suffix_maps_to: "dwi"
  priority: 1  # Try first

dwi_rl:
  named_set: {...}
  suffix_maps_to: "dwi"
  priority: 2  # Try second

dwi_is:
  named_set: {...}
  suffix_maps_to: "dwi"
  priority: 3  # Try last
```

**Logic:** When matching, try configurations in priority order. Stop at first match.

**Pros:**
- ✅ Deterministic behavior
- ✅ User controls resolution order

**Cons:**
- ⚠️ Requires user to understand priority system
- ⚠️ More configuration complexity
- ⚠️ Still only one match per subject (no multi-direction support)

---

### Task 3: Implement Chosen Solution

**Recommended:** Option A (List-Based Suffix Mapping)

**Implementation Steps:**

1. **Modify suffix mapping data structure**
   ```groovy
   // Change from:
   Map<String, String> suffixMappings
   
   // To:
   Map<String, List<String>> suffixMappings
   ```

2. **Update suffix mapping builder**
   ```groovy
   // When registering mappings:
   if (!suffixMappings.containsKey(suffix)) {
       suffixMappings[suffix] = []
   }
   suffixMappings[suffix].add(configKey)
   ```

3. **Update suffix matching logic**
   ```groovy
   // For each file with suffix 'dwi':
   def candidates = suffixMappings['dwi']  // ['dwi_ap', 'dwi_rl', 'dwi_is']
   
   for (configKey in candidates) {
       def config = configurations[configKey]
       if (matchesEntityFilters(file, config)) {
           // Use this configuration
           break
       }
   }
   ```

4. **Add conflict detection**
   ```groovy
   // If multiple configs match the same file, log warning:
   if (matches.size() > 1) {
       log.warn("Multiple configurations match file ${file}: ${matches}")
       log.warn("Using first match: ${matches[0]}")
   }
   ```

---

### Task 4: Fix Configuration File

**Update the user's YAML config** to be more explicit:

```yaml
loop_over:
  - subject
  - session
  - run

T1w:
  plain_set: {}
  suffix_maps_to: "T1w"

# Separate mask types with unique output names
lesion:
  plain_set:
    description: lesion
  suffix_maps_to: "lesion"  # Don't map to "mask"

wmparc:
  plain_set:
    description: wmparc
  suffix_maps_to: "wmparc"  # Don't map to "mask"

aparc_aseg:
  plain_set:
    description: aparc+aseg
  suffix_maps_to: "aparc_aseg"  # Don't map to "mask"

# Single base DWI for subjects without reversed phase
dwi:
  plain_set: {}
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi"

# DWI with AP/PA phase encoding
dwi_ap:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: ["ap", "pa"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi_reversed"  # Different output name

# DWI with RL/LR phase encoding  
dwi_rl:
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  required: ["rl", "lr"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi_reversed"  # Different output name

# DWI with IS/SI phase encoding
dwi_is:
  named_set:
    is: {direction: dir-IS}
    si: {direction: dir-SI}
  required: ["is", "si"]
  additional_extensions: [bvec, bval]
  suffix_maps_to: "dwi_reversed"  # Different output name

# Same pattern for sbref
sbref:
  plain_set: {}

sbref_ap:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: ["ap", "pa"]
  suffix_maps_to: "sbref_reversed"

sbref_rl:
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  required: ["rl", "lr"]
  suffix_maps_to: "sbref_reversed"

sbref_is:
  named_set:
    is: {direction: dir-IS}
    si: {direction: dir-SI}
  required: ["is", "si"]
  suffix_maps_to: "sbref_reversed"
```

**This workaround:**
- Gives unique output names (`dwi_reversed` vs `dwi`)
- Avoids collision
- **Still requires code fix** for list-based mapping

---

### Task 5: Add Tests

**Test file:** `src/test/groovy/nfneuro/channel/BidsHandlerSuffixCollisionSpec.groovy`

**Test cases:**
1. **Test multiple configs mapping to same suffix**
   - Define 3 configs all with `suffix_maps_to: "dwi"`
   - Verify all are registered in suffix map
   - Verify appropriate one is matched based on entity filters

2. **Test heterogeneous subject data**
   - Subject 1 has AP/PA DWI
   - Subject 2 has RL/LR DWI
   - Subject 3 has IS/SI DWI
   - Verify each gets correct configuration

3. **Test ambiguous match warning**
   - Create two configs that both match the same file
   - Verify warning is logged
   - Verify first match is used

---

### Task 6: Update Documentation

**Files to update:**
- `docs/configuration.md` - Document heterogeneous dataset handling
- `README.md` - Add example for multi-direction DWI
- `CHANGELOG.md` - Note breaking change if suffix mapping API changes

**Documentation should include:**
- Explanation of suffix collision issue
- How to configure heterogeneous datasets
- Best practices for naming configurations
- Migration guide if breaking changes

---

## Priority Order

1. ✅ **COMPLETED:** Task 1 - Code investigation (collision confirmed, no recent fixes)
2. ✅ **DECIDED:** Task 2 - Solution design (Option A: List-Based Suffix Mapping)
3. 🔴 **NEXT:** Task 3 - Implement list-based suffix mapping
4. **MEDIUM:** Task 5 - Add tests
5. **MEDIUM:** Task 4 - Fix configuration file (user workaround)
6. **LOW:** Task 6 - Update documentation

---

## Success Criteria

✅ Multiple configurations with same `suffix_maps_to` value coexist  
✅ Heterogeneous DWI datasets (mixed AP/PA, RL/LR, IS/SI) parse correctly  
✅ All subjects are processed (not just those matching last config)  
✅ Multiple mask types (lesion, wmparc, aparc_aseg) coexist  
✅ Clear logging shows which configuration matched each file  
✅ Tests verify collision-free behavior  
✅ Documentation explains how to configure heterogeneous datasets  

---

## Estimated Complexity

**Code changes:** Medium (2-3 hours)
- Modify suffix mapping from Map<String, String> to Map<String, List<String>>
- Update matching logic to try all candidates
- Add conflict detection and logging

**Testing:** Medium (2-3 hours)  
- Write new test cases
- Test with real heterogeneous datasets
- Verify backwards compatibility

**Documentation:** Low (1 hour)
- Update configuration guide
- Add examples
- Document breaking changes

**Total:** ~6 hours for complete solution
