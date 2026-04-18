package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class PostAccessDeniedException extends BaseException {

    public PostAccessDeniedException(Long postId) {
        super("Access denied to post: " + postId, ErrorType.AUTHORIZATION);
    }

    public PostAccessDeniedException(String message) {
        super(message, ErrorType.AUTHORIZATION);
    }
}
