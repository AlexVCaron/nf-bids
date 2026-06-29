#!/usr/bin/env nextflow

nextflow.enable.dsl=2

params.bids_dir = params.bids_dir ?: "${projectDir}/../data/custom/ds-dwi"
params.config = params.config ?: "${projectDir}/../configs/config_dwi.yaml"
params.libbids_sh = params.libbids_sh ?: null

workflow main_workflow {
    main:
        println "🧪 Testing Channel.fromBIDS..."

        def options = params.libbids_sh ? [libbids_sh: params.libbids_sh, flatten_output: false] : [flatten_output: false]
        bids_channel = Channel.fromBIDS(params.bids_dir, params.config, options)
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
