package com.lavacro.finances.domain.auth.permission;

import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
	private final JdbcClient jdbcClient;

	@Language("SQL")
	private static final String GET_ALL_PERMISSIONS = """
		SELECT p.name AS permission, r.name AS role, u.name AS user_name
		FROM rbac.permissions p
		JOIN rbac.role_permissions rp ON p.id = rp.permission_id
		JOIN rbac.roles r ON rp.role_id = r.id
		JOIN rbac.user_roles ur ON r.id = ur.role_id
		JOIN rbac.users u ON ur.user_id = u.id
	""";

	public List<PermissionDTO> getAllPermissions() {
		return jdbcClient.sql(GET_ALL_PERMISSIONS).query(PermissionDTO.class).list();
	}
}
