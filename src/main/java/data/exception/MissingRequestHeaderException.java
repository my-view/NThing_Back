package data.exception;

import data.constants.ErrorCode;
import lombok.Getter;

@Getter
public class MissingRequestHeaderException extends RuntimeException {
    private final ErrorCode errorCode;
    public MissingRequestHeaderException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}