package exception.auth;


import exception.base.BaseException;
import exception.base.ErrorType;

public class UserAlreadyExistsException extends BaseException {

    public UserAlreadyExistsException(String message) {
        super(message, ErrorType.CONFLICT);
    }

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("%s already taken: %s", field, value), ErrorType.CONFLICT);
    }
}
