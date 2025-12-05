# TODO: Suffix Mapping and Set Type Detection Fixes

## 🔴 CRITICAL ISSUES

### Issue 1: Suffix Mapping Direction is Backward
- [ ] **Problem:** Maps fileSuffix → configKey (many-to-one), should map configKey → fileSuffix (one-to-many)
- [ ] **Impact:** Multiple configs using same file suffix will overwrite each other
- [ ] **Files:** `SuffixMapper.groovy`, all handlers

### Issue 2: Output Keys Use File Suffix Instead of Config Key
- [ ] **Problem:** `channelData.addSuffixData(fileSuffix, ...)` should use configKey
- [ ] **Impact:** Output structure doesn't match documentation; suffix_maps_to doesn't work as documented
- [ ] **Files:** `PlainSetHandler.groovy:94`, `SequentialSetHandler.groovy:111`, `MixedSetHandler.groovy:119`
- [ ] **Correct Example:** `NamedSetHandler.groovy:123` (already uses configKey)

### Issue 3: `plain_set: {}` Returns Null Set Type
- [ ] **Problem:** Empty map is falsy in Groovy; `if (suffixConfig.plain_set)` fails
- [ ] **Impact:** Files with `plain_set: {}` are silently filtered out
- [ ] **Files:** `BaseSetHandler.groovy:25-36`

---

## 📋 IMPLEMENTATION CHECKLIST

### Phase 2: Fix Suffix Mapping Bijectivity

#### Task 2.1: Clarify Mapping Purpose ✏️
- [ ] Add comprehensive documentation to `SuffixMapper.groovy` header
- [ ] Explain: configKey (unique) → fileSuffix (non-unique)
- [ ] Document one-to-many relationship

#### Task 2.2: Decide on Mapping Strategy ⚖️
- [ ] Choose Option B (Reverse the mapping) - **RECOMMENDED**
- [ ] Alternative: Document limitation of current approach and add validation

#### Task 2.3: Update SuffixMapper Implementation 🔧
- [ ] Reverse mapping direction: `mapping[setType][configKey] = fileSuffix`
- [ ] Rename `resolveConfigKey()` → `getFileSuffixForConfig()` or similar
- [ ] Update method signatures and parameter names
- [ ] Update documentation strings
- [ ] Update logging messages

#### Task 2.4: Update BaseSetHandler.findMatchingGrouping() 🔍
- [ ] Review current logic: uses file suffix to look up config
- [ ] New approach: iterate configs and check if `suffix_maps_to` matches file suffix
- [ ] OR: Keep file suffix lookup but return ALL matching config keys
- [ ] Update logging for clarity

#### Task 2.5: Update BaseSetHandler.getSetIndex() 📍
- [ ] Ensure index includes configKey alongside fileSuffix
- [ ] Example: `[fileSuffix: 'dwi', configKey: 'dwi_fullreverse', group: 'ap']`
- [ ] Update all handler implementations

#### Task 2.6: Fix Output Keys in All Handlers 🔑
- [ ] **PlainSetHandler.groovy:94**
  - Change: `channelData.addSuffixData(suffix, nestedDataMap)`
  - To: `channelData.addSuffixData(configKey, nestedDataMap)`
- [ ] **SequentialSetHandler.groovy:111**
  - Change: `channelData.addSuffixData(suffix, dataMap)`
  - To: `channelData.addSuffixData(configKey, dataMap)`
- [ ] **MixedSetHandler.groovy:119**
  - Change: `channelData.addSuffixData(suffix, groupMap)`
  - To: `channelData.addSuffixData(configKey, groupMap)`
- [ ] Verify **NamedSetHandler.groovy:123** is already correct

#### Task 2.7: Pass Config Key Through Pipeline 🚰
- [ ] Review `packFileIntoSet()` in each handler
- [ ] Update to preserve configKey alongside fileSuffix
- [ ] Update `sets` and `allFiles` data structures if needed
- [ ] Ensure configKey is available in `processGroup()`

#### Task 2.8: Update Variable Names for Clarity 📝
- [ ] Rename `suffix` → `fileSuffix` where it refers to file suffix
- [ ] Add `configKey` variable where config key is used
- [ ] Update comments to clarify distinction
- [ ] Search for all usages of `suffix` variable and verify correctness

### Phase 3: Fix Plain Set Type Detection

