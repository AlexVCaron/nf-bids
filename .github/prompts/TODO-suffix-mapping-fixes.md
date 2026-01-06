# TODO: Suffix Mapping and Set Type Detection Fixes

## ✅ COMPLETED (January 5, 2026)

All critical issues have been resolved in 0.1.0-beta.9!

### Issue 1: Suffix Mapping Direction is Backward ✅
- [x] **Problem:** Maps fileSuffix → configKey (many-to-one), should map configKey → fileSuffix (one-to-many)
- [x] **Solution:** Inverted mapping in `SuffixMapper.groovy` (lines 48-49)
- [x] **Files:** `SuffixMapper.groovy`, all handlers
- [x] **Status:** COMPLETE - multiple configs can now share same file suffix

### Issue 2: Output Keys Use File Suffix Instead of Config Key ✅
- [x] **Problem:** `channelData.addSuffixData(fileSuffix, ...)` should use configKey
- [x] **Solution:** All handlers updated to use configKey in output
- [x] **Files:** `PlainSetHandler.groovy`, `SequentialSetHandler.groovy`, `MixedSetHandler.groovy`, `NamedSetHandler.groovy`
- [x] **Status:** COMPLETE - output now uses config keys

### Issue 3: `plain_set: {}` Returns Null Set Type ✅
- [x] **Problem:** Empty map is falsy in Groovy; `if (suffixConfig.plain_set)` fails
- [x] **Solution:** Changed to `containsKey('plain_set')` in `BaseSetHandler.getSetType()`
- [x] **Files:** `BaseSetHandler.groovy:26-34`
- [x] **Status:** COMPLETE - empty plain sets now detected correctly

---

## ✅ IMPLEMENTATION COMPLETED

### Phase 2: Fix Suffix Mapping Bijectivity ✅

#### Task 2.1: Clarify Mapping Purpose ✅
- [x] Add comprehensive documentation to `SuffixMapper.groovy` header
- [x] Explain: configKey (unique) → fileSuffix (non-unique)
- [x] Document one-to-many relationship

#### Task 2.2: Decide on Mapping Strategy ✅
- [x] Chose Option B (Reverse the mapping) - **IMPLEMENTED**

#### Task 2.3: Update SuffixMapper Implementation ✅
- [x] Reversed mapping direction: `mapping[setType][configKey] = fileSuffix`
- [x] Added `resolveConfigKeys()` method returning `List<String>`
- [x] Updated method signatures and parameter names
- [x] Updated documentation strings
- [x] Updated logging messages

#### Task 2.4: Update BaseSetHandler.findMatchingGrouping() ✅
- [x] Iterate through all candidate config keys from `resolveConfigKeys()`
- [x] Try each candidate with entity filter validation
- [x] Return first matching config key
- [x] Updated logging for clarity

#### Task 2.5: Update BaseSetHandler.getSetIndex() ✅
- [x] Index now includes both configKey and fileSuffix
- [x] Example: `[fileSuffix: 'dwi', configKey: 'dwi_ap']`
- [x] Updated all handler implementations

#### Task 2.6: Fix Output Keys in All Handlers ✅
- [x] **PlainSetHandler.groovy** - Changed to use `configKey`
- [x] **SequentialSetHandler.groovy** - Changed to use `configKey`
- [x] **MixedSetHandler.groovy** - Changed to use `configKey`
- [x] **NamedSetHandler.groovy** - Already correct

#### Task 2.7: Pass Config Key Through Pipeline ✅
- [x] Updated `packFileIntoSet()` in all handlers
- [x] Preserve configKey alongside fileSuffix
- [x] Updated `sets` and `allFiles` data structures
- [x] ConfigKey available in `processGroup()`

#### Task 2.8: Update Variable Names for Clarity ✅
- [x] Renamed `suffix` → `fileSuffix` where appropriate
- [x] Added `configKey` variable throughout pipeline
- [x] Updated comments to clarify distinction
- [x] Verified all usages

### Phase 3: Fix Plain Set Type Detection ✅

#### Task 3.1: Fix BaseSetHandler.getSetType() ✅
- [x] File: `BaseSetHandler.groovy:26-34`
- [x] Changed to use `containsKey('plain_set')`
- [x] Applied to all set types
- [x] Tested with empty maps

#### Task 3.2: Verify Validation for Missing Set Type ✅
- [x] Verified error messages in `BidsConfigValidator.groovy`
- [x] Confirmed clear and actionable errors

#### Task 3.3: Improve Error Messages in findMatchingGrouping ✅
- [x] Better messages when config not found
- [x] Show which config keys were tried
- [x] Clear indication when no match found

### Phase 4: Testing & Validation ✅

#### Task 4.1: Unit Tests for SuffixMapper ✅
- [x] All 78 unit tests passing
- [x] Tested multiple configs mapping to same suffix
- [x] Tested config without suffix_maps_to

#### Task 4.2: Unit Tests for getSetType() ✅
- [x] Test: `plain_set: {}` → returns "plain_set"
- [x] All set types validated
- [x] Null configuration handled

#### Task 4.3: Integration Tests for suffix_maps_to with plain_set ✅
- [x] Created comprehensive test suite in `test_heterogeneous_suffix_mapping_flat.nf.test`
- [x] Verified output key is configKey, not fileSuffix
- [x] Tested multiple configs using same file suffix
- [x] Verified no overwrites

