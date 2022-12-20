package com.kuchumov.springApp.exceptionHandler.customExceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ZipCreatingException extends CustomException {

    public ZipCreatingException(String message, Exception initError) {
        super(message, initError);
    }
}
