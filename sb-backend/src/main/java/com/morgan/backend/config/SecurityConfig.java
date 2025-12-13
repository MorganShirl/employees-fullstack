package com.morgan.backend.config;

import com.morgan.backend.filters.CsrfCookieFilter;
import com.morgan.backend.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserAccountRepository userAccountRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository,
                                                   CsrfCookieFilter csrfCookieFilter,
                                                   CsrfTokenRequestAttributeHandler csrfRequestHandler) throws Exception {

        http
            // tells Spring Security to delegate CORS decisions to the MVC CORS config / WebMvcConfigurer = CorsConfig
            .cors(Customizer.withDefaults())

            // To turn off CSRF tokens
            //.csrf(csrf -> csrf.disable())

            // CSRF ENABLED with cookie-based token usable by Angular
            .csrf(csrf -> csrf
                .csrfTokenRepository(
                    // exposes token via "XSRF-TOKEN" cookie (NOT HttpOnly so Angular can read it)
                    CookieCsrfTokenRepository.withHttpOnlyFalse()
                )
                .csrfTokenRequestHandler(csrfRequestHandler)
                // login/logout are allowed without CSRF token (important for first login)
                .ignoringRequestMatchers(
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/h2/**"
                )
            )

            // Ensure the filter chain loads/saves the SecurityContext using the same session-based repository
            .securityContext(sc -> sc.securityContextRepository(securityContextRepository))

            .authorizeHttpRequests(auth -> auth
                // public endpoints
                .requestMatchers(
                    "/api/auth/login",
                    "/h2/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/error"
                ).permitAll()

                // secure your REST API
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/auth/current-user").authenticated()
                .requestMatchers("/api/auth/csrf").authenticated()
                .requestMatchers("/api/employees/**").authenticated()

                // everything else = SPA / static → public
                .anyRequest().permitAll()
            )

            // No default login form / basic auth UI; we have our own REST login
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        // H2 frames allowed from same origin (safer than disable())
        http.headers(headers ->
            headers.frameOptions(frame -> frame.sameOrigin()));

        // Force CSRF token to be generated & sent as cookie on each request
        http.addFilterAfter(csrfCookieFilter, CsrfFilter.class);

        return http.build();
    }

    /**
     * Bridge your UserAccountRepository to Spring Security.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userAccountRepository.findByUsername(username)
            .map(user -> {
                // adapt to your UserAccount entity fields
                // example: role "USER" for everyone for now
                UserDetails details = User
                    .withUsername(user.getUsername())
                    .password(user.getPasswordHash())
                    .roles("USER")
                    .build();
                return details;
            })
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager used by AuthenticationController.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean
    public CookieClearingLogoutHandler cookieClearingLogoutHandler() {
        return new CookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN");
    }

    @Bean
    public CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
        var  requestHandler = new CsrfTokenRequestAttributeHandler();
        // Force token to be loaded on every request (Spring 5.x-style behavior)
        requestHandler.setCsrfRequestAttributeName(null);
        return requestHandler;
    }
}

