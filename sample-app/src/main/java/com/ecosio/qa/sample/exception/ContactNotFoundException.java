package com.ecosio.qa.sample.exception;

public class ContactNotFoundException extends RuntimeException {

    public ContactNotFoundException(String message) {
        super(message);
    }

    public ContactNotFoundException(Throwable cause) {
        super(cause);
    }

    public ContactNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
