/* groovylint-disable all */
package nfneuro.plugin.channel

import groovyx.gpars.dataflow.DataflowQueue
import spock.lang.Specification

/**
 * Tests for the outer-join behaviour introduced in BidsHandler.unifyResults
 * and PlainSetHandler.processGroup.
 *
 * The problem being addressed: when a single fromBIDS call configures T1w
 * (1 file) and fmap/epi (3 runs), the channel should emit 3 grouped items:
 * [T1w, fmap run-01], [T1w, fmap run-02], [T1w, fmap run-03].
 * Previously only 1 item was emitted (inner-join behaviour).
 */
class OuterJoinSpec extends Specification {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static List invokeUnifyResults(BidsHandler handler, DataflowQueue queue, List<String> loopOverEntities) {
        def method = handler.getClass().getDeclaredMethod('unifyResults', DataflowQueue, List)
        method.setAccessible(true)
        DataflowQueue result = method.invoke(handler, queue, loopOverEntities) as DataflowQueue
        List items = []
        result.each { items << it }
        return items
    }

    private static List invokeComputeOuterJoin(BidsHandler handler, List streams) {
        def method = handler.getClass().getDeclaredMethod('computeOuterJoin', List)
        method.setAccessible(true)
        return method.invoke(handler, streams) as List
    }

    private static DataflowQueue queueOf(List<Map> tuples) {
        DataflowQueue q = new DataflowQueue()
        tuples.each { q << it }
        return q
    }

    // -----------------------------------------------------------------------
    // computeOuterJoin unit tests
    // -----------------------------------------------------------------------

    def 'computeOuterJoin: single stream with single item returns one combo'() {
        given:
        def handler = new BidsHandler()
        def itemA = [data: [T1w: 'a'], filePaths: [], bidsParentDir: '']

        when:
        def combos = invokeComputeOuterJoin(handler, [[itemA]])

        then:
        combos.size() == 1
        (combos[0] as List).size() == 1
        (combos[0] as List)[0] == itemA
    }

    def 'computeOuterJoin: two streams with one item each returns one combo'() {
        given:
        def handler = new BidsHandler()
        def t1w = [data: [T1w: 'a'], filePaths: [], bidsParentDir: '']
        def epi  = [data: [epi: 'b'], filePaths: [], bidsParentDir: '']

        when:
        def combos = invokeComputeOuterJoin(handler, [[t1w], [epi]])

        then:
        combos.size() == 1
        def combo = combos[0] as List
        combo.size() == 2
        combo.contains(t1w)
        combo.contains(epi)
    }

    def 'computeOuterJoin: one stream with 1 item, one stream with 3 items returns 3 combos'() {
        given:
        def handler = new BidsHandler()
        def t1w  = [data: [T1w: 'a'],  filePaths: [], bidsParentDir: '']
        def epi1 = [data: [epi: 'b1'], filePaths: [], bidsParentDir: '']
        def epi2 = [data: [epi: 'b2'], filePaths: [], bidsParentDir: '']
        def epi3 = [data: [epi: 'b3'], filePaths: [], bidsParentDir: '']

        when:
        def combos = invokeComputeOuterJoin(handler, [[t1w], [epi1, epi2, epi3]])

        then:
        combos.size() == 3
        combos.every { combo -> (combo as List).size() == 2 }
        combos.collect { combo -> ((combo as List)[0] as Map) }.every { it == t1w }
        combos.collect { combo -> (combo as List)[1] as Map } as Set == [epi1, epi2, epi3] as Set
    }

    def 'computeOuterJoin: two streams with 2 items each returns 4 combos'() {
        given:
        def handler = new BidsHandler()
        def a1 = [data: [T1w: 'a1'], filePaths: [], bidsParentDir: '']
        def a2 = [data: [T1w: 'a2'], filePaths: [], bidsParentDir: '']
        def b1 = [data: [epi: 'b1'], filePaths: [], bidsParentDir: '']
        def b2 = [data: [epi: 'b2'], filePaths: [], bidsParentDir: '']

        when:
        def combos = invokeComputeOuterJoin(handler, [[a1, a2], [b1, b2]])

        then:
        combos.size() == 4
    }

    // -----------------------------------------------------------------------
    // unifyResults outer-join tests
    // -----------------------------------------------------------------------

    def 'unifyResults: single suffix items with same fingerprint remain separate (no merge)'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        def groupingKey = ['sub-01', 'ses-01']

        // Three fmap alternatives for the same loop-over group
        def epi1 = [data: [epi: [nii: 'fmap/run-01.nii.gz']], filePaths: ['fmap/run-01.nii.gz'], bidsParentDir: '']
        def epi2 = [data: [epi: [nii: 'fmap/run-02.nii.gz']], filePaths: ['fmap/run-02.nii.gz'], bidsParentDir: '']
        def epi3 = [data: [epi: [nii: 'fmap/run-03.nii.gz']], filePaths: ['fmap/run-03.nii.gz'], bidsParentDir: '']
        def queue = queueOf([
            [groupingKey, epi1],
            [groupingKey, epi2],
            [groupingKey, epi3]
        ])

