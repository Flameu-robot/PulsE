package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class FollowNotFoundException extends BaseException {

    public FollowNotFoundException(Long followerId, Long followeeId) {
        super("Follow not found: %d → %d".formatted(followerId, followeeId), ErrorType.NOT_FOUND);
    }
}