package data.exception;

import data.constants.ErrorCode;
import lombok.Getter;

@Getter
public class RefreshTokenExpiredException extends RuntimeException {
    private final ErrorCode errorCode;
    public RefreshTokenExpiredException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
