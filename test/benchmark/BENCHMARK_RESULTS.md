# Benchmark Results

Generated: 2026-07-06 16:07:38 UTC

## benchmark_grouptuple.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_grouptuple.nf` [cheesy_descartes] DSL2 - revision: d740764823

==========================================
Performance Benchmark: groupTuple vs groupTupleBy
==========================================
Nextflow version: 25.10.0
Plugin: nf-bids

==========================================
TEST: groupTuple with 100 items
==========================================

==========================================
TEST: groupTupleBy with 100 items
==========================================

==========================================
TEST: groupTuple with 1,000 items
==========================================

==========================================
TEST: groupTupleBy with 1,000 items
==========================================

==========================================
TEST: groupTuple with 10,000 items
==========================================

==========================================
TEST: groupTupleBy with 10,000 items
==========================================

==========================================
TEST: groupTupleBy with semantic keys (1,000 items)
==========================================

==========================================
Benchmark Complete
==========================================
groupTupleBy (100 items): 235ms
groupTuple (100 items): 406ms
groupTupleBy (1,000 items): 301ms
groupTuple (1,000 items): 387ms
groupTupleBy semantic (1,000 items): 219ms
groupTupleBy (10,000 items): 523ms
groupTuple (10,000 items): 762ms

## benchmark_join.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_join.nf` [desperate_brenner] DSL2 - revision: c7913de808

==========================================
Performance Benchmark: join vs joinBy
==========================================
Nextflow version: 25.10.0
Plugin: nf-bids

==========================================
TEST: join with 100 items per channel
==========================================

==========================================
TEST: joinBy with 100 items per channel
==========================================

==========================================
TEST: join with 1,000 items per channel
==========================================

==========================================
TEST: joinBy with 1,000 items per channel
==========================================

==========================================
TEST: join with 10,000 items per channel
==========================================

==========================================
TEST: joinBy with 10,000 items per channel
==========================================

==========================================
TEST: joinBy with semantic keys (1,000 items)
==========================================

==========================================
TEST: joinBy with different extractors (1,000 items)
==========================================

==========================================
Benchmark Complete
==========================================
joinBy (100 items): 394ms
join (100 items): 532ms
join (1,000 items): 796ms
joinBy different extractors (1,000 items): 1022ms
joinBy (1,000 items): 1321ms
joinBy semantic (1,000 items): 1084ms
join (10,000 items): 1729ms
joinBy (10,000 items): 3374ms

## benchmark_combine.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_combine.nf` [focused_panini] DSL2 - revision: 6fcc21b1c4

==========================================
Performance Benchmark: combineBy (0.1.0-beta.5)
==========================================
Nextflow version: 25.10.0
Plugin: nf-bids
Testing: Key-based combination with cartesian product

==========================================
BASELINE: Standard combine with 10x10 items (full cartesian)
==========================================

==========================================
TEST: combineBy with 10x10 items (5 keys, 2 items/key)
==========================================

==========================================
TEST: combineBy with 60x60 items, 10 keys (6 items/key)
==========================================

==========================================
TEST: combineBy with 200x200 items, 20 keys (10 items/key)
==========================================

==========================================
TEST: BIDS-like subject × session pairing (30 subjects, 2 sessions each)
==========================================

==========================================
Benchmark Complete
==========================================
combine (10×10 = 100 combinations): 162ms
combineBy (5 keys, 2×2 per key = 20 items): 78ms
combineBy BIDS (30 subjects, 1×2 per subject = 60 items): 58ms
combineBy (10 keys, 6×6 per key = 360 items): 87ms
combineBy (20 keys, 10×10 per key = 2000 items): 149ms

## benchmark_combineby_new.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_combineby_new.nf` [scruffy_fermi] DSL2 - revision: 32b101ca71


==========================================
CombineBy Performance Benchmark (0.1.0-beta.5+)
==========================================

=== Test 1: 20 items, 5 keys (2 left × 2 right per key) ===

=== Test 2: 60 items, 10 keys (3 left × 3 right per key) ===

=== Test 3: BIDS-like scenario (subjects × sessions) ===

==========================================
Benchmark Complete
==========================================
Result: 40 subject-session pairs (expected: 40) in 57ms
Result: 80 combinations (expected: 80) in 187ms
Result: 360 combinations (expected: 540) in 85ms

