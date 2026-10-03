package com.lavacro.finances.domain.auth.permission;

import org.springframework.data.relational.core.mapping.Column;

public record RolePermissionRowDTO(
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