        when:
        def unified = invokeUnifyResults(handler, queue, ['subject', 'session'])

        then:
        unified.size() == 3
        unified.every { item ->
            def data = ((item as List)[1] as Map).data as Map
            data.containsKey('epi')
        }
        // Each item should have a different epi path
        def epiPaths = unified.collect { item ->
            def data = ((item as List)[1] as Map).data as Map
            (data.epi as Map).nii
        }
        epiPaths as Set == ['fmap/run-01.nii.gz', 'fmap/run-02.nii.gz', 'fmap/run-03.nii.gz'] as Set
    }

    def 'unifyResults: T1w (1 item) × epi (3 items) produces 3 joined outputs'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        def groupingKey = ['sub-01', 'ses-01']

        def t1w = [data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']], filePaths: ['anat/sub-01_T1w.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']
        def epi1 = [data: [epi: [nii: 'fmap/run-01.nii.gz']], filePaths: ['fmap/run-01.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']
        def epi2 = [data: [epi: [nii: 'fmap/run-02.nii.gz']], filePaths: ['fmap/run-02.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']
        def epi3 = [data: [epi: [nii: 'fmap/run-03.nii.gz']], filePaths: ['fmap/run-03.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']

        def queue = queueOf([
            [groupingKey, t1w],
            [groupingKey, epi1],
            [groupingKey, epi2],
            [groupingKey, epi3]
        ])

        when:
        def unified = invokeUnifyResults(handler, queue, ['subject', 'session'])

        then:
        unified.size() == 3
        unified.every { item ->
            def data = ((item as List)[1] as Map).data as Map
            data.containsKey('T1w') && data.containsKey('epi')
        }
        // T1w path is the same in all outputs
        unified.every { item ->
            def data = ((item as List)[1] as Map).data as Map
            (data.T1w as Map).nii == 'anat/sub-01_T1w.nii.gz'
        }
        // Each output has a distinct epi path
        def epiPaths = unified.collect { item ->
            def data = ((item as List)[1] as Map).data as Map
            (data.epi as Map).nii
        }
        epiPaths as Set == ['fmap/run-01.nii.gz', 'fmap/run-02.nii.gz', 'fmap/run-03.nii.gz'] as Set
    }

    def 'unifyResults: disjoint T1w + dwi single items merge into one output (backward compat)'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        def groupingKey = ['sub-01', 'ses-01']

        def t1w = [data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']], filePaths: ['anat/sub-01_T1w.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']
        def dwi = [data: [dwi: [ap: [nii: 'dwi/sub-01_dir-AP_dwi.nii.gz']]], filePaths: ['dwi/sub-01_dir-AP_dwi.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']

        def queue = queueOf([
            [groupingKey, t1w],
            [groupingKey, dwi]
        ])

        when:
        def unified = invokeUnifyResults(handler, queue, ['subject', 'session'])

        then:
        unified.size() == 1
        def data = ((unified[0] as List)[1] as Map).data as Map
        data.containsKey('T1w')
        data.containsKey('dwi')
    }

    def 'unifyResults: multiple loop-over groups remain independent'() {
        given:
        def handler = new BidsHandler()
        handler.loopOverEntities = ['subject', 'session']
        def key01 = ['sub-01', 'ses-01']
        def key02 = ['sub-02', 'ses-01']

        def t1w01 = [data: [T1w: [nii: 'anat/sub-01_T1w.nii.gz']], filePaths: ['anat/sub-01_T1w.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']
        def epi01 = [data: [epi: [nii: 'fmap/sub-01_run-01.nii.gz']], filePaths: ['fmap/sub-01_run-01.nii.gz'], bidsParentDir: '', subject: 'sub-01', session: 'ses-01']
        def t1w02 = [data: [T1w: [nii: 'anat/sub-02_T1w.nii.gz']], filePaths: ['anat/sub-02_T1w.nii.gz'], bidsParentDir: '', subject: 'sub-02', session: 'ses-01']

        def queue = queueOf([
            [key01, t1w01],
            [key01, epi01],
            [key02, t1w02]
        ])

        when:
        def unified = invokeUnifyResults(handler, queue, ['subject', 'session'])

        then:
        unified.size() == 2
        // One combined item for sub-01 and one for sub-02
        def sub01Items = unified.findAll { item -> ((item as List)[0] as List)[0] == 'sub-01' }
        def sub02Items = unified.findAll { item -> ((item as List)[0] as List)[0] == 'sub-02' }
        sub01Items.size() == 1
        sub02Items.size() == 1

        def data01 = ((sub01Items[0] as List)[1] as Map).data as Map
        data01.containsKey('T1w') && data01.containsKey('epi')

        def data02 = ((sub02Items[0] as List)[1] as Map).data as Map
        data02.containsKey('T1w') && !data02.containsKey('epi')
    }

}
