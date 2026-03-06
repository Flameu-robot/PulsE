package exception.auth;


import exception.base.BaseException;
import exception.base.ErrorType;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super("Invalid username or password", ErrorType.AUTHENTICATION);
    }

    public InvalidCredentialsException(String message) {
        super(message, ErrorType.AUTHENTICATION);
    }
}
