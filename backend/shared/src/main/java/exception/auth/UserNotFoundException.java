package exception.auth;

import exception.base.BaseException;
import exception.base.ErrorType;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(String username) {
        super("User not found: " + username, ErrorType.NOT_FOUND);
    }

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id, ErrorType.NOT_FOUND);
    }
}
