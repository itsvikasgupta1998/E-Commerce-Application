package com.app.exceptions;

public class FileStorageException
        extends RuntimeException {

    public FileStorageException(
            String message
    ) {
        super(message);
    }
}