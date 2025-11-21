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
 * Unit tests for CombineByOp operator
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
class CombineByOpTest extends Specification {

    def 'should create operator instance without filter'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        
        when:
        def op = new CombineByOp(left, right, null, [:])
        
        then:
        op != null
        op.@filterPredicate == null
        op.@opts == [:]
    }
    
    def 'should create operator instance with filter'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        def filter = { l, r -> true }
        
        when:
        def op = new CombineByOp(left, right, filter, [:])
        
        then:
        op != null
        op.@filterPredicate == filter
    }
    
    def 'should return DataflowQueue from apply'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        
        when:
        def op = new CombineByOp(left, right, null, [:])
        def result = op.apply()
        
        then:
        result instanceof DataflowQueue
    }
    
    def 'should accept null opts map'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        
        when:
        def op = new CombineByOp(left, right, null, null)
        
        then:
        op != null
        op.@opts != null  // Should be empty map, not null
    }
    
    def 'should initialize empty buffers'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        
        when:
        def op = new CombineByOp(left, right, null, [:])
        
        then:
        op.@leftBuffer != null
        op.@leftBuffer.isEmpty()
        op.@rightBuffer != null
        op.@rightBuffer.isEmpty()
    }
    
    def 'should initialize completion flags to false'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        
        when:
        def op = new CombineByOp(left, right, null, [:])
        
        then:
        op.@leftComplete == false
        op.@rightComplete == false
    }
    
    def 'should accept complex filter predicates'() {
        given:
        def left = Channel.of([id: 'sub-01'])
        def right = Channel.of([session: 'ses-01'])
        def filter = { l, r -> 
            l.id.split('-')[1] <= r.session.split('-')[1]
        }
        
        when:
        def op = new CombineByOp(left, right, filter, [:])
        
        then:
        op.@filterPredicate == filter
    }
    
    def 'should accept null filter predicate'() {
        given:
        def left = Channel.of('A', 'B', 'C')
        def right = Channel.of(1, 2, 3)
        
        when:
        def opWithFilter = new CombineByOp(left, right, { l, r -> true }, [:])
        def opWithoutFilter = new CombineByOp(left, right, null, [:])
        
        then:
        opWithFilter.@filterPredicate != null
        opWithoutFilter.@filterPredicate == null
    }
}
