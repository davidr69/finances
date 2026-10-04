package com.lavacro.finances.domain.auth.permission;

import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

public record UserPermissionsDTO(
	@Column("user_id")
	Integer userId,
	@Column("user_name")
	String userName,
	String password,
	@Column("login_attempts")
	Integer loginAttempts,
	@Column("last_login")
	LocalDateTime lastLogin,
	Boolean locked,
	@Column("locked_ip")
	String lockedIp,
	@Column("role_id")
	Integer roleId,
	@Column("role_name")
	String roleName,
	@Column("permission_id")
	Integer permissionId,
	@Column("permission_name")
	String permissionName
) {
}
