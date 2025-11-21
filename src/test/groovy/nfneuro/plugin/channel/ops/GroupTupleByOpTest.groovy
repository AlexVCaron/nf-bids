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
 * Unit tests for GroupTupleByOp operator
 * 
 * NOTE: These are SMOKE TESTS only. Attempting to block on DataflowQueue.getVal()
 * in unit tests causes hangs. Comprehensive functional testing is done via
 * integration tests in validation/test_grouptupleby.nf which uses real Nextflow execution.
 * 
 * These tests verify:
 * - Operator instantiation
 * - Parameter handling
 * - Return type correctness
 * - Basic validation logic
 */
class GroupTupleByOpTest extends Specification {

    def 'should create operator instance'() {
        given:
        def input = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new GroupTupleByOp(input, extractor, [:])
        
        then:
        op != null
        op.@keyExtractor == extractor
        op.@opts == [:]
    }
    
    def 'should create operator with options'() {
        given:
        def input = Channel.of([id: 'A'])
        def extractor = { it.id }
        def opts = [size: 2, sort: true, remainder: false]
        
        when:
        def op = new GroupTupleByOp(input, extractor, opts)
        
        then:
        op != null
        op.@opts.size == 2
        op.@opts.sort == true
        op.@opts.remainder == false
    }
    
    def 'should return DataflowQueue from apply'() {
        given:
        def input = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new GroupTupleByOp(input, extractor, [:])
        def result = op.apply()
        
        then:
        result instanceof DataflowQueue
    }
    
    def 'should accept null opts map'() {
        given:
        def input = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new GroupTupleByOp(input, extractor, null)
        
        then:
        op != null
        op.@opts != null  // Should be empty map, not null
    }
    
    def 'should validate options are passed through'() {
        given:
        def input = Channel.of([id: 'A'])
        def extractor = { it.id }
        def opts = [size: 3, sort: true, remainder: false]
        
        when:
        def op = new GroupTupleByOp(input, extractor, opts)
        
        then:
        op.@opts.size == 3
        op.@opts.sort == true
        op.@opts.remainder == false
    }
    
    def 'should initialize empty group maps'() {
        given:
        def input = Channel.of([id: 'A'])
        def extractor = { it.id }
        
        when:
        def op = new GroupTupleByOp(input, extractor, [:])
        
        then:
        op.@groups != null
        op.@groups.isEmpty()
        op.@counts != null
        op.@counts.isEmpty()
    }
    
    def 'should accept different closure types'() {
        given:
        def input = Channel.of([id: 'A', nested: [value: 'B']])
        
        when:
        def op1 = new GroupTupleByOp(input, { it.id }, [:])
        def op2 = new GroupTupleByOp(input, { it.nested.value }, [:])
        def op3 = new GroupTupleByOp(input, { [it.id, it.nested.value] }, [:])
        
        then:
        op1 != null
        op2 != null
        op3 != null
    }
    
    def 'should handle sort option types'() {
        given:
        def input = Channel.of([id: 'A', val: 1])
        
        when:
        def opBool = new GroupTupleByOp(input, { it.id }, [sort: true])
        def opClosure = new GroupTupleByOp(input, { it.id }, [sort: { it.val }])
        def opNone = new GroupTupleByOp(input, { it.id }, [:])
        
        then:
        opBool.@opts.sort == true
        opClosure.@opts.sort instanceof Closure
        opNone.@opts.sort == null
    }
}
