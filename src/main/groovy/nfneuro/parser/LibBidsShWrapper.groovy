package nfneuro.plugin.parser

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.util.BidsErrorHandler

/**
 * Wrapper for libBIDS.sh bash library
 * 
 * Executes external libBIDS.sh script to parse BIDS datasets
 * and handles the interface between Groovy and Bash
 * 
 * @reference Bash parser wrapper: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf
 */
@Slf4j
@CompileStatic
class LibBidsShWrapper {
    
    private static final String DEFAULT_LIBBIDS_SUBMODULE = "libBIDS.sh/libBIDS.sh"
    private static final String DEFAULT_LIBBIDS_BUNDLED = "assets/libBIDS.sh"
    private static final List<String> SEARCH_PATHS = [
        "lib/libBIDS.sh",
        "libBIDS.sh/libBIDS.sh",           // Submodule in current dir
        "../libBIDS.sh/libBIDS.sh",        // Submodule one level up
        "../../libBIDS.sh/libBIDS.sh",     // Submodule two levels up
        "../../../libBIDS.sh/libBIDS.sh",  // Submodule three levels up (for plugin validation/)
        "/usr/local/bin/libBIDS.sh",       // System install
        "~/.local/bin/libBIDS.sh"          // User install
    ]
    
    /**
     * Parse BIDS directory to CSV using libBIDS.sh
     * 
     * Executes the bash script and captures CSV output
     * 
     * @param bidsDir Path to BIDS dataset
     * @param libBidsShPath Path to libBIDS.sh (optional, auto-detects if not provided)
     * @return File containing parsed CSV data
     * 
     * @reference libbids_sh_parse process implementation: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L1-L28
     */
    File parseBidsToCSV(String bidsDir, String libBidsShPath = null) {
        // Validate inputs early to prevent command injection
        if (libBidsShPath) {
            validateShellPath(libBidsShPath, "libBIDS.sh script")
        }
        if (bidsDir) {
            validateShellPath(bidsDir, "BIDS directory")
        }
        
        // Find libBIDS.sh script
        def scriptPath = libBidsShPath ?: findLibBidsScript()
        
        if (!scriptPath) {
            throw new FileNotFoundException(
                BidsErrorHandler.createDetailedError(
                    "libBIDS.sh script not found",
                    [
                        "Ensure the libBIDS.sh submodule is initialized: git submodule update --init",
                        "Provide explicit path via --libbids_sh parameter",
                        "Install libBIDS.sh to a standard location (/usr/local/bin or ~/.local/bin)"
                    ]
                )
            )
        }
        
        // Validate BIDS directory
        def bidsPath = new File(bidsDir)
        if (!bidsPath.exists()) {
            throw new FileNotFoundException("BIDS directory not found: ${bidsDir}")
        }
        if (!bidsPath.isDirectory()) {
            throw new IllegalArgumentException("BIDS path is not a directory: ${bidsDir}")
        }
        
        def outputFile = File.createTempFile("bids_parsed_", ".csv")
        outputFile.deleteOnExit()  // Clean up temp file on exit
        
        BidsLogger.logProgress("LibBidsShWrapper", "Executing libBIDS.sh parser on: ${bidsDir}")
        BidsLogger.logDebug("LibBidsShWrapper", "Using libBIDS.sh at: ${scriptPath}")
        BidsLogger.logProgress("LibBidsShWrapper", "Output CSV: ${outputFile.absolutePath}")
        
        try {
            def command = buildParseCommand(scriptPath, bidsDir, outputFile)
            BidsLogger.logDebug("LibBidsShWrapper", "Command: ${command}")
            
            def process = command.execute()
            
            // Capture output
            def stdout = new StringBuilder()
            def stderr = new StringBuilder()
            process.consumeProcessOutput(stdout, stderr)
            
            def exitCode = process.waitFor()
            
            if (exitCode != 0) {
                def errorMsg = "libBIDS.sh parsing failed with exit code ${exitCode}"
                if (stderr.length() > 0) {
                    errorMsg += ":\n${stderr}"
                }
                throw new RuntimeException(errorMsg)
            }
            
            // Verify output file was created and has content
            if (!outputFile.exists()) {
                throw new RuntimeException("libBIDS.sh did not create output file")
            }
            
            if (outputFile.length() == 0) {
                throw new RuntimeException(
                    "libBIDS.sh produced empty output - dataset may be empty or invalid"
                )
            }
            
            BidsLogger.logSuccess("LibBidsShWrapper", 
                "BIDS parsing completed: ${outputFile.length()} bytes written")
            
            return outputFile
            
        } catch (IOException e) {
            throw new RuntimeException(
                BidsErrorHandler.createDetailedError(
                    "Failed to execute libBIDS.sh: ${e.message}",
                    [
                        "Check that bash is available in PATH",
                        "Verify libBIDS.sh has read permissions",
                        "Ensure BIDS directory is accessible",
                        "Check disk space for temporary files"
                    ]
                ),
                e
            )
        }
    }
    
