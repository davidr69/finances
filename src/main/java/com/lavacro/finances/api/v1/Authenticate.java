package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.model.ActionResponse;
import com.lavacro.finances.repositories.AuthenticateRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class Authenticate {
	private final AuthenticateRepository authenticateRepository;

	public Authenticate(
			AuthenticateRepository authenticateRepository
	) {
		this.authenticateRepository = authenticateRepository;
	}

	@PostMapping(value = "/authenticate")
	public ActionResponse authenticate(
			HttpSession session,
			@RequestParam("user") final String user,
			@RequestParam("pass") final String pass) {

		log.info("user: {}", user);
		ActionResponse resp = new ActionResponse();

		List<RbacUsersEntity> listResults = authenticateRepository.isAuthenticated(pass, user);
		if(listResults.isEmpty()) {
			log.error("No results!");
			resp.setCode(1);
			resp.setMessage("Authentication error");
			return resp;
		}
		RbacUsersEntity rbacUsersEntity = listResults.getFirst();

		if(rbacUsersEntity.isAuthenticated()) {
			resp.setCode(0);
			resp.setMessage("success");

			session.setAttribute("user", user);
			log.info("Authenticated successfully");
		} else {
			resp.setCode(1);
			resp.setMessage("Authentication failed");
		}
		return resp;
	}
}
