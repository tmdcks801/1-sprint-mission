package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

interface BinaryContentStorage {
  UUID put(UUID id ,byte bytes[]);
  InputStream get(UUID id);
  <T> ResponseEntity<T> download(BinaryContentDto binaryContentDto);
}