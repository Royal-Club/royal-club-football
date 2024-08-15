package com.bjit.royalclub.royalclubfootball.exception;

import com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail;
import com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage;
import com.bjit.royalclub.royalclubfootball.util.ResponseBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.BatchUpdateException;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientException;

import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildFailureResponse;

@ControllerAdvice
@Component
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    String errorLog = "ERROR : {}";

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatusCode status,
                                                                         WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.METHOD_NOT_ALLOWED,
                RestErrorMessageDetail.HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers, HttpStatusCode status,
                                                                     WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, RestErrorMessageDetail.HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.NOT_ACCEPTABLE, RestErrorMessageDetail.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, RestErrorMessageDetail.CONVERSION_NOT_SUPPORTED_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST, RestErrorMessageDetail.HTTP_MESSAGE_NOT_READABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, RestErrorMessageDetail.HTTP_MESSAGE_NOT_WRITABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.NOT_FOUND, RestErrorMessageDetail.NO_HANDLER_FOUND_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
                                                                        HttpHeaders headers, HttpStatusCode status,
                                                                        WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletResponse response = servletWebRequest.getResponse();
            if (response != null && response.isCommitted()) {
                log.warn(errorLog, ex.getMessage());
                return buildFailureResponse(HttpStatus.SERVICE_UNAVAILABLE, RestErrorMessageDetail.ASYNC_REQUEST_TIMEOUT_ERROR_MESSAGE);
            }
        }
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, RestErrorMessageDetail.ASYNC_REQUEST_TIMEOUT_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseBuilder.buildFailureResponse(ex.getBindingResult(), RestResponseMessage.VALIDATION_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST, RestErrorMessageDetail.MISSING_PATH_VARIABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST, RestErrorMessageDetail.MISSING_SERVLET_REQUEST_PARAMETER_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST, RestErrorMessageDetail.TYPE_MISMATCH_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST, RestErrorMessageDetail.MISSING_SERVLET_REQUEST_PART_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(errorLog, ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST, RestErrorMessageDetail.SERVLET_REQUEST_BINDING_ERROR_MESSAGE);
    }

    @ExceptionHandler({
            SQLException.class,
            SQLDataException.class,
            SQLIntegrityConstraintViolationException.class,
            SQLSyntaxErrorException.class,
            SQLTimeoutException.class,
            SQLTransactionRollbackException.class,
            SQLFeatureNotSupportedException.class,
            BatchUpdateException.class,
            SQLNonTransientException.class,
            SQLTransientException.class,
            SQLRecoverableException.class,
            SQLClientInfoException.class
    })
    public ResponseEntity<Object> handleSqlExceptions(Exception ex) {
        log.error("SQL-related exception occurred: {}", ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, RestErrorMessageDetail.SQL_ERROR_MESSAGE);
    }
}
