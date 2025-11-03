# Usage Examples

Practical examples for using the nf-bids plugin in various scenarios.

## Table of Contents

- [Basic Examples](#basic-examples)
- [Advanced Workflows](#advanced-workflows)
- [Common Use Cases](#common-use-cases)
- [Multi-Modal Studies](#multi-modal-studies)
- [Quality Control](#quality-control)

>[!NOTE]
>All examples need the following lines in the `nextflow.config` file to load the plugin :
>
>```groovy
>plugins {
>    id 'nf-bids'
>}
>```

# Basic Examples

## T1w brain extraction with FSL `bet`

**`T1w.yaml`:**
```yaml
loop_over:
- subject

T1w:
  plain_set: {}
```

**`main.nf`:**

```groovy
#!/usr/bin/env nextflow

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    ch_t1w = Channel.fromBIDS(params.bids_dir, 'T1w.yaml')
        .map { key, data -> [
            key.findAll{ it != 'NA' }.join('_'),
            file(data.bidsParentDir) / data.data.T1w.nii
        ] }
    
    processT1w(ch_t1w)
}

process processT1w {
    publishDir "${params.output_dir}/${subject}", mode: 'copy'
    
    input:
        tuple val(subject), path(t1w)
    
    output:
        tuple val(subject), path("${subject}_brain.nii.gz"), emit: t1w
    
    script:
    """
    bet ${t1w} ${subject}_brain.nii.gz -f 0.5
    """
}
```

## Multi-Echo Resting-State fMRI combination with `tedana`

**`mersfmri.yaml`:**

```yaml
loop_over:
- subject
- session
- task

bold:
  sequential_set:
    by_entity: echo
```

**`main.nf`:**

```groovy
#!/usr/bin/env nextflow

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    ch_bold = Channel.fromBIDS(params.bids_dir, 'mfmri.yaml')
        .filter { key, data -> data.data.task == 'rest' }
        .map { key, data -> [
            key.findAll{ it != 'NA' }.join('_'),
            data.subject,
            data.session,
            data.data.bold.nii.collect{ file(data.bidsParentDir) / it }
        ] }
    
    multiEchoCombine(ch_bold)
}

process multiEchoCombine {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'
    
    input:
        tuple val(id), val(subject), val(session), path(echoes)
    
    output:
        tuple val(id), path("${id}_combined_bold.nii.gz"), emit: bold
    
    script:
    def echo_args = echoes.collect { "-e ${it}" }.join(' ')
    """
    tedana ${echo_args} -o ${id}_combined_bold.nii.gz
    """
}
```

---

## Concatenate DWI from Multiple Runs then run `dtifit`

**`msdwi.yaml`:**

```yaml
loop_over:
- subject
- session

dwi:
  plain_set:
    additional_extensions:
    - bval
    - bvec
```

**`main.nf`:**

```groovy
#!/usr/bin/env nextflow

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    ch_dwi = Channel.fromBIDS(params.bids_dir, 'msdwi.yaml')
        .map { key, data ->
            def id = [data.subject, data.session].join('_')
            def meta = [id: id, subject: data.subject, session: data.session]

            def acquisitions = data.data.dwi.nii.collect{ file(data.bidsParentDir) / it }
            def bvals = data.data.dwi.bval.collect{ file(data.bidsParentDir) / it }
            def bvecs = data.data.dwi.bvec.collect{ file(data.bidsParentDir) / it }
            
            return [meta, acquisitions, bvals, bvecs]
        }
    
    mergeDWI(ch_dwi)
    ch_merged_dwi = mergeDWI.out.dwi
        .join(mergeDWI.out.bval)
        .join(mergeDWI.out.bvec)

    processDWI(ch_merged_dwi)
}

process mergeDWI {
    input:
        tuple val(meta), val(dwis), val(bvals), val(bvecs)
    
    output:
        tuple val(meta), path("${prefix}_merged_dwi.nii.gz"), emit: dwi
        tuple val(meta), path("${prefix}_merged_dwi.bval"), emit: bval
        tuple val(meta), path("${prefix}_merged_dwi.bvec"), emit: bvec
    
    script:
    def prefix = meta.id
    def dwi_files = dwis.join(' ')
    def bval_files = bvals.join(' ')
    def bvec_files = bvecs.join(' ')
    """
    # Merge DWI volumes
    fslmerge -t ${prefix}_merged_dwi.nii.gz ${dwi_files}
    
    # Concatenate bvals
    paste -d ' ' ${bval_files} > ${prefix}_merged_dwi.bval
    
    # Concatenate bvecs
    paste -d ' ' ${bvec_files} > ${prefix}_merged_dwi.bvec
    """
}

process processDWI {
    publishDir "${params.output_dir}/${meta.subject}/${meta.session}", mode: 'copy'
    
    input:
        tuple val(meta), path(dwi), path(bval), path(bvec)
    
    output:
        tuple val(meta), path("${prefix}_dti_*"), emit: dti
    
    script:
    def prefix = meta.id
    """
    dtifit -k ${dwi} -b ${bval} -r ${bvec} -o ${prefix}_dti
    """
}
```

---

# Advanced Workflows

## Cross-Modal Analysis with T1w Reference

**`bold+T1w.yaml`:**

```yaml
loop_over:
- subject
- session
- task

bold:
  plain_set:
    include_cross_modal:
    - T1w 
```

**`main.nf`:**

```groovy
#!/usr/bin/env nextflow

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    ch_t1w_bold = Channel.fromBIDS(params.bids_dir, 'bold+T1w.yaml')
        .map { key, data ->
            def meta = [
                id: key.findAll{ it != 'NA' }.join('_'),
                subject: data.subject,
                session: data.session,
                task: data.task
            ]
  
            def bold = file(data.bidsParentDir) / data.data.bold.nii
            def t1w = file(data.bidsParentDir) / data.data.t1w.nii
  
            return [meta, t1w, bold]
        }
    
    // Process complete datasets
    registerBoldToT1w(ch_t1w_bold)
}

process registerBoldToT1w {
    publishDir "${params.output_dir}/${subject}/${session}/${task}", mode: 'copy'
    
    input:
        tuple val(meta), path(t1w), path(bold)
    
    output:
        tuple val(meta), path("${prefix}_registered_bold.nii.gz", emit: bold
        tuple val(meta), path "${prefix}_transform.mat", emit: transform
    
    script:
    def prefix = meta.id
    def subject = meta.subject
    def session = meta.session
    def task = meta.task
    """
    # Extract first volume as reference
    fslroi ${bold[0]} ref_vol.nii.gz 0 1
    
    # Register BOLD to T1w
    flirt -in ref_vol.nii.gz \
      -ref ${t1w} \
      -out ${prefix}_registered_bold.nii.gz \
      -omat ${prefix}_transform.mat
    """
}
```

---

### Multiple Tasks Processing

**`mtasks.yaml`:**

```yaml
loop_over:
- subject
- session
- task

bold:
  plain_set: {}
```

**`main.nf`:**

```groovy
#!/usr/bin/env nextflow

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    ch_tasks = Channel.fromBIDS(params.bids_dir, 'mtasks.yaml')
        .map { key, data ->
            def meta = [
                id: [data.subject, data.session].join('_'),
                subject: data.subject,
                session: data.session
            ]
  
            def bold = file(data.bidsParentDir) / data.data.bold.nii
  
            return [meta, data.task, bold]
        }
        .branch { meta, task, bold ->
            rest: task == 'rest' 
                return [meta, bold]
            nback: task == 'nback' 
                return [meta, bold]
            memory: task == 'memory' 
                return [meta, bold]
        }
    
    // Task-specific processing
    processRest(ch_tasks.rest)
    processNBack(ch_tasks.nback)
    processMemory(ch_tasks.memory)
}

process processRest {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'

    input:
        tuple val(meta), path(bold)
    
    output:
        tuple val(meta), path("${prefix}_rest_results.nii.gz"), emit: connectivity
    
    script:
    def prefix = meta.id
    def subject = meta.subject
    def session = meta.session
    """
    # Resting-state analysis
    3dRSFC -prefix ${prefix}_rest_results.nii.gz $bold
    """
}

process processNBack {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'

    input:
        tuple val(meta), path(bold)
    
    output:
        tuple val(meta), path("${prefix}_nback_results.nii.gz"), emit: stimulus
    
    script:
    def prefix = meta.id
    def subject = meta.subject
    def session = meta.session
    """
    # N-back task analysis
    3dDeconvolve -input $bold -prefix ${prefix}_nback_results.nii.gz
    """
}

process processMemory {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'

    input:
        tuple val(meta), path(bold)
    
    output:
        tuple val(meta), path("memory_results.nii.gz"), emit: stimulus
    
    script:
    def prefix = meta.id
    def subject = meta.subject
    def session = meta.session
    """
    # Memory task analysis
    3dDeconvolve -input $bold -prefix ${prefix}_memory_results.nii.gz
    """
}
```

---

## Common Use Cases

### DWI EPI Correction

>[!NOTE]
>Using version `0.1.0-beta.2` and prior, it is not possible to perform the correct
>**entity mapping** to split DWI, EPI and SBREF files based on, for example, the
>`acquisition` entity (`AP` vs `PA` and such). Implementation to come very soon !

**`dwi+epi.yaml`:**

```yaml
loop_over:
- subject
- session
- run

dwi:
  plain_set:
    additional_extensions:
    - bval
    - bvec
    include_cross_modal:
    - epi
```

**`main.nf`:**

```groovy
#!/usr/bin/env nextflow

params.bids_dir = '/data/bids'
params.output_dir = 'results'
params.phase_direction = "AP" //AP,PA,RL,LR,IS,SI

workflow {
    ch_dwi_epi = Channel.fromBIDS(params.bids_dir, 'dwi+epi.yaml')
        .map { key, data ->
            def id = key.findAll{ it != 'NA' }.join('_')
            def subpath = key.findAll{ it != 'NA' }.join('/')
            def meta = [id: id, subpath: subpath]

            def dwi = file(data.bidsParentDir) / data.data.dwi.nii
            def bval = file(data.bidsParentDir) / data.data.dwi.bval
            def bvec = file(data.bidsParentDir) / data.data.dwi.bvec
            def dwi_json = file(data.bidsParentDir) / data.data.dwi.json
            def epi = file(data.bidsParentDir) / data.data.epi.nii
            def epi_json = file(data.bidsParentDir) / data.data.epi.json
            
            return [meta, dwi, bval, bvec, epi, dwi_json, epi_json]
        }
        .multiMap { meta, dwi, bval, bvec, epi, dwi_json, epi_json ->
            data: [meta, dwi, bval, bvec, epi]
            json: [meta, dwi_json, epi_json]
        }

    acqparamExtraction(ch_dwi_epi.json)
    ch_for_correction = ch_dwi_epi.data
        .join(acqparamExtraction.out.acqparam)

    applyFieldMapCorrection(ch_for_correction)
}

process acqparamExtraction {
    input:
        tuple val(meta), path(dwi_json), path(epi_json)

    output:
        tuple val(meta), path("${prefix}_acqparam.txt"), emit: acqparam

    script:
    def prefix = meta.id
    def phase = [
      AP: "0 -1 0",
      PA: "0 1 0",
      LR: "-1 0 0",
      RL: "1 0 0",
      IS: "0 0 -1",
      SI: "0 0 1"
    ]
    """
    # Get TotalReadoutTime
    dwi_trt = \$(cat $dwi_json | jq "if .EffectiveEchoSpacing
        then .EffectiveEchoSpacing
        else if .TotalReadoutTime then .TotalReadoutTime 
        else (if .EchoSpacing then .EchoSpacing / 1000
              else if .DwellTime then .DwellTime
              else 1 / .PixelBandwidth
              end end) * (.ReconMatrixPE / (if .ParallelReductionFactorInPlane then .ParallelReductionFactorInPlane else 1 end) - 1)
        end end")
    epi_trt = \$(cat $epi_json | jq "if .EffectiveEchoSpacing
        then .EffectiveEchoSpacing
        else if .TotalReadoutTime then .TotalReadoutTime 
        else (if .EchoSpacing then .EchoSpacing / 1000
              else if .DwellTime then .DwellTime
              else 1 / .PixelBandwidth
              end end) * (.ReconMatrixPE / (if .ParallelReductionFactorInPlane then .ParallelReductionFactorInPlane else 1 end) - 1)
        end end")

    echo "${phase[params.phase_direction]} \$dwi_trt" >> ${prefix}_acqparam.txt
    echo "${phase[params.phase_direction.reverse()]} \$epi_trt" >> ${prefix}_acqparam.txt
    """
}

process applyFieldMapCorrection {
    publishDir "${params.output_dir}/${meta.subpath}", mode: 'copy'
    
    input:
        tuple val(meta), path(dwi), path(bval), path(bvec), path(epi), path(acqparam)
    
    output:
        tuple val(meta), path("${prefix}_corrected_dwi.nii.gz"), emit: dwi
        tuple val(meta), path("${prefix}_corrected_dwi.bval"), emit: bval
        tuple val(meta), path("${prefix}_corrected_dwi.bvec"), emit: bvec
        tuple val(meta), path("${prefix}_corrected_b0.nii.gz"), emit: b0
    
    script:
    def prefix = meta.id
    """
    # Extract b0 volume from DWI
    dwiextract -fslgrad $bvec $bval $dwi - -bzero | mrmath - mean mean_bzero.nii.gz -axis 3
    # Merge b0 volumes
    fslmerge -t b0_for_topup.nii.gz mean_bzero.nii.gz $epi
    # Create combined field map
    topup --imain=b0_for_topup.nii.gz \
          --config=b02b0.cnf \
          --datain=$acqparam \
          --out=topup_results
    
    # Apply correction
    applytopup --imain=$dwi \
               --topup=topup_results \
               --datain=acqparams.txt \
               --inindex=1 \
               --out=${prefix}_corrected_dwi.nii.gz

    applytopup --imain=mean_bzero.nii.gz,$epi \
               --topup=topup_results \
               --datain=acqparams.txt \
               --inindex=1,2 \
               --out=${prefix}_corrected_b0.nii.gz

    cp $bval ${prefix}_corrected_dwi.bval
    cp $bvec ${prefix}_corrected_dwi.bvec
    """
}
```

---

### Quality Control with `MultiQC`, `Scilpy` and `ImageMagick`

**`dwi+t1w.yaml`:**

```yaml
loop_over:
- subject
- session
- run

dwi:
  plain_set:
    additional_extensions:
    - bval
    - bvec
    include_cross_modal:
    - epi

t1w:
  plain_set: {}
```

```groovy
#!/usr/bin/env nextflow

workflow {
    ch_bids_data = Channel.fromBIDS(params.bids_dir, 'dwi+t1w.yaml')
        .branch { key, data ->
            dwi: data.data.dwi
            t1w: data.data.t1w
        }

    ch_dwi = ch_bids_data.dwi
        .map { key, data ->
            def id = key.findAll{ it != 'NA' }.join('_')
            def subpath = key.findAll{ it != 'NA' }.join('/')
            def meta = [id: id, subpath: subpath]

            def dwi = file(data.bidsParentDir) / data.data.dwi.nii
            def bval = file(data.bidsParentDir) / data.data.dwi.bval
            def bvec = file(data.bidsParentDir) / data.data.dwi.bvec
            def dwi_json = file(data.bidsParentDir) / data.data.dwi.json
            def epi = file(data.bidsParentDir) / data.data.epi.nii
            def epi_json = file(data.bidsParentDir) / data.data.epi.json
            
            return [meta, dwi, bval, bvec, epi, dwi_json, epi_json]
        }

    ch_t1w = ch_bids_data.t1w
        .map { key, data ->
            def id = key.findAll{ it != 'NA' }.join('_')
            def subpath = key.findAll{ it != 'NA' }.join('/')
            def meta = [id: id, subpath: subpath]

            def t1w = file(data.bidsParentDir) / data.data.t1w.nii
            def json = file(data.bidsParentDir) / data.data.t1w.json

            return [meta, t1w, json]
        }

    // QC for each modality
    qcDiffusion(ch_dwi)
    qcAnatomical(ch_t1w)
    
    // Combine QC reports
    ch_for_qc = Channel.empty()
        .mix(qcAnatomical.out.mqc)
        .mix(qcDiffusion.out.mqc)
        .collect()
    
    generateMultiQC(ch_for_qc)
}

process qcAnatomical {
    input:
        tuple val(meta), path(t1w), path(json)
    
    output:
        path("${meta.id}*_mqc.*"), emit: mqc
    
    script:
    def prefix = meta.id
    """
    cp $json ${prefix}_t1w_mqc.json
    scil_viz_screenshot_volume --display_slice_number $t1w t1w.png
    convert -delay 10 -loop 0 -morph 10 t1w*.png ${prefix}_t1w_mqc.gif
    """
}

process qcDiffusion {
    input:
        tuple val(subject), path(dwi), path(bval), path(bvec), path(epi), path(dwi_json), path(epi_json)
    
    output:
        path("${meta.id}*_mqc.*"), emit: mqc
    
    script:
    """
    cp $dwi_json ${prefix}_dwi_mqc.json
    cp $epi_json ${prefix}_epi_mqc.json
    # EPI screenshots
    scil_viz_screenshot_volume --display_slice_number $epi epi.png
    convert -delay 10 -loop 0 -morph 10 epi*.png ${prefix}_epi_mqc.gif
    # DWI screenshots
    ndirs=\$(scil_header_print_info --keys dim $dwi | sed 's/[^0-9]*[0-9]*[^0-9]*[0-9]*[^0-9]*[0-9]*[^0-9]*[0-9]*[^0-9]*\([0-9]*\).*/\1/')
    mkdir -p dwi_split
    scil_dwi_split_by_indices $dwi $bval $bvec \
        dwi_split\dwi \
        \$(seq 1 \$((ndirs-1)))
    i=0
    for f in dwi_split/dwi*.nii.gz
    do
        scil_viz_volume_screenshot --display_slice_number --slices 30 $f dwi_\${i}.png
        \$((++i))
    done
    convert -delay 10 -loop 0 -morph 10 dwi*.png ${prefix}_dwi_mqc.gif
    """
}

process generateMultiQC {
    publishDir "${params.output_dir}/qc", mode: 'copy'
    
    input:
        path(qc_files)
    
    output:
        path("multiqc_report.html"), emit: mqc
    
    script:
    """
    multiqc . -o . -n multiqc_report.html
    """
}
```

## Tips and Best Practices

### 1. Filter Early

```groovy
// Good: Filter before mapping
Channel.fromBIDS(params.bids_dir, params.config)
    .filter { key, data -> data.subject.startsWith('sub-control') }
    .map { key, data -> [data.subject, data.data.T1w] }

// Less efficient: Map then filter
Channel.fromBIDS(params.bids_dir, params.config)
    .map { key, data -> [data.subject, data.data.T1w] }
    .filter { subject, t1w -> subject.startsWith('sub-control') }
```

### 2. Use multiMap for Forking

```groovy
// Efficient parallel branching
Channel.fromBIDS(params.bids_dir, params.config)
    .multiMap { key, data ->
        anat: [data.subject, data.data.T1w]
        func: [data.subject, data.data.bold]
    }
    .set { forked }
```

## Related Documentation

- [API Reference](api.md) - Complete API documentation
- [Configuration Guide](configuration.md) - YAML configuration
- [Installation Guide](installation.md) - Setup and installation
