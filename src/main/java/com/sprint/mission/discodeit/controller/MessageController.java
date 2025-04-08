package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController implements MessageApi {

  private final MessageService messageService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @RequestPart("messageCreateRequest")  @Valid MessageCreateRequest messageCreateRequest,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(attachments)
        .map(files -> files.stream()
            .map(file -> {
              try {
                return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
                );
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
            .toList())
        .orElse(new ArrayList<>());
    log.info("메세지 생성 요청: {}", messageCreateRequest);
    MessageDto createdMessage = messageService.create(messageCreateRequest, attachmentRequests);
    log.info("메세지 생성 완료: {}", createdMessage.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdMessage);
  }

  @PatchMapping(path = "{messageId}")
  public ResponseEntity<MessageDto> update(@PathVariable("messageId") UUID messageId,
      @RequestBody  @Valid  MessageUpdateRequest request) {
    log.info("메세지 업데이트 요청: {}, request:{}", messageId,request);
    MessageDto updatedMessage = messageService.update(messageId, request);
    log.info("메세지 업데이트 완료: {}", updatedMessage.id());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedMessage);
  }

  @DeleteMapping(path = "{messageId}")
  public ResponseEntity<Void> delete(@PathVariable("messageId") UUID messageId) {
    log.info("메세지 삭제 요청: {}", messageId);
    messageService.delete(messageId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor,
      @PageableDefault(
          size = 50,
          page = 0,
          sort = "createdAt",
          direction = Direction.DESC
      ) Pageable pageable) {
    log.info("메세지 검색 요청: {}", channelId);
    PageResponse<MessageDto> messages = messageService.findAllByChannelId(channelId, cursor,
        pageable);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messages);
  }
}
