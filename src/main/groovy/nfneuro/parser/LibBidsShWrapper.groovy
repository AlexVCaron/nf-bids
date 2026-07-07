package nfneuro.plugin.parser

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.util.BidsLogger
import nfneuro.plugin.util.BidsErrorHandler

/**
 * Wrapper for the {@code libBIDS.sh} bash library.
 *
 * <p>Locates the {@code libBIDS.sh} script (embedded in the plugin distribution,
 * checked out as a git submodule, or provided explicitly by the caller), then
 * invokes {@code libBIDSsh_parse_bids_to_table} to produce a TSV file that is
 * subsequently read by {@link nfneuro.plugin.util.BidsCsvParser}.</p>
 *
 * <p>All user-supplied paths are validated against a shell-metacharacter deny-list
 * before being passed to the subprocess to prevent command injection.</p>
 */
@Slf4j
@CompileStatic
class LibBidsShWrapper {

    private static final String DEFAULT_LIBBIDS_SUBMODULE = "libBIDS.sh/libBIDS.sh"
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
     * Get the plugin's installation directory
     * This finds where the nf-bids plugin is installed (e.g., ~/.nextflow/plugins/nf-bids-{version}/)
     */
    private String getPluginLibPath() {
        // Try to find the plugin installation via the classpath
        def clazz = this.getClass()
        def classUrl = clazz.getResource("/${clazz.getName().replace('.', '/')}.class")

        if (classUrl != null) {
            def path = classUrl.toString()
            // Path will be like: jar:file:/home/user/.nextflow/plugins/nf-bids-0.1.0-beta.5/classes/nfneuro/parser/LibBidsShWrapper.class
            // or file:/home/user/.nextflow/plugins/nf-bids-0.1.0-beta.5/classes/nfneuro/parser/LibBidsShWrapper.class

            // Extract the plugin directory
            def pluginDirMatch = path =~ /(.+\/nf-bids-[^\/]+)\/(classes|lib)/
            if (pluginDirMatch.find()) {
                def pluginDir = pluginDirMatch.group(1)
                // Remove jar:file: or file: prefix
                pluginDir = pluginDir.replaceFirst(/^(jar:)?file:/, '')
                return "${pluginDir}/lib/libBIDS.sh"
            }
        }

        // Fallback: Try standard Nextflow plugin location
        def homeDir = System.getProperty('user.home')
        def pluginsDir = new File("${homeDir}/.nextflow/plugins")

        if (pluginsDir.exists()) {
            // Find nf-bids plugin directories
            def bidsPlugins = pluginsDir.listFiles()?.findAll { it.name.startsWith('nf-bids-') }

            if (bidsPlugins && bidsPlugins.size() > 0) {
                // Use the most recent version (sorted alphabetically, which works for semantic versioning)
                def latestPlugin = bidsPlugins.sort { it.name }.reverse()[0]
                def libBidsPath = new File(latestPlugin, 'lib/libBIDS.sh')

                if (libBidsPath.exists()) {
                    return libBidsPath.absolutePath
                }
            }
        }

        return null
    }

