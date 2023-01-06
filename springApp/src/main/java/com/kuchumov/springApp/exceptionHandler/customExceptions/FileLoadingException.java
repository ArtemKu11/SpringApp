package com.kuchumov.springApp.exceptionHandler.customExceptions;

public class FileLoadingException extends CustomException {
    public FileLoadingException(String message, Exception initError) {
        super(message, initError);
    }
}
