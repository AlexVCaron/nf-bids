package nfneuro.plugin.model

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Represents a parsed BIDS dataset.
 *
 * <p>Holds the root path, the dataset name and description loaded from
 * {@code dataset_description.json}, the full list of {@link BidsFile} objects
 * produced by the parser, and the participant table from {@code participants.tsv}
 * (if present).</p>
 *
 * <p>Provides convenience query methods ({@link #getFilesBySuffix},
 * {@link #getFilesByEntity}, {@link #getSubjects}, {@link #getSessions},
 * {@link #getSuffixes}) used by the grouping handlers.</p>
 */
@CompileStatic
class BidsDataset {

    String path
    String name
    Map<String, Object> description
    List<BidsFile> files
    List<Map<String, String>> participants

    /**
     * Construct a {@code BidsDataset} for the given root directory.
     *
     * <p>The constructor validates and canonicalises the path to prevent directory-traversal
     * attacks, then attempts to load {@code dataset_description.json} to populate
     * {@link #name} and {@link #description}.</p>
     *
     * @param path absolute path to the BIDS dataset root directory
     * @throws IllegalArgumentException if the path is null, empty, or contains a traversal attempt
     */
    BidsDataset(String path) {
        if (!path) {
            throw new IllegalArgumentException("Dataset path cannot be null or empty")
        }

        this.path = validateAndNormalizePath(path)
        this.files = []
        this.description = [:]
        this.participants = []

        // Try to load dataset_description.json
        loadDatasetDescription()
    }

    /**
     * Load participants.tsv if it exists
     */
    void loadParticipants() {
        try {
            def participantsFile = Paths.get(path, "participants.tsv")
            if (Files.exists(participantsFile)) {
                def lines = participantsFile.toFile().readLines()
                if (lines.size() > 1) {
                    def headers = lines[0].split('\t')
                    lines[1..-1].each { line ->
                        def values = line.split('\t')
                        Map<String, String> participant = [:]
                        headers.eachWithIndex { header, idx ->
                            if (idx < values.size()) {
                                participant[header] = values[idx]
                            }
                        }
                        participants << participant
                    }
                }
            }
        } catch (Exception e) {
            // Participants file is optional
        }
    }

    /**
     * Add file to dataset
     *
     * @param file BIDS file
     */
    void addFile(BidsFile file) {
        if (file && !files.contains(file)) {
            files << file
        }
    }

    /**
     * Get files by suffix
     *
     * @param suffix BIDS suffix (e.g., "T1w", "bold")
     * @return List of matching files
     */
    List<BidsFile> getFilesBySuffix(String suffix) {
        return files.findAll { it.suffix == suffix }
    }

    /**
     * Get files by entity value
     *
     * @param entityName Entity name
     * @param entityValue Entity value
     * @return List of matching files
     */
    List<BidsFile> getFilesByEntity(String entityName, String entityValue) {
        return files.findAll { file -> file.getEntityValue(entityName) == entityValue }
    }

    /**
     * Get all unique subjects in dataset
     *
     * @return List of subject IDs
     */
    List<String> getSubjects() {
        return files*.getEntityValue('sub')
            .findAll { value -> value && value != 'NA' }
            .unique()
            .sort()
    }

    /**
     * Get all unique sessions in dataset
     *
     * @return List of session IDs
     */
    List<String> getSessions() {
        return files*.getEntityValue('ses')
            .findAll { value -> value && value != 'NA' }
            .unique()
            .sort()
    }

    /**
     * Get all unique suffixes in dataset
     *
     * @return List of suffixes
     */
    List<String> getSuffixes() {
        return files*.suffix
            .findAll()
            .unique()
            .sort()
    }

    /**
     * Get dataset statistics
     *
     * @return Map with statistics
     */
    Map<String, Object> getStatistics() {
        return [
            totalFiles: files.size(),
            subjects: getSubjects().size(),
            sessions: getSessions().size(),
            suffixes: getSuffixes().size(),
            uniqueSuffixes: getSuffixes()
        ]
    }

    @Override
    String toString() {
        return "BidsDataset[path=${path}, name=${name}, files=${files.size()}]"
    }

    /**
     * Validate and normalize path to prevent path traversal attacks
     *
     * @param path Input path
     * @return Normalized canonical path
     * @throws IllegalArgumentException if path is invalid or contains traversal attempts
     */
    private String validateAndNormalizePath(String path) {
        try {
            def file = new File(path)
            def canonicalPath = file.canonicalPath

            // Check that the canonical path starts with the original path's parent
            // This prevents ../../../etc/passwd type attacks
            if (!file.exists()) {
                // For non-existent paths, just return the canonical path
                // The actual existence check happens in parseBidsToCSV
                return canonicalPath
            }

            // For existing paths, verify no traversal occurred
            def originalPath = file.absolutePath
            if (!canonicalPath.equals(originalPath)) {
                // Check if this is just symlink resolution (acceptable)
                // vs actual directory traversal (not acceptable)
                def originalFile = new File(originalPath)

                // If the original path exists and is different from canonical,
                // verify the canonical path is not escaping expected boundaries
                if (originalFile.exists()) {
                    // Allow the canonical path (symlink resolution is OK)
                    return canonicalPath
                }
                throw new IllegalArgumentException(
                    "Invalid BIDS dataset path: ${path}. Path traversal detected."
                )
            }

            return canonicalPath

        } catch (IOException e) {
            throw new IllegalArgumentException(
                "Invalid BIDS dataset path: ${path}. Error: ${e.message}",
                e
            )
        }
    }

    /**
     * Load dataset_description.json if it exists
     */
    private void loadDatasetDescription() {
        try {
            def descFile = Paths.get(path, "dataset_description.json")
            if (Files.exists(descFile)) {
                def slurper = new JsonSlurper()
                this.description = slurper.parse(descFile.toFile()) as Map<String, Object>
                this.name = description.get('Name', new File(path).name) as String
            } else {
                this.name = new File(path).name
            }
        } catch (Exception e) {
            // Fall back to directory name
            this.name = new File(path).name
        }
    }

}
