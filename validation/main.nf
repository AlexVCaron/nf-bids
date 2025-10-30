#!/usr/bin/env nextflow

/*
 * Simplified validation test for Channel.fromBIDS()
 * Outputs items with detailed statistics for test validation
 */

include { fromBIDS } from "plugin/nf-bids"

params.bids_dir = "${projectDir}/data/custom/ds-dwi"
params.config = "${projectDir}/configs/config_dwi.yaml"

/**
 * Get set type from data structure
 */
def getSetType(data) {
    if (!data || !(data instanceof Map)) return 'unknown'
    
    // Check each suffix to determine set type
    for (entry in data) {
        def key = entry.key
        def value = entry.value
        
        if (value instanceof List) {
            // Sequential set - array of files or nested arrays
            return 'sequential'
        } else if (value instanceof Map) {
            // Could be plain, named, or hierarchical
            if (value.containsKey('nii') || value.containsKey('json')) {
                // Plain set - direct file references
                return 'plain'
            } else {
                // Check if it's named (groups with file refs) or hierarchical (nested structure)
                def hasNamedGroups = false
                def hasNestedMaps = false
                
                for (innerEntry in value) {
                    def innerValue = innerEntry.value
                    if (innerValue instanceof Map) {
                        if (innerValue.containsKey('nii') || innerValue.containsKey('json')) {
                            // Named set - group names pointing to file maps
                            hasNamedGroups = true
                        } else {
                            // Hierarchical structure
                            hasNestedMaps = true
                        }
                    } else if (innerValue instanceof List) {
                        // Mixed set - named groups with sequential arrays
                        return 'mixed'
                    }
                }
                
                if (hasNamedGroups) return 'named'
                if (hasNestedMaps) return 'hierarchical_sequential'
            }
        }
    }
    return 'plain'
}

/**
 * Count files in a data structure recursively
 */
def countFiles(data) {
    if (!data) return 0
    if (data instanceof String) return 1
    if (data instanceof List) {
        return data.sum { countFiles(it) } ?: 0
    }
    if (data instanceof Map) {
        return data.values().sum { countFiles(it) } ?: 0
    }
    return 0
}

/**
 * Get statistics for plain sets
 */
def getPlainSetStats(data, totalFiles) {
    def stats = []
    data.each { suffix, value ->
        // Plain sets have direct file references in maps
        if (value instanceof Map) {
            def fileCount = 0
            // Count all file types (nii, json, etc.)
            value.each { ext, file ->
                if (file) fileCount++
            }
            if (fileCount > 0) {
                stats << [suffix: suffix, files: fileCount]
            }
        }
    }
    return stats
}

/**
 * Get statistics for named sets
 */
def getNamedSetStats(data, totalFiles) {
    def stats = []
    data.each { suffix, groups ->
        if (groups instanceof Map && !groups.containsKey('nii') && !groups.containsKey('json')) {
            def groupStats = []
            groups.each { groupName, files ->
                if (files instanceof Map) {
                    // Count actual files (nii + json sidecars)
                    def fileCount = 0
                    files.each { ext, filepath ->
                        if (filepath) fileCount++
                    }
                    if (fileCount > 0) {
                        groupStats << [name: groupName, files: fileCount]
                    }
                }
            }
            if (groupStats) {
                stats << [suffix: suffix, groups: groupStats]
            }
        }
    }
    return stats
}

/**
 * Get statistics for sequential sets
 */
def getSequentialSetStats(data, totalFiles) {
    def stats = []
    data.each { suffix, value ->
        if (value instanceof List) {
            stats << [suffix: suffix, items: value.size(), files: totalFiles]
        } else if (value instanceof Map && !value.containsKey('nii') && !value.containsKey('json')) {
            // Hierarchical sequential
            def dimensions = getMapDepth(value)
            def itemCount = countItemsInHierarchy(value)
            def hierarchy = getHierarchyKeys(value)
            stats << [suffix: suffix, type: 'hierarchical', dimensions: dimensions, items: itemCount, files: totalFiles, hierarchy: hierarchy]
        }
    }
    return stats
}

