#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 7: combineBy with Downstream Filters
 * Tests combineBy key extraction followed by downstream filtering
 */

include { combineBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 7: combineBy with Downstream Filter Predicates           ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    def matchCount = 0
    
    def scans = channel.of(
        [subject: "sub-01", modality: "T1w", quality: 0.9],
        [subject: "sub-02", modality: "T1w", quality: 0.7],
        [subject: "sub-03", modality: "T2w", quality: 0.95],
        [subject: "sub-04", modality: "T1w", quality: 0.85]
    )
    
    def analyses = channel.of(
        [analysis: "segmentation", requires: "T1w", minQuality: 0.8],
        [analysis: "registration", requires: "T1w", minQuality: 0.6],
        [analysis: "denoising", requires: "T2w", minQuality: 0.9]
    )
    
    scans
        .combineBy(analyses, { l -> l.modality }, { r -> r.requires })
        .filter { fusion ->
            // Complex predicate: subject match AND modality match AND quality threshold
            fusion.subject == "sub-01" && 
            fusion.modality == fusion.requires && 
            fusion.quality >= fusion.minQuality
        }
        .subscribe(
            onNext: { fused ->
                matchCount += 1
                println "  Match: ${fused.subject} ${fused.modality} (q=${fused.quality}) -> ${fused.analysis}"
                assert fused.modality == fused.requires
                assert fused.quality >= fused.minQuality
            },
            onComplete: {
                println "\n✅ Test 7 PASSED: combineBy + downstream filters work correctly"
                println "   Matches found: ${matchCount}"
                
                // sub-01 T1w q=0.9 should match segmentation (req T1w, min 0.8) and registration (req T1w, min 0.6)
                assert matchCount == 2
            }
        )
    
    // Wait for processing
    Thread.sleep(2000)
}
