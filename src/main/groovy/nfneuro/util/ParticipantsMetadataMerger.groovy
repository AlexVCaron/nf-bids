package nfneuro.plugin.util

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity

@CompileStatic
class ParticipantsMetadataMerger {

    private static final String NA_VALUE = 'NA'
    private static final String ENTITY_ALIASES_RESOURCE = 'nfneuro/entity_aliases.json'

    private final Map<String, String> aliasToEntity

    ParticipantsMetadataMerger() {
        this.aliasToEntity = buildAliasToEntityMap()
    }

    void mergeIntoMeta(Map meta, List<Map<String, String>> participantsMetadata, List<String> loopOverEntities) {
        if (!meta || participantsMetadata == null || participantsMetadata.isEmpty()) {
            return
        }

        Map<String, String> normalizedMeta = normalizeEntityMap(meta)
        List<String> normalizedLoopEntities = (loopOverEntities ?: [])
            .collect { name -> BidsEntity.normalizeName(name) }
            .unique() as List<String>

        List<Map<String, Object>> matchedRows = []
        int bestCoverage = -1
        List<Integer> bestPriority = []

        participantsMetadata.eachWithIndex { Map<String, String> row, int rowIndex ->
            Map<String, String> normalizedRow = normalizeEntityMap(row)
            List<String> candidateKeys = normalizedLoopEntities.findAll { String entity ->
                normalizedMeta.containsKey(entity) && normalizedRow.containsKey(entity)
            } as List<String>

            if (!candidateKeys) {
                return
            }

            boolean matches = candidateKeys.every { String entity ->
                normalizedMeta[entity] == normalizedRow[entity]
            }
            if (!matches) {
                return
            }

            List<Integer> priority = []
            if (candidateKeys.size() >= bestCoverage) {
                priority = buildCoveragePriority(candidateKeys, normalizedLoopEntities)
            }
            if (candidateKeys.size() > bestCoverage || (
                candidateKeys.size() == bestCoverage && comparePriority(priority, bestPriority) > 0
            )) {
                bestCoverage = candidateKeys.size()
                bestPriority = priority
                matchedRows = [[row: row, candidateKeys: candidateKeys, rowIndex: rowIndex, priority: priority]]
            } else if (candidateKeys.size() == bestCoverage && comparePriority(priority, bestPriority) == 0) {
                matchedRows << [row: row, candidateKeys: candidateKeys, rowIndex: rowIndex, priority: priority]
            }
        }

        if (!matchedRows) {
            return
        }

        if (matchedRows.size() > 1) {
            BidsLogger.logProgress(
                "nf-bids-handler",
                "├─ Warning: multiple participants.tsv rows matched equally for meta ${meta}. Using deterministic merge order."
            )
        }

        List<Map<String, Object>> sortedRows = matchedRows.sort { Map<String, Object> a, Map<String, Object> b ->
            (a.rowIndex as Integer) <=> (b.rowIndex as Integer)
        }

        Map<String, String> mergedParticipantValues = [:]
        sortedRows.each { Map<String, Object> match ->
            Map<String, String> row = match.row as Map<String, String>
            List<String> candidateKeys = match.candidateKeys as List<String>

            row.each { String rawKey, String rawValue ->
                String trimmedValue = rawValue?.trim()
                if (!trimmedValue || trimmedValue == NA_VALUE) {
                    return
                }

                String normalizedKey = normalizeEntityKey(rawKey)
                if (normalizedKey && candidateKeys.contains(normalizedKey)) {
                    return
                }

                if (mergedParticipantValues.containsKey(rawKey) && mergedParticipantValues[rawKey] != trimmedValue) {
                    BidsLogger.logProgress(
                        "nf-bids-handler",
                        "├─ Warning: conflicting participants.tsv values for '${rawKey}' (${mergedParticipantValues[rawKey]} vs ${trimmedValue}); keeping first value."
                    )
                    return
                }
                mergedParticipantValues[rawKey] = trimmedValue
            }
        }

        mergedParticipantValues.each { String key, String value ->
            if (meta.containsKey(key) && meta[key] != value) {
                BidsLogger.logProgress(
                    "nf-bids-handler",
                    "├─ Warning: participants.tsv key '${key}' conflicts with existing meta value '${meta[key]}'; keeping existing value."
                )
                return
            }

            if (!meta.containsKey(key)) {
                meta[key] = value
            }
        }
    }

    private Map<String, String> normalizeEntityMap(Map values) {
        Map<String, String> normalized = [:]
        values.each { rawKey, rawValue ->
            String entityKey = normalizeEntityKey(rawKey?.toString())
            if (!entityKey) {
                return
            }

            String entityValue = normalizeEntityValue(entityKey, rawValue?.toString())
            if (!entityValue || entityValue == NA_VALUE) {
                return
            }
            normalized[entityKey] = entityValue
        }
        return normalized
    }

    private String normalizeEntityKey(String key) {
        if (!key) {
            return null
        }

        String cleanKey = key.trim().toLowerCase()
        if (!cleanKey) {
            return null
        }

        String canonicalFromAlias = aliasToEntity[cleanKey]
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

    private String normalizeEntityValue(String entityKey, String value) {
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
            String normalizedPrefix = normalizeEntityKey(prefix)
            if (normalizedPrefix == entityKey) {
                cleanValue = remainder
            }
        }

        return BidsEntity.sanitizeValue(cleanValue)
    }

    private List<Integer> buildCoveragePriority(List<String> candidateKeys, List<String> orderedLoopEntities) {
        return orderedLoopEntities.collect { String entity ->
            return candidateKeys.contains(entity) ? 1 : 0
        } as List<Integer>
    }

    private int comparePriority(List<Integer> left, List<Integer> right) {
        if (right == null || right.isEmpty()) {
            return (left && !left.isEmpty()) ? 1 : 0
        }

        int size = Math.max(left?.size() ?: 0, right.size())
        for (int i = 0; i < size; i++) {
            int a = (left != null && i < left.size()) ? left[i] : 0
            int b = i < right.size() ? right[i] : 0
            if (a != b) {
                return a <=> b
            }
        }
        return 0
    }

    private Map<String, String> buildAliasToEntityMap() {
        Map<String, String> result = [:]

        BidsEntity.SHORT_ENTITY_MAPPING.each { String longName, String shortName ->
            String canonical = BidsEntity.normalizeName(longName)
            Set<String> aliases = new LinkedHashSet<String>()
            aliases << canonical
            aliases << longName.toLowerCase()
            aliases << "${canonical}_id"
            aliases << "${longName.toLowerCase()}_id"
            aliases.each { String alias -> result[alias] = canonical }
        }

        InputStream stream = this.class.classLoader.getResourceAsStream(ENTITY_ALIASES_RESOURCE)
        if (stream == null) {
            return result
        }

        Map parsed = (Map) new JsonSlurper().parse(stream)
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

        return result
    }
}
