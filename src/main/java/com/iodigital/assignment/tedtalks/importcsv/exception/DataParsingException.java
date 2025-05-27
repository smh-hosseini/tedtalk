package com.iodigital.assignment.tedtalks.importcsv.exception;

public class DataParsingException extends RuntimeException {

    public DataParsingException(String message) {
        super(message);
    }

    public DataParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
