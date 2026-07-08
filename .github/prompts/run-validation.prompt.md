---
description: "Run the full nf-bids validation suite: build, install, unit tests, dataset validation, edge cases."
agent: "agent"
tools: [execute, read]
---

Run the full nf-bids validation suite in order. **Stop and report on the first failure** — do not proceed to later steps if an earlier one fails.

All commands must run from the **repo root** unless a step explicitly says to `cd` elsewhere.

## Steps

1. **Compile** — `make assemble`
   - Confirms there are no Groovy compilation errors.

2. **Unit tests** — `make test`
   - Runs all Spock unit tests.

3. **Install** — `make install`
   - Required before any Nextflow/nf-test step; installs the just-built plugin into `~/.nextflow/plugins/`.

4. **Smoke test** — `cd test/validation && nextflow run main.nf`
   - Basic sanity check of the full pipeline on real data.

5. **Dataset validation suite** — `cd test/validation && bash test_datasets.sh`
   - Runs nf-test across all validation datasets.

6. **Edge cases** — `cd test/edge_cases && bash run_all_tests.sh`
   - Runs edge-case nf-test scenarios.

## Summary

After all steps, report:
- Which steps passed / failed
- Any error messages or test failure names
- Total pass/fail counts where available
