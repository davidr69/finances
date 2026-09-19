package com.lavacro.finances.domain.auth;

import com.lavacro.finances.domain.auth.permission.PermissionService;
import com.lavacro.finances.domain.auth.permission.UserDTO;
import com.lavacro.finances.model.ActionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthenticateAPI {
	private final AuthenticationManager authenticationManager;
	private final PermissionService permissionService;
	private final SecurityContextRepository securityContextRepository;
	private final AuthenticateService authenticateService;

	@PostMapping(value = "/authenticate")
	public ActionResponse authenticate(
			HttpServletRequest req,
			HttpServletResponse resp,
			@RequestParam("user") final String user,
			@RequestParam("pass") final String pass) {

		log.info("user: {}", user);
		ActionResponse response = new ActionResponse();

		UserDTO userDTO = permissionService.getUserPermissions(user);

		if (userDTO == null) {
			response.setCode(1);
			response.setMessage("Authentication error");
			return response;
		}

		if (userDTO.locked() != null && userDTO.locked()) {
			response.setCode(1);
			response.setMessage("User is locked");
			log.error("Attempted login for {} while user is locked", user);
			return response;
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(user, pass)
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			securityContextRepository.saveContext(SecurityContextHolder.getContext(), req, resp);

			authenticateService.updateLoginTime(userDTO.id());
			response.setCode(0);
			response.setMessage("success");
			log.info("Authenticated successfully for user: {}", user);
		} catch (AuthenticationException e) {
			log.error("Authentication failed for {}", user);
			int attempts = Optional.ofNullable(userDTO.loginAttempts()).orElse(0);
			attempts++;

			authenticateService.updateLoginAttempts(userDTO.id(), attempts);
			response.setCode(1);

			if (attempts >= 3) {
				authenticateService.lockUser(req.getRemoteAddr(), userDTO.id());
				response.setMessage("Too many failed attempts");
				log.error("Too many failed attempts for {}", user);
			} else {
				response.setMessage("Authentication failed");
			}
		}

		return response;
	}
}
