package com.lavacro.finances.services;

import com.lavacro.finances.entities.PermissionEntity;
import com.lavacro.finances.entities.RoleEntity;
import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.entities.UserRoleEntity;
import com.lavacro.finances.repositories.PermissionRepository;
import com.lavacro.finances.repositories.RoleRepository;
import com.lavacro.finances.repositories.RbacUserRepository;
import com.lavacro.finances.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final RbacUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;

    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    public List<PermissionEntity> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<UserRoleEntity> getUserRoles(Integer userId) {
        return userRoleRepository.findByUser_Id(userId);
    }

    @Transactional
    public void assignRoleToUser(Integer userId, String roleName) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        RbacUsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (userRoleRepository.findById_UserIdAndId_RoleId(userId, role.getId()).isPresent()) {
            log.info("User {} already has role {}", userId, roleName);
            return;
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setId(new UserRoleEntity.UserRoleId(userId, role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        log.info("Assigned role {} to user {}", roleName, userId);
    }

    @Transactional
    public void removeRoleFromUser(Integer userId, String roleName) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

        UserRoleEntity userRole = userRoleRepository.findById_UserIdAndId_RoleId(userId, role.getId())
                .orElseThrow(() -> new IllegalArgumentException("User does not have role: " + roleName));

        userRoleRepository.delete(userRole);
        log.info("Removed role {} from user {}", roleName, userId);
    }

    @Transactional
    public void removeAllRolesFromUser(Integer userId) {
        userRoleRepository.deleteByUser_Id(userId);
        log.info("Removed all roles from user {}", userId);
    }

    public Set<PermissionEntity> getUserPermissions(Integer userId) {
        RbacUsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .collect(java.util.stream.Collectors.toSet());
    }
}
