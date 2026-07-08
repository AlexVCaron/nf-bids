---
name: nf-bids-architecture
description: 'nf-bids plugin architecture: runtime flow of Channel.fromBIDS, set handler routing, suffix mapping design, flat output shaping, cross-modal broadcasting. Use when navigating the codebase, tracing data flow, or modifying handlers/output structure.'
---

# nf-bids Architecture

## Package map

Source dirs live under `src/main/groovy/nfneuro/<sub>/`, but package declarations are
`nfneuro.plugin.<sub>` — except the plugin entry points, whose package is plain `nfneuro.plugin`
(dir `src/main/groovy/nfneuro/plugin/`).

| Package | Contents |
|---|---|
| `nfneuro.plugin` | `BidsPlugin` (pf4j entry), `BidsExtension` (`@Factory fromBIDS` + operators), `BidsFactory`/`BidsObserver` (trace observer, banner only) |
| `nfneuro.plugin.channel` | `BidsChannelFactory` (pre-flight checks), `BidsHandler` (orchestrator), `operations/` (`GroupTupleByOp`, `JoinByOp`, `CombineByOp`, `keys/`) |
| `nfneuro.plugin.config` | `BidsConfigLoader`, `BidsConfigAnalyzer` (set-type flags, `loopOverEntities`), `BidsConfigValidator` |
| `nfneuro.plugin.parser` | `BidsParser`, `LibBidsShWrapper` (runs bundled libBIDS.sh), `BidsValidator` |
| `nfneuro.plugin.grouping` | `BaseSetHandler` + `PlainSetHandler`, `NamedSetHandler`, `SequentialSetHandler`, `MixedSetHandler` |
| `nfneuro.plugin.model` | `BidsEntity`, `BidsFile`, `BidsDataset`, `BidsChannelData` |
| `nfneuro.plugin.util` | `BidsLogger`, `BidsErrorHandler`, `BidsCsvParser`, `BidsEntityUtils`, `SuffixMapper`, `ParticipantsMetadataMerger` |

## Runtime flow of `Channel.fromBIDS()`

`BidsHandler` is the orchestrator (fluent builder: `withConfig`/`withBidsDir`/`withOpts`/`withParser`/`ignite`).
Its `execute()` runs these stages:

1. **Config load & analyze** — `loadConfiguration()`: `BidsConfigLoader.load()` (or `loadDefaults()`),
   then `SuffixMapper.suffixMapping(config)` and `BidsConfigAnalyzer` →
   `analyzeConfiguration()` (flags like `hasNamedSets`) and `getLoopOverEntities()`.
2. **Parse dataset** — `BidsParser.parseToDataset()` via libBIDS.sh →
   `BidsDataset`/`BidsFile` objects; `dataset.loadParticipants()` collects participants metadata.
3. **Route to set handlers** — `processDatasets()` instantiates one handler per set type present
   in the analysis (`hasNamedSets` → `NamedSetHandler`, likewise sequential/mixed/plain) and
   merges all handler queues into one `DataflowQueue`.
4. **Unify** — `unifyResults()`: group items by loop-over `groupingKey`, then bucket by
   data-key *fingerprint* (set of configKey names). Same fingerprint = alternatives for one
   slot; different fingerprints = independent streams, combined by cross-product
   (`computeOuterJoin`) — outer-join behaviour.
5. **Cross-modal broadcasting** — `applyCrossModalBroadcasting()`: pools available data per
   looping key, `applyIncludeCrossModal()` injects `include_cross_modal` requests, and
   `shouldKeepChannel()` drops groups whose data was *only* requested by others.
6. **Flatten & emit** — `validateAndEmitChannel()`: unless `flatten_output: false`,
   `flattenTupleToMap()` builds `[meta: {...}, <configKey>: {...}, ...]` — meta gets loop-over
   entity values plus participants metadata; configKey maps sit at the **top level** (not under
   `data`). All file paths become `java.nio.file.Path` via `FileHelper.asPath()` (never
   `java.io.File`); optional `unpack_json_sidecar` parses JSON sidecars into maps. Fails if
   zero groups were emitted; channel closed **only** by poison pill `Channel.STOP`.

Full trace: [documentation/modules/ROOT/pages/source-model/runtime-flow.adoc](documentation/modules/ROOT/pages/source-model/runtime-flow.adoc).

## Suffix mapping design (`suffix_maps_to`)

