package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BinaryContentMapper {
  BinaryContentMapper instance= Mappers.getMapper(BinaryContentMapper.class);
  BinaryContentDto binaryContentToDto(BinaryContent b);
  BinaryContent dtoToBinaryContent(BinaryContentDto b);
}
