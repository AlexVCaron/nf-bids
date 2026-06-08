#!/usr/bin/env bash
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/build/test-runner-logs"
mkdir -p "${LOG_DIR}"

if [[ -d "${ROOT_DIR}/.tmp-tools/bin" ]]; then
  export PATH="${ROOT_DIR}/.tmp-tools/bin:${PATH}"
fi

export TERM="${TERM:-xterm}"
export NXF_ANSI_LOG=false

PASS=0
FAIL=0
SKIP=0
RESULTS=()

run_step() {
  local name="$1"
  local command="$2"
  local logfile="${LOG_DIR}/$(echo "${name}" | tr ' /' '__').log"

  echo
  echo "============================================================"
  echo "▶ ${name}"
  echo "============================================================"

  local start end duration
  start=$(date +%s)

  if bash -lc "cd '${ROOT_DIR}' && ${command}" >"${logfile}" 2>&1; then
    end=$(date +%s)
    duration=$((end - start))
    echo "✅ PASS (${duration}s)"
    echo "   log: ${logfile}"
    RESULTS+=("PASS|${name}|${duration}|${logfile}")
    PASS=$((PASS + 1))
  else
    end=$(date +%s)
    duration=$((end - start))
    echo "❌ FAIL (${duration}s)"
    echo "   log: ${logfile}"
    tail -n 40 "${logfile}" || true
    RESULTS+=("FAIL|${name}|${duration}|${logfile}")
    FAIL=$((FAIL + 1))
  fi
}

check_cmd() {
  local cmd="$1"
  local name="$2"
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "⚠️  SKIP: ${name} (missing '${cmd}' in PATH)"
    RESULTS+=("SKIP|${name}|0|missing:${cmd}")
    SKIP=$((SKIP + 1))
    return 1
  fi
  return 0
}

echo "nf-bids unified test runner"
echo "Root: ${ROOT_DIR}"
echo "Logs: ${LOG_DIR}"

run_step "Gradle unit tests" "./gradlew test"
run_step "Install plugin locally" "./gradlew install"

if check_cmd "nextflow" "Nextflow suites"; then
  for nf_script in \
    validation/main.nf \
    validation/main_flat.nf \
    validation/test_combineby.nf \
    validation/test_grouptupleby.nf \
    validation/test_joinby.nf \
    validation/test_path_types.nf \
    validation/test_process_path_input.nf \
    validation/test_flattened_output.nf; do
    run_step "Nextflow script: ${nf_script}" "nextflow run '${nf_script}'"
  done

  run_step "Edge case suite" "bash validation/edge_cases/run_all_tests.sh"
fi

if check_cmd "nf-test" "nf-test suites"; then
  run_step "nf-test snapshots" "nf-test test validation/comparison_custom_datasets.nf.test validation/comparison_mixed_sets.nf.test validation/comparison_named_sets.nf.test validation/comparison_plain_sets.nf.test validation/comparison_sequential_sets.nf.test validation/test_flattened_output.nf.test validation/test_heterogeneous_suffix_mapping.nf.test"
fi

echo
printf '%-8s | %-58s | %-8s | %s\n' "STATUS" "SUITE" "SECONDS" "LOG"
printf '%s\n' "$(printf '%.0s-' {1..120})"
for row in "${RESULTS[@]}"; do
  IFS='|' read -r status name seconds log <<< "${row}"
  printf '%-8s | %-58s | %-8s | %s\n' "${status}" "${name:0:58}" "${seconds}" "${log}"
done

echo
TOTAL=$((PASS + FAIL + SKIP))
echo "Summary: total=${TOTAL} pass=${PASS} fail=${FAIL} skip=${SKIP}"

if [[ ${FAIL} -gt 0 ]]; then
  exit 1
fi
