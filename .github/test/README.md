# Local Workflow Testing with nektos act

This directory contains local test assets for running GitHub Actions workflows with nektos act.

## Goal

Run validation and benchmark workflows locally in test mode, while preventing any remote push side-effects.

- Validation workflow: runs normally in act.
- Benchmark workflow: runs benchmarks normally, but skips the remote commit/push step when ACT=true.

## Prerequisites

1. Docker installed and running.
2. nektos act installed.

### Install act on Linux

```bash
curl -s https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash
```

Verify install:

```bash
act --version
```

## Optional setup

Create a reusable secrets file for local runs:

```bash
cp .github/test/.secrets.example .github/test/.secrets.local
```

Then edit .github/test/.secrets.local if needed.

## Run validation workflow locally

From repository root (recommended, robust form):

```bash
act -C . pull_request \
  -P ubuntu-latest=catthehacker/ubuntu:full-latest \
  --workflows .github/workflows/validation.yml \
  --eventpath .github/test/events/pull_request_main.json \
  --secret-file .github/test/.secrets.local
```

If you do not need secrets:

```bash
act -C . pull_request \
  -P ubuntu-latest=catthehacker/ubuntu:full-latest \
  --workflows .github/workflows/validation.yml \
  --eventpath .github/test/events/pull_request_main.json
```

## Run benchmark workflow locally in test mode

Use a push-main event payload:

```bash
act -C . push \
  -P ubuntu-latest=catthehacker/ubuntu:full-latest \
  --workflows .github/workflows/benchmark.yml \
  --eventpath .github/test/events/push_main.json \
  --secret-file .github/test/.secrets.local
```

In act, the actor is `nektos/act`.
The benchmark workflow still runs the benchmark suite, but skips the remote commit/push step when running under act.

## Useful act flags

Use a larger image when action dependencies are heavy (or by default for this repo):

```bash
act -C . pull_request -P ubuntu-latest=catthehacker/ubuntu:full-latest \
  --workflows .github/workflows/validation.yml \
  --eventpath .github/test/events/pull_request_main.json
```

List all jobs in a workflow:

```bash
act -C . -W .github/workflows/validation.yml -l
```

Run a single job:

```bash
act -C . pull_request \
  -P ubuntu-latest=catthehacker/ubuntu:full-latest \
  --workflows .github/workflows/validation.yml \
  --eventpath .github/test/events/pull_request_main.json \
  --job validation
```

## Troubleshooting

- If you see checkout errors mentioning a temp path and "not located inside a git repository", run with `-C .` from repo root.
- If you see Java/Nextflow runtime issues in default act image, use `-P ubuntu-latest=catthehacker/ubuntu:full-latest`.
- If Docker auth warns during image pull, act usually retries unauthenticated successfully.

## Notes

- Local test assets live under .github/test and are versioned intentionally.
- Local secrets file .github/test/.secrets.local is ignored by git.
- The benchmark workflow has an act guard to prevent remote write behavior during local tests.
- Workflows include an act-only Nextflow fallback installer (`get.nextflow.io`) because some act images expose a non-executable toolcache Nextflow binary.
