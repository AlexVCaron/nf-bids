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

package nfneuro.plugin.channel.ops

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Channel
import nextflow.extension.CH
import nextflow.extension.DataflowHelper
import nfneuro.plugin.channel.KeyExtractor

/**
 * Joins two channels by closure-extracted keys.
 * 
 * This operator implements join logic similar to Nextflow's join operator,
 * but uses closures to dynamically extract the join key from each item
 * instead of requiring tuple indices.
 * 
 * Supports:
 * - Different key extractors for left and right channels
 * - Cartesian product for duplicate keys
 * - Remainder option for outer join (emit unmatched items with null partner)
 * 
 * @author Alex Valcourt Caron
 */
@Slf4j
@CompileStatic
class JoinByOp {
    
    private final DataflowReadChannel left
    private final DataflowReadChannel right
    private final Closure leftKeyExtractor
    private final Closure rightKeyExtractor
    private final Map opts
    private DataflowWriteChannel target
    
    // Join state (thread-safe access required)
    private final Map<Object, List<Object>> leftBuffer = new HashMap<>()
    private final Map<Object, List<Object>> rightBuffer = new HashMap<>()
    private final Set<Object> matchedKeys = new HashSet<>()
    
    // Completion tracking
    private boolean leftComplete = false
    private boolean rightComplete = false
    
    /**
     * Create a new JoinByOp operator.
     * 
     * @param left The left input channel
     * @param right The right input channel
     * @param leftKeyExtractor Closure that extracts the join key from left items
     * @param rightKeyExtractor Closure that extracts the join key from right items
     * @param opts Optional configuration (remainder for outer join)
     */
    JoinByOp(DataflowReadChannel left, 
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
    
    /**
     * Apply the join operator.
     * 
     * @return The target channel containing joined results
     */
    DataflowWriteChannel apply() {
        target = CH.create()
        
        // Subscribe to both channels
        final leftNext = { item -> onLeftItem(item) }
        final leftDone = { onLeftComplete() }
        final rightNext = { item -> onRightItem(item) }
        final rightDone = { onRightComplete() }
        
        DataflowHelper.subscribeImpl(left, [onNext: leftNext, onComplete: leftDone])
        DataflowHelper.subscribeImpl(right, [onNext: rightNext, onComplete: rightDone])
        
        return target
    }
    
    /**
     * Process an item from the left channel.
     * 
     * @param item The item to process
     */
    private synchronized void onLeftItem(Object item) {
        // Extract key
        def key = KeyExtractor.extractKey(item, leftKeyExtractor, 'joinBy(left)')
        
        if (key == null) {
            log.trace("joinBy: Skipping left item with null key: ${item}")
            return
        }
        
        // Buffer left item
        if (!leftBuffer.containsKey(key)) {
            leftBuffer[key] = []
        }
        leftBuffer[key].add(item)
        
        // Check right buffer for matches
        def rightItems = rightBuffer[key]
        if (rightItems) {
            // Emit cartesian product of left item with all matching right items
            rightItems.each { rightItem ->
                target.bind([item, rightItem])
            }
            matchedKeys.add(key)
        }
    }
    
    /**
     * Process an item from the right channel.
     * 
     * @param item The item to process
     */
    private synchronized void onRightItem(Object item) {
        // Extract key
        def key = KeyExtractor.extractKey(item, rightKeyExtractor, 'joinBy(right)')
        
        if (key == null) {
            log.trace("joinBy: Skipping right item with null key: ${item}")
            return
        }
        
        // Buffer right item
        if (!rightBuffer.containsKey(key)) {
            rightBuffer[key] = []
        }
        rightBuffer[key].add(item)
        
        // Check left buffer for matches
        def leftItems = leftBuffer[key]
        if (leftItems) {
            // Emit cartesian product of right item with all matching left items
            leftItems.each { leftItem ->
                target.bind([leftItem, item])
            }
            matchedKeys.add(key)
        }
    }
    
    /**
     * Called when the left channel is complete.
     */
    private synchronized void onLeftComplete() {
        log.trace("joinBy: Left channel complete")
        leftComplete = true
        checkCompletion()
    }
    
    /**
     * Called when the right channel is complete.
     */
    private synchronized void onRightComplete() {
        log.trace("joinBy: Right channel complete")
        rightComplete = true
        checkCompletion()
    }
    
    /**
     * Check if both channels are complete and emit remainder if needed.
     */
    private void checkCompletion() {
        if (!leftComplete || !rightComplete) {
            return
        }
        
        log.trace("joinBy: Both channels complete")
        
        // Check remainder option (default false for inner join)
        def remainder = opts.remainder ?: false
        
        if (remainder) {
            emitRemainder()
        }
        
        // Signal completion
        target.bind(Channel.STOP)
    }
    
    /**
     * Emit unmatched items with null partner (outer join).
     */
    private void emitRemainder() {
        // Emit unmatched left items
        leftBuffer.each { key, items ->
            if (!matchedKeys.contains(key)) {
                items.each { leftItem ->
                    target.bind([leftItem, null])
                }
            }
        }
        
        // Emit unmatched right items
        rightBuffer.each { key, items ->
            if (!matchedKeys.contains(key)) {
                items.each { rightItem ->
                    target.bind([null, rightItem])
                }
            }
        }
    }
}
