#!/usr/bin/env nextflow

/*
 * Direct test to verify Path objects in channel output
 */

include { fromBIDS } from "plugin/nf-bids"

params.bids_dir = params.bids_dir ?: "${projectDir}/../data/bids-examples/asl001"
params.config = params.config ?: "${projectDir}/../configs/config_asl.yaml"
params.libbids_sh = params.libbids_sh ?: null

workflow {
    log.info "=== Testing Path Object Types ==="
    
    def options = params.libbids_sh ? [libbids_sh: params.libbids_sh] : [:]

    channel.fromBIDS(params.bids_dir, params.config, options)
        .take(1)
        .map { item ->
            log.info "\nChecking item structure:"
            log.info "  Meta: ${item.meta}"
            
            // Check each suffix
            item.each { key, value ->
                if (key != 'meta') {
                    log.info "\n  Suffix '${key}':"
                    if (value instanceof Map) {
                        value.each { ext, file ->
                            log.info "    ${ext}: class=${file?.getClass()?.name}"
                            log.info "    ${ext}: isPath=${file instanceof java.nio.file.Path}"
                            log.info "    ${ext}: isFile=${file instanceof java.io.File}"
                            log.info "    ${ext}: value=${file}"
                        }
                    } else {
                        log.info "    class=${value?.getClass()?.name}"
                        log.info "    value=${value}"
                    }
                }
            }
            
            return item
        }
        .subscribe { 
            log.info "\n✅ Item processed successfully"
        }
}

workflow.onComplete {
    log.info "\n=== Test Complete ==="
}
