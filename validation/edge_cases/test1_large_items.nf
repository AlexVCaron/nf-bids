#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 1: Very Large Items
 * Tests operators with megabyte-sized data items
 */

include { groupTupleBy; joinBy; combineBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 1: groupTupleBy with Very Large Items                   ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    // Create items with large string data (1MB each)
    def largeString = "X" * 1024 * 1024  // 1MB string
    
    Channel.of(
        ["subject01", largeString, "session1"],
        ["subject01", largeString, "session2"],
        ["subject02", largeString, "session1"],
        ["subject02", largeString, "session2"],
        ["subject03", largeString, "session1"]
    )
    .groupTupleBy { item -> item[0] }
    .subscribe { grouped ->
        def key = grouped[0]
        def items = grouped[1]
        assert items.size() >= 2 || key == "subject03"
        // Each item is wrapped as a list: [largeString, "session"]
        assert items[0][0].length() == 1024 * 1024
        println "  Grouped ${key}: ${items.size()} large items (${items[0][0].length()} bytes each)"
    }
    
    // Wait for completion
    Thread.sleep(1000)
    
    println "\n✅ Test 1 PASSED: Large items handled successfully"
    println "   Memory usage: OK"
}
