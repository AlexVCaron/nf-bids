# Contributing to nf-bids

Thank you for your interest in contributing to the nf-bids plugin! This guide will help you get started.

---

## 🎯 Ways to Contribute

- 🐛 **Report bugs** - Found an issue? Let us know!
- 💡 **Suggest features** - Have an idea? We'd love to hear it!
- 📝 **Improve documentation** - Help others understand the plugin
- 🧪 **Add tests** - Increase code coverage and reliability
- 🔧 **Fix bugs** - Help us improve the plugin
- ✨ **Add features** - Contribute new functionality

---

## 🚀 Getting Started

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then:
git clone https://github.com/YOUR_USERNAME/nf-bids.git
cd nf-bids/
```

### 2. Set Up Development Environment

**Prerequisites**:
- Java 11 or later
- [Nextflow 23.10.0](https://nextflow.io) or later
- Gradle 8.14 (included via wrapper)
- Bash (for [libBIDS.sh](https://github.com/CoBrALab/libBIDS.sh))

**Verify setup**:
```bash
java -version
nextflow -version
./gradlew --version
```

### 3. Build and Test

```bash
# Build the plugin
make assemble

# Run tests
./gradlew test

# Install to local Nextflow
make install

# Test with a real workflow
cd validation/
nextflow run main.nf
```

---

## 📋 Development Workflow

### Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### Make Your Changes

1. **Write code** following [guidelines](#groovy-style-guidelines)
2. **[Add tests](#unit-tests)** for new functionality
4. **Update documentation** as needed
5. **Run tests** to ensure everything works

### Commit Your Changes

Use clear, descriptive commit messages, e.g. support for a derivative :

```bash
git add .
git commit -m "Add support for BIDS derivatives

- Implement derivatives path parsing
- Add tests for derivatives handling
- Update documentation with examples"
```

>[!NOTE]
>In a `bash` terminal, if a string `"` is not quoted by its sibling on the same row,
>next lines (pressing `enter`) will be considered as part of it, until a line with a
>_non-escaped_ `"` is met.

**Commit Message Format**:
- First line: Brief summary (50 chars or less)
- Blank line
- Detailed description (if needed)
- List specific changes

### Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request on GitHub with:
- Clear description of changes
- Reference to related issues
- Screenshots/examples (if applicable)

---

## 🧪 Testing Guidelines

### Unit Tests

All new functionality must have unit tests in `src/test/groovy/nfneuro`.

**Test Philosophy**:
- Keep tests minimal (< 10 per class)
- Test public API, not implementation
- Use clear, descriptive test names
- Avoid complex mocking

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests MyNewFeatureTest
```

### Integration Tests

Test with real BIDS datasets:

```bash
cd validation/
nextflow run main.nf
```

>[!NOTE]
>The base validation test might not catch outputs produced by your changes, but it's
>a good sanity test that other stuff still works. It is by no means an insurance of
>their validity.

Then, look in the **nf-test** test cases, if one could be improved to cover your changes. Those
tests cover the full length of [bids-datasets](https://bids.neuroimaging.io/datasets/examples.html),
using one of the many [test configurations](validation/configs), plus a few more complex
[use-cases](validation/data/custom). Run the full suite :

```bash
nf-test test validation/
```

>[!IMPORTANT]
>If you changed **nf-test** test cases, then you need to run those specific suites with `--update-snapshot`,
>then re-run the full suite, to see if you affected any other cases.

---

## 📝 Code Style

### Groovy Style Guidelines

```groovy
// Use @CompileStatic for type safety
@CompileStatic
class MyClass {
    
    // Clear, descriptive names
    private String descriptiveName
    
    // Document public methods
    /**
     * Does something useful
     * 
     * @param input The input parameter
     * @return The result
     */
    String doSomething(String input) {
        // Implementation
    }
    
    // Use early returns for clarity
    boolean validate(Object obj) {
        if (!obj) return false
        if (!(obj instanceof String)) return false
        return true
    }
}
```

### Key Conventions

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 100 characters (soft limit)
- **Naming**:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Imports**: Organized, no wildcards
- **Comments**: Clear JavaDoc for public APIs

### Type Safety

Use explicit types with @CompileStatic:

```groovy
// ✅ Correct
def itemList = item as List
def dataMap = itemList[1] as Map

