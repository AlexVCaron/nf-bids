# nf-bids Plugin Development Reference

> **Status**: Plugin is fully implemented and functional. This guide serves as a reference for understanding the implementation and making future enhancements.

**See also**:
- [architecture.md](architecture.md) - System architecture
- [development.md](development.md) - Development workflow
- [TODO.md](TODO.md) - Current priorities

---

## Implementation Reference

This guide maps plugin components to their original bids2nf workflow equivalents.

### Component Mapping

Plugin components map to original bids2nf workflow files:

| Plugin Component | Original File | Purpose |
|-----------------|---------------|----------|
| LibBidsShWrapper | `modules/parsers/lib_bids_sh_parser.nf` | Execute libBIDS.sh |
| BidsParser | `modules/parsers/lib_bids_sh_parser.nf` | Parse CSV output |
| BidsConfigLoader | `main.nf#L46-L48` | YAML loading |
| BidsConfigAnalyzer | `modules/utils/config_analyzer.nf` | Config analysis |
| PlainSetHandler | `subworkflows/emit_plain_sets.nf` | 1:1 file mapping |
| NamedSetHandler | `subworkflows/emit_named_sets.nf` | Entity grouping |
| SequentialSetHandler | `subworkflows/emit_sequential_sets.nf` | Ordered arrays |
| MixedSetHandler | `subworkflows/emit_mixed_sets.nf` | Nested structures |
| BidsChannelFactory | `main.nf` | Main orchestration |
| BidsHandler | New (async pattern) | Async execution |

### Cross-Modal Broadcasting

**Reference**: `main.nf#L119-L236`

This is the most complex feature:

```groovy
// Logic flow:
// 1. Collect task="NA" data as cross-modal sources
// 2. For task-specific channels, check include_cross_modal
// 3. Add requested cross-modal data to task channels
// 4. Filter out task="NA" channels if data was included elsewhere
```

## Testing Strategy

### Unit Tests

Create tests for each component:

```groovy
// BidsParserTest.groovy
def "test CSV parsing"() { ... }

// PlainSetHandlerTest.groovy
def "test plain set grouping"() { ... }

// BidsConfigAnalyzerTest.groovy
def "test configuration analysis"() { ... }
```

### Integration Tests

Use real BIDS datasets from `tests/data/`:

```bash
# Run validation test
cd plugins/nf-bids
nextflow run validation/test.nf
```

### Expected Outputs

Compare with `tests/expected_outputs/`:
- JSON structure validation
- Entity grouping verification
- File path correctness

## Common Implementation Patterns

### Pattern 1: Entity Extraction

```groovy
// Extract entities from BIDS filename
def extractEntities(String filename) {
    def entities = [:]
    filename.tokenize('_').each { part ->
        if (part.contains('-')) {
            def (key, val) = part.split('-', 2)
            entities[key] = val.replaceAll(/\.[^.]+$/, '')
        }
    }
    return entities
}
```

### Pattern 2: Grouping Key Creation

```groovy
// Build grouping key from entities
def buildGroupingKey(Map entities, List<String> loopOver) {
    return loopOver.collect { entity ->
        entities[entity] ?: 'NA'
    }
}
```

### Pattern 3: File Map Creation

```groovy
// Organize files by suffix
def createFileMap(List rows, Map config) {
    def fileMap = [:]
    rows.each { row ->
        def suffix = extractSuffix(row)
        if (!fileMap.containsKey(suffix)) {
            fileMap[suffix] = []
        }
        fileMap[suffix] << processRow(row)
    }
    return fileMap
}
```

## Debugging Tips

### Enable Verbose Logging

```groovy
// In BidsLogger
void logDebug(String context, String message) {
    if (System.getenv('BIDS_DEBUG')) {
        log.debug("[${context}] ${message}")
    }
}
```

### Trace Channel Data

```groovy
// In BidsChannelFactory
parsedData
    .view { "PARSED: ${it}" }  // Add view operators
    .filter { ... }
    .view { "FILTERED: ${it}" }
```

---

## Testing & Validation

See [testing.md](testing.md) and [TEST_SUITE.md](TEST_SUITE.md) for complete testing documentation.

### Validate libBIDS.sh Integration

```bash
# Test libBIDS.sh output
source libBIDS.sh/libBIDS.sh
libBIDSsh_parse_bids_to_csv tests/data/bids-examples/asl001 > /tmp/test.csv
cat /tmp/test.csv
```

---

**For development workflow, see**: [development.md](development.md)  
**For current priorities, see**: [TODO.md](TODO.md)
10. ⬜ Full integration testing

Good luck with the implementation! 🚀
