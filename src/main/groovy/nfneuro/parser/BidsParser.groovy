package nfneuro.plugin.parser

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import groovyx.gpars.dataflow.DataflowQueue
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
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf
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
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L1-L28
     */
    BidsDataset parseToDataset(String bidsDir, String libBidsShPath = null) {
        BidsLogger.logProgress("BidsParser", "Parsing BIDS dataset: ${bidsDir}")
        
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
        
        BidsLogger.logSuccess("BidsParser", 
            "Parsed ${bidsFiles.size()} files from dataset '${dataset.name}'")
        
        return dataset
    }
    
    /**
     * Parse BIDS dataset to CSV format (legacy method)
     * 
     * Uses libBIDS.sh to scan BIDS directory and create CSV representation
     * 
     * @param bidsDir Path to BIDS dataset directory
     * @param libBidsShPath Path to libBIDS.sh script (optional)
     * @return DataflowQueue containing parsed BidsFile objects
     * 
     * @reference libbids_sh_parse process: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L1-L28
     */
    DataflowQueue parse(String bidsDir, String libBidsShPath = null) {
        BidsLogger.logProgress("BidsParser", "Parsing BIDS dataset to channel: ${bidsDir}")
        
        // Execute libBIDS.sh wrapper
        def csvFile = libBidsWrapper.parseBidsToCSV(bidsDir, libBidsShPath)
        
        // Parse CSV to BidsFile objects
        def bidsFiles = csvParser.parse(csvFile)
        
        // Convert to channel
        return filesToChannel(bidsFiles)
    }
    
    /**
     * Convert list of BidsFile to DataflowQueue
     * 
     * @param bidsFiles List of BidsFile objects
     * @return DataflowQueue with BidsFile objects
     */
    private DataflowQueue filesToChannel(List<BidsFile> bidsFiles) {
        def queue = new DataflowQueue()
        
        bidsFiles.each { file ->
            queue.bind(file)  // Use .bind() not << to match nf-sqldb pattern
        }
        
        // Queue is returned unbound - no STOP marker here
        // Completion is handled by the final validateAndEmitChannel
        
        return queue
    }
    
    /**
     * Parse BIDS entities from filename
     * 
     * Extracts BIDS entities using standard naming convention
     * 
     * @param filename BIDS filename
     * @return Map of entity name to value
     * 
     * @reference Entity parsing from:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/libBIDS.sh/libBIDS.sh
     */
    static Map<String, String> parseEntitiesFromFilename(String filename) {
        Map<String, String> entities = [:]
        
        // Remove extension
        def nameWithoutExt = filename.replaceAll(/\.(nii\.gz|nii|json|tsv|bval|bvec)$/, '')
        
        // Split by underscore and parse entity-value pairs
        def parts = nameWithoutExt.split('_')
        parts.each { part ->
            if (part.contains('-')) {
                def keyValue = part.split('-', 2)
                def key = keyValue[0]
                def value = keyValue.length > 1 ? keyValue[1] : ''
                // Only include if it's a valid BIDS entity
                if (BidsCsvParser.STANDARD_ENTITIES.contains(key)) {
                    entities[key] = value
                }
            }
        }
        
        return entities
    }
    
    /**
     * Extract suffix from BIDS filename
     * 
     * @param filename BIDS filename
     * @return Suffix or null
     */
    static String extractSuffix(String filename) {
        def nameWithoutExt = filename.replaceAll(/\.(nii\.gz|nii|json|tsv|bval|bvec)$/, '')
        def parts = nameWithoutExt.split('_')
        
        // Last part is the suffix
        if (parts.length > 0) {
            return parts[-1]
        }
        
        return null
    }
}
