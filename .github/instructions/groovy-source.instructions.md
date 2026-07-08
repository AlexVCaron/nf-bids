---
description: "Use when editing nf-bids plugin Groovy sources: channel handlers, set handlers, parsers, config loaders. Covers output contract, configKey vs fileSuffix, Path conversion, Groovy gotchas."
applyTo: "src/main/groovy/**"
---

# nf-bids Groovy Source Conventions

## Style

- Classes use `@CompileStatic` + `@Slf4j` — **except** the set handlers (`BaseSetHandler` and its subclasses in `nfneuro/grouping/`), where `@CompileStatic` is deliberately commented out with a TODO (dynamic typing needed until the `BidsChannelData` refactor). Do not re-enable it there without the refactor.
- Package names are `nfneuro.plugin.*` while directories are `src/main/groovy/nfneuro/*` (no `plugin/` segment for most subpackages). Match the existing package declarations, not the folder path.
- Javadoc uses `@reference` tags linking to the original bids2nf implementation on GitHub (e.g. `@reference findMatchingGrouping function: https://github.com/agahkarakuzu/bids2nf/...`). Preserve and add these tags in new/edited methods ported from bids2nf.

## Flat output contract (never break)

`Channel.fromBIDS` emits one map per group:

```groovy
[meta: [subject: 'sub-01', session: 'ses-01', ...], dwi: [nii: Path, json: Path], ...]
```

- `meta` holds all loop-over entity values using **long entity names**, plus merged participants metadata and scalar top-level entries from enriched data.
- Data keys at top level are **config keys**, values contain `java.nio.file.Path` objects.
- Opt-out: `options.flatten_output = false` emits legacy `[groupingKey, enrichedData]` tuples. Both formats are public API — never break either.
- See `flattenTupleToMap` / `validateAndEmitChannel` in `BidsHandler`.

## Path handling

- Convert String paths with `nextflow.file.FileHelper.asPath()` (handles local files and `s3://`, `gs://`, `az://` URIs, auto-loads cloud plugins).
- Strings containing `'://'` or starting with `'/'` are parsed directly; otherwise resolve relative paths against `bidsParentDir` first:

```groovy
result = FileHelper.asPath(Paths.get(bidsParentDir, pathStr).toString())
```

- Never emit `java.io.File` into channels — convert with `.toPath()` if one appears.

## configKey vs fileSuffix

- **configKey** = unique YAML top-level key (e.g. `dwi_fullreverse`). Use it for output data keys: `channelData.addSuffixData(configKey, groupMap)`.
- **fileSuffix** = actual BIDS suffix (e.g. `dwi`), non-unique — only for matching input files.
- `SuffixMapper.suffixMapping(config)` builds an inverted `setType → (configKey → targetSuffix)` map from `suffix_maps_to` entries.
- `SuffixMapper.resolveConfigKeys(setType, suffix, mapping)` returns **all** candidate config keys for a suffix (falls back to `[suffix]`).
- Handlers must try each candidate in order, validating `filter` (entity patterns), `exclude_entities`, and `required_entities` before accepting a match — see `findMatchingGrouping` in `BaseSetHandler`.

## Groovy gotchas

- Empty map `[:]` is **falsy** in Groovy. Detect set types with `containsKey`, never truthiness:

```groovy
if (suffixConfig.containsKey('plain_set')) { ... }   // correct
if (suffixConfig.plain_set) { ... }                  // WRONG: empty map → false
```

Valid set-type keys: `plain_set`, `named_set`, `sequential_set`, `mixed_set` (see `BaseSetHandler.getSetType`).
- Map writes silently overwrite — when building many-to-one maps (e.g. suffix mappings), invert the map or collect into lists to avoid losing entries.

## Reserved keys

- `meta` is reserved: `BidsConfigLoader` rejects it as a top-level config/suffix key (`IllegalArgumentException`). Keep this check intact; don't introduce other output keys that could collide with config keys.

## Concurrency

- Results flow through GPars `DataflowQueue`; the pipeline runs asynchronously (`CompletableFuture` via `session.addIgniter` in DSL2).
- Deep-copy emitted structures if they might be mutated or iterated downstream (nf-test's `convertPathsToStrings` can throw `ConcurrentModificationException` on shared references) — see `deepCopy` in `BidsHandler`, applied to every flattened map before emission.

## Error handling & logging

**`BidsErrorHandler`** (all static, `@CompileStatic`):
- `tryWithContext(context, Closure)` — logs ERROR on exception, re-throws as `RuntimeException`. Use for critical operations that must not silently fail.
- `safeExecute(context, Closure, defaultValue = null)` — logs WARN, returns default. Use for optional fallbacks.
- `validateWithContext(condition, context, message)` — throws `IllegalStateException` if condition is false.
- `handleError(context, Exception)` — logs ERROR, throws `RuntimeException`.
- `handleErrorWithMessage(context, message)` — same without exception arg.
- `createDetailedError(error, List<String> suggestions)` — formats multi-line error with numbered suggestions.

**Custom exceptions** (all extend `RuntimeException`):
- `BidsProcessingException` — file I/O and parsing failures.
- `BidsValidationException` — BIDS dataset or config validation failures.
- `BidsConfigurationException` — config loading errors.

All accept `(String message)` and `(String message, Throwable cause)` constructors.

**`BidsLogger`** (all static):
- `logProgress(context?, message)` — INFO; use for milestone progress.
- `logDebug(context?, message)` — DEBUG; enable with `NXF_LOG_LEVEL=DEBUG`.
- `logWarning(context?, message)` — WARN with ⚠︎ prefix.
- `logError(context?, message)` — ERROR with ⛔️ prefix.
- `logSuccess(context?, message)` — INFO with ✅ prefix.
- `withTiming(context, operation, Closure)` — measures and logs wall-clock time at DEBUG.

**Context label convention** — always use these prefixes for greppable output:
- `"nf-bids"` — default/general progress messages.
- `"nf-bids-parser"` — `BidsParser.parseToDataset`.
- `"nf-bids-config"` — `BidsConfigLoader.load`.
- `"nf-bids-handler"` — `BidsHandler`.
- `"libBIDS-wrapper"` — `LibBidsShWrapper`.
