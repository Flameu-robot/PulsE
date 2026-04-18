package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

public class DuplicateFollowException extends BaseException {

    public DuplicateFollowException(Long followerId, Long followeeId) {
        super("User %d already follows user %d".formatted(followerId, followeeId), ErrorType.CONFLICT);
    }

    public static DuplicateFollowException selfFollow() {
        return new DuplicateFollowException("Cannot follow yourself");
    }

    private DuplicateFollowException(String message) {
        super(message, ErrorType.CONFLICT);
    }
}