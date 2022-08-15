package com.isatoltar.order.exception;

import com.isatoltar.order.exception.payload.CustomValidationExceptionResponse;
import com.isatoltar.order.exception.payload.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomValidationExceptionResponse> processValidationError(MethodArgumentNotValidException ex,
                                                                                    HttpServletRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<CustomValidationExceptionResponse.ValidationError> customFieldErrors = new ArrayList<>();

        fieldErrors.forEach(fieldError -> {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            customFieldErrors.add(new CustomValidationExceptionResponse.ValidationError(field, message));
        });

        CustomValidationExceptionResponse customValidationErrorResponse = new CustomValidationExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Request Contains inappropriate parameters",
                request.getRequestURI()
        );
        customValidationErrorResponse.setErrors(customFieldErrors);

        return new ResponseEntity<>(customValidationErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> processResourceNotFoundException(ResourceNotFoundException ex,
                                                                              HttpServletRequest request) {

        HttpStatus resourceFoundStatus = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(
                new ExceptionResponse(resourceFoundStatus.value(), ex.getMessage(), request.getRequestURI()),
                resourceFoundStatus
        );
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> processResourceAlreadyExistsException(ResourceAlreadyExistsException ex,
                                                                                   HttpServletRequest request) {

        return new ResponseEntity<>(
                new ExceptionResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> processBadRequestException(BadRequestException ex,
                                                                        HttpServletRequest request) {

        HttpStatus badRequestStatus = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(
                new ExceptionResponse(badRequestStatus.value(), ex.getMessage(), request.getRequestURI()),
                badRequestStatus
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionResponse> processConflictException(ConflictException ex,
                                                                      HttpServletRequest request) {

        return new ResponseEntity<>(
                new ExceptionResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                         HttpServletRequest request) {


        return new ResponseEntity<>(
                new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {

        List<CustomValidationExceptionResponse.ValidationError> customFieldErrors = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            customFieldErrors.add(
                    new CustomValidationExceptionResponse.ValidationError(
                            violation.getPropertyPath().toString(),
                            violation.getMessage()
                    )
            );
        }

        CustomValidationExceptionResponse customValidationErrorResponse = new CustomValidationExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Request Contains inappropriate parameters",
                request.getRequestURI()
        );
        customValidationErrorResponse.setErrors(customFieldErrors);

        return new ResponseEntity<>(customValidationErrorResponse, HttpStatus.BAD_REQUEST);
    }
}