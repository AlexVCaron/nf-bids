#!/usr/bin/env nextflow

/*
 * Integration test for combineBy operator (v0.1.0-beta.5+)
 * 
 * Tests key-based combination with cartesian product within groups.
 * This validates that the operator works correctly in a pipeline context.
 * 
 * BREAKING CHANGE from beta.4:
 * - Now uses key extractors instead of filter predicates
 * - Emits [key, leftItem, rightItem] tuples (includes key)
 */

nextflow.enable.dsl=2

// CRITICAL: You MUST include plugin operators explicitly, even though plugin is loaded in config
include { combineBy } from 'plugin/nf-bids'

workflow {
    
    println "\n=== Test 1: Basic key-based combination ===\n"
    
    subjects = channel.of(
        [id: 'sub-01', age: 25],
        [id: 'sub-02', age: 30]
    )
    sessions = channel.of(
        [id: 'sub-01', session: 'ses-01'],
        [id: 'sub-02', session: 'ses-01']
    )
    
    subjects
        .combineBy(
            sessions,
            { it.id },      // extract key from left
            { it.id }       // extract key from right
        )
        .map { key, subj, sess ->
            println "  Key=${key}: Subject(age=${subj.age}) × Session(${sess.session})"
            [key: key, age: subj.age, session: sess.session]
        }
        .collect()
    
    println "\n=== Test 2: Cartesian product within groups ===\n"
    
    scans = channel.of(
        [subject: 'sub-01', scan: 'T1w'],
        [subject: 'sub-01', scan: 'T2w'],
        [subject: 'sub-02', scan: 'dwi']
    )
    
    params = channel.of(
        [subject: 'sub-01', tr: 2000],
        [subject: 'sub-01', tr: 3000],
        [subject: 'sub-02', tr: 2500]
    )
    
    // Should produce 2×2=4 combinations for sub-01, 1×1=1 for sub-02
    scans
        .combineBy(
            params,
            { it.subject },
            { it.subject }
        )
        .map { key, scan, param ->
            println "  Key=${key}: ${scan.scan} with TR=${param.tr}ms"
            [key: key, scan: scan.scan, tr: param.tr]
        }
        .collect()
    
    println "\n=== Test 3: Different key extractors (asymmetric) ===\n"
    
    images = channel.of(
        [subjectId: 'sub-01', modality: 'anat'],
        [subjectId: 'sub-02', modality: 'func']
    )
    
    processing = channel.of(
        [subjId: 'sub-01', method: 'method1'],
        [subjId: 'sub-02', method: 'method2']
    )
    
    // Use different fields for key extraction
    images
        .combineBy(
            processing,
            { it.subjectId },   // extract from 'subjectId' field
            { it.subjId }       // extract from 'subjId' field
        )
        .map { key, img, proc ->
            println "  Key=${key}: ${img.modality} + ${proc.method}"
            [key: key, modality: img.modality, method: proc.method]
        }
        .collect()
    
    println "\n=== Test 4: Unmatched keys (should be dropped) ===\n"
    
    left = channel.of(
        [id: 'A', value: 1],
        [id: 'B', value: 2],
        [id: 'C', value: 3]
    )
    
    right = channel.of(
        [id: 'A', value: 10],
        [id: 'B', value: 20],
        [id: 'D', value: 30]  // D has no match in left
    )
    
    // Only A and B should produce output (C and D dropped)
    left
        .combineBy(
            right,
            { it.id },
            { it.id }
        )
        .map { key, l, r ->
            println "  Key=${key}: left=${l.value}, right=${r.value}"
            [key: key, left: l.value, right: r.value]
        }
        .collect()
    
    println "\n=== Test 5: Composite key extraction ===\n"
    
    dwi = channel.of(
        [sub: 'sub-01', ses: 'ses-01', type: 'dwi'],
        [sub: 'sub-01', ses: 'ses-02', type: 'dwi']
    )
    
    anat = channel.of(
        [sub: 'sub-01', ses: 'ses-01', type: 'T1w'],
        [sub: 'sub-01', ses: 'ses-02', type: 'T1w']
    )
    
    // Extract composite key (subject + session)
    dwi
        .combineBy(
            anat,
            { "${it.sub}_${it.ses}" },  // composite key
            { "${it.sub}_${it.ses}" }
        )
        .map { key, dwiScan, anatScan ->
            println "  Key=${key}: ${dwiScan.type} + ${anatScan.type}"
            [key: key, dwi: dwiScan.type, anat: anatScan.type]
        }
        .collect()
    
    println "\n=== Test 6: String key extraction from simple values ===\n"
    
    subjects_simple = channel.of('sub-01', 'sub-02', 'sub-03')
    sessions_simple = channel.of('ses-01', 'ses-02', 'ses-03')
    
    // Extract numeric part as key (combine sub-N with ses-N)
    subjects_simple
        .combineBy(
            sessions_simple,
            { it.split('-')[1] },   // extract '01', '02', '03'
            { it.split('-')[1] }
        )
        .map { key, subj, sess ->
            println "  Key=${key}: ${subj} × ${sess}"
            [key: key, subject: subj, session: sess]
        }
        .collect()
    
    println "\n=== Test 7: Empty channel (no output expected) ===\n"
    
    populated = channel.of([id: 'A', val: 1])
    empty = channel.empty()
    
    populated
        .combineBy(
            empty,
            { it.id },
            { it.id }
        )
        .map { key, l, r ->
            println "  ERROR: This should never print"
            [key: key, l: l, r: r]
        }
        .ifEmpty { println "  ✓ Empty result as expected" }
        .collect()
    
    println "\n=== Test 8: Complex map keys ===\n"
    
    experiments = channel.of(
        [id: [project: 'P1', exp: 'E1'], data: 'exp1_data'],
        [id: [project: 'P1', exp: 'E2'], data: 'exp2_data']
    )
    
    analyses = channel.of(
        [id: [project: 'P1', exp: 'E1'], method: 'GLM'],
        [id: [project: 'P1', exp: 'E2'], method: 'ICA']
    )
    
    experiments
        .combineBy(
            analyses,
            { it.id },
            { it.id }
        )
        .map { key, exp, ana ->
            println "  Key=${key}: ${exp.data} → ${ana.method}"
            [key: key, data: exp.data, method: ana.method]
        }
        .collect()
    
    println "\n✅ All combineBy tests completed!\n"
}
