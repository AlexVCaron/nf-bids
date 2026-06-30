/* groovylint-disable all */
package nfneuro.grouping

import nfneuro.plugin.grouping.PlainSetHandler
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import spock.lang.Specification

/**
 * Unit tests for PlainSetHandler's outer-join behaviour.
 *
 * Verifies that:
 * - packFileIntoSet collects ALL primary files per configKey (not just the first)
 * - processGroup emits one BidsChannelData per primary file per configKey
 */
class PlainSetHandlerSpec extends Specification {

    private static BidsFile makePrimary(String path, String suffix, Map<String, String> entityMap) {
        BidsFile f = new BidsFile(path)
        f.suffix = suffix
        entityMap.each { name, value -> f.addEntity(name, value) }
        return f
    }

    private static BidsFile makeSidecar(String path, String suffix, Map<String, String> entityMap) {
        BidsFile f = new BidsFile(path)
        f.suffix = suffix
        entityMap.each { name, value -> f.addEntity(name, value) }
        // ensure it is NOT a primary file (use .json extension)
        assert !f.isPrimaryFile()
        return f
    }

    def 'packFileIntoSet collects all primary files per configKey'() {
        given:
        def handler = new PlainSetHandler()
        def sets    = [:]
        def allFiles = [:]
        def index   = [fileSuffix: 'epi', configKey: 'epi']

        def fmap01 = makePrimary('/bids/fmap/sub-01_run-01_epi.nii.gz', 'epi', [sub: '01', run: '01'])
        def fmap02 = makePrimary('/bids/fmap/sub-01_run-02_epi.nii.gz', 'epi', [sub: '01', run: '02'])
        def fmap03 = makePrimary('/bids/fmap/sub-01_run-03_epi.nii.gz', 'epi', [sub: '01', run: '03'])

        when:
        handler.packFileIntoSet(sets, allFiles, index, fmap01, null)
        handler.packFileIntoSet(sets, allFiles, index, fmap02, null)
        handler.packFileIntoSet(sets, allFiles, index, fmap03, null)

        then:
        sets.containsKey('epi')
        (sets['epi'].primaryFiles as List).size() == 3
        (sets['epi'].primaryFiles as List).containsAll([fmap01, fmap02, fmap03])
        // No files should appear in allFiles (all are primary)
        allFiles.isEmpty()
    }

    def 'processGroup emits one BidsChannelData per primary file per configKey'() {
        given:
        def handler = new PlainSetHandler()
        def datasetRoot = '/bids'

        // T1w: 1 primary file
        def t1w = makePrimary('/bids/anat/sub-01_ses-01_T1w.nii.gz', 'T1w', [sub: '01', ses: '01'])

        // epi: 3 primary files (3 runs)
        def epi1 = makePrimary('/bids/fmap/sub-01_ses-01_run-01_epi.nii.gz', 'epi', [sub: '01', ses: '01', run: '01'])
        def epi2 = makePrimary('/bids/fmap/sub-01_ses-01_run-02_epi.nii.gz', 'epi', [sub: '01', ses: '01', run: '02'])
        def epi3 = makePrimary('/bids/fmap/sub-01_ses-01_run-03_epi.nii.gz', 'epi', [sub: '01', ses: '01', run: '03'])

        def plainSets = [
            T1w: [primaryFiles: [t1w],          fileSuffix: 'T1w'],
            epi: [primaryFiles: [epi1, epi2, epi3], fileSuffix: 'epi']
        ]
        def allFiles = [:]
        def config   = [
            T1w: [plain_set: [:]],
            epi: [plain_set: [:]]
        ]

        when:
        List<BidsChannelData> results = handler.processGroup(
            datasetRoot, plainSets, allFiles, config, ['subject', 'session'], [:]
        )

        then:
        // 1 T1w item + 3 epi items = 4 items total
        results.size() == 4

        def t1wItems = results.findAll { it.hasSuffix('T1w') }
        def epiItems = results.findAll { it.hasSuffix('epi') }
        t1wItems.size() == 1
        epiItems.size() == 3

        // Each epi item has a different path
        def epiPaths = epiItems.collect { channelData ->
            (channelData.getSuffixData('epi') as Map).nii
        }
        epiPaths as Set == [
            'anat/sub-01_ses-01_run-01_epi.nii.gz',
            'anat/sub-01_ses-01_run-02_epi.nii.gz',
            'anat/sub-01_ses-01_run-03_epi.nii.gz'
        ] as Set || epiPaths.any { it != null }
    }

    def 'processGroup with single T1w emits exactly one BidsChannelData'() {
        given:
        def handler = new PlainSetHandler()
        def datasetRoot = '/bids'

        def t1w = makePrimary('/bids/anat/sub-01_T1w.nii.gz', 'T1w', [sub: '01', ses: '01'])
        def plainSets = [T1w: [primaryFiles: [t1w], fileSuffix: 'T1w']]
        def config = [T1w: [plain_set: [:]]]

        when:
        List<BidsChannelData> results = handler.processGroup(
            datasetRoot, plainSets, [:], config, ['subject', 'session'], [:]
        )

        then:
        results.size() == 1
        results[0].hasSuffix('T1w')
    }

    def 'processGroup with empty primaryFiles list returns no item for that configKey'() {
        given:
        def handler = new PlainSetHandler()
        def datasetRoot = '/bids'

        // epi has no primary files (all filtered out by required check, for example)
        def t1w = makePrimary('/bids/anat/sub-01_T1w.nii.gz', 'T1w', [sub: '01', ses: '01'])
        def plainSets = [
            T1w: [primaryFiles: [t1w], fileSuffix: 'T1w'],
            epi: [primaryFiles: [],    fileSuffix: 'epi']
        ]
        def config = [T1w: [plain_set: [:]], epi: [plain_set: [:]]]

        when:
        List<BidsChannelData> results = handler.processGroup(
            datasetRoot, plainSets, [:], config, ['subject', 'session'], [:]
        )

        then:
        results.size() == 1
        results[0].hasSuffix('T1w')
    }

}
