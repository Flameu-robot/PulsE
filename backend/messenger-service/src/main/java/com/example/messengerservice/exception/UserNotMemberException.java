package com.example.messengerservice.exception;

import exception.base.BaseException;
import exception.base.ErrorType;

public class UserNotMemberException extends BaseException {
    public UserNotMemberException(Long memberId, Long groupId) {
        super("User with id " + memberId + " is not a member of group " + groupId, ErrorType.AUTHORIZATION);
    }
}
