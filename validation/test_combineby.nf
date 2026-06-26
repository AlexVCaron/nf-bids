#!/usr/bin/env nextflow

/*
 * Integration test for combineBy operator (0.1.0-beta.5+)
 * 
 * Tests key-based combination with cartesian product within groups.
 * This validates that the operator works correctly in a pipeline context.
 * 
 * BREAKING CHANGE from beta.4:
 * - Now uses key extractors instead of filter predicates
 * - Emits fused items (matching key is not included in output payload)
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
        .map { fused ->
            println "  Subject=${fused.id}: age=${fused.age}, session=${fused.session}"
            [id: fused.id, age: fused.age, session: fused.session]
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
        .map { fused ->
            println "  Subject=${fused.subject}: ${fused.scan} with TR=${fused.tr}ms"
            [subject: fused.subject, scan: fused.scan, tr: fused.tr]
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
        .map { fused ->
            println "  Subject(left=${fused.subjectId}, right=${fused.subjId}): ${fused.modality} + ${fused.method}"
            [subject: fused.subjectId, modality: fused.modality, method: fused.method]
        }
        .collect()
    
    println "\n=== Test 4: Unmatched keys (should be dropped) ===\n"
    
    left = channel.of(
        [id: 'A', left_value: 1],
        [id: 'B', left_value: 2],
        [id: 'C', left_value: 3]
    )
    
    right = channel.of(
        [id: 'A', right_value: 10],
        [id: 'B', right_value: 20],
        [id: 'D', right_value: 30]  // D has no match in left
    )
    
    // Only A and B should produce output (C and D dropped)
    left
        .combineBy(
            right,
            { it.id },
            { it.id }
        )
        .map { fused ->
            println "  Id=${fused.id}: left=${fused.left_value}, right=${fused.right_value}"
            [id: fused.id, left: fused.left_value, right: fused.right_value]
        }
        .collect()
    
    println "\n=== Test 5: Composite key extraction ===\n"
    
    dwi = channel.of(
        [sub: 'sub-01', ses: 'ses-01', dwi_type: 'dwi'],
        [sub: 'sub-01', ses: 'ses-02', dwi_type: 'dwi']
    )
    
    anat = channel.of(
        [sub: 'sub-01', ses: 'ses-01', anat_type: 'T1w'],
        [sub: 'sub-01', ses: 'ses-02', anat_type: 'T1w']
    )
    
    // Extract composite key (subject + session)
    dwi
        .combineBy(
            anat,
            { "${it.sub}_${it.ses}" },  // composite key
            { "${it.sub}_${it.ses}" }
        )
        .map { fused ->
            println "  Subject=${fused.sub}, Session=${fused.ses}: ${fused.dwi_type} + ${fused.anat_type}"
            [subject: fused.sub, session: fused.ses, dwi: fused.dwi_type, anat: fused.anat_type]
        }
        .collect()
    
    println "\n=== Test 6: String key extraction from simple values ===\n"
    
    subjects_simple = channel.of([subject: 'sub-01'], [subject: 'sub-02'], [subject: 'sub-03'])
    sessions_simple = channel.of([session: 'ses-01'], [session: 'ses-02'], [session: 'ses-03'])
    
    // Extract numeric part as key (combine sub-N with ses-N)
    subjects_simple
        .combineBy(
            sessions_simple,
            { it.subject.split('-')[1] },   // extract '01', '02', '03'
            { it.session.split('-')[1] }
        )
        .map { fused ->
            println "  Pair: ${fused.subject} × ${fused.session}"
            [subject: fused.subject, session: fused.session]
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
        .map { fused ->
            println "  ERROR: This should never print"
            fused
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
        .map { fused ->
            println "  Key=${fused.id}: ${fused.data} → ${fused.method}"
            [id: fused.id, data: fused.data, method: fused.method]
        }
        .collect()
    
    println "\n✅ All combineBy tests completed!\n"
}
