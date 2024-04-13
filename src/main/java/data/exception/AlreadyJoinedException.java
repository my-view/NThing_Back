package data.exception;

import data.constants.ErrorCode;
import lombok.Getter;

@Getter
public class AlreadyJoinedException extends RuntimeException {
    private final ErrorCode errorCode;
    public AlreadyJoinedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
