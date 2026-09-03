package com.johnxenakis.converter.storage.exception;

public class DuplicateFileException extends RuntimeException {

    private final String objectName;

    public DuplicateFileException(String message) {
        super(message);
        this.objectName = null;
    }

    public DuplicateFileException(String message, String objectName) {
        super(message);
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }
}
