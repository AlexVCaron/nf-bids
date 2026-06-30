#!/bin/bash
# Edge case test runner based on nf-test.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
PLUGIN_VERSION="0.1.0-beta.10"
PLUGINS_DIR="${NXF_PLUGINS_DIR:-$HOME/.nextflow/plugins}"

TEST_FILES=(
    "test/edge_cases/test1_large_items.nf.test"
    "test/edge_cases/test2_many_items.nf.test"
    "test/edge_cases/test3_nested_structures.nf.test"
    "test/edge_cases/test4_missing_fields.nf.test"
    "test/edge_cases/test5_concurrent.nf.test"
    "test/edge_cases/test6_join_many.nf.test"
    "test/edge_cases/test7_complex_filter.nf.test"
    "test/edge_cases/test8_combineby_edge_cases.nf.test"
)

usage() {
    cat << EOF
Usage: $0 [OPTIONS] [CASE_TAGS...]

Run edge-case suites via nf-test.

OPTIONS:
    -h, --help                Show this help message
    -v, --verbose             Show full Nextflow output
    -l, --list                List available case tags
        --update-snapshots    Re-record failing snapshots
        --clean-snapshots     Remove obsolete snapshots
        --ci                  Fail on snapshot differences (CI behavior)
        --dry-run             Discover tests without executing them

CASE_TAGS:
    Optional tags to run a subset (e.g. test1 test7 combineby).

EXAMPLES:
    $0
    $0 test7
    $0 --update-snapshots test1 test6
EOF
}

bootstrap_runtime() {
    echo "[bootstrap] Ensuring nf-bids plugin is installed..."
    (cd "${REPO_ROOT}" && ./gradlew install >/dev/null)

    if [[ ! -d "${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}" ]]; then
        echo "[bootstrap] ERROR: nf-bids plugin missing at ${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}"
        exit 1
    fi
}

resolve_nf_test_cmd() {
    if nf-test version >/dev/null 2>&1; then
        NF_TEST_CMD=(nf-test)
        return
    fi

    local jar_path="$HOME/.nf-test/nf-test.jar"
    if [[ -f "${jar_path}" ]]; then
        NF_TEST_CMD=(java -jar "${jar_path}")
        return
    fi

    echo "[bootstrap] ERROR: nf-test is not runnable."
    echo "[bootstrap] Expected either:"
    echo "  - a working nf-test launcher in PATH"
    echo "  - or ${jar_path}"
    echo "[bootstrap] Try: nf-test update"
    exit 1
}

VERBOSE=false
LIST_ONLY=false
UPDATE_SNAPSHOTS=false
CLEAN_SNAPSHOTS=false
CI_MODE=false
DRY_RUN=false
CASE_TAGS=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -l|--list)
            LIST_ONLY=true
            shift
            ;;
        --update-snapshots)
            UPDATE_SNAPSHOTS=true
            shift
            ;;
        --clean-snapshots)
            CLEAN_SNAPSHOTS=true
            shift
            ;;
        --ci)
            CI_MODE=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -*)
            echo "Unknown option: $1"
            usage
            exit 1
            ;;
        *)
            CASE_TAGS+=("$1")
            shift
            ;;
    esac
done

if [[ "${LIST_ONLY}" == true ]]; then
    echo "Available edge-case tags:"
    printf '  %s\n' test1 test2 test3 test4 test5 test6 test7 test8
    echo
    echo "Operator tags:"
    printf '  %s\n' grouptupleby joinby combineby
    exit 0
fi

bootstrap_runtime
resolve_nf_test_cmd

cd "${REPO_ROOT}"

CMD=("${NF_TEST_CMD[@]}" test -c nf-test.config)
if [[ "${VERBOSE}" == true ]]; then
    CMD+=(--verbose)
fi
if [[ "${UPDATE_SNAPSHOTS}" == true ]]; then
    CMD+=(--update-snapshot)
fi
if [[ "${CLEAN_SNAPSHOTS}" == true ]]; then
    CMD+=(--clean-snapshot)
fi
if [[ "${CI_MODE}" == true ]]; then
    CMD+=(--ci)
fi
if [[ "${DRY_RUN}" == true ]]; then
    CMD+=(--dry-run)
fi

for tag in "${CASE_TAGS[@]}"; do
    CMD+=(--tag "${tag}")
done

CMD+=("${TEST_FILES[@]}")

echo "Running edge cases via nf-test"
echo "Suites:"
printf '  %s\n' "${TEST_FILES[@]}"
if [[ ${#CASE_TAGS[@]} -gt 0 ]]; then
    echo "Tags: ${CASE_TAGS[*]}"
fi
echo

"${CMD[@]}"