#### Task 3.1: Fix BaseSetHandler.getSetType() 🔧
- [ ] File: `BaseSetHandler.groovy:25-36`
- [ ] Change: Use `containsKey('plain_set')` instead of `if (suffixConfig.plain_set)`
- [ ] Apply to all set types (plain_set, named_set, sequential_set, mixed_set)
- [ ] Test with empty maps: `[plain_set: [:]]`

#### Task 3.2: Verify Validation for Missing Set Type ✅
- [ ] File: `BidsConfigValidator.groovy:75-85`
- [ ] Confirm error message exists for missing set type
- [ ] Verify error is clear and actionable

#### Task 3.3: Improve Error Messages in findMatchingGrouping 💬
- [ ] File: `BaseSetHandler.groovy:~350`
- [ ] Better message when `getSetConfig()` returns null
- [ ] Show which set types exist in config
- [ ] Example: "No plain_set config, found: [named_set, sequential_set]"

### Phase 4: Testing & Validation

#### Task 4.1: Unit Tests for SuffixMapper 🧪
- [ ] Create `SuffixMapperSpec.groovy` if missing
- [ ] Test: Single config with suffix_maps_to
- [ ] Test: Multiple configs mapping to same suffix
- [ ] Test: Config without suffix_maps_to (identity)
- [ ] Test: Empty configuration
- [ ] Test: Null configuration

#### Task 4.2: Unit Tests for getSetType() 🧪
- [ ] Test: `plain_set: {}` → returns "plain_set"
- [ ] Test: `plain_set: { parts: [...] }` → returns "plain_set"
- [ ] Test: `named_set: {...}` → returns "named_set"
- [ ] Test: No set type → returns null
- [ ] Test: Multiple set types → returns first found

#### Task 4.3: Integration Tests for suffix_maps_to with plain_set 🧪
- [ ] Create test config: `dwi_simple: { suffix_maps_to: "dwi", plain_set: {} }`
- [ ] Verify output key is `dwi_simple`, not `dwi`
- [ ] Test multiple configs using same file suffix
- [ ] Verify they don't overwrite each other

#### Task 4.4: Integration Tests for plain_set: {} 🧪
- [ ] Use existing configs from `validation/configs/config_dwi.yaml`
- [ ] Verify T1w, sbref, T2w are processed (not filtered)
- [ ] Check output structure matches expectations

#### Task 4.5: Regression Tests 🧪
- [ ] Run full test suite: `./gradlew test`
- [ ] Verify NamedSetHandler tests still pass
- [ ] Update PlainSetHandler tests if needed
- [ ] Update SequentialSetHandler tests if needed
- [ ] Update MixedSetHandler tests if needed

#### Task 4.6: Manual Testing with Real Data 🧪
- [ ] Test with `validation/data/bids-examples/asl001`
- [ ] Test with dataset having multiple configs for same suffix
- [ ] Verify flattened output format
- [ ] Check that config keys (not file suffixes) appear in output

### Phase 5: Documentation & Cleanup

#### Task 5.1: Update docs/configuration.md 📚
- [ ] Clarify: output key is config key, not file suffix
- [ ] Add examples showing difference
- [ ] Explain when to use suffix_maps_to
- [ ] Document any limitations

#### Task 5.2: Update docs/examples.md 📚
- [ ] Add example: multiple configs for same file type
- [ ] Show how suffix_maps_to affects output structure
- [ ] Provide migration guide

#### Task 5.3: Add Code Comments 💬
- [ ] Document config key vs file suffix in all handlers
- [ ] Explain mapping direction in SuffixMapper
- [ ] Document why configKey is used for output

#### Task 5.4: Update CHANGELOG.md 📝
- [ ] Add breaking change notice
- [ ] Explain output structure change
- [ ] Provide migration guide for users

#### Task 5.5: Remove Unused Code 🗑️
- [ ] Check if `SuffixMapper.getOutputSuffix()` is used
- [ ] Remove if unused, or document if needed
- [ ] Clean up any other unused methods

---

## 🎯 SUCCESS CRITERIA

- [ ] All handlers use config key (not file suffix) for output
- [ ] `plain_set: {}` is correctly detected and processed
- [ ] Multiple configs can use same file suffix with different set types
- [ ] Output structure matches documentation examples
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Manual testing with validation datasets succeeds
- [ ] Documentation clearly explains config key vs file suffix
- [ ] CHANGELOG documents breaking changes
- [ ] No regression in existing functionality

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
