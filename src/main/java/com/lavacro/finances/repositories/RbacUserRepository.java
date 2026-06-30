package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.RbacUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RbacUserRepository extends JpaRepository<RbacUsersEntity, Integer> { }
