/* groovylint-disable all */
package nfneuro.plugin.channel

import groovyx.gpars.dataflow.DataflowQueue
import nextflow.extension.CH
import spock.lang.Specification

/**
 * Unit tests for the BidsHandler flattening transformer
 */
class BidsHandlerFlattenSpec extends Specification {

    def 'should flatten plain set paths to absolute Files and build meta'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']

        def groupingKey = ['sub-01','ses-01']
        def enrichedData = [
            data: [
                T1w: [
                    nii: 'anat/sub-01_T1w.nii.gz',
                    json: 'anat/sub-01_T1w.json'
                ]
            ],
            bidsParentDir: '/data/bids'
        ]

        def entityValues = [subject: 'sub-01', session: 'ses-01']
        def tuple = [groupingKey, enrichedData]

        when:
        def method = handler.getClass().getDeclaredMethod('flattenTupleToMap', List, Map)
        method.setAccessible(true)
        def flat = method.invoke(handler, tuple, entityValues)

        then:
        flat instanceof Map
        flat.meta.subject == 'sub-01'
        flat.meta.session == 'ses-01'
        flat.T1w instanceof Map
        flat.T1w.nii instanceof java.nio.file.Path
        flat.T1w.nii.isAbsolute()
        flat.T1w.json instanceof java.nio.file.Path
        flat.T1w.json.isAbsolute()
    }

    def 'should flatten named sets preserving nested maps'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']

        def groupingKey = ['sub-01','ses-01']
        def enrichedData = [
            data: [
                dwi: [
                    ap: [
                        nii: 'dwi/sub-01_dir-AP_dwi.nii.gz',
                        bval: 'dwi/sub-01_dir-AP_dwi.bval',
                        bvec: 'dwi/sub-01_dir-AP_dwi.bvec'
                    ]
                ]
            ],
            bidsParentDir: '/data/bids'
        ]

        def entityValues = [subject: 'sub-01', session: 'ses-01']
        def tuple = [groupingKey, enrichedData]

        when:
        def method = handler.getClass().getDeclaredMethod('flattenTupleToMap', List, Map)
        method.setAccessible(true)
        def flat = method.invoke(handler, tuple, entityValues)

        then:
        flat.dwi instanceof Map
        flat.dwi.ap.nii instanceof java.nio.file.Path
        flat.dwi.ap.bval instanceof java.nio.file.Path
        flat.dwi.ap.bvec instanceof java.nio.file.Path
        flat.dwi.ap.nii.isAbsolute()
    }

    def 'should convert lists of paths to lists of Files'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']

        def groupingKey = ['sub-01','ses-01']
        def enrichedData = [
            data: [
                bold: [
                    nii: ['func/sub-01_run-01_bold.nii.gz', 'func/sub-01_run-02_bold.nii.gz'],
                    json: ['func/sub-01_run-01_bold.json', 'func/sub-01_run-02_bold.json']
                ]
            ],
            bidsParentDir: '/data/bids'
        ]

        def entityValues = [subject: 'sub-01', session: 'ses-01']
        def tuple = [groupingKey, enrichedData]

        when:
        def method = handler.getClass().getDeclaredMethod('flattenTupleToMap', List, Map)
        method.setAccessible(true)
        def flat = method.invoke(handler, tuple, entityValues)

        then:
        flat.bold.nii instanceof List
        flat.bold.nii.every { it instanceof java.nio.file.Path }
        flat.bold.nii.every { it.isAbsolute() }
    }

    def 'validateAndEmitChannel should emit flattened items into target channel'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        handler.withTarget(CH.create())

        def results = new DataflowQueue()
        def groupingKey = ['sub-01','ses-01']
        def enrichedData = [
            data: [
                T1w: [
                    nii: 'anat/sub-01_T1w.nii.gz'
                ]
            ],
            bidsParentDir: '/data/bids'
        ]
        results << [groupingKey, enrichedData]

        when:
        def method = handler.getClass().getDeclaredMethod('validateAndEmitChannel', DataflowQueue)
        method.setAccessible(true)
        method.invoke(handler, results)

        then:
        // Read emitted item from target
        def emitted = handler.@target.val
        emitted instanceof Map
        emitted.meta.subject == 'sub-01'
        emitted.T1w.nii instanceof java.nio.file.Path
    }

    def 'should include enrichedData top level entity keys in meta'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        handler.withTarget(CH.create())

        def results = new DataflowQueue()
        def groupingKey = ['sub-01','ses-01']
        def enrichedData = [
            data: [
                T1w: [
                    nii: 'anat/sub-01_T1w.nii.gz'
                ]
            ],
            bidsParentDir: '/data/bids',
            // Entities that could be injected during parsing
            task: 'rest',
            subject: 'sub-01'
        ]
        results << [groupingKey, enrichedData]

        when:
        def method = handler.getClass().getDeclaredMethod('validateAndEmitChannel', DataflowQueue)
        method.setAccessible(true)
        method.invoke(handler, results)

        then:
        def emitted = handler.@target.val
        emitted.meta.subject == 'sub-01'
        emitted.meta.task == 'rest'
    }

    def 'should merge participants metadata with subject-only matching'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session', 'run', 'echo']
        setParticipants(handler, [
            [participant_id: 'sub-01', age: '34', group: 'control']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-02', 'run-1', 'echo-2'], [
            data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']],
            bidsParentDir: '/data/bids'
        ])

        then:
        flat.meta.subject == 'sub-01'
        flat.meta.age == '34'
        flat.meta.group == 'control'
        !flat.meta.containsKey('participant_id')
    }

    def 'should select most specific participants row across session run and echo'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session', 'run', 'echo']
        setParticipants(handler, [
            [participant_id: 'sub-01', age: '30', cohort: 'base'],
            [participant_id: 'sub-01', session_id: 'ses-01', cohort: 'ses'],
            [participant_id: 'sub-01', session_id: 'ses-01', run: 'run-1', cohort: 'run'],
            [participant_id: 'sub-01', session_id: 'ses-01', run: 'run-1', echo: 'echo-2', cohort: 'echo']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-01', 'run-1', 'echo-2'], [
            data: [bold: [nii: 'func/sub-01_task-rest_bold.nii.gz']],
            bidsParentDir: '/data/bids'
        ])

        then:
        flat.meta.cohort == 'echo'
        flat.meta.subject == 'sub-01'
        flat.meta.session == 'ses-01'
        flat.meta.run == 'run-1'
        flat.meta.echo == 'echo-2'
    }

    def 'should support normalized keys and values from participants metadata'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session', 'run']
        setParticipants(handler, [
            [subject_id: '01', session_id: '01', run: '1', handedness: 'right']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-01', 'run-1'], [
            data: [dwi: [nii: 'dwi/sub-01_run-01_dwi.nii.gz']],
            bidsParentDir: '/data/bids'
        ])

        then:
        flat.meta.handedness == 'right'
    }

    def 'should match participants aliases loaded from json config'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        setParticipants(handler, [
            [participant: 'sub-01', session_id: 'ses-01', cohort: 'config-alias']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-01'], [
            data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']],
            bidsParentDir: '/data/bids'
        ])

        then:
        flat.meta.cohort == 'config-alias'
    }

    def 'should match participants aliases loaded from custom json path option'() {
        given:
        File customAliases = File.createTempFile('entity-aliases', '.json')
        customAliases.text = '{"sub":["participant_code"]}'

        def handler = new BidsHandler()
        handler.withOpts([entity_aliases_json: customAliases.absolutePath])
        handler.loopOverEntities = ['subject', 'session']
        setParticipants(handler, [
            [participant_code: 'sub-01', session_id: 'ses-01', cohort: 'custom-alias']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-01'], [
            data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']],
            bidsParentDir: '/data/bids'
        ])

        then:
        flat.meta.cohort == 'custom-alias'

        cleanup:
        customAliases.delete()
    }

    def 'should resolve ambiguous best matches deterministically and preserve existing meta values'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        setParticipants(handler, [
            [participant_id: 'sub-01', session_id: 'ses-01', sex: 'F', site: 'A'],
            [participant_id: 'sub-01', session_id: 'ses-01', sex: 'M', site: 'B']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-01'], [
            data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']],
            bidsParentDir: '/data/bids',
            site: 'meta-site'
        ])

        then:
        flat.meta.sex == 'F'
        flat.meta.site == 'meta-site'
    }

    def 'should keep meta unchanged when no participants row matches'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        setParticipants(handler, [
            [participant_id: 'sub-02', age: '40']
        ])

        when:
        def flat = invokeFlatten(handler, ['sub-01', 'ses-01'], [
            data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']],
            bidsParentDir: '/data/bids'
        ])

        then:
        flat.meta.subject == 'sub-01'
        flat.meta.session == 'ses-01'
        !flat.meta.containsKey('age')
    }

    private static void setParticipants(BidsHandler handler, List<Map<String, String>> participants) {
        def field = handler.getClass().getDeclaredField('participantsMetadata')
        field.setAccessible(true)
        field.set(handler, participants)
    }

    private static Map invokeFlatten(BidsHandler handler, List<String> groupingKey, Map enrichedData) {
        def method = handler.getClass().getDeclaredMethod('flattenTupleToMap', List, Map)
        method.setAccessible(true)
        Map entityValues = [:]
        handler.loopOverEntities.eachWithIndex { entity, idx ->
            entityValues[entity] = groupingKey[idx]
        }
        return method.invoke(handler, [groupingKey, enrichedData], entityValues) as Map
    }

}
