package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.UserDetails;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthStatusController {
  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<?> me(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
    }
    return ResponseEntity.ok(authentication.getName());
  }

  @GetMapping("/auth/me")
  public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    UserDto userDto=userService.find(userDetails.getUserId());

    return ResponseEntity.ok(userDto);
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    SecurityContextHolder.clearContext();
    return ResponseEntity.ok().build();
  }

  @PutMapping("/auth/role")
  public ResponseEntity<UserDto> updateUserRole( RoleUpdateRequest request){

    UserDto userDto=userService.updateUserRole(request.getUserId(),request.getNewRole());
    return ResponseEntity.ok(userDto);

  }

}