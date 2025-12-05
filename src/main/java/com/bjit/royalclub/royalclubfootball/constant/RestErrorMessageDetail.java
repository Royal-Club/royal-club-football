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
    public static final String VENUE_IS_ALREADY_EXISTS = "This Venue with this name already exists";
    public static final String PLAYER_ALREADY_EXISTS = "Already a player registered with this email";
    public static final String PLAYER_IS_NOT_PARTICIPANT_YET = "Player is not a confirmed participant in the tournament";
    public static final String PLAYER_IS_NOT_FOUND = "Player is not found";
    public static final String INCORRECT_EMAIL = "Incorrect Email";
    public static final String PASSWORD_MISMATCH_EXCEPTION = "Password mismatch";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "New password cannot be the same as old password";
    public static final String TOURNAMENT_IS_NOT_FOUND = "Tournament is not Found";
    public static final String TEAM_IS_NOT_FOUND = "Team is not Found";
    public static final String NO_UPCOMING_TOURNAMENT = "No upcoming Tournament";
    public static final String PARTICIPANT_NOT_FOUND = "Participant is not Found";
    public static final String ALREADY_PARTICIPANT = "Already given vote for participant";
    public static final String TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE = "This Tournament is already past";
    public static final String COST_TYPE_ALREADY_EXISTS = "Already a cost type is exists";
    public static final String COST_TYPE_IS_NOT_FOUND = "Cost Type is not found";
    public static final String PLAYER_IS_ALREADY_ADDED_ANOTHER_TEAM = "This player is already added another team";
    public static final String PLAYER_IS_NOT_PART_OF_THIS_TEAM = "Player is not part of the specified team";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String UNAUTHORIZED = "You are not authorized to do this action";
    public static final String EMAIL_ALREADY_IN_USE = "Email address is already in use by another player.";

    public static final String AC_VOUCHER_TYPE_NOT_FOUND = "Voucher type is not found!";
    public static final String AC_CHART_NOT_FOUND = "Chart of Account is not found!";
    public static final String AC_VOUCHER_NOT_FOUND = "Voucher is not found!";
    public static final String AC_COLLECTION_NOT_FOUND = "Collection is not found!";
    public static final String AC_BILL_PAYMENT_NOT_FOUND = "Collection is not found!";
    public static final String AC_VOUCHER_DR_CR_AMOUNT_NOT_SAME = "Voucher amount Cr/Dr is not same!";
    public static final String AC_CHART_HAS_VOUCHER = "Cannot delete chart. There are vouchers associated with this chart.";
    public static final String AC_NATURE_NOT_FOUND = "AcNature not found for the given ID.";

    public static final String CLUB_RULE_IS_NOT_FOUND = "Club rule is not Found";

    // Fixture Generation Error Messages
    public static final String FIXTURES_ALREADY_EXIST = "Scheduled fixtures already exist for this tournament";
    public static final String NO_SCHEDULED_FIXTURES = "No scheduled fixtures found to clear";
    public static final String SCHEDULING_CONFLICT = "Fixture scheduling conflict detected";
    public static final String INVALID_TOURNAMENT_TYPE = "Invalid or unsupported tournament type";
    public static final String INSUFFICIENT_TEAMS = "Insufficient teams for this tournament type";
    public static final String INVALID_GROUP_CONFIGURATION = "Invalid group stage configuration";
    public static final String MATCH_IS_NOT_FOUND = "Match is not found";

    // Round Management Error Messages
    public static final String ROUND_IS_NOT_FOUND = "Round is not found";
    public static final String ROUND_NUMBER_ALREADY_EXISTS = "A round with this round number already exists in the tournament";
    public static final String ROUND_HAS_MATCHES = "Cannot delete round with existing matches";
    public static final String ROUND_NOT_COMPLETED = "Round is not completed yet";
    public static final String INVALID_ROUND_SEQUENCE = "Invalid round sequence order";
    public static final String GROUP_IS_NOT_FOUND = "Group is not found";
    public static final String GROUP_NAME_ALREADY_EXISTS = "A group with this name already exists in the round";
    public static final String GROUP_HAS_TEAMS = "Cannot delete group with assigned teams";
    public static final String TEAM_ALREADY_IN_GROUP = "Team is already assigned to this group";
    public static final String TEAM_ALREADY_IN_ROUND = "Team is already assigned to this round";
    public static final String GROUP_MAX_TEAMS_REACHED = "Group has reached maximum team capacity";
    public static final String STANDING_NOT_FOUND = "Standing is not found";
    public static final String ADVANCEMENT_RULE_NOT_FOUND = "Advancement rule is not found";

}
