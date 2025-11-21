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

package nfneuro.plugin.channel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nfneuro.plugin.channel.keys.CompositeKey

/**
 * Utility class for extracting and comparing keys from channel items using closures.
 * 
 * This class provides centralized logic for closure-based key extraction with proper
 * error handling, validation, and support for composite (multi-part) keys.
 * 
 * @author Alex Valcourt Caron
 */
@Slf4j
@CompileStatic
class KeyExtractor {
    
    /**
     * Extract a key from an item using a closure, with error handling.
     * 
     * This method safely invokes the keyExtractor closure on the item, handling
     * null returns and exceptions appropriately. List keys are automatically
     * wrapped in CompositeKey for proper equality semantics.
     * 
     * @param item The channel item to extract a key from
     * @param keyExtractor The closure that extracts the key
     * @param operatorName The name of the calling operator (for error messages)
     * @return The extracted key, or null if extraction failed or returned null
     * @throws IllegalStateException if the keyExtractor throws an exception
     */
    static Object extractKey(Object item, Closure keyExtractor, String operatorName) {
        if (keyExtractor == null) {
            throw new IllegalArgumentException("${operatorName}: keyExtractor cannot be null")
        }
        
        try {
            def key = keyExtractor.call(item)
            
            if (key == null) {
                log.trace("${operatorName}: item produced null key, skipping: ${item}")
                return null
            }
            
            // Normalize key - wrap lists in CompositeKey for proper equality
            if (key instanceof List) {
                return new CompositeKey(key as List<Object>)
            }
            
            return key
            
        } catch (Exception e) {
            throw new IllegalStateException(
                "${operatorName}: keyExtractor failed for item [${item}]: ${e.message}",
                e
            )
        }
    }
    
    /**
     * Validate that a keyExtractor closure has the correct arity (parameter count).
     * 
     * A valid keyExtractor must accept at least one parameter (the channel item).
     * Closures with more than 2 parameters will generate a warning as they may
     * indicate incorrect usage.
     * 
     * @param keyExtractor The closure to validate
     * @param operatorName The name of the calling operator (for error messages)
     * @throws IllegalArgumentException if the closure is invalid
     */
    static void validateKeyExtractor(Closure keyExtractor, String operatorName) {
        if (keyExtractor == null) {
            throw new IllegalArgumentException(
                "${operatorName}: keyExtractor closure is required"
            )
        }
        
        int params = keyExtractor.getMaximumNumberOfParameters()
        if (params == 0) {
            throw new IllegalArgumentException(
                "${operatorName}: keyExtractor must accept at least one parameter\n" +
                "  Expected: { item -> item.key }\n" +
                "  Found: { -> ... }"
            )
        }
        
        if (params > 2) {
            log.warn(
                "${operatorName}: keyExtractor has ${params} parameters but only first will be used. " +
                "Did you mean to use a different operator?"
            )
        }
    }
    
    /**
     * Compare two keys for equality.
     * 
     * @param key1 The first key
     * @param key2 The second key
     * @return true if the keys are equal, false otherwise
     */
    static boolean keysEqual(Object key1, Object key2) {
        if (key1 == null || key2 == null) {
            return false
        }
        return key1 == key2
    }
}
