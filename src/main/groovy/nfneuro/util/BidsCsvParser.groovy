package nfneuro.plugin.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.model.BidsFile

/**
 * CSV parser for libBIDS.sh output
 * 
 * Parses the CSV format produced by libBIDSsh_parse_bids_to_csv
 * 
 * @reference CSV format:
 *            derivatives,data_type,subject,session,sample,task,...,suffix,extension,path
 *            https://github.com/AlexVCaron/bids2nf/blob/main/libBIDS.sh/libBIDS.sh#L503-L524
 */
@Slf4j
@CompileStatic
class BidsCsvParser {
    
    // Standard BIDS entities in order from libBIDS.sh
    static final List<String> STANDARD_ENTITIES = [
        'sub', 'ses', 'sample', 'task', 'tracksys', 'acq', 'nuc', 'voi',
        'ce', 'trc', 'stain', 'rec', 'dir', 'run', 'mod', 'echo', 'flip',
        'inv', 'mt', 'part', 'proc', 'hemi', 'space', 'split', 'recording',
        'chunk', 'seg', 'res', 'den', 'label', 'desc'
    ]
    
    // Mapping from CSV column names (from libBIDS.sh) to BIDS entity short names
    static final Map<String, String> CSV_TO_ENTITY_MAP = [
        'subject': 'sub',
        'session': 'ses',
        'acquisition': 'acq',
        'ceagent': 'ce',
        'tracer': 'trc',
        'reconstruction': 'rec',
        'direction': 'dir',
        'modality': 'mod',
        'flip': 'flip',
        'inversion': 'inv',
        'mtransfer': 'mt',
        'processing': 'proc',
        'hemisphere': 'hemi',
        'segmentation': 'seg',
        'resolution': 'res',
        'density': 'den',
        'description': 'desc',
        'nucleus': 'nuc',
        'volume': 'voi'
    ]
    
    private List<String> headers
    private Map<String, Integer> headerIndex
    
    /**
     * Parse CSV file from libBIDS.sh
     * 
     * @param csvFile CSV file to parse
     * @return List of BidsFile objects
     */
    List<BidsFile> parse(File csvFile) {
        if (!csvFile.exists()) {
            throw new FileNotFoundException("CSV file not found: ${csvFile}")
        }
        
        List<BidsFile> files = []
        def lines = csvFile.readLines()
        
        if (lines.isEmpty()) {
            log.warn("CSV file is empty: ${csvFile}")
            return files
        }
        
        // Parse header
        parseHeader(lines[0])
        
        // Debug: log first few lines
        log.info("CSV Header: ${lines[0]}")
        if (lines.size() > 1) {
            log.info("First data row: ${lines[1]}")
        }
        
        // Parse data rows
        def suffixCounts = [:].withDefault { 0 }
        lines[1..-1].each { line ->
            if (line.trim()) {
                def bidsFile = parseRow(line)
                if (bidsFile) {
                    files << bidsFile
                    if (bidsFile.suffix) {
                        suffixCounts[bidsFile.suffix] = (suffixCounts[bidsFile.suffix] ?: 0) + 1
                    }
                }
            }
        }
        
        log.info("Parsed ${files.size()} BIDS files from CSV with suffixes: ${suffixCounts}")
        return files
    }
    
    /**
     * Parse CSV header line
     * 
     * Expected format: derivatives,data_type,subject,session,...,suffix,extension,path
     * 
     * @param headerLine First line of CSV
     */
    private void parseHeader(String headerLine) {
        headers = headerLine.split(',').collect { it.trim() }
        headerIndex = [:]
        
        headers.eachWithIndex { header, idx ->
            headerIndex[header] = idx
        }
        
        log.debug("CSV headers: ${headers}")
    }
    
