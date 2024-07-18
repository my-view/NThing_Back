package data.exception;

import data.constants.ErrorCode;
import lombok.Getter;

@Getter
public class NotJoinedException extends RuntimeException {
    private final ErrorCode errorCode;
    public NotJoinedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
