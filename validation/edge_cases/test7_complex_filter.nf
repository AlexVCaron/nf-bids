#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 7: combineBy with Complex Filters
 * Tests complex filter predicates with combineBy
 */

include { combineBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 7: combineBy with Complex Filter Predicates             ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    def matchCount = 0
    
    def scans = Channel.of(
        [subject: "sub-01", modality: "T1w", quality: 0.9],
        [subject: "sub-02", modality: "T1w", quality: 0.7],
        [subject: "sub-03", modality: "T2w", quality: 0.95],
        [subject: "sub-04", modality: "T1w", quality: 0.85]
    )
    
    def analyses = Channel.of(
        [analysis: "segmentation", requires: "T1w", minQuality: 0.8],
        [analysis: "registration", requires: "T1w", minQuality: 0.6],
        [analysis: "denoising", requires: "T2w", minQuality: 0.9]
    )
    
    scans
        .combineBy(analyses, { it.modality }, { it.requires })
        .filter { key, leftItem, rightItem ->
            // Complex predicate: subject match AND modality match AND quality threshold
            leftItem.subject == "sub-01" &&
            leftItem.quality >= rightItem.minQuality
        }
        .subscribe(
            onNext: { key, leftItem, rightItem ->
                matchCount++
                println "  Match: ${leftItem.subject} ${leftItem.modality} (q=${leftItem.quality}) -> ${rightItem.analysis}"
                assert leftItem.modality == rightItem.requires
                assert leftItem.quality >= rightItem.minQuality
            },
            onComplete: {
                println "\n✅ Test 7 PASSED: Complex filter predicates work correctly"
                println "   Matches found: ${matchCount}"
                
                // sub-01 T1w q=0.9 should match segmentation (req T1w, min 0.8) and registration (req T1w, min 0.6)
                assert matchCount == 2
            }
        )
    
    // Wait for processing
    Thread.sleep(2000)
}
