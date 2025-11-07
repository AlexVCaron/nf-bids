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
 *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf
 */
@CompileStatic
class BidsEntityUtils {

    /**
     * Check if entities match the required pattern
     *
     * @param entities Actual entities
     * @param requiredEntities Required entities
     * @return true if match
     *
     * @reference Entity matching logic from:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L15-L30
     */
    static final boolean entitiesMatch(
        List<BidsEntity> entities,
        List<BidsEntity> requiredEntities,
        Comparator comparator = Comparator.naturalOrder()
    ) {
        return requiredEntities.every { entity ->
            BidsEntity comparable = entities.find{ other -> other == entity }
            if (!comparable) {
                return false
            }

            return comparator.compare(comparable, entity)
        }
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L35-L70
     */
    static List<BidsFile> filterByEntities(List<BidsFile> files, List<BidsEntity> entityFilter) {
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L75-L110
     */
    static Map<String, List<BidsFile>> groupByEntity(List<BidsFile> files, String entityName) {
        Map<String, List<BidsFile>> grouped = [:].withDefault { [] }

        BidsLogger.logProgress("nf-bids-entity-utils", "Grouping files by entity: ${entityName}")
        files.each { file -> grouped[file.getEntityValue(BidsEntity.normalizeName(entityName))] << file }

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
    static Map<String, List<BidsFile>> groupByEntities(List<BidsFile> files, List<String> entityNames) {
        Map<String, List<BidsFile>> grouped = [:].withDefault { [] }

        BidsLogger.logProgress("nf-bids-entity-utils", "Grouping files by entities: ${entityNames.join(', ')}")
        files.each { file ->
            BidsLogger.logProgress("nf-bids-entity-utils", "├─ Processing file with entities: ${file.entities.collect { e -> "${e.name}-${e.value}" }.join(', ')}")
            grouped[BidsEntityUtils.groupingKey(file, entityNames)] << file
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
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/modules/grouping/entity_grouping_utils.nf#L155-L175
     */
    static List<String> extractEntityValues(BidsFile file, List<String> entityNames) {
        return entityNames.collect { entityName -> file.getEntityValue(BidsEntity.normalizeName(entityName)) }
    }

    /**
     * Get all unique values for an entity across files
     *
     * @param files List of BIDS files
     * @param entityName Entity name
     * @return Sorted list of unique values (excludes "NA")
     */
    static List<String> getUniqueValues(List<BidsFile> files, String entityName) {
        return files*.getEntityValue(entityName)
            .findAll { value -> value && value != "NA" }
            .unique()
            .sort()
    }

    /**
     * Build entity map from grouping key and entity names
     *
     * @param groupingKeyList Tuple key from channel
     * @param entityNames List of entity names
     * @return Map of entity name to value
     */
    static Map<String, BidsEntity> groupingKeyToMap(List groupingKeyList, List<String> entityNames) {
        Map<String, BidsEntity> entityMap = [:]

        entityNames.eachWithIndex { name, idx ->
            if (idx < groupingKeyList.size() && groupingKeyList[idx]&& groupingKeyList[idx] != "NA") {
                entityMap[name] = new BidsEntity(name, groupingKeyList[idx] as String)
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

        BidsFile firstFile = files[0]
        List<String> referenceValues = extractEntityValues(firstFile, entityNames)

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
            String value = file.getEntityValue(entityName)

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
            String value = file.getEntityValue(name)
            return (value && value != "NA") ? "${name}-${value}" : null
        }.findAll { repr -> repr != null }.join('_')
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
    static String comparisonKey(BidsFile file, List<String> entityNames) {
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
    static String groupingKey(BidsFile file, List<String> entities) {
        return BidsEntityUtils.comparisonKey(
            file, entities.collect { name -> BidsEntity.normalizeName(name) }
        )
    }

}
