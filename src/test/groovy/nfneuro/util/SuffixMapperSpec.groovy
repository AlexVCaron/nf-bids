package nfneuro.plugin.util

import spock.lang.Specification

/**
 * Test suffix mapping with inverted structure (configKey -> targetSuffix)
 * 
 * Verifies that multiple configs can map to the same suffix without collision
 */
class SuffixMapperSpec extends Specification {

    def "suffix mapping with multiple configs mapping to same suffix"() {
        given: "a configuration with multiple dwi configs mapping to 'dwi' suffix"
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

        then: "all three configs are present in the mapping (no collision)"
        mapping['named_set']['dwi_ap'] == 'dwi'
        mapping['named_set']['dwi_rl'] == 'dwi'
        mapping['named_set']['dwi_is'] == 'dwi'
        
        and: "plain_set dwi has no mapping (no suffix_maps_to)"
        !mapping['plain_set'].containsKey('dwi_ap')
        !mapping['plain_set'].containsKey('dwi_rl')
        !mapping['plain_set'].containsKey('dwi_is')
    }

    def "resolveConfigKeys returns all candidates for a suffix"() {
        given: "a suffix mapping with multiple configs for 'dwi'"
        def mapping = [
            named_set: [
                dwi_ap: 'dwi',
                dwi_rl: 'dwi',
                dwi_is: 'dwi'
            ]
        ]

        when: "resolving config keys for 'dwi' suffix"
        def candidates = SuffixMapper.resolveConfigKeys('named_set', 'dwi', mapping)

        then: "all three config keys are returned"
        candidates.size() == 3
        candidates.contains('dwi_ap')
        candidates.contains('dwi_rl')
        candidates.contains('dwi_is')
    }

    def "resolveConfigKeys returns suffix itself when no mapping exists"() {
        given: "a suffix mapping without entry for 'T1w'"
        def mapping = [
            plain_set: [
                dwi_ap: 'dwi'
            ]
        ]

        when: "resolving config keys for 'T1w' suffix"
        def candidates = SuffixMapper.resolveConfigKeys('plain_set', 'T1w', mapping)

        then: "suffix itself is returned as candidate"
        candidates == ['T1w']
    }

    def "resolveConfigKeys handles empty mapping"() {
        given: "an empty suffix mapping"
        def mapping = [:]

        when: "resolving config keys"
        def candidates = SuffixMapper.resolveConfigKeys('plain_set', 'dwi', mapping)

        then: "suffix itself is returned"
        candidates == ['dwi']
    }

    def "getTargetSuffix retrieves correct suffix from inverted mapping"() {
        given: "a suffix mapping with inverted structure"
        def mapping = [
            named_set: [
                dwi_ap: 'dwi',
                sbref_rl: 'sbref'
            ]
        ]

        when: "getting target suffix for config key"
        def suffix1 = SuffixMapper.getTargetSuffix('named_set', 'dwi_ap', mapping)
        def suffix2 = SuffixMapper.getTargetSuffix('named_set', 'sbref_rl', mapping)

        then: "correct suffixes are returned"
        suffix1 == 'dwi'
        suffix2 == 'sbref'
    }

    def "getTargetSuffix returns configKey when no mapping exists"() {
        given: "a suffix mapping without entry for a config"
        def mapping = [
            named_set: [
                dwi_ap: 'dwi'
            ]
        ]

        when: "getting target suffix for unmapped config key"
        def suffix = SuffixMapper.getTargetSuffix('named_set', 'T1w', mapping)

        then: "config key itself is returned"
        suffix == 'T1w'
    }

    def "multiple mask types with same suffix_maps_to don't collide"() {
        given: "multiple mask configs mapping to 'mask' suffix"
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

        then: "all three mask configs are present"
        mapping['plain_set']['lesion'] == 'mask'
        mapping['plain_set']['wmparc'] == 'mask'
        mapping['plain_set']['aparc_aseg'] == 'mask'

        when: "resolving config keys for 'mask' suffix"
        def candidates = SuffixMapper.resolveConfigKeys('plain_set', 'mask', mapping)

        then: "all three config keys are returned"
        candidates.size() == 3
        candidates.contains('lesion')
        candidates.contains('wmparc')
        candidates.contains('aparc_aseg')
    }

    def "mixed set types with same suffix handled independently"() {
        given: "configs with same suffix in different set types"
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
            ]
        ]

        when: "building suffix mapping"
        def mapping = SuffixMapper.suffixMapping(config)

        then: "plain_set has no mapping for dwi (no suffix_maps_to)"
        !mapping['plain_set'].containsKey('dwi_ap')
        !mapping['plain_set'].containsKey('dwi')

        and: "named_set has mapping for dwi_ap"
        mapping['named_set']['dwi_ap'] == 'dwi'

        when: "resolving for plain_set"
        def plainCandidates = SuffixMapper.resolveConfigKeys('plain_set', 'dwi', mapping)

        and: "resolving for named_set"
        def namedCandidates = SuffixMapper.resolveConfigKeys('named_set', 'dwi', mapping)

        then: "plain_set returns 'dwi' as candidate (no mapping)"
        plainCandidates == ['dwi']

        and: "named_set returns 'dwi_ap' as candidate"
        namedCandidates == ['dwi_ap']
    }
}
