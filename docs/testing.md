# Testing Guide

Comprehensive testing strategies and validation for the nf-bids plugin.

## Table of Contents

- [Testing Strategy](#testing-strategy)
- [Unit Tests](#unit-tests)
- [Integration Tests](#integration-tests)
- [Validation Tests](#validation-tests)
- [Test Data](#test-data)
- [Continuous Integration](#continuous-integration)

## Testing Strategy

### Test Pyramid

```
                    /\
                   /  \
                  /    \
                 / E2E  \          ← Integration/Validation
                /--------\
               /          \
              /   Unit     \       ← Unit Tests
             /--------------\
            /    Component   \     ← Component Tests
           /------------------\
```

### Testing Levels

1. **Unit Tests**: Individual classes and methods
2. **Component Tests**: Module interactions (config + parser)
3. **Integration Tests**: Full workflow with Nextflow
4. **Validation Tests**: Real BIDS datasets

## Unit Tests

### Framework: Spock

Located in `src/test/groovy/nextflow/bids/`

### Running Unit Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests BidsPluginTest

# Specific test method
./gradlew test --tests "BidsPluginTest.should create plugin instance"

# With detailed output
./gradlew test --info

# View HTML report
open build/reports/tests/test/index.html
```

### Test Structure

```groovy
package nextflow.bids.channel

import spock.lang.Specification
import spock.lang.Unroll
import nextflow.Session

class BidsChannelFactoryTest extends Specification {
    
    def setup() {
        // Runs before each test
    }
    
    def cleanup() {
        // Runs after each test
    }
    
    def setupSpec() {
        // Runs once before all tests
    }
    
    def cleanupSpec() {
        // Runs once after all tests
    }
    
    def "should create channel from BIDS directory"() {
        given: "a valid BIDS directory"
        def bidsDir = '/path/to/test/bids'
        def factory = new BidsChannelFactory()
        
        when: "creating a channel"
        def channel = factory.fromBIDS(bidsDir)
        
        then: "channel should be created"
        channel != null
        channel instanceof DataflowVariable
    }
    
    @Unroll
    def "should handle #scenario"() {
        given:
        def factory = new BidsChannelFactory()
        
        when:
        factory.fromBIDS(input)
        
        then:
        thrown(expectedException)
        
        where:
        scenario              | input              | expectedException
        "null directory"      | null               | IllegalArgumentException
        "empty directory"     | ""                 | IllegalArgumentException
        "missing directory"   | "/nonexistent"     | FileNotFoundException
    }
}
```

### Current Test Suite

**Status**: ✅ 29 tests passing (100% success rate)

**Test Files** (in `src/test/groovy/nfneuro/plugin/`):
1. BidsObserverTest.groovy (1 test)
2. BidsChannelFactoryTest.groovy (2 tests)
3. BidsConfigLoaderTest.groovy (4 tests) 
4. BidsConfigAnalyzerTest.groovy (5 tests)
5. BidsParserTest.groovy (2 tests)
6. BidsFileTest.groovy (6 tests)
7. BidsDatasetTest.groovy (3 tests)
8. PlainSetHandlerTest.groovy (3 tests)
9. NamedSetHandlerTest.groovy (1 test)
10. SequentialSetHandlerTest.groovy (1 test)
11. MixedSetHandlerTest.groovy (1 test)

See [TEST_SUITE.md](TEST_SUITE.md) for complete documentation.

### Example Test Structure

#### BidsPlugin

```groovy
// src/test/groovy/nfneuro/plugin/BidsObserverTest.groovy

class BidsPluginTest extends Specification {
    
    def "should load plugin"() {
        given:
        def plugin = new BidsPlugin()
        
        when:
        plugin.start()
        
        then:
        notThrown(Exception)
    }
    
    def "should register extension"() {
        given:
        def plugin = new BidsPlugin()
        
        when:
        def extensions = plugin.getExtensionClasses()
        
        then:
        extensions.contains(BidsExtension)
    }
}
```

#### BidsExtension

```groovy
// src/test/groovy/nextflow/bids/BidsExtensionTest.groovy

class BidsExtensionTest extends Specification {
    
    def "should register fromBIDS method"() {
        given:
        def extension = new BidsExtension()
        
        when:
        def methods = extension.getMethods()
        
        then:
        methods.fromBIDS != null
    }
    
    def "should create channel factory"() {
        given:
        def extension = new BidsExtension()
        
        when:
        def factory = extension.getChannelFactory()
        
        then:
        factory instanceof BidsChannelFactory
    }
}
```

#### Configuration

```groovy
// src/test/groovy/nextflow/bids/config/BidsConfigLoaderTest.groovy

class BidsConfigLoaderTest extends Specification {
    
    def "should load valid configuration"() {
        given:
        def configFile = createTempConfig("""
            loop_over:
              - subject
            T1w:
              plain_set:
                entities:
                  suffix: T1w
        """)
        
        when:
        def config = BidsConfigLoader.load(configFile)
        
        then:
        config.loop_over == ['subject']
        config.T1w.plain_set != null
    }
    
    def "should validate against schema"() {
        given:
        def configFile = createTempConfig("""
            invalid_key: value
        """)
        
        when:
        BidsConfigLoader.load(configFile)
        
        then:
        thrown(InvalidConfigurationException)
    }
    
    private File createTempConfig(String content) {
        def file = File.createTempFile("test-config", ".yaml")
        file.text = content
        file.deleteOnExit()
        return file
    }
}
```

#### Parser

```groovy
// src/test/groovy/nextflow/bids/parser/BidsParserTest.groovy

class BidsParserTest extends Specification {
    
    def "should parse BIDS entities from filename"() {
        given:
        def filename = "sub-01_ses-baseline_task-rest_bold.nii.gz"
        
        when:
        def entities = BidsParser.parseEntities(filename)
        
        then:
        entities.subject == "01"
        entities.session == "baseline"
        entities.task == "rest"
        entities.suffix == "bold"
    }
    
    def "should discover BIDS files"() {
        given:
        def bidsDir = createTestBidsDataset()
        
        when:
        def files = BidsParser.discoverFiles(bidsDir)
        
        then:
        files.size() > 0
        files.every { it instanceof BidsFile }
    }
}
```

#### Grouping Handlers

```groovy
// src/test/groovy/nextflow/bids/grouping/PlainSetHandlerTest.groovy

class PlainSetHandlerTest extends Specification {
    
    def "should handle plain set"() {
        given:
        def handler = new PlainSetHandler()
        def files = [
            createBidsFile("sub-01_T1w.nii.gz")
        ]
        def config = [
            entities: [suffix: "T1w"]
        ]
        
        when:
        def result = handler.handle(files, config)
        
        then:
        result instanceof String
        result.endsWith("T1w.nii.gz")
    }
}

// src/test/groovy/nextflow/bids/grouping/SequentialSetHandlerTest.groovy

class SequentialSetHandlerTest extends Specification {
    
    def "should order by sequence entity"() {
        given:
        def handler = new SequentialSetHandler()
        def files = [
            createBidsFile("sub-01_echo-3_bold.nii.gz"),
            createBidsFile("sub-01_echo-1_bold.nii.gz"),
            createBidsFile("sub-01_echo-2_bold.nii.gz")
        ]
        def config = [
            entities: [suffix: "bold"],
            sequence_by: "echo"
        ]
        
        when:
        def result = handler.handle(files, config)
        
        then:
        result instanceof List
        result.size() == 3
        result[0].contains("echo-1")
        result[1].contains("echo-2")
        result[2].contains("echo-3")
    }
}
```

### Mocking and Stubbing

```groovy
import spock.lang.Specification

class BidsChannelFactoryTest extends Specification {
    
    def "should use mocked parser"() {
        given: "a mocked parser"
        def mockParser = Mock(BidsParser)
        mockParser.discoverFiles(_) >> [
            createBidsFile("sub-01_T1w.nii.gz")
        ]
        
        and: "factory with mocked parser"
        def factory = new BidsChannelFactory()
        factory.parser = mockParser
        
        when:
        def channel = factory.fromBIDS("/fake/path")
        
        then:
        1 * mockParser.discoverFiles("/fake/path")
        channel != null
    }
}
```

## Integration Tests

### Framework: Nextflow Test

Located in `validation/test.nf`

### Running Integration Tests

```bash
cd validation

# Basic test
nextflow run test.nf -plugins nf-bids@0.1.0

# With test dataset
nextflow run test.nf \
    -plugins nf-bids@0.1.0 \
    --bids_dir ../../../tests/data/bids-examples/asl001 \
    --config ../config/test.yaml

# With all test datasets
./run_all_tests.sh
```

### Test Workflow

```groovy
// validation/test.nf

#!/usr/bin/env nextflow

nextflow.enable.dsl=2

params.bids_dir = 'test-data/asl001'
params.config = 'test-config.yaml'
params.expected_outputs = 'expected-outputs'

workflow {
    // Create BIDS channel
    Channel.fromBIDS(
        params.bids_dir,
        params.config
    )
    .set { bids_ch }
    
    // Validate outputs
    validateOutputs(bids_ch)
    
    // Compare with expected
    compareWithExpected(
        validateOutputs.out,
        params.expected_outputs
    )
}

process validateOutputs {
    input:
    tuple val(key), val(data)
    
    output:
    path "output_*.json"
    
    script:
    def subject = data.subject
    def session = data.session ?: "NA"
    def run = data.run ?: "NA"
    def task = data.task ?: "NA"
    
    """
    cat > output_${subject}_${session}_${run}_${task}.json <<EOF
    {
        "subject": "${subject}",
        "session": "${session}",
        "run": "${run}",
        "task": "${task}",
        "data": ${groovy.json.JsonOutput.toJson(data.data)}
    }
    EOF
    """
}

process compareWithExpected {
    publishDir 'results', mode: 'copy'
    
    input:
    path outputs
    path expected
    
    output:
    path "comparison_report.json"
    
    script:
    """
    #!/usr/bin/env python3
    import json
    import sys
    from pathlib import Path
    
    outputs = list(Path('.').glob('output_*.json'))
    expected = list(Path('${expected}').glob('*.json'))
    
    results = {
        'total': len(outputs),
        'matched': 0,
        'mismatched': 0,
        'missing': 0,
        'details': []
    }
    
    for out_file in outputs:
        with open(out_file) as f:
            out_data = json.load(f)
        
        # Find matching expected file
        exp_file = Path('${expected}') / out_file.name
        
        if not exp_file.exists():
            results['missing'] += 1
            results['details'].append({
                'file': out_file.name,
                'status': 'missing'
            })
            continue
        
        with open(exp_file) as f:
            exp_data = json.load(f)
        
        if out_data == exp_data:
            results['matched'] += 1
            results['details'].append({
                'file': out_file.name,
                'status': 'matched'
            })
        else:
            results['mismatched'] += 1
            results['details'].append({
                'file': out_file.name,
                'status': 'mismatched',
                'differences': compare_dicts(out_data, exp_data)
            })
    
    with open('comparison_report.json', 'w') as f:
        json.dump(results, f, indent=2)
    
    if results['mismatched'] > 0 or results['missing'] > 0:
        sys.exit(1)
    
    def compare_dicts(d1, d2, path=""):
        diffs = []
        all_keys = set(d1.keys()) | set(d2.keys())
        for key in all_keys:
            if key not in d1:
                diffs.append(f"{path}.{key}: missing in output")
            elif key not in d2:
                diffs.append(f"{path}.{key}: extra in output")
            elif d1[key] != d2[key]:
                if isinstance(d1[key], dict) and isinstance(d2[key], dict):
                    diffs.extend(compare_dicts(d1[key], d2[key], f"{path}.{key}"))
                else:
                    diffs.append(f"{path}.{key}: {d1[key]} != {d2[key]}")
        return diffs
    """
}
```

### Test Datasets

Use datasets from main bids2nf project:

```bash
# Link test data
ln -s ../../../tests/data/bids-examples validation/test-data

# Available datasets:
# - asl001, asl002          (ASL)
# - eeg_cbm                 (EEG)
# - qmri_irt1, qmri_megre   (qMRI)
# - ds-dwi, ds-dwi2, etc.   (DWI)
```

### Expected Outputs

Located in `validation/expected-outputs/`

Structure mirrors test datasets:
```
expected-outputs/
├── asl001/
│   └── sub-Sub103_NA_NA_NA_unified.json
├── ds-dwi/
│   └── sub-01_NA_NA_NA_unified.json
└── ...
```

## Validation Tests

### BIDS Validator Integration

```groovy
// validation/bids-validator.nf

workflow {
    Channel.fromPath(params.bids_datasets)
        .set { datasets }
    
    runBidsValidator(datasets)
    
    Channel.fromBIDS(
        datasets,
        params.config,
        [bids_validation: true]
    )
    .set { validated_ch }
}

process runBidsValidator {
    container 'bids/validator:latest'
    
    input:
    path bids_dir
    
    output:
    path "validation_report.json"
    
    script:
    """
    bids-validator ${bids_dir} --json > validation_report.json
    """
}
```

### Cross-Modal Broadcasting Validation

```groovy
// validation/cross-modal-test.nf

workflow {
    Channel.fromBIDS(
        params.bids_dir,
        params.config
    )
    .set { bids_ch }
    
    // Verify T1w is shared across tasks
    verifyCrossModal(bids_ch)
}

process verifyCrossModal {
    input:
    tuple val(key), val(data)
    
    script:
    """
    #!/usr/bin/env python3
    
    # Check that task-independent data (T1w) is present
    # in task-specific channels
    
    assert '${data.task}' in ['rest', 'nback'], "Unknown task"
    assert '${data.data.T1w}', "T1w missing in ${data.task} task"
    
    # Verify same T1w across all tasks
    print(f"Task ${data.task}: T1w present")
    """
}
```

## Test Data

### Creating Test Fixtures

```groovy
// src/test/groovy/nextflow/bids/BidsTestUtils.groovy

class BidsTestUtils {
    
    static File createTestBidsDataset() {
        def tmpDir = Files.createTempDirectory("test-bids").toFile()
        tmpDir.deleteOnExit()
        
        // Create BIDS structure
        new File(tmpDir, "dataset_description.json").text = '''
        {
            "Name": "Test Dataset",
            "BIDSVersion": "1.8.0"
        }
        '''
        
        // Create subject directory
        def subDir = new File(tmpDir, "sub-01")
        subDir.mkdirs()
        
        // Create anatomical
        def anatDir = new File(subDir, "anat")
        anatDir.mkdirs()
        new File(anatDir, "sub-01_T1w.nii.gz").text = "fake nifti"
        
        // Create functional
        def funcDir = new File(subDir, "func")
        funcDir.mkdirs()
        new File(funcDir, "sub-01_task-rest_bold.nii.gz").text = "fake nifti"
        
        return tmpDir
    }
    
    static BidsFile createBidsFile(String filename) {
        def file = new BidsFile()
        file.path = "/fake/path/${filename}"
        file.filename = filename
        file.entities = BidsParser.parseEntities(filename)
        return file
    }
    
    static File createTestConfig(String yaml) {
        def file = File.createTempFile("test-config", ".yaml")
        file.text = yaml
        file.deleteOnExit()
        return file
    }
}
```

### Test Data Repository

Shared test data location:
```
tests/data/
├── bids-examples/      # From BIDS examples
├── custom/             # Custom test datasets
│   ├── ds-dwi/
│   ├── ds-dwi2/
│   ├── ds-mrs_fmrs/
│   └── ds-mtsat/
└── expected_outputs/   # Expected results
```

## Continuous Integration

### GitHub Actions

```yaml
# .github/workflows/test.yml

name: Tests

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
        with:
          submodules: recursive
      
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle') }}
      
      - name: Run unit tests
        run: ./gradlew test
      
      - name: Publish test report
        uses: mikepenz/action-junit-report@v3
        if: always()
        with:
          report_paths: '**/build/test-results/test/TEST-*.xml'
  
  integration-tests:
    runs-on: ubuntu-latest
    needs: unit-tests
    
    steps:
      - uses: actions/checkout@v3
        with:
          submodules: recursive
      
      - uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Install Nextflow
        run: |
          curl -s https://get.nextflow.io | bash
          sudo mv nextflow /usr/local/bin/
      
      - name: Build plugin
        run: ./gradlew publishToMavenLocal
      
      - name: Run integration tests
        run: |
          cd validation
          nextflow run test.nf -plugins nf-bids@0.1.0
```

## Coverage Reports

### Generate Coverage

```bash
# With JaCoCo
./gradlew test jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html
```

### Coverage Goals

- **Overall**: >80%
- **Core logic**: >90%
- **Configuration**: >85%
- **Parsers**: >85%
- **Handlers**: >90%

## Related Documentation

- [Development Guide](development.md) - Development setup
- [Implementation Guide](implementation.md) - Implementation roadmap
- [API Reference](api.md) - API documentation
