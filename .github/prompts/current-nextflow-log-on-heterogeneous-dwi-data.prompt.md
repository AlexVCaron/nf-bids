```prompt

The nf-bids plugin (latest version) was used to parse a BIDS dataset with heterogeneous DWI acquisitions (some with single phase-encoding direction, some with AP/PA pairs, some with RL/LR pairs, some with IS/SI pairs). The BIDS configuration file used is provided below, along the Nextflow log showing the parsing process.

There is many problems to be identified in the logfile. One is that keys with same suffix_maps_to (e.g. "dwi_is", "dwi_ap", "dwi_rl") are somehow colliding somewhere in the
code. Looking at the log, only "dwi_is" gets executed, even if all three are detected when
the plugin loads the configuration.

1. Investigate the log and the configuration file, and identify all problems that can be found.
2. Suggest corrections to the BIDS configuration file to fix the problems identified.
3. Order those suggestion in a TODO list, in the prompts directory. Make sure this todo
list is formatted so it can be converted directly to a prompt for an AI system to fix the problems.


# Configuration below :

loop_over:
- subject
- session
- run

T1w:
  plain_set: {}
  suffix_maps_to: "T1w"
 # required: ["nii"]

lesion:
  plain_set:
    description: lesion
 # required: ["nii"]
  suffix_maps_to: "mask"

wmparc:
  plain_set:
    description: wmparc
 # required: ["nii"]
  suffix_maps_to: "mask"

aparc_aseg:
  plain_set:
    description: aparc+aseg
 # required: ["nii"]
  suffix_maps_to: "mask"

dwi:
  plain_set: {}
  additional_extensions:
    - bvec
    - bval
 # required: ["nii", "bval", "bvec"]

dwi_ap:
  named_set:
    ap:
      direction: dir-AP
    pa:
      direction: dir-PA
  required: ["ap", "pa"]
  additional_extensions:
    - bvec
    - bval
  suffix_maps_to: "dwi"

dwi_rl:
  named_set:
    rl:
      direction: dir-RL
    lr:
      direction: dir-LR
  required: ["rl", "lr"]
  additional_extensions:
    - bvec
    - bval
  suffix_maps_to: "dwi"

dwi_is:
  named_set:
    is:
      direction: dir-IS
    si:
      direction: dir-SI
  required: ["is", "si"]
  additional_extensions:
    - bvec
    - bval
  suffix_maps_to: "dwi"

sbref:
  plain_set: {}
  #required: ["nii"]

sbref_ap:
  named_set:
    ap:
      direction: dir-AP
    pa:
      direction: dir-PA
  required: ["ap", "pa"]
  suffix_maps_to: "sbref"

sbref_rl:
  named_set:
    rl:
      direction: dir-RL
    lr:
      direction: dir-LR
  required: ["rl", "lr"]
  suffix_maps_to: "sbref"

sbref_is:
  named_set:
    is:
      direction: dir-IS
    si:
      direction: dir-SI
  required: ["is", "si"]
  suffix_maps_to: "sbref"

# Nextflow log below :

Dec-05 01:56:55.528 [main] DEBUG nextflow.cli.Launcher - $> nextflow run . --input assets/tests/dummy_bids/ --outdir results -stub-run -profile docker -resume
Dec-05 01:56:55.665 [main] DEBUG nextflow.cli.CmdRun - N E X T F L O W  ~  version 25.10.0
Dec-05 01:56:56.729 [main] DEBUG nextflow.config.ConfigBuilder - Found config local: /workspaces/nf-tractoflow/nextflow.config
Dec-05 01:56:56.731 [main] DEBUG nextflow.config.ConfigBuilder - Parsing config file: /workspaces/nf-tractoflow/nextflow.config
Dec-05 01:56:56.747 [main] DEBUG nextflow.config.ConfigBuilder - Applying config profile: \`docker\`
Dec-05 01:56:57.083 [main] DEBUG nextflow.plugin.PluginsFacade - Using Default plugin manager
Dec-05 01:56:57.092 [main] INFO  o.pf4j.DefaultPluginStatusProvider - Enabled plugins: []
Dec-05 01:56:57.093 [main] INFO  o.pf4j.DefaultPluginStatusProvider - Disabled plugins: []
Dec-05 01:56:57.095 [main] INFO  org.pf4j.DefaultPluginManager - PF4J version 3.12.0 in 'deployment' mode
Dec-05 01:56:57.106 [main] DEBUG nextflow.plugin.PluginsFacade - Using Default plugin manager
Dec-05 01:56:58.552 [main] DEBUG nextflow.config.ConfigBuilder - Available config profiles: [bih, cfc_dev, uzl_omics, ifb_core, embl_hd, denbi_qbic, alice, mjolnir_globe, uppmax, giga, incliva, ilifu, ki_luria, uge, icr_alma, rosalind_uge, lugh, mccleary, unibe_ibu, vai, czbiohub_aws, jax, roslin, tes, scw, unc_longleaf, tigem, tubingen_apg, apollo, ipop_up, vsc_calcua, pdc_kth, ceci_nic5, ccga_cau, humantechnopole, stjude, daisybio, eddie, medair, biowulf, apptainer, full_pipeline, bi, bigpurple, adcra, cedars, pawsey_setonix, vsc_kul_uhasselt, pawsey_nimbus, rki, ucl_myriad, utd_ganymede, charliecloud, fred_hutch, seattlechildrens, icr_davros, ceres, arm, munin, rosalind, hasta, cfc, uzh, shu_bmrc, ebi_codon_slurm, ebc, ccga_dx, crick, ku_sund_danhead, marvin, lrz_cm4, shifter, biohpc_gen, mana, mamba, york_viking, unc_lccc, wehi, awsbatch, wustl_htcf, arcc, ceci_dragon2, imperial, maestro, software_license, cannon, genotoul, nci_gadi, abims, eva_grace, janelia, nu_genomics, googlebatch, oist, sahmri, kaust, alliance_canada, mpcdf, leicester, vsc_ugent, create, sage, cambridge, jex, podman, ebi_codon, cheaha, xanadu, nyu_hpc, reproducible, test, marjorie, computerome, ucd_sonic, gpu, seg_globe, mssm, sanger, dkfz, bluebear, pasteur, einstein, ethz_euler, m3c, test_full, imb, legacy, ucl_cscluster, tuos_stanage, azurebatch, seadragon, crukmi, csiro_petrichor, qmul_apocrita, wave, docker, engaging, gis, hypatia, psmn, eva, unity, cropdiversityhpc, nygc, fgcz, conda, crg, singularity, mpcdf_viper, pe2, dirac, self_hosted_runner, tufts, uw_hyak_pedslabs, binac2, debug, hki_genie, genouest, cbe, unsw_katana, utd_juno, gitpod, phoenix, seawulf, uod_hpc, fub_curta, lovelace, uct_hpc, aws_tower, binac, fsu_draco]
Dec-05 01:56:58.633 [main] DEBUG nextflow.plugin.PluginsFacade - Setting up plugin manager > mode=prod; embedded=false; plugins-dir=/workspaces/.nextflow/plugins; core-plugins: nf-amazon@3.4.1,nf-azure@1.20.2,nf-cloudcache@0.5.0,nf-codecommit@0.5.0,nf-console@1.3.0,nf-google@1.23.3,nf-k8s@1.2.2,nf-tower@1.17.1,nf-wave@1.16.1
Dec-05 01:56:58.640 [main] INFO  o.pf4j.DefaultPluginStatusProvider - Enabled plugins: []
Dec-05 01:56:58.640 [main] INFO  o.pf4j.DefaultPluginStatusProvider - Disabled plugins: []
Dec-05 01:56:58.642 [main] INFO  org.pf4j.DefaultPluginManager - PF4J version 3.12.0 in 'deployment' mode
Dec-05 01:56:58.656 [main] DEBUG nextflow.util.RetryConfig - Missing nextflow session - using default retry config
Dec-05 01:56:58.692 [main] INFO  org.pf4j.AbstractPluginManager - No plugins
Dec-05 01:56:58.694 [main] DEBUG nextflow.plugin.PluginsFacade - Plugins declared=[nf-schema@2.3.0, nf-bids@0.1.0-beta.8]
Dec-05 01:56:58.695 [main] DEBUG nextflow.plugin.PluginsFacade - Plugins default=[]
Dec-05 01:56:58.695 [main] DEBUG nextflow.plugin.PluginsFacade - Plugins resolved requirement=[nf-schema@2.3.0, nf-bids@0.1.0-beta.8]
Dec-05 01:56:59.147 [main] DEBUG n.plugin.HttpPluginRepository - Registry request: https://registry.nextflow.io/api/v1/plugins/dependencies?plugins=nf-bids%400.1.0-beta.8%2Cnf-schema%402.3.0&nextflowVersion=25.10.0
- code: 200
- body: {"plugins":[{"id":"nf-bids","downloadCount":20,"downloadGhCount":0,"releases":[{"version":"0.1.0-beta.8","url":"https://registry.nextflow.io/api/v1/plugins/nf-bids/0.1.0-beta.8/download/nf-bids-0.1.0-beta.8.zip","date":"2025-12-05T01:36:34.905985Z","sha512sum":"33c3fdec5c13bbb92b10a8e8c23667f50f0278f4ce2fce6c8704442c03d7a26060942a0486477d6d4fc520a64ce7419386134344e96993d0eb769937f0122cfb","requires":">=24.10.0","dependsOn":[],"downloadCount":1,"downloadGhCount":0,"status":"PUBLISHED","spec":null}],"projectUrl":null,"provider":"Unknown","description":null},{"id":"nf-schema","downloadCount":71970,"downloadGhCount":476811,"releases":[{"version":"2.3.0","url":"https://registry.nextflow.io/api/v1/plugins/nf-schema/2.3.0/download/nf-schema-2.3.0.zip","date":"2025-01-13T10:17:53.328631Z","sha512sum":"d2f787ca1086851f0fe7b97e2f77b53a186084d1e30e9b441ba3116908783c377d95ffc827dfcc8b15ab783d0d51a8e36638d0a2042963e355203713406f1b7c","requires":">=23.10.0","dependsOn":[],"downloadCount":3433,"downloadGhCount":127268,"status":"PUBLISHED","spec":null}],"projectUrl":"https://github.com/nextflow-io/nf-schema","provider":"nextflow-io","description":null}]}
Dec-05 01:56:59.208 [main] DEBUG nextflow.plugin.PluginUpdater - Installing plugin nf-schema version: 2.3.0
Dec-05 01:56:59.219 [main] INFO  org.pf4j.AbstractPluginManager - Plugin 'nf-schema@2.3.0' resolved
Dec-05 01:56:59.220 [main] INFO  org.pf4j.AbstractPluginManager - Start plugin 'nf-schema@2.3.0'
Dec-05 01:56:59.233 [main] DEBUG nextflow.plugin.BasePlugin - Plugin started nf-schema@2.3.0
Dec-05 01:56:59.234 [main] DEBUG nextflow.plugin.PluginUpdater - Installing plugin nf-bids version: 0.1.0-beta.8
Dec-05 01:56:59.239 [main] INFO  org.pf4j.AbstractPluginManager - Plugin 'nf-bids@0.1.0-beta.8' resolved
Dec-05 01:56:59.239 [main] INFO  org.pf4j.AbstractPluginManager - Start plugin 'nf-bids@0.1.0-beta.8'
Dec-05 01:56:59.241 [main] DEBUG nextflow.plugin.BasePlugin - Plugin started nf-bids@0.1.0-beta.8
Dec-05 01:56:59.250 [main] DEBUG n.secret.LocalSecretsProvider - Secrets store: /workspaces/.nextflow/secrets/store.json
Dec-05 01:56:59.253 [main] DEBUG nextflow.secret.SecretsLoader - Discovered secrets providers: [nextflow.secret.LocalSecretsProvider@5611bba] - activable => nextflow.secret.LocalSecretsProvider@5611bba
Dec-05 01:56:59.255 [main] DEBUG nextflow.cli.CmdRun - Applied DSL=2 by global default
Dec-05 01:56:59.268 [main] DEBUG nextflow.cli.CmdRun - Launching \`./main.nf\` [intergalactic_bassi] DSL2 - revision: c89b44826a
Dec-05 01:56:59.320 [main] DEBUG nextflow.Session - Session UUID: 3e61a812-b293-45f8-8c2c-90a4967b3acb
Dec-05 01:56:59.320 [main] DEBUG nextflow.Session - Run name: intergalactic_bassi
Dec-05 01:56:59.321 [main] DEBUG nextflow.Session - Executor pool size: 8
Dec-05 01:56:59.330 [main] DEBUG nextflow.file.FilePorter - File porter settings maxRetries=3; maxTransfers=50; pollTimeout=null
Dec-05 01:56:59.335 [main] DEBUG nextflow.util.ThreadPoolBuilder - Creating thread pool 'FileTransfer' minSize=10; maxSize=24; workQueue=LinkedBlockingQueue[-1]; allowCoreThreadTimeout=false
Dec-05 01:56:59.351 [main] DEBUG nextflow.cli.CmdRun - 
  Version: 25.10.0 build 10289
  Created: 22-10-2025 16:26 UTC 
  System: Linux 6.14.0-36-generic
  Runtime: Groovy 4.0.28 on OpenJDK 64-Bit Server VM 17.0.17-internal+0-adhoc..src
  Encoding: UTF-8 (ANSI_X3.4-1968)
  Process: 277889@0bc8761a96fc [172.17.0.4]
  CPUs: 8 - Mem: 62.5 GB (1.6 GB) - Swap: 20 GB (19.2 GB)
Dec-05 01:56:59.379 [main] DEBUG nextflow.Session - Work-dir: /workspaces/nf-tractoflow/work [ext2/ext3]
Dec-05 01:56:59.380 [main] DEBUG nextflow.Session - Script base path does not exist or is not a directory: /workspaces/nf-tractoflow/bin
Dec-05 01:56:59.389 [main] DEBUG nextflow.executor.ExecutorFactory - Extension executors providers=[]
Dec-05 01:56:59.400 [main] DEBUG nextflow.Session - Observer factory (v2): LinObserverFactory
Dec-05 01:56:59.403 [main] DEBUG nextflow.Session - Observer factory (v2): DefaultObserverFactory
Dec-05 01:56:59.427 [main] DEBUG nextflow.Session - Observer factory: ValidationObserverFactory
Dec-05 01:56:59.490 [main] DEBUG nextflow.cache.CacheFactory - Using Nextflow cache factory: nextflow.cache.DefaultCacheFactory
Dec-05 01:56:59.499 [main] DEBUG nextflow.util.CustomThreadPool - Creating default thread pool > poolSize: 9; maxThreads: 1000
Dec-05 01:56:59.681 [main] DEBUG nextflow.Session - Session start
Dec-05 01:56:59.699 [main] DEBUG nextflow.trace.TraceFileObserver - Workflow started -- trace file: results/pipeline_info/execution_trace_2025-12-05_01-56-56.txt
Dec-05 01:56:59.888 [main] DEBUG nextflow.script.ScriptRunner - > Launching execution
Dec-05 01:57:00.569 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsSummaryMap:paramsSummaryMap]; plugin Id: nf-schema
Dec-05 01:57:00.978 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [fromBIDS:fromBIDS]; plugin Id: nf-bids
Dec-05 01:57:01.029 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsSummaryLog:paramsSummaryLog]; plugin Id: nf-schema
Dec-05 01:57:01.030 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [validateParameters:validateParameters]; plugin Id: nf-schema
Dec-05 01:57:01.031 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsHelp:paramsHelp]; plugin Id: nf-schema
Dec-05 01:57:01.035 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsSummaryMap:paramsSummaryMap]; plugin Id: nf-schema
Dec-05 01:57:01.036 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [samplesheetToList:samplesheetToList]; plugin Id: nf-schema
Dec-05 01:57:06.199 [main] WARN  nextflow.script.IncludeDef - Include with \`addParams()\` is deprecated -- pass params as a workflow or process input instead
Dec-05 01:57:07.781 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [fromBIDS:fromBIDS]; plugin Id: nf-bids
Dec-05 01:57:07.831 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsSummaryLog:paramsSummaryLog]; plugin Id: nf-schema
Dec-05 01:57:07.832 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [validateParameters:validateParameters]; plugin Id: nf-schema
Dec-05 01:57:07.832 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsHelp:paramsHelp]; plugin Id: nf-schema
Dec-05 01:57:07.835 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [paramsSummaryMap:paramsSummaryMap]; plugin Id: nf-schema
Dec-05 01:57:07.836 [main] DEBUG nextflow.script.IncludeDef - Loading included plugin extensions with names: [samplesheetToList:samplesheetToList]; plugin Id: nf-schema
Dec-05 01:57:08.184 [main] INFO  nextflow.Nextflow - 
Dec-05 01:57:08.205 [main] INFO  nextflow.Nextflow - 
        -[2m------------------------------------------------------[0m-
                                                    [0;32m _.--'"'.[0m
        [0;34m        ___          ___       __   __      [0;32m(  ( (   )[0m
        [0;34m  |\ | |__  __ |\ | |__  |  | |__) /  \     [0;33m(o)_    ) )[0m
        [0;34m  | \| |       | \| |___ |__| |  \ \__/     [0;32m    (o)_.'[0m
                                                    [0;32m     )/[0m
        [0;35m  scilus/sf-tractomics 1.0dev[0m
        -[2m------------------------------------------------------[0m-
        [1mInput/output options[0m
  [0;34minput              : [0;32massets/tests/dummy_bids/[0m
  [0;34moutdir             : [0;32mresults[0m

[1mResampling options[0m
  [0;34mdwi_resolution     : [0;32m1[0m
  [0;34mt1_resolution      : [0;32m1[0m

[1mParticle Filtering Tractography options[0m
  [0;34mpft_theta          : [0;32m20[0m
  [0;34mpft_min_len        : [0;32m20[0m
  [0;34mpft_max_len        : [0;32m200[0m

[1mLocal Tractography options[0m
  [0;34mlocal_theta        : [0;32m20[0m
  [0;34mlocal_min_len      : [0;32m20[0m
  [0;34mlocal_max_len      : [0;32m200[0m

[1mGeneric options[0m
  [0;34mtrace_report_suffix: [0;32m2025-12-05_01-56-56[0m

[1mCore Nextflow options[0m
  [0;34mrunName            : [0;32mintergalactic_bassi[0m
  [0;34mcontainerEngine    : [0;32mdocker[0m
  [0;34mlaunchDir          : [0;32m/workspaces/nf-tractoflow[0m
  [0;34mworkDir            : [0;32m/workspaces/nf-tractoflow/work[0m
  [0;34mprojectDir         : [0;32m/workspaces/nf-tractoflow[0m
  [0;34muserName           : [0;32mroot[0m
  [0;34mprofile            : [0;32mdocker[0m
  [0;34mconfigFiles        : [0;32m/workspaces/nf-tractoflow/nextflow.config[0m

!! Only displaying parameters that differ from the pipeline defaults !!
-[2m----------------------------------------------------[0m-
        * The nf-neuro project
            https://scilus.github.io/nf-neuro

        * The nf-core framework
            https://doi.org/10.1038/s41587-020-0439-x

        * Software dependencies
            https://github.com/scilus/sf-tractomics/blob/master/CITATIONS.md
        
Dec-05 01:57:08.207 [main] INFO  nextflow.Nextflow - 
Dec-05 01:57:08.208 [main] DEBUG n.validation.ValidationExtension - Starting parameters validation
Dec-05 01:57:08.397 [main] DEBUG nextflow.validation.SchemaEvaluator - Could not validate the file /workspaces/nf-tractoflow/assets/tests/dummy_bids
Dec-05 01:57:08.402 [main] DEBUG n.validation.ValidationExtension - Finishing parameters validation
Dec-05 01:57:08.405 [main] WARN  nextflow.Nextflow - nf-core pipelines do not accept positional arguments. The positional argument \`true\` has been detected.
HINT: A common mistake is to provide multiple values separated by spaces e.g. \`-profile test, docker\`.

Dec-05 01:57:08.407 [main] WARN  nextflow.script.ScriptBinding - Access to undefined parameter \`bidsconfig\` -- Initialise it to a default value eg. \`params.bidsconfig = some_value\`
Dec-05 01:57:08.407 [main] WARN  nextflow.script.ScriptBinding - Access to undefined parameter \`libbids_sh\` -- Initialise it to a default value eg. \`params.libbids_sh = some_value\`
Dec-05 01:57:08.411 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids] Starting BIDS dataset parsing: assets/tests/dummy_bids/
Dec-05 01:57:08.412 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids] ✈︎✈︎✈︎ Pre-flight checks started
Dec-05 01:57:08.412 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids] ✓ Pre-flight checks completed
Dec-05 01:57:08.471 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config] Loaded BIDS configuration from: /workspaces/nf-tractoflow/assets/bidsconfig.yaml
Dec-05 01:57:08.472 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config] Configuration keys: [loop_over, T1w, lesion, wmparc, aparc_aseg, dwi, dwi_ap, dwi_rl, dwi_is, sbref, sbref_ap, sbref_rl, sbref_is]
Dec-05 01:57:08.472 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   loop_over: ArrayList = [subject, session, run]
Dec-05 01:57:08.472 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   T1w: LinkedHashMap = [plain_set:[:], suffix_maps_to:T1w]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   lesion: LinkedHashMap = [plain_set:[description:lesion], suffix_maps_to:mask]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   wmparc: LinkedHashMap = [plain_set:[description:wmparc], suffix_maps_to:mask]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   aparc_aseg: LinkedHashMap = [plain_set:[description:aparc+aseg], suffix_maps_to:mask]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   dwi: LinkedHashMap = [plain_set:[:], additional_extensions:[bvec, bval]]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   dwi_ap: LinkedHashMap = [named_set:[ap:[direction:dir-AP], pa:[direction:dir-PA]], required:[ap, pa], additional_extensions:[bvec, bval], suffix_maps_to:dwi]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   dwi_rl: LinkedHashMap = [named_set:[rl:[direction:dir-RL], lr:[direction:dir-LR]], required:[rl, lr], additional_extensions:[bvec, bval], suffix_maps_to:dwi]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   dwi_is: LinkedHashMap = [named_set:[is:[direction:dir-IS], si:[direction:dir-SI]], required:[is, si], additional_extensions:[bvec, bval], suffix_maps_to:dwi]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   sbref: LinkedHashMap = [plain_set:[:]]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   sbref_ap: LinkedHashMap = [named_set:[ap:[direction:dir-AP], pa:[direction:dir-PA]], required:[ap, pa], suffix_maps_to:sbref]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   sbref_rl: LinkedHashMap = [named_set:[rl:[direction:dir-RL], lr:[direction:dir-LR]], required:[rl, lr], suffix_maps_to:sbref]
Dec-05 01:57:08.473 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-config]   sbref_is: LinkedHashMap = [named_set:[is:[direction:dir-IS], si:[direction:dir-SI]], required:[is, si], suffix_maps_to:sbref]
Dec-05 01:57:08.480 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for plain_set: T1w -> T1w
Dec-05 01:57:08.480 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for plain_set: mask -> lesion
Dec-05 01:57:08.480 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for plain_set: mask -> wmparc
Dec-05 01:57:08.480 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for plain_set: mask -> aparc_aseg
Dec-05 01:57:08.480 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: dwi -> dwi_ap
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: dwi -> dwi_rl
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: dwi -> dwi_is
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: sbref -> sbref_ap
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: sbref -> sbref_rl
Dec-05 01:57:08.481 [main] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Suffix mapping for named_set: sbref -> sbref_is
Dec-05 01:57:08.483 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Built suffix mappings: [plain_set:[T1w:T1w, mask:aparc_aseg], named_set:[dwi:dwi_is, sbref:sbref_is]]
Dec-05 01:57:08.485 [main] INFO  n.plugin.config.BidsConfigAnalyzer - Analyzing configuration with keys: [loop_over, T1w, lesion, wmparc, aparc_aseg, dwi, dwi_ap, dwi_rl, dwi_is, sbref, sbref_ap, sbref_rl, sbref_is]
Dec-05 01:57:08.486 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking loop_over: ArrayList
Dec-05 01:57:08.486 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking T1w: LinkedHashMap
Dec-05 01:57:08.486 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [plain_set, suffix_maps_to]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found plain_set!
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking lesion: LinkedHashMap
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [plain_set, suffix_maps_to]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found plain_set!
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking wmparc: LinkedHashMap
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [plain_set, suffix_maps_to]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found plain_set!
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking aparc_aseg: LinkedHashMap
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [plain_set, suffix_maps_to]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found plain_set!
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking dwi: LinkedHashMap
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [plain_set, additional_extensions]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found plain_set!
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking dwi_ap: LinkedHashMap
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [named_set, required, additional_extensions, suffix_maps_to]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found named_set!
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking dwi_rl: LinkedHashMap
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [named_set, required, additional_extensions, suffix_maps_to]
Dec-05 01:57:08.487 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found named_set!
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking dwi_is: LinkedHashMap
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [named_set, required, additional_extensions, suffix_maps_to]
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found named_set!
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking sbref: LinkedHashMap
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [plain_set]
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found plain_set!
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking sbref_ap: LinkedHashMap
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [named_set, required, suffix_maps_to]
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found named_set!
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking sbref_rl: LinkedHashMap
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [named_set, required, suffix_maps_to]
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found named_set!
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -   Checking sbref_is: LinkedHashMap
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Value is a Map with keys: [named_set, required, suffix_maps_to]
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer -     Found named_set!
Dec-05 01:57:08.488 [main] INFO  n.plugin.config.BidsConfigAnalyzer - Analysis results: named=true, sequential=false, mixed=false, plain=true
Dec-05 01:57:08.489 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ┌─ ✓ Configuration analysis complete:
Dec-05 01:57:08.489 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ↬ Loop over entities: subject, session, run
Dec-05 01:57:08.490 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ⑆ Named sets: 6 patterns (dwi_ap, dwi_rl, dwi_is, sbref_ap, sbref_rl, sbref_is)
Dec-05 01:57:08.490 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ⑇ Sequential sets: 0 patterns ()
Dec-05 01:57:08.490 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ⑈ Mixed sets: 0 patterns ()
Dec-05 01:57:08.490 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ⑉ Plain sets: 6 patterns (T1w, lesion, wmparc, aparc_aseg, dwi, sbref)
Dec-05 01:57:08.490 [main] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ = TOTAL patterns: 12
Dec-05 01:57:08.719 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI
Dec-05 01:57:08.730 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI
Dec-05 01:57:08.752 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:08.754 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:08.768 [main] DEBUG nextflow.executor.Executor - [warm up] executor > local
Dec-05 01:57:08.778 [main] DEBUG n.processor.LocalPollingMonitor - Creating local task monitor for executor 'local' > cpus=8; memory=62.5 GB; capacity=8; pollInterval=100ms; dumpInterval=5m
Dec-05 01:57:08.781 [main] DEBUG n.processor.TaskPollingMonitor - >>> barrier register (monitor: local)
Dec-05 01:57:08.806 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI': maxForks=0; fair=false; array=0
Dec-05 01:57:08.860 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_REVDWI
Dec-05 01:57:08.863 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:DENOISE_REVDWI\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_REVDWI
Dec-05 01:57:08.868 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:08.868 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:08.870 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_REVDWI': maxForks=0; fair=false; array=0
Dec-05 01:57:08.907 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_TOPUP
Dec-05 01:57:08.908 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_TOPUP\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_TOPUP
Dec-05 01:57:08.913 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:08.913 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:08.914 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_TOPUP': maxForks=0; fair=false; array=0
Dec-05 01:57:08.949 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high\` matches labels \`process_high\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_EDDY
Dec-05 01:57:08.951 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_EDDY\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_EDDY
Dec-05 01:57:08.960 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:08.960 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:08.961 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_EDDY': maxForks=0; fair=false; array=0
Dec-05 01:57:08.993 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:UTILS_EXTRACTB0
Dec-05 01:57:08.994 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:UTILS_EXTRACTB0\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:UTILS_EXTRACTB0
Dec-05 01:57:08.997 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:08.997 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:08.998 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:UTILS_EXTRACTB0': maxForks=0; fair=false; array=0
Dec-05 01:57:09.035 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:BETCROP_FSLBETCROP
Dec-05 01:57:09.037 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:BETCROP_FSLBETCROP\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:BETCROP_FSLBETCROP
Dec-05 01:57:09.040 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.040 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.041 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:BETCROP_FSLBETCROP': maxForks=0; fair=false; array=0
Dec-05 01:57:09.059 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:N4_DWI
Dec-05 01:57:09.060 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_medium,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:N4_DWI
Dec-05 01:57:09.061 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:N4_DWI\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:N4_DWI
Dec-05 01:57:09.064 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.064 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.064 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:N4_DWI': maxForks=0; fair=false; array=0
Dec-05 01:57:09.079 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:NORMALIZE_DWI
Dec-05 01:57:09.082 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:NORMALIZE_DWI\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:NORMALIZE_DWI
Dec-05 01:57:09.086 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.086 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.088 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:NORMALIZE_DWI': maxForks=0; fair=false; array=0
Dec-05 01:57:09.102 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI
Dec-05 01:57:09.102 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_single,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI
Dec-05 01:57:09.105 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI
Dec-05 01:57:09.108 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.108 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.109 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI': maxForks=0; fair=false; array=0
Dec-05 01:57:09.119 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:EXTRACTB0_RESAMPLE
Dec-05 01:57:09.121 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:EXTRACTB0_RESAMPLE\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:EXTRACTB0_RESAMPLE
Dec-05 01:57:09.124 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.124 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.125 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:EXTRACTB0_RESAMPLE': maxForks=0; fair=false; array=0
Dec-05 01:57:09.134 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK
Dec-05 01:57:09.134 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_single,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK
Dec-05 01:57:09.136 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK
Dec-05 01:57:09.138 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.138 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.139 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK': maxForks=0; fair=false; array=0
Dec-05 01:57:09.166 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS
Dec-05 01:57:09.167 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS
Dec-05 01:57:09.170 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.170 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.171 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS': maxForks=0; fair=false; array=0
Dec-05 01:57:09.186 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:PREPROC_N4
Dec-05 01:57:09.186 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_medium,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:PREPROC_N4
Dec-05 01:57:09.190 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.190 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.190 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:PREPROC_N4': maxForks=0; fair=false; array=0
Dec-05 01:57:09.206 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE
Dec-05 01:57:09.207 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_single,process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE
Dec-05 01:57:09.209 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE
Dec-05 01:57:09.212 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.212 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.213 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE': maxForks=0; fair=false; array=0
Dec-05 01:57:09.238 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high\` matches labels \`process_high\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:BETCROP_ANTSBET
Dec-05 01:57:09.240 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_T1:BETCROP_ANTSBET\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:BETCROP_ANTSBET
Dec-05 01:57:09.243 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.243 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.244 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:BETCROP_ANTSBET': maxForks=0; fair=false; array=0
Dec-05 01:57:09.256 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_T1
Dec-05 01:57:09.261 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_T1\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_T1
Dec-05 01:57:09.264 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.265 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.266 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_T1': maxForks=0; fair=false; array=0
Dec-05 01:57:09.277 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_MASK
Dec-05 01:57:09.279 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_MASK\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_MASK
Dec-05 01:57:09.282 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.282 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.282 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_MASK': maxForks=0; fair=false; array=0
Dec-05 01:57:09.304 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_DTIMETRICS
Dec-05 01:57:09.306 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:RECONST_DTIMETRICS\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_DTIMETRICS
Dec-05 01:57:09.311 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.311 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.312 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_DTIMETRICS': maxForks=0; fair=false; array=0
Dec-05 01:57:09.346 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANATTODWI
Dec-05 01:57:09.348 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANATTODWI\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANATTODWI
Dec-05 01:57:09.352 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.352 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.353 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANATTODWI': maxForks=0; fair=false; array=0
Dec-05 01:57:09.370 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_medium\` matches labels \`process_medium\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANTS
Dec-05 01:57:09.375 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.376 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.376 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANTS': maxForks=0; fair=false; array=0
Dec-05 01:57:09.420 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_low\` matches labels \`process_low\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_WMPARC
Dec-05 01:57:09.423 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:TRANSFORM_WMPARC\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_WMPARC
Dec-05 01:57:09.425 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.425 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.426 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_WMPARC': maxForks=0; fair=false; array=0
Dec-05 01:57:09.440 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_low\` matches labels \`process_low\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_APARC_ASEG
Dec-05 01:57:09.443 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:TRANSFORM_APARC_ASEG\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_APARC_ASEG
Dec-05 01:57:09.445 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.445 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.446 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_APARC_ASEG': maxForks=0; fair=false; array=0
Dec-05 01:57:09.466 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_low\` matches labels \`process_low\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_LESION_MASK
Dec-05 01:57:09.468 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:TRANSFORM_LESION_MASK\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_LESION_MASK
Dec-05 01:57:09.471 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.471 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.472 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_LESION_MASK': maxForks=0; fair=false; array=0
Dec-05 01:57:09.495 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FASTSEG
Dec-05 01:57:09.497 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FASTSEG\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FASTSEG
Dec-05 01:57:09.499 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.499 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.500 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FASTSEG': maxForks=0; fair=false; array=0
Dec-05 01:57:09.520 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FREESURFERSEG
Dec-05 01:57:09.521 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FREESURFERSEG\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FREESURFERSEG
Dec-05 01:57:09.523 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.524 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.524 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FREESURFERSEG': maxForks=0; fair=false; array=0
Dec-05 01:57:09.571 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FRF
Dec-05 01:57:09.572 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:RECONST_FRF\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FRF
Dec-05 01:57:09.576 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.576 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.577 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FRF': maxForks=0; fair=false; array=0
Dec-05 01:57:09.625 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high\` matches labels \`process_high\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FODF
Dec-05 01:57:09.627 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:RECONST_FODF\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FODF
Dec-05 01:57:09.630 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.631 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.632 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FODF': maxForks=0; fair=false; array=0
Dec-05 01:57:09.664 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_PFTTRACKING
Dec-05 01:57:09.666 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:TRACKING_PFTTRACKING\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_PFTTRACKING
Dec-05 01:57:09.669 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.669 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.670 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_PFTTRACKING': maxForks=0; fair=false; array=0
Dec-05 01:57:09.726 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_high_memory\` matches labels \`process_high_memory\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_LOCALTRACKING
Dec-05 01:57:09.728 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:TRACTOFLOW:TRACKING_LOCALTRACKING\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_LOCALTRACKING
Dec-05 01:57:09.733 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.733 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.734 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_LOCALTRACKING': maxForks=0; fair=false; array=0
Dec-05 01:57:09.817 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:ENSEMBLE_TRACKING
Dec-05 01:57:09.823 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:.*:ENSEMBLE_TRACKING\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:ENSEMBLE_TRACKING
Dec-05 01:57:09.826 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.827 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.827 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:ENSEMBLE_TRACKING': maxForks=0; fair=false; array=0
Dec-05 01:57:09.839 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:QC_ENSEMBLE
Dec-05 01:57:09.846 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:09.846 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:09.847 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:QC_ENSEMBLE': maxForks=0; fair=false; array=0
Dec-05 01:57:09.999 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC
Dec-05 01:57:10.002 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:MULTIQC\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC
Dec-05 01:57:10.007 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:10.007 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:10.008 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC': maxForks=0; fair=false; array=0
Dec-05 01:57:10.058 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withLabel:process_single\` matches labels \`process_single\` for process with name SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC_GLOBAL
Dec-05 01:57:10.060 [main] DEBUG n.script.dsl.ProcessConfigBuilder - Config settings \`withName:MULTIQC_GLOBAL\` matches process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC_GLOBAL
Dec-05 01:57:10.064 [main] DEBUG nextflow.executor.ExecutorFactory - << taskConfig executor: null
Dec-05 01:57:10.064 [main] DEBUG nextflow.executor.ExecutorFactory - >> processorType: 'local'
Dec-05 01:57:10.065 [main] DEBUG nextflow.processor.TaskProcessor - Creating process 'SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC_GLOBAL': maxForks=0; fair=false; array=0
Dec-05 01:57:10.093 [main] DEBUG nextflow.Session - Config process names validation disabled as requested
Dec-05 01:57:10.098 [main] DEBUG nextflow.Session - Igniting dataflow network (105)
Dec-05 01:57:10.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-parser] Parsing BIDS dataset: assets/tests/dummy_bids/
Dec-05 01:57:10.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [libBIDS-wrapper] Found libBIDS.sh in plugin installation: /workspaces/.nextflow/plugins/nf-bids-0.1.0-beta.8/lib/libBIDS.sh
Dec-05 01:57:10.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [libBIDS-wrapper] Executing libBIDS.sh parser on: assets/tests/dummy_bids/
Dec-05 01:57:10.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [libBIDS-wrapper] Using libBIDS.sh at: /workspaces/.nextflow/plugins/nf-bids-0.1.0-beta.8/lib/libBIDS.sh
Dec-05 01:57:10.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [libBIDS-wrapper] Output CSV: /tmp/bids_parsed_17842977599795041458.csv
Dec-05 01:57:10.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [libBIDS-wrapper] Command: [bash, -c, set -euo pipefail && source "$1" && libBIDSsh_parse_bids_to_csv "$2" > "$3", bash, /workspaces/.nextflow/plugins/nf-bids-0.1.0-beta.8/lib/libBIDS.sh, assets/tests/dummy_bids/, /tmp/bids_parsed_17842977599795041458.csv]
Dec-05 01:57:10.107 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI
Dec-05 01:57:10.108 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_REVDWI
Dec-05 01:57:10.109 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_TOPUP
Dec-05 01:57:10.109 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_EDDY
Dec-05 01:57:10.109 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:UTILS_EXTRACTB0
Dec-05 01:57:10.109 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:BETCROP_FSLBETCROP
Dec-05 01:57:10.110 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:N4_DWI
Dec-05 01:57:10.110 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:NORMALIZE_DWI
Dec-05 01:57:10.111 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI
Dec-05 01:57:10.111 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:EXTRACTB0_RESAMPLE
Dec-05 01:57:10.111 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK
Dec-05 01:57:10.112 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS
Dec-05 01:57:10.112 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:PREPROC_N4
Dec-05 01:57:10.113 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE
Dec-05 01:57:10.113 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:BETCROP_ANTSBET
Dec-05 01:57:10.114 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_T1
Dec-05 01:57:10.114 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_MASK
Dec-05 01:57:10.114 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_DTIMETRICS
Dec-05 01:57:10.114 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANATTODWI
Dec-05 01:57:10.114 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANTS
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_WMPARC
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_APARC_ASEG
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_LESION_MASK
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FASTSEG
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FREESURFERSEG
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FRF
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FODF
Dec-05 01:57:10.115 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_PFTTRACKING
Dec-05 01:57:10.116 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_LOCALTRACKING
Dec-05 01:57:10.116 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:ENSEMBLE_TRACKING
Dec-05 01:57:10.116 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:QC_ENSEMBLE
Dec-05 01:57:10.118 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC
Dec-05 01:57:10.121 [main] DEBUG nextflow.processor.TaskProcessor - Starting process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC_GLOBAL
Dec-05 01:57:10.123 [main] DEBUG nextflow.script.ScriptRunner - Parsed script files:
  Script_2f9ffdf939afbbd9: /workspaces/nf-tractoflow/subworkflows/nf-neuro/preproc_t1/main.nf
  Script_bb2f7b4f867bf2cc: /workspaces/nf-tractoflow/modules/nf-neuro/registration/synthmorph/main.nf
  Script_2df3063afcf5b530: /workspaces/nf-tractoflow/modules/nf-neuro/utils/extractb0/main.nf
  Script_be6d3b182b271603: /workspaces/nf-tractoflow/subworkflows/nf-neuro/anatomical_segmentation/main.nf
  Script_2d97f02d408a566b: /workspaces/nf-tractoflow/subworkflows/nf-neuro/bundle_seg/main.nf
  Script_8aba9dd315fca708: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/freewater/main.nf
  Script_455dc2f7c946df54: /workspaces/nf-tractoflow/modules/nf-neuro/denoising/nlmeans/main.nf
  Script_736c3bfc4d9fb71d: /workspaces/nf-tractoflow/subworkflows/nf-neuro/reconst_fw_noddi/main.nf
  Script_2b7fa7f92f128d26: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/meanfrf/main.nf
  Script_112a3912160fc26b: /workspaces/nf-tractoflow/modules/nf-neuro/tractogram/resample/main.nf
  Script_42c33ed2026392c1: /workspaces/nf-tractoflow/modules/nf-neuro/tractogram/math/main.nf
  Script_de91a6e8298ee515: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/diffusivitypriors/main.nf
  Script_9aa9fe1773cb448f: /workspaces/nf-tractoflow/workflows/sf-tractomics.nf
  Script_5d6dd82df8d7c3c2: /workspaces/nf-tractoflow/modules/nf-neuro/qc/tractogram/main.nf
  Script_21cb2a260a5b01ad: /workspaces/nf-tractoflow/subworkflows/nf-neuro/registration/main.nf
  Script_d342c672562a66c4: /workspaces/nf-tractoflow/modules/nf-neuro/registration/ants/main.nf
  Script_cbc608857e25dd31: /workspaces/nf-tractoflow/modules/nf-neuro/registration/convert/main.nf
  Script_11d0d76f806b2477: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/shsignal/main.nf
  Script_a94d150435d2d9e1: /workspaces/nf-tractoflow/modules/nf-neuro/bundle/stats/main.nf
  Script_31c3f1047ba02378: /workspaces/nf-tractoflow/modules/nf-neuro/preproc/eddy/main.nf
  Script_e2f38101349c1392: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/dtimetrics/main.nf
  Script_c1518306e626ceb7: /workspaces/nf-tractoflow/modules/nf-neuro/tractogram/removeinvalid/main.nf
  Script_ff9869e2c2a63be4: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/qball/main.nf
  Script_81d03a272757cb80: /workspaces/nf-tractoflow/modules/nf-neuro/segmentation/fastseg/main.nf
  Script_b7191ff88ebe68a4: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/meandiffusivitypriors/main.nf
  Script_409b25371dc0678b: /workspaces/nf-tractoflow/modules/nf-neuro/segmentation/freesurferseg/main.nf
  Script_9407f937718ba987: /workspaces/nf-tractoflow/modules/local/io/safecastinputs/main.nf
  Script_cbfb68bdc118c163: /workspaces/nf-tractoflow/modules/nf-neuro/preproc/topup/main.nf
  Script_8dead906fc4bf4e9: /workspaces/nf-tractoflow/subworkflows/local/utils_nfcore_sf-tractomics_pipeline/main.nf
  Script_12d5308f92775892: /workspaces/nf-tractoflow/modules/nf-neuro/bundle/centroid/main.nf
  Script_80692f44ed67f93c: /workspaces/nf-tractoflow/subworkflows/nf-neuro/tractoflow/main.nf
  Script_7db9fdd8ba82a764: /workspaces/nf-tractoflow/modules/nf-neuro/image/math/main.nf
  Script_4f323e730e6e371e: /workspaces/nf-tractoflow/modules/nf-neuro/preproc/normalize/main.nf
  Script_389732afbe4df49e: /workspaces/nf-tractoflow/subworkflows/nf-neuro/tractometry/main.nf
  Script_c0cea35fd7459d66: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/frf/main.nf
  Script_1398a026eb397b44: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/fodf/main.nf
  Script_5c4e8d4051efa81e: /workspaces/nf-tractoflow/subworkflows/nf-core/utils_nfcore_pipeline/main.nf
  Script_27606586420d23e2: /workspaces/nf-tractoflow/modules/nf-neuro/image/resample/main.nf
  Script_ccd0b138a70c0e6d: /workspaces/nf-tractoflow/modules/nf-neuro/bundle/labelmap/main.nf
  Script_7ea198b181d51f8e: /workspaces/nf-tractoflow/subworkflows/nf-neuro/atlas_iit/main.nf
  Script_8c5818b7aabecbed: /workspaces/nf-tractoflow/modules/nf-neuro/registration/anattodwi/main.nf
  Script_a64a54a01f8015c7: /workspaces/nf-tractoflow/subworkflows/nf-core/utils_nfschema_plugin/main.nf
  Script_00f2459c0534bdae: /workspaces/nf-tractoflow/modules/nf-neuro/io/readbids/main.nf
  Script_c474f999c442eec1: /workspaces/nf-tractoflow/modules/nf-neuro/bundle/fixelafd/main.nf
  Script_773cf2e2032888ad: /workspaces/nf-tractoflow/modules/nf-neuro/betcrop/synthstrip/main.nf
  Script_c72310babcc74c38: /workspaces/nf-tractoflow/modules/nf-neuro/tracking/pfttracking/main.nf
  Script_145b63ebbbee0db8: /workspaces/nf-tractoflow/modules/nf-neuro/segmentation/synthseg/main.nf
  Script_e045b2592328d50b: /workspaces/nf-tractoflow/subworkflows/nf-neuro/preproc_dwi/main.nf
  Script_7e12ae2b3f1cf5dc: /workspaces/nf-tractoflow/modules/nf-neuro/registration/antsapplytransforms/main.nf
  Script_dec74d9be344d1de: /workspaces/nf-tractoflow/subworkflows/nf-neuro/io_bids/main.nf
  Script_d2214b81fedaad02: /workspaces/nf-tractoflow/modules/nf-neuro/tracking/localtracking/main.nf
  Script_e9f10f4836823c32: /workspaces/nf-tractoflow/modules/nf-neuro/preproc/n4/main.nf
  Script_91b7255802d5b260: /workspaces/nf-tractoflow/main.nf
  Script_4f612cfd8c52e8cd: /workspaces/nf-tractoflow/subworkflows/nf-core/utils_nextflow_pipeline/main.nf
  Script_22bf4e2be2d755cc: /workspaces/nf-tractoflow/subworkflows/nf-neuro/topup_eddy/main.nf
  Script_5b6ee02d1e020c05: /workspaces/nf-tractoflow/modules/nf-neuro/denoising/mppca/main.nf
  Script_2d0c1fc15a80c462: /workspaces/nf-tractoflow/modules/nf-neuro/bundle/uniformize/main.nf
  Script_537facf57b29e07d: /workspaces/nf-tractoflow/modules/nf-neuro/betcrop/antsbet/main.nf
  Script_42ca8c64ea0a53bf: /workspaces/nf-tractoflow/modules/nf-neuro/betcrop/fslbetcrop/main.nf
  Script_1f9da8948b79b273: /workspaces/nf-tractoflow/modules/nf-neuro/registration/easyreg/main.nf
  Script_db820217c23d568c: /workspaces/nf-tractoflow/modules/nf-neuro/bundle/recognize/main.nf
  Script_846af580eb469609: /workspaces/nf-tractoflow/modules/nf-neuro/preproc/gibbs/main.nf
  Script_21d40020418efdd0: /workspaces/nf-tractoflow/modules/nf-neuro/qc/multiqc/main.nf
  Script_80781b933270616a: /workspaces/nf-tractoflow/modules/nf-neuro/stats/metricsinroi/main.nf
  Script_15de5d41e2b7bcc4: /workspaces/nf-tractoflow/modules/nf-neuro/reconst/noddi/main.nf
  Script_ba46ce49d4dac46a: /workspaces/nf-tractoflow/modules/nf-neuro/image/cropvolume/main.nf
Dec-05 01:57:10.124 [main] DEBUG nextflow.script.ScriptRunner - > Awaiting termination 
Dec-05 01:57:10.124 [main] DEBUG nextflow.Session - Session await
Dec-05 01:57:10.976 [Actor Thread 4] DEBUG nextflow.sort.BigSort - Sort completed -- entries: 1; slices: 1; internal sort time: 0.001 s; external sort time: 0.016 s; total time: 0.017 s
Dec-05 01:57:10.976 [Actor Thread 7] DEBUG nextflow.sort.BigSort - Sort completed -- entries: 1; slices: 1; internal sort time: 0.001 s; external sort time: 0.016 s; total time: 0.017 s
Dec-05 01:57:10.977 [Actor Thread 4] DEBUG nextflow.file.FileCollector - >> temp file exists? true
Dec-05 01:57:10.977 [Actor Thread 7] DEBUG nextflow.file.FileCollector - >> temp file exists? false
Dec-05 01:57:10.978 [Actor Thread 7] DEBUG nextflow.file.FileCollector - Missed collect-file cache -- cause: java.nio.file.NoSuchFileException: /workspaces/nf-tractoflow/work/collect-file/16d465169fc38c40b41dc13dc73dd53e
Dec-05 01:57:10.979 [Actor Thread 4] DEBUG nextflow.file.FileCollector - Retrieved cached collect-files from: /workspaces/nf-tractoflow/work/collect-file/1cfc3f048994d322cdfb6b0acf1efdbc
Dec-05 01:57:10.993 [Actor Thread 4] DEBUG nextflow.file.FileCollector - Deleting file collector temp dir: /tmp/nxf-1738715488407542476
Dec-05 01:57:11.115 [Actor Thread 7] DEBUG nextflow.file.FileCollector - Saved collect-files list to: /workspaces/nf-tractoflow/work/collect-file/16d465169fc38c40b41dc13dc73dd53e
Dec-05 01:57:11.117 [Actor Thread 7] DEBUG nextflow.file.FileCollector - Deleting file collector temp dir: /tmp/nxf-10834603640597716573
Dec-05 01:57:12.373 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [libBIDS-wrapper] BIDS parsing completed: 45577 bytes written
Dec-05 01:57:12.374 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - CSV Header: derivatives,data_type,subject,session,sample,task,tracksys,acquisition,nucleus,volume,ceagent,tracer,stain,reconstruction,direction,run,modality,echo,flip,inversion,mtransfer,part,processing,hemisphere,space,split,recording,chunk,segmentation,resolution,density,label,description,suffix,extension,path
Dec-05 01:57:12.376 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - First data row: NA,anat,sub-01,ses-02,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,T1w,json,assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json
Dec-05 01:57:12.388 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.390 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.394 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.394 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.397 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.397 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.398 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.400 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.400 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.401 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.403 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.403 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.404 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.406 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.407 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.407 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.409 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.409 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.409 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.411 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.412 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.412 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.414 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '01'
Dec-05 01:57:12.414 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.414 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.416 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.416 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.419 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.419 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.421 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.421 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.421 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.423 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.424 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.424 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.426 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.426 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.426 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.428 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.428 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.428 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.430 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.430 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.430 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.433 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '02'
Dec-05 01:57:12.433 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.433 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.435 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.435 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.437 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.438 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.440 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.440 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.441 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.443 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.443 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.443 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.445 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.445 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.445 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.447 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.447 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.447 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.449 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.449 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.449 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.451 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.451 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.451 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.453 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.453 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.453 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.454 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.455 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.455 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.456 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.456 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.456 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.457 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.457 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.458 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.458 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.460 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.460 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.460 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.462 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.462 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.462 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.463 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.463 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.463 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.465 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.465 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.465 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.466 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.466 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.467 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.468 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.468 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.468 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.470 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.470 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.470 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.471 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '03'
Dec-05 01:57:12.471 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.472 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.473 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.473 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.474 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.474 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.476 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.476 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.476 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.478 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.478 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.478 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.480 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.480 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.480 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.482 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.482 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.482 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.484 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.484 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.484 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.486 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.487 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.488 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.491 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.491 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.491 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.493 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.493 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.493 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.495 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.495 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.497 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.498 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.500 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.500 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.500 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.503 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.503 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.503 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.505 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.505 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.506 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.507 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.507 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.507 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.509 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.509 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.510 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.511 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.512 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.512 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.514 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.514 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.514 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.516 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.516 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.516 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.518 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.518 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.518 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.520 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.520 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.520 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.522 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.522 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.522 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.523 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.524 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.524 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.526 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.526 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.526 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.527 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.527 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.529 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.529 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.530 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.531 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.531 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.532 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.532 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.533 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.534 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.534 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.534 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.535 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.535 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.535 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.536 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.537 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.537 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.538 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.538 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.538 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.539 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.540 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.540 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.541 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.541 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.542 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.543 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.543 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.543 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.544 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '04'
Dec-05 01:57:12.544 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.545 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.546 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.546 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.547 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.547 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.548 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.548 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.548 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.550 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.550 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.550 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.551 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.552 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.552 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.553 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.553 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.553 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.554 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.555 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.555 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.556 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.556 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.556 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.558 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.558 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.558 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.560 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.560 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.561 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.562 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.563 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.563 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.563 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.564 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.564 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.564 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.566 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.566 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.566 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.567 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.567 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.567 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.568 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.568 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.568 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.570 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.570 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.570 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.572 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.573 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.573 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.574 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.575 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.576 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.577 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.579 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.579 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.579 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.580 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.581 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.581 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.582 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.582 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.582 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.583 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.583 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.583 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.584 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.585 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.585 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.586 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.586 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.586 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.587 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.587 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.587 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.588 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.589 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.589 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.590 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '05'
Dec-05 01:57:12.590 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.590 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.591 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.591 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.592 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.592 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.593 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.594 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.594 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.595 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.595 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.595 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.596 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.596 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.596 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.597 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.597 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.597 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.598 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.598 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.598 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.599 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.599 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.599 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.600 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.600 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.601 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.601 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.602 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.602 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.602 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.603 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.603 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.603 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.604 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.604 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.604 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.605 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.605 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.605 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.606 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.606 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.607 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.607 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.608 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.608 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.609 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.609 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.609 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.610 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '06'
Dec-05 01:57:12.610 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.610 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.611 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.611 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.612 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.612 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.613 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.613 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.613 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.614 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.614 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.614 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.615 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.615 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.615 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.617 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.617 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.617 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.619 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.619 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.619 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.620 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.620 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.620 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.621 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.621 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.621 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.622 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.622 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.622 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.623 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.623 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.623 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.624 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.624 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.624 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.625 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.626 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.626 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.627 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.627 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.628 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.628 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.629 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.629 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.629 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.630 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.630 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.630 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.631 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.631 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.631 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.632 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.632 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.632 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.633 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.633 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.633 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.634 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.634 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.635 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.635 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.636 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.636 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.637 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.637 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.637 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.638 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.638 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.638 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.639 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.639 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.639 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.640 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.640 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.640 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.641 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.641 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.641 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.642 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '07'
Dec-05 01:57:12.642 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.642 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.643 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.643 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.644 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.644 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.645 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.646 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.646 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.647 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.647 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.647 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.649 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.649 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.649 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.650 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.650 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.650 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.651 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.651 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.651 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.652 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.652 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.652 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.653 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.653 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.653 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.654 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.654 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.655 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.655 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.656 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.656 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.657 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.658 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.658 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.658 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.659 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.659 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.659 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.660 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.660 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.660 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.661 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.661 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.661 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.662 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.662 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.662 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.663 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.663 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.663 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.664 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.664 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.664 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.665 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.665 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.666 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.666 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.668 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.668 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.668 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.670 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.670 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.670 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.671 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.671 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.671 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.672 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.672 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.672 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.673 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.673 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.673 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.674 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.674 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.674 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.675 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.675 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.675 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.676 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.676 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.676 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.677 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.677 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.677 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.678 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.678 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.678 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.679 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '08'
Dec-05 01:57:12.679 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.679 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.680 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.680 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.681 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.681 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.682 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.682 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.682 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.683 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.683 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.683 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.684 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.684 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.685 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.685 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.685 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.685 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.686 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.686 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.687 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.687 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.687 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.688 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.688 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.688 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.688 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.689 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.689 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.689 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.690 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '09'
Dec-05 01:57:12.690 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.690 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.691 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.691 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.692 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.692 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.693 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.693 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.693 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'aparc+aseg'
Dec-05 01:57:12.694 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.694 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.694 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'wmparc'
Dec-05 01:57:12.695 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.695 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.695 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.696 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.696 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.696 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.697 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.697 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.697 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.698 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.698 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.698 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.699 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.699 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.699 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.700 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.700 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.701 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.701 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.701 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.702 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.702 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.702 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '01'
Dec-05 01:57:12.702 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.703 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.703 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.704 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.704 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.705 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.705 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.705 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.706 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.706 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.706 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.707 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.707 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.707 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.708 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.708 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.708 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.709 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.709 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.709 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.710 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.710 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.710 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.711 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.711 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.711 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.712 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.712 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.712 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.713 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.713 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.713 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.714 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.714 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.714 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.715 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.715 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '02'
Dec-05 01:57:12.715 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.716 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.716 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.717 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.717 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.718 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.718 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.718 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'description' to entity 'desc' with value 'lesion'
Dec-05 01:57:12.719 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.719 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.719 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.720 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.720 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.720 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.721 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.721 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.721 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.722 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.722 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.722 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'AP'
Dec-05 01:57:12.723 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.723 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.723 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.724 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.724 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.725 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.726 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.726 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.726 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.727 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.727 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.727 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.728 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.728 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.728 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.729 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'subject' to entity 'sub' with value '10'
Dec-05 01:57:12.729 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'session' to entity 'ses' with value '03'
Dec-05 01:57:12.729 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Mapping CSV column 'direction' to entity 'dir' with value 'PA'
Dec-05 01:57:12.729 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsCsvParser - Parsed 231 BIDS files from CSV with suffixes: [T1w:42, mask:33, dwi:120, sbref:24, epi:12]
Dec-05 01:57:12.745 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-parser] ✅ Parsed 231 files from dataset 'Dummy BIDS dataset for sf-tractomics pipeline testing'
Dec-05 01:57:12.749 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ⎌ Running handler: NamedSetHandler ...
Dec-05 01:57:12.750 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing mixed sets with 231 files
Dec-05 01:57:12.751 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Loop-over entities: [subject, session, run]
Dec-05 01:57:12.754 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] Grouping files by entities: subject, session, run
Dec-05 01:57:12.754 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02
Dec-05 01:57:12.755 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02
Dec-05 01:57:12.755 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, desc-aparc+aseg
Dec-05 01:57:12.755 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, desc-lesion
Dec-05 01:57:12.755 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, desc-wmparc
Dec-05 01:57:12.756 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.756 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.756 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.756 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.757 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03
Dec-05 01:57:12.757 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03
Dec-05 01:57:12.757 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.757 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.757 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.757 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.758 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.758 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.758 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02
Dec-05 01:57:12.758 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02
Dec-05 01:57:12.758 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, desc-lesion
Dec-05 01:57:12.758 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-PA
Dec-05 01:57:12.759 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-PA
Dec-05 01:57:12.760 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03
Dec-05 01:57:12.760 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03
Dec-05 01:57:12.760 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, desc-aparc+aseg
Dec-05 01:57:12.760 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, desc-wmparc
Dec-05 01:57:12.760 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.761 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.761 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.761 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.761 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-PA
Dec-05 01:57:12.761 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-PA
Dec-05 01:57:12.761 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01
Dec-05 01:57:12.762 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01
Dec-05 01:57:12.762 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.762 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.762 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.763 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.763 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.763 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.763 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.763 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.763 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02
Dec-05 01:57:12.764 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02
Dec-05 01:57:12.764 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, desc-aparc+aseg
Dec-05 01:57:12.764 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, desc-lesion
Dec-05 01:57:12.764 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, desc-wmparc
Dec-05 01:57:12.764 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.764 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.765 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.765 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.765 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.765 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.765 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.765 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.766 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.766 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.766 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03
Dec-05 01:57:12.766 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03
Dec-05 01:57:12.767 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.767 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.767 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.767 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.767 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.767 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01
Dec-05 01:57:12.768 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, desc-aparc+aseg
Dec-05 01:57:12.769 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, desc-lesion
Dec-05 01:57:12.769 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, desc-wmparc
Dec-05 01:57:12.769 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.769 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.769 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.770 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.770 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02
Dec-05 01:57:12.770 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02
Dec-05 01:57:12.770 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, desc-lesion
Dec-05 01:57:12.770 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.770 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.771 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.771 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.771 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.771 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.771 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03
Dec-05 01:57:12.771 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03
Dec-05 01:57:12.772 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, desc-lesion
Dec-05 01:57:12.772 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.772 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.772 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.772 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.772 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-PA
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-PA
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.773 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.774 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.774 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.774 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-PA
Dec-05 01:57:12.774 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-PA
Dec-05 01:57:12.774 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02
Dec-05 01:57:12.774 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02
Dec-05 01:57:12.775 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.775 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.775 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.775 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.775 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.775 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.776 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.776 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.776 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01
Dec-05 01:57:12.776 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01
Dec-05 01:57:12.776 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, desc-lesion
Dec-05 01:57:12.776 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.777 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.777 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.778 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.778 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.778 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.778 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, desc-aparc+aseg
Dec-05 01:57:12.779 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, desc-lesion
Dec-05 01:57:12.780 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, desc-wmparc
Dec-05 01:57:12.780 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.780 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.780 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.780 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.780 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.781 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.781 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.781 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.781 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.781 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.781 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, desc-aparc+aseg
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, desc-lesion
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, desc-wmparc
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.782 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, desc-aparc+aseg
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, desc-wmparc
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.783 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, desc-aparc+aseg
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, desc-lesion
Dec-05 01:57:12.784 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, desc-wmparc
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-PA
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-PA
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02
Dec-05 01:57:12.785 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, desc-aparc+aseg
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, desc-lesion
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, desc-wmparc
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-PA
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-PA
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, desc-aparc+aseg
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, desc-wmparc
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.786 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, desc-lesion
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.787 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, desc-lesion
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.788 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.790 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json
Dec-05 01:57:12.792 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.793 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.794 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.796 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.797 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.798 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.798 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.798 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.798 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.798 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.798 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.800 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.800 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.800 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.800 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.806 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.807 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.808 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.809 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.810 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.811 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.811 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.811 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.812 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.813 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.813 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.813 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.813 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.814 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.815 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.816 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.817 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.818 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.819 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.820 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.820 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/fmap/sub-03_ses-02_dir-PA_epi.json
Dec-05 01:57:12.820 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.820 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.820 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/fmap/sub-03_ses-02_dir-PA_epi.nii.gz
Dec-05 01:57:12.820 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.821 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.821 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.821 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.821 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.821 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.821 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.822 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.823 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.824 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.825 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/fmap/sub-03_ses-03_dir-PA_epi.json
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/fmap/sub-03_ses-03_dir-PA_epi.nii.gz
Dec-05 01:57:12.826 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz
Dec-05 01:57:12.827 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.828 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.829 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.829 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.829 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.829 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.830 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.831 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.832 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.833 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.834 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.834 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.834 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.834 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.834 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.834 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval
Dec-05 01:57:12.836 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:12.836 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json
Dec-05 01:57:12.836 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:12.836 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json
Dec-05 01:57:12.836 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.837 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.838 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.839 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.840 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.841 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.842 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.843 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.844 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.845 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:12.846 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.847 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.848 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.849 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.850 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json
Dec-05 01:57:12.851 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.852 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.853 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.854 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.855 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.856 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.857 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.858 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.859 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.860 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.861 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.862 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.863 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.864 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.865 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.866 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.867 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.868 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.869 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.870 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.872 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/fmap/sub-05_ses-03_dir-PA_epi.json
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/fmap/sub-05_ses-03_dir-PA_epi.nii.gz
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.873 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.874 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.875 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.876 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/fmap/sub-06_ses-01_dir-PA_epi.json
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/fmap/sub-06_ses-01_dir-PA_epi.nii.gz
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.877 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.878 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.879 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.880 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:12.881 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.882 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.883 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.883 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.883 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.884 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.884 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.885 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.886 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.887 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.888 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.889 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.890 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json
Dec-05 01:57:12.891 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.892 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.893 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.894 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.895 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.896 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.897 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.898 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.899 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.900 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.901 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.902 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.902 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.902 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.902 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.903 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.903 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.903 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.903 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.903 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.904 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.905 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.905 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.905 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.905 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.905 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.906 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.907 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.908 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.909 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.910 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.911 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:12.912 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.913 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.914 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.915 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.916 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/fmap/sub-08_ses-03_dir-PA_epi.json
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/fmap/sub-08_ses-03_dir-PA_epi.nii.gz
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.917 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.918 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.919 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.920 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.921 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/fmap/sub-09_ses-02_dir-PA_epi.json
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/fmap/sub-09_ses-02_dir-PA_epi.nii.gz
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.922 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.923 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.924 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.925 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.926 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.927 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.928 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bval
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.json
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.929 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.930 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.931 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.932 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.932 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.932 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.932 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.932 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.933 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.934 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.934 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.936 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.936 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.936 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.936 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.936 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.937 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.938 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.939 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.940 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.941 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.941 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.941 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.941 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.941 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.941 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz
Dec-05 01:57:12.943 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: T1w - FILTERED
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'mask': mask
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No configuration for suffix: mask - FILTERED
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.944 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.945 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.946 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.947 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.948 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.949 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'dwi': dwi_is
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for dwi
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'dwi' to config key 'dwi_is'
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: dwi, config key: dwi_is
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.950 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Resolved config key for suffix 'sbref': sbref_is
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Found configuration for sbref
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'sbref' to config key 'sbref_is'
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Matched file suffix: sbref, config key: sbref_is
Dec-05 01:57:12.951 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json
Dec-05 01:57:12.952 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-named_set] No group name found for file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:12.954 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ⎌ Running handler: PlainSetHandler ...
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing mixed sets with 231 files
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Loop-over entities: [subject, session, run]
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] Grouping files by entities: subject, session, run
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, desc-aparc+aseg
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, desc-lesion
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, desc-wmparc
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-01, ses-02, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.955 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-02, ses-03, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, desc-lesion
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-AP
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-PA
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-02, dir-PA
Dec-05 01:57:12.956 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, desc-aparc+aseg
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, desc-wmparc
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-PA
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-03, ses-03, dir-PA
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-AP
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-01, dir-PA
Dec-05 01:57:12.957 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, desc-aparc+aseg
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, desc-lesion
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, desc-wmparc
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-02, dir-PA
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-AP
Dec-05 01:57:12.958 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-04, ses-03, dir-PA
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, desc-aparc+aseg
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, desc-lesion
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, desc-wmparc
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-01, dir-AP
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, desc-lesion
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.959 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-02, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, desc-lesion
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-PA
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-05, ses-03, dir-PA
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.960 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-AP
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-PA
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-01, dir-PA
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-AP
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-06, ses-02, dir-PA
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01
Dec-05 01:57:12.961 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, desc-lesion
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-AP
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.962 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-01, dir-PA
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, desc-aparc+aseg
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, desc-lesion
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, desc-wmparc
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-AP
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.963 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-07, ses-03, dir-PA
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, desc-aparc+aseg
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, desc-lesion
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, desc-wmparc
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.964 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-01, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, desc-aparc+aseg
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, desc-wmparc
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.965 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-02, dir-AP
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, desc-aparc+aseg
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, desc-lesion
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, desc-wmparc
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.966 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-AP
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-PA
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-08, ses-03, dir-PA
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, desc-aparc+aseg
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, desc-lesion
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, desc-wmparc
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.967 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-AP
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-PA
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-09, ses-02, dir-PA
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, desc-aparc+aseg
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, desc-wmparc
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.968 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-AP
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-01, dir-PA
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, desc-lesion
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.969 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-AP
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-02, dir-PA
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, desc-lesion
Dec-05 01:57:12.970 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-AP
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-entity-utils] ├─ Processing file with entities: sub-10, ses-03, dir-PA
Dec-05 01:57:12.971 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json
Dec-05 01:57:12.972 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.972 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.972 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:12.972 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.972 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:12.973 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.974 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.975 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.976 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.976 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.978 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, aparc_aseg, dwi]
Dec-05 01:57:12.978 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:12.979 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:12.980 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz
Dec-05 01:57:12.980 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz
Dec-05 01:57:12.981 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.984 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.985 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.985 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.985 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:12.986 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.987 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:12.988 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:12.989 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.989 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.989 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:12.989 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:12.989 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:12.989 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.990 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, dwi, sbref]
Dec-05 01:57:12.990 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:12.990 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:12.990 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz
Dec-05 01:57:12.990 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz
Dec-05 01:57:12.990 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json
Dec-05 01:57:12.991 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:12.991 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.991 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:12.991 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval
Dec-05 01:57:12.991 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:12.991 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.992 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:12.993 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.994 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/fmap/sub-03_ses-02_dir-PA_epi.json
Dec-05 01:57:12.995 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.996 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.996 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-02/fmap/sub-03_ses-02_dir-PA_epi.nii.gz
Dec-05 01:57:12.996 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:12.996 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:12.997 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:12.997 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:12.997 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:12.997 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz
Dec-05 01:57:12.997 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz
Dec-05 01:57:12.997 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:12.998 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval
Dec-05 01:57:12.999 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:12.999 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json
Dec-05 01:57:12.999 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:12.999 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.999 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:12.999 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.000 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.001 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/fmap/sub-03_ses-03_dir-PA_epi.json
Dec-05 01:57:13.002 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-03/ses-03/fmap/sub-03_ses-03_dir-PA_epi.nii.gz
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, aparc_aseg, dwi]
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz
Dec-05 01:57:13.003 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.004 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.005 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.006 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.007 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.008 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.009 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 2 plain sets: [T1w, dwi]
Dec-05 01:57:13.009 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.009 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.009 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz
Dec-05 01:57:13.010 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz
Dec-05 01:57:13.010 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json
Dec-05 01:57:13.010 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.010 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.010 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.011 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.011 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.011 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.011 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval
Dec-05 01:57:13.011 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:13.011 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.012 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.013 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.014 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.015 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.016 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.017 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.019 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.019 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.019 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.019 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz
Dec-05 01:57:13.020 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz
Dec-05 01:57:13.020 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json
Dec-05 01:57:13.020 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.020 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.021 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.021 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:13.023 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json
Dec-05 01:57:13.024 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.024 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.024 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.024 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.024 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.025 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.026 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, dwi, sbref]
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz
Dec-05 01:57:13.027 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.028 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval
Dec-05 01:57:13.029 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:13.029 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json
Dec-05 01:57:13.029 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.030 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.030 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.030 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json
Dec-05 01:57:13.030 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json
Dec-05 01:57:13.030 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.030 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.031 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.032 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.033 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.034 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.034 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.034 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.034 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.035 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, aparc_aseg, dwi]
Dec-05 01:57:13.035 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.035 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.035 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz
Dec-05 01:57:13.035 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz
Dec-05 01:57:13.035 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.036 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.037 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.037 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.037 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json
Dec-05 01:57:13.037 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.037 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.037 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.038 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.039 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.040 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.040 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.040 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.040 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.040 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.040 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.041 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.041 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.041 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.041 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz
Dec-05 01:57:13.041 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz
Dec-05 01:57:13.041 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.042 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.043 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.043 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.043 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.044 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.045 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json
Dec-05 01:57:13.046 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/fmap/sub-05_ses-03_dir-PA_epi.json
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-05/ses-03/fmap/sub-05_ses-03_dir-PA_epi.nii.gz
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.047 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.048 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.048 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.048 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.048 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.049 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json
Dec-05 01:57:13.050 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.051 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/fmap/sub-06_ses-01_dir-PA_epi.json
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-01/fmap/sub-06_ses-01_dir-PA_epi.nii.gz
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 2 plain sets: [T1w, dwi]
Dec-05 01:57:13.052 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.053 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.054 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.055 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.056 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.056 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.056 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.056 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.056 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 2 plain sets: [T1w, dwi]
Dec-05 01:57:13.056 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.057 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.058 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.059 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.060 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.061 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.062 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.062 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.062 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.062 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz
Dec-05 01:57:13.062 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz
Dec-05 01:57:13.062 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json
Dec-05 01:57:13.063 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.063 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:13.063 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:13.063 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.063 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:13.064 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json
Dec-05 01:57:13.065 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.065 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz
Dec-05 01:57:13.065 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz
Dec-05 01:57:13.065 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json
Dec-05 01:57:13.065 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json
Dec-05 01:57:13.066 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.066 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.066 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.066 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.066 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.067 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.068 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.069 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.070 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.071 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.073 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.073 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.073 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.073 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz
Dec-05 01:57:13.074 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz
Dec-05 01:57:13.074 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json
Dec-05 01:57:13.074 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.074 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.074 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.075 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.075 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.075 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.075 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.075 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.076 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.076 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval
Dec-05 01:57:13.076 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:13.076 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json
Dec-05 01:57:13.076 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.076 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.077 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-lesion_mask.nii.gz
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.078 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.079 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, aparc_aseg, dwi]
Dec-05 01:57:13.080 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.081 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.081 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz
Dec-05 01:57:13.081 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz
Dec-05 01:57:13.081 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json
Dec-05 01:57:13.081 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.082 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.083 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json
Dec-05 01:57:13.083 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.083 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.083 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.083 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.084 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.085 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.086 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.087 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.087 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.087 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.087 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz
Dec-05 01:57:13.087 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz
Dec-05 01:57:13.088 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json
Dec-05 01:57:13.088 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.088 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.088 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.088 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.089 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.090 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.091 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.092 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.093 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/fmap/sub-08_ses-03_dir-PA_epi.json
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-08/ses-03/fmap/sub-08_ses-03_dir-PA_epi.nii.gz
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.094 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.095 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.095 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.095 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.095 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz
Dec-05 01:57:13.095 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz
Dec-05 01:57:13.095 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json
Dec-05 01:57:13.096 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.096 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.096 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.096 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.097 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.098 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.099 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.100 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.101 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/fmap/sub-09_ses-02_dir-PA_epi.json
Dec-05 01:57:13.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-09/ses-02/fmap/sub-09_ses-02_dir-PA_epi.nii.gz
Dec-05 01:57:13.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'epi': epi
Dec-05 01:57:13.102 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] No configuration for suffix: epi - FILTERED
Dec-05 01:57:13.103 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, aparc_aseg, dwi]
Dec-05 01:57:13.103 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.103 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.103 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz
Dec-05 01:57:13.103 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz
Dec-05 01:57:13.103 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.104 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.105 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-aparc+aseg_mask.nii.gz
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.106 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.107 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.nii.gz
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.108 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.109 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.110 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 3 plain sets: [T1w, aparc_aseg, dwi]
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 2 suffixes: [T1w, dwi]
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.111 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bval
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.bvec
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-AP_dwi.json
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec
Dec-05 01:57:13.112 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json
Dec-05 01:57:13.113 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json
Dec-05 01:57:13.113 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.113 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.113 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.114 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.115 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.nii.gz
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.116 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.117 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.118 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.118 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.118 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.118 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.119 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.119 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.119 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.119 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz
Dec-05 01:57:13.119 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz
Dec-05 01:57:13.119 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bval
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.bvec
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_dwi.json
Dec-05 01:57:13.120 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval
Dec-05 01:57:13.121 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec
Dec-05 01:57:13.121 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json
Dec-05 01:57:13.121 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.121 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.121 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz
Dec-05 01:57:13.121 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'T1w': T1w
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for T1w
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'T1w' to config key 'T1w'
Dec-05 01:57:13.122 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: T1w, config key: T1w
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: T1w
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'mask': aparc_aseg
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for mask
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [suffix-mapping] Resolving suffix 'mask' to config key 'aparc_aseg'
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: mask, config key: aparc_aseg
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: mask
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.123 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.nii.gz
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.124 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'dwi': dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: dwi, config key: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: dwi
Dec-05 01:57:13.125 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Resolved config key for suffix 'sbref': sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Found configuration for sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Matched file suffix: sbref, config key: sbref
Dec-05 01:57:13.126 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Sequence config missing entities for suffix: sbref
Dec-05 01:57:13.127 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] processGroup called with 4 plain sets: [T1w, aparc_aseg, dwi, sbref]
Dec-05 01:57:13.127 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] allFiles has 3 suffixes: [T1w, dwi, sbref]
Dec-05 01:57:13.127 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: T1w, file suffix: T1w, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz], fileSuffix:T1w]
Dec-05 01:57:13.127 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: T1w, file suffix: T1w, file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz
Dec-05 01:57:13.127 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz
Dec-05 01:57:13.127 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: aparc_aseg, file suffix: mask, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz], fileSuffix:mask]
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: aparc_aseg, file suffix: mask, file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: dwi, file suffix: dwi, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz], fileSuffix:dwi]
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: dwi, file suffix: dwi, file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bval
Dec-05 01:57:13.128 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.bvec
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-AP_dwi.json
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Processing config key: sbref, file suffix: sbref, setData: [files:[file:assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz], fileSuffix:sbref]
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Emitting plain set for config key: sbref, file suffix: sbref, file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set] Building nested data map for primary file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz
Dec-05 01:57:13.129 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-plain_set]   ├─ Checking associated file: assets/tests/dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json
Dec-05 01:57:13.131 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.132 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.133 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.134 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.134 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.134 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:null, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.134 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-01, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json, nii:dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json, dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz, dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz], bidsParentDir:null, subject:01, session:02, run:NA]]
Dec-05 01:57:13.134 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-02, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json, nii:dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz], dwi:[bval:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json, nii:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json, dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:02, session:03, run:NA]]
Dec-05 01:57:13.135 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-03, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json, nii:dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json, dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz, dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:03, session:02, run:NA]]
Dec-05 01:57:13.135 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-03, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json, nii:dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json, dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz, dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz], bidsParentDir:null, subject:03, session:03, run:NA]]
Dec-05 01:57:13.135 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-04, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json, nii:dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz], dwi:[bval:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval, bvec:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec, json:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json, nii:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz]], filePaths:[dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json, dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz], bidsParentDir:null, subject:04, session:01, run:NA]]
Dec-05 01:57:13.135 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-04, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json, nii:dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval, bvec:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec, json:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json, nii:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json, dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz, dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:04, session:02, run:NA]]
Dec-05 01:57:13.135 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-04, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json, nii:dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz], dwi:[bval:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval, bvec:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec, json:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json, nii:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json, nii:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz]], filePaths:[dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json, dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz], bidsParentDir:null, subject:04, session:03, run:NA]]
Dec-05 01:57:13.136 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-05, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json, nii:dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval, bvec:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec, json:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json, nii:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json, dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz, dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz], bidsParentDir:null, subject:05, session:01, run:NA]]
Dec-05 01:57:13.136 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-05, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json, nii:dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json, dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz, dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:05, session:02, run:NA]]
Dec-05 01:57:13.136 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-05, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json, nii:dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json, nii:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json, dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz, dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:05, session:03, run:NA]]
Dec-05 01:57:13.136 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-06, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json, nii:dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz], dwi:[bval:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval, bvec:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec, json:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json, nii:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json, dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz], bidsParentDir:null, subject:06, session:01, run:NA]]
Dec-05 01:57:13.136 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-06, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json, nii:dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz], dwi:[bval:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval, bvec:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec, json:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json, nii:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz]], filePaths:[dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json, dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz], bidsParentDir:null, subject:06, session:02, run:NA]]
Dec-05 01:57:13.136 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-07, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json, nii:dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval, bvec:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec, json:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json, nii:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json, nii:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json, dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz, dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:07, session:01, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-07, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json, nii:dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval, bvec:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec, json:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json, nii:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json, nii:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz]], filePaths:[dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json, dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz, dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz], bidsParentDir:null, subject:07, session:03, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-08, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json, nii:dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval, bvec:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec, json:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json, nii:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json, dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz, dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz], bidsParentDir:null, subject:08, session:01, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-08, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json, nii:dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json, dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz, dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:08, session:02, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-08, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json, nii:dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json, nii:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json, dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz, dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:08, session:03, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-09, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json, nii:dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json, dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz, dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz], bidsParentDir:null, subject:09, session:02, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-10, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json, nii:dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval, bvec:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec, json:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json, nii:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz]], filePaths:[dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json, dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz, dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz], bidsParentDir:null, subject:10, session:01, run:NA]]
Dec-05 01:57:13.137 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-10, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json, nii:dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval, bvec:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec, json:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json, nii:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json, dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz, dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:null, subject:10, session:02, run:NA]]
Dec-05 01:57:13.138 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Unifying item: [[sub-10, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json, nii:dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval, bvec:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec, json:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json, nii:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json, nii:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz]], filePaths:[dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json, dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz, dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz], bidsParentDir:null, subject:10, session:03, run:NA]]
Dec-05 01:57:13.142 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[NA, NA, NA], [data:[:], filePaths:[], bidsParentDir:assets/tests, subject:NA, session:NA, run:NA]]
Dec-05 01:57:13.144 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-01, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json, nii:dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.json, dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_T1w.nii.gz, dummy_bids/sub-01/ses-02/anat/sub-01_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bval, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.json, dummy_bids/sub-01/ses-02/dwi/sub-01_ses-02_dir-AP_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-01, session:ses-02, run:NA]]
Dec-05 01:57:13.144 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-02, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json, nii:dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz], dwi:[bval:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json, nii:dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.json, dummy_bids/sub-02/ses-03/anat/sub-02_ses-03_T1w.nii.gz, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bval, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.json, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_dwi.nii.gz, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.json, dummy_bids/sub-02/ses-03/dwi/sub-02_ses-03_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-02, session:ses-03, run:NA]]
Dec-05 01:57:13.145 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-03, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json, nii:dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.json, dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_T1w.nii.gz, dummy_bids/sub-03/ses-02/anat/sub-03_ses-02_desc-lesion_mask.nii.gz, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bval, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.json, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_dwi.nii.gz, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.json, dummy_bids/sub-03/ses-02/dwi/sub-03_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-03, session:ses-02, run:NA]]
Dec-05 01:57:13.145 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-03, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json, nii:dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.json, dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_T1w.nii.gz, dummy_bids/sub-03/ses-03/anat/sub-03_ses-03_desc-wmparc_mask.nii.gz, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bval, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.json, dummy_bids/sub-03/ses-03/dwi/sub-03_ses-03_dir-AP_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-03, session:ses-03, run:NA]]
Dec-05 01:57:13.145 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-04, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json, nii:dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz], dwi:[bval:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval, bvec:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec, json:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json, nii:dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz]], filePaths:[dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.json, dummy_bids/sub-04/ses-01/anat/sub-04_ses-01_T1w.nii.gz, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bval, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.bvec, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.json, dummy_bids/sub-04/ses-01/dwi/sub-04_ses-01_dir-PA_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-04, session:ses-01, run:NA]]
Dec-05 01:57:13.145 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-04, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json, nii:dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval, bvec:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec, json:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json, nii:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.json, dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_T1w.nii.gz, dummy_bids/sub-04/ses-02/anat/sub-04_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bval, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.bvec, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.json, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-PA_dwi.nii.gz, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.json, dummy_bids/sub-04/ses-02/dwi/sub-04_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-04, session:ses-02, run:NA]]
Dec-05 01:57:13.146 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-04, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json, nii:dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz], dwi:[bval:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval, bvec:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec, json:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json, nii:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json, nii:dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz]], filePaths:[dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.json, dummy_bids/sub-04/ses-03/anat/sub-04_ses-03_T1w.nii.gz, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bval, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.bvec, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.json, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_dwi.nii.gz, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.json, dummy_bids/sub-04/ses-03/dwi/sub-04_ses-03_dir-PA_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-04, session:ses-03, run:NA]]
Dec-05 01:57:13.146 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-05, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json, nii:dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval, bvec:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec, json:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json, nii:dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.json, dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_T1w.nii.gz, dummy_bids/sub-05/ses-01/anat/sub-05_ses-01_desc-wmparc_mask.nii.gz, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bval, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.bvec, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.json, dummy_bids/sub-05/ses-01/dwi/sub-05_ses-01_dir-AP_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-05, session:ses-01, run:NA]]
Dec-05 01:57:13.146 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-05, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json, nii:dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.json, dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_T1w.nii.gz, dummy_bids/sub-05/ses-02/anat/sub-05_ses-02_desc-lesion_mask.nii.gz, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bval, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.json, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_dwi.nii.gz, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.json, dummy_bids/sub-05/ses-02/dwi/sub-05_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-05, session:ses-02, run:NA]]
Dec-05 01:57:13.146 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-05, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json, nii:dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json, nii:dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.json, dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_T1w.nii.gz, dummy_bids/sub-05/ses-03/anat/sub-05_ses-03_desc-lesion_mask.nii.gz, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bval, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.json, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_dwi.nii.gz, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.json, dummy_bids/sub-05/ses-03/dwi/sub-05_ses-03_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-05, session:ses-03, run:NA]]
Dec-05 01:57:13.146 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-06, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json, nii:dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz], dwi:[bval:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval, bvec:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec, json:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json, nii:dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.json, dummy_bids/sub-06/ses-01/anat/sub-06_ses-01_T1w.nii.gz, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bval, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.bvec, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.json, dummy_bids/sub-06/ses-01/dwi/sub-06_ses-01_dir-AP_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-06, session:ses-01, run:NA]]
Dec-05 01:57:13.147 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-06, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json, nii:dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz], dwi:[bval:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval, bvec:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec, json:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json, nii:dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz]], filePaths:[dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.json, dummy_bids/sub-06/ses-02/anat/sub-06_ses-02_T1w.nii.gz, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bval, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.bvec, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.json, dummy_bids/sub-06/ses-02/dwi/sub-06_ses-02_dir-PA_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-06, session:ses-02, run:NA]]
Dec-05 01:57:13.147 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-07, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json, nii:dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval, bvec:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec, json:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json, nii:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json, nii:dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.json, dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_T1w.nii.gz, dummy_bids/sub-07/ses-01/anat/sub-07_ses-01_desc-lesion_mask.nii.gz, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bval, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.bvec, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.json, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-PA_dwi.nii.gz, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.json, dummy_bids/sub-07/ses-01/dwi/sub-07_ses-01_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-07, session:ses-01, run:NA]]
Dec-05 01:57:13.147 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-07, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json, nii:dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval, bvec:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec, json:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json, nii:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json, nii:dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz]], filePaths:[dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.json, dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_T1w.nii.gz, dummy_bids/sub-07/ses-03/anat/sub-07_ses-03_desc-wmparc_mask.nii.gz, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bval, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.bvec, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.json, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_dwi.nii.gz, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.json, dummy_bids/sub-07/ses-03/dwi/sub-07_ses-03_dir-PA_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-07, session:ses-03, run:NA]]
Dec-05 01:57:13.147 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-08, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json, nii:dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval, bvec:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec, json:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json, nii:dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.json, dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_T1w.nii.gz, dummy_bids/sub-08/ses-01/anat/sub-08_ses-01_desc-wmparc_mask.nii.gz, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bval, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.bvec, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.json, dummy_bids/sub-08/ses-01/dwi/sub-08_ses-01_dir-AP_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-08, session:ses-01, run:NA]]
Dec-05 01:57:13.147 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-08, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json, nii:dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.json, dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_T1w.nii.gz, dummy_bids/sub-08/ses-02/anat/sub-08_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bval, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.json, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_dwi.nii.gz, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.json, dummy_bids/sub-08/ses-02/dwi/sub-08_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-08, session:ses-02, run:NA]]
Dec-05 01:57:13.147 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-08, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json, nii:dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval, bvec:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec, json:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json, nii:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz], sbref:[json:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json, nii:dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.json, dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_T1w.nii.gz, dummy_bids/sub-08/ses-03/anat/sub-08_ses-03_desc-wmparc_mask.nii.gz, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bval, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.bvec, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.json, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_dwi.nii.gz, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.json, dummy_bids/sub-08/ses-03/dwi/sub-08_ses-03_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-08, session:ses-03, run:NA]]
Dec-05 01:57:13.148 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-09, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json, nii:dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval, bvec:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec, json:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json, nii:dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz]], filePaths:[dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.json, dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_T1w.nii.gz, dummy_bids/sub-09/ses-02/anat/sub-09_ses-02_desc-wmparc_mask.nii.gz, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bval, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.bvec, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.json, dummy_bids/sub-09/ses-02/dwi/sub-09_ses-02_dir-AP_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-09, session:ses-02, run:NA]]
Dec-05 01:57:13.148 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-10, ses-01, NA], [data:[T1w:[json:dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json, nii:dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz], dwi:[bval:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval, bvec:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec, json:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json, nii:dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz]], filePaths:[dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.json, dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_T1w.nii.gz, dummy_bids/sub-10/ses-01/anat/sub-10_ses-01_desc-wmparc_mask.nii.gz, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bval, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.bvec, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.json, dummy_bids/sub-10/ses-01/dwi/sub-10_ses-01_dir-PA_dwi.nii.gz], bidsParentDir:assets/tests, subject:sub-10, session:ses-01, run:NA]]
Dec-05 01:57:13.148 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-10, ses-02, NA], [data:[T1w:[json:dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json, nii:dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval, bvec:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec, json:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json, nii:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json, nii:dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz]], filePaths:[dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.json, dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_T1w.nii.gz, dummy_bids/sub-10/ses-02/anat/sub-10_ses-02_desc-lesion_mask.nii.gz, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bval, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.bvec, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.json, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-PA_dwi.nii.gz, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.json, dummy_bids/sub-10/ses-02/dwi/sub-10_ses-02_dir-AP_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-10, session:ses-02, run:NA]]
Dec-05 01:57:13.149 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Processing item for cross-modal broadcasting: [[sub-10, ses-03, NA], [data:[T1w:[json:dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json, nii:dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz], aparc_aseg:[nii:dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz], dwi:[bval:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval, bvec:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec, json:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json, nii:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz], sbref:[json:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json, nii:dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz]], filePaths:[dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.json, dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_T1w.nii.gz, dummy_bids/sub-10/ses-03/anat/sub-10_ses-03_desc-lesion_mask.nii.gz, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bval, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.bvec, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.json, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_dwi.nii.gz, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.json, dummy_bids/sub-10/ses-03/dwi/sub-10_ses-03_dir-PA_sbref.nii.gz], bidsParentDir:assets/tests, subject:sub-10, session:ses-03, run:NA]]
Dec-05 01:57:13.150 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key:  with 1 entries
Dec-05 01:57:13.151 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key:  with available data:  | 
Dec-05 01:57:13.151 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [NA, NA, NA]
Dec-05 01:57:13.152 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-01_ses-02 with 1 entries
Dec-05 01:57:13.152 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-01_ses-02 with available data:  | 
Dec-05 01:57:13.153 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-01, ses-02, NA]
Dec-05 01:57:13.154 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-01, ses-02, NA] with data: T1w, aparc_aseg, dwi
Dec-05 01:57:13.154 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-02_ses-03 with 1 entries
Dec-05 01:57:13.154 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-02_ses-03 with available data:  | 
Dec-05 01:57:13.154 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-02, ses-03, NA]
Dec-05 01:57:13.154 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-02, ses-03, NA] with data: T1w, dwi, sbref
Dec-05 01:57:13.154 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-03_ses-02 with 1 entries
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-03_ses-02 with available data:  | 
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-03, ses-02, NA]
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-03, ses-02, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-03_ses-03 with 1 entries
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-03_ses-03 with available data:  | 
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-03, ses-03, NA]
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-03, ses-03, NA] with data: T1w, aparc_aseg, dwi
Dec-05 01:57:13.155 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-04_ses-01 with 1 entries
Dec-05 01:57:13.156 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-04_ses-01 with available data:  | 
Dec-05 01:57:13.156 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-04, ses-01, NA]
Dec-05 01:57:13.156 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-04, ses-01, NA] with data: T1w, dwi
Dec-05 01:57:13.156 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-04_ses-02 with 1 entries
Dec-05 01:57:13.156 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-04_ses-02 with available data:  | 
Dec-05 01:57:13.156 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-04, ses-02, NA]
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-04, ses-02, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-04_ses-03 with 1 entries
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-04_ses-03 with available data:  | 
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-04, ses-03, NA]
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-04, ses-03, NA] with data: T1w, dwi, sbref
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-05_ses-01 with 1 entries
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-05_ses-01 with available data:  | 
Dec-05 01:57:13.157 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-05, ses-01, NA]
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-05, ses-01, NA] with data: T1w, aparc_aseg, dwi
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-05_ses-02 with 1 entries
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-05_ses-02 with available data:  | 
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-05, ses-02, NA]
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-05, ses-02, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-05_ses-03 with 1 entries
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-05_ses-03 with available data:  | 
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-05, ses-03, NA]
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-05, ses-03, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.158 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-06_ses-01 with 1 entries
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-06_ses-01 with available data:  | 
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-06, ses-01, NA]
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-06, ses-01, NA] with data: T1w, dwi
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-06_ses-02 with 1 entries
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-06_ses-02 with available data:  | 
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-06, ses-02, NA]
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-06, ses-02, NA] with data: T1w, dwi
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-07_ses-01 with 1 entries
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-07_ses-01 with available data:  | 
Dec-05 01:57:13.159 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-07, ses-01, NA]
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-07, ses-01, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-07_ses-03 with 1 entries
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-07_ses-03 with available data:  | 
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-07, ses-03, NA]
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-07, ses-03, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-08_ses-01 with 1 entries
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-08_ses-01 with available data:  | 
Dec-05 01:57:13.160 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-08, ses-01, NA]
Dec-05 01:57:13.161 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-08, ses-01, NA] with data: T1w, aparc_aseg, dwi
Dec-05 01:57:13.161 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-08_ses-02 with 1 entries
Dec-05 01:57:13.161 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-08_ses-02 with available data:  | 
Dec-05 01:57:13.161 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-08, ses-02, NA]
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-08, ses-02, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-08_ses-03 with 1 entries
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-08_ses-03 with available data:  | 
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-08, ses-03, NA]
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-08, ses-03, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-09_ses-02 with 1 entries
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-09_ses-02 with available data:  | 
Dec-05 01:57:13.162 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-09, ses-02, NA]
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-09, ses-02, NA] with data: T1w, aparc_aseg, dwi
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-10_ses-01 with 1 entries
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-10_ses-01 with available data:  | 
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-10, ses-01, NA]
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-10, ses-01, NA] with data: T1w, aparc_aseg, dwi
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-10_ses-02 with 1 entries
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-10_ses-02 with available data:  | 
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-10, ses-02, NA]
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-10, ses-02, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.163 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying broadcasting for looping key: sub-10_ses-03 with 1 entries
Dec-05 01:57:13.164 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Applying cross-modal broadcasting for key: sub-10_ses-03 with available data:  | 
Dec-05 01:57:13.164 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Enhancing entry for grouping key: [sub-10, ses-03, NA]
Dec-05 01:57:13.164 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ Broadcasting enhanced data for key: [sub-10, ses-03, NA] with data: T1w, aparc_aseg, dwi, sbref
Dec-05 01:57:13.175 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] ├─ ✅ SUCCESS
Dec-05 01:57:13.176 [ForkJoinPool.commonPool-worker-1] INFO  nfneuro.plugin.util.BidsLogger - [nf-bids-handler] └─ nf-bids job complete: 21 data groups processed
Dec-05 01:57:13.401 [Actor Thread 28] INFO  nextflow.processor.TaskProcessor - [0e/5462af] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-04_ses-01)
Dec-05 01:57:13.402 [Actor Thread 21] INFO  nextflow.processor.TaskProcessor - [ac/d07007] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-01_ses-02)
Dec-05 01:57:13.402 [Actor Thread 16] INFO  nextflow.processor.TaskProcessor - [e0/aea975] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-05_ses-01)
Dec-05 01:57:13.402 [Actor Thread 19] INFO  nextflow.processor.TaskProcessor - [6e/e0d436] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-01_ses-02)
Dec-05 01:57:13.402 [Actor Thread 25] INFO  nextflow.processor.TaskProcessor - [5d/dcc2a4] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-04_ses-03)
Dec-05 01:57:13.401 [Actor Thread 24] INFO  nextflow.processor.TaskProcessor - [f6/b21e66] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-03_ses-03)
Dec-05 01:57:13.402 [Actor Thread 11] INFO  nextflow.processor.TaskProcessor - [9a/fda164] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-04_ses-01)
Dec-05 01:57:13.402 [Actor Thread 30] INFO  nextflow.processor.TaskProcessor - [97/264bab] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-03_ses-02)
Dec-05 01:57:13.402 [Actor Thread 23] INFO  nextflow.processor.TaskProcessor - [56/fbce7b] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-04_ses-02)
Dec-05 01:57:13.402 [Actor Thread 4] INFO  nextflow.processor.TaskProcessor - [7e/ce7b4f] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-02_ses-03)
Dec-05 01:57:13.402 [Actor Thread 17] INFO  nextflow.processor.TaskProcessor - [40/66d745] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-05_ses-01)
Dec-05 01:57:13.402 [Actor Thread 9] INFO  nextflow.processor.TaskProcessor - [c8/69cfe6] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-02_ses-03)
Dec-05 01:57:13.402 [Actor Thread 10] INFO  nextflow.processor.TaskProcessor - [97/00d771] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-03_ses-03)
Dec-05 01:57:13.402 [Actor Thread 18] INFO  nextflow.processor.TaskProcessor - [d7/cbe3da] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-04_ses-03)
Dec-05 01:57:13.401 [Actor Thread 6] INFO  nextflow.processor.TaskProcessor - [37/249746] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-04_ses-02)
Dec-05 01:57:13.402 [Actor Thread 27] INFO  nextflow.processor.TaskProcessor - [e3/107ca4] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-03_ses-02)
Dec-05 01:57:13.482 [Actor Thread 29] INFO  nextflow.processor.TaskProcessor - [2e/bf388c] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-06_ses-02)
Dec-05 01:57:13.484 [Actor Thread 12] INFO  nextflow.processor.TaskProcessor - [a3/47b8c9] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-05_ses-02)
Dec-05 01:57:13.485 [Actor Thread 19] INFO  nextflow.processor.TaskProcessor - [a4/d70f9b] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-08_ses-01)
Dec-05 01:57:13.485 [Actor Thread 13] INFO  nextflow.processor.TaskProcessor - [2c/74b363] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-07_ses-03)
Dec-05 01:57:13.486 [Actor Thread 28] INFO  nextflow.processor.TaskProcessor - [2e/f15980] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-07_ses-01)
Dec-05 01:57:13.486 [Actor Thread 16] INFO  nextflow.processor.TaskProcessor - [d2/2831ce] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-06_ses-01)
Dec-05 01:57:13.487 [Actor Thread 31] INFO  nextflow.processor.TaskProcessor - [b9/64519c] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-08_ses-02)
Dec-05 01:57:13.496 [Actor Thread 14] INFO  nextflow.processor.TaskProcessor - [65/e546ce] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-05_ses-03)
Dec-05 01:57:13.511 [Actor Thread 13] INFO  nextflow.processor.TaskProcessor - [38/416562] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-09_ses-02)
Dec-05 01:57:13.511 [Actor Thread 2] INFO  nextflow.processor.TaskProcessor - [b5/56acfd] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-10_ses-01)
Dec-05 01:57:13.511 [Actor Thread 27] INFO  nextflow.processor.TaskProcessor - [6d/dd1015] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-10_ses-02)
Dec-05 01:57:13.517 [Actor Thread 16] INFO  nextflow.processor.TaskProcessor - [48/b9d2c4] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-08_ses-03)
Dec-05 01:57:13.517 [Actor Thread 31] INFO  nextflow.processor.TaskProcessor - [40/89cdec] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:DENOISE_DWI (sub-10_ses-03)
Dec-05 01:57:13.626 [Actor Thread 10] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.631 [Actor Thread 11] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.635 [Actor Thread 21] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.640 [Actor Thread 4] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.646 [Actor Thread 30] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.649 [Actor Thread 29] INFO  nextflow.processor.TaskProcessor - [5b/e527e0] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-05_ses-02)
Dec-05 01:57:13.649 [Actor Thread 17] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.659 [Actor Thread 25] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.662 [Actor Thread 11] INFO  nextflow.processor.TaskProcessor - [4f/8fee7a] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-06_ses-01)
Dec-05 01:57:13.663 [Actor Thread 29] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.664 [Actor Thread 6] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.665 [Actor Thread 18] INFO  nextflow.processor.TaskProcessor - [f8/7aa86f] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-05_ses-03)
Dec-05 01:57:13.671 [Actor Thread 11] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.674 [Actor Thread 18] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.679 [Actor Thread 10] INFO  nextflow.processor.TaskProcessor - [17/2aad83] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-06_ses-02)
Dec-05 01:57:13.684 [Actor Thread 10] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.685 [Actor Thread 12] INFO  nextflow.processor.TaskProcessor - [1a/993c50] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-08_ses-02)
Dec-05 01:57:13.686 [Actor Thread 17] INFO  nextflow.processor.TaskProcessor - [ad/fb37e2] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-07_ses-03)
Dec-05 01:57:13.688 [Actor Thread 14] INFO  nextflow.processor.TaskProcessor - [de/ad6574] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-07_ses-01)
Dec-05 01:57:13.690 [Actor Thread 6] INFO  nextflow.processor.TaskProcessor - [1d/6257f5] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-08_ses-03)
Dec-05 01:57:13.693 [Actor Thread 6] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.694 [Actor Thread 17] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.698 [Actor Thread 14] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.700 [Actor Thread 12] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.702 [Actor Thread 4] INFO  nextflow.processor.TaskProcessor - [96/9378e6] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-08_ses-01)
Dec-05 01:57:13.706 [Actor Thread 28] INFO  nextflow.processor.TaskProcessor - [62/50fa72] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-10_ses-02)
Dec-05 01:57:13.710 [Actor Thread 18] INFO  nextflow.processor.TaskProcessor - [51/368fbc] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-09_ses-02)
Dec-05 01:57:13.712 [Actor Thread 11] INFO  nextflow.processor.TaskProcessor - [8e/7c82ab] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-10_ses-01)
Dec-05 01:57:13.712 [Actor Thread 6] INFO  nextflow.processor.TaskProcessor - [83/7a78ae] Cached process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS (sub-10_ses-03)
Dec-05 01:57:13.721 [Actor Thread 4] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.722 [Task submitter] DEBUG n.executor.local.LocalTaskHandler - Launch cmd line: /bin/bash -ue .command.run
Dec-05 01:57:13.724 [Task submitter] INFO  nextflow.Session - [85/be2b5e] Submitted process > SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:PREPROC_EDDY (sub-02_ses-03)
Dec-05 01:57:13.724 [Actor Thread 28] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.726 [Actor Thread 6] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.728 [Actor Thread 11] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:13.729 [Actor Thread 18] DEBUG nextflow.processor.TaskProcessor - Process SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:DENOISING_NLMEANS > Skipping output binding because one or more optional files are missing: fileoutparam<1:1>
Dec-05 01:57:23.959 [SIGINT handler] DEBUG nextflow.Session - Session aborted -- Cause: SIGINT
Dec-05 01:57:24.018 [SIGINT handler] DEBUG nextflow.Session - The following nodes are still active:
[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:TOPUP_EDDY:UTILS_EXTRACTB0
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:BETCROP_FSLBETCROP
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:N4_DWI
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:NORMALIZE_DWI
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_DWI
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:EXTRACTB0_RESAMPLE
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_DWI:RESAMPLE_MASK
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_RESAMPLE
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:BETCROP_ANTSBET
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_T1
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:PREPROC_T1:IMAGE_CROPVOLUME_MASK
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_DTIMETRICS
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANATTODWI
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:T1_REGISTRATION:REGISTRATION_ANTS
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_WMPARC
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_APARC_ASEG
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRANSFORM_LESION_MASK
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FASTSEG
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:ANATOMICAL_SEGMENTATION:SEGMENTATION_FREESURFERSEG
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FRF
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:RECONST_FODF
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_PFTTRACKING
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:TRACTOFLOW:TRACKING_LOCALTRACKING
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:ENSEMBLE_TRACKING
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:QC_ENSEMBLE
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (value) OPEN  ; channel: multiqc_files
  port 2: (value) bound ; channel: multiqc_config
  port 3: (value) bound ; channel: extra_multiqc_config
  port 4: (value) bound ; channel: multiqc_logo
  port 5: (value) bound ; channel: replace_names
  port 6: (value) bound ; channel: sample_names
  port 7: (cntrl) -     ; channel: $

[process] SCILUS_SF_TRACTOMICS:SF_TRACTOMICS:MULTIQC_GLOBAL
  status=ACTIVE
  port 0: (queue) OPEN  ; channel: -
  port 1: (value) OPEN  ; channel: multiqc_files
  port 2: (value) bound ; channel: multiqc_config
  port 3: (value) bound ; channel: extra_multiqc_config
  port 4: (value) bound ; channel: multiqc_logo
  port 5: (value) bound ; channel: replace_names
  port 6: (value) bound ; channel: sample_names
  port 7: (cntrl) -     ; channel: $

Dec-05 01:57:24.025 [SIGINT handler] INFO  nextflow.Nextflow - -[0;35m[scilus/sf-tractomics][0;31m Pipeline completed with errors[0m-
Dec-05 01:57:24.028 [Task monitor] DEBUG n.processor.TaskPollingMonitor - <<< barrier arrives (monitor: local) - terminating tasks monitor poll loop
Dec-05 01:57:24.029 [SIGINT handler] WARN  n.processor.TaskPollingMonitor - Killing running tasks (1)
Dec-05 01:57:24.048 [SIGINT handler] DEBUG n.trace.WorkflowStatsObserver - Workflow completed > WorkflowStats[succeededCount=0; failedCount=0; ignoredCount=0; cachedCount=42; pendingCount=41; submittedCount=0; runningCount=0; retriesCount=0; abortedCount=1; succeedDuration=0ms; failedDuration=0ms; cachedDuration=32m 26s;loadCpus=0; loadMemory=0; peakRunning=1; peakCpus=8; peakMemory=12 GB; ]
Dec-05 01:57:24.828 [SIGINT handler] DEBUG nextflow.trace.ReportObserver - Workflow completed -- rendering execution report
Dec-05 01:57:25.487 [SIGINT handler] DEBUG nextflow.trace.TimelineObserver - Workflow completed -- rendering execution timeline
Dec-05 01:57:25.556 [SIGINT handler] DEBUG nextflow.trace.TraceFileObserver - Workflow completed -- saving trace file
Dec-05 01:57:25.561 [SIGINT handler] ERROR nextflow.Nextflow - Pipeline failed. Please refer to troubleshooting docs: https://nf-co.re/docs/usage/troubleshooting
Dec-05 01:57:25.565 [main] DEBUG nextflow.Session - Session await > all processes finished
Dec-05 01:57:25.565 [main] DEBUG nextflow.Session - Session await > all barriers passed
Dec-05 01:57:25.566 [main] DEBUG n.trace.WorkflowStatsObserver - Workflow completed > WorkflowStats[succeededCount=0; failedCount=0; ignoredCount=0; cachedCount=42; pendingCount=41; submittedCount=0; runningCount=0; retriesCount=0; abortedCount=1; succeedDuration=0ms; failedDuration=0ms; cachedDuration=32m 26s;loadCpus=0; loadMemory=0; peakRunning=1; peakCpus=8; peakMemory=12 GB; ]
Dec-05 01:57:25.579 [Actor Thread 17] DEBUG nextflow.sort.BigSort - Sort completed -- entries: 3; slices: 1; internal sort time: 0.002 s; external sort time: 0.002 s; total time: 0.004 s
Dec-05 01:57:25.580 [Actor Thread 17] DEBUG nextflow.file.FileCollector - >> temp file exists? true
Dec-05 01:57:25.583 [Actor Thread 17] DEBUG nextflow.file.FileCollector - Retrieved cached collect-files from: /workspaces/nf-tractoflow/work/collect-file/c5cab245eae837a5d52b12db301183cc
Dec-05 01:57:25.585 [Actor Thread 17] DEBUG nextflow.file.FileCollector - Deleting file collector temp dir: /tmp/nxf-1362122337480675885
Dec-05 01:57:25.638 [main] DEBUG nextflow.trace.ReportObserver - Workflow completed -- rendering execution report
Dec-05 01:57:26.200 [main] WARN  nextflow.trace.ReportObserver - Failed to render execution report -- see the log file for details
nextflow.exception.AbortOperationException: Report file already exists: results/pipeline_info/execution_report_2025-12-05_01-56-56.html -- enable the 'report.overwrite' option in your config file to overwrite existing files
	at nextflow.trace.TraceHelper.newFileWriter(TraceHelper.groovy:67)
	at nextflow.trace.ReportObserver.renderHtml(ReportObserver.groovy:243)
	at nextflow.trace.ReportObserver.onFlowComplete(ReportObserver.groovy:129)
	at nextflow.Session$_notifyFlowComplete_lambda39.doCall(Session.groovy:1121)
	at nextflow.Session.notifyEvent(Session.groovy:1151)
	at nextflow.Session.notifyFlowComplete(Session.groovy:1121)
	at nextflow.Session.shutdown0(Session.groovy:779)
	at nextflow.Session.destroy(Session.groovy:724)
	at nextflow.script.ScriptRunner.shutdown(ScriptRunner.groovy:263)
	at nextflow.script.ScriptRunner.execute(ScriptRunner.groovy:147)
	at nextflow.cli.CmdRun.run(CmdRun.groovy:428)
	at nextflow.cli.Launcher.run(Launcher.groovy:515)
	at nextflow.cli.Launcher.main(Launcher.groovy:675)
Caused by: java.nio.file.FileAlreadyExistsException: results/pipeline_info/execution_report_2025-12-05_01-56-56.html
	at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:94)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:106)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:111)
	at java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(UnixFileSystemProvider.java:218)
	at java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(FileSystemProvider.java:484)
	at java.base/java.nio.file.Files.newOutputStream(Files.java:228)
	at java.base/java.nio.file.Files.newBufferedWriter(Files.java:3008)
	at nextflow.trace.TraceHelper.newFileWriter(TraceHelper.groovy:64)
	... 12 common frames omitted
Dec-05 01:57:26.203 [main] DEBUG nextflow.trace.TimelineObserver - Workflow completed -- rendering execution timeline
Dec-05 01:57:26.252 [main] WARN  nextflow.trace.TimelineObserver - Failed to render execution timeline -- see the log file for details
nextflow.exception.AbortOperationException: Timeline file already exists: results/pipeline_info/execution_timeline_2025-12-05_01-56-56.html -- enable the 'timeline.overwrite' option in your config file to overwrite existing files
	at nextflow.trace.TraceHelper.newFileWriter(TraceHelper.groovy:67)
	at nextflow.trace.TimelineObserver.renderHtml(TimelineObserver.groovy:164)
	at nextflow.trace.TimelineObserver.onFlowComplete(TimelineObserver.groovy:91)
	at nextflow.Session$_notifyFlowComplete_lambda39.doCall(Session.groovy:1121)
	at nextflow.Session.notifyEvent(Session.groovy:1151)
	at nextflow.Session.notifyFlowComplete(Session.groovy:1121)
	at nextflow.Session.shutdown0(Session.groovy:779)
	at nextflow.Session.destroy(Session.groovy:724)
	at nextflow.script.ScriptRunner.shutdown(ScriptRunner.groovy:263)
	at nextflow.script.ScriptRunner.execute(ScriptRunner.groovy:147)
	at nextflow.cli.CmdRun.run(CmdRun.groovy:428)
	at nextflow.cli.Launcher.run(Launcher.groovy:515)
	at nextflow.cli.Launcher.main(Launcher.groovy:675)
Caused by: java.nio.file.FileAlreadyExistsException: results/pipeline_info/execution_timeline_2025-12-05_01-56-56.html
	at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:94)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:106)
	at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:111)
	at java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(UnixFileSystemProvider.java:218)
	at java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(FileSystemProvider.java:484)
	at java.base/java.nio.file.Files.newOutputStream(Files.java:228)
	at java.base/java.nio.file.Files.newBufferedWriter(Files.java:3008)
	at nextflow.trace.TraceHelper.newFileWriter(TraceHelper.groovy:64)
	... 12 common frames omitted
Dec-05 01:57:26.253 [main] DEBUG nextflow.trace.TraceFileObserver - Workflow completed -- saving trace file
Dec-05 01:57:26.260 [main] DEBUG nextflow.cache.CacheDB - Closing CacheDB done
Dec-05 01:57:26.307 [main] INFO  org.pf4j.AbstractPluginManager - Stop plugin 'nf-bids@0.1.0-beta.8'
Dec-05 01:57:26.308 [main] DEBUG nextflow.plugin.BasePlugin - Plugin stopped nf-bids
Dec-05 01:57:26.308 [main] INFO  org.pf4j.AbstractPluginManager - Stop plugin 'nf-schema@2.3.0'
Dec-05 01:57:26.308 [main] DEBUG nextflow.plugin.BasePlugin - Plugin stopped nf-schema
Dec-05 01:57:26.313 [main] DEBUG nextflow.script.ScriptRunner - > Execution complete -- Goodbye
```