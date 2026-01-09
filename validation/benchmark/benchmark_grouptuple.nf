#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Performance benchmark: groupTuple vs groupTupleBy
 * 
 * Compares execution time and behavior between:
 * - Standard groupTuple(by: index) 
 * - New groupTupleBy { closure }
 */

include { groupTupleBy } from 'plugin/nf-bids'

// ============================================================================
// Test Data Generation
// ============================================================================

def generateTestData(size) {
    def data = []
    
    // Generate realistic BIDS-like data
    // Mix of subjects, sessions, runs to create grouping scenarios
    def subjects = (1..Math.min((int)(size/10), 100)).collect { "sub-${String.format('%02d', it)}" }
    def sessions = ['ses-01', 'ses-02']
    
    size.times { i ->
        def subj = subjects[i % subjects.size()]
        def sess = sessions[i % sessions.size()]
        
        // Use simple 2-element tuples for groupTuple compatibility
        data << [subj, sess]
    }
    
    return data
}

// ============================================================================
// Benchmark Test 1: Small Dataset (100 items)
// ============================================================================

workflow test_small_grouptuple {
    println "\n=========================================="
    println "TEST: groupTuple with 100 items"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    Channel
        .from(generateTestData(100))
        .groupTuple(by: 0)
        .map { key, sessions ->
            [key, sessions.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTuple (100 items): ${endTime - startTime}ms"
            }
        )
}

workflow test_small_grouptupleby {
    println "\n=========================================="
    println "TEST: groupTupleBy with 100 items"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    Channel
        .from(generateTestData(100))
        .groupTupleBy { it[0] }
        .map { key, items ->
            [key, items.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTupleBy (100 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 2: Medium Dataset (1,000 items)
// ============================================================================

workflow test_medium_grouptuple {
    println "\n=========================================="
    println "TEST: groupTuple with 1,000 items"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    Channel
        .from(generateTestData(1000))
        .groupTuple(by: 0)
        .map { key, sessions ->
            [key, sessions.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTuple (1,000 items): ${endTime - startTime}ms"
            }
        )
}

workflow test_medium_grouptupleby {
    println "\n=========================================="
    println "TEST: groupTupleBy with 1,000 items"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    Channel
        .from(generateTestData(1000))
        .groupTupleBy { it[0] }
        .map { key, items ->
            [key, items.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTupleBy (1,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 3: Large Dataset (10,000 items)
// ============================================================================

workflow test_large_grouptuple {
    println "\n=========================================="
    println "TEST: groupTuple with 10,000 items"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    Channel
        .from(generateTestData(10000))
        .groupTuple(by: 0)
        .map { key, sessions ->
            [key, sessions.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTuple (10,000 items): ${endTime - startTime}ms"
            }
        )
}

workflow test_large_grouptupleby {
    println "\n=========================================="
    println "TEST: groupTupleBy with 10,000 items"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    Channel
        .from(generateTestData(10000))
        .groupTupleBy { it[0] }
        .map { key, items ->
            [key, items.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTupleBy (10,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Benchmark Test 4: Semantic Grouping (Map-based)
// ============================================================================

workflow test_semantic_grouptupleby {
    println "\n=========================================="
    println "TEST: groupTupleBy with semantic keys (1,000 items)"
    println "=========================================="
    
    def startTime = System.currentTimeMillis()
    
    def subjects = (1..100).collect { "sub-${String.format('%02d', it)}" }
    def data = (1..1000).collect { i ->
        [subject: subjects[i % subjects.size()], session: "ses-0${(i % 2) + 1}", index: i]
    }
    
    Channel
        .from(data)
        .groupTupleBy { it.subject }
        .map { key, items ->
            [key, items.size()]
        }
        .subscribe(
            onNext: { },
            onComplete: {
                def endTime = System.currentTimeMillis()
                println "groupTupleBy semantic (1,000 items): ${endTime - startTime}ms"
            }
        )
}

// ============================================================================
// Main Workflow
// ============================================================================

workflow {
    println "=========================================="
    println "Performance Benchmark: groupTuple vs groupTupleBy"
    println "=========================================="
    println "Nextflow version: ${nextflow.version}"
    println "Plugin: nf-bids"
    println ""
    
    // Run all benchmarks
    test_small_grouptuple()
    test_small_grouptupleby()
    
    test_medium_grouptuple()
    test_medium_grouptupleby()
    
    test_large_grouptuple()
    test_large_grouptupleby()
    
    test_semantic_grouptupleby()
    
    println "\n=========================================="
    println "Benchmark Complete"
    println "=========================================="
}
