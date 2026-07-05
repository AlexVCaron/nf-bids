package nfneuro.plugin.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile

/**
 * Tabular parser for the output of {@code libBIDSsh_parse_bids_to_table}.
 *
 * <p>Reads the header row to discover column indices, then converts each data row
 * into a {@link nfneuro.plugin.model.BidsFile} object with all BIDS entity fields
 * and the file path populated.  Columns correspond to BIDS entities plus
 * {@code suffix}, {@code extension}, and {@code path}.</p>
 */
@Slf4j
@CompileStatic
class BidsCsvParser {

    private List<String> headers
    private Map<String, Integer> headerIndex

    /**
     * Parse TSV table file from libBIDS.sh
     *
     * @param csvFile TSV file to parse
     * @return List of BidsFile objects
     */
    List<BidsFile> parse(File csvFile) {
        if (!csvFile.exists()) {
            throw new FileNotFoundException("TSV file not found: ${csvFile}")
        }

        List<BidsFile> files = []
        def lines = csvFile.readLines()

        if (lines.isEmpty()) {
            log.warn("TSV file is empty: ${csvFile}")
            return files
        }

        // Parse header
        parseHeader(lines[0])

        // Debug: log first few lines
        log.info("TSV Header: ${lines[0]}")
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

        log.info("Parsed ${files.size()} BIDS files from TSV with suffixes: ${suffixCounts}")
        return files
    }

    /**
     * Parse TSV header line
     *
     * Expected format: derivatives\tdatatype\tsubject\tsession\t...\tsuffix\textension\tpath
     *
     * @param headerLine First line of TSV
     */
    private void parseHeader(String headerLine) {
        headers = headerLine.split('\t', -1).collect { it.trim() }
        headerIndex = [:]

        headers.eachWithIndex { header, idx ->
            headerIndex[header] = idx
        }

        log.debug("TSV headers: ${headers}")
    }

    /**
     * Parse a single TSV row into a BidsFile
     *
     * @param line TSV line
     * @return BidsFile or null if invalid
     */
    BidsFile parseRow(String line) {
        def values = parseTsvLine(line)

        if (values.size() != headers.size()) {
            log.warn("Invalid TSV row (column count mismatch): ${line}")
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
            // First, process TSV columns that need mapping (e.g., 'subject' -> 'sub')
            BidsEntity.SHORT_ENTITY_MAPPING.each { csvColumn, entityName ->
                def value = getValue(values, csvColumn)
                if (value && value != 'NA') {
                    // Strip entity prefix (e.g., "sub-Sub103" -> "Sub103")
                    def cleanValue = stripEntityPrefix(value, entityName ?: csvColumn)
                    log.info("Mapping TSV column '${csvColumn}' to entity '${entityName ?: csvColumn}' with value '${cleanValue}'")
                    bidsFile.addEntity(entityName ?: csvColumn, cleanValue)
                }
            }

            // // Then, process columns that match entity names directly
            // BidsEntity.SHORT_ENTITIES.each { entity ->
            //     // Skip if already added from TSV mapping
            //     if (!bidsFile.hasEntity(entity)) {
            //         def value = getValue(values, entity)
            //         if (value && value != 'NA') {
            //             // Strip entity prefix (e.g., "run-01" -> "01")
            //             def cleanValue = stripEntityPrefix(value, entity)
            //             bidsFile.addEntity(entity, cleanValue)
            //         }
            //     }
            // }

            // Add metadata
            def dataType = getValue(values, 'datatype')
            if (dataType && dataType != 'NA') {
                bidsFile.addMetadata('datatype', dataType)
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
            log.error("Failed to parse TSV row: ${line}", e)
            return null
        }
    }

    /**
     * Parse TSV line
     *
     * @param line TSV line
     * @return List of values
     */
    private List<String> parseTsvLine(String line) {
        return line.split('\t', -1).collect { it.trim() }
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
     * @param value Entity value from TSV row (may include prefix)
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

        BidsEntity.SHORT_ENTITIES.each { entity ->
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
