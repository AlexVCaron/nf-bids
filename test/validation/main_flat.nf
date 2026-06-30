#!/usr/bin/env nextflow

include { fromBIDS } from 'plugin/nf-bids'

nextflow.enable.dsl=2

params.bids_dir = params.bids_dir ?: "${projectDir}/../data/custom/ds-dwi4"
params.config = params.config ?: "${projectDir}/../configs/config_heterogeneous_dwi.yaml"
params.libbids_sh = params.libbids_sh ?: null

workflow main_workflow {
    main:
        println "🧪 Testing Channel.fromBIDS with flat output..."

        def options = params.libbids_sh ? [libbids_sh: params.libbids_sh, flatten_output: true] : [flatten_output: true]
        bids_channel = channel.fromBIDS(params.bids_dir, params.config, options)
        pipeline_status = channel.value('ok')

        bids_channel.count().subscribe { count ->
            println "✓ Pipeline completed with ${count} items"
        }

    emit:
        bids_channel = bids_channel
        pipeline_status = pipeline_status
}

workflow {
    main:
        main_workflow()
}
