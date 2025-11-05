package nfneuro.plugin.util

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile

/**
 * Utility methods for working with BIDS entities
 * 
 * Provides common entity operations like filtering, matching, and extraction
 * 
 * @reference Entity utilities from:
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf
 */
@CompileStatic
class BidsEntityUtils {

    public static final Map<String, String> SHORT_ENTITY_MAPPING = [
        'subject': 'sub',
        'session': 'ses',
        'acquisition': 'acq',
        'ceagent': 'ce',
        'tracer': 'trc',
        'reconstruction': 'rec',
        'direction': 'dir',
        'modality': 'mod',
        'mtransfer': 'mt',
        'inversion': 'inv',
        'processing': 'proc',
        'hemisphere': 'hemi',
        'segmentation': 'seg',
        'resolution': 'res',
        'density': 'den',
        'nucleus': 'nuc',
        'volume': 'voi'
    ]

    public static final List<String> LONG_ENTITIES = SHORT_ENTITY_MAPPING.keySet().toList()
    public static final List<String> SHORT_ENTITIES = SHORT_ENTITY_MAPPING.values().toList()

    static final boolean shortEntityExists(String entityName) {
        return SHORT_ENTITIES.contains(entityName)
    }

    static final boolean longEntityExists(String entityName) {
        return LONG_ENTITIES.contains(entityName)
    }

    static final String normalizeEntityName(String entityName) {
        try {
            return SHORT_ENTITY_MAPPING[entityName]
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown long entity name: ${entityName}")
        }
    }

    /**
     * Check if entities match the required pattern
     *
     * @param entities Actual entity values
     * @param requiredEntities Required entity pattern
     * @return true if match
     *
     * @reference Entity matching logic from:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L15-L30
     */
    static final boolean entitiesMatch(Map entities, Map requiredEntities, Comparator comparator = Comparator.naturalOrder()) {
        return requiredEntities.every { key, value ->
            if (value == null || value == 'NA') {
                return true  // Wildcard match
            }
            return comparator.compare(entities[key], value)
        }
    }

    /**
     * Compare entity value with normalization
     *
     * Handles different zero-padding formats: "flip-02" matches "flip-2"
     * Also handles prefix differences: config has "mt-on" while file stores "on"
     *
     * @param actualValue Value from file entity (without prefix, e.g., "on")
     * @param expectedValue Value from configuration pattern (with prefix, e.g., "mt-on")
     * @return true if value match after normalization
     *
     * @reference entityValuesMatch function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L47-L53
     */
    static final boolean entityValueMatch(String actualValue, String expectedValue) {
        if (!actualValue || !expectedValue) {
            return actualValue == expectedValue
        }

        // Strip prefixes from expected value if present (e.g., "mt-on" -> "on")
        def cleanExpected = expectedValue
        if (expectedValue.contains('-')) {
            def parts = expectedValue.split('-', 2)
            if (parts.length == 2) {
                cleanExpected = parts[1]  // Get part after prefix
            }
        }

        return normalizeEntityValue(actualValue) == normalizeEntityValue(cleanExpected)
    }

    /**
     * Normalize entity value for comparison
     *
     * Removes leading zeros from numeric parts: "flip-02" → "flip-2"
     *
     * @param value Entity value to normalize
     * @return Normalized value
     *
     * @reference normalizeEntityValue function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/subworkflows/emit_mixed_sets.nf#L32-L46
     */
    static final String normalizeEntityValue(String value) {
        if (!value || !value.contains('-')) {
            return value
        }

        def parts = value.split('-', 2)
        if (parts.length != 2) {
            return value
        }

        def prefix = parts[0]
        def suffix = parts[1]

        // Remove leading zeros: "flip-02" → "flip-2"
        if (suffix.isNumber()) {
            try {
                def numericSuffix = Integer.parseInt(suffix)
                return "${prefix}-${numericSuffix}"
            } catch (NumberFormatException e) {
                return value
            }
        }
        return value
    }

    /**
     * Filter files by entity values
     *
     * Returns files that match all specified entity criteria
     *
     * @param files List of BIDS files
     * @param entityFilter Map of entity name to required value (null/"NA" = wildcard)
     * @return Filtered list of files
     *
     * @reference Entity filtering:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L35-L70
     */
    static List<BidsFile> filterByEntities(List<BidsFile> files, Map<String, String> entityFilter) {
        if (!entityFilter) {
            return files
        }

        return files.findAll { file -> entitiesMatch(file.entities, entityFilter) }
    }

    /**
     * Group files by entity value
     *
     * @param files List of BIDS files
     * @param entityName Entity to group by (e.g., "acq", "dir")
     * @return Map of entity value to list of files
     *
     * @reference Grouping by entity:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L75-L110
     */
    static Map<String, List<BidsFile>> groupByEntity(List<BidsFile> files, String entityName) {
        Map<String, List<BidsFile>> grouped = [:].withDefault { [] as List<BidsFile> }

        files.each { file ->
            def value = file.getEntity(entityName) ?: "NA"
            grouped[value] << file
        }

        return grouped
    }

