package nfneuro.plugin.util

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity
import nfneuro.plugin.model.BidsFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Utility methods for matching and grouping {@link nfneuro.plugin.model.BidsFile} objects
 * by their {@link nfneuro.plugin.model.BidsEntity} values.
 *
 * <p>The key method is {@link #groupByEntities}, which is called by
 * {@link nfneuro.plugin.grouping.BaseSetHandler} to partition files into per-group
 * buckets before set-type–specific packing.</p>
 */
@CompileStatic
class BidsEntityUtils {

    static final String NA_VALUE = 'NA'
    static final String ENTITY_ALIASES_RESOURCE = 'nfneuro/entity_aliases.json'

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

    static Map<String, String> buildAliasToEntityMap(String aliasesJsonPath = null) {
        Map<String, String> result = [:]

        BidsEntity.SHORT_ENTITY_MAPPING.each { String longName, String shortName ->
            String canonical = BidsEntity.normalizeName(longName)
            Set<String> aliases = new LinkedHashSet<String>()
            aliases << canonical
            aliases << longName.toLowerCase()
            aliases << (canonical + '_id')
            aliases << (longName.toLowerCase() + '_id')
            aliases.each { String alias -> result[alias] = canonical }
        }

        addAliasesFromStream(BidsEntityUtils.class.classLoader.getResourceAsStream(ENTITY_ALIASES_RESOURCE), result)

        if (aliasesJsonPath) {
            Path aliasesPath = Paths.get(aliasesJsonPath)
            if (Files.exists(aliasesPath) && Files.isRegularFile(aliasesPath)) {
                addAliasesFromStream(Files.newInputStream(aliasesPath), result)
            }
        }

        return result
    }

    static Map<String, String> normalizeEntityMap(Map values, Map<String, String> aliasToEntity) {
        Map<String, String> aliases = aliasToEntity ?: [:]
        Map<String, String> normalized = [:]
        values.each { rawKey, rawValue ->
            String entityKey = normalizeEntityKey(rawKey?.toString(), aliases)
            if (!entityKey) {
                return
            }

            String entityValue = normalizeEntityValue(entityKey, rawValue?.toString(), aliases)
            if (!entityValue || entityValue == NA_VALUE) {
                return
            }
            normalized[entityKey] = entityValue
        }
        return normalized
    }

    static String normalizeEntityKey(String key, Map<String, String> aliasToEntity) {
        Map<String, String> aliases = aliasToEntity ?: [:]
        if (!key) {
            return null
        }

        String cleanKey = key.trim().toLowerCase()
        if (!cleanKey) {
            return null
        }

        String canonicalFromAlias = aliases[cleanKey]
        if (canonicalFromAlias) {
            return canonicalFromAlias
        }

        if (cleanKey.endsWith('_id')) {
            String base = cleanKey.substring(0, cleanKey.length() - 3)
            if (BidsEntity.longEntityExists(base) || BidsEntity.shortEntityExists(base)) {
                return BidsEntity.normalizeName(base)
            }
        }

        if (BidsEntity.longEntityExists(cleanKey) || BidsEntity.shortEntityExists(cleanKey)) {
            return BidsEntity.normalizeName(cleanKey)
        }

        return null
    }

    static String normalizeEntityValue(String entityKey, String value, Map<String, String> aliasToEntity) {
        if (!value) {
            return null
        }

        String cleanValue = value.trim()
        if (!cleanValue || cleanValue == NA_VALUE) {
            return null
        }

        int sep = cleanValue.indexOf('-')
        if (sep > 0 && sep < cleanValue.length() - 1) {
            String prefix = cleanValue.substring(0, sep)
            String remainder = cleanValue.substring(sep + 1)
            String normalizedPrefix = normalizeEntityKey(prefix, aliasToEntity)
            if (normalizedPrefix == entityKey) {
                cleanValue = remainder
            }
        }

        return BidsEntity.sanitizeValue(cleanValue)
    }

    private static void addAliasesFromStream(InputStream stream, Map<String, String> result) {
        if (stream == null) {
            return
        }

        try (InputStream closeable = stream) {
            Map parsed = (Map) new JsonSlurper().parse(closeable)
            parsed.each { Object key, Object value ->
                String canonical = BidsEntity.normalizeName(key.toString().toLowerCase())
                List aliases = value instanceof List ? (List) value : []
                aliases.each { Object alias ->
                    String cleanAlias = alias?.toString()?.trim()?.toLowerCase()
                    if (cleanAlias) {
                        result[cleanAlias] = canonical
                    }
                }
            }
        }
    }

}
