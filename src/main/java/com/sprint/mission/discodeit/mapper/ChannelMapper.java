package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

  ChannelMapper instance= Mappers.getMapper(ChannelMapper.class);
  ChannelDto channelToDto(Channel c);
  Channel dtoToChannel(ChannelDto c);

}
