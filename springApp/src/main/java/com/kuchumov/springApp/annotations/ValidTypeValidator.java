package com.kuchumov.springApp.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;

public class ValidTypeValidator implements ConstraintValidator<ValidType, String> {

    private HashSet<String> types;
    private String message;

    @Override
    public void initialize(ValidType constraintAnnotation) {
        this.types = new HashSet<>(Arrays.asList(constraintAnnotation.value()));
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
        if ( object == null ) {
            return true;
        }

        boolean isValid;

        isValid = this.types.contains(object);

        if ( !isValid ) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate(
                            this.message + " Incorrect Type: " + object
                    )
                    .addConstraintViolation();
        }

        return isValid;
    }
}

