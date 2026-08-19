package com.bjit.royalclub.royalclubfootball.exception;

/**
 * A team chat storage sweep could not be completed.
 *
 * <p>Deliberately not one of the {@code HttpStatus}-carrying service exceptions: this is raised by a
 * background purge, where there is no caller to answer and nothing useful to say in a response body.
 * Its only job is to tell the purge that the room's files are still out there, so the room must be
 * left in a state the next sweep will pick up again.
 */
public class TeamChatStorageException extends RuntimeException {

    public TeamChatStorageException(String message) {
        super(message);
    }

    public TeamChatStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
