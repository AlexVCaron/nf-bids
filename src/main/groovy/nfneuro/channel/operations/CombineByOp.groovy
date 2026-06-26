/*
 * Copyright 2025, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nfneuro.plugin.channel.operations

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.extension.CH
import nextflow.extension.DataflowHelper
import nfneuro.plugin.channel.operations.keys.KeyExtractor

/**
 * Combines two channels by extracting and matching keys.
 *
 * This operator implements combine logic similar to Nextflow's combine(by:) operator,
 * but uses closures to dynamically extract the join key from each item
 * instead of requiring tuple indices.
 *
 * Key features:
 * - Uses closures for flexible key extraction from both channels
 * - Emits fused items (key is used internally for matching and not emitted)
 * - Produces full cartesian product for items with matching keys
 * - Drops unmatched keys (inner join semantics)
 *
 * Use cases:
 * - Combine subjects with sessions by subject ID
 * - Combine scans with parameters by matching fields
 * - Generate all pairwise combinations within groups
 *
 * Example:
 * <pre>
 * subjects.combineBy(
 *     sessions,
 *     { it.subject }     // extract key from both left and right items
 * )
 * .view { fused -> "Combined item: ${fused}" }
 * </pre>
 *
 * @author Alex Valcourt Caron
 */
@Slf4j
@CompileStatic
class CombineByOp {

    private final DataflowReadChannel left
    private final DataflowReadChannel right
    private final Closure leftKeyExtractor
    private final Closure rightKeyExtractor
    private final Map opts
    private DataflowWriteChannel target

    // Buffers to store items grouped by key (thread-safe access required)
    private final Map<Object, List<Object>> leftBuffer = [:]
    private final Map<Object, List<Object>> rightBuffer = [:]

    // Completion tracking
    private boolean leftComplete = false
    private boolean rightComplete = false
    private final Object lock = new Object()

    /**
     * Create a new CombineByOp operator.
     *
     * @param left The left input channel
     * @param right The right input channel
     * @param leftKeyExtractor Closure that extracts the key from left items
     * @param rightKeyExtractor Closure that extracts the key from right items
     * @param opts Optional configuration (reserved for future use: remainder, filter)
     */
    CombineByOp(DataflowReadChannel left,
                DataflowReadChannel right,
                Closure leftKeyExtractor,
                Closure rightKeyExtractor,
                Map opts) {
        this.left = left
        this.right = right
        this.leftKeyExtractor = leftKeyExtractor
        this.rightKeyExtractor = rightKeyExtractor
        this.opts = opts ?: [:]
    }

    CombineByOp(DataflowReadChannel left,
                DataflowReadChannel right,
                Closure leftKeyExtractor,
                Closure rightKeyExtractor) {
        this(left, right, leftKeyExtractor, rightKeyExtractor, [:])
    }

    CombineByOp(DataflowReadChannel left,
                DataflowReadChannel right,
                Closure keyExtractor,
                Map opts) {
        this(left, right, keyExtractor, keyExtractor, opts)
    }

    CombineByOp(DataflowReadChannel left,
                DataflowReadChannel right,
                Closure keyExtractor) {
        this(left, right, keyExtractor, keyExtractor, [:])
    }

    /**
     * Apply the combine operator.
     *
     * @return The target channel containing combined results
     */
    DataflowWriteChannel apply() {
        target = CH.create()

        // Subscribe to both channels
        final Closure leftNext = { item -> onLeftItem(item) }
        final Closure leftDone = { onLeftComplete() }
        final Closure rightNext = { item -> onRightItem(item) }
        final Closure rightDone = { onRightComplete() }

        DataflowHelper.subscribeImpl(left, [onNext: leftNext, onComplete: leftDone])
        DataflowHelper.subscribeImpl(right, [onNext: rightNext, onComplete: rightDone])

        return target
    }

    /**
     * Process an item from the left channel.
     *
     * @param item The item to process
     */
    private void onLeftItem(Object item) {
        synchronized (lock) {
            // Extract key from left item
            Object key = KeyExtractor.extractKey(item, leftKeyExtractor, 'combineBy(left)')

            if (key == null) {
                log.trace("combineBy: Skipping left item with null key: ${item}")
                return
            }

            // Buffer left item by key (use computeIfAbsent for thread safety)
            List<Object> leftList = leftBuffer.computeIfAbsent(key) { k -> [] }
            leftList.add(item)

            // Check right buffer for matching key
            List<Object> rightItems = rightBuffer[key]
            if (rightItems) {
                // Emit cartesian product: this left item × all right items with same key
                rightItems.each { rightItem ->
                    target.bind(fuseItems(item, rightItem))
                }
            }
        }
    }

    /**
     * Process an item from the right channel.
     *
     * @param item The item to process
     */
    private void onRightItem(Object item) {
        synchronized (lock) {
            // Extract key from right item
            Object key = KeyExtractor.extractKey(item, rightKeyExtractor, 'combineBy(right)')

            if (key == null) {
                log.trace("combineBy: Skipping right item with null key: ${item}")
                return
            }

            // Buffer right item by key (use computeIfAbsent for thread safety)
            List<Object> rightList = rightBuffer.computeIfAbsent(key) { k -> [] }
            rightList.add(item)

            // Check left buffer for matching key
            List<Object> leftItems = leftBuffer[key]
            if (leftItems) {
                // Emit cartesian product: this right item × all left items with same key
                leftItems.each { leftItem ->
                    target.bind(fuseItems(leftItem, item))
                }
            }
        }
    }

    /**
     * Called when the left channel is complete.
     */
    private void onLeftComplete() {
        synchronized (lock) {
            log.trace("combineBy: Left channel complete")
            leftComplete = true
            checkCompletion()
        }
    }

    /**
     * Called when the right channel is complete.
     */
    private void onRightComplete() {
        synchronized (lock) {
            log.trace("combineBy: Right channel complete")
            rightComplete = true
            checkCompletion()
        }
    }

    /**
     * Check if both channels are complete and finalize.
     */
    private void checkCompletion() {
        if (!leftComplete || !rightComplete) {
            return
        }

        // Count total combinations emitted (sum of cartesian products per key)
        int totalCombinations = 0
        leftBuffer.each { key, leftItems ->
            List<Object> rightItems = rightBuffer[key]
            if (rightItems) {
                totalCombinations += leftItems.size() * rightItems.size()
            }
        }

        /* groovylint-disable-next-line LineLength */
        log.trace("combineBy: Both channels complete, emitted ${totalCombinations} combinations across ${leftBuffer.keySet().size()} keys")

        // Signal completion
        target.bind(Channel.STOP)
    }

    /**
     * Fuse two matched items into a single emission payload.
     * - Map + Map -> merged map (right-side values take precedence on key collisions)
     * - List + List -> concatenated list
     * - Otherwise -> 2-item list [left, right]
     */
    private static Object fuseItems(Object leftItem, Object rightItem) {
        if (leftItem == null) {
            return rightItem
        }
        if (rightItem == null) {
            return leftItem
        }
        if (leftItem instanceof Map && rightItem instanceof Map) {
            return new LinkedHashMap((Map)leftItem) + (Map)rightItem
        }
        if (leftItem instanceof List && rightItem instanceof List) {
            List<Object> fused = new ArrayList<>((List)leftItem)
            fused.addAll((List)rightItem)
            return fused
        }
        return [leftItem, rightItem]
    }

}
