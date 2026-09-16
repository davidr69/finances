package com.lavacro.finances.domain.auth.permission;

import com.lavacro.finances.domain.auth.entity.UserRoleEntity;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDTO(
	Integer id,
	String name,
	String password,
	Integer loginAttempts,
	LocalDateTime lastLogin,
	Boolean locked,
	String lockedIp,
	Set<RoleDTO> roles
) {
}
