package com.kuchumov.springApp.exceptionHandler.customExceptions;

public class TransliterateException extends CustomException {

    public TransliterateException(String message, Exception initError) {
        super(message, initError);
    }
}
