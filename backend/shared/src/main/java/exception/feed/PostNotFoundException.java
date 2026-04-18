package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class PostNotFoundException extends BaseException {

    public PostNotFoundException(Long id) {
        super("Post not found with id: " + id, ErrorType.NOT_FOUND);
    }
}
