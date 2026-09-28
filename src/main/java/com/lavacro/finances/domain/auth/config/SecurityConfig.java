package com.lavacro.finances.domain.auth.config;

import com.lavacro.finances.domain.auth.keycloak.KeycloakJwtAuthoritiesConverter;
import com.lavacro.finances.domain.auth.keycloak.KeycloakOidcAuthoritiesMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@Profile("!reconcile")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600, redisNamespace = "finances")
@Slf4j
public class SecurityConfig {
	private final KeycloakOidcAuthoritiesMapper keycloakOidcAuthoritiesMapper;
	private final KeycloakJwtAuthoritiesConverter keycloakJwtAuthoritiesConverter;

	// Spring Security auto-registers this endpoint for the "keycloak"
	// registration configured under spring.security.oauth2.client in
	// application.yml - it kicks off the redirect to Keycloak's hosted login.
	private static final String OIDC_LOGIN_INIT = "/oauth2/authorization/keycloak";

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.securityContext(securityContext -> securityContext
				.securityContextRepository(securityContextRepository())
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/**", "/css/**", "/js/**", "/font-awesome-4.7.0/**", "/favicon.ico").permitAll()
				.requestMatchers("/upload", "/api/v1/upload_statement").hasAuthority("PERMISSION_UPLOAD_STATEMENT")
				.requestMatchers("/merge_statement", "/api/v1/statement_merge").hasAuthority("PERMISSION_MERGE_STATEMENT")
				.requestMatchers("/api/v1/refresh_vectors").hasAuthority("PERMISSION_REFRESH_VECTORS")
				.anyRequest().authenticated()
			)
			// Browser flow: unauthenticated page load -> redirect to Keycloak's
			// hosted login -> session cookie on return. See authenticationEntryPoint
			// below for how unauthenticated /api/** requests are handled differently.
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userAuthoritiesMapper(keycloakOidcAuthoritiesMapper)
				)
				.failureHandler((req, resp, exc) -> {
					log.error("OIDC login failed: {}", exc.getMessage(), exc);
					resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login failed: " + exc.getMessage());
				})
			)
			// REST API flow: caller presents Authorization: Bearer <JWT> up front
			// (preemptive auth) - no redirect, no session. Spring validates the
			// JWT's signature/exp/iss against Keycloak's JWKS before authorities
			// are ever computed.
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					if (request.getServletPath().startsWith("/api/")) {
						// A missing/invalid Bearer token on an API call gets a
						// plain 401 - never a redirect. Browsers redirect, API
						// clients get a status code they can branch on.
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} else {
						log.info("Redirecting to Keycloak login");
						response.sendRedirect(request.getContextPath() + OIDC_LOGIN_INIT);
					}
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					// 403 either way. For browser requests we deliberately do NOT
					// redirect back to Keycloak here: an authenticated user who
					// lacks a permission still has a valid SSO session, so a
					// redirect to OIDC_LOGIN_INIT would just bounce straight
					// back with the same insufficient roles - an infinite loop.
					log.info("Access denied");
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				})
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
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
			);

		return http.build();
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(keycloakJwtAuthoritiesConverter);
		return converter;
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
		OidcIdTokenDecoderFactory delegate = new OidcIdTokenDecoderFactory();
		return registration -> {
			JwtDecoder decoder = delegate.createDecoder(registration);
			return token -> {
				String header = new String(
					java.util.Base64.getUrlDecoder().decode(token.split("\\.")[0]),
					java.nio.charset.StandardCharsets.UTF_8);
				log.info("ID token header: {}", header);
				return decoder.decode(token);
			};
		};
	}
}
