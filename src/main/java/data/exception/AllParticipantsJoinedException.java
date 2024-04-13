package data.exception;

import data.constants.ErrorCode;
import lombok.Getter;

@Getter
public class AllParticipantsJoinedException extends RuntimeException {
    private final ErrorCode errorCode;
    public AllParticipantsJoinedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
