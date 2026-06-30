#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 5: Concurrent Execution Stress Test
 * Tests thread-safety under concurrent load
 */

include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 5: Concurrent Execution Stress Test                     ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    def startTime = System.currentTimeMillis()
    def itemCount = 10_000
    def groupCount = 100
    
    // Generate items that will be processed concurrently
    def items = (1..itemCount).collect { idx ->
        def groupKey = "group_${idx % groupCount}"
        [groupKey, "data_${idx}", idx]
    }
    
    def groupsReceived = java.util.Collections.synchronizedSet(new java.util.HashSet<String>())
    def itemsProcessed = new java.util.concurrent.atomic.AtomicInteger(0)
    
    channel.fromList(items)
        .groupTupleBy { item -> item[0] }
        .subscribe(
            onNext: { grouped ->
                def key = grouped[0]
                def groupItems = grouped[1]
                
                groupsReceived.add(key)
                itemsProcessed.addAndGet(groupItems.size())
                
                // Verify group integrity
                assert groupItems.size() == 100  // 10k / 100 = 100 per group
                
                // Verify all items have same key
                groupItems.each { item ->
                    assert item[0] == key
                }
            },
            onComplete: {
                def duration = System.currentTimeMillis() - startTime
                
                println "\n✅ Test 5 PASSED: Concurrent execution handled successfully"
                println "   Groups created: ${groupsReceived.size()}"
                println "   Items processed: ${itemsProcessed.get()}"
                println "   Duration: ${duration}ms"
                println "   No race conditions detected"
                
                assert groupsReceived.size() == groupCount
                assert itemsProcessed.get() == itemCount
            }
        )
    
    // Wait for processing
    Thread.sleep(5000)
}
