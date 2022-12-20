package com.kuchumov.springApp.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = ValidTypeValidator.class)
public @interface ValidType {

    String message() default "com.kuchumov.springApp.annotations.ValidType.incorrectTypeError";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String[] value() default {"application/javascript", "application/zip", "application/gzip", "application/msword", "application/xml", "application/pdf",
                                "audio/mp4", "audio/aac", "audio/mpeg",
                                "image/gif", "image/jpeg", "image/pjpeg", "image/png", "image/svg+xml", "image/tiff", "image/vnd.microsoft.icon",
                                "text/css", "text/csv", "text/html", "text/markdown", "text/javascript", "text/php", "text/xml", "text/plain",
                                "video/mpeg", "video/mp4", "video/ogg", "video/x-ms-wmv", "video/x-msvideo",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel.sheet.macroEnabled.12",
                                "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.mozilla.xul+xml"
    };
}