package com.fernando.iop.exceptions;

public class UserAlreadyConfirmedException extends RuntimeException {
    public UserAlreadyConfirmedException(String message) {
        super(message);
    }
}
