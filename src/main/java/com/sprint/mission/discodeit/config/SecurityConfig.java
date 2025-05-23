package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.component.SessionRegistryRepo;
import com.sprint.mission.discodeit.filter.UserNamePasswordFilter;
import com.sprint.mission.discodeit.handler.LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AuthenticationManager authenticationManager,
      SessionRegistryRepo sessionRegistryRepo,
      DataSource dataSource,SessionRegistry sessionRegistry
  ) throws Exception {

    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName("XSRF-TOKEN");
    csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");

    http
        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler((request, response, authentication) ->
                response.setStatus(HttpServletResponse.SC_OK))
            .deleteCookies("JSESSIONID", "XSRF-TOKEN", "remember-me")
            .invalidateHttpSession(true)
            .addLogoutHandler((request, response, authentication) -> {
              if (authentication != null) {
                persistentTokenRepository(dataSource).removeUserTokens(authentication.getName());
              }
            })
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
          auth.requestMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMIN");
          auth.requestMatchers(HttpMethod.POST, "/api/channels/public").hasAnyRole("CHANNEL_MANAGER", "ADMIN");
          auth.requestMatchers(HttpMethod.PUT, "/api/channels/public/**").hasAnyRole("CHANNEL_MANAGER", "ADMIN");
          auth.requestMatchers(HttpMethod.DELETE, "/api/channels/public/**").hasAnyRole("CHANNEL_MANAGER", "ADMIN");
          auth.requestMatchers("/api/**").authenticated();
          auth.anyRequest().authenticated();
        })
        .securityContext(context -> context
            .securityContextRepository(new HttpSessionSecurityContextRepository())
        )
        .addFilterBefore(
            customAuthFilter(authenticationManager, sessionRegistryRepo),
            UsernamePasswordAuthenticationFilter.class
        )
        .rememberMe(remember -> remember
            .key("tmdcks1234")
            .tokenValiditySeconds(60 * 60 * 24 * 21)
            .rememberMeParameter("remember-me")
            .tokenRepository(persistentTokenRepository(dataSource))
        ).sessionManagement(session -> session
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false) // 새 로그인 허용, 기존 세션 만료
            .sessionRegistry(sessionRegistry)
        )
    ;
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
      SessionRegistryRepo sessionRegistryRepo
  ) {
    UserNamePasswordFilter filter =
        new UserNamePasswordFilter(authenticationManager);

    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookieName("CSRF-TOKEN");
    csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");

    filter.setAuthenticationSuccessHandler(new LoginSuccessHandler(sessionRegistryRepo));
    return filter;
  }

  @Bean
  public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
    JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
    repo.setDataSource(dataSource);
    repo.setCreateTableOnStartup(true);
    repo.setCreateTableOnStartup(true);
    return repo;
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }
}
