package com.guilhermesemog.unimove.exception;

public class BookingAlreadyExists extends RuntimeException {
    public BookingAlreadyExists(String message) {
        super(message);
    }
}
