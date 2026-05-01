package com.bjit.royalclub.royalclubfootball.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus;

    public ResourceNotFoundException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.httpStatus = httpStatus;
    }

    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