    /**
     * Parse BIDS directory to TSV table using libBIDS.sh
     *
     * Executes the bash script and captures TSV output
     *
     * @param bidsDir Path to BIDS dataset
     * @param libBidsShPath Path to libBIDS.sh (optional, auto-detects if not provided)
     * @return File containing parsed TSV data
     *
     * @reference libbids_sh_parse process implementation:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L1-L28
     */
    File parseBidsToTable(String bidsDir, String libBidsShPath = null, Boolean bidsignore = true, Boolean defaultIgnores = true) {
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

        def outputFile = File.createTempFile("bids_parsed_", ".tsv")
        outputFile.deleteOnExit()  // Clean up temp file on exit

        BidsLogger.logProgress("libBIDS-wrapper", "Executing libBIDS.sh parser on: ${bidsDir}")
        BidsLogger.logProgress("libBIDS-wrapper", "Using libBIDS.sh at: ${scriptPath}")
        BidsLogger.logProgress("libBIDS-wrapper", "Output TSV: ${outputFile.absolutePath}")

        try {
            def command = buildParseCommand(scriptPath, bidsDir, outputFile, bidsignore, defaultIgnores)
            BidsLogger.logProgress("libBIDS-wrapper", "Command: ${command}")

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

            BidsLogger.logProgress("libBIDS-wrapper",
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
        // FIRST: Try plugin installation directory (embedded libBIDS.sh)
        def pluginLibPath = getPluginLibPath()
        if (pluginLibPath) {
            def pluginScript = new File(pluginLibPath)
            if (pluginScript.exists() && pluginScript.canRead()) {
                BidsLogger.logProgress("libBIDS-wrapper",
                    "Found libBIDS.sh in plugin installation: ${pluginScript.absolutePath}")
                return pluginScript.absolutePath
            }
        }

        // Try search paths
        for (searchPath in SEARCH_PATHS) {
            def expandedPath = searchPath.replaceFirst('^~', System.getProperty('user.home'))
            def scriptFile = new File(expandedPath)

            if (scriptFile.exists() && scriptFile.canRead()) {
                BidsLogger.logProgress("libBIDS-wrapper",
                    "Found libBIDS.sh at: ${scriptFile.absolutePath}")
                return scriptFile.absolutePath
            }
        }

        // Try relative to working directory
        def workingDir = new File(System.getProperty('user.dir'))
        def relativeScript = new File(workingDir, DEFAULT_LIBBIDS_SUBMODULE)
        if (relativeScript.exists() && relativeScript.canRead()) {
            BidsLogger.logProgress("libBIDS-wrapper",
                "Found libBIDS.sh at: ${relativeScript.absolutePath}")
            return relativeScript.absolutePath
        }

        BidsLogger.logProgress("libBIDS-wrapper",
            "libBIDS.sh not found in standard locations or plugin installation")
        return null
    }

    /**
     * Build the bash command to execute libBIDS.sh
     *
     * Uses array form with proper escaping to prevent command injection
     *
     * @param scriptPath Path to libBIDS.sh (already validated)
     * @param bidsDir BIDS directory to parse (already validated)
     * @param outputFile Output TSV file
     * @return Command list for ProcessBuilder
     *
     * @reference Bash command structure:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf#L17-L24
     */
    private List<String> buildParseCommand(String scriptPath, String bidsDir, File outputFile, Boolean bidsignore, Boolean defaultIgnores = true) {
        // Additional validation for output file path
        validateShellPath(outputFile.absolutePath, "output file")
        List<String> extraArgs = new ArrayList<>()

        if (bidsignore) {
            BidsLogger.logProgress("libBIDS-wrapper", "BIDS validation is disabled (bidsignore=true)")
        } else {
            BidsLogger.logProgress("libBIDS-wrapper", "BIDS validation is enabled (bidsignore=false)")
            extraArgs.add("--no-bidsignore")
        }

        if (defaultIgnores) {
            BidsLogger.logProgress("libBIDS-wrapper", "Default ignores are enabled")
        } else {
            BidsLogger.logProgress("libBIDS-wrapper", "Default ignores are disabled")
            extraArgs.add("--no-default-ignores")
        }

        // Use array form with bash to avoid string interpolation risks
        return [
            'bash',
            '-c',
            'set -euo pipefail && source "$1" && libBIDSsh_parse_bids_to_table "$2" ${@:4} > "$3"',
            'bash',  // $0
            scriptPath,  // $1
            bidsDir,  // $2
            outputFile.absolutePath  // $3
        ] + extraArgs
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
     * Verify that a {@code libBIDS.sh} script exists, is readable, and contains the
     * expected {@code libBIDSsh_parse_bids_to_table} function signature.
     *
     * @param scriptPath absolute path to the candidate {@code libBIDS.sh} file
     * @return {@code true} if the script is usable; {@code false} otherwise (errors are logged)
     */
    boolean validateLibBidsScript(String scriptPath) {
        def script = new File(scriptPath)

        if (!script.exists()) {
            BidsLogger.logProgress("libBIDS-wrapper",
                "libBIDS.sh not found: ${scriptPath}")
            return false
        }

        if (!script.canRead()) {
            BidsLogger.logProgress("libBIDS-wrapper",
                "libBIDS.sh is not readable: ${scriptPath}")
            return false
        }

        // Check if it looks like libBIDS.sh (contains expected functions)
        def content = script.text
        if (!content.contains('libBIDSsh_parse_bids_to_table')) {
            BidsLogger.logProgress("libBIDS-wrapper",
                "File does not appear to be compatible libBIDS.sh (requires libBIDS.sh >= v2.0)")
            return false
        }

        return true
    }

    /**
     * Attempt to extract the version string from a {@code libBIDS.sh} script.
     *
     * <p>Looks for a {@code VERSION="x.y.z"} assignment near the top of the file.</p>
     *
     * @param scriptPath absolute path to the {@code libBIDS.sh} file
     * @return the version string (e.g. {@code "1.2.0"}), or {@code "unknown"} if not found or on any error
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
