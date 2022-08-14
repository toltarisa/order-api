package com.isatoltar.order.exception.payload;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class CustomValidationExceptionResponse extends ExceptionResponse {

    List<ValidationError> errors;

    public CustomValidationExceptionResponse(Integer status, String message, String path) {
        super(status, message, path);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ValidationError {
        String field;
        String message;
    }
}
