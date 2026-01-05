package nfneuro.grouping

import nfneuro.plugin.util.SuffixMapper
import spock.lang.Specification

/**
 * Integration test for heterogeneous datasets with suffix mapping collisions
 * 
 * Tests the inverted map structure (configKey -> targetSuffix) to ensure:
 * - Multiple configs can map to same suffix without collision
 * - Correct config keys are resolved for files
 * - Entity filters properly distinguish between configs
 */
class HeterogeneousSuffixMappingSpec extends Specification {

    def "suffix mapping: multiple DWI configs don't collide"() {
        given: "heterogeneous DWI configuration"
        def config = [
            dwi_ap: [
                named_set: [
                    ap: [direction: 'dir-AP'],
                    pa: [direction: 'dir-PA']
                ],
                suffix_maps_to: 'dwi'
            ],
            dwi_rl: [
                named_set: [
                    rl: [direction: 'dir-RL'],
                    lr: [direction: 'dir-LR']
                ],
                suffix_maps_to: 'dwi'
            ],
            dwi_is: [
                named_set: [
                    is: [direction: 'dir-IS'],
                    si: [direction: 'dir-SI']
                ],
                suffix_maps_to: 'dwi'
            ]
        ]

        when: "building suffix mapping"
        def mapping = SuffixMapper.suffixMapping(config)

        then: "all three configs present in inverted map"
        mapping['named_set']['dwi_ap'] == 'dwi'
        mapping['named_set']['dwi_rl'] == 'dwi'
        mapping['named_set']['dwi_is'] == 'dwi'

        when: "resolving config keys for dwi suffix"
        def candidates = SuffixMapper.resolveConfigKeys('named_set', 'dwi', mapping)

        then: "all three are returned as candidates"
        candidates.size() == 3
        candidates.containsAll(['dwi_ap', 'dwi_rl', 'dwi_is'])
    }

    def "suffix mapping: multiple mask configs don't collide"() {
        given: "multiple mask types"
        def config = [
            lesion: [
                plain_set: [description: 'lesion'],
                suffix_maps_to: 'mask'
            ],
            wmparc: [
                plain_set: [description: 'wmparc'],
                suffix_maps_to: 'mask'
            ],
            aparc_aseg: [
                plain_set: [description: 'aparc+aseg'],
                suffix_maps_to: 'mask'
            ]
        ]

        when: "building suffix mapping"
        def mapping = SuffixMapper.suffixMapping(config)

        then: "all three mask configs present"
        mapping['plain_set']['lesion'] == 'mask'
        mapping['plain_set']['wmparc'] == 'mask'
        mapping['plain_set']['aparc_aseg'] == 'mask'

        when: "resolving config keys"
        def candidates = SuffixMapper.resolveConfigKeys('plain_set', 'mask', mapping)

        then: "all three are returned"
        candidates.size() == 3
        candidates.containsAll(['lesion', 'wmparc', 'aparc_aseg'])
    }

    def "suffix mapping: plain_set and named_set independent"() {
        given: "same suffix in different set types"
        def config = [
            dwi: [
                plain_set: [:]
            ],
            dwi_ap: [
                named_set: [
                    ap: [direction: 'dir-AP'],
                    pa: [direction: 'dir-PA']
                ],
                suffix_maps_to: 'dwi'
            ],
            dwi_rl: [
                named_set: [
                    rl: [direction: 'dir-RL'],
                    lr: [direction: 'dir-LR']
                ],
                suffix_maps_to: 'dwi'
            ]
        ]

        when: "building suffix mapping"
        def mapping = SuffixMapper.suffixMapping(config)

        then: "plain_set dwi has no mapping (no suffix_maps_to)"
        !mapping['plain_set'].containsKey('dwi')
        !mapping['plain_set'].containsKey('dwi_ap')

        and: "named_set has both DWI configs"
        mapping['named_set']['dwi_ap'] == 'dwi'
        mapping['named_set']['dwi_rl'] == 'dwi'

        when: "resolving for plain_set"
        def plainCandidates = SuffixMapper.resolveConfigKeys('plain_set', 'dwi', mapping)

        then: "returns 'dwi' itself (no mapping)"
        plainCandidates == ['dwi']

        when: "resolving for named_set"
        def namedCandidates = SuffixMapper.resolveConfigKeys('named_set', 'dwi', mapping)

        then: "returns both mapped configs"
        namedCandidates.size() == 2
        namedCandidates.containsAll(['dwi_ap', 'dwi_rl'])
    }

