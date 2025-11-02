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

## Multi-Echo fMRI combination with `tedana`

**`mfmri.yaml`:**

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

            def acquisitions = data.data.dwi.collect{ file(data.bidsParentDir) / it }
            def bvals = data.data.bval.collect{ file(data.bidsParentDir) / it }
            def bvecs = data.data.bvec.collect{ file(data.bidsParentDir) / it }
            
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

          def bold = file(data.bidsParentDir) / data.data.bold
          def t1w = file(data.bidsParentDir) / data.data.t1w

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

          def bold = file(data.bidsParentDir) / data.data.bold

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

### Field Map Correction

```groovy
#!/usr/bin/env nextflow

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .filter { key, data -> 
            data.data.fmap && data.data.bold
        }
        .map { key, data ->
            // Extract AP and PA field maps
            def fmap_acq = data.data.fmap.values().first()
            def ap = fmap_acq[0]  // dir-AP
            def pa = fmap_acq[1]  // dir-PA
            
            [data.subject, data.session, data.task, 
             ap, pa, data.data.bold]
        }
        .set { fmap_bold_ch }
    
    applyFieldMapCorrection(fmap_bold_ch)
}

process applyFieldMapCorrection {
    publishDir "${params.output_dir}/${subject}/${session}/${task}", 
               mode: 'copy'
    
    input:
    tuple val(subject), val(session), val(task),
          path(fmap_ap), path(fmap_pa), path(bold)
    
    output:
    path "corrected_bold.nii.gz"
    
    script:
    """
    # Create combined field map
    topup --imain=${fmap_ap},${fmap_pa} \
          --datain=acqparams.txt \
          --out=topup_results
    
    # Apply correction
    applytopup --imain=${bold[0]} \
               --topup=topup_results \
               --out=corrected_bold.nii.gz
    """
}
```

**Configuration:**
```yaml
loop_over:
  - subject
  - session
  - task

cross_modal_broadcasting:
  - fmap

fmap:
  mixed_set:
    entities:
      suffix: epi
    group_by: acquisition
    sequence_by: direction

bold:
  sequential_set:
    entities:
      suffix: bold
    sequence_by: echo
```

---

### Quality Control with MultiQC

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .set { bids_ch }
    
    // QC for each modality
    qcAnatomical(
        bids_ch.map { k, d -> [d.subject, d.data.T1w] }
    )
    qcFunctional(
        bids_ch.map { k, d -> [d.subject, d.data.bold] }
    )
    qcDiffusion(
        bids_ch.map { k, d -> [d.subject, d.data.dwi] }
    )
    
    // Combine QC reports
    Channel.empty()
        .mix(qcAnatomical.out, qcFunctional.out, qcDiffusion.out)
        .collect()
        .set { all_qc }
    
    generateMultiQC(all_qc)
}

process qcAnatomical {
    input:
    tuple val(subject), path(t1w)
    
    output:
    path "${subject}_T1w_qc.json"
    
    script:
    """
    mriqc_anat ${t1w} ${subject}_T1w_qc.json
    """
}

process qcFunctional {
    input:
    tuple val(subject), path(bold)
    
    output:
    path "${subject}_bold_qc.json"
    
    script:
    """
    mriqc_func ${bold[0]} ${subject}_bold_qc.json
    """
}

process qcDiffusion {
    input:
    tuple val(subject), path(dwi)
    
    output:
    path "${subject}_dwi_qc.json"
    
    script:
    """
    # DWI QC metrics
    dtiqa ${dwi} ${subject}_dwi_qc.json
    """
}

process generateMultiQC {
    publishDir "${params.output_dir}/qc", mode: 'copy'
    
    input:
    path qc_files
    
    output:
    path "multiqc_report.html"
    
    script:
    """
    multiqc . -o . -n multiqc_report.html
    """
}
```

---

## Multi-Modal Studies

### Structural + Functional + Diffusion

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .filter { key, data ->
            data.data.T1w && data.data.bold && data.data.dwi
        }
        .map { key, data -> [
            data.subject,
            data.session,
            data.data.T1w,
            data.data.bold,
            data.data.dwi
        ]}
        .set { multimodal_ch }
    
    // Parallel processing
    processAnatomical(multimodal_ch.map { s, ses, t1, _, _ -> [s, ses, t1] })
    processFunctional(multimodal_ch.map { s, ses, _, bold, _ -> [s, ses, bold] })
    processDiffusion(multimodal_ch.map { s, ses, _, _, dwi -> [s, ses, dwi] })
    
    // Combine results
    combineMultiModal(
        processAnatomical.out
            .join(processFunctional.out)
            .join(processDiffusion.out)
    )
}

process processAnatomical {
    input:
    tuple val(subject), val(session), path(t1w)
    
    output:
    tuple val(subject), val(session), path("anat_results")
    
    script:
    """
    mkdir anat_results
    recon-all -i ${t1w} -subjid ${subject} -all -sd anat_results
    """
}

process processFunctional {
    input:
    tuple val(subject), val(session), path(bold)
    
    output:
    tuple val(subject), val(session), path("func_results")
    
    script:
    """
    mkdir func_results
    fmriprep_bold ${bold[0]} func_results
    """
}

process processDiffusion {
    input:
    tuple val(subject), val(session), val(dwi)
    
    output:
    tuple val(subject), val(session), path("dwi_results")
    
    script:
    """
    mkdir dwi_results
    mrtrix_pipeline ${dwi.values().join(' ')} dwi_results
    """
}

process combineMultiModal {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'
    
    input:
    tuple val(subject), val(session), 
          path(anat), path(func), path(dwi)
    
    output:
    path "combined_results"
    
    script:
    """
    mkdir -p combined_results
    cp -r ${anat} combined_results/
    cp -r ${func} combined_results/
    cp -r ${dwi} combined_results/
    """
}
```

**Configuration:**
```yaml
loop_over:
  - subject
  - session

cross_modal_broadcasting:
  - T1w

T1w:
  plain_set:
    entities:
      suffix: T1w

bold:
  sequential_set:
    entities:
      suffix: bold
    sequence_by: echo

dwi:
  named_set:
    entities:
      suffix: dwi
    group_by: acquisition
```

---

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
