/* groovylint-disable all */
package nfneuro.plugin.util

import nfneuro.plugin.model.BidsFile
import spock.lang.Specification

class BidsCsvParserSpec extends Specification {

    def 'parse should read v3 libBIDS TSV with datatype metadata'() {
        given:
        def parser = new BidsCsvParser()
        File table = File.createTempFile('bids-table', '.tsv')
        table.text = (
            'derivatives\tdatatype\tsubject\ttemplate\tsession\tcohort\tsample\ttask\ttracksys\tacquisition\tnucleus\tvolume\tceagent\ttracer\tstain\treconstruction\tdirection\trun\tmodality\techo\tflip\tinversion\tmtransfer\tpart\tprocessing\themisphere\tspace\tsplit\trecording\tchunk\tatlas\tsegmentation\tscale\tresolution\tdensity\tlabel\tdescription\tsuffix\textension\tpath\n' +
            'NA\tanat\tsub-01\ttpl-MNI152\tses-01\tcohort-control\tNA\ttask-rest\tNA\tacq-highres\tNA\tNA\tNA\tNA\tNA\trec-defaced\tNA\trun-01\tNA\tNA\tNA\tNA\tNA\tNA\tNA\tNA\tspace-MNI152\tNA\tNA\tNA\tatlas-HOA\tseg-gray\tscale-2\tres-1mm\tden-10k\tNA\tdesc-preproc\tT1w\tnii.gz\t/data/sub-01/ses-01/anat/sub-01_ses-01_acq-highres_rec-defaced_space-MNI152_desc-preproc_T1w.nii.gz\n'
        )

        when:
        List<BidsFile> files = parser.parse(table)

        then:
        files.size() == 1
        files[0].getEntityValue('sub') == '01'
        files[0].getEntityValue('tpl') == 'MNI152'
        files[0].getEntityValue('ses') == '01'
        files[0].getEntityValue('cohort') == 'control'
        files[0].getEntityValue('acq') == 'highres'
        files[0].getEntityValue('rec') == 'defaced'
        files[0].getEntityValue('space') == 'MNI152'
        files[0].getEntityValue('atlas') == 'HOA'
        files[0].getEntityValue('seg') == 'gray'
        files[0].getEntityValue('scale') == '2'
        files[0].getEntityValue('res') == '1mm'
        files[0].getEntityValue('den') == '10k'
        files[0].getEntityValue('desc') == 'preproc'
        files[0].getMetadata('datatype') == 'anat'
        files[0].getMetadata('data_type') == null

        cleanup:
        table.delete()
    }

    def 'parseRow should return null when TSV column count mismatches header'() {
        given:
        def parser = new BidsCsvParser()
        File table = File.createTempFile('bids-table-invalid', '.tsv')
        table.text = (
            'derivatives\tdatatype\tsubject\tsuffix\textension\tpath\n' +
            'NA\tanat\tsub-01\tT1w\tnii.gz\n'
        )

        when:
        List<BidsFile> files = parser.parse(table)

        then:
        files.isEmpty()

        cleanup:
        table.delete()
    }
}
