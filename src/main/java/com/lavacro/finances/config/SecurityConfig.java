package com.lavacro.finances.config;

import com.lavacro.finances.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@Profile("!reconcile")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;
	private final SessionConfig.SessionValidationFilter sessionValidationFilter;

	private static final String LOGIN = "/login.html";

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.securityContext(securityContext -> securityContext
				.securityContextRepository(securityContextRepository())
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(LOGIN, "/actuator/**", "/authenticate", "/css/**", "/js/**", "/font-awesome-4.7.0/**", "/favicon.ico").permitAll()
				.requestMatchers("/upload", "/api/v1/upload_statement").hasAuthority("PERMISSION_UPLOAD_STATEMENT")
				.requestMatchers("/merge_statement", "/api/v1/statement_merge").hasAuthority("PERMISSION_MERGE_STATEMENT")
				.requestMatchers("/api/v1/refresh_vectors").hasAuthority("PERMISSION_REFRESH_VECTORS")
				.anyRequest().authenticated()
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					if (request.getServletPath().startsWith("/api/")) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} else {
						log.info("Redirecting to login page");
						response.sendRedirect(loginRedirectUrl(request));
					}
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					if (request.getServletPath().startsWith("/api/")) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
					} else {
						log.info("Access denied");
						response.sendRedirect(loginRedirectUrl(request));
					}
				})
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessHandler((request, response, authentication) -> {
					log.info("Logging out");
					response.sendRedirect(loginRedirectUrl(request));
				})
				.addLogoutHandler((request, response, authentication) -> {
					log.info("Invalidating session");
					request.getSession().invalidate();
				})
				.permitAll()
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
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
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
		return authConfig.getAuthenticationManager();
	}

	private String loginRedirectUrl(HttpServletRequest request) {
		log.info("login redirect: {}", request.getRequestURI());
		log.info("context: {}", request.getContextPath());
		return request.getContextPath() + LOGIN;
	}
}
