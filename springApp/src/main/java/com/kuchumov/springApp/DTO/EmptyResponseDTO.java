package com.kuchumov.springApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class EmptyResponseDTO {
    private HttpStatus status;
    private String message;
}
