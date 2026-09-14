package com.lavacro.finances.domain.auth.repository;

import com.lavacro.finances.domain.auth.entity.UserRoleEntity;
import com.lavacro.finances.domain.auth.entity.UserRoleEntity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {
    List<UserRoleEntity> findByUser_Id(Integer userId);
    Optional<UserRoleEntity> findById_UserIdAndId_RoleId(Integer userId, Integer roleId);
    void deleteByUser_Id(Integer userId);
}
