package com.lavacro.finances.domain.auth.permission;

import org.springframework.data.relational.core.mapping.Column;

public record PermissionDTO(
	@Column(value = "user_name")
	String userName,
	@Column(value = "role_name")
	String roleName,
	String permission
) {
}
