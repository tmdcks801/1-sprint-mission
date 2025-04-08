package com.sprint.mission.discodeit.exception.Channel;

import java.util.Map;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class ChannelNotFoundExeption extends ChannelException{
	public ChannelNotFoundExeption(ErrorCode errorCode) {
		super(errorCode);
	}
	public ChannelNotFoundExeption(ErrorCode errorCode, Map<String, Object> details) {
		super(errorCode);
	}
}
