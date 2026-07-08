---
name: nextflow-plugin-dev
description: 'Nextflow plugin development mechanics for nf-bids. Use when adding operators or factory methods, registering extension points, debugging plugin loading, or when code changes seem to have no effect at runtime.'
---

# Nextflow Plugin Development (nf-bids)

## Extension point model

Nextflow uses [pf4j](https://pf4j.org/) for plugin management. The plugin JAR
manifest names the entry-point class; DSL extension classes are registered in
`src/main/resources/META-INF/extensions.idx` — one fully-qualified class name
per line. In nf-bids this file has exactly **one line**:

```
nfneuro.plugin.BidsExtension
```

The same class is also declared in `build.gradle` under
`nextflowPlugin { extensionPoints = ['nfneuro.plugin.BidsExtension'] }` —
keep both in sync.

**Key constraint:** keep ALL `@Factory` and `@Operator` methods for the plugin
in this single extension class (`BidsExtension extends PluginExtensionPoint`).
Do not create additional extension classes per operator. `BidsExtension`
currently registers:

- `fromBIDS` (`@Factory`)
- `groupTupleBy` (`@Operator`)
- `joinBy` (`@Operator`, two overloads)
- `combineBy` (`@Operator`, overloaded for single- and dual-extractor forms)

The extension receives the active `Session` via its `init(Session)` override.

## @Factory methods

- Create channels; invoked by users as `Channel.name(...)`.
- Any parameter signature is allowed (e.g. `fromBIDS(String bidsDir, String
  configPath = null, Map options = [:])`).
- Must return `DataflowWriteChannel`.
- Delegate real work to a factory class (`BidsChannelFactory`) rather than
  implementing logic in the extension.

## @Operator methods

- Invoked dot-style on a channel: `channel.name(...)`.
- **First parameter MUST be `DataflowReadChannel`** (the receiver channel is
  injected there); return type must be `DataflowWriteChannel`. This contract
  is enforced when the plugin loads.
- Binary operators (join/combine style) take a second `DataflowReadChannel`
  as the second parameter.
- Overloads are permitted (see `joinBy` / `combineBy`).

## Dev cycle (CRITICAL)

```
edit → ./gradlew assemble (or make assemble) → make install → test
```

- Nextflow loads plugins from `~/.nextflow/plugins/`, **NOT** from `build/`.
  If you skip `make install` (which runs `./gradlew install`), Nextflow
  silently keeps running the OLD cached plugin — your changes appear to have
  no effect.
- Always run Gradle **from the project root**. Running `./gradlew` from a
  subdirectory fails with "Project directory is not part of the build".
- Publish with `make release` (`./gradlew releasePlugin`).

## Design rule: no overloading of core operators

Plugins cannot overload built-in Nextflow operators. New DSL methods need
**new semantic names** — this is why nf-bids provides `groupTupleBy`,
`joinBy`, and `combineBy` instead of overloading `groupTuple`, `join`, and
`combine`.

## Where things live

| Concern | Location |
|---|---|
| Plugin descriptor (provider, entry class, extension points, min Nextflow version) | `build.gradle` (`nextflowPlugin { }` block) |
| Extension registration | `src/main/resources/META-INF/extensions.idx` |
| Plugin entry class | `nfneuro.plugin.BidsPlugin` (minimal `BasePlugin` subclass) |
| DSL extension | `nfneuro.plugin.BidsExtension` |
| Trace observer wiring | `nfneuro.plugin.BidsFactory` (`TraceObserverFactory`) → `BidsObserver` |
| Operator implementations | `nfneuro.plugin.channel.operations` (`GroupTupleByOp`, `JoinByOp`, `CombineByOp`, `keys.KeyExtractor`) |
| Channel construction | `nfneuro.plugin.channel.BidsChannelFactory` |

## Deep dives

- `documentation/modules/ROOT/pages/architecture.adoc` — system architecture.
- `documentation/modules/ROOT/pages/concepts/plugin-bootstrap.adoc` — how the
  four `nfneuro.plugin` classes wire into Nextflow's plugin framework.
- `documentation/modules/ROOT/pages/concepts/channel-operators.adoc` —
  operator semantics.

## Plugin bootstrap lifecycle

pf4j scans the plugin JAR and registers two services:

1. **`BidsFactory`** (`TraceObserverFactory`) — creates `BidsObserver`; called once per session.
2. **`BidsExtension`** (`PluginExtensionPoint`) — registered via `META-INF/extensions.idx`; `init(Session)` called after load.

`BidsPlugin` itself extends `BasePlugin` only — it has no logic. Do not add lifecycle code there.

## fromBIDS full signature

```groovy
@Factory
DataflowWriteChannel fromBIDS(String bidsDir, String configPath = null, Map options = [:])
```

Calling in a workflow requires an **explicit include** — `nextflow.config` alone is not sufficient:

```groovy
include { fromBIDS } from 'plugin/nf-bids'
```

## Async ignition (DSL2)

In DSL2 mode `BidsChannelFactory.fromBIDS` defers work by registering an igniter:

```groovy
session.addIgniter { -> handler.perform(true) }
```

`perform(true)` runs `execute()` inside `CompletableFuture.runAsync` on a background thread. The channel is returned immediately (empty) and populated asynchronously.

## Shell security rules

`LibBidsShWrapper` always passes paths as **array arguments**, never string interpolation, to prevent shell injection:

```groovy
['bash', '-c', 'set -euo pipefail && source "$1" && libBIDSsh_parse_bids_to_table "$2" > "$3"',
 'bash', scriptPath, bidsDir, outputFile]
```

All user-supplied paths are validated against a blocklist before the subprocess is created.
Blocklist characters: `;  &  |  $  `` (  )  <  >  "  '  \` and newlines.
