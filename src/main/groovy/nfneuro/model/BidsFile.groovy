package nfneuro.plugin.model

import java.nio.file.Files
import java.nio.file.Paths
import groovy.transform.CompileStatic

/**
 * Represents a BIDS file with metadata
 * 
 * @reference BIDS file structure from parsing: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/parsers/lib_bids_sh_parser.nf
 */
@CompileStatic
class BidsFile {
    
    String path
    String suffix
    Map<String, String> entities
    Map<String, Object> metadata
    
    // Optional file metadata
    Long fileSize
    Long lastModified
    
    // Associated files (JSON sidecar, TSV files, etc.)
    String jsonSidecar
    List<String> associatedFiles
    
    BidsFile(String path) {
        if (!path) {
            throw new IllegalArgumentException("File path cannot be null or empty")
        }
        this.path = path
        this.entities = [:]
        this.metadata = [:]
        this.associatedFiles = []
        
        // Extract suffix from filename if possible
        extractSuffixFromPath()
    }
    
    /**
     * Extract suffix from file path
     * e.g., sub-01_T1w.nii.gz -> suffix = "T1w"
     */
    private void extractSuffixFromPath() {
        def filename = new File(path).name
        // Remove extensions (.nii.gz, .nii, .json, etc.)
        def nameWithoutExt = filename.replaceAll(/\.(nii\.gz|nii|json|tsv|bval|bvec)$/, '')
        // Extract last part after underscore (suffix)
        def parts = nameWithoutExt.split('_')
        if (parts.length > 0) {
            this.suffix = parts[-1]
        }
    }
    
    /**
     * Get entity value by name
     * 
     * @param entityName Entity name (e.g., "sub", "ses")
     * @return Entity value or "NA" if not present
     */
    String getEntity(String entityName) {
        return entities.getOrDefault(entityName, "NA")
    }
    
    /**
     * Add entity to file
     * 
     * @param name Entity name
     * @param value Entity value
     */
    void addEntity(String name, String value) {
        if (value && value != "NA") {
            entities[name] = value
        }
    }
    
    /**
     * Check if entity exists
     * 
     * @param entityName Entity name
     * @return true if entity is present and not "NA"
     */
    boolean hasEntity(String entityName) {
        def value = entities[entityName]
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
     * Get parent directory
     * 
     * @return Parent directory path
     */
    String getParentDir() {
        return new File(path).parent
    }
    
    /**
     * Get sidecar path (alias for jsonSidecar for compatibility)
     * 
     * @return JSON sidecar path or null
     */
    String getSidecarPath() {
        return jsonSidecar
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
}
