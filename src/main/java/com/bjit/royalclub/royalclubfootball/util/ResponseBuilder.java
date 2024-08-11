package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.model.ErrorResponse;
import com.bjit.royalclub.royalclubfootball.model.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

public final class ResponseBuilder {
    private ResponseBuilder() {
    }

    private static List<ErrorResponse> getCustomError(BindingResult result) {
        List<ErrorResponse> errorResponses = new ArrayList<>();
        result.getFieldErrors().forEach(fieldError -> {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
            errorResponses.add(errorResponse);
        });
        return errorResponses;
    }

    public static ResponseEntity<Object> getFailureResponse(BindingResult result, String message) {
        Response response = Response.builder()
                .message(message)
                .errors(getCustomError(result))
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timeStamp(System.currentTimeMillis())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<Object> getFailureResponse(HttpStatus status, String message) {
        Response response = Response.builder()
                .message(message)
                .status(status.getReasonPhrase())
                .statusCode(status.value())
                .timeStamp(System.currentTimeMillis())
                .build();
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Object> getSuccessResponse(HttpStatus status, String message, Object content) {
        Response response = Response.builder()
                .message(message)
                .status(status.getReasonPhrase())
                .statusCode(status.value())
                .content(content)
                .timeStamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Object> getSuccessResponse(HttpStatus status, String message) {
        Response response = Response.builder()
                .message(message)
                .status(status.getReasonPhrase())
                .statusCode(status.value())
                .timeStamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Object> getSuccessResponse(HttpStatus status, String message, Object content,
                                                            int numberOfElement) {
        Response response = Response.builder()
                .message(message)
                .status(status.getReasonPhrase())
                .statusCode(status.value())
                .content(content)
                .timeStamp(System.currentTimeMillis())
                .numberOfElement(numberOfElement)
                .build();
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Object> getSuccessResponse(HttpStatus status, String message, Object content,
                                                            int numberOfElement, Long rowCount) {
        Response response = Response.builder()
                .message(message)
                .status(status.getReasonPhrase())
                .statusCode(status.value())
                .content(content)
                .timeStamp(System.currentTimeMillis())
                .numberOfElement(numberOfElement)
                .rowCount(rowCount)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
