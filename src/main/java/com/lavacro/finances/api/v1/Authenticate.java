package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.AuthenticatedEntity;
import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.model.ActionResponse;
import com.lavacro.finances.repositories.AuthenticateRepository;
import com.lavacro.finances.repositories.RbacUserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@Slf4j
public class Authenticate {
	private final AuthenticateRepository authenticateRepository;
	private final RbacUserRepository rbacUserRepository;

	public Authenticate(
			AuthenticateRepository authenticateRepository,
			RbacUserRepository rbacUserRepository
	) {
		this.authenticateRepository = authenticateRepository;
		this.rbacUserRepository = rbacUserRepository;
	}

	@PostMapping(value = "/authenticate")
	public ActionResponse authenticate(
			HttpServletRequest req,
			HttpSession session,
			@RequestParam("user") final String user,
			@RequestParam("pass") final String pass) {

		log.info("user: {}", user);
		ActionResponse resp = new ActionResponse();

		AuthenticatedEntity authenticated = authenticateRepository.getUser(pass, user);
		if(authenticated == null) {
			log.error("No results!");
			resp.setCode(1);
			resp.setMessage("Authentication error");
			return resp;
		}

		RbacUsersEntity userEntity = rbacUserRepository.findById(authenticated.getId()).orElse(new RbacUsersEntity());
		if(userEntity.getLocked() != null && userEntity.getLocked()) {
			resp.setCode(1);
			resp.setMessage("User is locked");
			log.error("Attempted login for {} while user is locked", user);
		} else {
			if (Boolean.TRUE.equals(authenticated.getAuthenticated())) {
				userEntity.setLastLogin(LocalDateTime.now());
				userEntity.setLoginAttempts(null);
				resp.setCode(0);
				resp.setMessage("success");

				session.setAttribute("user", user);
				log.info("Authenticated successfully");
			} else {
				log.error("Authentication failed for {}", user);
				int attempts = Optional.ofNullable(userEntity.getLoginAttempts()).orElse(0);
				attempts++;
				userEntity.setLoginAttempts(attempts);
				resp.setCode(1);
				if(attempts >= 3) {
					userEntity.setLocked(true);
					userEntity.setLockedIp(req.getRemoteAddr());
					resp.setMessage("Too many failed attempts");
					log.error("Too many failed attempts for {}", user);
				} else {
					resp.setMessage("Authentication failed");
				}
			}
			rbacUserRepository.save(userEntity);
		}
		return resp;
	}
}
