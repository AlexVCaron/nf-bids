#!/usr/bin/env nextflow

/*
 * Test workflow for flattened output format
 * Validates the default flattened output structure
 */

include { fromBIDS } from "plugin/nf-bids"

params.bids_dir = params.bids_dir ?: "${projectDir}/../data/custom/ds-dwi2"
params.config = params.config ?: "${projectDir}/../configs/config_dwi.yaml"
params.use_legacy = false
params.libbids_sh = null

workflow test_flattened_output {
    main:
        // Use flattened output by default, or legacy if specified
        def options = params.use_legacy ? [flatten_output: false] : [:]
        if (params.libbids_sh) {
            options.libbids_sh = params.libbids_sh
        }
        
        def ch = Channel.fromBIDS(params.bids_dir, params.config, options)
        
        // Simply pass through items for testing
        // Validation will be done in the nf-test assertions
        def validated_ch = ch.map { item ->
            if (params.use_legacy) {
                println "Legacy format item: [${item[0]}, enrichedData]"
            } else {
                def suffixes = item.keySet().findAll { it != 'meta' }
                println "Flattened format item: meta=${item.meta.keySet()}, suffixes=${suffixes}"
            }
            return item
        }
    
    emit:
        test_results = validated_ch
}

workflow {
    test_flattened_output()
    test_flattened_output.out.test_results.view { item ->
        if (params.use_legacy) {
            "Legacy: ${item[0]}"
        } else {
            "Flattened: subject=${item.meta.subject}, suffixes=${item.keySet().findAll { it != 'meta' }}"
        }
    }
}
