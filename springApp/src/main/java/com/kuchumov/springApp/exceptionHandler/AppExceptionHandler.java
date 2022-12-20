package com.kuchumov.springApp.exceptionHandler;


import com.kuchumov.springApp.exceptionHandler.customExceptions.CustomException;
import com.kuchumov.springApp.exceptionHandler.customExceptions.FileIdNotFoundException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class AppExceptionHandler {

    private final AppExceptionHandlerService appExceptionHandlerService;

    @Autowired
    public AppExceptionHandler(AppExceptionHandlerService appExceptionHandlerService) {
        this.appExceptionHandlerService = appExceptionHandlerService;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return appExceptionHandlerService.handleConstraintViolationException(e);
    }

    @ExceptionHandler(SizeLimitExceededException.class)
    ResponseEntity<?> handleSizeLimitExceededException(SizeLimitExceededException e) {
        return appExceptionHandlerService.handleSizeLimitExceededException(e);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    ResponseEntity<?> handleFileSizeLimitExceededException(FileSizeLimitExceededException e) {
        return appExceptionHandlerService.handleFileSizeLimitExceededException(e);
    }

    @ExceptionHandler(CustomException.class)
    ResponseEntity<?> handleCustomException(CustomException e) {
        return appExceptionHandlerService.handleCustomException(e);
    }
}