    /**
     * Parse a single CSV row into a BidsFile
     * 
     * @param line CSV line
     * @return BidsFile or null if invalid
     */
    BidsFile parseRow(String line) {
        def values = parseCsvLine(line)
        
        if (values.size() != headers.size()) {
            log.warn("Invalid CSV row (column count mismatch): ${line}")
            return null
        }
        
        try {
            def path = getValue(values, 'path')
            if (!path || path == 'NA') {
                log.warn("Row missing path: ${line}")
                return null
            }
            
            def bidsFile = new BidsFile(path)
            
            // Extract suffix
            def suffix = getValue(values, 'suffix')
            if (suffix && suffix != 'NA') {
                bidsFile.suffix = suffix
            }
            
            // Extract all BIDS entities
            // First, process CSV columns that need mapping (e.g., 'subject' -> 'sub')
            CSV_TO_ENTITY_MAP.each { csvColumn, entityName ->
                def value = getValue(values, csvColumn)
                if (value && value != 'NA') {
                    // Strip entity prefix (e.g., "sub-Sub103" -> "Sub103")
                    def cleanValue = stripEntityPrefix(value, entityName)
                    bidsFile.addEntity(entityName, cleanValue)
                }
            }
            
            // Then, process columns that match entity names directly
            STANDARD_ENTITIES.each { entity ->
                // Skip if already added from CSV mapping
                if (!bidsFile.hasEntity(entity)) {
                    def value = getValue(values, entity)
                    if (value && value != 'NA') {
                        // Strip entity prefix (e.g., "run-01" -> "01")
                        def cleanValue = stripEntityPrefix(value, entity)
                        bidsFile.addEntity(entity, cleanValue)
                    }
                }
            }
            
            // Add metadata
            def dataType = getValue(values, 'data_type')
            if (dataType && dataType != 'NA') {
                bidsFile.addMetadata('data_type', dataType)
            }
            
            def derivatives = getValue(values, 'derivatives')
            if (derivatives && derivatives != 'NA') {
                bidsFile.addMetadata('derivatives', derivatives)
            }
            
            def extension = getValue(values, 'extension')
            if (extension && extension != 'NA') {
                bidsFile.addMetadata('extension', extension)
            }
            
            return bidsFile
            
        } catch (Exception e) {
            log.error("Failed to parse CSV row: ${line}", e)
            return null
        }
    }
    
    /**
     * Parse CSV line handling quoted values
     * 
     * @param line CSV line
     * @return List of values
     */
    private List<String> parseCsvLine(String line) {
        List<String> values = []
        def currentValue = new StringBuilder()
        def inQuotes = false
        
        for (int i = 0; i < line.length(); i++) {
            def c = line.charAt(i)
            if (c == '"' as char) {
                inQuotes = !inQuotes
            } else if (c == ',' as char && !inQuotes) {
                values << currentValue.toString().trim()
                currentValue = new StringBuilder()
            } else {
                currentValue.append(c)
            }
        }
        
        // Add last value
        values << currentValue.toString().trim()
        
        return values
    }
    
    /**
     * Strip BIDS entity prefix from value
     * 
     * Always strips the entity prefix for clean internal storage.
     * Prefixes will be added back when needed (e.g., for enriched data output).
     * 
     * Examples:
     *   - "sub-invivo1" -> "invivo1"
     *   - "run-01" -> "01"
     *   - "flip-02" -> "02"
     *   - "mt-off" -> "off"
     * 
     * @param value Entity value from CSV (may include prefix)
     * @param entityName Entity key name (sub, ses, run, flip, mt, etc.)
     * @return Cleaned value without prefix
     */
    private String stripEntityPrefix(String value, String entityName) {
        if (!value || value == 'NA') {
            return value
        }
        
        // Check if value starts with "entityName-"
        def prefix = "${entityName}-"
        if (value.startsWith(prefix)) {
            return value.substring(prefix.length())
        }
        
        // Value doesn't have prefix, return as-is
        return value
    }
    
    /**
     * Get value from parsed row by header name
     * 
     * @param values Parsed row values
     * @param headerName Header name
     * @return Value or null
     */
    private String getValue(List<String> values, String headerName) {
        def idx = headerIndex[headerName]
        if (idx == null || idx >= values.size()) {
            return null
        }
        return values[idx]
    }
    
    /**
     * Get all entity values from a row
     * 
     * @param values Parsed row values
     * @return Map of entity name to value
     */
    Map<String, String> getEntities(List<String> values) {
        Map<String, String> entities = [:]
        
        STANDARD_ENTITIES.each { entity ->
            def value = getValue(values, entity)
            if (value && value != 'NA') {
                entities[entity] = value
            }
        }
        
        return entities
    }
    
    /**
     * Convert parsed row to map
     * 
     * @param values Parsed row values
     * @return Map of header to value
     */
    Map<String, String> rowToMap(List<String> values) {
        Map<String, String> map = [:]
        
        headers.eachWithIndex { header, idx ->
            if (idx < values.size()) {
                def value = values[idx]
                if (value && value != 'NA') {
                    map[header] = value
                }
            }
        }
        
        return map
    }
}
