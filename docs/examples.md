# Usage Examples

Practical examples for using the nf-bids plugin in various scenarios.

## Table of Contents

- [Basic Examples](#basic-examples)
- [Advanced Workflows](#advanced-workflows)
- [Common Use Cases](#common-use-cases)
- [Multi-Modal Studies](#multi-modal-studies)
- [Quality Control](#quality-control)

## Basic Examples

### Simple T1w Processing

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .map { key, data -> [data.subject, data.data.T1w] }
        .set { t1w_ch }
    
    processT1w(t1w_ch)
}

process processT1w {
    publishDir "${params.output_dir}/${subject}", mode: 'copy'
    
    input:
    tuple val(subject), path(t1w)
    
    output:
    path "${subject}_brain.nii.gz"
    
    script:
    """
    bet ${t1w} ${subject}_brain.nii.gz -f 0.5
    """
}
```

**Configuration (config.yaml):**
```yaml
loop_over:
  - subject

T1w:
  plain_set:
    entities:
      suffix: T1w
```

---

### Multi-Echo fMRI

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .filter { key, data -> data.task == 'rest' }
        .map { key, data -> 
            [data.subject, data.session, data.data.bold]
        }
        .set { bold_ch }
    
    multiEchoCombine(bold_ch)
}

process multiEchoCombine {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'
    
    input:
    tuple val(subject), val(session), path(echoes)
    
    output:
    path "${subject}_${session}_combined_bold.nii.gz"
    
    script:
    def echo_args = echoes.collect { "-e ${it}" }.join(' ')
    """
    tedana ${echo_args} -o ${subject}_${session}_combined_bold.nii.gz
    """
}
```

**Configuration:**
```yaml
loop_over:
  - subject
  - session
  - task

bold:
  sequential_set:
    entities:
      suffix: bold
    sequence_by: echo
```

---

### DWI with Multiple Acquisitions

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .map { key, data ->
            def acquisitions = data.data.dwi
            def bvals = data.data.bval
            def bvecs = data.data.bvec
            
            [data.subject, data.session, acquisitions, bvals, bvecs]
        }
        .set { dwi_ch }
    
    mergeDWI(dwi_ch)
    processDWI(mergeDWI.out)
}

process mergeDWI {
    input:
    tuple val(subject), val(session), val(dwi_map), val(bval_map), val(bvec_map)
    
    output:
    tuple val(subject), val(session), path("merged_dwi.nii.gz"), 
          path("merged.bval"), path("merged.bvec")
    
    script:
    def dwi_files = dwi_map.values().join(' ')
    def bval_files = bval_map.values().join(' ')
    def bvec_files = bvec_map.values().join(' ')
    """
    # Merge DWI volumes
    fslmerge -t merged_dwi.nii.gz ${dwi_files}
    
    # Concatenate bvals
    paste -d ' ' ${bval_files} > merged.bval
    
    # Concatenate bvecs
    paste -d ' ' ${bvec_files} > merged.bvec
    """
}

process processDWI {
    publishDir "${params.output_dir}/${subject}/${session}", mode: 'copy'
    
    input:
    tuple val(subject), val(session), path(dwi), path(bval), path(bvec)
    
    output:
    path "dti_*"
    
    script:
    """
    dtifit -k ${dwi} -b ${bval} -r ${bvec} -o dti
    """
}
```

**Configuration:**
```yaml
loop_over:
  - subject
  - session

dwi:
  named_set:
    entities:
      suffix: dwi
    group_by: acquisition

bval:
  named_set:
    entities:
      suffix: bval
    group_by: acquisition

bvec:
  named_set:
    entities:
      suffix: bvec
    group_by: acquisition
```

---

## Advanced Workflows

### Cross-Modal Analysis with T1w Reference

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .branch { key, data ->
            has_both: data.data.T1w && data.data.bold
                return [data.subject, data.session, data.task, 
                        data.data.T1w, data.data.bold]
            missing: true
        }
        .set { branched }
    
    // Process complete datasets
    registerBoldToT1w(branched.has_both)
    
    // Log incomplete datasets
    branched.missing
        .subscribe { key, data ->
            log.warn "Incomplete data for ${data.subject}/${data.session}"
        }
}

process registerBoldToT1w {
    publishDir "${params.output_dir}/${subject}/${session}/${task}", 
               mode: 'copy'
    
    input:
    tuple val(subject), val(session), val(task), path(t1w), path(bold)
    
    output:
    path "registered_bold.nii.gz"
    path "transform.mat"
    
    script:
    """
    # Extract first volume as reference
    fslroi ${bold[0]} ref_vol.nii.gz 0 1
    
    # Register BOLD to T1w
    flirt -in ref_vol.nii.gz -ref ${t1w} \
          -out registered_bold.nii.gz \
          -omat transform.mat
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
```

---

### Parallel Task Processing

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

params.bids_dir = '/data/bids'
params.output_dir = 'results'

workflow {
    Channel.fromBIDS(params.bids_dir, 'config.yaml')
        .multiMap { key, data ->
            rest: data.task == 'rest' 
                ? [data.subject, data.session, data.data.bold] 
                : null
            nback: data.task == 'nback' 
                ? [data.subject, data.session, data.data.bold] 
                : null
            memory: data.task == 'memory' 
                ? [data.subject, data.session, data.data.bold] 
                : null
        }
        .set { tasks }
    
    // Task-specific processing
    processRest(tasks.rest)
    processNBack(tasks.nback)
    processMemory(tasks.memory)
    
    // Combine results
    combineTaskResults(
        processRest.out,
        processNBack.out,
        processMemory.out
    )
}

process processRest {
    input:
    tuple val(subject), val(session), path(bold)
    
    output:
    tuple val(subject), val(session), path("rest_results.nii.gz")
    
    script:
    """
    # Resting-state analysis
    3dRSFC -prefix rest_results.nii.gz ${bold[0]}
    """
}

process processNBack {
    input:
    tuple val(subject), val(session), path(bold)
    
    output:
    tuple val(subject), val(session), path("nback_results.nii.gz")
    
    script:
    """
    # N-back task analysis
    3dDeconvolve -input ${bold[0]} -prefix nback_results.nii.gz
    """
}

process processMemory {
    input:
    tuple val(subject), val(session), path(bold)
    
    output:
    tuple val(subject), val(session), path("memory_results.nii.gz")
    
    script:
    """
    # Memory task analysis
    3dDeconvolve -input ${bold[0]} -prefix memory_results.nii.gz
    """
}

process combineTaskResults {
    publishDir "${params.output_dir}", mode: 'copy'
    
    input:
    tuple val(subject), val(session), path(rest)
    tuple val(subject), val(session), path(nback)
    tuple val(subject), val(session), path(memory)
    
    output:
    path "combined_results/${subject}/${session}/"
    
    script:
    """
    mkdir -p combined_results/${subject}/${session}
    cp ${rest} combined_results/${subject}/${session}/
    cp ${nback} combined_results/${subject}/${session}/
    cp ${memory} combined_results/${subject}/${session}/
    """
}
```

---

## Common Use Cases

### Field Map Correction

```groovy
#!/usr/bin/env nextflow

plugins {
    id 'nf-bids@0.1.0'
}

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

### 3. Handle Missing Data

```groovy
// Check for required data
Channel.fromBIDS(params.bids_dir, params.config)
    .filter { key, data ->
        if (!data.data.T1w) {
            log.warn "Missing T1w for ${data.subject}"
            return false
        }
        return true
    }
```

### 4. Use Demand-Driven Loading

```groovy
// Default: Fast metadata-only loading
Channel.fromBIDS(
    bidsDir: params.bids_dir,
    demand_driven: true  // Recommended for large datasets
)
```

## Related Documentation

- [API Reference](api.md) - Complete API documentation
- [Configuration Guide](configuration.md) - YAML configuration
- [Installation Guide](installation.md) - Setup and installation
