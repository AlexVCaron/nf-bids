package nfneuro.plugin.model

import groovy.transform.CompileStatic

/**
 * Container for BIDS channel data assembled by the set handlers.
 *
 * <p>Accumulates per-suffix data maps, associated file paths, and loop-over entity
 * values during the grouping phase.  {@link #toChannelTuple(List)} serialises the
 * container into the {@code [groupingKey, enrichedData]} tuple format consumed by
 * {@link nfneuro.plugin.channel.BidsHandler} before flattening.</p>
 *
 * <p>The flat output emitted by {@code Channel.fromBIDS()} is built from this
 * structure by converting string paths to {@code java.nio.file.Path} objects and
 * placing suffix data maps at the top level alongside a {@code meta} map of
 * entity values.</p>
 */
@CompileStatic
class BidsChannelData {

    Map<String, Object> data
    List<String> filePaths
    String bidsParentDir
    Map<String, String> entities

    /**
     * Construct an empty {@code BidsChannelData} container.
     */
    BidsChannelData() {
        this.data = [:]
        this.filePaths = []
        this.entities = [:]
    }

    /**
     * Add suffix data
     *
     * @param suffix BIDS suffix
     * @param suffixData Data for this suffix (can be String path, Map, or List)
     */
    void addSuffixData(String suffix, Object suffixData) {
        if (suffix && suffixData != null) {
            data[suffix] = suffixData
        }
    }

    /**
     * Get suffix data
     *
     * @param suffix BIDS suffix
     * @return Suffix data or null
     */
    Object getSuffixData(String suffix) {
        return data[suffix]
    }

    /**
     * Check if suffix exists in data
     *
     * @param suffix BIDS suffix
     * @return true if suffix data exists
     */
    boolean hasSuffix(String suffix) {
        return data.containsKey(suffix)
    }

    /**
     * Get all suffixes in this channel data
     *
     * @return List of suffix names
     */
    List<String> getSuffixes() {
        return new ArrayList<>(data.keySet())
    }

    /**
     * Add file path
     *
     * @param filePath File path to add
     */
    void addFilePath(String filePath) {
        if (filePath && !filePaths.contains(filePath)) {
            filePaths << filePath
        }
    }

    /**
     * Add multiple file paths
     *
     * @param paths List of file paths
     */
    void addFilePaths(List<String> paths) {
        paths.each { path ->
            addFilePath(path)
        }
    }

    /**
     * Add entity value
     *
     * @param entityName Entity name
     * @param entityValue Entity value
     */
    void addEntity(String entityName, String entityValue) {
        if (entityName && entityValue) {
            entities[entityName] = entityValue
        }
    }

    /**
     * Get entity value
     *
     * @param entityName Entity name
     * @return Entity value or "NA"
     */
    String getEntity(String entityName) {
        return entities.getOrDefault(entityName, "NA")
    }

    /**
     * Check if entity exists
     *
     * @param entityName Entity name
     * @return true if entity exists and is not "NA"
     */
    boolean hasEntity(String entityName) {
        String value = entities[entityName]
        return value && value != "NA"
    }

    /**
     * Validate that channel data has required content
     *
     * @return true if valid (has data and entities)
     */
    boolean isValid() {
        return !data.isEmpty() && !entities.isEmpty()
    }

    /**
     * Check if channel data is empty
     *
     * @return true if no data
     */
    boolean isEmpty() {
        return data.isEmpty()
    }

    /**
     * Convert to channel tuple format
     *
     * @param loopOverEntities Entities to use for grouping key (also determines which entities appear in enriched data)
     * @return Tuple [groupingKey, enrichedData]
     */
    Object toChannelTuple(List<String> loopOverEntities) {
        List groupingKey = loopOverEntities.collect { entity ->
            getEntityWithPrefix(entity)
        }

        Map enrichedData = [
            data: data,
            filePaths: filePaths,
            bidsParentDir: bidsParentDir
        ]

        loopOverEntities.each { entityName ->
            enrichedData[entityName] = getEntity(BidsEntity.normalizeName(entityName))
        }

        return [groupingKey, enrichedData]
    }

    @Override
    String toString() {
        String entityStr = entities.collect { k, v -> "${k}=${v}" }.join(', ')
        String suffixStr = data.keySet().join(', ')
        return "BidsChannelData[entities=[${entityStr}], suffixes=[${suffixStr}], files=${filePaths.size()}]"
    }

    @Override
    boolean equals(Object obj) {
        if (this.is(obj)) { return true }
        if (!(obj instanceof BidsChannelData)) { return false }
        BidsChannelData other = (BidsChannelData) obj
        return entities == other.entities && data == other.data
    }

    @Override
    int hashCode() {
        return Objects.hash(entities, data)
    }

        /**
     * Get entity value with BIDS prefix
     *
     * Converts entity value to full BIDS format (e.g., "Sub103" -> "sub-Sub103")
     * Normalizes entity name to short form before lookup.
     *
     * @param entityName Entity name (may be long or short)
     * @return Entity value with prefix or "NA"
     */
    private String getEntityWithPrefix(String entityName) {
        // Normalize to short name for lookup
        String shortName = BidsEntity.normalizeName(entityName)
        String value = getEntity(shortName)

        if (value == "NA") {
            return "NA"
        }

        return "${shortName}-${value}"
    }

}
