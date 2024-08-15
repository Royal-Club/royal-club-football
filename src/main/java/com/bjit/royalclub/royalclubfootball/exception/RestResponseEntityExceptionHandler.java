package com.bjit.royalclub.royalclubfootball.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Component
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

}