// ❌ Avoid
def x = item[1]
```

---

## 📚 Documentation

### Update Documentation

When adding features, update relevant docs:

- [README.md](README.md) - For user-facing changes
- [API docs](docs/api.md) - For new public methods
- [Examples](docs/examples.md) - For new use cases
- [Configuration](docs/configuration.md) - For configuration changes
- [Architecture docs](docs/architecture.md) - For structural changes

---

## 🔍 Pull Request Checklist

Before submitting your PR, ensure:

- [ ] Code follows style guidelines
- [ ] All tests pass (`./gradlew test`)
- [ ] Validation tests pass
- [ ] New functionality has tests
- [ ] Documentation is updated
- [ ] Commit messages are clear
- [ ] No merge conflicts with main
- [ ] PR description explains changes

---

## 🐛 Reporting Bugs

### Before Reporting

1. Check [existing issues](https://github.com/nf-neuro/nf-bids/issues)
2. Verify it's not a configuration issue
3. Test with the latest version

### Bug Report Template

```markdown
**Description**
Clear description of the bug

**To Reproduce**
Steps to reproduce:
1. Run command X
2. With configuration Y
3. See error Z

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Environment**
- OS: [e.g., Ubuntu 22.04]
- Nextflow version: [e.g., 25.10.0]
- Plugin version: [e.g., 0.1.0]
- Java version: [e.g., 11]

**Additional Context**
- Error messages
- Logs
- Screenshots
```

---

## 💡 Feature Requests

### Proposing New Features

1. **Check existing issues** for similar requests
2. **Explain the use case** - Why is this needed?
3. **Describe the solution** - How should it work?
4. **Consider alternatives** - What other approaches exist?

### Feature Request Template

```markdown
**Problem Statement**
What problem does this solve?

**Proposed Solution**
How should this feature work?

**Example Usage**
```groovy
// Code example of how you'd use it
```

**Alternatives Considered**
What other solutions did you consider?

**Additional Context**
Any other relevant information
```

---

## 🏗️ Architecture Guidelines

### Plugin Structure

```
src/main/groovy/nfneuro/plugin/
├── channel/       # Channel factory & handlers
├── config/        # Configuration loading
├── grouping/      # Set type handlers
├── model/         # Data models
├── parser/        # BIDS parsing
├── plugin/        # Plugin lifecycle
└── util/          # Utilities
```

### Design Principles

1. **Separation of Concerns** - Each class has single responsibility
2. **Async Execution** - Use session.addIgniter for non-blocking ops
3. **Type Safety** - Prefer @CompileStatic with explicit types
4. **Testability** - Design for easy testing
5. **Clear APIs** - Public methods are well-documented

### Adding New Set Types

If adding a new set handler:

1. Extend `BaseSetHandler`
2. Implement `process()` method
3. Add configuration parsing
4. Write unit tests
5. Add integration test
6. Update documentation

---

## 📞 Getting Help

- **Questions**: Open a [discussion](https://github.com/AlexVCaron/nf-bids/discussions)
- **Bugs**: Create an [issue](https://github.com/AlexVCaron/nf-bids/issues)

---

## 🎓 Learning Resources

### Nextflow Plugin Development

- [Nextflow Plugin Documentation](https://www.nextflow.io/docs/latest/plugins.html)
- [Plugin Template](https://github.com/nextflow-io/nf-hello)
- [nf-sqldb](https://github.com/nextflow-io/nf-sqldb) - Reference implementation

### BIDS Specification

- [BIDS Specification](https://bids-specification.readthedocs.io/)
- [BIDS Examples](https://github.com/bids-standard/bids-examples)
- [BIDS Starter Kit](https://bids-standard.github.io/bids-starter-kit/)

### Groovy & Testing

- [Groovy Documentation](https://groovy-lang.org/documentation.html)
- [Spock Framework](https://spockframework.org/)
- [CompileStatic](https://docs.groovy-lang.org/latest/html/gapi/groovy/transform/CompileStatic.html)

---

## 📜 Code of Conduct

### Our Standards

- **Be respectful** and inclusive
- **Be patient** with newcomers
- **Be constructive** in feedback
- **Focus on** what's best for the community

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Publishing private information
- Other unprofessional conduct

---

## 🙏 Recognition

Thank you for contributing to nf-bids! We'll try to acknowlegde you every time we can 🎉

---

## 📝 License

By contributing, you agree that your contributions will be licensed under the MIT License.
