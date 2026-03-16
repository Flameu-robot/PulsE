package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class DuplicateBookmarkException extends BaseException {

    public DuplicateBookmarkException(Long postId, Long userId) {
        super("User %d already bookmarked post %d".formatted(userId, postId), ErrorType.CONFLICT);
    }
}