    def "real-world heterogeneous dataset scenario"() {
        given: "complete heterogeneous DWI + masks configuration"
        def config = [
            T1w: [
                plain_set: [:]
            ],
            lesion: [
                plain_set: [description: 'lesion'],
                suffix_maps_to: 'mask'
            ],
            wmparc: [
                plain_set: [description: 'wmparc'],
                suffix_maps_to: 'mask'
            ],
            aparc_aseg: [
                plain_set: [description: 'aparc+aseg'],
                suffix_maps_to: 'mask'
            ],
            dwi: [
                plain_set: [:]
            ],
            dwi_ap: [
                named_set: [
                    ap: [direction: 'dir-AP'],
                    pa: [direction: 'dir-PA']
                ],
                required: ['ap', 'pa'],
                suffix_maps_to: 'dwi'
            ],
            dwi_rl: [
                named_set: [
                    rl: [direction: 'dir-RL'],
                    lr: [direction: 'dir-LR']
                ],
                required: ['rl', 'lr'],
                suffix_maps_to: 'dwi'
            ],
            dwi_is: [
                named_set: [
                    is: [direction: 'dir-IS'],
                    si: [direction: 'dir-SI']
                ],
                required: ['is', 'si'],
                suffix_maps_to: 'dwi'
            ],
            sbref: [
                plain_set: [:]
            ],
            sbref_ap: [
                named_set: [
                    ap: [direction: 'dir-AP'],
                    pa: [direction: 'dir-PA']
                ],
                required: ['ap', 'pa'],
                suffix_maps_to: 'sbref'
            ],
            sbref_rl: [
                named_set: [
                    rl: [direction: 'dir-RL'],
                    lr: [direction: 'dir-LR']
                ],
                required: ['rl', 'lr'],
                suffix_maps_to: 'sbref'
            ],
            sbref_is: [
                named_set: [
                    is: [direction: 'dir-IS'],
                    si: [direction: 'dir-SI']
                ],
                required: ['is', 'si'],
                suffix_maps_to: 'sbref'
            ]
        ]

        when: "building suffix mapping"
        def mapping = SuffixMapper.suffixMapping(config)

        then: "T1w has no mapping (no suffix_maps_to)"
        !mapping['plain_set'].containsKey('T1w')

        and: "all three masks are mapped"
        mapping['plain_set']['lesion'] == 'mask'
        mapping['plain_set']['wmparc'] == 'mask'
        mapping['plain_set']['aparc_aseg'] == 'mask'

        and: "plain dwi has no mapping"
        !mapping['plain_set'].containsKey('dwi')

        and: "all three DWI named configs are mapped"
        mapping['named_set']['dwi_ap'] == 'dwi'
        mapping['named_set']['dwi_rl'] == 'dwi'
        mapping['named_set']['dwi_is'] == 'dwi'

        and: "plain sbref has no mapping"
        !mapping['plain_set'].containsKey('sbref')

        and: "all three sbref named configs are mapped"
        mapping['named_set']['sbref_ap'] == 'sbref'
        mapping['named_set']['sbref_rl'] == 'sbref'
        mapping['named_set']['sbref_is'] == 'sbref'

        when: "resolving candidates for each suffix type"
        def maskCandidates = SuffixMapper.resolveConfigKeys('plain_set', 'mask', mapping)
        def dwiCandidates = SuffixMapper.resolveConfigKeys('named_set', 'dwi', mapping)
        def sbrefCandidates = SuffixMapper.resolveConfigKeys('named_set', 'sbref', mapping)

        then: "all mask configs available"
        maskCandidates.size() == 3
        maskCandidates.containsAll(['lesion', 'wmparc', 'aparc_aseg'])

        and: "all DWI configs available"
        dwiCandidates.size() == 3
        dwiCandidates.containsAll(['dwi_ap', 'dwi_rl', 'dwi_is'])

        and: "all sbref configs available"
        sbrefCandidates.size() == 3
        sbrefCandidates.containsAll(['sbref_ap', 'sbref_rl', 'sbref_is'])
    }
}
