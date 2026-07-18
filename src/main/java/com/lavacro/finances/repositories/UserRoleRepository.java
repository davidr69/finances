package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.UserRoleEntity;
import com.lavacro.finances.entities.UserRoleEntity.UserRoleId;
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
