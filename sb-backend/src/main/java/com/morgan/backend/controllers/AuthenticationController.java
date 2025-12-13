package com.morgan.backend.controllers;

import com.morgan.backend.entities.UserAccount;
import com.morgan.backend.repositories.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextLogoutHandler securityContextLogoutHandler;
    private final CookieClearingLogoutHandler cookieClearingLogoutHandler;
    private final UserAccountRepository userAccountRepository;

    public record LoginRequestDto(
        @NotBlank(message = "User name is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
    ) {}

    public record LoginResponseDto(
        String username,
        String email
    ) {}

    /**
     * Performs a REST login while using Spring Security’s session-based mechanism.
     * <p>After successful authentication:</p>
     * <ul>
     *   <li>The Authentication object is placed into a new {@link SecurityContext}.</li>
     *   <li>The SecurityContext is explicitly saved through the SecurityContextRepository,
     *       which creates or uses an {@code HttpSession} and stores the context in it.</li>
     *   <li>During this process, the server sends a {@code JSESSIONID} cookie to the browser
     *       if no session existed yet.</li>
     * </ul>
     * <p>On later requests, the browser automatically includes the {@code JSESSIONID} cookie,
     * allowing Spring Security to retrieve the stored SecurityContext from the session
     * and recognize the user as authenticated.</p>
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request,
                                                  HttpServletRequest servletRequest,
                                                  HttpServletResponse servletResponse) {
        log.info("Request login for username [{}]", request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
                )
            );

            // 1) Build a fresh SecurityContext
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // 2) Explicitly save it to the HttpSession via SecurityContextRepository
            securityContextRepository.saveContext(securityContext, servletRequest, servletResponse);

            String username = authentication.getName();

            // Get full user from DB so we have email
            UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB: " + username));

            // You can enrich the response with roles, etc.
            LoginResponseDto response = new LoginResponseDto(user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            if (ex instanceof BadCredentialsException) {
                log.warn("Invalid login for username [{}]", request.username());
            } else {
                log.warn("Authentication failed for username [{}]: {}", request.username(), ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<LoginResponseDto> getCurrentUser(Authentication authentication) {
        log.info("Request get /current-user");
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();

        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException(
                "Authenticated user not found in DB: " + username));


        return ResponseEntity.ok(new LoginResponseDto(user.getUsername(), user.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Request logout for [{}]", authentication != null ? authentication.getName() : "anonymous");

        if (authentication != null) {
            // 1) Invalidate session + clear SecurityContext
            securityContextLogoutHandler.logout(request, response, authentication);
            // 2) Explicitly clear the JSESSIONID cookie in the browser
            cookieClearingLogoutHandler.logout(request, response, authentication);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        log.info("Request get /csrf");
        // Just accessing `token` forces Spring to create it and send the XSRF-TOKEN cookie
        return Map.of("token", token.getToken());
    }
}