/**
 * Get hierarchy structure with file counts
 */
def getHierarchyKeys(map, depth = 1) {
    if (!(map instanceof Map)) return [:]
    def keys = [:]
    
    // Get keys at this level with their item/file counts
    def levelItems = []
    map.each { key, value ->
        def itemInfo = [name: key]
        if (value instanceof Map) {
            itemInfo.items = countItemsInHierarchy(value)
        } else if (value instanceof String) {
            itemInfo.files = 1
        }
        levelItems << itemInfo
    }
    keys["level${depth}"] = levelItems
    
    // Recurse to next level if exists
    def firstValue = map.values().find { it instanceof Map }
    if (firstValue) {
        keys.putAll(getHierarchyKeys(firstValue, depth + 1))
    }
    
    return keys
}

/**
 * Count items in a hierarchical structure
 */
def countItemsInHierarchy(map) {
    if (!(map instanceof Map)) return 0
    def count = 0
    map.each { key, value ->
        if (value instanceof String) {
            count++
        } else if (value instanceof Map) {
            count += countItemsInHierarchy(value)
        }
    }
    return count
}

/**
 * Get statistics for mixed sets
 */
def getMixedSetStats(data, totalFiles) {
    def stats = []
    data.each { suffix, groups ->
        if (groups instanceof Map) {
            def groupStats = []
            groups.each { groupName, items ->
                if (items instanceof List) {
                    // Count files in the list (each item might have nii+json)
                    def fileCount = items.size() * 2  // Approximate - assumes nii+json pairs
                    groupStats << [name: groupName, items: items.size(), files: fileCount]
                }
            }
            if (groupStats) {
                stats << [suffix: suffix, groups: groupStats]
            }
        }
    }
    return stats
}

/**
 * Get depth of nested map structure
 */
def getMapDepth(map) {
    if (!(map instanceof Map)) return 0
    if (map.isEmpty()) return 1
    
    def maxDepth = 1
    map.values().each { value ->
        if (value instanceof Map) {
            maxDepth = Math.max(maxDepth, 1 + getMapDepth(value))
        }
    }
    return maxDepth
}

/**
 * Generate detailed statistics for an item
 */
def generateItemStats(item) {
    // Item is a tuple: [groupingKey, enrichedData]
    def groupingKey = item[0]
    def enrichedData = item[1]
    
    def entities = enrichedData.subMap(enrichedData.keySet().findAll { it != 'data' && it != 'filePaths' && it != 'bidsParentDir' })
    def data = enrichedData.data ?: [:]
    def filePaths = enrichedData.filePaths ?: []
    
    def setType = getSetType(data)
    // Use actual filePaths count instead of trying to count from data structure
    def totalFiles = filePaths.size()
    
    def stats = [
        type: setType,
        entities: entities,
        totalFiles: totalFiles,
        suffixes: data.keySet().toList()
    ]
    
    // Add type-specific statistics
    switch(setType) {
        case 'plain':
            stats.details = getPlainSetStats(data, totalFiles)
            break
        case 'named':
            stats.details = getNamedSetStats(data, totalFiles)
            break
        case 'sequential':
        case 'hierarchical_sequential':
            stats.details = getSequentialSetStats(data, totalFiles)
            break
        case 'mixed':
            stats.details = getMixedSetStats(data, totalFiles)
            break
    }
    
    return stats
}

workflow main_workflow {
    println "🧪 Testing Channel.fromBIDS..."
    
    def options = params.libbids_sh ? [libbids_sh: params.libbids_sh] : [:]
    def ch = Channel.fromBIDS(params.bids_dir, params.config, options)
    
    // Process and output statistics for each item
    def output_ch = ch.map { item ->
        def stats = generateItemStats(item)
        def statsJson = groovy.json.JsonOutput.toJson(stats)
        println "ITEM_STATS: ${statsJson}"
        return item  // Pass through for counting and emit
    }
    
    output_ch.count().subscribe { count ->
        println "✓ Pipeline completed with ${count} items"
    }
    
    emit:
        bids_channel = output_ch
}

workflow {
    main_workflow()
}
