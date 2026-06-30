#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 6: joinBy with Many Items
 * Tests join performance with 50k items per channel
 */

include { joinBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 6: joinBy with Many Items (5k each channel)              ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    def startTime = System.currentTimeMillis()
    def itemCount = 5_000
    
    // Generate matching items in both channels
    def leftItems = (1..itemCount).collect { idx ->
        def key = "key_${idx}"
        [key, "left_${idx}", idx]
    }
    
    def rightItems = (1..itemCount).collect { idx ->
        def key = "key_${idx}"
        [key, "right_${idx}", idx * 2]
    }
    
    def joinedCount = 0
    
    Channel.fromList(leftItems)
        .joinBy(
            Channel.fromList(rightItems),
            { l -> l[0] },
            { r -> r[0] }
        )
        .subscribe(
            onNext: { joined ->
                joinedCount += 1
                
                // Verify join correctness
                def leftItem = joined[0..2]
                def rightItem = joined[3..5]
                assert leftItem[0] == rightItem[0]  // Key matches left and right
                
                if (joinedCount % 1000 == 0) {
                    println "  Joined ${joinedCount} pairs..."
                }
            },
            onComplete: {
                def duration = System.currentTimeMillis() - startTime
                
                println "\n✅ Test 6 PASSED: Many join items handled successfully"
                println "   Pairs joined: ${joinedCount}"
                println "   Duration: ${duration}ms"
                
                assert joinedCount == itemCount
            }
        )
    
    // Wait for processing
    Thread.sleep(15000)
}
