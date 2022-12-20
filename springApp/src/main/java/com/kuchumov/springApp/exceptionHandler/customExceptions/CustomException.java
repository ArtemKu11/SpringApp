package com.kuchumov.springApp.exceptionHandler.customExceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public abstract class CustomException extends RuntimeException {
    private Exception initError;
    public CustomException(String message, Exception initError) {
        super(message);
        this.initError = initError;
    }

    public CustomException(String message) {
        super(message);
    }
}
