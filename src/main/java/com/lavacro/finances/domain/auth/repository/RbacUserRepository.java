package com.lavacro.finances.domain.auth.repository;

import com.lavacro.finances.domain.auth.entity.RbacUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RbacUserRepository extends JpaRepository<RbacUsersEntity, Integer> {
    Optional<RbacUsersEntity> findByName(String name);
}
