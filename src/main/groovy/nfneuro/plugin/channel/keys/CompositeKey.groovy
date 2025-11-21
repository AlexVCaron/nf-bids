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

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Wrapper for composite (multi-part) keys to ensure proper equality semantics.
 * 
 * When grouping or joining by multiple fields, this class wraps the list of key parts
 * to provide correct hashCode and equals behavior for use in hash-based collections.
 * 
 * @author Alex Valcourt Caron
 */
@CompileStatic
@EqualsAndHashCode
@ToString(includePackage = false)
class CompositeKey {
    
    /**
     * The list of key parts that make up this composite key
     */
    final List<Object> parts
    
    /**
     * Create a composite key from a list of parts
     * 
     * @param parts The key parts (will be copied to prevent external modification)
     */
    CompositeKey(List<Object> parts) {
        this.parts = parts ? new ArrayList<>(parts) : []
    }
    
    /**
     * Get the number of parts in this composite key
     * 
     * @return The size of the parts list
     */
    int size() {
        return parts.size()
    }
    
    /**
     * Get a specific part of the composite key by index
     * 
     * @param index The index of the part to retrieve
     * @return The key part at the specified index
     */
    Object get(int index) {
        return parts[index]
    }
}
