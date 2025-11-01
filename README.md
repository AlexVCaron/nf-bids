# nf-bids Plugin

> **BIDS dataset integration for Nextflow workflows**

A Nextflow plugin that provides native BIDS (Brain Imaging Data Structure) dataset support through channel factories, enabling seamless integration of neuroimaging data into Nextflow pipelines.

[![nf-bids](https://img.shields.io/badge/nf&hyphen;bids-0.1.0&hyphen;beta.2-mediumseagreen)](https://registry.nextflow.io/plugins/nf-bids/0.1.0-beta.2)
[![Nextflow](https://img.shields.io/badge/nextflow-&geq;23.x-mediumseagreen)](https://www.nextflow.io/docs/latest/install.html)
[![libBIDS.sh](https://img.shields.io/badge/libBIDS.sh-schema&hyphen;guided-blue)](https://github.com/CoBrALab/libBIDS.sh/releases/tag/v1.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Validation](https://img.shields.io/badge/bids2nf&hyphen;baseline-100%25-success)](https://github.com/agahkarakuzu/bids2nf)
[![License](https://img.shields.io/badge/license-MIT-blue)]()

---

## 🚀 Quick Start

### Prerequisites

- Bash (for [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh) integration)

### Installation

The plugin is officially published on the [Nextflow Plugins Registry](https://registry.nextflow.io/plugins/nf-bids).
To install it, add the lines below in your `nextflow.config` file :

```groovy
plugins {
    id 'nf-bids@0.1.0-beta.2'
}
```

Then, include and use the `fromBIDS` channel factory provided by the plugin :

```groovy
include { fromBIDS } from 'plugin/nf-bids'

workflow {
    // Load BIDS dataset
    Channel.fromBIDS(
        '/path/to/bids/dataset',
        '/path/to/config.yaml'
    )
    .view()
}
```

### `Channel.fromBIDS(bids_dir,config,[options])`

|input|type|description|
|-|-|-|
|`bids_dir`|`path-like`|Directory containing a valid [BIDS](https://bids-specification.readthedocs.io/en/stable/) input dataset.|
|`config`|`path-like`|Path to a `yaml` configuration file for entity parsing. See [configuration](docs/configuration.md).|
|`options.libbids_sh`|`path-like`|(Optional) Path to an alternative [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh) parsing script.|
|`options.validate`|`boolean`|(Not implement) Run the [BIDS Validator](https://github.com/bids-standard/bids-validator) on the input dataset before parsing.|
|`options.validator_version`|`string`|(Not implemented) [BIDS Validator version](https://github.com/bids-standard/bids-validator/releases) to use.|
|`options.ignore_codes`|`path-like`|(Not implemented) [BIDS Validator](https://github.com/bids-standard/bids-validator) error codes to ignore.|

---

## 📖 Documentation

### For Users

- **[Migration Guide](docs/MIGRATION_GUIDE.md)** - Migrate from baseline bids2nf
- **[Configuration Guide](docs/configuration.md)** - Configure BIDS parsing and grouping
- **[Examples](docs/examples.md)** - Real-world usage examples

### For Contributors

- **[Plugin Status](STATUS.md)** - Current status and validation results ✅
- **[Contributing Guide](CONTRIBUTING.md)** - How to contribute to the plugin
- **[Development Setup](docs/development.md)** - Set up your development environment
- **[Architecture Overview](docs/architecture.md)** - Understand the plugin architecture
- **[API Reference](docs/api.md)** - Complete API documentation
- **[Testing Guide](docs/TEST_SUITE.md)** - Run and write tests
- **[Changelog](CHANGELOG.md)** - Development history and releases

---

## 🎯 Features

✅ **Native BIDS Support** - Direct integration with BIDS datasets  
✅ **Channel Factory Pattern** - Async execution with `Channel.fromBIDS()`  
✅ **Flexible Grouping** - Plain, named, sequential, and mixed set types  
✅ **Cross-Modal Broadcasting** - Share anatomical data across modalities  
✅ **Type-Safe** - Full @CompileStatic support with proper type checking  
✅ **libBIDS.sh Integration** - Leverages battle-tested BIDS parsing  
✅ **Comprehensive Tests** - 30+ unit tests with 100% success rate  

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
- Review [closed issues](https://github.com/AlexVCaron/bids2nf/issues?q=is%3Aissue+is%3Aclosed)
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
