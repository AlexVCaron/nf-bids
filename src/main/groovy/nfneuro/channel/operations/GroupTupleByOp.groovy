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
 * Groups channel items by closure-extracted keys.
 *
 * This operator implements grouping logic similar to Nextflow's groupTuple,
 * but uses a closure to dynamically extract the grouping key from each item
 * instead of requiring tuple indices.
 *
 * @author Alex Valcourt Caron
 */
@Slf4j
@CompileStatic
class GroupTupleByOp {

    private final DataflowReadChannel source
    private final Closure keyExtractor
    private final Map opts
    private DataflowWriteChannel target

    // Grouping state
    private final Map<Object, List<Object>> groups = [:]
    private final Map<Object, Integer> counts = [:]
    private final Object lock = new Object()

    /**
     * Create a new GroupTupleByOp operator.
     *
     * @param source The input channel to group
     * @param keyExtractor Closure that extracts the grouping key from each item
     * @param opts Optional configuration (size, sort, remainder)
     */
    GroupTupleByOp(DataflowReadChannel source, Closure keyExtractor, Map opts) {
        this.source = source
        this.keyExtractor = keyExtractor
        this.opts = opts ?: [:]
    }

    GroupTupleByOp(DataflowReadChannel source, Closure keyExtractor) {
        this(source, keyExtractor, [:])
    }

    /**
     * Apply the grouping operator.
     *
     * @return The target channel containing grouped results
     */
    DataflowWriteChannel apply() {
        target = CH.createBy(source)

        // Process items and emit groups
        final Closure next = { item -> processItem(item) }
        final Closure done = { completeGrouping() }

        // Subscribe to source channel
        DataflowHelper.subscribeImpl(source, [onNext: next, onComplete: done])

        return target
    }

    /**
     * Process each item from the source channel.
     *
     * @param item The item to process
     */
    private void processItem(Object item) {
        synchronized (lock) {
            // Extract key using the keyExtractor closure
            Object key = KeyExtractor.extractKey(item, keyExtractor, 'groupTupleBy')

            if (key == null) {
                log.trace("groupTupleBy: Skipping item with null key: ${item}")
                return
            }

            // Initialize group if not exists (use computeIfAbsent for thread safety)
            List<Object> itemList = groups.computeIfAbsent(key) { k -> [] }

            // Add item to group
            itemList.add(item)

            // Increment count atomically
            counts[key] = (counts[key] ?: 0) + 1

            // Check if group is complete (size option)
            Integer expectedSize = opts.size as Integer
            if (expectedSize != null && counts[key] >= expectedSize) {
                emitGroup(key)
            }
        }
    }

    /**
     * Called when the source channel is complete.
     * Emits all remaining groups if remainder option is true.
     */
    private void completeGrouping() {
        synchronized (lock) {
            // Check remainder option (default true)
            boolean remainder = opts.remainder != null ? opts.remainder : true

            if (remainder) {
                // Emit all remaining groups
                groups.keySet().toList().each { key ->
                    emitGroup(key)
                }
            }

            // Signal completion
            target.bind(Channel.STOP)
        }
    }

    /**
     * Emit a group to the target channel.
     *
     * @param key The grouping key
     */
    private void emitGroup(Object key) {
        synchronized (lock) {
            List<Object> items = groups.remove(key)
            counts.remove(key)

            if (items == null || items.isEmpty()) {
                return
            }

            // Apply sort if requested
            if (opts.sort) {
                if (opts.sort instanceof Boolean && opts.sort) {
                    // Natural sort
                    items = items.sort()
                } else if (opts.sort instanceof Closure) {
                    // Sort with closure
                    items = items.sort(opts.sort as Closure)
                } else if (opts.sort instanceof Comparator) {
                    // Sort with comparator
                    items.sort(opts.sort as Comparator)
                } else {
                    log.warn("groupTupleBy: Invalid sort option type: ${opts.sort.class.name}, skipping sort")
                }
            }

            // Emit [key, items] tuple
            target.bind([key, items])
        }
    }

}
