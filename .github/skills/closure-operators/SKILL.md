---
name: closure-operators
description: 'nf-bids closure-based channel operators groupTupleBy, joinBy, combineBy: signatures, return formats, KeyExtractor/CompositeKey internals, design rationale. Use when using, modifying, or extending these operators.'
---

# Closure-based channel operators

## Why they exist

Nextflow plugins cannot overload core operators (`groupTuple`, `join`, `combine`), so nf-bids
registers new closure-based names via `@Operator` methods on `BidsExtension`. Instead of
positional tuple indexes, each operator takes a **key-extractor closure** that receives an item
and returns its key — enabling grouping/joining of maps and arbitrary Groovy objects (like the
`[key, data, meta]` / map structures emitted by `Channel.fromBIDS`). Import before use:

```groovy
include { groupTupleBy; joinBy; combineBy } from 'plugin/nf-bids'
```

Items whose extractor returns `null` are silently skipped (logged at trace level) by all three
operators.

## groupTupleBy

```groovy
channel.groupTupleBy(Closure keyExtractor)
channel.groupTupleBy(Closure keyExtractor, Map opts)
```

Emits `[key, [items...]]` tuples — one per distinct key. Options:

- `size` (Integer): emit a group as soon as it reaches this many items.
- `sort` (Boolean | Closure | Comparator): sort items within each group before emission
  (invalid types log a warning and skip sorting).
- `remainder` (Boolean, default **true**): at source completion, emit groups that never reached
  `size`. Set `false` to drop incomplete groups.

```groovy
channel
    .of([subject: 'sub-01', file: 'a.txt'],
        [subject: 'sub-01', file: 'b.txt'],
        [subject: 'sub-02', file: 'c.txt'])
    .groupTupleBy { it.subject }
// -> ['sub-01', [[subject:'sub-01', ...], [subject:'sub-01', ...]]], ['sub-02', [[...]]]
```

Return a List from the closure for a composite key: `groupTupleBy { [it.subject, it.session] }`.

## joinBy

```groovy
left.joinBy(right, Closure keyExtractor)
left.joinBy(right, Closure keyExtractor, Map opts)
left.joinBy(right, Closure leftKey, Closure rightKey)
left.joinBy(right, Closure leftKey, Closure rightKey, Map opts)
```

`rightKey` defaults to `leftKey`. Emits one **fused item** per matching pair — there is NO
`[key, left, right]` tuple. Fusion rules (`fuseItems`):

- Map + Map → merged map, right-side values win on key collisions.
- List + List → concatenated list.
- Otherwise → 2-item list `[leftItem, rightItem]`.

Duplicate keys produce the cartesian product of matching pairs. Options:

- `remainder` (Boolean, default false): outer join — unmatched items are emitted alone
  (fused with a `null` partner, which yields the item unchanged).

`failOnDuplicate` / `failOnMismatch` are **not implemented**.

```groovy
anat = channel.of([subject: 'sub-01', t1: 't1.nii'])
func = channel.of([subject: 'sub-01', bold: 'bold.nii'])
anat.joinBy(func) { it.subject }
// -> [subject:'sub-01', t1:'t1.nii', bold:'bold.nii']
```

## combineBy

```groovy
left.combineBy(right, Closure keyExtractor)
left.combineBy(right, Closure keyExtractor, Map opts)
left.combineBy(right, Closure leftKey, Closure rightKey)
left.combineBy(right, Closure leftKey, Closure rightKey, Map opts)
```

Cartesian product of left × right items **within each matching key**; same fusion rules as
`joinBy` (fused items, no key tuple). Unmatched keys are always dropped — the `opts` map is
currently reserved for future use (`remainder`, `filter` are accepted but have no effect).

```groovy
subjects = channel.of([id: 'sub-01', age: 25])
sessions = channel.of([id: 'sub-01', session: 'ses-01'], [id: 'sub-01', session: 'ses-02'])
subjects.combineBy(sessions) { it.id }
// -> [id:'sub-01', age:25, session:'ses-01'], [id:'sub-01', age:25, session:'ses-02']
```

## Internals

- **`KeyExtractor`** (static utility):
  - `validateKeyExtractor(closure, operatorName)` — throws `IllegalArgumentException` if the
    closure is null or takes zero parameters; warns if it takes more than two.
  - `extractKey(item, closure, operatorName)` — invokes the closure; `null` key → item skipped;
    a `List` key is wrapped in `CompositeKey`; closure exceptions are rethrown as
    `IllegalStateException` with item context.
- **`CompositeKey`** — wraps a `List` of key parts (defensive copy) with `@EqualsAndHashCode`,
  giving multi-part keys stable equality/hashing for use as buffer-map keys. Exposes `size()`,
  `get(index)`, `parts`.
- All three ops buffer items in maps keyed by extracted key, synchronized on an internal lock,
  and bind `Channel.STOP` when input channel(s) complete.

## Source and test locations

- Registration (`@Operator` methods): `src/main/groovy/nfneuro/plugin/BidsExtension.groovy`.
- Implementations (package `nfneuro.plugin.channel.operations`):
  `src/main/groovy/nfneuro/channel/operations/{GroupTupleByOp,JoinByOp,CombineByOp}.groovy`
  and `keys/{KeyExtractor,CompositeKey}.groovy`.
- Tests: `src/test/groovy/nfneuro/channel/operations/{GroupTupleByOpTest,JoinByOpTest,CombineByOpTest}.groovy`
  and `keys/{KeyExtractorTest,CompositeKeyTest}.groovy`.

## Migration gotchas

When migrating from Nextflow's index-based operators:

1. **`joinBy`/`combineBy` output is a fused item, not a tuple.** The output is a merged map, not `[key, left, right]`.
   Wrong: `anat.joinBy(func) { it.subject }.map { key, l, r -> ... }` — this destructures incorrectly.
   Right: `anat.joinBy(func) { it.subject }.map { fused -> fused.T1w }`.

2. **Items stay whole in `groupTupleBy` lists.** The list contains full original items, not just values.
   Wrong: `items.collect { it[0] }` (tuple index).
   Right: `items.collect { it.nii }` (field name).

3. **Null keys are silently dropped.** Provide a default if a field may be absent:
   ```groovy
   channel.groupTupleBy { [it.subject, it.session ?: 'no-session'] }
   ```

## Performance

Typical overhead for BIDS datasets: **10–30 ms**. A regression >50% warrants investigation.
See benchmark suite: `test/benchmark/benchmark_*.nf`.

## Documentation

- Concept page: `documentation/modules/ROOT/pages/concepts/channel-operators.adoc`.
- Migration guide (index-based → closure-based): `documentation/modules/ROOT/pages/appendices/migration-closure.adoc`.
