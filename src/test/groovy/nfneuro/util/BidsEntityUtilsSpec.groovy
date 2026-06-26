/* groovylint-disable all */
package nfneuro.plugin.util

import spock.lang.Specification

class BidsEntityUtilsSpec extends Specification {

    def 'normalizeEntityKey should resolve built-in and json aliases'() {
        given:
        File aliases = File.createTempFile('entity-aliases', '.json')
        aliases.text = '{"sub":["custom_participant"]}'
        Map<String, String> aliasMap = BidsEntityUtils.buildAliasToEntityMap(aliases.absolutePath)

        expect:
        BidsEntityUtils.normalizeEntityKey('participant_id', aliasMap) == 'sub'
        BidsEntityUtils.normalizeEntityKey('custom_participant', aliasMap) == 'sub'
        BidsEntityUtils.normalizeEntityKey('session_id', aliasMap) == 'ses'

        cleanup:
        aliases.delete()
    }

    def 'normalizeEntityValue should strip matching entity prefixes'() {
        given:
        Map<String, String> aliasMap = BidsEntityUtils.buildAliasToEntityMap()

        expect:
        BidsEntityUtils.normalizeEntityValue('sub', 'sub-01', aliasMap) == '1'
        BidsEntityUtils.normalizeEntityValue('ses', 'ses-02', aliasMap) == '2'
        BidsEntityUtils.normalizeEntityValue('run', 'run-1', aliasMap) == '1'
    }

    def 'normalizeEntityMap should normalize keys and values through alias map'() {
        given:
        Map<String, String> aliasMap = BidsEntityUtils.buildAliasToEntityMap()
        Map input = [participant_id: 'sub-01', session_id: 'ses-01', run: 'run-1', site: 'A']

        when:
        Map<String, String> normalized = BidsEntityUtils.normalizeEntityMap(input, aliasMap)

        then:
        normalized == [sub: '1', ses: '1', run: '1']
    }
}
