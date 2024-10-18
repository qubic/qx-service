package org.qubic.qx.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;

/**
 * Class for handling/intercepting exceptions that occur when calling controller methods so that we return better
 * error information to the caller.
 */
@ControllerAdvice
public class CustomErrorHandler {

    /**
     * We need to hand constraint violation exceptions because they are not spring exceptions and are therefore
     * not converted to the correct status code automatically.
     *
     * @param exception The constraint violation exception.
     * @param webRequest The request (we need the prepared response of it).
     * @throws IOException In case something goes wrong when sending the error.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException exception,
                                                   ServletWebRequest webRequest) throws IOException {
        if (webRequest.getResponse() != null) {
            webRequest.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
        }
    }

}