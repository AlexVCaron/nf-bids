---
description: "Use when writing or running nf-bids tests: Spock unit tests, nf-test integration tests, validation datasets, edge cases, benchmarks."
applyTo: "src/test/**, test/**"
---

# nf-bids Testing Instructions

Full strategy: [documentation/modules/ROOT/pages/guides/testing.adoc](../../documentation/modules/ROOT/pages/guides/testing.adoc). Four layers, each gated on the previous — fix earlier-layer failures before moving on.

## Prerequisite for ALL integration/validation/edge-case/benchmark runs

Build **and install** the plugin first:

```bash
make assemble && make install   # or: ./gradlew assemble install
```

Nextflow loads the plugin from `~/.nextflow/plugins`, **not** from `build/`. Running nf-test or Nextflow workflows against a stale installed plugin silently tests old code.

## Layer 1 — Unit tests (Spock)

- Location: `src/test/groovy/nfneuro/` — Spock specifications named `*Spec.groovy` (some JUnit-style `*Test.groovy` classes also exist, e.g. operator tests under `channel/operations/`).
- Run: `./gradlew test` or `make test`.
- Single class: `./gradlew test --tests nfneuro.plugin.channel.BidsHandlerFlattenSpec`
- Reports: `build/reports/tests/test/index.html`.
- Do not proceed to integration tests while unit tests are red.

## Layer 2 — Integration / validation tests (nf-test)

- Location: `test/validation/` (configured as `testsDir` in [nf-test.config](../../nf-test.config); `workDir` is `.nf-test`).
- Run the full suite from repo root: `nf-test test test/validation/`
- Preferred wrapper (CI-consistent): `cd test/validation && bash test_datasets.sh`
- Quick smoke test (no baseline comparison, NOT a substitute for nf-test): `cd test/validation && nextflow run main.nf`
- Suites include `comparison_plain_sets`, `comparison_named_sets`, `comparison_sequential_sets`, `comparison_mixed_sets`, `comparison_custom_datasets`, `test_heterogeneous_suffix_mapping`, `test_flattened_output`, plus operator tests (`test_joinby`, `test_combineby`, `test_grouptupleby`, `test_path_types`, `test_process_path_input`, `test_unpack_json_sidecar`).

### nf-test file conventions

- Exactly **ONE** top-level test suite per `.nf.test` file — multiple suites in one file cause partial test discovery.
- Snapshots (`*.nf.test.snap`) are committed. Regenerate a single suite when output intentionally changed:
  `nf-test test test/validation/<suite>.nf.test --update-snapshot`, then re-run the full suite for regressions.
- Never update snapshots across the full suite unless behaviour intentionally changed everywhere.
- Both runner scripts (`test_datasets.sh`, `run_all_tests.sh`) support `--update-snapshots` and `--clean-snapshots`.

## Layer 3 — Edge cases and benchmarks

Edge cases (`test/edge_cases/`): boundary behaviour — large items, many items, nested structures, missing fields, concurrency, high-fan-out joins, `combineBy` edge cases.

```bash
nf-test test test/edge_cases/          # via nf-test
cd test/edge_cases && ./run_all_tests.sh   # direct workflows, interactive inspection
```

Benchmarks (`test/benchmark/`): compare closure-based operators against Nextflow built-ins.

```bash
cd test/benchmark && ./run_all_benchmarks.sh
# or individually: nextflow run benchmark_grouptuple.nf (join, combine, combineby_new)
```

Investigate sustained benchmark regressions before release.

## Layer 4 — Documentation build verification

`./gradlew docs` (or `make docs`) must succeed before release — Antora catches broken xrefs, PlantUML diagrams must render (requires Graphviz `dot`). Preview with `make docs-serve`.

## Test data and configs

- BIDS example datasets: `test/data/bids-examples/` (plus custom datasets in `test/data/custom/`).
- YAML pipeline configs: `test/configs/config_*.yaml`, one per modality/scenario (e.g. `config_dwi.yaml`, `config_mp2rage.yaml`).

## Local CI reproduction (act)

```bash
act pull_request -W .github/workflows/validation.yml -j validation
act pull_request -W .github/workflows/validation.yml -j edge_cases
act workflow_dispatch -W .github/workflows/benchmark.yml -j benchmark
```

## Unit test class inventory

All 10 unit test classes with coverage areas:

- `BidsHandlerFlattenSpec` — flat output format, `meta` key, `flatten_output` opt-out
- `BidsConfigLoaderSpec` — YAML loading, `meta` rejection, validation pipeline
- `HeterogeneousSuffixMappingSpec` — multiple configs with same suffix, `suffix_maps_to`, `exclude_entities`
- `SuffixMapperSpec` — mapping direction, `resolveConfigKeys()`
- `GroupTupleByOpTest` — `groupTupleBy` key extraction and grouping correctness
- `JoinByOpTest` — `joinBy` single and dual key extractors, missing-match behaviour
- `CombineByOpTest` — `combineBy` cartesian-product semantics and output shape
- `CompositeKeyTest` — multi-field composite key construction and equality
- `KeyExtractorTest` — closure-based key extractor, type safety, null handling
- `BidsExtensionTest` — plugin registration, operator availability on `Channel`

Run a single class: `./gradlew test --tests nfneuro.plugin.channel.BidsHandlerFlattenSpec`

Test reports: `build/reports/tests/test/index.html`

## Integration test suites

7 nf-test suites (each with one top-level test suite per file):

- `comparison_plain_sets.nf.test` — plain set output shape, single file per suffix
- `comparison_named_sets.nf.test` — named dimension grouping (e.g. `dwi_ap`/`dwi_pa`)
- `comparison_sequential_sets.nf.test` — sequential sets, echo/flip array ordering
- `comparison_mixed_sets.nf.test` — mixed sets, nested grouping + sequencing
- `comparison_custom_datasets.nf.test` — edge-of-spec datasets, custom scenarios
- `test_heterogeneous_suffix_mapping.nf.test` — multiple configs sharing a BIDS suffix
- `test_flattened_output.nf.test` — flat format, `meta` key, `Path` types

⚠️ **NEVER** run `--update-snapshots` unless you intentionally changed output behavior — it silently accepts regressions.

## Edge-case workflow inventory

8 edge-case workflows from `test/edge_cases/`:

`test1_large_items`, `test2_many_items`, `test3_nested_structures`, `test4_missing_fields`, `test5_concurrent`, `test6_join_many`, `test7_complex_filter`, `test8_combineby_edge_cases`

Run: `nf-test test test/edge_cases/` or `cd test/edge_cases && ./run_all_tests.sh`

## Benchmark expectations

Typical overhead: 10–30 ms per operator for BIDS datasets. A sustained >50 % regression warrants investigation before release.

Run: `cd test/benchmark && ./run_all_benchmarks.sh` or individual workflows (`benchmark_grouptuple.nf`, `benchmark_join.nf`, `benchmark_combine.nf`, `benchmark_combineby_new.nf`)
