package com.app.exceptions;

public class TokenExpiredException
        extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }
}
