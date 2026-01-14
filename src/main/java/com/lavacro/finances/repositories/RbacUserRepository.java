package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.RbacUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RbacUserRepository extends JpaRepository<RbacUsersEntity, Integer> { }
