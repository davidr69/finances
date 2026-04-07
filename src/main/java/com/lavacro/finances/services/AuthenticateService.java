package com.lavacro.finances.services;

import com.lavacro.finances.dto.AuthenticatedDTO;
import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.repositories.RbacUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticateService {
	private final JdbcClient jdbcClient;
	private final RbacUserRepository rbacUserRepository;

	@Language("SQL")
	private static final String USER_QUERY = """
		SELECT id, password = CRYPT(?, password) AS authenticated
		FROM rbac.users
		WHERE name = ?
	""";

	AuthenticateService(JdbcClient jdbcClient, RbacUserRepository rbacUserRepository) {
		this.jdbcClient = jdbcClient;
		this.rbacUserRepository = rbacUserRepository;
	}

	public AuthenticatedDTO authenticate(String name, String password) {
		log.info("authenticate: {}", name);

		return jdbcClient.sql(USER_QUERY).params(password, name).query(AuthenticatedDTO.class).single();
	}

	public RbacUsersEntity findUser(Integer userId) {
		return rbacUserRepository.findById(userId).orElse(new RbacUsersEntity());
	}

	public void updateUser(RbacUsersEntity user) {
		rbacUserRepository.save(user);
	}
}
