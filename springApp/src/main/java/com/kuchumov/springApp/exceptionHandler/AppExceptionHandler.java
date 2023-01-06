package com.kuchumov.springApp.exceptionHandler;


import com.kuchumov.springApp.dto.EmptyResponseDTO;
import com.kuchumov.springApp.exceptionHandler.customExceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class AppExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<EmptyResponseDTO> handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errorMessages = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        StringBuilder stringBuilder = new StringBuilder();
        for (String message :
                errorMessages) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(". ").append(message);
            } else {
                stringBuilder.append(message);
            }
        }

        EmptyResponseDTO emptyResponseDTO = new EmptyResponseDTO(HttpStatus.BAD_REQUEST, stringBuilder.toString());
        log.error(errorMessages.toString(), e);
        return new ResponseEntity<>(emptyResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SizeLimitExceededException.class)
    ResponseEntity<EmptyResponseDTO> handleSizeLimitExceededException(SizeLimitExceededException e) {
        EmptyResponseDTO emptyResponseDTO = new EmptyResponseDTO(HttpStatus.BAD_REQUEST, "Request size exception. " +
                "Max request size = 100 MB, Max file size = 15 MB");
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(emptyResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    ResponseEntity<EmptyResponseDTO> handleFileSizeLimitExceededException(FileSizeLimitExceededException e) {
        EmptyResponseDTO emptyResponseDTO = new EmptyResponseDTO(HttpStatus.BAD_REQUEST, "File size exception. " +
                "Max file size = 15 MB");
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(emptyResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    ResponseEntity<EmptyResponseDTO> handleCustomException(CustomException e) {
        return handleCustomExceptionLogic(e);
    }

    private ResponseEntity<EmptyResponseDTO> handleCustomExceptionLogic (CustomException e) {
        ResponseEntity<EmptyResponseDTO> responseEntity;

        if (e.getClass().equals(FileIdNotFoundException.class)
                || e.getClass().equals(ParsingDateException.class)) {

            responseEntity = customHandling(e, HttpStatus.BAD_REQUEST);

        } else if (e.getClass().equals(ZipCreatingException.class)
                || e.getClass().equals(ParsingFormDataException.class)
                || e.getClass().equals(TransliterateException.class)
                || e.getClass().equals(FileLoadingException.class)) {

            responseEntity = customHandling(e, HttpStatus.INTERNAL_SERVER_ERROR);

        } else {
            EmptyResponseDTO emptyResponseDTO = new EmptyResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Неизвестная ошибка");
            responseEntity = new ResponseEntity<>(emptyResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    private ResponseEntity<EmptyResponseDTO> customHandling(CustomException e, HttpStatus httpStatus) {
        EmptyResponseDTO emptyResponseDTO;
        if (e.getMessage() != null) {
            emptyResponseDTO = new EmptyResponseDTO(httpStatus, e.getMessage());
        } else {
            emptyResponseDTO = new EmptyResponseDTO(httpStatus, "Ошибка без описания");
        }

        if (e.getInitError() != null) {
            log.error(e.getInitError().getMessage(), e.getInitError());
        }
        return new ResponseEntity<>(emptyResponseDTO, httpStatus);
    }
}
