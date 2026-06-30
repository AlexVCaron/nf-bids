#!/usr/bin/env nextflow

/*
 * Integration test for groupTupleBy operator
 * 
 * IMPORTANT: The include statement is REQUIRED to use plugin operators,
 * even though the plugin is loaded via nextflow.config.
 */

nextflow.enable.dsl=2

include { groupTupleBy } from 'plugin/nf-bids'

workflow {
    
    // Test 1: Basic grouping by simple key
    println "Test 1: Basic grouping by simple key"
    channel.of(
        [id: 'A', value: 1],
        [id: 'A', value: 2],
        [id: 'B', value: 3]
    )
    .groupTupleBy { it -> it.id }
    .view { key, items -> 
        println "  Key: ${key}, Items: ${items.size()}"
        assert key in ['A', 'B']
        if (key == 'A') assert items.size() == 2
        if (key == 'B') assert items.size() == 1
    }
    
    // Test 2: Nested field extraction
    println "\nTest 2: Nested field extraction"
    channel.of(
        [meta: [subject: 'sub-01'], file: 'f1.nii'],
        [meta: [subject: 'sub-01'], file: 'f2.nii'],
        [meta: [subject: 'sub-02'], file: 'f3.nii']
    )
    .groupTupleBy { it -> it.meta.subject }
    .view { key, items -> 
        println "  Subject: ${key}, Files: ${items.collect { it -> it.file }}"
        assert key in ['sub-01', 'sub-02']
    }
    
    // Test 3: Composite key (multiple fields)
    println "\nTest 3: Composite key grouping"
    channel.of(
        [subject: 'sub-01', session: 'ses-01', run: 1],
        [subject: 'sub-01', session: 'ses-01', run: 2],
        [subject: 'sub-01', session: 'ses-02', run: 1]
    )
    .groupTupleBy { it -> [it.subject, it.session] }
    .view { key, items -> 
        println "  Key: ${key}, Runs: ${items.collect { it -> it.run }}"
        assert items.size() >= 1
    }
    
    // Test 4: Grouping with sort option
    println "\nTest 4: Grouping with sort"
    channel.of(
        [id: 'A', value: 3],
        [id: 'A', value: 1],
        [id: 'A', value: 2]
    )
    .groupTupleBy({ it -> it.id }, [sort: { it -> it.value }])
    .view { key, items -> 
        println "  Key: ${key}, Sorted values: ${items.collect { it -> it.value }}"
        assert items[0].value == 1
        assert items[1].value == 2
        assert items[2].value == 3
    }
    
    // Test 5: Computed key extraction
    println "\nTest 5: Computed key from path"
    channel.of(
        [path: '/data/sub-01/anat/T1.nii'],
        [path: '/data/sub-01/func/bold.nii'],
        [path: '/data/sub-02/anat/T1.nii']
    )
    .groupTupleBy { it -> it.path.split('/')[2] }
    .view { key, items -> 
        println "  Subject: ${key}, Files: ${items.size()}"
        assert key in ['sub-01', 'sub-02']
    }
    
    println "\n✅ All groupTupleBy tests passed!"
}