    /**
     * Find libBIDS.sh script in standard locations
     * 
     * Searches for libBIDS.sh in multiple common locations
     * 
     * @return Path to libBIDS.sh or null if not found
     */
    private String findLibBidsScript() {
        // Try search paths
        for (searchPath in SEARCH_PATHS) {
            def expandedPath = searchPath.replaceFirst('^~', System.getProperty('user.home'))
            def scriptFile = new File(expandedPath)
            
            if (scriptFile.exists() && scriptFile.canRead()) {
                BidsLogger.logDebug("LibBidsShWrapper", 
                    "Found libBIDS.sh at: ${scriptFile.absolutePath}")
                return scriptFile.absolutePath
            }
        }
        
        // Try relative to working directory
        def workingDir = new File(System.getProperty('user.dir'))
        def relativeScript = new File(workingDir, DEFAULT_LIBBIDS_SUBMODULE)
        if (relativeScript.exists() && relativeScript.canRead()) {
            BidsLogger.logDebug("LibBidsShWrapper", 
                "Found libBIDS.sh at: ${relativeScript.absolutePath}")
            return relativeScript.absolutePath
        }
        
        BidsLogger.logWarning("LibBidsShWrapper", 
            "libBIDS.sh not found in standard locations")
        return null
    }
    
    /**
     * Build the bash command to execute libBIDS.sh
     * 
     * Uses array form with proper escaping to prevent command injection
     * 
     * @param scriptPath Path to libBIDS.sh (already validated)
     * @param bidsDir BIDS directory to parse (already validated)
     * @param outputFile Output CSV file
     * @return Command list for ProcessBuilder
     * 
     * @reference Bash command structure: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L17-L24
     */
    private List<String> buildParseCommand(String scriptPath, String bidsDir, File outputFile) {
        // Additional validation for output file path
        validateShellPath(outputFile.absolutePath, "output file")
        
        // Use array form with bash to avoid string interpolation risks
        return [
            'bash',
            '-c',
            'set -euo pipefail && source "$1" && libBIDSsh_parse_bids_to_csv "$2" > "$3"',
            'bash',  // $0
            scriptPath,  // $1
            bidsDir,  // $2
            outputFile.absolutePath  // $3
        ]
    }
    
    /**
     * Validate that a path doesn't contain shell metacharacters
     * 
     * Prevents command injection by ensuring paths are safe
     * 
     * @param path Path to validate
     * @param description Description for error messages
     * @throws IllegalArgumentException if path contains dangerous characters
     */
    private void validateShellPath(String path, String description) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("${description} path cannot be empty")
        }
        
        // Check for dangerous shell metacharacters
        // Allow: alphanumeric, /, ., -, _, ~, space
        // Disallow: ; & | $ ` ( ) < > " ' \ and newlines
        def dangerousChars = /[;&|$`()<>"'\\]/
        if (path =~ dangerousChars) {
            throw new IllegalArgumentException(
                "${description} contains dangerous characters: ${path}\n" +
                "Paths must not contain shell metacharacters: ; & | \$ ` ( ) < > \" ' \\"
            )
        }
        
        // Check for newlines/control characters
        if (path =~ /[\n\r\t]/) {
            throw new IllegalArgumentException(
                "${description} contains control characters"
            )
        }
    }
    
    /**
     * Validate libBIDS.sh script exists and is executable
     * 
     * @param scriptPath Path to libBIDS.sh
     * @return true if valid, false otherwise
     */
    boolean validateLibBidsScript(String scriptPath) {
        def script = new File(scriptPath)
        
        if (!script.exists()) {
            BidsLogger.logError("LibBidsShWrapper", 
                "libBIDS.sh not found: ${scriptPath}")
            return false
        }
        
        if (!script.canRead()) {
            BidsLogger.logError("LibBidsShWrapper", 
                "libBIDS.sh is not readable: ${scriptPath}")
            return false
        }
        
        // Check if it looks like libBIDS.sh (contains expected functions)
        def content = script.text
        if (!content.contains('libBIDSsh_parse_bids_to_csv')) {
            BidsLogger.logWarning("LibBidsShWrapper", 
                "File does not appear to be libBIDS.sh (missing expected functions)")
            return false
        }
        
        return true
    }
    
    /**
     * Get version of libBIDS.sh if available
     * 
     * @param scriptPath Path to libBIDS.sh
     * @return Version string or "unknown"
     */
    String getLibBidsVersion(String scriptPath) {
        try {
            def script = new File(scriptPath)
            if (!script.exists()) {
                return "unknown"
            }
            
            // Look for version in script
            def content = script.text
            def versionPattern = /VERSION=["']?([0-9.]+)["']?/
            def matcher = content =~ versionPattern
            
            if (matcher.find()) {
                return matcher.group(1)
            }
            
            return "unknown"
        } catch (Exception e) {
            return "unknown"
        }
    }
}
