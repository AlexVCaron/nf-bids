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

/**
 * Combines two channels with optional filtering.
 * 
 * This operator produces the cartesian product of two channels,
 * optionally filtered by a predicate closure. Without a filter,
 * all possible [left, right] combinations are emitted.
 * 
 * Use cases:
 * - Combine subjects with sessions
 * - Combine parameters with datasets
 * - Generate all pairwise combinations with custom filtering
 * 
 * @author Alex Valcourt Caron
 */
@Slf4j
@CompileStatic
class CombineByOp {
    
    private final DataflowReadChannel left
    private final DataflowReadChannel right
    private final Closure filterPredicate
    private final Map opts
    private DataflowWriteChannel target
    
    // Buffers to store all items from both channels
    private final List<Object> leftBuffer = []
    private final List<Object> rightBuffer = []
    
    // Completion tracking
    private boolean leftComplete = false
    private boolean rightComplete = false
    
    /**
     * Create a new CombineByOp operator.
     * 
     * @param left The left input channel
     * @param right The right input channel
     * @param filterPredicate Optional closure to filter combinations (receives [left, right])
     * @param opts Optional configuration (reserved for future use)
     */
    CombineByOp(DataflowReadChannel left, 
                DataflowReadChannel right,
                Closure filterPredicate,
                Map opts) {
        this.left = left
        this.right = right
        this.filterPredicate = filterPredicate
        this.opts = opts ?: [:]
    }
    
    /**
     * Apply the combine operator.
     * 
     * @return The target channel containing combined results
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
        // Add to buffer
        leftBuffer.add(item)
        
        // Combine with all existing right items
        rightBuffer.each { rightItem ->
            emitIfValid(item, rightItem)
        }
    }
    
    /**
     * Process an item from the right channel.
     * 
     * @param item The item to process
     */
    private synchronized void onRightItem(Object item) {
        // Add to buffer
        rightBuffer.add(item)
        
        // Combine with all existing left items
        leftBuffer.each { leftItem ->
            emitIfValid(leftItem, item)
        }
    }
    
    /**
     * Emit combination if it passes the filter (or if no filter).
     * 
     * @param leftItem The left item
     * @param rightItem The right item
     */
    private void emitIfValid(Object leftItem, Object rightItem) {
        try {
            // If no filter, emit all combinations
            if (filterPredicate == null) {
                target.bind([leftItem, rightItem])
                return
            }
            
            // Apply filter predicate
            def result = filterPredicate.call(leftItem, rightItem)
            
            // Emit if predicate returns true
            if (result) {
                target.bind([leftItem, rightItem])
            }
        } catch (Exception e) {
            log.error("combineBy: Error evaluating filter predicate for items [${leftItem}, ${rightItem}]: ${e.message}", e)
            throw new IllegalStateException(
                "combineBy: Filter predicate failed\n" +
                "  Left item: ${leftItem}\n" +
                "  Right item: ${rightItem}\n" +
                "  Error: ${e.message}",
                e
            )
        }
    }
    
    /**
     * Called when the left channel is complete.
     */
    private synchronized void onLeftComplete() {
        log.trace("combineBy: Left channel complete")
        leftComplete = true
        checkCompletion()
    }
    
    /**
     * Called when the right channel is complete.
     */
    private synchronized void onRightComplete() {
        log.trace("combineBy: Right channel complete")
        rightComplete = true
        checkCompletion()
    }
    
    /**
     * Check if both channels are complete and finalize.
     */
    private void checkCompletion() {
        if (!leftComplete || !rightComplete) {
            return
        }
        
        log.trace("combineBy: Both channels complete, emitted ${leftBuffer.size() * rightBuffer.size()} total combinations")
        
        // Signal completion
        target.bind(Channel.STOP)
    }
}
