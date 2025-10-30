# Plugin Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                         nf-bids Plugin                                 │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │ Entry Point: Channel.fromBIDS()                                  │ │
│  │ ┌────────────────────────────────────────────────────────────┐   │ │
│  │ │  BidsChannelFactory                                        │   │ │
│  │ │  • fromBIDS(bidsDir, config, options)                      │   │ │
│  │ │  • preFlightChecks()                                       │   │ │
│  │ │  • processDatasets()                                       │   │ │
│  │ │  • applyCrossModalBroadcasting()                           │   │ │
│  │ │  • validateAndEmitChannel()                                │   │ │
│  │ └────────────────────────────────────────────────────────────┘   │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                               │                                        │
│           ┌───────────────────┼───────────────────┐                   │
│           │                   │                   │                   │
│  ┌────────▼────────┐ ┌────────▼────────┐ ┌───────▼────────┐          │
│  │  Configuration  │ │    Parsers      │ │   Grouping     │          │
│  ├─────────────────┤ ├─────────────────┤ ├────────────────┤          │
│  │ ConfigLoader    │ │ BidsParser      │ │ BaseSetHandler │          │
│  │ ConfigAnalyzer  │ │ LibBidsWrapper  │ │  ├─Plain       │          │
│  │                 │ │ BidsValidator   │ │  ├─Named       │          │
│  │ • load()        │ │                 │ │  ├─Sequential  │          │
│  │ • analyze()     │ │ • parse()       │ │  └─Mixed       │          │
│  │ • getSummary()  │ │ • validate()    │ │                │          │
│  └─────────────────┘ └─────────────────┘ └────────────────┘          │
│           │                   │                   │                   │
│           │                   │                   │                   │
│  ┌────────▼───────────────────▼───────────────────▼────────┐          │
│  │                   Model Layer                           │          │
│  ├─────────────────────────────────────────────────────────┤          │
│  │  BidsEntity  │  BidsFile  │  BidsDataset  │  BidsChannelData  │   │
│  └─────────────────────────────────────────────────────────┘          │
│           │                                                            │
│  ┌────────▼─────────────────────────────────────────────────┐         │
│  │                Utility Layer                             │         │
│  ├──────────────────────────────────────────────────────────┤         │
│  │  BidsLogger         │         BidsErrorHandler           │         │
│  └──────────────────────────────────────────────────────────┘         │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘

External Dependencies:
┌──────────────────────┐     ┌──────────────────────┐
│   libBIDS.sh         │────▶│  Bash Execution      │
│   (Git Submodule)    │     │  • Parse BIDS to CSV │
└──────────────────────┘     └──────────────────────┘

┌──────────────────────┐     ┌──────────────────────┐
│  bids-validator      │────▶│  Docker Container    │
│  (Docker)            │     │  • Validate BIDS     │
└──────────────────────┘     └──────────────────────┘

Data Flow:
──────────

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


Reference Mapping to Original Code:
───────────────────────────────────

Plugin Class              Original File                         Line Refs
─────────────────────────────────────────────────────────────────────────
BidsChannelFactory       main.nf                                L20-L249
BidsParser               modules/parsers/lib_bids_sh_parser.nf  L1-L28
LibBidsShWrapper         modules/parsers/lib_bids_sh_parser.nf  L17-L24
BidsValidator            modules/parsers/bids_validator.nf      L1-L85
BidsConfigAnalyzer       modules/utils/config_analyzer.nf       L1-L100
PlainSetHandler          subworkflows/emit_plain_sets.nf        L1-L50
NamedSetHandler          subworkflows/emit_named_sets.nf        L1-L70
SequentialSetHandler     subworkflows/emit_sequential_sets.nf   L1-L80
MixedSetHandler          subworkflows/emit_mixed_sets.nf        L1-L100
BaseSetHandler           modules/grouping/entity_grouping_utils.nf
BidsLogger               modules/utils/error_handling.nf        L1-L15
BidsErrorHandler         modules/utils/error_handling.nf        L35-L48


Set Type Processing Examples:
─────────────────────────────

Plain Set:
  Input:  sub-01_T1w.nii.gz
  Output: { file: "path", entities: {...}, metadata: {...} }

Named Set:
  Input:  sub-01_acq-AP_dwi.nii.gz, sub-01_acq-PA_dwi.nii.gz
  Output: { AP: [file1], PA: [file2] }

Sequential Set:
  Input:  sub-01_echo-1_T2star.nii.gz, sub-01_echo-2_T2star.nii.gz, ...
  Output: [ {sequence: 1, file: ...}, {sequence: 2, file: ...}, ... ]

Mixed Set:
  Input:  sub-01_acq-AP_echo-1_dwi.nii.gz, sub-01_acq-AP_echo-2_dwi.nii.gz, ...
  Output: { AP: [{sequence: 1, ...}, {sequence: 2, ...}], PA: [...] }


Implementation Checklist:
─────────────────────────

Core Functionality:
  [ ] LibBidsShWrapper.parseBidsToCSV() - Execute bash
  [ ] BidsParser.parse() - CSV to channel
  [ ] BidsParser.csvToChannel() - Channel creation
  [ ] BidsParser.parseCsvLine() - Row parsing

Configuration:
  [ ] BidsConfigLoader.load() - YAML parsing
  [ ] BidsConfigAnalyzer.analyzeConfiguration() - Set detection
  [ ] BidsConfigAnalyzer.getLoopOverEntities() - Entity extraction
  [ ] BidsConfigAnalyzer.getConfigurationSummary() - Stats

Handlers:
  [ ] PlainSetHandler.process() - Plain processing
  [ ] NamedSetHandler.process() - Named grouping
  [ ] SequentialSetHandler.process() - Sequential ordering
  [ ] MixedSetHandler.process() - Nested structures

Base Handler:
  [ ] BaseSetHandler.findMatchingGrouping() - Pattern matching
  [ ] BaseSetHandler.createFileMap() - File organization
  [ ] BaseSetHandler.validateRequiredFiles() - Validation
  [ ] BaseSetHandler.extractEntitiesFromFilename() - Entity parsing

Channel Factory:
  [ ] BidsChannelFactory.processDatasets() - Routing
  [ ] BidsChannelFactory.applyCrossModalBroadcasting() - Broadcasting
  [ ] BidsChannelFactory.unifyResults() - Merging
  [ ] BidsChannelFactory.extractEntityValues() - Entity extraction
  [ ] BidsChannelFactory.applyIncludeCrossModal() - Include logic
  [ ] BidsChannelFactory.shouldKeepChannel() - Filtering

Testing:
  [ ] Unit tests for each component
  [ ] Integration tests with real datasets
  [ ] Validation against expected outputs
  [ ] Cross-modal broadcasting tests

Documentation:
  [ ] API documentation
  [ ] Usage examples
  [ ] Migration guide
  [ ] Troubleshooting guide
```
