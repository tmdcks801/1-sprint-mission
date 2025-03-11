package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserMapper INSTANCE= Mappers.getMapper(UserMapper.class);
  UserDto userToDto(User user);
  User dtoToUser(UserDto userDto);
}
