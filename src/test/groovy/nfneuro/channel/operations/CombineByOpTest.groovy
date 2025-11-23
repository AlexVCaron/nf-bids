/* groovylint-disable all */
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
 * - Operator instantiation with key extractors
 * - Parameter handling and options
 * - Return type correctness
 * - Buffer initialization
 * - Completion tracking
 */
class CombineByOpTest extends Specification {

    def 'should create operator instance with single key extractor'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        def keyExtractor = { it.id }

        when:
        def op = new CombineByOp(left, right, keyExtractor)

        then:
        op != null
        op.@leftKeyExtractor == keyExtractor
        op.@rightKeyExtractor == keyExtractor
    }

    def 'should create operator instance with separate key extractors'() {
        given:
        def left = Channel.of([id: 'sub-01'])
        def right = Channel.of([subject: 'sub-01'])
        def leftExtractor = { it.id }
        def rightExtractor = { it.subject }

        when:
        def op = new CombineByOp(left, right, leftExtractor, rightExtractor)

        then:
        op != null
        op.@leftKeyExtractor == leftExtractor
        op.@rightKeyExtractor == rightExtractor
    }

    def 'should return DataflowQueue from apply'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)

        when:
        def op = new CombineByOp(left, right, { it })
        def result = op.apply()

        then:
        result instanceof DataflowQueue
    }

    def 'should accept null opts map and initialize to empty'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)

        when:
        def op = new CombineByOp(left, right, { it }, null)

        then:
        op != null
        op.@opts != null
        op.@opts.isEmpty()
    }

    def 'should initialize empty buffers'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)

        when:
        def op = new CombineByOp(left, right, { it }, [:])

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
        def op = new CombineByOp(left, right, { it }, [:])

        then:
        op.@leftComplete == false
        op.@rightComplete == false
    }

    def 'should accept complex key extractor closures'() {
        given:
        def left = Channel.of([id: 'sub-01', suffix: 'T1w'])
        def right = Channel.of([subject: 'sub-01', datatype: 'anat'])
        def leftExtractor = { it.id }
        def rightExtractor = { it.subject }

        when:
        def op = new CombineByOp(left, right, leftExtractor, rightExtractor)

        then:
        op.@leftKeyExtractor == leftExtractor
        op.@rightKeyExtractor == rightExtractor
    }

    def 'should support same key extractor for both channels'() {
        given:
        def left = Channel.of([subject: 'sub-01'], [subject: 'sub-02'])
        def right = Channel.of([subject: 'sub-01'], [subject: 'sub-03'])
        def keyExtractor = { it.subject }

        when:
        def op = new CombineByOp(left, right, keyExtractor)

        then:
        op.@leftKeyExtractor == keyExtractor
        op.@rightKeyExtractor == keyExtractor
    }

    def 'should support numeric key extraction'() {
        given:
        def left = Channel.of([id: 1], [id: 2])
        def right = Channel.of([id: 1], [id: 3])
        def keyExtractor = { it.id }

        when:
        def op = new CombineByOp(left, right, keyExtractor)

        then:
        op.@leftKeyExtractor == keyExtractor
        op.@rightKeyExtractor == keyExtractor
    }

    def 'should support string key extraction with manipulation'() {
        given:
        def left = Channel.of('sub-01_ses-01', 'sub-02_ses-01')
        def right = Channel.of('sub-01_bold', 'sub-03_bold')
        def leftExtractor = { it.split('_')[0] }  // Extract subject
        def rightExtractor = { it.split('_')[0] }  // Extract subject

        when:
        def op = new CombineByOp(left, right, leftExtractor, rightExtractor)

        then:
        op.@leftKeyExtractor == leftExtractor
        op.@rightKeyExtractor == rightExtractor
    }

    def 'should accept options map'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        def opts = [remainder: true, filter: true]

        when:
        def op = new CombineByOp(left, right, { it }, opts)

        then:
        op.@opts == opts
        op.@opts.remainder == true
        op.@opts.filter == true
    }

    def 'should support all four constructor signatures'() {
        given:
        def left = Channel.of('A')
        def right = Channel.of(1)
        def leftExt = { it.left }
        def rightExt = { it.right }
        def opts = [:]

        when:
        // 4 args: left, right, leftExt, rightExt, opts
        def op1 = new CombineByOp(left, right, leftExt, rightExt, opts)
        // 3 args: left, right, leftExt, rightExt
        def op2 = new CombineByOp(left, right, leftExt, rightExt)
        // 3 args: left, right, keyExt, opts
        def op3 = new CombineByOp(left, right, leftExt, opts)
        // 2 args: left, right, keyExt
        def op4 = new CombineByOp(left, right, leftExt)

        then:
        op1.@leftKeyExtractor == leftExt
        op1.@rightKeyExtractor == rightExt
        op1.@opts == opts

        op2.@leftKeyExtractor == leftExt
        op2.@rightKeyExtractor == rightExt
        op2.@opts.isEmpty()

        op3.@leftKeyExtractor == leftExt
        op3.@rightKeyExtractor == leftExt
        op3.@opts == opts

        op4.@leftKeyExtractor == leftExt
        op4.@rightKeyExtractor == leftExt
        op4.@opts.isEmpty()
    }

}
