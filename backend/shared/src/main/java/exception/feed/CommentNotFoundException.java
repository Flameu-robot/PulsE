package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class CommentNotFoundException extends BaseException {

    public CommentNotFoundException(Long id) {
        super("Comment not found with id: " + id, ErrorType.NOT_FOUND);
    }
}
