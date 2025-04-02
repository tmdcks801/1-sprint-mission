package com.sprint.mission.discodeit.dto.request;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record ReadStatusCreateRequest(
	@NotBlank
    UUID userId,
	@NotBlank
    UUID channelId,
    Instant lastReadAt
) {

}
