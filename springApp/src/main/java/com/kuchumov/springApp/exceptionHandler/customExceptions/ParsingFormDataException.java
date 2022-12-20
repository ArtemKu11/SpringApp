package com.kuchumov.springApp.exceptionHandler.customExceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParsingFormDataException extends CustomException {

    public ParsingFormDataException(String message, Exception initError) {
        super(message, initError);
    }
}
