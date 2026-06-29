#!/usr/bin/env nextflow

/*
 * Integration test for joinBy operator
 * 
 * Tests the closure-based join functionality with real Nextflow execution.
 * This validates that the operator works correctly in a pipeline context.
 */

nextflow.enable.dsl=2

// CRITICAL: You MUST include plugin operators explicitly, even though plugin is loaded in config
include { joinBy } from 'plugin/nf-bids'

workflow {
    
    println "\nTest 1: Basic join with same key extractor\n"
    
    anatomical = channel.of(
        [subject: 'sub-01', anat_file: 't1.nii'],
        [subject: 'sub-02', anat_file: 't1.nii']
    )
    
    functional = channel.of(
        [subject: 'sub-01', bold_file: 'bold.nii'],
        [subject: 'sub-02', bold_file: 'bold.nii']
    )
    
    anatomical
        .joinBy(functional) { it.subject }
        .map { fused ->
            println "  Joined: ${fused.subject} -> T1w + bold"
            [subject: fused.subject, t1: fused.anat_file, bold: fused.bold_file]
        }
        .collect()
    
    println "\nTest 2: Join with different key extractors\n"
    
    subjects = channel.of(
        [id: 'sub-01', data: 'subj_data'],
        [id: 'sub-02', data: 'subj_data']
    )
    
    participants = channel.of(
        [participant_id: 'sub-01', info: 'participant_info'],
        [participant_id: 'sub-02', info: 'participant_info']
    )
    
    subjects
        .joinBy(participants, { it.id }, { it.participant_id })
        .map { fused ->
            println "  Joined: id=${fused.id}, participant_id=${fused.participant_id}"
            [id: fused.id, data: fused.data, info: fused.info]
        }
        .collect()
    
    println "\nTest 3: Join with nested field extraction\n"
    
    images = channel.of(
        [meta: [sub: '01', ses: '01'], image_file: 'img.nii'],
        [meta: [sub: '02', ses: '01'], image_file: 'img.nii']
    )
    
    masks = channel.of(
        [meta: [sub: '01', ses: '01'], mask_file: 'mask.nii'],
        [meta: [sub: '02', ses: '01'], mask_file: 'mask.nii']
    )
    
    images
        .joinBy(masks) { it.meta.sub }
        .map { fused ->
            println "  Joined: sub-${fused.meta.sub} -> ${fused.image_file} + ${fused.mask_file}"
            [subject: fused.meta.sub, image: fused.image_file, mask: fused.mask_file]
        }
        .collect()
    
    println "\nTest 4: Join with composite key\n"
    
    runs1 = channel.of(
        [subject: 'sub-01', session: 'ses-01', run: 1, anat_type: 'anat'],
        [subject: 'sub-01', session: 'ses-02', run: 1, anat_type: 'anat']
    )
    
    runs2 = channel.of(
        [subject: 'sub-01', session: 'ses-01', run: 1, func_type: 'func'],
        [subject: 'sub-01', session: 'ses-02', run: 1, func_type: 'func']
    )
    
    runs1
        .joinBy(runs2) { [it.subject, it.session] }
        .map { fused ->
            println "  Joined: [${fused.subject}, ${fused.session}] -> ${fused.anat_type} + ${fused.func_type}"
            [subject: fused.subject, session: fused.session, anat: fused.anat_type, func: fused.func_type]
        }
        .collect()
    
    println "\nTest 5: Inner join (no remainder) - unmatched items dropped\n"
    
    left = channel.of(
        [id: 'A', val: 1],
        [id: 'B', val: 2],
        [id: 'C', val: 3]
    )
    
    right = channel.of(
        [id: 'A', data: 'x'],
        [id: 'B', data: 'y']
        // Note: No 'C' in right channel
    )
    
    left
        .joinBy(right, { it.id }, [remainder: false])
        .map { fused ->
            println "  Matched: ${fused.id} -> val=${fused.val}, data=${fused.data}"
            [id: fused.id, val: fused.val, data: fused.data]
        }
        .collect()
    
    println "\nTest 6: Outer join (with remainder) - unmatched items included\n"
    
    left2 = channel.of(
        [id: 'A', val: 1],
        [id: 'B', val: 2],
        [id: 'C', val: 3]
    )
    
    right2 = channel.of(
        [id: 'A', data: 'x'],
        [id: 'B', data: 'y'],
        [id: 'D', data: 'z']
    )
    
    left2
        .joinBy(right2, { it.id }, [remainder: true])
        .map { fused ->
            def leftId = fused.containsKey('val') ? fused.id : 'null'
            def rightId = fused.containsKey('data') ? fused.id : 'null'
            def val = fused.containsKey('val') ? fused.val : 'null'
            def data = fused.containsKey('data') ? fused.data : 'null'
            println "  Result: id=${fused.id}, left=${leftId}, right=${rightId}, val=${val}, data=${data}"
            [id: fused.id, left_id: leftId, right_id: rightId, val: val, data: data]
        }
        .collect()
    
    println "\n✅ All joinBy tests completed!\n"
}
