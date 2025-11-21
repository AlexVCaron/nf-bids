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
     * @return Channel emitting [key, leftItem, rightItem] tuples where keys match
     * 
     * @example
     * <pre>
     * anatomical = Channel.of([subject: 'sub-01', file: 't1.nii'])
     * functional = Channel.of([subject: 'sub-01', file: 'bold.nii'])
     * 
     * anatomical.joinBy(functional) { it.subject }
     * // Emits: ['sub-01', [subject:'sub-01', file:'t1.nii'], [subject:'sub-01', file:'bold.nii']]
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
     * Combine two channels by extracting and matching keys.
     * 
     * Similar to Nextflow's combine(by:) operator but uses closures to extract
     * keys from each item instead of tuple indices. Emits the cartesian product
     * of items within each key group.
     * 
     * @param left Left input channel
     * @param right Right input channel
     * @param leftKeyExtractor Closure that extracts the key from left items
     * @param rightKeyExtractor Closure that extracts the key from right items
     * @param opts Optional configuration (reserved for future: remainder)
     * @return Channel emitting [key, leftItem, rightItem] tuples
     * 
     * @example
     * <pre>
     * subjects = Channel.of(
     *   [id: 'sub-01', age: 25],
     *   [id: 'sub-02', age: 30]
     * )
     * sessions = Channel.of(
     *   [id: 'sub-01', session: 'ses-01'],
     *   [id: 'sub-01', session: 'ses-02'],
     *   [id: 'sub-02', session: 'ses-01']
     * )
     * 
     * // Combine by subject ID (produces cartesian product for duplicates)
     * subjects.combineBy(
     *   sessions,
     *   { it.id },     // extract key from left
     *   { it.id }      // extract key from right
     * )
     * .view { key, subj, sess ->
     *   "Subject ${key}: age=${subj.age}, session=${sess.session}"
     * }
     * </pre>
     */
    @Operator
    DataflowWriteChannel combineBy(
        DataflowReadChannel left,
        DataflowReadChannel right,
        Closure leftKeyExtractor,
        Closure rightKeyExtractor,
        Map opts = [:]
    ) {
        // Validate key extractors
        KeyExtractor.validateKeyExtractor(leftKeyExtractor, 'combineBy(leftKeyExtractor)')
        KeyExtractor.validateKeyExtractor(rightKeyExtractor, 'combineBy(rightKeyExtractor)')
        
        def op = new CombineByOp(left, right, leftKeyExtractor, rightKeyExtractor, opts)
        return op.apply()
    }

}
