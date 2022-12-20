package com.kuchumov.springApp.exceptionHandler.customExceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParsingDateException extends CustomException {

    public ParsingDateException(String message, Exception initError) {
        super(message, initError);
    }
}
