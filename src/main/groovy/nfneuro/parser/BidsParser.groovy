package nfneuro.plugin.parser

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import groovyx.gpars.dataflow.DataflowQueue
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import nfneuro.plugin.model.BidsDataset
import nfneuro.plugin.util.BidsCsvParser
import nfneuro.plugin.util.BidsLogger

/**
 * Main BIDS dataset parser
 *
 * Orchestrates the parsing of BIDS datasets using libBIDS.sh wrapper
 * and converts results to BidsDataset and Nextflow channels
 *
 * @reference Parser implementation:
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf
 */
@Slf4j
@CompileStatic
class BidsParser {

    private final Session session
    private final LibBidsShWrapper libBidsWrapper
    private final BidsCsvParser csvParser

    BidsParser(Session session) {
        this.session = session
        this.libBidsWrapper = new LibBidsShWrapper()
        this.csvParser = new BidsCsvParser()
    }

    /**
     * Parse BIDS dataset to BidsDataset object
     *
     * Uses libBIDS.sh to scan BIDS directory and create structured dataset
     *
     * @param bidsDir Path to BIDS dataset directory
     * @param libBidsShPath Path to libBIDS.sh script (optional)
     * @return BidsDataset object
     *
     * @reference libbids_sh_parse process:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L1-L28
     */
    BidsDataset parseToDataset(String bidsDir, String libBidsShPath = null) {
        BidsLogger.logProgress("nf-bids-parser", "Parsing BIDS dataset: ${bidsDir}")

        // Execute libBIDS.sh wrapper
        def csvFile = libBidsWrapper.parseBidsToCSV(bidsDir, libBidsShPath)

        // Parse CSV to BidsFile objects
        def bidsFiles = csvParser.parse(csvFile)

        // Create BidsDataset
        def dataset = new BidsDataset(bidsDir)
        bidsFiles.each { file ->
            dataset.addFile(file)
        }

        // Load additional dataset metadata
        dataset.loadParticipants()

        BidsLogger.logSuccess("nf-bids-parser",
            "Parsed ${bidsFiles.size()} files from dataset '${dataset.name}'")

        return dataset
    }

}
