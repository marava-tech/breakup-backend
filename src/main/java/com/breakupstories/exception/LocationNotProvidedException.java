package com.breakupstories.exception;

/**
 * Exception thrown when location coordinates are not provided
 */
public class LocationNotProvidedException extends RuntimeException {
    
    public LocationNotProvidedException(String message) {
        super(message);
    }
    
    public LocationNotProvidedException(String message, Throwable cause) {
        super(message, cause);
    }
} 