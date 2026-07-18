package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.model.ActionResponse;
import com.lavacro.finances.repositories.RbacUserRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class Authenticate {
	private final AuthenticationManager authenticationManager;
	private final RbacUserRepository userRepository;
	private final SecurityContextRepository securityContextRepository;

	@PostMapping(value = "/authenticate")
	public ActionResponse authenticate(
			HttpServletRequest req,
			HttpServletResponse resp,
			@RequestParam("user") final String user,
			@RequestParam("pass") final String pass) {

		log.info("user: {}", user);
		ActionResponse response = new ActionResponse();

		RbacUsersEntity userEntity = userRepository.findByName(user)
				.orElse(null);

		if (userEntity == null) {
			response.setCode(1);
			response.setMessage("Authentication error");
			return response;
		}

		if (userEntity.getLocked() != null && userEntity.getLocked()) {
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

			userEntity.setLastLogin(LocalDateTime.now());
			userEntity.setLoginAttempts(null);
			userRepository.save(userEntity);

			response.setCode(0);
			response.setMessage("success");
			log.info("Authenticated successfully for user: {}", user);
		} catch (AuthenticationException e) {
			log.error("Authentication failed for {}", user);
			int attempts = Optional.ofNullable(userEntity.getLoginAttempts()).orElse(0);
			attempts++;
			userEntity.setLoginAttempts(attempts);
			response.setCode(1);

			if (attempts >= 3) {
				userEntity.setLocked(true);
				userEntity.setLockedIp(req.getRemoteAddr());
				response.setMessage("Too many failed attempts");
				log.error("Too many failed attempts for {}", user);
			} else {
				response.setMessage("Authentication failed");
			}
			userRepository.save(userEntity);
		}

		return response;
	}
}
