package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class DuplicateLikeException extends BaseException {

    public DuplicateLikeException(Long postId, Long userId) {
        super("User %d already liked post %d".formatted(userId, postId), ErrorType.CONFLICT);
    }
}
