package nfneuro.plugin.model

import java.nio.file.Files
import java.nio.file.Paths
import groovy.transform.CompileStatic

/**
 * Represents a single file entry in a BIDS dataset.
 *
 * <p>Stores the file path, BIDS suffix (e.g. {@code "T1w"}, {@code "bold"}),
 * the list of {@link BidsEntity} key–value pairs parsed from the filename,
 * optional JSON sidecar and other associated file paths, and file-system
 * metadata (size, modification time) loaded on demand.</p>
 *
 * <p>Instances are created by {@link nfneuro.plugin.util.BidsCsvParser} from
 * the TSV output of {@code libBIDSsh_parse_bids_to_table}.  The
 * {@link #isPrimaryFile()} and {@link #getBasename()} helpers drive grouping
 * logic in the set handlers.</p>
 */
@CompileStatic
class BidsFile {

    public static final Map<String, String> VALID_EXTENSIONS = [
        'nii': '',
        'nii.gz': 'nii',
        'json': '',
        'bval': '',
        'bvec': '',
        'tsv': '',
        'txt': '',
        'edf': '',
        'eeg': ''
    ]

    public static final List<String> PRIMARY_EXTENSIONS = [
        'nii.gz',
        'nii',
        'tsv',
        'edf',
        'eeg'
    ]

    public static final List<String> TYPE_ALLOWING_PARTS = [
        'nii',
        'nii.gz'
    ]

    String path
    String suffix
    List<BidsEntity> entities
    Map<String, Object> metadata

    // Optional file metadata
    Long fileSize
    Long lastModified

    // Associated files (JSON sidecar, TSV files, etc.)
    String jsonSidecar
    List<String> associatedFiles

    public static boolean typeAllowsParts(String type) {
        return TYPE_ALLOWING_PARTS.contains(type)
    }

    /**
     * Construct a {@code BidsFile} for the given path.
     *
     * <p>The BIDS suffix is extracted automatically from the filename if possible.</p>
     *
     * @param path absolute or relative path to the BIDS file
     * @throws IllegalArgumentException if {@code path} is null or empty
     */
    BidsFile(String path) {
        if (!path) {
            throw new IllegalArgumentException("File path cannot be null or empty")
        }
        this.path = path
        this.entities = []
        this.metadata = [:]
        this.associatedFiles = []

        // Extract suffix from filename if possible
        extractSuffixFromPath()
    }

    BidsEntity getEntity(String name) {
        return entities.find { entity -> entity.name == name }
    }

    /**
     * Get entity value by name
     *
     * @param entityName Entity name (e.g., "sub", "ses")
     * @return Entity value or "NA" if not present
     */
    String getEntityValue(String entityName) {
        return this.getEntity(entityName)?.value ?: "NA"
    }

    /**
     * Add entity to file
     *
     * @param name Entity name
     * @param value Entity value
     */
    void addEntity(String name, String value) {
        if (value && value != "NA") {
            this.addEntity(new BidsEntity(name, value))
        }
    }

    void addEntity(BidsEntity entity) {
        if (this.hasEntity(entity.name)) {
            this.getEntity(entity.name).value = entity.value
        }
        else {
            this.entities << entity
        }
    }

    /**
     * Check if entity exists
     *
     * @param entityName Entity name
     * @return true if entity is present and not "NA"
     */
    boolean hasEntity(String entityName) {
        String value = this.getEntityValue(entityName)
        return value && value != "NA"
    }

    /**
     * Get metadata value by key
     *
     * @param key Metadata key
     * @return Metadata value or null
     */
    Object getMetadata(String key) {
        return metadata[key]
    }

    /**
     * Add metadata to file
     *
     * @param key Metadata key
     * @param value Metadata value
     */
    void addMetadata(String key, Object value) {
        metadata[key] = value
    }

    /**
     * Load file metadata (size, modification time)
     */
    void loadFileMetadata() {
        try {
            def filePath = Paths.get(path)
            if (Files.exists(filePath)) {
                this.fileSize = Files.size(filePath)
                this.lastModified = Files.getLastModifiedTime(filePath).toMillis()
            }
        } catch (Exception e) {
            // Silently ignore - metadata is optional
        }
    }

    /**
     * Add associated file (JSON sidecar, etc.)
     *
     * @param filePath Path to associated file
     */
    void addAssociatedFile(String filePath) {
        if (filePath && !associatedFiles.contains(filePath)) {
            associatedFiles << filePath

            // Track JSON sidecar specifically
            if (filePath.endsWith('.json')) {
                this.jsonSidecar = filePath
            }
        }
    }

    /**
     * Get filename without path
     *
     * @return Filename
     */
    String getFilename() {
        return new File(path).name
    }

    /**
     * Get the base name of a file without extension
     */
    String getBasename() {
        String regex = VALID_EXTENSIONS.keySet().join('|').replaceAll('\\.', '\\\\.')
        /* groovylint-disable-next-line JavaIoPackageAccess */
        return new File(path).name.replaceAll(/\.($regex)$/, '')
    }

    /**
     * Get the base name of a file without extension
     */
    String getBasename(List<String> excludeEntities) {
        String basename = getBasename()

        excludeEntities.each { entityName ->
            BidsEntity entity = getEntity(entityName)
            if (entity) {
                String entityPattern = "_${entity.name}-${entity.value}"
                basename = basename.replace(entityPattern, '')
            }
        }

        return basename
    }

    /**
     * Get extension type for categorization
     */
    String getExtensionType() {
        def filename = new File(path).name
        for (ext in VALID_EXTENSIONS.keySet()) {
            if (filename.endsWith(".${ext}")) {
                return ext
            }
        }
        return null
    }

    String getType() {
        String extensionType = getExtensionType()
        return VALID_EXTENSIONS[extensionType] ?: extensionType
    }

    /**
     * Get parent directory
     *
     * @return Parent directory path
     */
    String getParentDir() {
        return new File(path).parent
    }

    /**
     * Convert absolute path to relative path from base path
     */
    String relativeTo(String basePath) {
        def base = Paths.get(basePath).toAbsolutePath()
        def filePath = Paths.get(path).toAbsolutePath()
        return base.relativize(filePath).toString()
    }

    /**
     * Get sidecar path (alias for jsonSidecar for compatibility)
     *
     * @return JSON sidecar path or null
     */
    String getSidecarPath() {
        return jsonSidecar
    }

    boolean isPrimaryFile() {
        return PRIMARY_EXTENSIONS.contains(getExtensionType())
    }

    @Override
    String toString() {
        return path
    }

    @Override
    boolean equals(Object obj) {
        if (this.is(obj)) return true
        if (!(obj instanceof BidsFile)) return false
        BidsFile other = (BidsFile) obj
        return path == other.path
    }

    @Override
    int hashCode() {
        return Objects.hash(path)
    }

    /**
     * Extract suffix from file path
     * e.g., sub-01_T1w.nii.gz -> suffix = "T1w"
     */
    private void extractSuffixFromPath() {
        def filename = new File(path).name
        // Remove extensions (.nii.gz, .nii, .json, etc.)
        def nameWithoutExt = filename.replaceAll(".${getExtensionType()}", '')
        // Extract last part after underscore (suffix)
        def parts = nameWithoutExt.split('_')
        if (parts.length > 0) {
            this.suffix = parts[-1]
        }
    }

}
