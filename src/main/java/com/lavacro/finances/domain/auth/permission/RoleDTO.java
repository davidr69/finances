package com.lavacro.finances.domain.auth.permission;

import java.util.Set;

public record RoleDTO(
	Integer id,
	String roleName,
	Set<PermissionDTO> permissions
) {
}
