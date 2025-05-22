package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.component.SessionRegistry;
import com.sprint.mission.discodeit.entity.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@AllArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final CsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
  private final SessionRegistry sessionRegistry;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {

    CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
    csrfTokenRepository.saveToken(csrfToken, request, response);

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
    repo.saveContext(context, request, response);


    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("token", csrfToken.getToken());
    responseBody.put("id", userDetails.getUserId());
    responseBody.put("username", userDetails.getUsername());

    response.setContentType("application/json");

    sessionRegistry.register(userDetails.getUserId(), request.getSession(false));
    new ObjectMapper().writeValue(response.getWriter(), responseBody);
  }
}
