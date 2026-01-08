#!/usr/bin/env nextflow

/*
 * Edge case test for combineBy operator (0.1.0-beta.5+)
 * 
 * Tests boundary conditions and edge cases:
 * - Null keys
 * - Duplicate keys with high cardinality
 * - Empty channels
 * - Very large keys
 * - Complex object keys
 */

nextflow.enable.dsl=2

include { combineBy } from 'plugin/nf-bids'

workflow {
    
    println "\n=== Test 1: Null key handling (should skip gracefully) ===\n"
    
    left1 = channel.of(
        [id: 'A', value: 1],
        [id: null, value: 2],  // null key
        [id: 'B', value: 3]
    )
    
    right1 = channel.of(
        [id: 'A', data: 'x'],
        [id: null, data: 'y'],  // null key
        [id: 'B', data: 'z']
    )
    
    left1.combineBy(right1, { it.id })
        .count()
        .view { count -> "✓ Test 1: ${count} combinations (expected: 2, nulls skipped)" }
    
    println "\n=== Test 2: High cardinality within single key ===\n"
    
    // 50 left items × 50 right items for same key = 2500 combinations
    left2 = channel.from(
        (1..50).collect { [key: 'HIGH', value: it] }
    )
    
    right2 = channel.from(
        (1..50).collect { [key: 'HIGH', data: it] }
    )
    
    left2.combineBy(right2, { it.key })
        .count()
        .view { count -> "✓ Test 2: ${count} combinations (expected: 2500)" }
    
    println "\n=== Test 3: Many keys with single items ===\n"
    
    // 1000 unique keys, 1 item each = 1000 combinations
    left3 = channel.from(
        (1..1000).collect { [key: "k${it}", value: it] }
    )
    
    right3 = channel.from(
        (1..1000).collect { [key: "k${it}", data: it] }
    )
    
    left3.combineBy(right3, { it.key })
        .count()
        .view { count -> "✓ Test 3: ${count} combinations (expected: 1000)" }
    
    println "\n=== Test 4: Complex map keys ===\n"
    
    left4 = channel.of(
        [keyMap: [a: 1, b: 2], value: 'X'],
        [keyMap: [a: 1, b: 2], value: 'Y'],  // same key
        [keyMap: [a: 3, b: 4], value: 'Z']
    )
    
    right4 = channel.of(
        [keyMap: [a: 1, b: 2], data: 'P'],
        [keyMap: [a: 1, b: 2], data: 'Q'],   // same key
        [keyMap: [a: 3, b: 4], data: 'R']
    )
    
    left4.combineBy(right4, { it.keyMap })
        .count()
        .view { count -> "✓ Test 4: ${count} combinations (expected: 5 = 2×2 + 1×1)" }
    
    println "\n=== Test 5: Very long string keys ===\n"
    
    def longKey = 'k' * 1000  // 1000 character key
    
    left5 = channel.of(
        [key: longKey, value: 1],
        [key: 'short', value: 2]
    )
    
    right5 = channel.of(
        [key: longKey, data: 'a'],
        [key: 'short', data: 'b']
    )
    
    left5.combineBy(right5, { it.key })
        .count()
        .view { count -> "✓ Test 5: ${count} combinations (expected: 2)" }
    
    println "\n=== Test 6: Asymmetric key distribution ===\n"
    
    // Left: 100 items with key 'A', 1 item with key 'B'
    // Right: 1 item with key 'A', 100 items with key 'B'
    left6 = channel.from(
        (1..100).collect { [key: 'A', value: it] } + [[key: 'B', value: 999]]
    )
    
    right6 = channel.from(
        [[key: 'A', data: 1]] + (1..100).collect { [key: 'B', data: it] }
    )
    
    left6.combineBy(right6, { it.key })
        .count()
        .view { count -> "✓ Test 6: ${count} combinations (expected: 200 = 100×1 + 1×100)" }
    
    println "\n=== Test 7: Empty left channel ===\n"
    
    left7 = channel.empty()
    right7 = channel.of([key: 'A', data: 1])
    
    left7.combineBy(right7, { it.key })
        .count()
        .ifEmpty { 0 }
        .view { count -> "✓ Test 7: ${count} combinations (expected: 0)" }
    
    println "\n=== Test 8: Empty right channel ===\n"
    
    left8 = channel.of([key: 'A', value: 1])
    right8 = channel.empty()
    
    left8.combineBy(right8, { it.key })
        .count()
        .ifEmpty { 0 }
        .view { count -> "✓ Test 8: ${count} combinations (expected: 0)" }
    
    println "\n=== Test 9: List keys (converted to CompositeKey) ===\n"
    
    left9 = channel.of(
        [keyList: ['a', 'b'], value: 1],
        [keyList: ['a', 'b'], value: 2],  // duplicate
        [keyList: ['c', 'd'], value: 3]
    )
    
    right9 = channel.of(
        [keyList: ['a', 'b'], data: 'x'],
        [keyList: ['c', 'd'], data: 'y']
    )
    
    left9.combineBy(right9, { it.keyList })
        .count()
        .view { count -> "✓ Test 9: ${count} combinations (expected: 3 = 2×1 + 1×1)" }
    
    println "\n✅ All edge case tests completed!\n"
}
