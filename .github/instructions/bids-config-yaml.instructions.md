---
description: "Use when writing or editing bids2nf YAML configuration files for the nf-bids plugin: set types (plain/named/sequential/mixed), suffix_maps_to, exclude_entities, cross-modal includes."
applyTo: "**/bids2nf*.yaml, test/configs/**"
---

# bids2nf YAML Configuration Guidelines

## Structure

- Each top-level key is a **configKey** — unique across the file, becomes the output map key (`item.<configKey>`).
- The global `loop_over` key (a list) declares which entities to group by:

```yaml
loop_over:
  - subject
  - session
```

- `meta` is **reserved** and rejected as a configKey.
- Entity names must be **long form**: `subject`, `session`, `acquisition`, `direction`, `reconstruction`, `run`, `echo`, `flip`, `inversion` — never short keys (`sub`, `ses`, `acq`, `dir`…).

## Set types

Exactly one set-type key per configKey. Empty bodies are valid — code checks key *presence*, not truthiness.

**plain_set** — one file slot per suffix:
```yaml
T1w:
  plain_set: {}
```

**named_set** — named slots selected by entity values; at least one group required:
```yaml
dwi_ap:
  named_set:
    ap: { direction: dir-AP }
    pa: { direction: dir-PA }
  required: ["ap", "pa"]
  suffix_maps_to: "dwi"
```

**sequential_set** — ordered arrays by entity (e.g. echoes, flips).

**mixed_set** — named groups that each contain sequences.

## suffix_maps_to

Maps this configKey to a different file suffix. Multiple configKeys may share one target suffix:
```yaml
dwi_ap:
  suffix_maps_to: "dwi"
dwi_rl:
  suffix_maps_to: "dwi"
```

## ⚠ Double-matching gotcha

When multiple configKeys share a suffix, files can match several configs. Add distinguishing constraints:
- **entity filter in the set body** — entity-value patterns inside `named_set`/`sequential_set` groups
- **`exclude_entities`** — list of entity names; files that carry any of these are skipped by this config

```yaml
dwi:
  plain_set:
    exclude_entities:
      - direction   # exclude phased-encoded variants; leave them to dwi_ap / dwi_rl
  suffix_maps_to: "dwi"
```

## Other options (all optional)

| Option | Scope | Purpose |
|--------|-------|---------|
| `additional_extensions` | configKey or set body | Extra file extensions to include (e.g. `[bvec, bval]`) |
| `include_cross_modal` | set body | List of configKeys whose data is merged into this output group |
| `required` | configKey level | List of named groups that must all be present |
| `exclude_entities` | set body | Skip files carrying any of these entity names |

**`include_cross_modal`** note: a configKey whose data is *only* requested by others via `include_cross_modal` is dropped from the channel output (it is folded into the requester's item instead).

## Default loop_over

If `loop_over` is omitted, it defaults to `[subject, session, run, task]`.

## Sequential set fields

`sequential_set` requires exactly one of:
- `by_entity: "echo"` — single entity (String)
- `by_entities: [echo, run]` — multiple entities (List, non-empty)

Optional `order` field: `flat` (default) or `hierarchical`. Ignored when using `by_entity`.

```yaml
mese:
  sequential_set:
    by_entity: "echo"
    order: flat
  additional_extensions: [json]
```

## Mixed set fields

`mixed_set` requires `named_dimension` and `sequential_dimension` keys that reference entity names. Each named group then contains a sequence.

```yaml
mpm:
  mixed_set:
    named_dimension: acquisition
    sequential_dimension: echo
```

## Runtime options

Pass as the third `options` argument to `Channel.fromBIDS(bidsDir, configPath, options)`:

| Option | Default | Effect |
|--------|---------|--------|
| `flatten_output` | `true` | Flat `[meta: ..., <configKey>: ...]` map format |
| `unpack_json_sidecar` | `false` | Parse `.json` sidecars into maps instead of keeping as `Path` |
| `libbids_sh` | auto | Override path to `libBIDS.sh` script |

## Reference

See [documentation/modules/ROOT/pages/concepts/configuration.adoc](../documentation/modules/ROOT/pages/concepts/configuration.adoc) for the full specification and validation rules.
