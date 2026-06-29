#!/bin/bash

# Edge Case Test Runner
# Runs all edge case tests for closure-based channel operators

set -e  # Exit on first error

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Edge Case Test Suite - Closure-Based Channel Operators       ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PLUGIN_VERSION="0.1.0-beta.10"
PLUGINS_DIR="${NXF_PLUGINS_DIR:-$HOME/.nextflow/plugins}"

ensure_nf_bids_plugin() {
    if [ -d "${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}" ]; then
        return 0
    fi

    echo "nf-bids plugin not found in ${PLUGINS_DIR}; installing with ./gradlew install"
    (cd "${REPO_ROOT}" && ./gradlew install > /dev/null)

    if [ ! -d "${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}" ]; then
        echo "Failed to install nf-bids plugin at ${PLUGINS_DIR}/nf-bids-${PLUGIN_VERSION}"
        return 1
    fi
}

ensure_nf_bids_plugin

# Track results
PASSED=0
FAILED=0
TESTS=()

run_test() {
    local test_file=$1
    local test_name=$2
    
    echo "Running: $test_name"
    if nextflow run "$test_file" > /dev/null 2>&1; then
        PASSED=$((PASSED + 1))
        TESTS+=("✅ $test_name")
    else
        FAILED=$((FAILED + 1))
        TESTS+=("❌ $test_name")
        echo "FAILED: $test_name"
    fi
    echo ""
}

# Run all tests
run_test "test1_large_items.nf" "Test 1: Very Large Items"
run_test "test2_many_items.nf" "Test 2: Many Small Items (100k)"
run_test "test3_nested_structures.nf" "Test 3: Complex Nested Structures"
run_test "test4_missing_fields.nf" "Test 4: Non-Existent Field Access"
run_test "test5_concurrent.nf" "Test 5: Concurrent Execution"
run_test "test6_join_many.nf" "Test 6: joinBy with Many Items"
run_test "test7_complex_filter.nf" "Test 7: combineBy Complex Filters"

# Summary
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Test Results Summary                                          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

for test in "${TESTS[@]}"; do
    echo "$test"
done

echo ""
echo "Total: $((PASSED + FAILED)) tests"
echo "Passed: $PASSED"
echo "Failed: $FAILED"

if [ $FAILED -eq 0 ]; then
    echo ""
    echo "✅ All edge case tests passed!"
    exit 0
else
    echo ""
    echo "❌ Some tests failed"
    exit 1
fi
