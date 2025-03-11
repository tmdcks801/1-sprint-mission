package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  MessageMapper instance= Mappers.getMapper(MessageMapper.class);
  MessageDto messageToDto(Message m);
  Message dtoToMessage(MessageDto m);


}
