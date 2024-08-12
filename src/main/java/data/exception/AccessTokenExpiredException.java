package data.exception;

import data.constants.ErrorCode;
import lombok.Getter;

@Getter
public class AccessTokenExpiredException extends RuntimeException {
    private final ErrorCode errorCode;
    public AccessTokenExpiredException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
