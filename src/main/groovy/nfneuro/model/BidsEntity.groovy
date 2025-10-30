package nfneuro.plugin.model

import groovy.transform.CompileStatic

/**
 * Represents a BIDS entity (subject, session, run, task, etc.)
 * 
 * @reference BIDS entity specification: 
 *            https://bids-specification.readthedocs.io/en/stable/
 */
@CompileStatic
class BidsEntity {
    
    // Known BIDS entities from specification
    // https://bids-specification.readthedocs.io/en/stable/99-appendices/04-entity-table.html
    static final List<String> KNOWN_ENTITIES = [
        'sub', 'ses', 'sample', 'task', 'tracksys', 'acq', 'nuc', 'voi',
        'ce', 'trc', 'stain', 'rec', 'dir', 'run', 'mod', 'echo', 'flip',
        'inv', 'mt', 'part', 'proc', 'hemi', 'space', 'split', 'recording',
        'chunk', 'seg', 'res', 'den', 'label', 'from', 'to', 'desc', 'roi'
    ]
    
    String name
    String value
    
    BidsEntity(String name, String value) {
        if (!name) {
            throw new IllegalArgumentException("Entity name cannot be null or empty")
        }
        if (!value) {
            throw new IllegalArgumentException("Entity value cannot be null or empty")
        }
        this.name = name
        this.value = value
    }
    
    /**
     * Check if this entity name is a known BIDS entity
     * 
     * @return true if entity is in BIDS specification
     */
    boolean isKnownEntity() {
        return KNOWN_ENTITIES.contains(name)
    }
    
    /**
     * Validate entity name format (alphanumeric, no special chars except underscore)
     * 
     * @return true if valid
     */
    boolean isValidName() {
        return name ==~ /^[a-zA-Z][a-zA-Z0-9_]*$/
    }
    
    /**
     * Validate entity value format (alphanumeric)
     * 
     * @return true if valid
     */
    boolean isValidValue() {
        return value ==~ /^[a-zA-Z0-9]+$/
    }
    
    @Override
    String toString() {
        return "${name}-${value}"
    }
    
    @Override
    boolean equals(Object obj) {
        if (this.is(obj)) return true
        if (!(obj instanceof BidsEntity)) return false
        BidsEntity other = (BidsEntity) obj
        return name == other.name && value == other.value
    }
    
    @Override
    int hashCode() {
        return Objects.hash(name, value)
    }
}
