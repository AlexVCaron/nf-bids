#!/usr/bin/env nextflow

/*
 * Integration test for combineBy operator
 * 
 * Tests the cartesian product with optional filtering functionality.
 * This validates that the operator works correctly in a pipeline context.
 */

nextflow.enable.dsl=2

// CRITICAL: You MUST include plugin operators explicitly, even though plugin is loaded in config
include { combineBy } from 'plugin/nf-bids'

workflow {
    
    println "\nTest 1: Basic combine without filter (cartesian product)\n"
    
    subjects = channel.of('sub-01', 'sub-02', 'sub-03')
    sessions = channel.of('ses-01', 'ses-02')
    
    subjects
        .combineBy(sessions)
        .map { subj, sess ->
            println "  Combined: ${subj} with ${sess}"
            [subject: subj, session: sess]
        }
        .collect()
    
    println "\nTest 2: Combine with filter (conditional combinations)\n"
    
    subjects2 = channel.of('sub-01', 'sub-02', 'sub-03')
    sessions2 = channel.of('ses-01', 'ses-02', 'ses-03')
    
    // Only combine if session number <= subject number
    subjects2
        .combineBy(sessions2) { subj, sess ->
            def subjNum = subj.split('-')[1] as Integer
            def sessNum = sess.split('-')[1] as Integer
            sessNum <= subjNum
        }
        .map { subj, sess ->
            println "  Filtered: ${subj} with ${sess}"
            [subject: subj, session: sess]
        }
        .collect()
    
    println "\nTest 3: Combine with complex maps\n"
    
    anatomical = channel.of(
        [type: 'T1w', params: [res: '1mm']],
        [type: 'T2w', params: [res: '1mm']]
    )
    
    contrasts = channel.of(
        [contrast: 'high', value: 0.8],
        [contrast: 'low', value: 0.2]
    )
    
    anatomical
        .combineBy(contrasts)
        .map { anat, contrast ->
            println "  Combined: ${anat.type} (${anat.params.res}) with ${contrast.contrast} contrast (${contrast.value})"
            [type: anat.type, params: anat.params, contrast: contrast.contrast, value: contrast.value]
        }
        .collect()
    
    println "\nTest 4: Combine with matching filter\n"
    
    images = channel.of(
        [id: 'A', modality: 'anat'],
        [id: 'B', modality: 'func']
    )
    
    processing = channel.of(
        [id: 'A', method: 'method1'],
        [id: 'B', method: 'method2'],
        [id: 'C', method: 'method3']
    )
    
    // Only combine if IDs match
    images
        .combineBy(processing) { img, proc -> img.id == proc.id }
        .map { img, proc ->
            println "  Matched: ${img.id} -> ${img.modality} with ${proc.method}"
            [id: img.id, modality: img.modality, method: proc.method]
        }
        .collect()
    
    println "\nTest 5: Combine all parameters with all datasets\n"
    
    parameters = channel.of(
        [threshold: 0.5, smoothing: 'low'],
        [threshold: 0.7, smoothing: 'high']
    )
    
    datasets = channel.of('dataset1', 'dataset2')
    
    parameters
        .combineBy(datasets)
        .map { param, dataset ->
            println "  Config: threshold=${param.threshold}, smoothing=${param.smoothing}, dataset=${dataset}"
            [threshold: param.threshold, smoothing: param.smoothing, dataset: dataset]
        }
        .collect()
    
    println "\nTest 6: Empty result with strict filter\n"
    
    left = channel.of('A', 'B', 'C')
    right = channel.of(1, 2, 3)
    
    // Filter that rejects everything
    left
        .combineBy(right) { l, r -> false }
        .map { l, r ->
            println "  This should never print"
            [left: l, right: r]
        }
        .collect()
    
    println "\nTest 7: Combine with numeric comparison\n"
    
    values1 = channel.of(1, 2, 3)
    values2 = channel.of(2, 4, 6)
    
    // Only combine if left < right
    values1
        .combineBy(values2) { v1, v2 -> v1 < v2 }
        .map { v1, v2 ->
            println "  Valid pair: ${v1} < ${v2}"
            [v1: v1, v2: v2]
        }
        .collect()
    
    println "\n✅ All combineBy tests completed!\n"
}
