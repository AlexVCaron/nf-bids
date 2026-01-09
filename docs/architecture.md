# Plugin Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                         nf-bids Plugin                                 │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ Entry Point: Channel.fromBIDS()                                  │  │
│  │ ┌────────────────────────────────────────────────────────────┐   │  │
│  │ │  BidsChannelFactory                                        │   │  │
│  │ │  • fromBIDS(bidsDir, config, options)                      │   │  │
│  │ │  • preFlightChecks()                                       │   │  │
│  │ │  • processDatasets()                                       │   │  │
│  │ │  • applyCrossModalBroadcasting()                           │   │  │
│  │ │  • validateAndEmitChannel()                                │   │  │
│  │ └────────────────────────────────────────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                   │                                    │
│               ┌───────────────────┼───────────────────┐                │
│               │                   │                   │                │
│      ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼───────┐        │
│      │  Configuration  │ │    Parsers      │ │   Grouping     │        │
│      ├─────────────────┤ ├─────────────────┤ ├────────────────┤        │
│      │ ConfigLoader    │ │ BidsParser      │ │ BaseSetHandler │        │
│      │ ConfigValidator │ │ LibBidsShWrapper│ │ ├─PlainSet     │        │
│      │ ConfigAnalyzer  │ │ BidsValidator   │ │ ├─NamedSet     │        │
│      │                 │ │                 │ │ ├─SequentialSet│        │
│      │ • load()        │ │ • parse()       │ │ └─MixedSet     │        │
│      │ • validate()    │ │ • validate()    │ │                │        │
│      │ • analyze()     │ │ • wrapper()     │ │ • process()    │        │
│      └─────────────────┘ └─────────────────┘ └────────────────┘        │
│               │                   │                   │                │
│               │                   │                   │                │
│      ┌────────▼───────────────────▼───────────────────▼────────┐       │
│      │                       Model Layer                       │       │
│      ├─────────────────────────────────────────────────────────┤       │
│      │  BidsEntity │ BidsFile │ BidsDataset │ BidsChannelData  │       │
│      └─────────────────────────────────────────────────────────┘       │
│                                   │                                    |
│      ┌────────────────────────────▼─────────────────────────────┐      │
│      │                      Utility Layer                       │      │
│      ├──────────────────────────────────────────────────────────┤      │
│      │         BidsLogger         │       BidsErrorHandler      │      │
│      └──────────────────────────────────────────────────────────┘      │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

## External Dependencies:
```
┌──────────────────────┐     ┌──────────────────────┐
│   libBIDS.sh         │────▶│  Bash Execution      │
│   (Git Submodule)    │     │  • Parse BIDS to CSV │
└──────────────────────┘     └──────────────────────┘

┌──────────────────────┐     ┌──────────────────────┐
│  bids-validator      │────▶│  Docker Container    │
│  (Docker)            │     │  • Validate BIDS     │
└──────────────────────┘     └──────────────────────┘
```

## Data Flow:

```
1. BIDS Directory
         │
         ▼
2. libBIDS.sh Parser ──▶ CSV Output
         │
         ▼
3. BidsParser ──▶ DataflowQueue
         │
         ▼
4. Config Analysis ──▶ Route to Handlers
         │
         ├──▶ PlainSetHandler
         ├──▶ NamedSetHandler
         ├──▶ SequentialSetHandler
         └──▶ MixedSetHandler
         │
         ▼
5. Combine Results ──▶ Unified Channel
         │
         ▼
6. Cross-Modal Broadcasting
         │
         ▼
7. Final Channel ──▶ User Workflow
```

## Reference Mapping to Original Code:


|Plugin Class|Original File|Line Refs|
|-|-|-|
|**BidsChannelFactory**|main.nf|L20-L249|
|**BidsParser**|modules/parsers/lib_bids_sh_parser.nf|L1-L28|
|**LibBidsShWrapper**|modules/parsers/lib_bids_sh_parser.nf|L17-L24|
|**BidsValidator**|modules/parsers/bids_validator.nf|L1-L85|
|**BidsConfigAnalyzer**|modules/utils/config_analyzer.nf|L1-L100|
|**BaseSetHandler**|modules/grouping/entity_grouping_utils.nf|All around|
|**PlainSetHandler**|subworkflows/emit_plain_sets.nf|L1-L50|
|**NamedSetHandler**|subworkflows/emit_named_sets.nf|L1-L70|
|**SequentialSetHandler**|subworkflows/emit_sequential_sets.nf|L1-L80|
|**MixedSetHandler**|subworkflows/emit_mixed_sets.nf|L1-L100|
|**BidsLogger**|modules/utils/error_handling.nf|L1-L15|
|**BidsErrorHandler**|modules/utils/error_handling.nf|L35-L48|


