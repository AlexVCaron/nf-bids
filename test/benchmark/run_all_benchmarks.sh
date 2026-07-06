#!/bin/bash

set -euo pipefail

cd "$(dirname "$0")"

bootstrap_runtime() {
  local repo_root
  repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
  echo "[bootstrap] Installing nf-bids plugin locally..."
  (cd "${repo_root}" && ./gradlew install >/dev/null)
}

bootstrap_runtime

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PLUGIN_VERSION="0.1.0-beta.11"
PLUGINS_DIR="${NXF_PLUGINS_DIR:-$HOME/.nextflow/plugins}"

ensure_nf_bids_plugin() {
  if [ -d "${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}" ]; then
    return 0
  fi

  echo "nf-bids plugin not found in ${PLUGINS_DIR}; installing with ./gradlew install"
  (cd "${REPO_ROOT}" && ./gradlew install > /dev/null)

  if [ ! -d "${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}" ]; then
    echo "Failed to install nf-bids plugin at ${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}" >&2
    return 1
  fi
}

ensure_nf_bids_plugin

{
  echo "# Benchmark Results"
  echo
  echo "Generated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')"
  echo

  for benchmark in \
    benchmark_grouptuple.nf \
    benchmark_join.nf \
    benchmark_combine.nf \
    benchmark_combineby_new.nf
  do
    echo "## ${benchmark}"
    echo
    nextflow run "${benchmark}"
    echo
  done
} | tee BENCHMARK_RESULTS.md