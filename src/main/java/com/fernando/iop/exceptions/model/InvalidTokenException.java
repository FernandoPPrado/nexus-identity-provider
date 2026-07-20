package com.fernando.iop.exceptions.model;

public class InvalidTokenException extends RuntimeException{

    public InvalidTokenException(String message) {
        super(message);
    }
}
