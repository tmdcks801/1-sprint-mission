package com.sprint.mission.discodeit.exception.Channel;

import java.util.Map;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class PrivateChannelUpdateExeption extends ChannelException{
	public PrivateChannelUpdateExeption(ErrorCode errorCode) {
		super(errorCode);
	}
	public PrivateChannelUpdateExeption(ErrorCode errorCode, Map<String, Object> details) {
		super(errorCode);
	}
}
