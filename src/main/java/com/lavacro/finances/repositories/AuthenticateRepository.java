package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.AuthenticatedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthenticateRepository extends JpaRepository<AuthenticatedEntity, Integer> {
	@Query(value = """
			SELECT id, password = CRYPT(:password, password) AS authenticated
			FROM rbac.users
			WHERE name = :user
			""", nativeQuery = true)
	AuthenticatedEntity getUser(@Param("password") String password, @Param("user") String user);
}
