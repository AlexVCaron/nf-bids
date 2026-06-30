#!/usr/bin/env nextflow
nextflow.enable.dsl=2

// Test just groupTuple to see if it has issues

workflow {
    channel
        .from([
            ['sub-01', 'ses-01', 'run-1', 'file1.txt', [subject: 'sub-01']],
            ['sub-01', 'ses-02', 'run-1', 'file2.txt', [subject: 'sub-01']],
            ['sub-02', 'ses-01', 'run-1', 'file3.txt', [subject: 'sub-02']]
        ])
        .groupTuple(by: 0)
        .view()
}
