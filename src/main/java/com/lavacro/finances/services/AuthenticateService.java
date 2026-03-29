package com.lavacro.finances.services;

import com.lavacro.finances.entities.AuthenticatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@Slf4j
public class AuthenticateService {
	private final DataSource datasource;

	@Language("SQL")
	private static final String USER_QUERY = """
		SELECT id, password = CRYPT(?, password) AS authenticated
		FROM rbac.users
		WHERE name = ?
	""";

	AuthenticateService(DataSource dataSource) {
		this.datasource = dataSource;
	}

	public AuthenticatedDTO authenticate(String name, String password) {
		log.info("authenticate: {}", name);

		AuthenticatedDTO authenticatedDTO = null;
		try (
			var conn = datasource.getConnection();
			var stmt = conn.prepareStatement(USER_QUERY)
		) {
			stmt.setString(1, password);
			stmt.setString(2, name);
			stmt.execute();

			if(stmt.getResultSet().next()) {
				authenticatedDTO = new AuthenticatedDTO();
				authenticatedDTO.setAuthenticated(stmt.getResultSet().getBoolean("authenticated"));
				authenticatedDTO.setId(stmt.getResultSet().getInt("id"));
			}

		} catch(Exception e) {
			log.error(e.getMessage());
		}

		return authenticatedDTO;
	}
}
