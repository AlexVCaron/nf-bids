# nf-bids — Copilot Instructions

## Project identity

- **nf-bids** is a Nextflow plugin (Groovy) providing `Channel.fromBIDS()` to turn BIDS neuroimaging datasets into Nextflow channels.
- Built with Gradle (always use the wrapper `./gradlew`); unit tests use Spock, integration tests use nf-test.
- Wraps the bundled **libBIDS.sh** bash library — a git submodule at `libBIDS.sh/`. **Never modify files inside `libBIDS.sh/`.**
- Plugin/Nextflow/Gradle version requirements: check `build.gradle` (do not hardcode versions in code or docs).

## Package layout

Source dirs live under `src/main/groovy/nfneuro/`, but package declarations are `nfneuro.plugin.*`:

- `nfneuro.plugin.plugin` (`src/main/groovy/nfneuro/plugin/`) — `BidsPlugin`, `BidsExtension`, `BidsFactory`, `BidsObserver` (plugin entry points).
- `nfneuro.plugin.channel` — `BidsChannelFactory`, `BidsHandler` (+ `operations/`): channel construction and emission.
- `nfneuro.plugin.config` — `BidsConfigLoader`, `BidsConfigAnalyzer`, `BidsConfigValidator`: bids2nf YAML config handling.
- `nfneuro.plugin.parser` — `BidsParser`, `LibBidsShWrapper`, `BidsValidator`: dataset parsing via libBIDS.sh.
- `nfneuro.plugin.grouping` — `BaseSetHandler` + `PlainSetHandler`, `NamedSetHandler`, `SequentialSetHandler`, `MixedSetHandler`.
- `nfneuro.plugin.model` — `BidsEntity`, `BidsFile`, `BidsDataset`, `BidsChannelData`.
- `nfneuro.plugin.util` — `BidsLogger`, `BidsErrorHandler`, `BidsCsvParser`, `BidsEntityUtils`, `SuffixMapper`, `ParticipantsMetadataMerger`.

Unit tests mirror this layout under `src/test/groovy/nfneuro/`.

## Build / test / install

Run all commands from the **project root**:

- `make assemble` — compile and package the plugin JAR.
- `make test` or `./gradlew test` — unit tests (`./gradlew test --tests <FQCN>` for one class).
- `make install` — install into the local Nextflow plugin cache (`~/.nextflow/plugins`).
- `make release` — publish the plugin.
- Integration/validation suite: `cd test/validation && bash test_datasets.sh && cd ../..` (add `--update-snapshots` to refresh nf-test snapshots); base sanity run: `cd test/validation && nextflow run main.nf`.
- Edge cases: `test/edge_cases/run_all_tests.sh`.
- Docs: `make docs` (Antora site + API reference + diagrams), `make docs-serve` (local preview), `make docs-diagrams` (PlantUML only).

## Critical gotchas

- **Run Gradle from the project root only.** Running `./gradlew` from a subdirectory fails with "Project directory is not part of the build".
- **Building is not enough.** After code changes, run `make install` — otherwise Nextflow keeps using the stale cached plugin from `~/.nextflow/plugins`.
- **Emit `java.nio.file.Path`, never `java.io.File`.** Use `nextflow.file.FileHelper.asPath()` for file paths; `File` breaks process `path` inputs and remote filesystems (s3, etc.).
- **Groovy truthiness trap:** an empty map `[:]` is falsy. When detecting set types in configs, use `containsKey('plain_set')` (etc.), not truthiness checks.
- **`configKey` ≠ `fileSuffix`:** `configKey` is the unique YAML top-level key used for OUTPUT map keys; `fileSuffix` is non-unique and used for INPUT file matching. Suffix mapping direction is `configKey → fileSuffix`.
- **`meta` is a reserved key** in bids2nf YAML configs — never use it as a config key.
- **`include` is required in every workflow file.** Adding the plugin to `nextflow.config` is not enough — every `.nf` file that calls `Channel.fromBIDS` must also have `include { fromBIDS } from 'plugin/nf-bids'`.

## Conventions

- Branch names: `feature/your-feature-name`, `fix/your-bug-fix`.
- Commit messages: summary ≤ 50 chars, blank line, then detailed description / list of changes.
- Methods carry `@reference` javadoc tags linking back to the original bids2nf implementation — preserve and add them when porting logic.
- Keep unit tests minimal (< 10 per class), test public API rather than implementation details.

## Where to learn more

- `documentation/modules/development/pages/index.adoc` — system architecture.
- `documentation/modules/development/pages/guides/development.adoc` — dev workflow, CI (act) usage.
- `documentation/modules/development/pages/guides/testing.adoc` — full testing matrix (unit, validation, edge cases, benchmarks, docs build).
- `documentation/modules/development/pages/guides/release.adoc` — release process.
- Deep domain knowledge (BIDS semantics, config grammar, etc.) lives in `.github/skills/`.
