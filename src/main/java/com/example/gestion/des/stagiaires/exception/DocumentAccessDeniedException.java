package com.example.gestion.des.stagiaires.exception;

public class DocumentAccessDeniedException extends RuntimeException {
    public DocumentAccessDeniedException(String message) {
        super(message);
    }

    public DocumentAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
