#!/usr/bin/env nextflow

/*
 * Validation test for Channel.fromBIDS() with flat output enabled
 * Used for testing flat output structure and heterogeneous suffix mappings
 */

include { fromBIDS } from "plugin/nf-bids"

params.bids_dir = "${projectDir}/data/custom/ds-dwi4"
params.config = "${projectDir}/configs/config_heterogeneous_dwi.yaml"

workflow main_workflow {
    println "🧪 Testing Channel.fromBIDS with flat output..."
    
    def options = params.libbids_sh ? 
        [libbids_sh: params.libbids_sh, flatten_output: true] : 
        [flatten_output: true]
    
    def ch = Channel.fromBIDS(params.bids_dir, params.config, options)
    
    // Process and output each item with detailed information
    def output_ch = ch.map { item ->
        if (params.debug) {
            println "\n=== CHANNEL ITEM ==="
            println "Raw item: ${item}"
            println "==================="
        }
        return item
    }
    
    output_ch.count().subscribe { count ->
        println "✓ Pipeline completed with ${count} items"
    }
    
    emit:
        bids_channel = output_ch
}

workflow {
    main_workflow()
}
