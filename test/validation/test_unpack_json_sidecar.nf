#!/usr/bin/env nextflow

/*
 * Test workflow for the unpack_json_sidecar option.
 * Validates that .json sidecars are emitted as parsed maps (not paths)
 * when unpack_json_sidecar: true is set on Channel.fromBIDS.
 */

include { fromBIDS } from "plugin/nf-bids"

params.bids_dir = params.bids_dir ?: "${projectDir}/../data/custom/ds-dwi3"
params.config   = params.config   ?: "${projectDir}/../configs/config_dwi.yaml"

workflow test_unpack_json_sidecar {
    main:
        def ch = channel.fromBIDS(params.bids_dir, params.config, [unpack_json_sidecar: true])

    emit:
        test_results = ch
}

workflow {
    test_unpack_json_sidecar()
    test_unpack_json_sidecar.out.test_results.view { item ->
        "subject=${item.meta.subject}, T1w.json class=${item.T1w?.json?.getClass()?.simpleName}"
    }
}
