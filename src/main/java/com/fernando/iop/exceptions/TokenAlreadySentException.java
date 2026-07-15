package com.fernando.iop.exceptions;

public class TokenAlreadySentException extends RuntimeException {
    public TokenAlreadySentException(String message) {
        super(message);
    }
}
