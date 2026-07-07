/* groovylint-disable all */
package nfneuro.grouping

import nfneuro.plugin.grouping.MixedSetHandler
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.util.SuffixMapper
import spock.lang.Specification

/**
 * Unit tests for {@link MixedSetHandler}.
 *
 * <p>Covers the two regressions described in the "Mixed set doesn't honor its
 * config" issue:</p>
 * <ul>
 *   <li>{@code suffix_maps_to} must be honored so a mixed-set config key (e.g.
 *       {@code epi_full}) matches files of the mapped suffix (e.g. {@code epi}).</li>
 *   <li>When an entity is used as the {@code sequential_dimension} <em>and</em> is
 *       part of {@code loop_over}, files spanning that dimension must be collapsed
 *       into a single item (grouping-key value {@code NA}) so they fuse with the
 *       other results, instead of producing one item per value.</li>
 * </ul>
 */
class MixedSetHandlerSpec extends Specification {

    private static BidsFile mk(String path, String suffix, Map<String, String> ents) {
        BidsFile f = new BidsFile(path)
        f.suffix = suffix
        ents.each { k, v -> f.addEntity(k, v) }
        return f
    }

    private static List<BidsChannelData> drain(def queue) {
        List<BidsChannelData> results = []
        queue.each { results << it }
        return results
    }

    def 'setName is mixed_set so suffix_maps_to resolves for mixed sets'() {
        expect: 'setName matches the getSetType key ("mixed_set") used to build the suffix mapping'
        new MixedSetHandler().setName() == 'mixed_set'
    }

    def 'suffix_maps_to matches mapped-suffix files and sequential_dimension collapses into one fused item'() {
        given: 'a mixed set epi_full mapping to the epi suffix, with run in loop_over'
        def handler = new MixedSetHandler()
        def root = '/bids'
        def config = [
            epi_full: [
                suffix_maps_to: 'epi',
                mixed_set: [
                    named_dimension: 'direction',
                    sequential_dimension: 'run',
                    named_groups: [
                        pa: [direction: 'dir-PA'],
                        ap: [direction: 'dir-AP']
                    ]
                ]
            ]
        ]
        def mapping = SuffixMapper.suffixMapping(config)

        and: 'two PA epi runs (run-01 and run-02)'
        def files = [
            mk('/bids/sub-01/ses-005/fmap/sub-01_ses-005_dir-PA_run-01_epi.nii.gz', 'epi', [sub: '01', ses: '005', dir: 'PA', run: '01']),
            mk('/bids/sub-01/ses-005/fmap/sub-01_ses-005_dir-PA_run-01_epi.json',   'epi', [sub: '01', ses: '005', dir: 'PA', run: '01']),
            mk('/bids/sub-01/ses-005/fmap/sub-01_ses-005_dir-PA_run-02_epi.nii.gz', 'epi', [sub: '01', ses: '005', dir: 'PA', run: '02']),
            mk('/bids/sub-01/ses-005/fmap/sub-01_ses-005_dir-PA_run-02_epi.json',   'epi', [sub: '01', ses: '005', dir: 'PA', run: '02'])
        ]

        when:
        def results = drain(handler.process(root, files, config, ['subject', 'session', 'run'], mapping))

        then: 'exactly one fused item is emitted (runs are not split)'
        results.size() == 1

        and: 'the item is emitted under the mapped config key (suffix_maps_to honored)'
        def data = results[0].getSuffixData('epi_full') as Map
        data != null
        data.containsKey('pa')

        and: 'both runs are sequenced under the pa group'
        (data.pa as Map).nii == [
            'sub-01/ses-005/fmap/sub-01_ses-005_dir-PA_run-01_epi.nii.gz',
            'sub-01/ses-005/fmap/sub-01_ses-005_dir-PA_run-02_epi.nii.gz'
        ]

        and: 'run is collapsed to NA so the item fuses with run:NA results'
        def tuple = results[0].toChannelTuple(['subject', 'session', 'run'])
        (tuple[0] as List) == ['sub-01', 'ses-005', 'NA']
    }

    def 'sequential_dimension not in loop_over is unaffected'() {
        given: 'a mixed set sequencing over echo, which is NOT a loop_over entity'
        def handler = new MixedSetHandler()
        def root = '/bids'
        def config = [
            MPM: [
                mixed_set: [
                    named_dimension: 'acquisition',
                    sequential_dimension: 'echo',
                    named_groups: [
                        MTw: [acquisition: 'acq-MTw']
                    ]
                ]
            ]
        ]
        def mapping = SuffixMapper.suffixMapping(config)

        def files = [
            mk('/bids/sub-01/anat/sub-01_acq-MTw_echo-1_MPM.nii.gz', 'MPM', [sub: '01', acq: 'MTw', echo: '1']),
            mk('/bids/sub-01/anat/sub-01_acq-MTw_echo-2_MPM.nii.gz', 'MPM', [sub: '01', acq: 'MTw', echo: '2'])
        ]

        when:
        def results = drain(handler.process(root, files, config, ['subject', 'session'], mapping))

        then: 'a single item with both echoes sequenced under MTw'
        results.size() == 1
        def data = results[0].getSuffixData('MPM') as Map
        (data.MTw as Map).nii == [
            'sub-01/anat/sub-01_acq-MTw_echo-1_MPM.nii.gz',
            'sub-01/anat/sub-01_acq-MTw_echo-2_MPM.nii.gz'
        ]
    }
}
