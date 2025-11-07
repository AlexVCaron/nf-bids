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

    public static final Map<String, String> SHORT_ENTITY_MAPPING = [
        'subject': 'sub',
        'session': 'ses',
        'sample': '',
        'task': 'task',
        'tracksys': '',
        'acquisition': 'acq',
        'nucleus': 'nuc',
        'volume': 'voi',
        'ceagent': 'ce',
        'tracer': 'trc',
        'stain': '',
        'reconstruction': 'rec',
        'direction': 'dir',
        'run': '',
        'modality': 'mod',
        'echo': '',
        'flip': '',
        'inversion': 'inv',
        'mtransfer': 'mt',
        'part': '',
        'processing': 'proc',
        'hemisphere': 'hemi',
        'space': '',
        'split': '',
        'recording': '',
        'chunk': '',
        'segmentation': 'seg',
        'resolution': 'res',
        'density': 'den',
        'label': '',
        'description': 'desc'
    ]

    public static final List<String> LONG_ENTITIES = SHORT_ENTITY_MAPPING.keySet().toList()
    public static final List<String> SHORT_ENTITIES = SHORT_ENTITY_MAPPING.collect { k, v -> v ?: k }

    static final boolean shortEntityExists(String entityName) {
        return SHORT_ENTITIES.contains(entityName)
    }

    static final boolean longEntityExists(String entityName) {
        return LONG_ENTITIES.contains(entityName)
    }

    /*
     * Normalize long entity name to short form
     *
     * @param entityName Long entity name
     * @return Short entity name
     *
     */
    static final String normalizeName(String entityName) {
        try {
            return SHORT_ENTITY_MAPPING[entityName] ?: entityName
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown long entity name: ${entityName}")
        }
    }

    /**
     * Normalize entity value for comparison
     *
     * Removes leading zeros from numeric parts: "flip-02" → "flip-2" or "02" → "2"
     *
     * @param value Entity value to normalize
     * @return Normalized value
     *
     * @reference normalizeEntityValue function:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L32-L46
     */
    static final String normalizeValue(String value) {
        if (!value) {
            return value
        }

        String[] parts = value.split('-')
        String prefix = parts[0]

        if (parts.length == 1) {
            return sanitizeValue(prefix)
        }

        return "${prefix}-${sanitizeValue(parts[1])}"
    }

    static final String sanitizeValue(String value) {
        if (!value) {
            return value
        }

        String[] parts = value.split('-')
        String suffix = parts[0]

        if (parts.length > 1) {
            suffix = parts[1]
        }

        // Remove leading zeros: "flip-02" → "flip-2"
        if ((suffix as String).isNumber()) {
            try {
                suffix = Integer.parseInt(suffix as String)
            } catch (NumberFormatException e) {
                // Ignore, keep original suffix
            }
        }

        return suffix as String
    }

    String name
    String value

    BidsEntity(String name, String value) {
        if (!name) {
            throw new IllegalArgumentException("Entity name cannot be null or empty")
        }
        if (!value) {
            throw new IllegalArgumentException("Entity value cannot be null or empty")
        }
        this.name = normalizeName(name)
        this.value = value
    }

    /**
     * Check if this entity name is a known BIDS entity
     *
     * @return true if entity is in BIDS specification
     */
    boolean isKnownEntity() {
        return LONG_ENTITIES.contains(name) || SHORT_ENTITIES.contains(name)
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

    boolean nameMatches(BidsEntity other) {
        return BidsEntity.normalizeName(this.name) == BidsEntity.normalizeName(other.name)
    }

    boolean valueMatches(BidsEntity other) {
        return BidsEntity.normalizeValue(this.value) == BidsEntity.normalizeValue(other.value)
    }

    @Override
    String toString() {
        return "${name}-${value}"
    }

    @Override
    boolean equals(Object obj) {
        if (this.is(obj)) { return true }
        if (!(obj instanceof BidsEntity)) { return false }

        return this.nameMatches(obj as BidsEntity) && this.valueMatches(obj as BidsEntity)
    }

    @Override
    int hashCode() {
        return Objects.hash(name, value)
    }
}
