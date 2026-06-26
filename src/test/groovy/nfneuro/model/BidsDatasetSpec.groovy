/* groovylint-disable all */
package nfneuro.plugin.model

import spock.lang.Specification

class BidsDatasetSpec extends Specification {

    def 'loadParticipants should read participants.tsv from dataset root'() {
        given:
        File datasetDir = File.createTempDir('bids-dataset', '')
        new File(datasetDir, 'participants.tsv').text = '\uFEFFparticipant_id\tsession_id\tgroup\nsub-01\tses-01\tcontrol\n'

        and:
        def dataset = new BidsDataset(datasetDir.absolutePath)

        when:
        dataset.loadParticipants()

        then:
        dataset.participants.size() == 1
        dataset.participants[0].participant_id == 'sub-01'
        dataset.participants[0].session_id == 'ses-01'
        dataset.participants[0].group == 'control'

        cleanup:
        datasetDir.deleteDir()
    }

    def 'loadParticipants should replace stale rows on reload'() {
        given:
        File datasetDir = File.createTempDir('bids-dataset', '')
        File participants = new File(datasetDir, 'participants.tsv')
        participants.text = 'participant_id\tgroup\nsub-01\tcontrol\n'

        and:
        def dataset = new BidsDataset(datasetDir.absolutePath)
        dataset.loadParticipants()
        participants.text = 'participant_id\tgroup\nsub-02\tpatient\n'

        when:
        dataset.loadParticipants()

        then:
        dataset.participants.size() == 1
        dataset.participants[0].participant_id == 'sub-02'
        dataset.participants[0].group == 'patient'

        cleanup:
        datasetDir.deleteDir()
    }
}
