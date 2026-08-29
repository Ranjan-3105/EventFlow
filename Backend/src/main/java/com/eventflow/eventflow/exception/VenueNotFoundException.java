package com.eventflow.eventflow.exception;

public class VenueNotFoundException extends RuntimeException{
    public VenueNotFoundException(String message) {
        super(message);
    }
}
