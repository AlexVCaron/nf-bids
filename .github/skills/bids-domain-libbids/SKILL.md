---
name: bids-domain-libbids
description: 'BIDS neuroimaging dataset structure and the bundled libBIDS.sh parser: entities, long/short names, TSV table contract, bidsignore behavior, JSON sidecars. Use when working on parsing, entity handling, or writing BIDS test data or configs.'
---

# BIDS Domain & libBIDS.sh

## BIDS basics

BIDS filenames are entity-value chains followed by suffix + extension:
```
sub-01_ses-02_acq-highres_dir-AP_dwi.nii.gz
└─ entities ────────────────┘  └─suffix┘└ext┘
```
Files live under `<root>/<subject>/<session>/<datatype>/` (e.g. `anat/`, `dwi/`, `fmap/`, `eeg/`).
JSON sidecars (same basename, `.json` extension) carry acquisition metadata.

## Entity long↔short names

The plugin and bids2nf YAML configs **always use the long (name) form**. The short key form appears only in filenames.

| filename token (key) | table column / config name |
|----------------------|---------------------------|
| `sub` | `subject` |
| `ses` | `session` |
| `acq` | `acquisition` |
| `rec` | `reconstruction` |
| `dir` | `direction` |
| `inv` | `inversion` |
| `mt` | `mtransfer` |
| `part` | `part` |
| `task`, `run`, `echo`, `flip`, `chunk`, `split` | same |
| `tpl` | `template` |
| `cohort`, `atlas`, `scale` | same (verify in libBIDS.sh/AGENTS.md) |

Passing a short key (e.g. `sub`) where a long name is expected silently fails to match.

## libBIDS.sh contract

`libBIDS.sh/` is a **git submodule — never modify files inside it.**

The library is a single sourced Bash file; no build step required.  
Core entry point: **`libBIDSsh_parse_bids_to_table`** — BIDS directory → TSV table.

### TSV table columns (left → right)

`derivatives` | `datatype` | *entity names (long form)* | `suffix` | `extension` | `path` | *(optional)* `json_path`

### Public API (8 functions)

| Function | Purpose |
|----------|---------|
| `libBIDSsh_parse_bids_to_table` | Parse BIDS dir → TSV; honors `.bidsignore` by default |
| `libBIDSsh_table_filter` | AWK-based filter: columns, rows, drop-NA, invert |
| `libBIDSsh_drop_na_columns` | Remove all-NA columns |
| `libBIDSsh_apply_bidsignore` | Drop rows excluded by `.bidsignore` |
| `libBIDSsh_extension_json_rows_to_column_json_path` | Fold JSON sidecar rows → `json_path` column |
| `libBIDSsh_table_column_to_array` | TSV column → Bash array |
| `libBIDSsh_table_iterator` | Iterate rows into associative array, with sorting |
| `libBIDSsh_json_to_associative_array` | Parse JSON file → Bash associative array |

## .bidsignore behavior

- **Honored by default** in `libBIDSsh_parse_bids_to_table`.
- Opt-out flags: `--no-bidsignore`, `--no-default-ignores`.
- Plugin exposes these as `use_bidsignore` and `use_default_ignores` options (passed to `BidsParser.parseToDataset`).

## JSON sidecar matching

Exact filename only — no BIDS inheritance resolution.

## How the plugin locates libBIDS.sh

`LibBidsShWrapper` searches a priority list: plugin `lib/`, submodule path, system install. Override explicitly via the `libbids_sh` option on `Channel.fromBIDS`.

## Custom entities

Extend supported entities via `libBIDS.sh/custom/custom_entities.json.tpl` (requires `jq`). See `libBIDS.sh/AGENTS.md`.

## Reference

See `libBIDS.sh/AGENTS.md` and `libBIDS.sh/README.md` for the authoritative API reference.  
See [documentation/modules/ROOT/pages/concepts/bids-parsing.adoc](../documentation/modules/ROOT/pages/concepts/bids-parsing.adoc) for how the plugin wraps the library.

## libBIDS.sh discovery priority

`LibBidsShWrapper` searches in this order (first found wins):

1. Embedded `lib/libBIDS.sh` inside the plugin JAR (standard plugin distribution)
2. `./lib/libBIDS.sh` (relative to working directory)
3. `libBIDS.sh/libBIDS.sh` (git submodule in current directory)
4. `../libBIDS.sh/libBIDS.sh` (one level up)
5. `../../libBIDS.sh/libBIDS.sh` (two levels up)
6. `../../../libBIDS.sh/libBIDS.sh` (three levels up — for `test/validation/` subdirectory)
7. `/usr/local/bin/libBIDS.sh` (system-wide install)
8. `~/.local/bin/libBIDS.sh` (user-local install)

Override all steps: set `libbids_sh` option in `Channel.fromBIDS(..., [libbids_sh: '/path/to/libBIDS.sh'])`.

## Path security

Before calling the subprocess, `bidsDir` and all user-supplied paths are validated against a shell-metacharacter blocklist.
Blocked characters: `;`, `&`, `|`, `$`, `` ` ``, `(`, `)`, `<`, `>`, `"`, `'`, `\`, and newlines.
Validation failure throws `IllegalArgumentException` before any shell command runs.

## Model classes populated by the parser

The parsing pipeline populates these objects (used by all set handlers):

- **`BidsFile`** — relative path, suffix, extension, `List<BidsEntity>` entities
- **`BidsEntity`** — name+value pair (e.g. `subject=sub-01`); `normalizeName()` maps short↔long
- **`BidsDataset`** — root path, dataset name, `List<BidsFile>` files, `List<Map>` participants (from `participants.tsv`)
- **`BidsChannelData`** — internal carrier from set handlers; wraps grouped file refs + `bidsParentDir`; `toChannelTuple()` serializes to `[groupingKey, enrichedData]`
