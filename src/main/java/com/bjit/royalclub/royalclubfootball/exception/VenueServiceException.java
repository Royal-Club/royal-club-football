package com.bjit.royalclub.royalclubfootball.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VenueServiceException extends RuntimeException {
    private final HttpStatus httpStatus;

    public VenueServiceException(String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.httpStatus = httpStatus;
    }
}
