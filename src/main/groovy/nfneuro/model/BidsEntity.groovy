package nfneuro.plugin.model

import groovy.transform.CompileStatic

/**
 * Represents a single BIDS entity (key–value pair such as {@code sub-01} or {@code ses-BL}).
 *
 * <p>Stores the entity in its short form (e.g. {@code "sub"}) regardless of whether
 * the long form (e.g. {@code "subject"}) was supplied on construction.
 * Equality and hash-code are based on normalised name and sanitised value, so
 * {@code "flip-02"} equals {@code "flip-2"}.</p>
 *
 * <p>The static utility methods {@link #normalizeName}, {@link #normalizeValue},
 * and {@link #sanitizeValue} are used throughout the grouping handlers for
 * consistent entity comparison.</p>
 */
@CompileStatic
class BidsEntity {

    public static final Map<String, String> SHORT_ENTITY_MAPPING = [
        'subject': 'sub',
        'template': 'tpl',
        'session': 'ses',
        'cohort': '',
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
        'atlas': '',
        'segmentation': 'seg',
        'scale': '',
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

    /**
     * Construct a BIDS entity.
     *
     * <p>The name is normalised to its short form via {@link #normalizeName}.</p>
     *
     * @param name  entity key in long or short form (e.g. {@code "subject"} or {@code "sub"})
     * @param value entity value without prefix (e.g. {@code "01"})
     * @throws IllegalArgumentException if {@code name} or {@code value} is null or empty
     */
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
