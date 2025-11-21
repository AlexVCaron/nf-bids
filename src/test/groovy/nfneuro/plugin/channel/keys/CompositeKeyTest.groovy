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

package nfneuro.plugin.channel.keys

import spock.lang.Specification

/**
 * Unit tests for CompositeKey class
 */
class CompositeKeyTest extends Specification {

    def 'should create composite key from list'() {
        when:
        def key = new CompositeKey(['a', 'b', 'c'])
        
        then:
        key.size() == 3
        key.get(0) == 'a'
        key.get(1) == 'b'
        key.get(2) == 'c'
    }
    
    def 'should handle empty list'() {
        when:
        def key = new CompositeKey([])
        
        then:
        key.size() == 0
        key.parts == []
    }
    
    def 'should handle null list'() {
        when:
        def key = new CompositeKey(null)
        
        then:
        key.size() == 0
        key.parts == []
    }
    
    def 'should test equality for identical lists'() {
        given:
        def key1 = new CompositeKey(['a', 'b', 'c'])
        def key2 = new CompositeKey(['a', 'b', 'c'])
        
        expect:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }
    
    def 'should test inequality for different lists'() {
        given:
        def key1 = new CompositeKey(['a', 'b', 'c'])
        def key2 = new CompositeKey(['a', 'b', 'd'])
        
        expect:
        key1 != key2
    }
    
    def 'should test inequality for different sizes'() {
        given:
        def key1 = new CompositeKey(['a', 'b'])
        def key2 = new CompositeKey(['a', 'b', 'c'])
        
        expect:
        key1 != key2
    }
    
    def 'should test equality for different list instances with same content'() {
        given:
        def list1 = ['a', 'b', 'c']
        def list2 = ['a', 'b', 'c']
        def key1 = new CompositeKey(list1)
        def key2 = new CompositeKey(list2)
        
        expect:
        key1 == key2
        key1.hashCode() == key2.hashCode()
    }
    
    def 'should maintain hashCode consistency'() {
        given:
        def key = new CompositeKey(['a', 'b', 'c'])
        def hash1 = key.hashCode()
        def hash2 = key.hashCode()
        
        expect:
        hash1 == hash2
    }
    
    def 'should copy list to prevent external modification'() {
        given:
        def list = ['a', 'b', 'c']
        def key = new CompositeKey(list)
        
        when:
        list[0] = 'x'
        
        then:
        key.get(0) == 'a'  // Original value preserved
    }
    
    def 'should work with nested structures'() {
        given:
        def key1 = new CompositeKey([[a: 1], [b: 2]])
        def key2 = new CompositeKey([[a: 1], [b: 2]])
        
        expect:
        key1 == key2
    }
    
    def 'should work with mixed types'() {
        given:
        def key1 = new CompositeKey(['string', 123, true])
        def key2 = new CompositeKey(['string', 123, true])
        
        expect:
        key1 == key2
        key1.size() == 3
    }
    
    def 'should have proper toString representation'() {
        when:
        def key = new CompositeKey(['a', 'b', 'c'])
        def str = key.toString()
        
        then:
        str.contains('CompositeKey')
        str.contains('[a, b, c]')
    }
    
    def 'should work in hash-based collections'() {
        given:
        def key1 = new CompositeKey(['a', 'b'])
        def key2 = new CompositeKey(['a', 'b'])
        def key3 = new CompositeKey(['c', 'd'])
        def map = [:]
        
        when:
        map[key1] = 'value1'
        map[key3] = 'value3'
        
        then:
        map[key2] == 'value1'  // key2 equals key1, so retrieves same value
        map[key3] == 'value3'
        map.size() == 2
    }
}