#### Task 4.4: Integration Tests for plain_set: {} ✅
- [x] All validation configs tested
- [x] T1w, sbref, T2w processed correctly
- [x] Output structure matches expectations

#### Task 4.5: Regression Tests ✅
- [x] Full test suite passing: `./gradlew test`
- [x] All handler tests passing
- [x] 25+ integration tests passing
- [x] 4 new heterogeneous tests passing

#### Task 4.6: Manual Testing with Real Data ✅
- [x] Tested with `validation/data/custom/ds-dwi4`
- [x] Verified multiple configs for same suffix
- [x] Checked flat output format
- [x] Config keys appear in output (not file suffixes)

### Phase 5: Documentation & Cleanup ✅

#### Task 5.1: Update docs/configuration.md ✅
- [x] Clarified output key is config key
- [x] Added heterogeneous dataset examples
- [x] Explained when to use suffix_maps_to
- [x] Documented `exclude_entities` pattern

#### Task 5.2: Update docs/examples.md ⏳
- [ ] Add example: multiple configs for same file type
- [ ] Show how suffix_maps_to affects output structure
- [ ] Provide migration guide

#### Task 5.3: Add Code Comments ✅
- [x] Documented config key vs file suffix in all handlers
- [x] Explained mapping direction in SuffixMapper
- [x] Documented why configKey is used for output

#### Task 5.4: Update CHANGELOG.md ✅
- [x] Added breaking change notice
- [x] Explained output structure change
- [x] Provided migration information

#### Task 5.5: Remove Unused Code ⏳
- [ ] Review for unused methods
- [ ] Clean up if needed

---

## ✅ SUCCESS CRITERIA - ALL MET!

- [x] All handlers use config key (not file suffix) for output
- [x] `plain_set: {}` is correctly detected and processed
- [x] Multiple configs can use same file suffix with different set types
- [x] Output structure matches documentation examples
- [x] All 78 unit tests pass
- [x] All 25+ integration tests pass
- [x] 4 new heterogeneous dataset tests pass
- [x] Manual testing with validation datasets succeeds
- [x] Documentation clearly explains config key vs file suffix
- [x] CHANGELOG documents breaking changes
- [x] No regression in existing functionality

---

## 🎉 COMPLETION SUMMARY

**Date Completed:** January 5, 2026  
**Version:** 0.1.0-beta.9

All critical issues have been resolved:
1. ✅ Suffix mapping inverted (configKey → fileSuffix)
2. ✅ Output keys use config keys, not file suffixes
3. ✅ Empty plain sets detected correctly
4. ✅ Heterogeneous datasets fully supported
5. ✅ Comprehensive test suite created and passing
6. ✅ Documentation updated

**New Features:**
- Multiple configs can share same `suffix_maps_to` value
- `exclude_entities` pattern prevents double-matching
- Config keys preserved in output structure
- Full support for heterogeneous acquisition schemes

**Test Coverage:**
- 78 unit tests passing
- 25+ integration tests passing
- 4 new heterogeneous dataset tests
- Test dataset: `validation/data/custom/ds-dwi4`
- Test workflow: `validation/main_flat.nf`
- Test suite: `validation/test_heterogeneous_suffix_mapping_flat.nf.test`

**Remaining Work (Optional):**
- [ ] Add more examples to docs/examples.md
- [ ] Code cleanup and optimization
- [ ] Performance testing with large datasets

---

## 🔍 KEY INSIGHTS

### The Core Problem
**Config Key ≠ File Suffix**
- Config key: `dwi_fullreverse` (unique identifier in YAML)
- File suffix: `dwi` (extracted from filename `sub-01_dwi.nii.gz`)
- Multiple config keys can map to same file suffix
- Output should use config key, not file suffix

### Why Current Code is Wrong
1. **SuffixMapper** maps backwards: fileSuffix → configKey (many-to-one)
   - Should map: configKey → fileSuffix (one-to-many)

2. **Three handlers** use file suffix for output key:
   - PlainSetHandler, SequentialSetHandler, MixedSetHandler
   - NamedSetHandler is correct (uses configKey)

3. **getSetType()** uses Groovy truthiness:
   - Empty map `[:]` is falsy
   - `if (suffixConfig.plain_set)` fails for `plain_set: {}`
   - Should use `containsKey()`

### The Fix Strategy
1. Reverse SuffixMapper direction OR iterate configs instead of mapping
2. Pass configKey through entire pipeline alongside fileSuffix
3. Use configKey for output in all handlers
4. Use containsKey() for set type detection

---

## 📚 REFERENCE FILES

### Must Read
- Plan document: `.github/prompts/plan-suffix-mapping-and-set-type-fixes.md`
- Config example: `validation/configs/config_dwi.yaml`
- Documentation: `docs/configuration.md` (lines 295-325)

### Files to Modify
- `src/main/groovy/nfneuro/util/SuffixMapper.groovy`
- `src/main/groovy/nfneuro/grouping/BaseSetHandler.groovy`
- `src/main/groovy/nfneuro/grouping/PlainSetHandler.groovy`
- `src/main/groovy/nfneuro/grouping/SequentialSetHandler.groovy`
- `src/main/groovy/nfneuro/grouping/MixedSetHandler.groovy`

### Files to Reference (Correct Implementation)
- `src/main/groovy/nfneuro/grouping/NamedSetHandler.groovy` (line 123)
