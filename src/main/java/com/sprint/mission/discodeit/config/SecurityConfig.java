package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.component.SessionRegistry;
import com.sprint.mission.discodeit.filter.UserNamePasswordFilter;
import com.sprint.mission.discodeit.handler.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AuthenticationManager authenticationManager,
      SessionRegistry sessionRegistry
  ) throws Exception {

    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName("XSRF-TOKEN");
    csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");

    http
        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK))
            .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            .invalidateHttpSession(true)
        )
        .formLogin(AbstractHttpConfigurer::disable)
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .ignoringRequestMatchers("/api/auth/login", "/api/auth/csrf-token")
        )
        .authorizeHttpRequests(auth -> {
          auth
              .requestMatchers(
                  "/", "/index.html", "/favicon.ico", "/error",
                  "/css/**", "/js/**", "/images/**",
                  "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**",
                  "/api/auth/csrf-token", "/api/users/**", "/assets/**", "/api/auth/login"
              ).permitAll();

          auth
              .requestMatchers(HttpMethod.PUT, "/api/auth/role")
              .hasRole("ADMIN");

          auth
              .requestMatchers(HttpMethod.POST, "/api/channels/public")
              .hasAnyRole("CHANNEL_MANAGER", "ADMIN"); // ✅ 계층 대신 명시 허용

          auth
              .requestMatchers(HttpMethod.PUT, "/api/channels/public/**")
              .hasAnyRole("CHANNEL_MANAGER", "ADMIN");

          auth
              .requestMatchers(HttpMethod.DELETE, "/api/channels/public/**")
              .hasAnyRole("CHANNEL_MANAGER", "ADMIN");

          auth
              .requestMatchers("/api/**")
              .authenticated();

          auth
              .anyRequest()
              .authenticated();
        })
        .securityContext(context -> context
            .securityContextRepository(new HttpSessionSecurityContextRepository())
        )
        .addFilterBefore(
            customAuthFilter(authenticationManager, sessionRegistry),
            UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder
  ) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }

  @Bean
  public UserNamePasswordFilter customAuthFilter(
      AuthenticationManager authenticationManager,
      SessionRegistry sessionRegistry
  ) {
    UserNamePasswordFilter filter =
        new UserNamePasswordFilter(authenticationManager);

    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName("CSRF-TOKEN");
    csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");

    filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(sessionRegistry));
    return filter;
  }
}
