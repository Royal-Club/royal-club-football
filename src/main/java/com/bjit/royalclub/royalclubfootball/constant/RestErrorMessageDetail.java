package com.bjit.royalclub.royalclubfootball.constant;

public class RestErrorMessageDetail {
    private RestErrorMessageDetail() {
    }

    public static final String HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR_MESSAGE = "Sorry, the requested HTTP method is not supported.";
    public static final String HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR_MESSAGE = "Unsupported media type in the request.";
    public static final String HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR_MESSAGE = "None of the requested media types are acceptable.";
    public static final String CONVERSION_NOT_SUPPORTED_ERROR_MESSAGE = "Conversion not supported for the given data type.";
    public static final String HTTP_MESSAGE_NOT_READABLE_ERROR_MESSAGE = "Unable to read the HTTP message.";
    public static final String HTTP_MESSAGE_NOT_WRITABLE_ERROR_MESSAGE = "Unable to write the HTTP message.";
    public static final String NO_HANDLER_FOUND_ERROR_MESSAGE = "No handler found for the request.";
    public static final String ASYNC_REQUEST_TIMEOUT_ERROR_MESSAGE = "Async request timed out.";
    public static final String MISSING_PATH_VARIABLE_ERROR_MESSAGE = "Missing path variable in the request.";
    public static final String MISSING_SERVLET_REQUEST_PARAMETER_ERROR_MESSAGE = "Missing servlet request parameter.";
    public static final String TYPE_MISMATCH_ERROR_MESSAGE = "Type mismatch in request parameters.";
    public static final String MISSING_SERVLET_REQUEST_PART_ERROR_MESSAGE = "Missing servlet request part.";
    public static final String SERVLET_REQUEST_BINDING_ERROR_MESSAGE = "ServletRequest binding error.";
    public static final String METHOD_ARGUMENT_TYPE_MISMATCH_ERROR_MESSAGE = "Method argument type mismatch.";
    public static final String BINDING_ERROR_MESSAGE = "Cannot bind input due to invalid format.";
    public static final String INVALID_FORMAT_ERROR_MESSAGE = "Invalid data format in the request.";
    public static final String MAX_UPLOAD_SIZE_EXCEEDED_ERROR_MESSAGE = "Uploaded file size exceeds the maximum limit.";
    public static final String BAD_CREDENTIALS_ERROR_MESSAGE = "Username or password is invalid.";
    public static final String SQL_ERROR_MESSAGE = "An error occurred while processing the database.";
    public static final String TIMEOUT_ERROR_MESSAGE = "Request processing time exceeds the maximum limit.";
    public static final String ACCESS_DENIED_ERROR_MESSAGE = "You do not have permission to access this resource.";
    public static final String RESOURCE_NOT_FOUND_ERROR_MESSAGE = "The requested resource was not found.";
    public static final String DATA_INTEGRITY_VIOLATION_ERROR_MESSAGE = "Data integrity violation occurred.";
    public static final String CONSTRAINT_VIOLATION_ERROR_MESSAGE = "A constraint was violated in the request data.";
    public static final String UNAUTHORIZED_ERROR_MESSAGE = "Authentication is required to access this resource.";
    public static final String FORBIDDEN_ERROR_MESSAGE = "You are not authorized to access this resource.";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "An internal server error occurred. Please try again later.";
    public static final String SERVICE_UNAVAILABLE_ERROR_MESSAGE = "The service is temporarily unavailable. Please try again later.";

    public static final String VENUE_IS_NOT_FOUND = "Venue is not Found";
    public static final String PLAYER_ALREADY_EXISTS = "Already a player registered with this email";
    public static final String PLAYER_IS_NOT_FOUND = "Player is not found";
    public static final String MATCH_SCHEDULE_IS_NOT_FOUND = "Venue is not Found";

}