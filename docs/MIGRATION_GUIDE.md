# Migration Guide: From bids2nf to nf-bids Plugin

This comprehensive guide helps you migrate workflows from the original bids2nf implementation to the nf-bids plugin, covering all versions from the baseline through v0.1.0-beta.9.

---

## 📍 Quick Navigation

- [Which Version Am I On?](#which-version-am-i-on) - Identify your current version
- [Should I Migrate?](#should-i-migrate) - Assess migration necessity
- [Migration Paths](#migration-paths) - Choose your upgrade path
- [Breaking Changes](#breaking-changes-by-version) - What changed and when
- [Troubleshooting](#troubleshooting) - Common issues and solutions
- 📚 [Configuration Guide](configuration.md) - Complete YAML options reference

---

## 📊 Version Timeline

| Version | Release Date | Key Changes | Breaking? |
|---------|--------------|-------------|-----------|
| **Baseline bids2nf** | Pre-2024 | Original Nextflow subworkflow | - |
| **v0.1.0** | ~Dec 2024 | Initial plugin implementation | ❌ No |
| **v0.1.0-beta.1 to 4** | Early 2025 | Core functionality stabilization | ⚠️ Minor |
| **v0.1.0-beta.5** | ~Oct 2025 | `combineBy` redesign, Java Path objects | ⚠️ Yes (operators) |
| **v0.1.0-beta.6** | ~Nov 2025 | Flat output (opt-in), suffix mapping fix | ⚠️ Optional |
| **v0.1.0-beta.9** | Dec 6, 2025 | **Flat output DEFAULT** | ⚠️ **YES** |
| **v0.2.0** | Future | Legacy format removal | ⚠️ **YES** |

---

## 🤔 Which Version Am I On?

### Check Your Current Setup

**Using baseline bids2nf?**
```groovy
// If you have this in your workflow:
include { bids2nf } from './modules/bids2nf'
// → You're on BASELINE
```

**Using the plugin?**
```groovy
// Check your nextflow.config:
plugins {
    id 'nf-bids@0.1.0-beta.9'  // Your version here
}
```

**No version specified?**
```bash
# Check what version is actually loaded:
nextflow plugin list
# Look for: nf-bids@<version>
```

---

## 🎯 Should I Migrate?

### Decision Tree

```
Are you using baseline bids2nf?
├─ YES → [Path 1: Complete Migration Required] ⚠️
└─ NO → Are you on plugin version beta.1-5?
    ├─ YES → [Path 2: Output Format Update Required] ⚠️
    └─ NO → Are you on beta.6-8?
        ├─ Using flatten_output: false? → [Path 3: Update Needed] ⚠️
        └─ Using flatten_output: true? → [Path 4: Minimal Changes] ✅
```

### Quick Assessment

| Your Situation | Migration Path | Effort | Timeline |
|---------------|----------------|--------|----------|
| Baseline bids2nf | Path 1 | 🔴 High | 1-2 weeks |
| Plugin beta.1-5 | Path 2 | 🟡 Medium | 3-5 days |
| Plugin beta.6-8 (legacy format) | Path 3 | 🟡 Medium | 2-3 days |
| Plugin beta.6-8 (flat format) | Path 4 | 🟢 Low | 1 day |

---

---

# Migration Paths

## Path 1: Baseline bids2nf → v0.1.0-beta.9

**For:** Users currently using the original bids2nf subworkflow.

**Changes Required:**
1. ✅ Install plugin infrastructure
2. ✅ Update imports and channel factory calls
3. ✅ Migrate to flat output format
4. ✅ Adapt to Java Path objects
5. ✅ Optional: Adopt new configuration features

**Estimated Effort:** 🔴 High (1-2 weeks for production workflows)

### Step 1: Install the Plugin

Edit your `nextflow.config`:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.9'
}
```

**Tip:** You can specify any version or omit `@0.1.0-beta.9` for latest.

### Step 2: Update Workflow Imports

**Before (Baseline):**
```groovy
include { bids2nf } from './modules/bids2nf'

workflow {
    bids_channel = bids2nf(
        params.bids_dir,
        'config.yaml'
    )
}
```

**After (Plugin):**
```groovy
include { fromBIDS } from 'plugin/nf-bids'

workflow {
    bids_channel = Channel.fromBIDS(
        params.bids_dir,
        'config.yaml'
    )
}
```

### Step 3: Understand the New Output Structure

**Old Structure (Baseline - Tuple Format):**
```groovy
[
    ["sub-01", "ses-01", "NA", "NA"],  // [subject, session, run, task]
    [
        bidsParentDir: "/path/to/bids",
        subject: "sub-01",
        session: "ses-01",
        run: "NA",
        task: "NA",
        data: [
            dwi: [
                nii: "sub-01/ses-01/dwi/sub-01_ses-01_dwi.nii.gz",  // Relative path
                json: "sub-01/ses-01/dwi/sub-01_ses-01_dwi.json",
                bval: "sub-01/ses-01/dwi/sub-01_ses-01_dwi.bval",
                bvec: "sub-01/ses-01/dwi/sub-01_ses-01_dwi.bvec"
            ],
            T1w: [
                nii: "sub-01/ses-01/anat/sub-01_ses-01_T1w.nii.gz",
                json: "sub-01/ses-01/anat/sub-01_ses-01_T1w.json"
            ]
        ]
    ]
]
```

**New Structure (beta.9 - Flat Map Format):**
```groovy
[
    meta: [
        subject: "sub-01",
        session: "ses-01",
        run: "NA",
        task: "NA"
    ],
    dwi: [
        nii: Path("/path/to/bids/sub-01/ses-01/dwi/sub-01_ses-01_dwi.nii.gz"),  // Absolute Path
        json: Path("/path/to/bids/sub-01/ses-01/dwi/sub-01_ses-01_dwi.json"),
        bval: Path("/path/to/bids/sub-01/ses-01/dwi/sub-01_ses-01_dwi.bval"),
        bvec: Path("/path/to/bids/sub-01/ses-01/dwi/sub-01_ses-01_dwi.bvec")
    ],
    T1w: [
        nii: Path("/path/to/bids/sub-01/ses-01/anat/sub-01_ses-01_T1w.nii.gz"),
        json: Path("/path/to/bids/sub-01/ses-01/anat/sub-01_ses-01_T1w.json")
    ]
]
```

**Key Differences:**
- ✅ **Flat structure:** No nested tuple `[key, data]`
- ✅ **Named entities:** Access via `item.meta.subject` not `item[0][0]`
- ✅ **Absolute paths:** All paths are absolute `java.nio.file.Path` objects
- ✅ **Direct access:** `item.dwi.nii` instead of `file("${bidsDir}/${data.data.dwi.nii}")`
- ✅ **No path concatenation:** Files are ready to use immediately

### Step 4: Update Entity Access

**Before:**
```groovy
.map { groupingKey, enrichedData ->
    def subject = groupingKey[0]
    def session = groupingKey[1]
    def run = groupingKey[2]
    def task = groupingKey[3]
    
    // ... use entities
}
```

**After:**
```groovy
.map { item ->
    def subject = item.meta.subject
    def session = item.meta.session
    def run = item.meta.run
    def task = item.meta.task
    
    // ... use entities
}
```

### Step 5: Update File Path Handling

**Before:**
```groovy
.map { groupingKey, enrichedData ->
    def bidsDir = enrichedData.bidsParentDir
    def dwi_nii = file("${bidsDir}/${enrichedData.data.dwi.nii}")
    def dwi_bval = file("${bidsDir}/${enrichedData.data.dwi.bval}")
    def t1w = file("${bidsDir}/${enrichedData.data.T1w.nii}")
    
    return [groupingKey[0], dwi_nii, dwi_bval, t1w]
}
```

**After:**
```groovy
.map { item ->
    // Files are already absolute Path objects - use directly!
    def dwi_nii = item.dwi.nii
    def dwi_bval = item.dwi.bval
    def t1w = item.T1w.nii
    
    return [item.meta.subject, dwi_nii, dwi_bval, t1w]
}
```

### Step 6: Update Process Inputs

**Before:**
```groovy
process myProcess {
    input:
    tuple val(subject), path(dwi), path(bval), path(t1w)
    
    script:
    """
    # Process files
    """
}
```

**After (No Changes Needed!):**
```groovy
process myProcess {
    input:
    tuple val(subject), path(dwi), path(bval), path(t1w)
    
    script:
    """
    # Process files - Path objects work seamlessly
    """
}
```

**Note:** `java.nio.file.Path` objects work directly with Nextflow `path` inputs!

### Step 7: Handle Named Sets

**Before:**
```groovy
def apNii = file("${data.bidsParentDir}/${data.data.dwi.ap.nii}")
def paNii = file("${data.bidsParentDir}/${data.data.dwi.pa.nii}")
```

**After:**
```groovy
// Note: Output uses config key (e.g., dwi_ap) not file suffix (dwi)
def apNii = item.dwi_ap.ap.nii  // If config key is "dwi_ap"
def paNii = item.dwi_ap.pa.nii
```

**Important:** Since beta.6, outputs use **config keys** not file suffixes. If your config has:
```yaml
dwi_ap:
  suffix_maps_to: "dwi"
  named_set: ...
```
The output key will be `dwi_ap`, not `dwi`.

### Step 8: Handle Sequential Sets

**Before:**
```groovy
def echos = data.data.mese.nii.collect { relativePath ->
    file("${data.bidsParentDir}/${relativePath}")
}
```

**After:**
```groovy
def echos = item.mese.nii  // Already a list of Path objects
```

### Step 9: Test Your Migration

**Side-by-side comparison:**
```groovy
// Keep both temporarily for validation
baseline_channel = bids2nf(params.bids_dir, 'config.yaml')
plugin_channel = Channel.fromBIDS(params.bids_dir, 'config.yaml')

baseline_channel
    .map { key, data -> [key[0], data.data.T1w.nii] }
    .view { "BASELINE: $it" }

plugin_channel
    .map { item -> [item.meta.subject, item.T1w.nii] }
    .view { "PLUGIN: $it" }
```

### Step 10: Configuration Compatibility

**Good News:** Your YAML configuration file works unchanged! 

**However**, new options are available:

```yaml
# New in beta.6: exclude_entities
dwi:
  plain_set:
    exclude_entities: [direction]  # Exclude files with dir-AP, dir-PA, etc.
  additional_extensions: [.bval, .bvec]

# Heterogeneous dataset support
dwi_ap:
  suffix_maps_to: "dwi"
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  additional_extensions: [.bval, .bvec]
```

📖 **See [Configuration Guide](configuration.md) for complete YAML configuration options.**

---

## Path 2: v0.1.0-beta.1-5 → v0.1.0-beta.9

**For:** Users already on the plugin but using the legacy tuple format.

**Changes Required:**
1. ✅ Migrate to flat output format
2. ⚠️ Update `combineBy` operator usage (if applicable)

**Estimated Effort:** 🟡 Medium (3-5 days)

### Step 1: Update Plugin Version

Edit your `nextflow.config`:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.9'  // Update version
}
```

### Step 2: Understand Default Behavior Change

**In beta.1-5:** Output was always tuple format  
**In beta.9:** Output is flat map format by default

You have two options:

**Option A: Migrate to Flat Format (Recommended)**
- Follow Steps 3-6 from [Path 1](#step-3-understand-the-new-output-structure)
- Update entity access patterns
- Update file path handling
- No changes to configuration needed

**Option B: Keep Legacy Format (Temporary)**
```groovy
Channel.fromBIDS(
    params.bids_dir,
    'config.yaml',
    [flatten_output: false]  // Explicitly opt-out
)
```

**⚠️ Warning:** Legacy format will be removed in v0.2.0. Migrate as soon as possible.

### Step 3: Update combineBy Usage (If Applicable)

**Breaking Change in beta.5:** The `combineBy` operator API changed from filter predicates to key extractors.

**Before (beta.4):**
```groovy
subjects.combineBy(sessions) { subj, sess ->
    subj.id == sess.subject  // Filter predicate
}
.view { subj, sess -> "${subj} with ${sess}" }  // 2 elements
```

**After (beta.5+):**
```groovy
subjects.combineBy(
    sessions,
    { it.id },      // Left key extractor
    { it.subject }  // Right key extractor
)
.view { key, subj, sess -> "${key}: ${subj} with ${sess}" }  // 3 elements (key added)
```

**See [channel-operators.md](channel-operators.md#combineby) for detailed migration guide.**

### Step 4: Test and Validate

Run your existing test suite:

```bash
# If using nf-test
nf-test test

# If using integration tests
./run_integration_tests.sh
```

---

## Path 3: v0.1.0-beta.6-8 (Legacy Format) → v0.1.0-beta.9

**For:** Users on beta.6-8 but still using `flatten_output: false`.

**Changes Required:**
1. ✅ Adopt flat output format OR explicitly opt-out

**Estimated Effort:** 🟡 Medium (2-3 days to fully migrate)

### Option A: Migrate to Flat Format (Recommended)

Follow Steps 3-8 from [Path 1](#step-3-understand-the-new-output-structure):
- Update entity access patterns
- Update file path handling
- Update any tuple index references
- Remove `flatten_output: false` option

### Option B: Explicitly Opt-Out (Temporary)

Keep using legacy format:

```groovy
Channel.fromBIDS(
    params.bids_dir,
    'config.yaml',
    [flatten_output: false]  // Required in beta.9
)
```

**Previously:** `flatten_output: false` was default (no explicit option needed)  
**Now:** Must explicitly specify to use legacy format  
**Future:** Legacy format will be removed in v0.2.0

### Recommendation

**Migrate now** to avoid a larger migration later. The flat format offers:
- ✅ Cleaner, more maintainable code
- ✅ Better IDE autocomplete support
- ✅ Type safety with Path objects
- ✅ No manual path concatenation
- ✅ Future-proof (legacy format being removed)

---

## Path 4: v0.1.0-beta.6-8 (Flat Format) → v0.1.0-beta.9

**For:** Users already using `flatten_output: true` in beta.6-8.

**Changes Required:**
1. ✅ Remove explicit `flatten_output: true` (now default)
2. ✅ Optional: Review new configuration features

**Estimated Effort:** 🟢 Low (1 day)

### Step 1: Update Plugin Version

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.9'
}
```

### Step 2: Remove Explicit flatten_output Option

**Before (beta.6-8):**
```groovy
Channel.fromBIDS(
    params.bids_dir,
    'config.yaml',
    [flatten_output: true]  // Explicit opt-in
)
```

**After (beta.9):**
```groovy
Channel.fromBIDS(
    params.bids_dir,
    'config.yaml'
    // flatten_output: true is now default - no option needed
)
```

### Step 3: Test

Run your existing tests - everything should work unchanged!

```bash
nextflow run main.nf
```

### Step 4: Optional Enhancements

Consider adopting new configuration features:

**Heterogeneous datasets:**
```yaml
# Multiple subjects with different acquisition schemes
dwi:
  plain_set:
    exclude_entities: [direction]

dwi_ap:
  suffix_maps_to: "dwi"
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
```

📖 **See [Configuration Guide](configuration.md) for complete YAML configuration options.**

---

---

## Migration-Critical Configuration Options

For complete configuration documentation including set types, all options, and detailed examples, see:

📖 **[Configuration Guide](configuration.md)** - Complete YAML configuration reference

This section covers only the **migration-critical** configuration options you need to know when upgrading between versions.

---

### Quick Reference Table

| Option | Since | Description | Migration Impact |
|--------|-------|-------------|------------------|
| **`additional_extensions`** | v0.1.0 | Include additional file types (`.bval`, `.bvec`, etc.) | No breaking changes |
| **`include_cross_modal`** | v0.1.0 | Include files from other suffixes | No breaking changes |
| **`required`** | v0.1.0 | Enforce presence of named groups | No breaking changes |
| **`suffix_maps_to`** | v0.1.0 | Use files with different suffix than config key | ⚠️ **Output key changed in beta.6** |
| **`exclude_entities`** | v0.1.0-beta.6 🆕 | Exclude files with specific entities | 🆕 New option - required for heterogeneous datasets |

---

### `exclude_entities` (New in beta.6)

**Critical for heterogeneous datasets.** When using `suffix_maps_to`, you **MUST** use `exclude_entities` in plain configs to prevent double-matching.

**Example:**
```yaml
# ✅ CORRECT - Files match only one config
dwi:
  plain_set: {}
  exclude_entities: [direction]  # Files without direction entity
    
dwi_ap:
  suffix_maps_to: "dwi"
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
```

📖 **See:** [Configuration Guide - exclude_entities](configuration.md#exclude_entities) for complete documentation.

---

### `suffix_maps_to` (Breaking Change in beta.6)

**Breaking change in beta.6:** Output key changed from file suffix to config key.

**Example:**
```yaml
dwi_with_reverse:
  suffix_maps_to: "dwi"  # Look for *_dwi.nii.gz files
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
```

**Output:**
- **Before beta.6:** Output key = `dwi` (file suffix)
- **Since beta.6:** Output key = `dwi_with_reverse` (config key)

📖 **See:** [Configuration Guide - suffix_maps_to](configuration.md#suffix_maps_to) for complete documentation and patterns.

---

## Heterogeneous Dataset Pattern

**New in beta.6:** Support for datasets where different subjects have different acquisition schemes.

### The Problem

Some subjects have AP/PA phase encoding, others have RL/LR, and some have no phase encoding at all.

### The Solution

Use multiple configurations with `suffix_maps_to` and `exclude_entities`:

```yaml
loop_over:
  - subject
  - session

# Plain DWI (no phase encoding)
dwi:
  plain_set:
    exclude_entities: [direction]  # ← CRITICAL: Only files WITHOUT direction
  additional_extensions: [.bval, .bvec]

# AP/PA phase encoding
dwi_ap:
  suffix_maps_to: "dwi"
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: [ap, pa]
  additional_extensions: [.bval, .bvec]

# RL/LR phase encoding
dwi_rl:
  suffix_maps_to: "dwi"
  named_set:
    rl: {direction: dir-RL}
    lr: {direction: dir-LR}
  required: [rl, lr]
  additional_extensions: [.bval, .bvec]
```

### Output Examples

**Subject with no phase encoding:**
```groovy
[
  meta: [subject: 'sub-01', ...],
  dwi: [
    nii: Path('/.../sub-01_dwi.nii.gz'),
    bval: Path('/.../sub-01_dwi.bval'),
    bvec: Path('/.../sub-01_dwi.bvec')
  ]
]
```

**Subject with AP/PA phase encoding:**
```groovy
[
  meta: [subject: 'sub-02', ...],
  dwi_ap: [  // Config key, not "dwi"
    ap: [nii: Path('/.../sub-02_dir-AP_dwi.nii.gz'), ...],
    pa: [nii: Path('/.../sub-02_dir-PA_dwi.nii.gz'), ...]
  ]
]
```

**Subject with RL/LR phase encoding:**
```groovy
[
  meta: [subject: 'sub-03', ...],
  dwi_rl: [  // Different config key
    rl: [nii: Path('/.../sub-03_dir-RL_dwi.nii.gz'), ...],
    lr: [nii: Path('/.../sub-03_dir-LR_dwi.nii.gz'), ...]
  ]
]
```

### Key Principles

1. **Config keys differ** - Each config has unique output key (`dwi`, `dwi_ap`, `dwi_rl`)
2. **File suffix same** - All use `*_dwi.nii.gz` files (via `suffix_maps_to`)
3. **Entities distinguish** - `exclude_entities` prevents double-matching
4. **No collisions** - Each file matches exactly one configuration

---

---

# Breaking Changes by Version

## v0.1.0-beta.9 (Dec 6, 2025)

### ⚠️ Flat Output DEFAULT

**Impact:** 🔴 High - All users must update or opt-out

**What Changed:**
- `Channel.fromBIDS` now outputs **flat map format** by default
- Previously required `flatten_output: true` to enable
- Legacy tuple format now requires explicit `flatten_output: false`

**Who's Affected:**
- ✅ **Not affected:** Users already using `flatten_output: true` in beta.6-8
- ⚠️ **Affected:** Users on beta.6-8 using `flatten_output: false` or no option
- ⚠️ **Affected:** Users upgrading from beta.5 or earlier

**Migration:**
```groovy
// Option 1: Migrate to flat format (recommended)
Channel.fromBIDS(bidsDir, config)  // No option needed - flat is default

// Option 2: Keep legacy format (temporary - will be removed in v0.2.0)
Channel.fromBIDS(bidsDir, config, [flatten_output: false])
```

**See:** [Path 1](#path-1-baseline-bids2nf--v010-beta9), [Path 2](#path-2-v010-beta1-5--v010-beta9), [Path 3](#path-3-v010-beta6-8-legacy-format--v010-beta9)

---

## v0.1.0-beta.6 (Nov 2025)

### ⚠️ Flat Output Introduced (Opt-in)

**Impact:** 🟡 Medium - Optional feature, no breaking changes if not enabled

**What Changed:**
- New flat map output structure available via `flatten_output: true`
- Traditional tuple format remains default in this version
- Config keys now used in output (not file suffixes)

**New Features:**
- `exclude_entities` configuration option
- Heterogeneous dataset support
- Fixed suffix mapping direction

### ⚠️ Suffix Mapping Output Keys

**Impact:** 🟡 Medium - Only affects `suffix_maps_to` users

**What Changed:**
- Output now uses **config key** instead of file suffix
- Enables multiple configs to map to same suffix

**Before (beta.5):**
```yaml
dwi_with_reverse:
  suffix_maps_to: "dwi"
  named_set: ...
```
```groovy
// Output used FILE SUFFIX:
[..., data: [dwi: [...]]]  // Used "dwi" not "dwi_with_reverse"
```

**After (beta.6+):**
```groovy
// Output uses CONFIG KEY:
[meta: [...], dwi_with_reverse: [...]]  // Uses config key
```

**Migration:**
Update code accessing output keys:
```groovy
// Before:
def files = item.data.dwi  // Used file suffix

// After:
def files = item.dwi_with_reverse  // Use config key
```

### 🆕 exclude_entities Configuration

**New Feature:** Filter files by entity presence

```yaml
dwi:
  plain_set:
    exclude_entities: [direction, reconstruction]
```

**Critical for heterogeneous datasets** - prevents files from matching multiple configs.

---

## v0.1.0-beta.5 (Oct 2025)

### ⚠️ combineBy Operator Redesign

**Impact:** 🔴 High - All `combineBy` users must update

**What Changed:**
- Replaced filter predicates with key extractors
- Output includes key as first element
- Aligns with Nextflow's `combine(by:)` pattern

**Before (beta.4):**
```groovy
subjects.combineBy(sessions) { subj, sess ->
    subj.id == sess.subject  // Predicate function
}
.view { subj, sess ->  // 2 elements
    "${subj} with ${sess}"
}
```

**After (beta.5+):**
```groovy
subjects.combineBy(
    sessions,
    { it.id },      // Left key extractor
    { it.subject }  // Right key extractor
)
.view { key, subj, sess ->  // 3 elements (key added!)
    "${key}: ${subj} with ${sess}"
}
```

**Migration Steps:**
1. Replace predicate function with two key extractors
2. Update downstream code to handle 3-element tuples (key added)
3. Test cartesian product behavior within matching keys

**See:** [channel-operators.md](channel-operators.md#combineby-migration-beta4-to-beta5)

### ⚠️ Java File → Path Conversion

**Impact:** 🟡 Low - Most code unaffected

**What Changed:**
- All file paths now use `java.nio.file.Path` (was `java.io.File`)
- Fixes compatibility with Nextflow process `path` inputs
- Enables cloud storage support (S3, GCS, Azure)

**Migration:**
Most workflows require no changes. If using File-specific APIs:
```groovy
// Before:
def file = item.T1w
file.listFiles()  // File method

// After:
def path = item.T1w
Files.list(path)  // Path API
// OR
path.toFile().listFiles()  // Convert if needed
```

**Benefits:**
- ✅ Nextflow process `path` inputs work correctly
- ✅ Cloud storage support
- ✅ Modern Java NIO API

---

## v0.1.0-beta.1 to 4 (Early 2025)

### Core Functionality

**Impact:** ❌ No breaking changes from v0.1.0

**Changes:**
- Bug fixes and stability improvements
- Test suite expansion
- Documentation updates
- Performance optimizations

---

## v0.1.0 (Dec 2024)

### Initial Plugin Release

**Impact:** 🔴 High - Architecture change from baseline

**What Changed:**
- Converted from Nextflow subworkflow to plugin
- Same output format as baseline (tuple format)
- Installation via `plugins { id 'nf-bids' }`

**Migration from Baseline:**
```groovy
// Before (baseline):
include { bids2nf } from './modules/bids2nf'
bids_channel = bids2nf(bidsDir, config)

// After (v0.1.0):
include { fromBIDS } from 'plugin/nf-bids'
bids_channel = Channel.fromBIDS(bidsDir, config)
```

**Benefits:**
- ✅ Cleaner workflow code
- ✅ Easier updates (version pinning)
- ✅ Better integration with Nextflow ecosystem

---

# Troubleshooting

## Common Issues by Version

### Beta.9: "Output structure changed"

**Symptoms:**
- `Cannot get property 'data' on null object`
- `No such property: data for class: java.util.LinkedHashMap`
- Files not found in expected locations

**Cause:** Flat output is now default (was tuple format)

**Solutions:**

**Option 1 - Migrate to Flat Format:**
```groovy
// Update entity access:
def subject = item.meta.subject  // Not item[0][0]

// Update file access:
def t1w = item.T1w.nii  // Not file("${item[1].bidsParentDir}/${item[1].data.T1w.nii}")
```

**Option 2 - Temporarily Use Legacy Format:**
```groovy
Channel.fromBIDS(bidsDir, config, [flatten_output: false])
```

### Beta.6+: "Files appearing in multiple outputs"

**Symptoms:**
- Same file matched by multiple configurations
- Duplicate processing
- Unexpected output keys

**Cause:** Using `suffix_maps_to` without `exclude_entities`

**Solution:**
```yaml
# Add exclude_entities to plain config:
dwi:
  plain_set:
    exclude_entities: [direction]  # ← Critical!

dwi_ap:
  suffix_maps_to: "dwi"
  named_set:
    ap: {direction: dir-AP}
```

**Explanation:** Without `exclude_entities`, files with `dir-AP` match BOTH `dwi` and `dwi_ap`.

### Beta.6+: "Cannot access output.dwi"

**Symptoms:**
- `No such property: dwi`
- Output key not found

**Cause:** Config uses `suffix_maps_to`, output uses config key (not file suffix)

**Solution:**
```groovy
// Use config key, not file suffix:
def files = item.dwi_with_reverse  // Config key
// NOT: def files = item.dwi  // File suffix doesn't exist in output
```

### Beta.5+: "combineBy returns wrong number of elements"

**Symptoms:**
- `ArrayIndexOutOfBoundsException`
- "Too many values to unpack"

**Cause:** `combineBy` now returns 3 elements (was 2)

**Solution:**
```groovy
// Update to handle key:
.view { key, left, right ->  // Not just { left, right }
    "${key}: ${left} with ${right}"
}
```

### Beta.5+: "Process path input error"

**Symptoms:**
- `Unexpected path value: [java.io.File]`
- Process staging fails

**Cause:** Your version still uses `File` objects (beta.4 or earlier)

**Solution:** Upgrade to beta.5+ for automatic `Path` conversion

### All Versions: "Empty channel / no emissions"

**Symptoms:**
- Channel appears empty
- No files processed

**Common Causes:**

**1. Missing required groups:**
```yaml
dwi:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: [ap, pa]  # ← Both must exist
```
**Solution:** Check if all subjects have both groups, or remove `required`

**2. Entity mismatch:**
```yaml
# Config expects long name:
dwi:
  named_set:
    ap:
      direction: "dir-AP"  # ← Should match file exactly
```
**Solution:** Check actual entity values in filenames: `sub-01_dir-AP_dwi.nii.gz`

**3. Exclude entities too broad:**
```yaml
dwi:
  plain_set:
    exclude_entities: [direction, echo, flip, ...]  # ← Excluding everything!
```
**Solution:** Only exclude entities that distinguish configs

**4. Loop over entities:**
```yaml
loop_over:
  - subject
  - session
  - run  # ← If run is "NA" for all files, will separate them unnecessarily
```
**Solution:** Only include entities that exist with non-NA values

---

## Debugging Tips

### 1. Enable Debug Output

Add `.view()` to inspect channel content:

```groovy
Channel.fromBIDS(params.bids_dir, params.config)
    .view { "Channel item: ${it}" }
    .set { bids_ch }
```

### 2. Check Configuration Validation

Plugin validates config at startup:

```bash
nextflow run main.nf -profile test
# Look for validation warnings in output
```

### 3. Test with Small Dataset

Create minimal test dataset:
```
test_data/
  sub-01/
    anat/
      sub-01_T1w.nii.gz
      sub-01_T1w.json
```

### 4. Compare Outputs

Run side-by-side comparison:

```groovy
baseline_ch = bids2nf(bidsDir, config)
plugin_ch = Channel.fromBIDS(bidsDir, config)

baseline_ch.view { "BASELINE: $it" }
plugin_ch.view { "PLUGIN: $it" }
```

### 5. Check File Matching

Use validation scripts:

```bash
cd validation/
bash test_datasets.sh
```

---

## Getting Help

### Before Opening an Issue

1. ✅ Check which version you're using
2. ✅ Read relevant migration path in this guide
3. ✅ Review troubleshooting section above
4. ✅ Test with minimal example
5. ✅ Check if configuration is valid

### When Opening an Issue

Include:
- **Version:** Plugin version (`nextflow plugin list`)
- **Configuration:** Your YAML config (anonymize if needed)
- **Error:** Full error message and stack trace
- **Dataset:** Description of BIDS structure
- **Minimal Example:** Reproducible test case if possible

### Resources

- 📚 [Configuration Guide](configuration.md) - Complete config reference
- 🏗️ [Architecture](architecture.md) - How the plugin works
- 🔧 [Channel Operators](channel-operators.md) - Custom operators
- 📝 [Examples](examples.md) - Real-world usage patterns
- 🐛 [GitHub Issues](https://github.com/nf-neuro/nf-bids/issues) - Report bugs

---

# Benefits After Migration

Once fully migrated to v0.1.0-beta.9 with flat output:

## Code Quality

- ✅ **Cleaner code** - Direct property access instead of tuple indices
- ✅ **Better readability** - `item.meta.subject` vs `item[0][0]`
- ✅ **Type safety** - Named fields prevent index errors
- ✅ **IDE support** - Autocomplete works with map keys

## Maintainability

- ✅ **Easier debugging** - Clear property names in errors
- ✅ **Simpler refactoring** - Add/remove entities without index changes
- ✅ **Self-documenting** - Code is more understandable

## Functionality

- ✅ **Heterogeneous datasets** - Multiple configs per suffix
- ✅ **No path concatenation** - Files are absolute Paths ready to use
- ✅ **Cloud storage** - Path objects work with S3, GCS, Azure
- ✅ **Process compatibility** - Path inputs work seamlessly

## Performance

- ✅ **Faster startup** - Plugin loads once, not per run
- ✅ **Better caching** - Nextflow can cache plugin dependencies
- ✅ **Optimized parsing** - Compiled Groovy code runs faster

---

# Summary: Quick Migration Checklist

## For Baseline → Beta.9 Users

- [ ] Add `plugins { id 'nf-bids@0.1.0-beta.9' }` to config
- [ ] Replace `bids2nf()` with `Channel.fromBIDS()`
- [ ] Update entity access: `item.meta.subject` not `item[0][0]`
- [ ] Update file access: `item.T1w.nii` not `file("${bidsDir}/${path}")`
- [ ] Update process inputs (usually no changes)
- [ ] Test with small dataset
- [ ] Run full test suite
- [ ] Consider new config features (`exclude_entities`, heterogeneous support)

## For Beta.1-5 → Beta.9 Users

- [ ] Update plugin version to beta.9
- [ ] Decide: Migrate to flat or opt-out with `flatten_output: false`
- [ ] If migrating: Update entity and file access patterns
- [ ] If using `combineBy`: Update to key extractor API
- [ ] Test thoroughly

## For Beta.6-8 (Legacy) → Beta.9 Users

- [ ] Update plugin version to beta.9
- [ ] Add `flatten_output: false` to keep legacy format (temporary)
- [ ] Plan migration to flat format (legacy will be removed in v0.2.0)
- [ ] OR: Migrate now using Path 1 instructions

## For Beta.6-8 (Flat) → Beta.9 Users

- [ ] Update plugin version to beta.9
- [ ] Remove `flatten_output: true` (now default)
- [ ] Test - should work unchanged!
- [ ] Optionally review new features

---

**Happy migrating! 🚀**

Need help? Check [Troubleshooting](#troubleshooting) or [open an issue](https://github.com/nf-neuro/nf-bids/issues).

```groovy
[
  [subject, session, run],  // Grouping key
  [
    data: [
      T1w: [nii: 'relative/path/T1w.nii.gz', json: 'relative/path/T1w.json']
    ],
    filePaths: ['relative/path/T1w.nii.gz', ...],
    bidsParentDir: '/absolute/path/to/dataset'
  ]
]
```

**New Structure (beta.6+):**
```groovy
[
  meta: [subject: 'sub-01', session: 'ses-01', run: 'NA'],
  T1w: [
    nii: Path('/absolute/path/to/dataset/sub-01/ses-01/anat/sub-01_ses-01_T1w.nii.gz'),
    json: Path('/absolute/path/to/dataset/sub-01/ses-01/anat/sub-01_ses-01_T1w.json')
  ]
]
```

#### Migration Steps

**1. Update Entity Access:**

```groovy
// OLD: Access by tuple index
def subject = item[0][0]
def session = item[0][1]
def run = item[0][2]

// NEW: Access by name
def subject = item.meta.subject
def session = item.meta.session
def run = item.meta.run
```

**2. Update File Path Access:**

```groovy
// OLD: Concatenate paths manually
def bidsDir = item[1].bidsParentDir
def t1wPath = item[1].data.T1w.nii
def t1wFile = file("${bidsDir}/${t1wPath}")

// NEW: Direct access (already absolute Path)
def t1wFile = item.T1w.nii  // Ready to use!
```

**3. Update Process Inputs:**

```groovy
// OLD: Convert to file in workflow
Channel.fromBIDS(params.bids_dir, params.config)
    .map { key, data ->
        def t1w = file("${data.bidsParentDir}/${data.data.T1w.nii}")
        return [key[0], t1w]  // [subject, file]
    }
    .set { t1w_channel }

process myProcess {
    input:
    tuple val(subject), path(t1w)
    // ...
}

// NEW: Use directly
Channel.fromBIDS(params.bids_dir, params.config)
    .map { item ->
        return [item.meta.subject, item.T1w.nii]
    }
    .set { t1w_channel }

process myProcess {
    input:
    tuple val(subject), path(t1w)  // Path works directly!
    // ...
}
```

**4. Handle Named Sets:**

```groovy
// OLD: Access named set groups
def apNii = file("${data.bidsParentDir}/${data.data.dwi.ap.nii}")
def paNii = file("${data.bidsParentDir}/${data.data.dwi.pa.nii}")

// NEW: Direct access (note: config key instead of file suffix)
def apNii = item.dwi_ap.ap.nii  // If config key is "dwi_ap"
def paNii = item.dwi_ap.pa.nii
```

**5. Handle Sequential Sets:**

```groovy
// OLD: Access sequential items
def echos = data.data.mese.collect { echo ->
    file("${data.bidsParentDir}/${echo.nii}")
}

// NEW: Direct access  
def echos = item.mese.collect { echo ->
    echo.nii  // Already a Path
}
```

#### Opt-Out (Temporary)

**Happy migrating! 🚀**

Need help? Check [Troubleshooting](#troubleshooting) or [open an issue](https://github.com/nf-neuro/nf-bids/issues).
