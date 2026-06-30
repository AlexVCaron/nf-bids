#!/usr/bin/env nextflow
nextflow.enable.dsl=2

/*
 * Edge Case Test 3: Complex Nested Structures
 * Tests operators with realistic BIDS-like nested maps
 */

include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    println "╔════════════════════════════════════════════════════════════════╗"
    println "║  Test 3: groupTupleBy with Complex Nested Structures          ║"
    println "╚════════════════════════════════════════════════════════════════╝"
    
    channel.of(
        [
            subject: "sub-01",
            session: "ses-01",
            metadata: [
                acquisition: [date: "2025-01-01", scanner: "Siemens"],
                processing: [version: "1.0", steps: ["denoising", "registration"]]
            ],
            files: [
                anat: [t1: "t1.nii.gz", t2: "t2.nii.gz"],
                func: [bold: ["run-1.nii.gz", "run-2.nii.gz"]]
            ]
        ],
        [
            subject: "sub-01",
            session: "ses-02",
            metadata: [
                acquisition: [date: "2025-02-01", scanner: "Siemens"],
                processing: [version: "1.0", steps: ["denoising"]]
            ],
            files: [
                anat: [t1: "t1.nii.gz"],
                func: [bold: ["run-1.nii.gz"]]
            ]
        ],
        [
            subject: "sub-02",
            session: "ses-01",
            metadata: [
                acquisition: [date: "2025-01-15", scanner: "GE"],
                processing: [version: "2.0", steps: ["registration"]]
            ],
            files: [
                anat: [t1: "t1.nii.gz", flair: "flair.nii.gz"]
            ]
        ]
    )
    .groupTupleBy { item -> item.subject }
    .subscribe(
        onNext: { grouped ->
            def subject = grouped[0]
            def sessions = grouped[1]
            
            println "  Subject ${subject}: ${sessions.size()} sessions"
            sessions.each { session ->
                assert session.metadata.acquisition.date != null
                assert session.files.anat.t1 != null
                println "    - ${session.session}: scanner=${session.metadata.acquisition.scanner}, files=${session.files.keySet()}"
            }
        },
        onComplete: {
            println "\n✅ Test 3 PASSED: Nested structures handled successfully"
        }
    )
    
    Thread.sleep(2000)
}
