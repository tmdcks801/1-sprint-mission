package com.sprint.mission.discodeit.dto.data;

import java.util.UUID;


public record BinaryContentDto(
    String fileName,
    Long size,
    String contentType,
    String path,
    UUID usings
) {
}
