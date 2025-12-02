# nf-bids Plugin

> **BIDS dataset integration and closure-based channel operators for Nextflow workflows**

A Nextflow plugin that provides:
- **BIDS dataset parsing** through channel factories
- **Closure-based channel operators** for flexible data grouping and joining

[![nf-bids](https://img.shields.io/badge/nf&hyphen;bids-0.1.0&hyphen;beta.5-mediumseagreen)](https://registry.nextflow.io/plugins/nf-bids/0.1.0-beta.5)
[![Nextflow](https://img.shields.io/badge/nextflow-&geq;24.10.0-mediumseagreen)](https://www.nextflow.io/docs/latest/install.html)
[![libBIDS.sh](https://img.shields.io/badge/libBIDS.sh-schema&hyphen;guided-blue)](https://github.com/CoBrALab/libBIDS.sh/releases/tag/v1.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-78%20passing-success)]()
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)]()

---

## 🚀 Quick Start

### Prerequisites

- Bash (for [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh) integration)

### Installation

The plugin is officially published on the [Nextflow Plugins Registry](https://registry.nextflow.io/plugins/nf-bids).
To install it, add the lines below in your `nextflow.config` file:

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.5'
}
```

Then, include and use the operators provided by the plugin:

```groovy
include { fromBIDS } from 'plugin/nf-bids'
include { groupTupleBy; joinBy; combineBy } from 'plugin/nf-bids'

workflow {
    // Load BIDS dataset with flattened output (default)
    Channel.fromBIDS(
        '/path/to/bids/dataset',
        '/path/to/config.yaml'
    )
    .map { item ->
        // Access metadata through item.meta
        def subject = item.meta.subject
        def session = item.meta.session
        
        // Access data through top-level suffixes (absolute File paths)
        def t1w = item.T1w.nii    // No file() or bidsParentDir needed
        def json = item.T1w.json
        
        [subject, session, t1w, json]
    }
    .groupTupleBy { it[0] }  // Group by subject
    .view()
}
```

### Output Format Examples

The flattened output structure provides intuitive access to BIDS data:

**Plain Set (single file per suffix):**
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    T1w: [
        nii: file('/data/bids/sub-01/anat/sub-01_T1w.nii.gz'),
        json: file('/data/bids/sub-01/anat/sub-01_T1w.json')
    ]
]
// Access: item.meta.subject, item.T1w.nii
```

**Named Set (multiple acquisition directions):**
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    dwi: [
        ap: [
            nii: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz'),
            bval: file('/data/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval')
        ],
        pa: [
            nii: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz'),
            bval: file('/data/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval')
        ]
    ]
]
// Access: item.dwi.ap.nii, item.dwi.pa.bval
```

**Sequential Set (multiple runs):**
```groovy
[
    meta: [subject: 'sub-01', session: 'ses-01'],
    bold: [
        nii: [
            file('/data/bids/sub-01/func/sub-01_run-01_bold.nii.gz'),
            file('/data/bids/sub-01/func/sub-01_run-02_bold.nii.gz')
        ],
        json: [
            file('/data/bids/sub-01/func/sub-01_run-01_bold.json'),
            file('/data/bids/sub-01/func/sub-01_run-02_bold.json')
        ]
    ]
]
// Access: item.bold.nii[0], item.bold.nii.size()
```

**All file paths are absolute `File` objects** — no need for `file(bidsParentDir) /` constructions.

For legacy workflows, disable flattening: `Channel.fromBIDS(bids_dir, config, [flatten_output: false])`

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
|`options.flatten_output`|`boolean`|(Optional) When true (default), `Channel.fromBIDS()` emits flattened maps (with `meta` top-level key and top-level suffixes); when false, the original `[groupingKey, enrichedData]` tuples are emitted (legacy behavior).|

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

### For Users

- **[Channel Operators Guide](docs/channel-operators.md)** - Complete reference for groupTupleBy, joinBy, combineBy
- **[Performance Benchmark](docs/PERFORMANCE_BENCHMARK.md)** - ⚡ Performance comparison: closure-based vs built-in operators
- **[Closure Migration Guide](docs/CLOSURE_MIGRATION_GUIDE.md)** - Migrate from index-based to closure-based operators
- **[BIDS Migration Guide](docs/MIGRATION_GUIDE.md)** - Migrate from baseline bids2nf to plugin
- **[Configuration Guide](docs/configuration.md)** - Configure BIDS parsing and grouping
- **[Examples](docs/examples.md)** - Real-world usage examples

### For Contributors

- **[Plugin Status](STATUS.md)** - Current status and validation results ✅
- **[Contributing Guide](CONTRIBUTING.md)** - How to contribute to the plugin
- **[Development Setup](docs/development.md)** - Set up your development environment
- **[Architecture Overview](docs/architecture.md)** - Understand the plugin architecture
- **[Changelog](CHANGELOG.md)** - Development history and releases

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
