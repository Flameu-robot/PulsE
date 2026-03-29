package exception.feed;

import exception.base.BaseException;
import exception.base.ErrorType;

import java.util.List;

public class GroupPermissionDeniedException extends BaseException {

    public GroupPermissionDeniedException(List<Long> deniedGroupIds) {
        super("No permission to post in groups: " + deniedGroupIds, ErrorType.AUTHORIZATION);
    }
}
