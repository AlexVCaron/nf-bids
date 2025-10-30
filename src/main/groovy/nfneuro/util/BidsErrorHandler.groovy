package nfneuro.plugin.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Error handling utilities for BIDS processing
 * 
 * Provides context-aware error handling with detailed messages
 * 
 * @reference Error handling: 
 *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf
 */
@Slf4j
@CompileStatic
class BidsErrorHandler {
    
    /**
     * Execute code block with error context
     * 
     * Wraps execution with context information for better error messages
     * 
     * @param context Error context identifier
     * @param closure Code to execute
     * @return Result of closure execution
     * 
     * @reference tryWithContext function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L35-L48
     */
    static <T> T tryWithContext(String context, Closure<T> closure) {
        try {
            return closure.call()
        } catch (Exception e) {
            log.error("[${context}] Error occurred: ${e.message}")
            if (log.isDebugEnabled()) {
                log.debug("[${context}] Stack trace:", e)
            }
            throw new RuntimeException(
                "Error in ${context}: ${e.message}",
                e
            )
        }
    }
    
    /**
     * Execute code block safely with default value on failure
     * 
     * @param context Error context identifier
     * @param closure Code to execute
     * @param defaultValue Value to return on failure
     * @return Result of closure or default value
     * 
     * @reference safeExecute function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L42-L48
     */
    static <T> T safeExecute(String context, Closure<T> closure, T defaultValue = null) {
        try {
            return closure.call()
        } catch (Exception e) {
            log.warn("[${context}] ⚠︎ Operation failed: ${e.message}")
            if (log.isDebugEnabled()) {
                log.debug("[${context}] Stack trace:", e)
            }
            return defaultValue
        }
    }
    
    /**
     * Validate condition with context
     * 
     * @param condition Condition to validate
     * @param context Error context
     * @param message Error message if condition fails
     * 
     * @reference validateAndThrow function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L28-L32
     */
    static void validateWithContext(boolean condition, String context, String message) {
        if (!condition) {
            log.error("[${context}] ${message}")
            throw new IllegalStateException("[${context}] ${message}")
        }
    }
    
    /**
     * Handle error with context
     * 
     * @param context Error context
     * @param e Exception that occurred
     * 
     * @reference handleError function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L1-L7
     */
    static void handleError(String context, Exception e) {
        log.error("[${context}] Error occurred: ${e.message}")
        if (log.isDebugEnabled()) {
            log.debug("[${context}] Stack trace:", e)
        }
        throw new RuntimeException("[${context}] ${e.message}", e)
    }
    
    /**
     * Handle error with custom message
     * 
     * @param context Error context
     * @param message Error message
     * 
     * @reference handleErrorWithMessage function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L9-L12
     */
    static void handleErrorWithMessage(String context, String message) {
        log.error("[${context}] ${message}")
        throw new RuntimeException("[${context}] ${message}")
    }
    
    /**
     * Handle warning with context
     * 
     * @param context Warning context
     * @param message Warning message
     * 
     * @reference handleWarning function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L14-L16
     */
    static void handleWarning(String context, String message) {
        log.warn("[${context}] ⚠︎ ${message}")
    }
    
    /**
     * Create detailed error message with suggestions
     * 
     * @param error Main error message
     * @param suggestions List of suggestions to resolve
     * @return Formatted error message
     */
    static String createDetailedError(String error, List<String> suggestions) {
        def message = new StringBuilder()
        message.append("⛔️ ERROR\n")
        message.append("${error}\n\n")
        message.append("Possible solutions:\n")
        suggestions.eachWithIndex { suggestion, index ->
            message.append("  ${index + 1}. ${suggestion}\n")
        }
        return message.toString()
    }
    
    /**
     * Custom exception for BIDS-specific errors
     */
    static class BidsProcessingException extends RuntimeException {
        BidsProcessingException(String message) {
            super(message)
        }
        
        BidsProcessingException(String message, Throwable cause) {
            super(message, cause)
        }
    }
    
    /**
     * Custom exception for BIDS validation errors
     */
    static class BidsValidationException extends RuntimeException {
        BidsValidationException(String message) {
            super(message)
        }
        
        BidsValidationException(String message, Throwable cause) {
            super(message, cause)
        }
    }
    
    /**
     * Custom exception for BIDS configuration errors
     */
    static class BidsConfigurationException extends RuntimeException {
        BidsConfigurationException(String message) {
            super(message)
        }
        
        BidsConfigurationException(String message, Throwable cause) {
            super(message, cause)
        }
    }
}
