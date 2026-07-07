/* groovylint-disable all */
package nfneuro.grouping

import nfneuro.plugin.grouping.MixedSetHandler
import nfneuro.plugin.grouping.NamedSetHandler
import nfneuro.plugin.grouping.PlainSetHandler
import nfneuro.plugin.grouping.SequentialSetHandler
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.util.SuffixMapper
import spock.lang.Specification

/**
 * Verifies that the global (suffix-level) {@code exclude_entities} option is
 * honoured for every set type, while the per-set {@code exclude_entities} is
 * still respected.
 *
 * Regression test for https://github.com/nf-neuro/nf-bids/issues/31 where the
 * global {@code exclude_entities} had no effect on named/mixed/sequential sets.
 */
class GlobalExcludeEntitiesSpec extends Specification {

    private static BidsFile makeFile(String path, String suffix, Map<String, String> entityMap) {
        BidsFile f = new BidsFile(path)
        f.suffix = suffix
        entityMap.each { name, value -> f.addEntity(name, value) }
        return f
    }

    def 'global exclude_entities filters files for #setType'() {
        given: 'a config with a suffix-level exclude_entities and no per-set exclude'
        def handler = handlerFactory.call()
        def config = [ (suffix): configEntry ]
        def mapping = SuffixMapper.suffixMapping(config)

        and: 'a file carrying the excluded entity and one without it'
        def excludedFile = makeFile("/bids/sub-01_rec-dis2d_${suffix}.nii.gz", suffix, [sub: '01', rec: 'dis2d', flip: '1', inv: '1', acq: 'anat'])
        def keptFile     = makeFile("/bids/sub-01_${suffix}.nii.gz", suffix, [sub: '01', flip: '1', inv: '1', acq: 'anat'])

        expect: 'the file with the excluded entity is rejected'
        handler.findMatchingGrouping(excludedFile, config, mapping) == null

        and: 'the file without it still matches'
        handler.findMatchingGrouping(keptFile, config, mapping) != null

        where:
        setType           | suffix   | handlerFactory                   | configEntry
        'plain_set'       | 'T1w'    | { new PlainSetHandler() }        | [exclude_entities: ['reconstruction'], plain_set: [:]]
        'named_set'       | 'MTS'    | { new NamedSetHandler() }        | [exclude_entities: ['reconstruction'], named_set: [MTw: [flip: 'flip-1']]]
        'sequential_set'  | 'MP2RAGE'| { new SequentialSetHandler() }   | [exclude_entities: ['reconstruction'], sequential_set: [by_entity: 'inversion']]
        'mixed_set'       | 'mpm'    | { new MixedSetHandler() }        | [exclude_entities: ['reconstruction'], mixed_set: [named_dimension: 'acquisition', sequential_dimension: 'echo', named_groups: [MTw: [acquisition: 'acq-anat']]]]
    }

    def 'per-set exclude_entities is still honoured alongside the global one'() {
        given: 'a plain_set with a per-set exclude and a global exclude'
        def handler = new PlainSetHandler()
        def config = [ dwi: [exclude_entities: ['reconstruction'], plain_set: [exclude_entities: ['direction']]] ]
        def mapping = SuffixMapper.suffixMapping(config)

        expect: 'file excluded by the per-set entity'
        handler.findMatchingGrouping(makeFile('/bids/sub-01_dir-AP_dwi.nii.gz', 'dwi', [sub: '01', dir: 'AP']), config, mapping) == null

        and: 'file excluded by the global entity'
        handler.findMatchingGrouping(makeFile('/bids/sub-01_rec-dis2d_dwi.nii.gz', 'dwi', [sub: '01', rec: 'dis2d']), config, mapping) == null

        and: 'file without either entity matches'
        handler.findMatchingGrouping(makeFile('/bids/sub-01_dwi.nii.gz', 'dwi', [sub: '01']), config, mapping) != null
    }
}
