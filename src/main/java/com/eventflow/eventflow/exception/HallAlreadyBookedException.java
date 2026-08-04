package com.eventflow.eventflow.exception;

public class HallAlreadyBookedException extends RuntimeException {
    public HallAlreadyBookedException(String message) {
        super(message);
    }
}
