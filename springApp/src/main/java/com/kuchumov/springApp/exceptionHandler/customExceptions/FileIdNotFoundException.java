package com.kuchumov.springApp.exceptionHandler.customExceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileIdNotFoundException extends CustomException {

    public FileIdNotFoundException(String message, Exception initError) {
        super(message, initError);
    }

    public FileIdNotFoundException(String message) {
        super(message);
    }
}
