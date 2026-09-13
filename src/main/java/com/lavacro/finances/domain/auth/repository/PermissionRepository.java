package com.lavacro.finances.domain.auth.repository;

import com.lavacro.finances.domain.auth.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Integer> {
    Optional<PermissionEntity> findByName(String name);
}
