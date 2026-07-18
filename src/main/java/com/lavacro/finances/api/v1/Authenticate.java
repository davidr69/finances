package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.model.ActionResponse;
import com.lavacro.finances.repositories.RbacUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@Slf4j
public class Authenticate {
	private final AuthenticationManager authenticationManager;
	private final RbacUserRepository userRepository;

	public Authenticate(AuthenticationManager authenticationManager, RbacUserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
	}

	@PostMapping(value = "/authenticate")
	public ActionResponse authenticate(
			HttpServletRequest req,
			@RequestParam("user") final String user,
			@RequestParam("pass") final String pass) {

		log.info("user: {}", user);
		ActionResponse resp = new ActionResponse();

		RbacUsersEntity userEntity = userRepository.findByName(user)
				.orElse(null);

		if (userEntity == null) {
			resp.setCode(1);
			resp.setMessage("Authentication error");
			return resp;
		}

		if (userEntity.getLocked() != null && userEntity.getLocked()) {
			resp.setCode(1);
			resp.setMessage("User is locked");
			log.error("Attempted login for {} while user is locked", user);
			return resp;
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(user, pass)
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			userEntity.setLastLogin(LocalDateTime.now());
			userEntity.setLoginAttempts(null);
			userRepository.save(userEntity);

			resp.setCode(0);
			resp.setMessage("success");
			log.info("Authenticated successfully for user: {}", user);
		} catch (AuthenticationException e) {
			log.error("Authentication failed for {}", user);
			int attempts = Optional.ofNullable(userEntity.getLoginAttempts()).orElse(0);
			attempts++;
			userEntity.setLoginAttempts(attempts);
			resp.setCode(1);

			if (attempts >= 3) {
				userEntity.setLocked(true);
				userEntity.setLockedIp(req.getRemoteAddr());
				resp.setMessage("Too many failed attempts");
				log.error("Too many failed attempts for {}", user);
			} else {
				resp.setMessage("Authentication failed");
			}
			userRepository.save(userEntity);
		}

		return resp;
	}
}
