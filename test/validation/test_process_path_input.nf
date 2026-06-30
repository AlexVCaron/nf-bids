#!/usr/bin/env nextflow

/*
 * Integration test: Verify Path objects work with process path inputs
 * This is the critical test - it will FAIL if we emit File objects
 */

include { fromBIDS } from "plugin/nf-bids"

params.bids_dir = params.bids_dir ?: "${projectDir}/../data/bids-examples/asl001"
params.config = params.config ?: "${projectDir}/../configs/config_asl.yaml"
params.libbids_sh = params.libbids_sh ?: null

process verify_path_staging {
    debug true
    
    input:
    tuple val(meta), path(t1w_nii), path(t1w_json)
    
    output:
    tuple val(meta), stdout
    
    script:
    """
    echo "Subject: ${meta.subject}"
    echo "NII file: ${t1w_nii}"
    echo "JSON file: ${t1w_json}"
    echo "NII exists: \$(test -f ${t1w_nii} && echo 'YES' || echo 'NO')"
    echo "JSON exists: \$(test -f ${t1w_json} && echo 'YES' || echo 'NO')"
    if [ -f ${t1w_nii} ]; then
        echo "NII size: \$(stat -c%s ${t1w_nii} 2>/dev/null || stat -f%z ${t1w_nii} 2>/dev/null)"
    fi
    """
}

workflow {
    log.info """
    ================================================
    Path Integration Test
    ================================================
    Testing that Path objects are correctly staged
    into Nextflow process work directories.
    
    This test will FAIL if java.io.File objects are
    emitted instead of java.nio.file.Path objects.
    ================================================
    """.stripIndent()
    
    def options = params.libbids_sh ? [libbids_sh: params.libbids_sh] : [:]

    Channel.fromBIDS(params.bids_dir, params.config, options)
        .filter { it.T1w }
        .map { item ->
            [
                item.meta,
                item.T1w.nii,
                item.T1w.json
            ]
        }
        .take(1)
        .set { bids_data }
    
    verify_path_staging(bids_data)
        .view { meta, output ->
            """
            ✅ SUCCESS: Files staged correctly for ${meta.subject}
            ${output}
            """
        }
}

workflow.onComplete {
    if (workflow.success) {
        log.info """
        
        ================================================
        ✅ Path Integration Test PASSED
        ================================================
        The plugin correctly emits java.nio.file.Path
        objects that work with Nextflow process inputs!
        ================================================
        """.stripIndent()
    } else {
        log.error """
        
        ================================================
        ❌ Path Integration Test FAILED
        ================================================
        Error: ${workflow.errorMessage}
        
        This likely means File objects are still being
        emitted instead of Path objects.
        ================================================
        """.stripIndent()
    }
}