`SuffixMapper.suffixMapping(config)` builds `setType → configKey → targetSuffix`
(e.g. `named_set → dwi_fullreverse → dwi`). The direction is deliberate: **configKey is unique**
(YAML top-level key), fileSuffix is not — an inverted `suffix → configKey` map would lose
entries when several configKeys map to one suffix.

- `resolveConfigKeys(setType, suffix, mapping)` searches the map values and returns **all**
  candidate configKeys for a file suffix (falls back to `[suffix]` when unmapped).
- `BaseSetHandler` iterates the candidates and validates each against the set config's
  `filters`, `exclude_entities`, and `required_entities` before assigning the file.
- configKey is used for OUTPUT map keys; targetSuffix for INPUT file matching.

## Async model

- `ignite(session)` creates the output channel via `CH.create()`, then (DSL2) defers work with
  `session.addIgniter { perform(true) }` — the channel is consumable before parsing starts.
- `perform(async=true)` runs `execute()` in a `CompletableFuture`; failures abort the session
  via `handlerException`.
- Intermediate results move through unbound GPars `DataflowQueue`s between stages; only
  `validateAndEmitChannel()` binds to the target channel and sends `Channel.STOP`.

## Diagrams

PlantUML sources in [documentation/diagrams](documentation/diagrams):
[class-core.puml](documentation/diagrams/class-core.puml),
[package-dependencies.puml](documentation/diagrams/package-dependencies.puml),
[libbids-integration.puml](documentation/diagrams/libbids-integration.puml),
[sequence-frombids.puml](documentation/diagrams/sequence-frombids.puml).
Rendered gallery: [documentation/modules/ROOT/pages/diagrams.adoc](documentation/modules/ROOT/pages/diagrams.adoc).

## Deep-dive docs

- [architecture.adoc](documentation/modules/ROOT/pages/architecture.adoc) — system architecture
- [source-model/index.adoc](documentation/modules/ROOT/pages/source-model/index.adoc) — class inventory
- [source-model/runtime-flow.adoc](documentation/modules/ROOT/pages/source-model/runtime-flow.adoc) — step-by-step runtime trace
- Concepts: [plugin-bootstrap.adoc](documentation/modules/ROOT/pages/concepts/plugin-bootstrap.adoc),
  [configuration.adoc](documentation/modules/ROOT/pages/concepts/configuration.adoc),
  [bids-parsing.adoc](documentation/modules/ROOT/pages/concepts/bids-parsing.adoc),
  [output-shaping.adoc](documentation/modules/ROOT/pages/concepts/output-shaping.adoc),
  [channel-operators.adoc](documentation/modules/ROOT/pages/concepts/channel-operators.adoc),
  [error-handling.adoc](documentation/modules/ROOT/pages/concepts/error-handling.adoc)

## Output shapes per handler

The **top-level data key is always the configKey**, never the BIDS file suffix. All file values are absolute `java.nio.file.Path` objects.

**PlainSetHandler**
```groovy
[meta: [subject: 'sub-01', ...], T1w: [nii: Path(...), json: Path(...)]]
// Access: item.T1w.nii
```

**NamedSetHandler** (e.g. configKey `dwi_ap` with `suffix_maps_to: "dwi"`)
```groovy
[meta: [...], dwi_ap: [ap: [nii: Path, bval: Path, bvec: Path], pa: [...]]]
// Access: item.dwi_ap.ap.nii  item.dwi_ap.pa.bval
```

**SequentialSetHandler** (echoes as arrays)
```groovy
[meta: [...], mese: [nii: [Path1, Path2, ...], json: [Path1, Path2, ...]]]
// Access: item.mese.nii[0]  item.mese.nii.size()
```

**MixedSetHandler** (named groups each containing sequences)
```groovy
[meta: [...], mpm: [MTw: [nii: [Path, Path], json: [Path, Path]], PDw: [...]]]
// Access: item.mpm.MTw.nii[0]
```

## Cross-modal broadcasting

`include_cross_modal: [T1w]` inside a set body copies T1w data into this group's emitted item.
A configKey whose data is **only** requested by others via `include_cross_modal` is **dropped from output** — it exists solely to provide data to the requesting group.

## Config analysis flags

`BidsConfigAnalyzer` extracts four booleans used by `BidsHandler.processDatasets` to route files:
`hasNamedSets`, `hasSequentialSets`, `hasMixedSets`, `hasPlainSets`.

## Legacy format (deprecated)

`flatten_output: false` option emits `[groupingKey, enrichedMap]` tuples with **relative string paths** instead of flat maps with `Path` objects. This format was deprecated and removed in a later version.
