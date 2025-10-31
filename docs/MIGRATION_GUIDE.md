# Migration Guide: From Baseline bids2nf to nf-bids Plugin

This guide helps you migrate workflows from the original bids2nf Nextflow implementation to the new nf-bids plugin.

---

## Why Migrate?

The nf-bids plugin offers several advantages:

- ✅ **100% Compatible**: Produces identical outputs to the baseline
- ✅ **Cleaner Integration**: Standard Nextflow plugin installation
- ✅ **Better Maintained**: Modular codebase with comprehensive tests
- ✅ **Easier Updates**: Install new versions without workflow changes
- ✅ **Same Configuration**: Uses the same YAML configuration format

---

## Quick Migration

### Before (Baseline)

```groovy
// Include the bids2nf module
include { bids2nf } from './modules/bids2nf'

workflow {
    // Load BIDS data
    bids_channel = bids2nf(
        params.bids_dir,
        'bids2nf.yaml'
    )
    
    bids_channel.view()
}
```

### After (Plugin)

#### nextflow.config

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.2'
}
```

#### main.nf

```groovy
include { fromBIDS ] from 'plugins/nf-bids'

workflow {
    // Load BIDS data - same configuration file!
    bids_channel = Channel.fromBIDS(
        params.bids_dir,
        'bids2nf.yaml'
    )
    
    bids_channel.view()
}
```

---

## Step-by-Step Migration

### 1. Add Plugin to Configuration

Edit your `nextflow.config`:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.2'
}
```

Or specify the GitHub repository:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.2' from 'https://github.com/AlexVCaron/nf-bids'
}
```

### 2. Update Workflow Imports

**Remove** the old module include:

```groovy
// DELETE THIS:
include { bids2nf } from './modules/bids2nf'
include { bids2nf } from './subworkflows/bids2nf'
```

**Add** the channel factory:

```groovy
include { fromBIDS } from 'plugins/nf-bids'
```

### 3. Update Channel Creation

**Change** the function call:

```groovy
// OLD:
bids_channel = bids2nf(params.bids_dir, 'config.yaml')

// NEW:
bids_channel = Channel.fromBIDS(params.bids_dir, 'config.yaml')
```

### 4. Keep Your Configuration

**No changes needed!** The plugin uses the exact same YAML configuration format:

```yaml
# bids2nf.yaml - works with both baseline and plugin!
dwi:
  plain_set:
    additional_extensions:
      - .bval
      - .bvec

T1w:
  plain_set: {}

MP2RAGE:
  sequential_set:
    by_entity: "inversion"
    exclude_entities: ["reconstruction"]
```

---

## Configuration Compatibility

The plugin supports **all** baseline configuration options:

### Plain Sets
```yaml
dwi:
  plain_set:
    additional_extensions: [.bval, .bvec]
    include_cross_modal: true
```

### Named Sets
```yaml
MTS:
  named_set:
    T1w:
      flip: "flip-02"
      mtransfer: "mt-off"
    MTw:
      flip: "flip-01"
      mtransfer: "mt-on"
    PDw:
      flip: "flip-01"
      mtransfer: "mt-off"
  required: ["T1w", "MTw", "PDw"]
```

### Sequential Sets
```yaml
VFA:
  sequential_set:
    by_entity: "flip"
```

### Mixed Sets
```yaml
MPM:
  mixed_set:
    T1w:
      acquisition: "acq-T1w"
    MTw:
      acquisition: "acq-MTw"
    PDw:
      acquisition: "acq-PDw"
  sequential:
    by_entity: "echo"
```

### Advanced Options
```yaml
# Exclude specific entity variants
MP2RAGE:
  sequential_set:
    by_entity: "inversion"
    exclude_entities: ["reconstruction"]  # Exclude rec-dis2d files

# Map suffixes to other suffixes
dwi_fullreverse:
  suffix_maps_to: "dwi"
  named_set:
    fwd:
      direction: "dir-AP"
    rev:
      direction: "dir-PA"
  required: ["fwd", "rev"]
```

---

## Output Format

The plugin produces **identical output** to the baseline:

```groovy
[
    ["sub-01", "NA", "NA", "NA"],  // [subject, session, run, task]
    bidsParentDir: "/path/to/bids",
    subject: "sub-01",
    session: "NA",
    run: "NA",
    task: "NA",
    data: [
        dwi: [
            nii: "sub-01/dwi/sub-01_dwi.nii.gz",
            json: "sub-01/dwi/sub-01_dwi.json",
            bval: "sub-01/dwi/sub-01_dwi.bval",
            bvec: "sub-01/dwi/sub-01_dwi.bvec"
        ]
    ]
]
```

Your downstream processes need **no changes**!

---

## Testing Your Migration

### 1. Run Side-by-Side Comparison

Keep both versions temporarily:

```groovy
// Compare outputs
baseline_channel = bids2nf(params.bids_dir, 'config.yaml')
plugin_channel = Channel.fromBIDS(params.bids_dir, 'config.yaml')

baseline_channel.view { "BASELINE: $it" }
plugin_channel.view { "PLUGIN: $it" }
```

### 2. Use the Validation Scripts

The repository includes comparison tools:

```bash
# From tests/ directory
bash compare_baseline_plugin.sh

# View differences
cat comparison_reports/COMPARISON_INDEX.md
```

### 3. Run Your Existing Tests

Your workflow tests should pass without modification:

```bash
# If you have nf-test tests
nf-test test --profile test

# If you have integration tests
./run_integration_tests.sh
```

---

## Troubleshooting

### Plugin Not Found

**Error**: `Cannot find plugin 'nf-bids'`

**Solution**: Ensure `nextflow.config` has the plugin declaration:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.2'
}
```

### Channel Method Not Available

**Error**: `No signature of method: Channel.fromBIDS()`

**Solution**: 
1. Check plugin is properly installed: `nextflow plugin list`
2. Update Nextflow: `nextflow self-update`
3. Clear cache: `rm -rf .nextflow/`

### Different Output Structure

**Issue**: Output format seems different

**Check**:
1. Ensure you're using the same configuration file
2. Verify YAML indentation and structure
3. Check for typos in entity names

**Note**: The plugin produces **identical** outputs - if you see differences, it's likely a configuration issue.

---

## Rollback Plan

If you need to rollback temporarily:

### 1. Keep Baseline Code

Don't delete the old modules immediately:

```bash
# Keep these for now
git stash  # Stash your plugin changes
```

### 2. Comment Out Plugin

```groovy
// Temporarily disable
// plugins {
//     id 'nf-bids@0.1.0-beta.2'
// }
```

### 3. Restore Old Import

```groovy
include { bids2nf } from './modules/bids2nf'
```

---

## Benefits After Migration

Once migrated, you'll enjoy:

- 🚀 **Faster startup**: Plugin loads once, not on every run
- 🧹 **Cleaner codebase**: No module files to maintain
- 🔄 **Easy updates**: Just bump version number
- ✅ **Better tested**: 100% validation coverage
- 📚 **Better documented**: Comprehensive docs and examples

---

## Need Help?

- **Documentation**: See [docs/](../docs/) directory
- **Examples**: Check [docs/examples.md](examples.md)
- **Issues**: Report problems via GitHub issues
- **Questions**: Open a discussion on GitHub

---

## Version History

- **0.1.0-beta.2** (Oct 2025): 100% baseline alignment achieved
- **0.1.0** (Oct 2025): Initial plugin release

---

Happy migrating! 🚀