    /**
     * Group files by multiple entities
     *
     * Creates hierarchical grouping by multiple entity names
     *
     * @param files List of BIDS files
     * @param entityNames List of entity names to group by (in order)
     * @return Nested map structure with files at leaf level
     */
    static Map<String, List<BidsFile>> groupByMultipleEntities(List<BidsFile> files, List<String> entityNames) {
        Map<String, List<BidsFile>> grouped = [:].withDefault { [] as List<BidsFile> }

        files.each { file ->
            def key = BidsEntityUtils.createGroupingKey(file, entityNames)
            grouped[key] << file
        }

        return grouped
    }

    /**
     * Extract entity values as ordered list
     *
     * @param file BIDS file
     * @param entityNames List of entity names in desired order
     * @return List of entity values in same order
     *
     * @reference Entity value extraction:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L155-L175
     */
    static List<String> extractEntityValues(BidsFile file, List<String> entityNames) {
        return entityNames.collect { entityName ->
            file.getEntity(entityName) ?: "NA"
        }
    }

    /**
     * Get all unique values for an entity across files
     *
     * @param files List of BIDS files
     * @param entityName Entity name
     * @return Sorted list of unique values (excludes "NA")
     */
    static List<String> getUniqueValues(List<BidsFile> files, String entityName) {
        return files.collect { it.getEntity(entityName) }
            .findAll { it && it != "NA" }
            .unique()
            .sort()
    }

    /**
     * Build entity map from grouping key and entity names
     *
     * @param groupingKey Tuple key from channel
     * @param entityNames List of entity names
     * @return Map of entity name to value
     */
    static Map<String, String> groupingKeyToMap(List groupingKey, List<String> entityNames) {
        Map<String, String> entityMap = [:]

        entityNames.eachWithIndex { name, idx ->
            if (idx < groupingKey.size()) {
                entityMap[name] = (groupingKey[idx] as String) ?: "NA"
            } else {
                entityMap[name] = "NA"
            }
        }

        return entityMap
    }

    /**
     * Check if files have consistent entity values
     *
     * Validates that all files in a group have the same value for given entities
     *
     * @param files List of BIDS files
     * @param entityNames Entity names to check
     * @return true if all files have same values for these entities
     */
    static boolean hasConsistentEntities(List<BidsFile> files, List<String> entityNames) {
        if (files.isEmpty()) {
            return true
        }

        def firstFile = files[0]
        def referenceValues = extractEntityValues(firstFile, entityNames)

        return files.every { file ->
            extractEntityValues(file, entityNames) == referenceValues
        }
    }

    /**
     * Sort files by entity value
     *
     * Sorts files based on entity values (numeric if possible, lexicographic otherwise)
     *
     * @param files List of BIDS files
     * @param entityName Entity to sort by
     * @return Sorted list of files
     */
    static List<BidsFile> sortByEntity(List<BidsFile> files, String entityName) {
        return files.sort { file ->
            def value = file.getEntity(entityName)

            // Try numeric sorting first
            if (value ==~ /\d+/) {
                return value.toInteger()
            }

            // Fall back to lexicographic
            return value
        }
    }

    /**
     * Create entity string representation
     *
     * Builds a readable string like "sub-01_ses-01_run-1"
     *
     * @param file BIDS file
     * @param entityNames Entities to include
     * @return Entity string
     */
    static String toEntityString(BidsFile file, List<String> entityNames) {
        return entityNames.collect { name ->
            def value = file.getEntity(name)
            if (value && value != "NA") {
                return "${name}-${value}"
            }
            return null
        }.findAll { it != null }.join('_')
    }

    /**
     * Validate required entities are present
     *
     * @param file BIDS file
     * @param requiredEntities List of entity names that must be present
     * @return true if all required entities exist
     */
    static boolean hasRequiredEntities(BidsFile file, List<String> requiredEntities) {
        return requiredEntities.every { entityName ->
            file.hasEntity(entityName)
        }
    }

    /**
     * Create entity comparison key
     *
     * Builds a key for entity-based comparison/deduplication
     *
     * @param file BIDS file
     * @param entityNames Entities to include in key
     * @return Comparison key string
     */
    static String createComparisonKey(BidsFile file, List<String> entityNames) {
        return extractEntityValues(file, entityNames).join('|')
    }

        /**
     * Build group key from file entities
     *
     * Normalizes entity names from long to short format before lookup.
     * Config uses long names (subject, session) but files store short names (sub, ses).
     *
     * @param file BIDS file
     * @param entities Entities to include in key (may be long or short names)
     * @return Group key string
     */
    static String createGroupingKey(BidsFile file, List<String> entities) {
        return BidsEntityUtils.createComparisonKey(
            file, entities.collect { BidsEntityUtils.normalizeEntityName(it) }
        )
    }
}
