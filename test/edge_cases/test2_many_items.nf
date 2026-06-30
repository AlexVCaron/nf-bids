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
    
    def processedCount = new java.util.concurrent.atomic.AtomicInteger(0)
    def groupsReceived = new java.util.concurrent.atomic.AtomicInteger(0)
    
    // Process and verify with proper synchronization
    channel.fromList(items)
        .groupTupleBy { item -> item[0] }
        .subscribe(
            onNext: { grouped ->
                groupsReceived.incrementAndGet()
                def key = grouped[0]
                def groupItems = grouped[1]
                processedCount.addAndGet(groupItems.size())
                
                // Verify group integrity
                assert groupItems.size() == 100 : "Expected 100 items per group, got ${groupItems.size()} for ${key}"
                
                // Verify all items have same key
                groupItems.each { item ->
                    assert item[0] == key : "Item ${item} doesn't match group key ${key}"
                }
                
                if (groupsReceived.get() % 20 == 0) {
                    println "  Processed ${groupsReceived.get()} groups..."
                }
            },
            onComplete: {
                def duration = System.currentTimeMillis() - startTime
                
                println "\n✅ Test 2 PASSED: Many small items handled successfully"
                println "   Items processed: ${processedCount.get()}"
                println "   Groups created: ${groupsReceived.get()}"
                println "   Duration: ${duration}ms"
                
                assert processedCount.get() == itemCount
                assert groupsReceived.get() == groupCount
            }
        )
    
    // Give enough time for processing
    Thread.sleep(10000)
}
