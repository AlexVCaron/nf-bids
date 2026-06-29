# Benchmark Results

Generated: 2026-06-29 16:21:17 UTC

## benchmark_grouptuple.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_grouptuple.nf` [big_ritchie] DSL2 - revision: e63eabcf1d

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
groupTupleBy (100 items): 273ms
groupTuple (100 items): 420ms
groupTupleBy (1,000 items): 312ms
groupTupleBy semantic (1,000 items): 212ms
groupTuple (1,000 items): 446ms
groupTupleBy (10,000 items): 576ms
groupTuple (10,000 items): 794ms

## benchmark_join.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_join.nf` [sad_elion] DSL2 - revision: 46d6ec9a9c

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
joinBy (100 items): 349ms
join (100 items): 473ms
join (1,000 items): 749ms
joinBy different extractors (1,000 items): 851ms
joinBy (1,000 items): 1071ms
joinBy semantic (1,000 items): 905ms
join (10,000 items): 2107ms
joinBy (10,000 items): 3275ms

## benchmark_combine.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_combine.nf` [high_varahamihira] DSL2 - revision: 75a04b8c81

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
combineBy (5 keys, 2×2 per key = 20 items): 114ms
combine (10×10 = 100 combinations): 240ms
combineBy BIDS (30 subjects, 1×2 per subject = 60 items): 72ms
combineBy (10 keys, 6×6 per key = 360 items): 101ms
combineBy (20 keys, 10×10 per key = 2000 items): 129ms

## benchmark_combineby_new.nf


 N E X T F L O W   ~  version 25.10.0

Launching `benchmark_combineby_new.nf` [furious_kowalevski] DSL2 - revision: 764f7a5fd4


==========================================
CombineBy Performance Benchmark (0.1.0-beta.5+)
==========================================

=== Test 1: 20 items, 5 keys (2 left × 2 right per key) ===

=== Test 2: 60 items, 10 keys (3 left × 3 right per key) ===

=== Test 3: BIDS-like scenario (subjects × sessions) ===

==========================================
Benchmark Complete
==========================================
Result: 40 subject-session pairs (expected: 40) in 41ms
Result: 80 combinations (expected: 80) in 136ms
Result: 360 combinations (expected: 540) in 68ms

