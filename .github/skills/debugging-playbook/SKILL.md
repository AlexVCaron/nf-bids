---
name: debugging-playbook
description: 'Debugging playbook for nf-bids: known gotcha catalogue, root-cause methodology for grouping and matching bugs, logging, multi-candidate matching diagnosis. Use when channel output is wrong, groups are missing, files match the wrong config, or changes seem to have no effect.'
---

# nf-bids Debugging Playbook

## Gotcha catalogue

| Symptom | Cause | Fix |
|---------|-------|-----|
| Code change has no effect at runtime | Nextflow loads plugin from `~/.nextflow/plugins/` (cached) | `make install` after every build |
| `Project directory is not part of the build` | Gradle run from a subdirectory | `cd` to repo root, then run `./gradlew` |
| Only one of several configs sharing a suffix produces output | Map key collision in SuffixMapper or missing entity disambiguation | Check direction is `configKey → fileSuffix`; add `exclude_entities`/entity filters |
| Config with empty set body is silently ignored (`plain_set: {}`) | Groovy empty map `[:]` is falsy | Use `containsKey('plain_set')` — **never** truthiness |
| Process fails on file inputs or remote filesystem broken | `java.io.File` leaked into output | Use `nextflow.file.FileHelper.asPath()` everywhere |
| Same file lands in multiple output groups | Double-matching across configs sharing a suffix | Add `exclude_entities` or entity-value patterns to disambiguate |
| `No data groups were processed!` | No files match patterns / wrong BIDS dir / incomplete required groups | Check the full error message from `BidsHandler.validateAndEmitChannel` for exact cause list |

## Reading the logs

`BidsLogger.logProgress` emits at **SLF4J INFO** level, prefixed `[<context>]` (e.g. `[nf-bids-handler]`).  
These lines appear in Nextflow's console output (when log level ≥ INFO) and in `.nextflow.log`.  
Debug-level detail uses `BidsLogger.logDebug` — enable with `NXF_LOG_LEVEL=DEBUG`.

## Root-cause methodology for grouping bugs

1. **Reproduce minimally** — one dataset from `test/data/bids-examples/` + a stripped config.
2. **Read the config analysis summary** in the logs — confirms `loop_over` entities, handler routing (hasNamedSets etc.), suffix mappings built.
3. **Trace pipeline stages in order** — find the first stage where data goes wrong:
   - Config analysis → handler routing → per-handler processing → `unifyResults` (outer join) → `applyCrossModalBroadcasting` → `flattenTupleToMap` → emit
4. **Check handler output** — each handler logs `Running handler: <Class> ...` before processing.
5. **Fix consistently** — Plain/Named/Sequential/MixedSetHandler share the same patterns; a fix usually applies to all four.

## Multi-candidate matching

`SuffixMapper.resolveConfigKeys(setType, suffix, mapping)` returns **all** configKeys that target a given file suffix.  
`BaseSetHandler.findMatchingGrouping` tries each candidate; a file should pass exactly **one** candidate's entity validation (filters, `exclude_entities`, `required_entities`).  
If zero candidates match → file is skipped (silent). If multiple match → non-deterministic output. Both are bugs.

## Quick verification loop

```bash
./gradlew test --tests "nfneuro.plugin.<ClassName>"   # targeted unit test
make install                                           # required before integration test
nf-test test test/validation/<specific>.nf.test        # single integration test
```

## Error handler patterns

- Use `BidsErrorHandler.tryWithContext("context", { ... })` for operations that must abort on failure — logs ERROR + re-throws.
- Use `BidsErrorHandler.safeExecute("context", { ... }, defaultValue)` for optional fallbacks — logs WARN and returns the default; does not abort.
- Use `BidsErrorHandler.createDetailedError(message, ["suggestion1", "suggestion2"])` to produce numbered suggestion lists in error output.

## Concrete log output

At INFO level, a successful config load prints a tree like this (grep `nf-bids-handler` in `.nextflow.log`):
```
[nf-bids-handler] ┌─ ✓ Configuration analysis complete:
[nf-bids-handler] ├─ ↬ Loop over entities: subject, session
[nf-bids-handler] ├─ ⑆ Named sets: 1 patterns (dwi_ap)
[nf-bids-handler] ├─ ⑉ Plain sets: 1 patterns (T1w)
[nf-bids-handler] └─ = TOTAL patterns: 2
```
If this block is **absent**, config loading failed before dispatch. Check for `[nf-bids-config]` ERROR lines above it.

## libBIDS.sh failure surface

When `LibBidsShWrapper` subprocess exits non-zero, it throws `BidsProcessingException` with the shell stderr captured in the message. To see it:
```bash
grep 'libBIDS-wrapper\|BidsProcessingException' .nextflow.log
```

## Reference

See [documentation/modules/ROOT/pages/concepts/error-handling.adoc](../documentation/modules/ROOT/pages/concepts/error-handling.adoc) for `BidsErrorHandler` patterns.
