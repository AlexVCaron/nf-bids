package nfneuro.plugin.util

import groovy.transform.CompileStatic
import nfneuro.plugin.model.BidsEntity

@CompileStatic
class ParticipantsMetadataMerger {

    private static final String NA_VALUE = BidsEntityUtils.NA_VALUE

    private final Map<String, String> aliasToEntity

    ParticipantsMetadataMerger(String aliasesJsonPath = null) {
        this.aliasToEntity = BidsEntityUtils.buildAliasToEntityMap(aliasesJsonPath)
    }

    void mergeIntoMeta(Map meta, List<Map<String, String>> participantsMetadata, List<String> loopOverEntities) {
        if (!meta || participantsMetadata == null || participantsMetadata.isEmpty()) {
            return
        }

        Map<String, String> normalizedMeta = BidsEntityUtils.normalizeEntityMap(meta, aliasToEntity)
        List<String> normalizedLoopEntities = (loopOverEntities ?: [])
            .collect { name -> BidsEntity.normalizeName(name) }
            .unique() as List<String>

        List<Map<String, Object>> matchedRows = []
        int bestCoverage = -1
        List<Integer> bestPriority = []

        participantsMetadata.eachWithIndex { Map<String, String> row, int rowIndex ->
            Map<String, String> normalizedRow = BidsEntityUtils.normalizeEntityMap(row, aliasToEntity)
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
            String metaSummary = buildMetaSummary(meta)
            BidsLogger.logProgress(
                "nf-bids-handler",
                "├─ Warning: multiple participants.tsv rows matched equally for ${metaSummary}. Using deterministic merge order by participants.tsv row index."
            )
        }

        List<Map<String, Object>> sortedRows = matchedRows.sort { Map<String, Object> a, Map<String, Object> b ->
            (a.rowIndex as Integer) <=> (b.rowIndex as Integer)
        }

        Map<String, String> mergedParticipantValues = [:]
        sortedRows.each { Map<String, Object> match ->
            Map<String, String> row = match.row as Map<String, String>
            List<String> candidateKeys = match.candidateKeys as List<String>
            int rowIndex = match.rowIndex as Integer

            row.each { String rawKey, String rawValue ->
                String trimmedValue = rawValue?.trim()
                if (!trimmedValue || trimmedValue == NA_VALUE) {
                    return
                }

                String normalizedKey = BidsEntityUtils.normalizeEntityKey(rawKey, aliasToEntity)
                if (normalizedKey && candidateKeys.contains(normalizedKey)) {
                    return
                }

                if (mergedParticipantValues.containsKey(rawKey) && mergedParticipantValues[rawKey] != trimmedValue) {
                    BidsLogger.logProgress(
                        "nf-bids-handler",
                        "├─ Warning: conflicting participants.tsv values for '${rawKey}' at row ${rowIndex} (${mergedParticipantValues[rawKey]} vs ${trimmedValue}); keeping first value."
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
                    "├─ Warning: participants.tsv key '${key}' value '${value}' conflicts with existing meta value '${meta[key]}'; keeping existing value."
                )
                return
            }

            if (!meta.containsKey(key)) {
                meta[key] = value
            }
        }
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

    private String buildMetaSummary(Map meta) {
        List<String> preferredKeys = ['subject', 'session', 'run', 'task', 'echo']
        Map selected = [:]

        preferredKeys.each { String key ->
            if (meta.containsKey(key) && meta[key] != null) {
                selected[key] = meta[key]
            }
        }

        if (selected.isEmpty()) {
            selected = meta ?: [:]
        }

        return "meta ${selected}"
    }
}
