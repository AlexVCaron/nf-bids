#!/usr/bin/env nextflow

/*
 * Research script to understand Nextflow's combine(by:) operator behavior
 * Testing: output format, key extraction, cartesian product within groups
 */

nextflow.enable.dsl=2

workflow {
    // Test 1: Basic combine with by parameter (single index)
    println "\n=== Test 1: Basic combine(by:) with index ==="
    
    left = Channel.of(
        ['A', 'left1'],
        ['A', 'left2'],
        ['B', 'left3']
    )
    
    right = Channel.of(
        ['A', 'right1'],
        ['A', 'right2'],
        ['B', 'right3']
    )
    
    left.combine(right, by: 0)
        .view { "Test 1 output: $it (size: ${it.size()})" }
    
    // Test 2: combine(by:) with multiple indices
    println "\n=== Test 2: combine(by:) with multiple indices ==="
    
    left2 = Channel.of(
        ['A', 'X', 'left1'],
        ['A', 'Y', 'left2'],
        ['B', 'X', 'left3']
    )
    
    right2 = Channel.of(
        ['A', 'X', 'right1'],
        ['A', 'X', 'right2'],
        ['B', 'X', 'right3']
    )
    
    left2.combine(right2, by: [0, 1])
        .view { "Test 2 output: $it (size: ${it.size()})" }
    
    // Test 3: Cartesian product within groups
    println "\n=== Test 3: Cartesian product within matching keys ==="
    
    left3 = Channel.of(
        ['KEY1', 'L1'],
        ['KEY1', 'L2'],
        ['KEY2', 'L3']
    )
    
    right3 = Channel.of(
        ['KEY1', 'R1'],
        ['KEY1', 'R2'],
        ['KEY2', 'R3']
    )
    
    left3.combine(right3, by: 0)
        .view { "Test 3 output: $it - Expected: KEY1 with 4 combinations (L1xR1, L1xR2, L2xR1, L2xR2)" }
    
    // Test 4: Unmatched keys
    println "\n=== Test 4: Unmatched keys ==="
    
    left4 = Channel.of(
        ['A', 'left1'],
        ['B', 'left2'],
        ['C', 'left3']
    )
    
    right4 = Channel.of(
        ['A', 'right1'],
        ['B', 'right2'],
        ['D', 'right3']
    )
    
    left4.combine(right4, by: 0)
        .view { "Test 4 output: $it - Keys C and D should not appear" }
    
    // Test 5: Empty channels
    println "\n=== Test 5: Empty channel behavior ==="
    
    left5 = Channel.of(['A', 'left1'])
    right5 = Channel.empty()
    
    left5.combine(right5, by: 0)
        .view { "Test 5 output: $it" }
        .ifEmpty { println "Test 5: Channel is empty (expected)" }
}
