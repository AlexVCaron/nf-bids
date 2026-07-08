#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Performance benchmark: combineBy key-based combination
 * 
 * Tests the new combineBy operator with key extraction and
 * cartesian product within groups.
 */

include { combineBy } from 'plugin/nf-bids'

workflow {
    println "\n=========================================="
    println "CombineBy Performance Benchmark (0.1.0-beta.5+)"
    println "=========================================="
    
    // Test 1: Small dataset with multiple keys
    println "\n=== Test 1: 20 items, 5 keys (2 left × 2 right per key) ==="
    def start1 = System.currentTimeMillis()
    
    def left1 = channel.from(
        (0..19).collect { i -> [key: "k${i % 5}", value: "L${i}"] }
    )
    def right1 = channel.from(
        (0..19).collect { i -> [key: "k${i % 5}", value: "R${i}"] }
    )
    
    left1
        .combineBy(right1, { it -> it.key })
        .count()
        .view { count ->
            def end1 = System.currentTimeMillis()
            "Result: ${count} combinations (expected: 80) in ${end1 - start1}ms"
        }
    
    // Test 2: Medium dataset with fewer keys (more items per key)
    println "\n=== Test 2: 60 items, 10 keys (3 left × 3 right per key) ==="
    def start2 = System.currentTimeMillis()
    
    def left2 = channel.from(
        (0..59).collect { i -> [key: "k${i % 10}", value: "L${i}"] }
    )
    def right2 = channel.from(
        (0..59).collect { i -> [key: "k${i % 10}", value: "R${i}"] }
    )
    
    left2
        .combineBy(right2, { it -> it.key })
        .count()
        .view { count ->
            def end2 = System.currentTimeMillis()
            "Result: ${count} combinations (expected: 360) in ${end2 - start2}ms"
        }
    
    // Test 3: Realistic BIDS scenario
    println "\n=== Test 3: BIDS-like scenario (subjects × sessions) ==="
    def start3 = System.currentTimeMillis()
    
    def subjects = channel.from(
        (1..20).collect { i ->
            [subject: "sub-${String.format('%02d', i)}", age: 20 + i]
        }
    )
    def sessions = channel.from(
        (1..20).collect { i ->
            (1..2).collect { j ->
                [subject: "sub-${String.format('%02d', i)}", session: "ses-${String.format('%02d', j)}"]
            }
        }.flatten()
    )
    
    subjects
        .combineBy(sessions, { it -> it.subject })
        .count()
        .view { count ->
            def end3 = System.currentTimeMillis()
            "Result: ${count} subject-session pairs (expected: 40) in ${end3 - start3}ms"
        }
    
    println "\n=========================================="
    println "Benchmark Complete"
    println "=========================================="
}
