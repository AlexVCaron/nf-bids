# Benchmark Results

Generated: 2026-07-08 18:16:16 UTC

## benchmark_grouptuple.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_grouptuple.nf` [suspicious_kilby] DSL2 - revision: d740764823

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
groupTupleBy (100 items): 321ms
groupTuple (100 items): 476ms
groupTupleBy (1,000 items): 396ms
groupTupleBy semantic (1,000 items): 233ms
groupTuple (1,000 items): 504ms
groupTupleBy (10,000 items): 528ms
groupTuple (10,000 items): 915ms

## benchmark_join.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_join.nf` [angry_brattain] DSL2 - revision: c7913de808

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
join (100 items): 579ms
joinBy (100 items): 460ms
join (1,000 items): 735ms
joinBy (1,000 items): 1180ms
joinBy different extractors (1,000 items): 1078ms
joinBy semantic (1,000 items): 1138ms
join (10,000 items): 1727ms
joinBy (10,000 items): 3320ms

## benchmark_combine.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_combine.nf` [desperate_lamarck] DSL2 - revision: 6fcc21b1c4

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
combine (10×10 = 100 combinations): 212ms
combineBy (5 keys, 2×2 per key = 20 items): 126ms
combineBy BIDS (30 subjects, 1×2 per subject = 60 items): 121ms
combineBy (10 keys, 6×6 per key = 360 items): 152ms
combineBy (20 keys, 10×10 per key = 2000 items): 213ms

## benchmark_combineby_new.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_combineby_new.nf` [pensive_dalembert] DSL2 - revision: d45186c24a


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
Result: 80 combinations (expected: 80) in 217ms
Result: 360 combinations (expected: 360) in 97ms

