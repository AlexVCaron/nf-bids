---
description: "Guided creation of a new bids2nf YAML configuration file for a BIDS modality or dataset. Helps choose the right set type and avoid double-matching gotchas."
agent: "agent"
tools: [read, edit, search]
---

Help the user create a new bids2nf YAML configuration file. Work interactively — ask before generating.

Follow all rules in `.github/instructions/bids-config-yaml.instructions.md`.

## Interview

Ask the user:
1. **Modality / suffix** — what BIDS suffix(es) do they want to capture? (e.g. `T1w`, `dwi`, `bold`)
2. **loop_over entities** — which entities define a unique dataset unit? (e.g. subject + session)
3. **Grouping structure** — single file per subject? Multiple directions/echoes/flips? Nested?
4. **Multiple configs for the same suffix?** — e.g. DWI with and without phase-encoding direction. If yes, note the `exclude_entities` requirement.

## Recommendation

Based on the answers, recommend a set type with a one-line rationale:
- `plain_set` — one file slot, no grouping needed
- `named_set` — named slots by entity values (direction, flip, part…)
- `sequential_set` — ordered array by entity (echo, run…)
- `mixed_set` — named groups each containing a sequence

## Draft config

Generate a minimal YAML draft. Rules:
- Entity names must be **long form** (`direction`, `acquisition`, not `dir`, `acq`)
- `meta` cannot be used as a configKey
- Show `suffix_maps_to` if the configKey differs from the file suffix
- Show `exclude_entities` if multiple configKeys share a suffix

## Verify

Search `test/configs/` for an existing config for the same modality and show it as a reference.

## Confirm and save

Show the final draft and ask the user to confirm before writing the file.  
Suggest saving to `test/configs/config_<modality>.yaml` (or user-supplied path).
