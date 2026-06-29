#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 2: Many Small Items
 * Tests operators with high volume (100k items)
 */

include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 2: groupTupleBy with Many Small Items (10k)             ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    def startTime = System.currentTimeMillis()
    def itemCount = 10_000
    def groupCount = 100
    
    // Generate 10k items across 100 groups
    def items = (1..itemCount).collect { idx ->
        def groupKey = "group_${idx % groupCount}"
        [groupKey, "data_${idx}", idx]
    }
    
    def processedCount = 0
    def groupsReceived = 0
    
    // Process and verify with proper synchronization
    Channel.fromList(items)
        .groupTupleBy { it[0] }
        .subscribe(
            onNext: { grouped ->
                groupsReceived++
                def key = grouped[0]
                def groupItems = grouped[1]
                processedCount += groupItems.size()
                
                // Verify group integrity
                assert groupItems.size() == 100, "Expected 100 items per group, got ${groupItems.size()} for ${key}"
                
                // Verify all items have same key
                groupItems.each { item ->
                    assert item[0] == key, "Item ${item} doesn't match group key ${key}"
                }
                
                if (groupsReceived % 20 == 0) {
                    println "  Processed ${groupsReceived} groups..."
                }
            },
            onComplete: {
                def duration = System.currentTimeMillis() - startTime
                
                println "\n✅ Test 2 PASSED: Many small items handled successfully"
                println "   Items processed: ${processedCount}"
                println "   Groups created: ${groupsReceived}"
                println "   Duration: ${duration}ms"
                
                assert processedCount == itemCount
                assert groupsReceived == groupCount
            }
        )
    
    // Give enough time for processing
    Thread.sleep(10000)
}
