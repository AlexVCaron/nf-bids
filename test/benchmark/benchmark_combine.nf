#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Performance benchmark: combine(by:) vs combineBy
 * 
 * Compares execution time and behavior between:
 * - Standard combine (full cartesian product)
 * - Standard combine(by:) (key-based combination)
 * - New combineBy with closure-based key extraction
 */

include { combineBy } from 'plugin/nf-bids'

// ============================================================================
// Test Data Generation
// ============================================================================

def generateKeyedData(size, prefix, numKeys) {
    def data = []
    size.times { i ->
        def key = "key_${i % numKeys}"  // Distribute items across keys
        data << [key: key, value: "${prefix}_${i}"]
    }
    return data
}

def generateSimpleData(size, prefix) {
    def data = []
    size.times { i ->
        data << "${prefix}_${i}"
    }
    return data
}

// ============================================================================
// Benchmark Test 1: Small Dataset for Comparison
// ============================================================================

workflow test_small_combine {
    println "\n=========================================="
    println "BASELINE: Standard combine with 10x10 items (full cartesian)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateSimpleData(10, 'left'))
    def right = channel.from(generateSimpleData(10, 'right'))
    
    left
        .combine(right)
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "combine (10×10 = 100 combinations): ${endTime - startTime}ms"
            }
        )
}

workflow test_small_combineby {
    println "\n=========================================="
    println "TEST: combineBy with 10x10 items (5 keys, 2 items/key)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    // Generate data with 5 unique keys, 2 items per key on each side
    def left = channel.from(generateKeyedData(10, 'left', 5))
    def right = channel.from(generateKeyedData(10, 'right', 5))
    
    left
        .combineBy(
            right,
            { it -> it.key }  // extract key from both left and right (same extractor)
        )
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                // 5 keys × (2 left × 2 right) = 5 × 4 = 20 total combinations
                println "combineBy (5 keys, 2×2 per key = 20 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 2: Medium Dataset (60 items, 10 keys)
// ============================================================================

workflow test_medium_combineby {
    println "\n=========================================="
    println "TEST: combineBy with 60x60 items, 10 keys (6 items/key)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateKeyedData(60, 'left', 10))
    def right = channel.from(generateKeyedData(60, 'right', 10))
    
    left
        .combineBy(right, { it -> it.key })
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                // 10 keys × (6×6) = 360 combinations
                println "combineBy (10 keys, 6×6 per key = 360 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 3: Large Dataset (200 items, 20 keys)
// ============================================================================

workflow test_large_combineby {
    println "\n=========================================="
    println "TEST: combineBy with 200x200 items, 20 keys (10 items/key)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateKeyedData(200, 'left', 20))
    def right = channel.from(generateKeyedData(200, 'right', 20))
    
    left
        .combineBy(right, { it -> it.key })
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                // 20 keys × (10×10) = 2000 combinations
                println "combineBy (20 keys, 10×10 per key = 2000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 4: BIDS-Like Scenario (Subject × Session pairing)
// ============================================================================

workflow test_bids_combineby {
    println "\n=========================================="
    println "TEST: BIDS-like subject × session pairing (30 subjects, 2 sessions each)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def subjects = (1..30).collect { subId ->
        [subject: "sub-${String.format('%02d', subId)}", age: 20 + subId] 
    }
    
    def sessions = (1..30).collectMany { subId ->
        (1..2).collect { sessNum ->
            [subject: "sub-${String.format('%02d', subId)}", session: "ses-${String.format('%02d', sessNum)}"]
        }
    }
    
    def left = channel.from(subjects)
    def right = channel.from(sessions)
    
    left
        .combineBy(right, { it -> it.subject })
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                // 30 subjects × 2 sessions = 60 combinations
                println "combineBy BIDS (30 subjects, 1×2 per subject = 60 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Main Workflow
// ============================================================================

workflow {
    println "=========================================="
    println "Performance Benchmark: combineBy (0.1.0-beta.5)"
    println "=========================================="
    println "Nextflow version: ${nextflow.version}"
    println "Plugin: nf-bids"
    println "Testing: Key-based combination with cartesian product"
    println ""
    
    // Run all benchmarks
    test_small_combine()
    test_small_combineby()
    
    test_medium_combineby()
    
    test_large_combineby()
    
    test_bids_combineby()
    
    println "\n=========================================="
    println "Benchmark Complete"
    println "=========================================="
}
