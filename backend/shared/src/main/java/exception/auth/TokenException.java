package exception.auth;

import exception.base.BaseException;
import exception.base.ErrorType;

public class TokenException extends BaseException {

    public TokenException(String message) {
        super(message, ErrorType.AUTHENTICATION);
    }

    public TokenException(String message, Throwable cause) {
        super(message, ErrorType.AUTHENTICATION, cause);
    }
}
