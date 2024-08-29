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
import org.springframework.lang.NonNull;
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

    private static final String ERROR_LOG = "ERROR: {}";
    private static final String WARN_LOG = "WARNING: {}";

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @NonNull HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "HTTP method not supported: " + ex.getMethod());
        return buildFailureResponse(HttpStatus.METHOD_NOT_ALLOWED,
                RestErrorMessageDetail.HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            @NonNull HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "HTTP media type not supported: " + ex.getContentType());
        return buildFailureResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                RestErrorMessageDetail.HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            @NonNull HttpMediaTypeNotAcceptableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "HTTP media type not acceptable. Supported types: " + ex.getSupportedMediaTypes());
        return buildFailureResponse(HttpStatus.NOT_ACCEPTABLE,
                RestErrorMessageDetail.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(
            @NonNull ConversionNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error(ERROR_LOG, "Conversion not supported: " + ex.getPropertyName());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                RestErrorMessageDetail.CONVERSION_NOT_SUPPORTED_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "HTTP message not readable: " + ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST,
                RestErrorMessageDetail.HTTP_MESSAGE_NOT_READABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            @NonNull HttpMessageNotWritableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error(ERROR_LOG, "HTTP message not writable: " + ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                RestErrorMessageDetail.HTTP_MESSAGE_NOT_WRITABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            @NonNull NoHandlerFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "No handler found for request: " + ex.getRequestURL());
        return buildFailureResponse(HttpStatus.NOT_FOUND,
                RestErrorMessageDetail.NO_HANDLER_FOUND_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            @NonNull AsyncRequestTimeoutException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletResponse response = servletWebRequest.getResponse();
            if (response != null && response.isCommitted()) {
                log.warn(WARN_LOG, "Async request timeout: " + ex.getMessage());
                return buildFailureResponse(HttpStatus.SERVICE_UNAVAILABLE,
                        RestErrorMessageDetail.ASYNC_REQUEST_TIMEOUT_ERROR_MESSAGE);
            }
        }
        log.error(ERROR_LOG, "Async request timeout occurred: " + ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                RestErrorMessageDetail.ASYNC_REQUEST_TIMEOUT_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "Method argument not valid: " + ex.getBindingResult().toString());
        return ResponseBuilder.buildFailureResponse(ex.getBindingResult(),
                RestResponseMessage.VALIDATION_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            @NonNull MissingPathVariableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "Missing path variable: " + ex.getVariableName());
        return buildFailureResponse(HttpStatus.BAD_REQUEST,
                RestErrorMessageDetail.MISSING_PATH_VARIABLE_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "Missing servlet request parameter: " + ex.getParameterName());
        return buildFailureResponse(HttpStatus.BAD_REQUEST,
                RestErrorMessageDetail.MISSING_SERVLET_REQUEST_PARAMETER_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            @NonNull TypeMismatchException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "Type mismatch: " + ex.getValue());
        return buildFailureResponse(HttpStatus.BAD_REQUEST,
                RestErrorMessageDetail.TYPE_MISMATCH_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            @NonNull MissingServletRequestPartException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "Missing servlet request part: " + ex.getRequestPartName());
        return buildFailureResponse(HttpStatus.BAD_REQUEST,
                RestErrorMessageDetail.MISSING_SERVLET_REQUEST_PART_ERROR_MESSAGE);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            @NonNull ServletRequestBindingException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.warn(WARN_LOG, "Servlet request binding error: " + ex.getMessage());
        return buildFailureResponse(HttpStatus.BAD_REQUEST,
                RestErrorMessageDetail.SERVLET_REQUEST_BINDING_ERROR_MESSAGE);
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
    public ResponseEntity<Object> handleSqlExceptions(@NonNull Exception ex) {
        log.error(ERROR_LOG, "SQL-related exception occurred: " + ex.getMessage());
        return buildFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                RestErrorMessageDetail.SQL_ERROR_MESSAGE);
    }

    @ExceptionHandler(VenueServiceException.class)
    public ResponseEntity<Object> handleVenueServiceException(VenueServiceException ex) {
        log.error(ERROR_LOG, ex.getMessage());
        return buildFailureResponse(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(PlayerServiceException.class)
    public ResponseEntity<Object> handlePlayerServiceException(PlayerServiceException ex) {
        log.error(ERROR_LOG, ex.getMessage());
        return buildFailureResponse(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(TournamentServiceException.class)
    public ResponseEntity<Object> handleMatchScheduleServiceException(TournamentServiceException ex) {
        log.error(ERROR_LOG, ex.getMessage());
        return buildFailureResponse(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(TeamServiceException.class)
    public ResponseEntity<Object> handleTeamServiceServiceException(TeamServiceException ex) {
        log.error(ERROR_LOG, ex.getMessage());
        return buildFailureResponse(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(CostTypeServiceException.class)
    public ResponseEntity<Object> handleCostTypeServiceException(CostTypeServiceException ex) {
        log.error(ERROR_LOG, ex.getMessage());
        return buildFailureResponse(ex.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleJWTException(SecurityException ex) {
        log.error(ERROR_LOG, "JWT exception: " + ex.getMessage());
        return buildFailureResponse(HttpStatus.UNAUTHORIZED, RestErrorMessageDetail.JWT_ERROR_MESSAGE);
    }
}
