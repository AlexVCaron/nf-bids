package nfneuro.plugin.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Centralized logging for BIDS operations
 * 
 * Provides consistent logging patterns with colored output and progress tracking
 */
@Slf4j
@CompileStatic
class BidsLogger {
    
    private static final String DEFAULT_CONTEXT = "bids2nf"
    
    /**
     * Log progress message with context
     * 
     * @param context Processing context (e.g., "bids2nf")
     * @param message Message to log
     * 
     * @reference logProgress function: 
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L18-L20
     */
    static void logProgress(String context, String message) {
        log.info("[${context}] ${message}")
    }
    
    /**
     * Log progress message with default context
     * 
     * @param message Message to log
     */
    static void logProgress(String message) {
        logProgress(DEFAULT_CONTEXT, message)
    }
    
    /**
     * Log debug message with context
     * 
     * @param context Processing context
     * @param message Debug message
     * 
     * @reference logDebug function:
     *            https://github.com/AlexVCaron/bids2nf/blob/main/modules/utils/error_handling.nf#L22-L26
     */
    static void logDebug(String context, String message) {
        if (log.isDebugEnabled()) {
            log.debug("[${context}] ${message}")
        }
    }
    
    /**
     * Log debug message with default context
     * 
     * @param message Debug message
     */
    static void logDebug(String message) {
        logDebug(DEFAULT_CONTEXT, message)
    }
    
    /**
     * Log warning message with context
     * 
     * @param context Processing context
     * @param message Warning message
     */
    static void logWarning(String context, String message) {
        log.warn("[${context}] ⚠︎ ${message}")
    }
    
    /**
     * Log warning message with default context
     * 
     * @param message Warning message
     */
    static void logWarning(String message) {
        logWarning(DEFAULT_CONTEXT, message)
    }
    
    /**
     * Log error message with context
     * 
     * @param context Processing context
     * @param message Error message
     */
    static void logError(String context, String message) {
        log.error("[${context}] ⛔️ ${message}")
    }
    
    /**
     * Log error message with default context
     * 
     * @param message Error message
     */
    static void logError(String message) {
        logError(DEFAULT_CONTEXT, message)
    }
    
    /**
     * Log success message with context
     * 
     * @param context Processing context
     * @param message Success message
     */
    static void logSuccess(String context, String message) {
        log.info("[${context}] ✅ ${message}")
    }
    
    /**
     * Log success message with default context
     * 
     * @param message Success message
     */
    static void logSuccess(String message) {
        logSuccess(DEFAULT_CONTEXT, message)
    }
    
    /**
     * Log configuration summary
     * 
     * @param context Processing context
     * @param config Configuration map
     */
    static void logConfig(String context, Map<String, Object> config) {
        logProgress(context, "Configuration:")
        config.each { key, value ->
            logProgress(context, "  ${key}: ${value}")
        }
    }
    
    /**
     * Log statistics
     * 
     * @param context Processing context
     * @param stats Statistics map
     */
    static void logStats(String context, Map<String, Object> stats) {
        logProgress(context, "Statistics:")
        stats.each { key, value ->
            logProgress(context, "  ${key}: ${value}")
        }
    }
    
    /**
     * Log timing information
     * 
     * @param context Processing context
     * @param operation Operation name
     * @param duration Duration in milliseconds
     */
    static void logTiming(String context, String operation, long duration) {
        def seconds = duration / 1000.0
        logDebug(context, "⏱ ${operation} took ${seconds}s")
    }
    
    /**
     * Execute closure with timing
     * 
     * @param context Processing context
     * @param operation Operation name
     * @param closure Code to execute
     * @return Result of closure
     */
    static <T> T withTiming(String context, String operation, Closure<T> closure) {
        def start = System.currentTimeMillis()
        try {
            return closure.call()
        } finally {
            def duration = System.currentTimeMillis() - start
            logTiming(context, operation, duration)
        }
    }
    
    // Convenience methods for compatibility
    
    /**
     * Convenience method for debug logging (alias for logDebug)
     */
    static void debug(Object message) {
        logDebug(message.toString())
    }
    
    /**
     * Convenience method for info logging (alias for logProgress)
     */
    static void info(Object message) {
        logProgress(message.toString())
    }
    
    /**
     * Convenience method for warn logging (alias for logWarning)
     */
    static void warn(Object message) {
        logWarning(message.toString())
    }
    
    /**
     * Convenience method for trace logging (same as debug)
     */
    static void trace(Object message) {
        logDebug(message.toString())
    }
}
