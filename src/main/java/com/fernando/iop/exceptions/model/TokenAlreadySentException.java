package com.fernando.iop.exceptions.model;

public class TokenAlreadySentException extends RuntimeException {
    public TokenAlreadySentException(String message) {
        super(message);
    }
}
