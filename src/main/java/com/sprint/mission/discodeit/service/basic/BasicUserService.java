package com.sprint.mission.discodeit.service.basic;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.User.UserAlreadyExistException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

	private final UserRepository userRepository;
	private final UserStatusRepository userStatusRepository;
	private final UserMapper userMapper;
	private final BinaryContentRepository binaryContentRepository;
	private final BinaryContentStorage binaryContentStorage;

	@Transactional
	@Override
	public UserDto create(UserCreateRequest userCreateRequest,
		Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {

		log.info("사용자 생성 요청: username={}, email={}", userCreateRequest.username(), userCreateRequest.email());
		String username = userCreateRequest.username();
		String email = userCreateRequest.email();

		if (userRepository.existsByEmail(email)) {
			log.error("사용자 생성 실패 - 이미 존재하는 이메일: {}", email);
			throw new UserAlreadyExistException(ErrorCode.USER_EMAIL_DUPLICATED);
		}
		if (userRepository.existsByUsername(username)) {
			log.error("사용자 생성 실패 - 이미 존재하는 사용자명: {}", username);
			throw new UserAlreadyExistException(ErrorCode.USER_ALREADY_EXISTS);
		}

		BinaryContent nullableProfile = optionalProfileCreateRequest
			.map(profileRequest -> {
				log.debug("프로필 파일 저장: fileName={}", profileRequest.fileName());
				String fileName = profileRequest.fileName();
				String contentType = profileRequest.contentType();
				byte[] bytes = profileRequest.bytes();
				BinaryContent binaryContent = new BinaryContent(fileName, (long)bytes.length, contentType);
				binaryContentRepository.save(binaryContent);
				binaryContentStorage.put(binaryContent.getId(), bytes);
				return binaryContent;
			})
			.orElse(null);

		String password = userCreateRequest.password();
		User user = new User(username, email, password, nullableProfile);
		Instant now = Instant.now();
		UserStatus userStatus = new UserStatus(user, now);
		userRepository.save(user);
		userStatusRepository.save(userStatus);


		log.info("사용자 생성 완료: id={}, username={}", user.getId(), username);
		BinaryContentDto binaryContentDto = null;
		if(user.getProfile()!=null){
			binaryContentDto=new BinaryContentDto(user.getProfile().getId(),user.getProfile().getFileName(),
				user.getProfile().getSize(),user.getProfile().getContentType());
		}else{
			new BinaryContentDto(null, "profile.jpg", 100L, "image/jpeg");
		}

		boolean status=(user.getStatus()!=null)?user.getStatus().isOnline():null;
		UserDto userDto=new UserDto(user.getId(), user.getUsername(), user.getEmail()
			,binaryContentDto,status);
		//User user1 = new User(username, email, password, nullableProfile);

		return userDto;
	}

	@Override
	public UserDto find(UUID userId) {
		log.debug("사용자 조회 요청: userId={}", userId);
		return userRepository.findById(userId)
			.map(user -> {
				log.info("사용자 조회 성공: userId={}", userId);
				return userMapper.toDto(user);
			})
			.orElseThrow(() -> {
				log.error("사용자 조회 실패 - 존재하지 않음: userId={}", userId);
				return new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
			});
	}

	@Override
	public List<UserDto> findAll() {
		log.info("전체 사용자 조회 요청");
		List<UserDto> userList = userRepository.findAllWithProfileAndStatus()
			.stream()
			.map(userMapper::toDto)
			.toList();
		log.info("전체 사용자 조회 완료 - 총 {}명", userList.size());
		return userList;
	}

	@Transactional
	@Override
	public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
		Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {

		log.info("사용자 수정 요청: userId={}, newUsername={}, newEmail={}",
			userId, userUpdateRequest.newUsername(), userUpdateRequest.newEmail());

		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.error("사용자 수정 실패 - 존재하지 않음: userId={}", userId);
				return new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
			});

		String newUsername = userUpdateRequest.newUsername();
		String newEmail = userUpdateRequest.newEmail();

		if (userRepository.existsByEmail(newEmail)) {
			log.error("사용자 수정 실패 - 이미 존재하는 이메일: {}", newEmail);
			throw new UserAlreadyExistException(ErrorCode.USER_EMAIL_DUPLICATED);
		}
		if (userRepository.existsByUsername(newUsername)) {
			log.error("사용자 수정 실패 - 이미 존재하는 사용자명: {}", newUsername);
			throw new UserAlreadyExistException(ErrorCode.USER_ALREADY_EXISTS);
		}

		BinaryContent nullableProfile = optionalProfileCreateRequest
			.map(profileRequest -> {
				log.debug("프로필 파일 수정: fileName={}", profileRequest.fileName());
				String fileName = profileRequest.fileName();
				String contentType = profileRequest.contentType();
				byte[] bytes = profileRequest.bytes();
				BinaryContent binaryContent = new BinaryContent(fileName, (long)bytes.length, contentType);
				binaryContentRepository.save(binaryContent);
				binaryContentStorage.put(binaryContent.getId(), bytes);
				return binaryContent;
			})
			.orElse(null);

		String newPassword = userUpdateRequest.newPassword();
		user.update(newUsername, newEmail, newPassword, nullableProfile);
		userRepository.save(user);

		log.info("사용자 수정 완료: userId={}", userId);
		return userMapper.toDto(user);
	}

	@Transactional
	@Override
	public void delete(UUID userId) {
		log.warn("사용자 삭제 요청: userId={}", userId);

		if (!userRepository.existsById(userId)) {
			log.error("사용자 삭제 실패 - 존재하지 않음: userId={}", userId);
			throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
		}

		userRepository.deleteById(userId);
		log.warn("사용자 삭제 완료: userId={}", userId);
	}
}
