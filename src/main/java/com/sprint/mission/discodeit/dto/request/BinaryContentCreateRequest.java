package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BinaryContentCreateRequest(
	@NotBlank
    String fileName,
	@NotBlank
    String contentType,
	@Size(max = 10_000_000)
    byte[] bytes
) {

}
