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

import nextflow.Channel
import spock.lang.Specification
import groovyx.gpars.dataflow.DataflowQueue

/**
 * Unit tests for JoinByOp operator
 * 
 * NOTE: These are SMOKE TESTS only. Attempting to block on DataflowQueue.getVal()
 * in unit tests causes hangs. Comprehensive functional testing should be done via
 * integration tests with real Nextflow execution.
 * 
 * These tests verify:
 * - Operator instantiation
 * - Parameter handling
 * - Return type correctness
 * - Basic validation logic
 */
class JoinByOpTest extends Specification {

    def 'should create operator instance'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'A'])
        def leftExtractor = { it.id }
        def rightExtractor = { it.id }
        
        when:
        def op = new JoinByOp(left, right, leftExtractor, rightExtractor, [:])
        
        then:
        op != null
        op.@leftKeyExtractor == leftExtractor
        op.@rightKeyExtractor == rightExtractor
        op.@opts == [:]
    }
    
    def 'should create operator with options'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'A'])
        def extractor = { it.id }
        def opts = [remainder: true]
        
        when:
        def op = new JoinByOp(left, right, extractor, extractor, opts)
        
        then:
        op != null
        op.@opts.remainder == true
    }
    
    def 'should return DataflowQueue from apply'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new JoinByOp(left, right, extractor, extractor, [:])
        def result = op.apply()
        
        then:
        result instanceof DataflowQueue
    }
    
    def 'should accept null opts map'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new JoinByOp(left, right, extractor, extractor, null)
        
        then:
        op != null
        op.@opts != null  // Should be empty map, not null
    }
    
    def 'should initialize empty buffers'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new JoinByOp(left, right, extractor, extractor, [:])
        
        then:
        op.@leftBuffer != null
        op.@leftBuffer.isEmpty()
        op.@rightBuffer != null
        op.@rightBuffer.isEmpty()
        op.@matchedKeys != null
        op.@matchedKeys.isEmpty()
    }
    
    def 'should initialize completion flags to false'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new JoinByOp(left, right, extractor, extractor, [:])
        
        then:
        op.@leftComplete == false
        op.@rightComplete == false
    }
    
    def 'should accept different extractors for left and right'() {
        given:
        def left = Channel.of([subject: 'sub-01'])
        def right = Channel.of([participant: 'sub-01'])
        def leftExtractor = { it.subject }
        def rightExtractor = { it.participant }
        
        when:
        def op = new JoinByOp(left, right, leftExtractor, rightExtractor, [:])
        
        then:
        op.@leftKeyExtractor == leftExtractor
        op.@rightKeyExtractor == rightExtractor
    }
    
    def 'should handle remainder option'() {
        given:
        def left = Channel.of([id: 'A'])
        def right = Channel.of([id: 'B'])
        def extractor = { it.id }
        
        when:
        def opWithRemainder = new JoinByOp(left, right, extractor, extractor, [remainder: true])
        def opWithoutRemainder = new JoinByOp(left, right, extractor, extractor, [remainder: false])
        def opDefault = new JoinByOp(left, right, extractor, extractor, [:])
        
        then:
        opWithRemainder.@opts.remainder == true
        opWithoutRemainder.@opts.remainder == false
        opDefault.@opts.remainder == null  // Default handled in checkCompletion
    }
}
