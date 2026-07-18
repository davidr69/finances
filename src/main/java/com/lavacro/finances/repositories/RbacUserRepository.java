package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.RbacUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RbacUserRepository extends JpaRepository<RbacUsersEntity, Integer> {
    Optional<RbacUsersEntity> findByName(String name);
}
