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

}
