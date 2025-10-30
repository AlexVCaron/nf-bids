# API Reference

Complete API documentation for the nf-bids plugin.

## Channel.fromBIDS()

The main entry point for creating BIDS channels.

### Signatures

```groovy
// Minimal: BIDS directory only
Channel.fromBIDS(String bidsDir)

// With configuration file
Channel.fromBIDS(String bidsDir, String configFile)

// With options map
Channel.fromBIDS(Map options)
```

### Parameters

#### Map Options

When using the map signature, the following keys are supported:

| Key | Type | Required | Default | Description |
|-----|------|----------|---------|-------------|
| `bidsDir` | String | Yes | - | Path to BIDS dataset root |
| `config` | String | No | null | Path to YAML configuration file |
| `bids_validation` | Boolean | No | false | Enable BIDS validator |
| `ignore_codes` | List<Integer> | No | [] | BIDS validator codes to ignore |
| `libbids_sh` | String | No | bundled | Path to libBIDS.sh installation |
| `demand_driven` | Boolean | No | true | Load data files on-demand |
| `cross_modal_broadcasting` | Boolean | No | true | Enable cross-modal broadcasting |

#### String Parameters

```groovy
Channel.fromBIDS(
    '/path/to/bids/dataset',     // bidsDir (required)
    '/path/to/config.yaml'       // config (optional)
)
```

### Return Value

Returns a Nextflow `DataflowVariable` channel emitting tuples:

```groovy
[
    groupingKey,      // List<String>: Entity values for grouping
    enrichedData      // Map: Structured BIDS data
]
```

### Examples

#### Basic Usage

```groovy
// Minimal: auto-detect configuration
Channel.fromBIDS('/data/bids')

// With configuration
Channel.fromBIDS(
    '/data/bids',
    '/configs/bids2nf.yaml'
)

// With options
Channel.fromBIDS(
    bidsDir: params.bids_dir,
    config: params.config,
    bids_validation: true
)
```

#### In Workflows

```groovy
workflow {
    // Simple pipeline
    Channel.fromBIDS(params.bids_dir, params.config)
        .set { bids_ch }
    
    myProcess(bids_ch)
}

workflow {
    // With filtering
    Channel.fromBIDS(params.bids_dir, params.config)
        .filter { key, data -> 
            data.subject.startsWith('sub-control') 
        }
        .set { control_subjects }
    
    controlAnalysis(control_subjects)
}

workflow {
    // With mapping
    Channel.fromBIDS(params.bids_dir, params.config)
        .map { key, data ->
            [data.subject, data.session, data.data.T1w]
        }
        .set { anatomicals }
    
    processT1w(anatomicals)
}
```

## Channel Output Structure

### groupingKey

A list containing the values of entities specified in `loop_over`:

```groovy
// For loop_over: [subject, session, run, task]
groupingKey = ['sub-01', 'ses-baseline', 'NA', 'rest']
```

**Notes:**
- Order matches `loop_over` configuration
- Missing entities appear as `"NA"`
- Used for channel deduplication and grouping

### enrichedData

A map containing all BIDS metadata and file paths:

```groovy
enrichedData = [
    // Entity values (from groupingKey)
    subject: 'sub-01',
    session: 'ses-baseline',
    run: 'NA',
    task: 'rest',
    
    // Data files (organized by suffix/data type)
    data: [
        T1w: '/path/to/sub-01_ses-baseline_T1w.nii.gz',
        bold: [
            '/path/to/sub-01_ses-baseline_task-rest_echo-1_bold.nii.gz',
            '/path/to/sub-01_ses-baseline_task-rest_echo-2_bold.nii.gz',
            '/path/to/sub-01_ses-baseline_task-rest_echo-3_bold.nii.gz'
        ],
        dwi: [
            'acq01': '/path/to/sub-01_ses-baseline_acq-acq01_dwi.nii.gz',
            'acq02': '/path/to/sub-01_ses-baseline_acq-acq02_dwi.nii.gz'
        ]
    ],
    
    // All file paths (flat list)
    filePaths: [
        '/path/to/sub-01_ses-baseline_T1w.nii.gz',
        '/path/to/sub-01_ses-baseline_task-rest_echo-1_bold.nii.gz',
        // ...
    ],
    
    // BIDS dataset parent directory
    bidsParentDir: '/path/to/bids'
]
```

### Data Organization by Set Type

#### Plain Set

```groovy
data.T1w = '/path/to/file.nii.gz'  // Single file path
```

