#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 4: Non-Existent Field Access
 * Tests graceful handling of missing fields in closures
 */

include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 4: Handling Non-Existent Field Access                   ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    channel.of(
        [subject: "sub-01", session: "ses-01", run: 1],
        [subject: "sub-01", session: "ses-02", run: 2],
        [subject: "sub-02", session: "ses-01"]  // Missing 'run' field
    )
    .groupTupleBy { item -> 
        // Access potentially missing field - Groovy returns null for missing fields
        item.subject ?: "unknown"
    }
    .subscribe(
        onNext: { grouped ->
            def key = grouped[0]
            def items = grouped[1]
            println "  Grouped ${key}: ${items.size()} items"
            assert key != null
            
            // Verify items can have different fields
            items.each { item ->
                println "    - session: ${item.session}, run: ${item.run ?: 'N/A'}"
            }
        },
        onComplete: {
            println "\n✅ Test 4 PASSED: Missing fields handled gracefully"
        }
    )
    
    Thread.sleep(2000)
}
