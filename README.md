# nf-bids Plugin

> **BIDS dataset integration and closure-based channel operators for Nextflow workflows**

A Nextflow plugin that provides:
- **BIDS dataset parsing** through channel factories with **flat output format**
- **Heterogeneous dataset support** for mixed acquisition schemes
- **Closure-based channel operators** for flexible data grouping and joining

[![nf-bids](https://img.shields.io/badge/nf&hyphen;bids-0.1.0&hyphen;beta.10-mediumseagreen)](https://registry.nextflow.io/plugins/nf-bids@0.1.0-beta.10)
[![Nextflow](https://img.shields.io/badge/nextflow-&geq;24.10.0-mediumseagreen)](https://www.nextflow.io/docs/latest/install.html)
[![libBIDS.sh](https://img.shields.io/badge/libBIDS.sh-schema&hyphen;guided-blue)](https://github.com/CoBrALab/libBIDS.sh/releases/tag/v1.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-100%2B%20passing-success)]()
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)]()

📖 **Full documentation:** https://nf-neuro.github.io/nf-bids

---

## ✨ What's New in 0.1.0-beta.10

- 🎯 **Flat Output Format**: Simplified data structure with direct access to files and metadata
- 🔧 **Heterogeneous Dataset Support**: Multiple configs can now share the same file suffix
- 📦 **Type Safety**: All file paths are `java.nio.file.Path` objects, ready for process inputs
- 🚀 **Better Performance**: Optimized suffix mapping with candidate matching

**⚠️ Breaking Change:** The output format has changed. See [Migration Guide](docs/MIGRATION_GUIDE.md) for upgrade instructions.

---

## 🚀 Quick Start

### Prerequisites

- Bash (for [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh) integration)

### Installation

The plugin is officially published on the [Nextflow Plugins Registry](https://registry.nextflow.io/plugins/nf-bids).
To install it, add the lines below in your `nextflow.config` file:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.10'
}
```

Then, include and use the operators provided by the plugin:

```groovy
include { fromBIDS } from 'plugin/nf-bids'
include { groupTupleBy; joinBy; combineBy } from 'plugin/nf-bids'

workflow {
    // Load BIDS dataset with flat output (default in 0.1.0-beta.9+)
    Channel.fromBIDS(
        '/path/to/bids/dataset',
        '/path/to/config.yaml'
    )
    .map { item ->
        // Access metadata through item.meta (named entities)
        def subject = item.meta.subject
        def session = item.meta.session
        
        // Access data through top-level config keys
        // All paths are absolute Path objects - ready for process inputs!
        def t1w = item.T1w.nii
        def json = item.T1w.json
        
        [subject, session, t1w, json]
    }
    .groupTupleBy { it[0] }  // Group by subject
    .view()
}
```

### Output Format Examples

The flat output structure (default in 0.1.0-beta.9+) provides intuitive access to BIDS data:

**Plain Set (single file per suffix):**
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01', run: 'NA'],
    T1w: [
        nii: Path('/data/bids/sub-01/anat/sub-01_T1w.nii.gz'),
        json: Path('/data/bids/sub-01/anat/sub-01_T1w.json')
    ]
]
// Access: item.meta.subject, item.T1w.nii
```

**Named Set (multiple acquisition directions):**
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01', run: 'NA'],
    dwi_ap: [  // Config key, not file suffix
        ap: [
            nii: Path('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz'),
            bval: Path('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval'),
            bvec: Path('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec')
        ],
        pa: [
            nii: Path('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz'),
            bval: Path('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval'),
            bvec: Path('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec')
        ]
    ]
]
// Access: item.dwi_ap.ap.nii, item.dwi_ap.pa.bval
```

**Sequential Set (multiple echoes):**
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01', run: 'NA'],
    mese: [
        [  // Echo 1
            nii: Path('/data/bids/sub-01/anat/sub-01_echo-1_MESE.nii.gz'),
            json: Path('/data/bids/sub-01/anat/sub-01_echo-1_MESE.json')
        ],
        [  // Echo 2
            nii: Path('/data/bids/sub-01/anat/sub-01_echo-2_MESE.nii.gz'),
            json: Path('/data/bids/sub-01/anat/sub-01_echo-2_MESE.json')
        ]
    ]
]
// Access: item.mese[0].nii, item.mese.size()
```

**Key Features:**
- ✅ All file paths are absolute `java.nio.file.Path` objects
- ✅ Direct access to metadata through `item.meta.*`
- ✅ Config keys preserved (e.g., `dwi_ap` not collapsed to `dwi`)
- ✅ No path concatenation needed - paths are ready to use
- ✅ Compatible with Nextflow process `path` inputs

**Legacy Format:**  
For backward compatibility: `Channel.fromBIDS(bids_dir, config, [flatten_output: false])`

### API Reference

#### `Channel.fromBIDS(bids_dir, config, [options])`

Load and parse a BIDS dataset into a Nextflow channel.

|Parameter|Type|Description|
|-|-|-|
|`bids_dir`|`path-like`|Directory containing a valid [BIDS](https://bids-specification.readthedocs.io/en/stable/) input dataset.|
|`config`|`path-like`|Path to a `yaml` configuration file for entity parsing. See [configuration](docs/configuration.md).|
|`options.libbids_sh`|`path-like`|(Optional) Path to an alternative [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh) parsing script.|
|`options.validate`|`boolean`|(Not implemented) Run the [BIDS Validator](https://github.com/bids-standard/bids-validator) on the input dataset before parsing.|
|`options.validator_version`|`string`|(Not implemented) [BIDS Validator version](https://github.com/bids-standard/bids-validator/releases) to use.|
|`options.ignore_codes`|`path-like`|(Not implemented) [BIDS Validator](https://github.com/bids-standard/bids-validator) error codes to ignore.|
|`options.flatten_output`|`boolean`|When `true` (default in 0.1.0-beta.9+), emit flattened maps with `meta` and top-level config keys; when `false`, emit legacy `[groupingKey, enrichedData]` tuples.|

#### Closure-Based Channel Operators

The plugin provides three powerful operators for flexible channel manipulation:

**`groupTupleBy(keyExtractor, [options])`**  
Group channel items by dynamically extracted keys.

```groovy
channel
    .of([subject: 'sub-01', file: 'a.nii'],
        [subject: 'sub-01', file: 'b.nii'],
        [subject: 'sub-02', file: 'c.nii'])
    .groupTupleBy { it.subject }
// Output: ['sub-01', [[subject:'sub-01', file:'a.nii'], [subject:'sub-01', file:'b.nii']]]
//         ['sub-02', [[subject:'sub-02', file:'c.nii']]]
```

**`joinBy(rightChannel, keyExtractor, [options])`**  
Join two channels by dynamically extracted keys.

```groovy
anatomical
    .joinBy(functional) { it.subject }
// Matches items from both channels by subject field
```

**`combineBy(rightChannel, leftKeyExtractor, [rightKeyExtractor], [options])`**  
Combine channels by extracting keys from left/right items and emitting `[key, leftItem, rightItem]` tuples. Items are matched by key, with cartesian product within each key group.

```groovy
// Match subjects with their sessions by subject ID
subjects = Channel.of([id: 'sub-01', age: 25], [id: 'sub-02', age: 30])
sessions = Channel.of([id: 'sub-01', session: 'ses-01'], 
                      [id: 'sub-01', session: 'ses-02'],
                      [id: 'sub-02', session: 'ses-01'])

subjects
    .combineBy(sessions, { it.id })
    .filter { key, subj, sess ->
        // Custom filtering logic
        subj.age >= 18
    }
// Produces: [sub-01, [id:sub-01, age:25], [id:sub-01, session:ses-01]]
//           [sub-01, [id:sub-01, age:25], [id:sub-01, session:ses-02]]
//           [sub-02, [id:sub-02, age:30], [id:sub-02, session:ses-01]]
```

**See:** [Channel Operators Documentation](docs/channel-operators.md) for complete reference

---

## 📖 Documentation

The full documentation is published at **https://nf-neuro.github.io/nf-bids** and covers:

| Topic | Site section |
|-------|-------------|
| Installation & quick start | [Installation Guide](https://nf-neuro.github.io/nf-bids/guides/installation.html) |
| Architecture & data flow | [Architecture Overview](https://nf-neuro.github.io/nf-bids/architecture.html) |
| Configuration YAML reference | [Configuration](https://nf-neuro.github.io/nf-bids/concepts/configuration.html) |
| Output format (flat / legacy) | [Output Shaping](https://nf-neuro.github.io/nf-bids/concepts/output-shaping.html) |
| Channel operators reference | [Channel Operators](https://nf-neuro.github.io/nf-bids/concepts/channel-operators.html) |
| Migrate from bids2nf / older betas | [BIDS Migration Guide](https://nf-neuro.github.io/nf-bids/appendices/migration-bids.html) |
| Migrate from index-based operators | [Closure Operators Migration](https://nf-neuro.github.io/nf-bids/appendices/migration-closure.html) |
| API reference (GroovyDoc) | [API Reference](https://nf-neuro.github.io/nf-bids/api-reference.html) |

For contributors:

| Topic | Link |
|-------|------|
| Contributing guide | [CONTRIBUTING.md](CONTRIBUTING.md) |
| Development setup | [docs/development.md](docs/development.md) |
| Changelog | [CHANGELOG.md](CHANGELOG.md) |

---

## 🎯 Features

### BIDS Dataset Support
✅ **Native BIDS Support** - Direct integration with BIDS datasets  
✅ **Channel Factory Pattern** - Async execution with `Channel.fromBIDS()`  
✅ **Flexible Grouping** - Plain, named, sequential, and mixed set types  
✅ **Cross-Modal Broadcasting** - Share anatomical data across modalities  
✅ **libBIDS.sh Integration** - Leverages battle-tested BIDS parsing  

### Closure-Based Channel Operators
✅ **Semantic Grouping** - `groupTupleBy { it.subject }` vs `groupTuple(by: 0)`  
✅ **Flexible Joins** - `joinBy(right, { it.key })` with any data structure  
✅ **Key-Based Combinations** - `combineBy(right, { it.id })` with cartesian products  
✅ **Composite Keys** - `groupTupleBy { "${it.subject}_${it.session}" }` without extra steps  
✅ **Competitive Performance** - ~10-30ms overhead, sub-200ms for typical BIDS workflows ([benchmark](docs/PERFORMANCE_BENCHMARK.md))  

### Quality & Reliability
✅ **Type-Safe** - Full @CompileStatic support with proper type checking  
✅ **Thread-Safe** - Validated under concurrent load (10k items)  
✅ **Comprehensive Tests** - 78 tests passing (unit + integration + edge cases)  
✅ **100% Compatibility** - Validated against bids2nf baseline  

---

### Key Technologies

- **Nextflow**: 25.10.0 (plugin framework)
- **Groovy**: 4.0.23 (with @CompileStatic)
- **GPars**: DataflowQueue for async channels
- **Gradle**: 8.14 (build system)
- **Spock**: Testing framework

---

## 🤝 Contributing

We welcome contributions! Here's how to get started:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes** and add tests
4. **Run the test suite**: `./gradlew test`
5. **Run the nf-test suite**: `nf-test test validation/`
6. **Update snapshots** (optional) : `nf-test test validation/ --update-snapshot`
7. **Commit your changes**: `git commit -m 'Add amazing feature'`
8. **Push to your branch**: `git push origin feature/amazing-feature`
9. **Open a Pull Request**

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

### Need help?

- Check the [documentation](docs/)
- Review [closed issues](https://github.com/nf-neuro/nf-bids/issues?q=is%3Aissue+is%3Aclosed)
- Open a new issue with details

---

## 📚 Additional Resources

- **[BIDS Specification](https://bids-specification.readthedocs.io/)** - Official BIDS docs
- **[Nextflow Plugins](https://www.nextflow.io/docs/latest/plugins.html)** - Plugin development guide
- **[libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh)** - BIDS parsing library

---

## 🙏 Acknowledgments

- Nextflow team for the plugin framework
- BIDS community for the specification
- @agahkarakuzu for the initial [bids2nf](https://github.com/agahkarakuzu/bids2nf) implementation
- @gdevenyi and the @CoBrALab for the [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh) bash parser

---

**Made with ❤️ for the neuroimaging community**
