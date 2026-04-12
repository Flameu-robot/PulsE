package com.example.messengerservice.exception;

import exception.base.BaseException;
import exception.base.ErrorType;

public class ChannelNotFoundException extends BaseException {
    public ChannelNotFoundException(Long channelId, Long groupId) {
        super("Channel with id " + channelId + " not found in group " + groupId, ErrorType.NOT_FOUND);
    }

    public ChannelNotFoundException(Long groupId) {
        super("Default channel not found in group " + groupId, ErrorType.NOT_FOUND);
    }
}
