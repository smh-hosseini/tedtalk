package com.iodigital.assignment.tedtalks.importcsv.exception;

public class TedTalkImportException extends RuntimeException {

    public TedTalkImportException(String message) {
        super(message);
    }

    public TedTalkImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
