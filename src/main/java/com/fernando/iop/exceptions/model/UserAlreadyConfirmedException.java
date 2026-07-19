package com.fernando.iop.exceptions.model;

public class UserAlreadyConfirmedException extends RuntimeException {
    public UserAlreadyConfirmedException(String message) {
        super(message);
    }
}
