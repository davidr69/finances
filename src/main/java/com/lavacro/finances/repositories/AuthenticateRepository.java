package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.RbacUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthenticateRepository extends JpaRepository<RbacUsersEntity, Integer> {
	@Query(value = """
			SELECT id, password = CRYPT(:password, password) AS authenticated
			FROM rbac.users
			WHERE name = :user
			""", nativeQuery = true)
	List<RbacUsersEntity> isAuthenticated(@Param("password") String password, @Param("user") String user);
}
