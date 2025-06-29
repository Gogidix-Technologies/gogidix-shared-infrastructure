package com.exalt.ecosystem.shared.geolocation.exception;

/**
 * Exception thrown when there's an error in geo-location operations.
 */
public class GeoLocationException extends Exception {
    private static final long serialVersionUID = 1L;

    public GeoLocationException(String message) {
        super(message);
    }

    public GeoLocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
