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
        [subject: 'sub-01', type: 'T1w', file: 't1.nii'],
        [subject: 'sub-02', type: 'T1w', file: 't1.nii']
    )
    
    functional = channel.of(
        [subject: 'sub-01', type: 'bold', file: 'bold.nii'],
        [subject: 'sub-02', type: 'bold', file: 'bold.nii']
    )
    
    anatomical
        .joinBy(functional) { it.subject }
        .map { key, left, right -> 
            println "  Joined: ${key} -> T1w + bold"
            [subject: key, t1: left.file, bold: right.file]
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
        .map { key, left, right ->
            println "  Joined: ${key} (${left.id} with ${right.participant_id})"
            [id: key, data: left.data, info: right.info]
        }
        .collect()
    
    println "\nTest 3: Join with nested field extraction\n"
    
    images = channel.of(
        [meta: [sub: '01', ses: '01'], file: 'img.nii'],
        [meta: [sub: '02', ses: '01'], file: 'img.nii']
    )
    
    masks = channel.of(
        [meta: [sub: '01', ses: '01'], file: 'mask.nii'],
        [meta: [sub: '02', ses: '01'], file: 'mask.nii']
    )
    
    images
        .joinBy(masks) { it.meta.sub }
        .map { key, img, mask ->
            println "  Joined: sub-${key} -> ${img.file} + ${mask.file}"
            [subject: key, image: img.file, mask: mask.file]
        }
        .collect()
    
    println "\nTest 4: Join with composite key\n"
    
    runs1 = channel.of(
        [subject: 'sub-01', session: 'ses-01', run: 1, type: 'anat'],
        [subject: 'sub-01', session: 'ses-02', run: 1, type: 'anat']
    )
    
    runs2 = channel.of(
        [subject: 'sub-01', session: 'ses-01', run: 1, type: 'func'],
        [subject: 'sub-01', session: 'ses-02', run: 1, type: 'func']
    )
    
    runs1
        .joinBy(runs2) { [it.subject, it.session] }
        .map { key, anat, func ->
            println "  Joined: ${key} -> ${anat.type} + ${func.type}"
            [subject: anat.subject, session: anat.session, anat: anat.type, func: func.type]
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
        .map { key, l, r ->
            println "  Matched: ${key} -> val=${l.val}, data=${r.data}"
            [id: key, val: l.val, data: r.data]
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
        .map { key, l, r ->
            def leftId = l?.id ?: 'null'
            def rightId = r?.id ?: 'null'
            def val = l?.val ?: 'null'
            def data = r?.data ?: 'null'
            println "  Result: key=${key}, left=${leftId}, right=${rightId}, val=${val}, data=${data}"
            [key: key, left_id: leftId, right_id: rightId, val: val, data: data]
        }
        .collect()
    
    println "\n✅ All joinBy tests completed!\n"
}
