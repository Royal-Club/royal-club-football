package com.bjit.royalclub.royalclubfootball.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TeamServiceException extends RuntimeException {
    private final HttpStatus httpStatus;

    public TeamServiceException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.httpStatus = httpStatus;

    }
}
