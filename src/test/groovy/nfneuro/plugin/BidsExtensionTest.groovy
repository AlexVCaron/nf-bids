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

import groovyx.gpars.dataflow.DataflowQueue
import nextflow.Session
import spock.lang.Specification

/**
 * Unit tests for BidsExtension validation logic.
 * Tests parameter validation for operator methods.
 */
class BidsExtensionTest extends Specification {

    BidsExtension extension
    DataflowQueue channel1
    DataflowQueue channel2

    def setup() {
        extension = new BidsExtension()
        extension.init(Mock(Session))
        channel1 = new DataflowQueue()
        channel2 = new DataflowQueue()
    }

    // ========================================================================
    // groupTupleBy Validation Tests
    // ========================================================================

    def "groupTupleBy validates key extractor is a closure"() {
        when:
        extension.groupTupleBy(channel1, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("keyExtractor closure is required")
    }

    def "groupTupleBy validates key extractor has parameters"() {
        when:
        extension.groupTupleBy(channel1, { -> 'key' })

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("must accept at least one parameter")
    }

    def "groupTupleBy accepts valid key extractor"() {
        when:
        def result = extension.groupTupleBy(channel1, { it.id })

        then:
        noExceptionThrown()
        result != null
    }

    // ========================================================================
    // joinBy Validation Tests
    // ========================================================================

    def "joinBy validates left key extractor is a closure"() {
        when:
        extension.joinBy(channel1, channel2, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("keyExtractor closure is required")
    }

    def "joinBy validates left key extractor has parameters"() {
        when:
        extension.joinBy(channel1, channel2, { -> 'key' })

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("must accept at least one parameter")
    }

    def "joinBy validates right key extractor when provided"() {
        when:
        extension.joinBy(channel1, channel2, { it.id }, { -> 'key' })

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("must accept at least one parameter")
    }

    def "joinBy accepts valid extractors"() {
        when:
        def result = extension.joinBy(channel1, channel2, { it.id })

        then:
        noExceptionThrown()
        result != null
    }

    def "joinBy accepts different extractors for left and right"() {
        when:
        def result = extension.joinBy(channel1, channel2, { it.subjectId }, { it.subject })

        then:
        noExceptionThrown()
        result != null
    }

    // ========================================================================
    // combineBy Validation Tests
    // ========================================================================

    def "combineBy accepts null filter predicate"() {
        when:
        def result = extension.combineBy(channel1, channel2, null)

        then:
        noExceptionThrown()
        result != null
    }

    def "combineBy validates filter has at least 2 parameters"() {
        when:
        extension.combineBy(channel1, channel2, { left -> true })

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("filterPredicate must accept 2 parameters")
        e.message.contains("Found: closure with 1 parameter")
    }

    def "combineBy rejects filter with no parameters"() {
        when:
        extension.combineBy(channel1, channel2, { -> true })

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("filterPredicate must accept 2 parameters")
        e.message.contains("Found: closure with 0 parameter")
    }

    def "combineBy accepts valid filter predicate with 2 parameters"() {
        when:
        def result = extension.combineBy(channel1, channel2, { left, right -> left.id == right.id })

        then:
        noExceptionThrown()
        result != null
    }

    def "combineBy accepts filter predicate with more than 2 parameters"() {
        when:
        def result = extension.combineBy(channel1, channel2, { left, right, extra -> left.id == right.id })

        then:
        noExceptionThrown()
        result != null
    }
}
