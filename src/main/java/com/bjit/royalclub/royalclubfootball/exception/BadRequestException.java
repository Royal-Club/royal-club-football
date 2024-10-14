package com.bjit.royalclub.royalclubfootball.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends RuntimeException {
    private final HttpStatus httpStatus;

    public BadRequestException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.httpStatus = httpStatus;

    }
}