# Set Type Processing Examples:

## Plain Set

**Since:** Baseline / v0.1.0  
**Output Format:** Flat map (default since beta.9)

```
Input:  sub-01_T1w.nii.gz

Output (flat format):
[
  meta: [
    subject: 'sub-01',
    session: 'NA',
    run: 'NA'
  ],
  T1w: [
    nii: Path("/absolute/path/to/bids/sub-01/anat/sub-01_T1w.nii.gz"),
    json: Path("/absolute/path/to/bids/sub-01/anat/sub-01_T1w.json")
  ]
]
```

**Configuration:**
```yaml
loop_over: [subject, session, run]

T1w:
  plain_set: {}
```

## Named Set

**Since:** Baseline / v0.1.0  
**Output Format:** Flat map with named groups (default since beta.9)

```
Input:  sub-01_dir-AP_dwi.nii.gz, sub-01_dir-PA_dwi.nii.gz

Output (flat format):
[
  meta: [
    subject: 'sub-01',
    session: 'NA'
  ],
  dwi: [
    ap: [
      nii: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.nii.gz"),
      json: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.json"),
      bval: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.bval"),
      bvec: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-AP_dwi.bvec")
    ],
    pa: [
      nii: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.nii.gz"),
      json: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.json"),
      bval: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.bval"),
      bvec: Path("/absolute/path/to/bids/sub-01/dwi/sub-01_dir-PA_dwi.bvec")
    ]
  ]
]
```

**Configuration:**
```yaml
loop_over: [subject, session]

dwi:
  named_set:
    ap: {direction: dir-AP}
    pa: {direction: dir-PA}
  required: [ap, pa]
  additional_extensions: [.bval, .bvec]
```

## Sequential Set

**Since:** Baseline / v0.1.0  
**Output Format:** Flat map with arrays (default since beta.9)

```
Input:  sub-01_echo-1_megre.nii.gz, sub-01_echo-2_megre.nii.gz, sub-01_echo-3_megre.nii.gz

Output (flat format):
[
  meta: [
    subject: 'sub-01',
    session: 'NA'
  ],
  megre: [
    nii: [
      Path("/absolute/path/to/bids/sub-01/anat/sub-01_echo-1_megre.nii.gz"),
      Path("/absolute/path/to/bids/sub-01/anat/sub-01_echo-2_megre.nii.gz"),
      Path("/absolute/path/to/bids/sub-01/anat/sub-01_echo-3_megre.nii.gz")
    ],
    json: [
      Path("/absolute/path/to/bids/sub-01/anat/sub-01_echo-1_megre.json"),
      Path("/absolute/path/to/bids/sub-01/anat/sub-01_echo-2_megre.json"),
      Path("/absolute/path/to/bids/sub-01/anat/sub-01_echo-3_megre.json")
    ]
  ]
]
```

**Configuration:**
```yaml
loop_over: [subject, session]

megre:
  sequential_set:
    by_entity: echo
```

## Mixed Set

**Since:** Baseline / v0.1.0  
**Output Format:** Flat map with nested structure (default since beta.9)

```
Input:  sub-01_acq-MTw_echo-1_MPM.nii.gz, sub-01_acq-MTw_echo-2_MPM.nii.gz,
        sub-01_acq-PDw_echo-1_MPM.nii.gz, sub-01_acq-PDw_echo-2_MPM.nii.gz,
        sub-01_acq-T1w_echo-1_MPM.nii.gz, sub-01_acq-T1w_echo-2_MPM.nii.gz

Output (flat format):
[
  meta: [
    subject: 'sub-01',
    session: 'NA'
  ],
  mpm: [
    MTw: [
      nii: [
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-1_MPM.nii.gz"),
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-2_MPM.nii.gz")
      ],
      json: [
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-1_MPM.json"),
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-MTw_echo-2_MPM.json")
      ]
    ],
    PDw: [
      nii: [
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-PDw_echo-1_MPM.nii.gz"),
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-PDw_echo-2_MPM.nii.gz")
      ],
      json: [...]
    ],
    T1w: [
      nii: [
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-T1w_echo-1_MPM.nii.gz"),
        Path("/absolute/path/to/bids/sub-01/anat/sub-01_acq-T1w_echo-2_MPM.nii.gz")
      ],
      json: [...]
    ]
  ]
]
```

**Configuration:**
```yaml
loop_over: [subject, session]

mpm:
  mixed_set:
    named_dimension: acquisition
    sequential_dimension: echo
    named_groups:
      MTw: {acquisition: acq-MTw}
      PDw: {acquisition: acq-PDw}
      T1w: {acquisition: acq-T1w}
  required: [MTw, PDw, T1w]
```