#### Named Set

```groovy
data.dwi = [
    'acq01': '/path/to/acq-acq01_dwi.nii.gz',
    'acq02': '/path/to/acq-acq02_dwi.nii.gz'
]  // Map: group_by value -> file path
```

#### Sequential Set

```groovy
data.bold = [
    '/path/to/echo-1_bold.nii.gz',
    '/path/to/echo-2_bold.nii.gz',
    '/path/to/echo-3_bold.nii.gz'
]  // List: ordered by sequence_by entity
```

#### Mixed Set

```groovy
data.fmap = [
    'acq01': [
        '/path/to/acq-acq01_dir-AP_epi.nii.gz',
        '/path/to/acq-acq01_dir-PA_epi.nii.gz'
    ],
    'acq02': [
        '/path/to/acq-acq02_dir-LR_epi.nii.gz',
        '/path/to/acq-acq02_dir-RL_epi.nii.gz'
    ]
]  // Map of lists: group_by -> [sequence_by ordered files]
```

## Advanced Usage

### Accessing Data in Processes

```groovy
process myProcess {
    input:
    tuple val(key), val(data)
    
    script:
    """
    # Access entity values
    echo "Subject: ${data.subject}"
    echo "Session: ${data.session}"
    echo "Task: ${data.task}"
    
    # Access specific data types
    echo "T1w: ${data.data.T1w}"
    echo "First BOLD echo: ${data.data.bold[0]}"
    
    # Access by acquisition
    echo "DWI acq01: ${data.data.dwi.acq01}"
    
    # All files for this group
    echo "All files: ${data.filePaths.join(' ')}"
    """
}
```

### Multi-Process Pipelines

```groovy
workflow {
    Channel.fromBIDS(params.bids_dir, params.config)
        .set { bids_ch }
    
    // Fork channel for parallel processing
    bids_ch.multiMap { key, data ->
        anatomical: [key, data.data.T1w]
        functional: [key, data.data.bold]
        diffusion: [key, data.data.dwi]
    }.set { split_ch }
    
    processT1w(split_ch.anatomical)
    processBold(split_ch.functional)
    processDwi(split_ch.diffusion)
}
```

### Combining with Other Channels

```groovy
workflow {
    // BIDS data
    Channel.fromBIDS(params.bids_dir, params.config)
        .set { bids_ch }
    
    // External reference data
    Channel.fromPath(params.templates)
        .set { template_ch }
    
    // Combine
    bids_ch
        .combine(template_ch)
        .set { combined_ch }
    
    processWithTemplate(combined_ch)
}
```

## Error Handling

### Common Errors

#### Invalid BIDS Directory

```groovy
// ERROR: Directory does not exist
Channel.fromBIDS('/nonexistent/path')
// Exception: BIDS directory not found: /nonexistent/path
```

#### Invalid Configuration

```groovy
// ERROR: Configuration file not found
Channel.fromBIDS('/data/bids', '/nonexistent/config.yaml')
// Exception: Configuration file not found: /nonexistent/config.yaml
```

#### BIDS Validation Errors

```groovy
// ERROR: Dataset fails BIDS validation
Channel.fromBIDS(
    bidsDir: '/data/invalid-bids',
    bids_validation: true
)
// Exception: BIDS validation failed with errors: [...]
```

### Validation Options

```groovy
// Ignore specific validation codes
Channel.fromBIDS(
    bidsDir: '/data/bids',
    bids_validation: true,
    ignore_codes: [99, 36]  // Ignore warnings about dataset_description.json
)
```

## Performance Considerations

### Demand-Driven Data Loading

By default, only file paths are loaded initially. Actual data loading is deferred:

```groovy
// Fast: Only metadata loaded
Channel.fromBIDS(
    bidsDir: '/data/large-bids',
    demand_driven: true  // default
)

// Slower: All data loaded upfront
Channel.fromBIDS(
    bidsDir: '/data/large-bids',
    demand_driven: false
)
```

### Parallel Processing

The plugin uses Groovy dataflow for parallel channel creation:

```groovy
// Multiple BIDS datasets processed in parallel
Channel.fromPath(params.datasets)
    .flatMap { dataset ->
        Channel.fromBIDS(dataset, params.config)
    }
```

## Related Documentation

- [Configuration Guide](configuration.md) - YAML configuration
- [Examples](examples.md) - Usage examples
- [Development Guide](development.md) - Plugin development
