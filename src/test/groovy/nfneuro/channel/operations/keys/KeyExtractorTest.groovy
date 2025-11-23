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

package nfneuro.plugin.channel.operations.keys

import spock.lang.Specification

/**
 * Unit tests for KeyExtractor utility class
 */
class KeyExtractorTest extends Specification {

    def 'should extract simple key'() {
        given:
        def item = [id: 'A', value: 123]
        def extractor = { it.id }
        
        when:
        def key = KeyExtractor.extractKey(item, extractor, 'test')
        
        then:
        key == 'A'
    }
    
    def 'should extract nested field'() {
        given:
        def item = [metadata: [subject: 'sub-01', session: 'ses-01']]
        def extractor = { it.metadata.subject }
        
        when:
        def key = KeyExtractor.extractKey(item, extractor, 'test')
        
        then:
        key == 'sub-01'
    }
    
    def 'should extract computed value'() {
        given:
        def item = [path: '/data/sub-01/file.txt']
        def extractor = { it.path.split('/')[2] }
        
        when:
        def key = KeyExtractor.extractKey(item, extractor, 'test')
        
        then:
        key == 'sub-01'
    }
    
    def 'should wrap list keys in CompositeKey'() {
        given:
        def item = [subject: 'sub-01', session: 'ses-01']
        def extractor = { [it.subject, it.session] }
        
        when:
        def key = KeyExtractor.extractKey(item, extractor, 'test')
        
        then:
        key instanceof CompositeKey
        (key as CompositeKey).size() == 2
        (key as CompositeKey).get(0) == 'sub-01'
        (key as CompositeKey).get(1) == 'ses-01'
    }
    
    def 'should return null for null key'() {
        given:
        def item = [id: null]
        def extractor = { it.id }
        
        when:
        def key = KeyExtractor.extractKey(item, extractor, 'test')
        
        then:
        key == null
    }
    
    def 'should throw exception when keyExtractor is null'() {
        when:
        KeyExtractor.extractKey([id: 'A'], null, 'test')
        
        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains('keyExtractor cannot be null')
    }
    
    def 'should throw exception when keyExtractor fails'() {
        given:
        def item = [id: 'A']
        def extractor = { it.nonExistentField.value }
        
        when:
        KeyExtractor.extractKey(item, extractor, 'testOp')
        
        then:
        def e = thrown(IllegalStateException)
        e.message.contains('testOp')
        e.message.contains('keyExtractor failed')
    }
    
    def 'should validate keyExtractor is not null'() {
        when:
        KeyExtractor.validateKeyExtractor(null, 'testOp')
        
        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains('keyExtractor closure is required')
    }
    
    def 'should reject zero-arity closure'() {
        given:
        def extractor = { -> 'constant' }
        
        when:
        KeyExtractor.validateKeyExtractor(extractor, 'testOp')
        
        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains('must accept at least one parameter')
    }
    
    def 'should accept single-parameter closure'() {
        given:
        def extractor = { it.id }
        
        when:
        KeyExtractor.validateKeyExtractor(extractor, 'testOp')
        
        then:
        noExceptionThrown()
    }
    
    def 'should accept two-parameter closure'() {
        given:
        def extractor = { item, index -> item.id }
        
        when:
        KeyExtractor.validateKeyExtractor(extractor, 'testOp')
        
        then:
        noExceptionThrown()
    }
    
    def 'should warn about multi-parameter closure'() {
        given:
        def extractor = { a, b, c -> a }
        
        when:
        KeyExtractor.validateKeyExtractor(extractor, 'testOp')
        
        then:
        noExceptionThrown()
        // Note: Warning is logged, not thrown
    }
    
    def 'should compare keys for equality'() {
        expect:
        KeyExtractor.keysEqual('A', 'A')
        !KeyExtractor.keysEqual('A', 'B')
        KeyExtractor.keysEqual(123, 123)
        !KeyExtractor.keysEqual(123, 456)
    }
    
    def 'should handle null in keysEqual'() {
        expect:
        !KeyExtractor.keysEqual(null, 'A')
        !KeyExtractor.keysEqual('A', null)
        !KeyExtractor.keysEqual(null, null)
    }
    
    def 'should compare composite keys'() {
        given:
        def key1 = new CompositeKey(['A', 'B'])
        def key2 = new CompositeKey(['A', 'B'])
        def key3 = new CompositeKey(['A', 'C'])
        
        expect:
        KeyExtractor.keysEqual(key1, key2)
        !KeyExtractor.keysEqual(key1, key3)
    }
    
    def 'should extract keys from various types'() {
        expect:
        KeyExtractor.extractKey([id: 'A'], { it.id }, 'test') == 'A'
        KeyExtractor.extractKey(['A', 'B'], { it[0] }, 'test') == 'A'
        KeyExtractor.extractKey('simple', { it }, 'test') == 'simple'
        KeyExtractor.extractKey(123, { it * 2 }, 'test') == 246
    }
    
    def 'should handle complex nested extraction'() {
        given:
        def item = [
            metadata: [
                subject: 'sub-01',
                session: 'ses-01',
                run: 1
            ],
            files: [
                '/path/to/file1.nii',
                '/path/to/file2.nii'
            ]
        ]
        
        when:
        def key1 = KeyExtractor.extractKey(item, { it.metadata.subject }, 'test')
        def key2 = KeyExtractor.extractKey(item, { [it.metadata.subject, it.metadata.session] }, 'test')
        def key3 = KeyExtractor.extractKey(item, { it.files.size() }, 'test')
        
        then:
        key1 == 'sub-01'
        key2 instanceof CompositeKey
        (key2 as CompositeKey).parts == ['sub-01', 'ses-01']
        key3 == 2
    }
}
