/* groovylint-disable all */
package nfneuro.grouping

import nfneuro.plugin.grouping.SequentialSetHandler
import nfneuro.plugin.model.BidsChannelData
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.util.SuffixMapper
import spock.lang.Specification

/**
 * Unit tests for {@link SequentialSetHandler}.
 *
 * <p>Covers entity consumption and grouping for sequential sets: when the entity
 * used as the {@code by_entity}/{@code by_entities} sequence dimension is also a
 * {@code loop_over} entity, files spanning that dimension must be collapsed into a
 * single item (grouping-key value {@code NA}) so they fuse with the other results,
 * instead of producing one item per value.</p>
 */
class SequentialSetHandlerSpec extends Specification {

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

    def 'sequence entity in loop_over collapses into one fused item'() {
        given: 'a sequential set sequencing over echo, which is also a loop_over entity'
        def handler = new SequentialSetHandler()
        def root = '/bids'
        def config = [
            MEGRE: [
                sequential_set: [
                    by_entity: 'echo'
                ]
            ]
        ]
        def mapping = SuffixMapper.suffixMapping(config)

        and: 'three echoes for the same subject/session'
        def files = [
            mk('/bids/sub-01/ses-005/anat/sub-01_ses-005_echo-1_MEGRE.nii.gz', 'MEGRE', [sub: '01', ses: '005', echo: '1']),
            mk('/bids/sub-01/ses-005/anat/sub-01_ses-005_echo-2_MEGRE.nii.gz', 'MEGRE', [sub: '01', ses: '005', echo: '2']),
            mk('/bids/sub-01/ses-005/anat/sub-01_ses-005_echo-3_MEGRE.nii.gz', 'MEGRE', [sub: '01', ses: '005', echo: '3'])
        ]

        when:
        def results = drain(handler.process(root, files, config, ['subject', 'session', 'echo'], mapping))

        then: 'exactly one fused item is emitted (echoes are not split)'
        results.size() == 1

        and: 'all echoes are sequenced under the config key'
        def data = results[0].getSuffixData('MEGRE') as Map
        data.nii == [
            'sub-01/ses-005/anat/sub-01_ses-005_echo-1_MEGRE.nii.gz',
            'sub-01/ses-005/anat/sub-01_ses-005_echo-2_MEGRE.nii.gz',
            'sub-01/ses-005/anat/sub-01_ses-005_echo-3_MEGRE.nii.gz'
        ]

        and: 'echo is collapsed to NA so the item fuses with echo:NA results'
        def tuple = results[0].toChannelTuple(['subject', 'session', 'echo'])
        (tuple[0] as List) == ['sub-01', 'ses-005', 'NA']
    }

    def 'sequence entity not in loop_over is unaffected'() {
        given: 'a sequential set sequencing over echo, which is NOT a loop_over entity'
        def handler = new SequentialSetHandler()
        def root = '/bids'
        def config = [
            MEGRE: [
                sequential_set: [
                    by_entity: 'echo'
                ]
            ]
        ]
        def mapping = SuffixMapper.suffixMapping(config)

        def files = [
            mk('/bids/sub-01/anat/sub-01_echo-1_MEGRE.nii.gz', 'MEGRE', [sub: '01', echo: '1']),
            mk('/bids/sub-01/anat/sub-01_echo-2_MEGRE.nii.gz', 'MEGRE', [sub: '01', echo: '2'])
        ]

        when:
        def results = drain(handler.process(root, files, config, ['subject', 'session'], mapping))

        then: 'a single item with both echoes sequenced'
        results.size() == 1
        def data = results[0].getSuffixData('MEGRE') as Map
        data.nii == [
            'sub-01/anat/sub-01_echo-1_MEGRE.nii.gz',
            'sub-01/anat/sub-01_echo-2_MEGRE.nii.gz'
        ]
    }
}
