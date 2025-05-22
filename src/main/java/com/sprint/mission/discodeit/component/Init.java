package com.sprint.mission.discodeit.component;

import com.sprint.mission.discodeit.eenum.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Init {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @PostConstruct
  public void initAdmin() {
    String adminUsername = "admin";
    String adminEmail = "admin@admin.com";

    boolean exists = userRepository.existsByUsername(adminUsername);
    if (exists) return;

    User admin = new User(
        adminUsername,
        adminEmail,
        passwordEncoder.encode("tmdcks1234"),
        null
    );
    admin.setRole(Role.ROLE_ADMIN);

    userRepository.save(admin);
  }
}
