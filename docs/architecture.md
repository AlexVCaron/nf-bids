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
│      │ ConfigAnalyzer  │ │ LibBidsWrapper  │ │  ├─Plain       │        │
│      │                 │ │ BidsValidator   │ │  ├─Named       │        │
│      │ • load()        │ │                 │ │  ├─Sequential  │        │
│      │ • analyze()     │ │ • parse()       │ │  └─Mixed       │        │
│      │ • getSummary()  │ │ • validate()    │ │                │        │
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

## Plain set

```
Input:  sub-01_T1w.nii.gz
Output: [
  [
    ["sub-01"],
    {
      data: {
        T1w: {
          nii: "sub-01_T1w.nii.gz",
          json: "sub-01_T1w.json"
        }
      },
      subject: "sub-01",
      bidsParentDir: "..."
    }
  ],
  ...
]
```

## Named set

```
Input:  sub-01_acq-AP_dwi.nii.gz, sub-01_acq-PA_dwi.nii.gz
Output: [
  [
    ["sub-01"],
    {
      data: {
        dwi: {
          ap: {
            nii: "sub-01_acq-AP_dwi.nii.gz",
            json: "sub-01_acq-AP_dwi.json"
          },
          pa: {
            nii: "sub-01_acq-PA_dwi.nii.gz",
            json: "sub-01_acq-PA_dwi.json"
          }
        }
      },
      subject: "sub-01",
      bidsParentDir: "..."
    }
  ],
  ...
]
```

## Sequential set

```
Input:  sub-01_echo-1_T2star.nii.gz, sub-01_echo-2_T2star.nii.gz, ...
Output: [
  [
    ["sub-01"],
    {
      data: {
        T2star: {
          nii: [
            "sub-01_echo-1_T2star.nii.gz",
            "sub-01_echo-2_T2star.nii.gz",
            ...
          ],
          json: [
            "sub-01_echo-1_T2star.json",
            "sub-01_echo-2_T2star.json",
            ...
          ]
        }
      },
      subject: "sub-01",
      bidsParentDir: "..."
    }
  ],
  ...
]
```

## Mixed set

```
Input:  sub-01_acq-AP_echo-1_dwi.nii.gz, sub-01_acq-AP_echo-2_dwi.nii.gz, ...
Output: [
  [
    ["sub-01"],
    {
      data: {
        dwi: {
          ap: {
            nii: [
              "sub-01_acq-AP_echo-1_dwi.nii.gz",
              "sub-01_acq-AP_echo-2_dwi.nii.gz",
              ...
            ],
            json: [
              "sub-01_acq-AP_echo-1_dwi.json",
              "sub-01_acq-AP_echo-2_dwi.json",
              ...
            ]
          },
          pa: {
            nii: [
              "sub-01_acq-PA_echo-1_dwi.nii.gz",
              "sub-01_acq-PA_echo-2_dwi.nii.gz",
              ...
            ],
            json: [
              "sub-01_acq-PA_echo-1_dwi.json",
              "sub-01_acq-PA_echo-2_dwi.json",
              ...
            ]
          }
        }
      },
      subject: "sub-01",
      bidsParentDir: "..."
    }
  ],
  ...
]
```
