#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Performance benchmark: join vs joinBy
 * 
 * Compares execution time and behavior between:
 * - Standard join(by: index)
 * - New joinBy { closure }
 */

include { joinBy } from 'plugin/nf-bids'

// ============================================================================
// Test Data Generation
// ============================================================================

def generateLeftData(size) {
    def data = []
    def subjects = (1..Math.min(size, 100)).collect { subId ->
        "sub-${String.format('%02d', subId)}"
    }
    
    size.times { i ->
        def subj = subjects[i % subjects.size()]
        data << [subj, "anatomical_${i}.nii.gz", [subject: subj, type: 'anat', index: i]]
    }
    
    return data
}

def generateRightData(size) {
    def data = []
    def subjects = (1..Math.min(size, 100)).collect { subId ->
        "sub-${String.format('%02d', subId)}"
    }
    
    size.times { i ->
        def subj = subjects[i % subjects.size()]
        data << [subj, "functional_${i}.nii.gz", [subject: subj, type: 'func', index: i]]
    }
    
    return data
}

// ============================================================================
// Benchmark Test 1: Small Dataset (100 items each)
// ============================================================================

workflow test_small_join {
    println "\n=========================================="
    println "TEST: join with 100 items per channel"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(100))
    def right = channel.from(generateRightData(100))
    
    left
        .join(right, by: 0)
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "join (100 items): ${endTime - startTime}ms"
            }
        )
}

workflow test_small_joinby {
    println "\n=========================================="
    println "TEST: joinBy with 100 items per channel"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(100))
    def right = channel.from(generateRightData(100))
    
    left
        .joinBy(right) { it -> it[0] }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "joinBy (100 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 2: Medium Dataset (1,000 items each)
// ============================================================================

workflow test_medium_join {
    println "\n=========================================="
    println "TEST: join with 1,000 items per channel"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(1000))
    def right = channel.from(generateRightData(1000))
    
    left
        .join(right, by: 0)
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "join (1,000 items): ${endTime - startTime}ms"
            }
        )
}

workflow test_medium_joinby {
    println "\n=========================================="
    println "TEST: joinBy with 1,000 items per channel"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(1000))
    def right = channel.from(generateRightData(1000))
    
    left
        .joinBy(right) { it -> it[0] }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "joinBy (1,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 3: Large Dataset (10,000 items each)
// ============================================================================

workflow test_large_join {
    println "\n=========================================="
    println "TEST: join with 10,000 items per channel"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(10000))
    def right = channel.from(generateRightData(10000))
    
    left
        .join(right, by: 0)
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "join (10,000 items): ${endTime - startTime}ms"
            }
        )
}

workflow test_large_joinby {
    println "\n=========================================="
    println "TEST: joinBy with 10,000 items per channel"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(10000))
    def right = channel.from(generateRightData(10000))
    
    left
        .joinBy(right) { it -> it[0] }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "joinBy (10,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 4: Semantic Joining (Map-based)
// ============================================================================

workflow test_semantic_joinby {
    println "\n=========================================="
    println "TEST: joinBy with semantic keys (1,000 items)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(1000).collect { it -> it[2] })  // Use map objects
    def right = channel.from(generateRightData(1000).collect { it -> it[2] })  // Use map objects
    
    left
        .joinBy(right) { it -> it.subject }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "joinBy semantic (1,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 5: Different Extractors
// ============================================================================

workflow test_different_extractors_joinby {
    println "\n=========================================="
    println "TEST: joinBy with different extractors (1,000 items)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def left = channel.from(generateLeftData(1000).collect { it ->
        [id: it[0], file: it[1], meta: it[2]] 
    })
    def right = channel.from(generateRightData(1000).collect { it ->
        [subject: it[0], file: it[1], meta: it[2]] 
    })
    
    left
        .joinBy(right, { it -> it.id }, { it -> it.subject })
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "joinBy different extractors (1,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Main Workflow
// ============================================================================

workflow {
    println "=========================================="
    println "Performance Benchmark: join vs joinBy"
    println "=========================================="
    println "Nextflow version: ${nextflow.version}"
    println "Plugin: nf-bids"
    println ""
    
    // Run all benchmarks
    test_small_join()
    test_small_joinby()
    
    test_medium_join()
    test_medium_joinby()
    
    test_large_join()
    test_large_joinby()
    
    test_semantic_joinby()
    test_different_extractors_joinby()
    
    println "\n=========================================="
    println "Benchmark Complete"
    println "=========================================="
}
