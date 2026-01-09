#!/bin/bash

# Edge Case Test Runner
# Runs all edge case tests for closure-based channel operators

set -e  # Exit on first error

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Edge Case Test Suite - Closure-Based Channel Operators       ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"

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
