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
        
        return files.findAll { file ->
            entityFilter.every { entityName, requiredValue ->
                if (!requiredValue || requiredValue == "NA") {
                    return true  // Wildcard match
                }
                
                def actualValue = file.getEntity(entityName)
                return actualValue == requiredValue
            }
        }
    }
    
    /**
     * Check if file matches entity pattern
     * 
     * @param file BIDS file to check
     * @param pattern Map of entity name to required value
     * @return true if all entities match
     */
    static boolean matchesPattern(BidsFile file, Map<String, String> pattern) {
        return pattern.every { entityName, requiredValue ->
            if (!requiredValue || requiredValue == "NA") {
                return true
            }
            return file.getEntity(entityName) == requiredValue
        }
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
    static Object groupByMultipleEntities(List<BidsFile> files, List<String> entityNames) {
        if (entityNames.isEmpty()) {
            return files
        }
        
        def firstEntity = entityNames[0]
        def remainingEntities = entityNames.size() > 1 ? entityNames[1..-1] : []
        
        def grouped = groupByEntity(files, firstEntity)
        
        if (remainingEntities.isEmpty()) {
            return grouped
        }
        
        // Recursively group by remaining entities
        return grouped.collectEntries { value, fileList ->
            [value, groupByMultipleEntities(fileList, remainingEntities)]
        }
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
     * Create grouping key from file entities
     * 
     * Builds a tuple key for channel grouping
     * 
     * @param file BIDS file
     * @param loopOverEntities Entities to include in key
     * @return Tuple key as list
     */
    static List<String> createGroupingKey(BidsFile file, List<String> loopOverEntities) {
        return extractEntityValues(file, loopOverEntities)
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
}
