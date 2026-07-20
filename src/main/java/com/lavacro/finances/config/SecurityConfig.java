package com.lavacro.finances.config;

import com.lavacro.finances.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final SessionConfig.SessionValidationFilter sessionValidationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .securityContext(securityContext -> securityContext
                .securityContextRepository(securityContextRepository())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login.html", "/authenticate", "/css/**", "/js/**", "/font-awesome-4.7.0/**", "/favicon.ico").permitAll()
                .requestMatchers("/upload", "/api/v1/upload_statement").hasAuthority("PERMISSION_UPLOAD_STATEMENT")
                .requestMatchers("/merge_statement", "/api/v1/statement_merge").hasAuthority("PERMISSION_MERGE_STATEMENT")
                .requestMatchers("/api/v1/refresh_vectors").hasAuthority("PERMISSION_REFRESH_VECTORS")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        String contextPath = request.getContextPath();
                        response.sendRedirect(contextPath + "/login.html");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    } else {
                        String contextPath = request.getContextPath();
                        response.sendRedirect(contextPath + "/login.html");
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    String contextPath = request.getContextPath();
                    response.sendRedirect(contextPath + "/login.html");
                })
                .addLogoutHandler((request, response, authentication) -> {
                    request.getSession().invalidate();
                })
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .invalidSessionStrategy((request, response) -> {
                    String contextPath = request.getContextPath();
                    response.sendRedirect(contextPath + "/login.html");
                })
                .sessionFixation().migrateSession()
                .maximumSessions(1)
            )
            .addFilterBefore(sessionValidationFilter, SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
