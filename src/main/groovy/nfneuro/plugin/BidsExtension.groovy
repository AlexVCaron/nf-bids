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

package nfneuro.plugin

import groovy.transform.CompileStatic
import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.Session
import nextflow.plugin.extension.Factory
import nextflow.plugin.extension.Operator
import nextflow.plugin.extension.PluginExtensionPoint
import nfneuro.plugin.channel.BidsChannelFactory
import nfneuro.plugin.channel.KeyExtractor
import nfneuro.plugin.channel.ops.GroupTupleByOp
import nfneuro.plugin.channel.ops.JoinByOp
import nfneuro.plugin.channel.ops.CombineByOp

/**
 * Nextflow BIDS plugin extension point.
 * 
 * Provides BIDS dataset parsing via channel factories and 
 * closure-based channel grouping operators.
 * 
 * @author Various contributors
 */
@CompileStatic
class BidsExtension extends PluginExtensionPoint {

    private Session session

    @Override
    protected void init(Session session) {
        this.session = session
    }
    
    // ========================================================================
    // BIDS Channel Factory
    // ========================================================================
    
    /**
     * Parse BIDS dataset and return channel
     * 
     * Creates a channel from a BIDS dataset with structured data grouping
     * according to the provided configuration.
     * 
     * Usage:
     *   Channel.fromBIDS('/path/to/bids/dataset', 'config.yaml', [bids_validation: false])
     * 
     * @param bidsDir Path to BIDS dataset directory
     * @param configPath Path to configuration YAML file (optional)
     * @param options Additional options map (bids_validation, libbids_sh_path, etc.)
     * @return DataflowWriteChannel containing structured BIDS data
     * 
     * @reference Main workflow implementation:
     *            https://github.com/agahkarakuzu/bids2nf/blob/main/main.nf#L20-L56
     */
    @Factory
    DataflowWriteChannel fromBIDS(
        String bidsDir,
        String configPath = null,
        Map options = [:]
    ) {
        return new BidsChannelFactory(session).fromBIDS(bidsDir, configPath, options) as DataflowWriteChannel
    }
    
    // ========================================================================
    // Channel Grouping Operators
    // ========================================================================
    
    /**
     * Group channel items by dynamically extracted keys.
     * 
     * Similar to groupTuple but uses a closure to extract the grouping key
     * from each item, allowing for flexible grouping logic based on computed
     * values, nested fields, or multiple attributes.
     * 
     * @param source The input channel to group
     * @param keyExtractor Closure that receives an item and returns its grouping key
     * @param opts Optional configuration map (size, sort, remainder)
     * @return Channel emitting [key, [items]] tuples
     * 
     * @example
     * <pre>
     * channel
     *   .of([subject: 'sub-01', file: 'a.txt'],
     *       [subject: 'sub-01', file: 'b.txt'],
     *       [subject: 'sub-02', file: 'c.txt'])
     *   .groupTupleBy { it.subject }
     *   // Emits: ['sub-01', [[subject:'sub-01', file:'a.txt'], [subject:'sub-01', file:'b.txt']]]
     *   //        ['sub-02', [[subject:'sub-02', file:'c.txt']]]
     * </pre>
     */
    @Operator
    DataflowWriteChannel groupTupleBy(
        DataflowReadChannel source,
        Closure keyExtractor,
        Map opts = [:]
    ) {
        KeyExtractor.validateKeyExtractor(keyExtractor, 'groupTupleBy')
        
        def op = new GroupTupleByOp(source, keyExtractor, opts)
        return op.apply()
    }
    
    /**
     * Join two channels by dynamically extracted keys.
     * 
     * Similar to join but uses closures to extract matching keys from each channel,
     * allowing for flexible join conditions based on computed values or nested fields.
     * 
     * @param left Left input channel
     * @param right Right input channel
     * @param leftKeyExtractor Closure extracting key from left channel items
     * @param rightKeyExtractor Optional closure for right items (defaults to leftKeyExtractor)
     * @param opts Optional configuration (remainder, failOnDuplicate, failOnMismatch)
     * @return Channel emitting [leftItem, rightItem] pairs where keys match
     * 
     * @example
     * <pre>
     * anatomical = Channel.of([subject: 'sub-01', file: 't1.nii'])
     * functional = Channel.of([subject: 'sub-01', file: 'bold.nii'])
     * 
     * anatomical.joinBy(functional) { it.subject }
     * // Emits: [[subject:'sub-01', file:'t1.nii'], [subject:'sub-01', file:'bold.nii']]
     * </pre>
     */
    @Operator
    DataflowWriteChannel joinBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure leftKeyExtractor,
        Closure rightKeyExtractor = null,
        Map opts = [:]
    ) {
        KeyExtractor.validateKeyExtractor(leftKeyExtractor, 'joinBy')
        
        // Default right extractor to same as left
        def rightExtractor = rightKeyExtractor ?: leftKeyExtractor
        KeyExtractor.validateKeyExtractor(rightExtractor, 'joinBy')
        
        def op = new JoinByOp(left, right, leftKeyExtractor, rightExtractor, opts)
        return op.apply()
    }
    
    /**
     * Combine two channels with optional filtering predicate.
     * 
     * Similar to combine but allows filtering combinations using a predicate closure.
     * Without a filter, produces all combinations (cartesian product).
     * 
     * @param left Left input channel
     * @param right Right input channel
     * @param filterPredicate Optional closure to filter combinations (receives [left, right])
     * @param opts Optional configuration (TBD)
     * @return Channel emitting [leftItem, rightItem] for valid combinations
     * 
     * @example
     * <pre>
     * subjects = Channel.of('sub-01', 'sub-02')
     * sessions = Channel.of('ses-01', 'ses-02')
     * 
     * // Only combine if session number <= subject number
     * subjects.combineBy(sessions) { subj, sess ->
     *   sess.split('-')[1] <= subj.split('-')[1]
     * }
     * </pre>
     */
    @Operator
    DataflowWriteChannel combineBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure filterPredicate = null,
        Map opts = [:]
    ) {
        // Validate filter if provided
        if (filterPredicate != null) {
            int params = filterPredicate.getMaximumNumberOfParameters()
            if (params < 2) {
                throw new IllegalArgumentException(
                    "combineBy: filterPredicate must accept 2 parameters [leftItem, rightItem]\n" +
                    "  Expected: { left, right -> ... }\n" +
                    "  Found: closure with ${params} parameter(s)"
                )
            }
        }
        
        def op = new CombineByOp(left, right, filterPredicate, opts)
        return op.apply()
    }

}
