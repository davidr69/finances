package com.lavacro.finances.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticateService {
	private final JdbcClient jdbcClient;

	void updateLoginTime(Integer userId) {
		jdbcClient.sql("UPDATE rbac.users SET last_login = ?, login_attempts = NULL WHERE id = ?")
			.param(LocalDateTime.now())
			.param(userId)
			.update();
	}

	void updateLoginAttempts(Integer userId, Integer attempts) {
		jdbcClient.sql("UPDATE rbac.users SET login_attempts = ? WHERE id = ?")
			.param(attempts)
			.param(userId)
			.update();

	}

	void lockUser(String ipAddr, Integer userId) {
		jdbcClient.sql("UPDATE rbac.users SET locked = true, locked_ip = ? WHERE id = ?")
			.param(ipAddr)
			.param(userId)
			.update();

	}
}
