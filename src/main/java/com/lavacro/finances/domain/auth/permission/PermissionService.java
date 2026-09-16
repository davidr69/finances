package com.lavacro.finances.domain.auth.permission;

import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionService {
	private final JdbcClient jdbcClient;

	@Language("SQL")
	private static final String GET_ALL_PERMISSIONS = """
		SELECT u.id AS user_id, u.name AS user_name, u.password, u.login_attempts,
			u.last_login, u.locked, u.locked_ip,
			r.id AS role_id, r.name AS role_name, p.id AS permission_id, p.name AS permission_name
		FROM rbac.users u
		JOIN rbac.user_roles ur ON u.id = ur.user_id
		JOIN rbac.roles r ON ur.role_id = r.id
		JOIN rbac.role_permissions rp ON r.id = rp.role_id
		JOIN rbac.permissions p ON rp.permission_id = p.id
		WHERE u.name = ?
		ORDER BY role_name
	""";

	public UserDTO getUserPermissions(String user) {
		var rows = jdbcClient.sql(GET_ALL_PERMISSIONS).param(user).query(UserPermissionsDTO.class).list();
		if (rows.isEmpty()) {
//			return new UserDTO(null, user, null, null, null, null, null, new HashSet<>());
			return null;
		}

		Set<PermissionDTO> permissions = new HashSet<>();
		Set<RoleDTO> roles = new HashSet<>();
		boolean first = true;

		UserPermissionsDTO model = null;
		UserPermissionsDTO lastRow = null;

		String roleName = null;

		for (var row : rows) {
			PermissionDTO permission = new PermissionDTO(row.permissionId(), row.permissionName());
			permissions.add(permission);

			if(first) {
				first = false;
				model = row;
			}

			if(roleName == null) {
				roleName = row.roleName();
			} else if (!roleName.equals(row.roleName())) {
				roles.add(new RoleDTO(row.roleId(), row.roleName(), permissions));
				permissions = new HashSet<>();
				roleName = row.roleName();
			}
			lastRow = row;
		}

		roles.add(new RoleDTO(lastRow.roleId(), lastRow.roleName(), permissions));

		return new UserDTO(
			model.userId(),
			model.userName(),
			model.password(),
			model.loginAttempts(),
			model.lastLogin(),
			model.locked(),
			model.lockedIp(),
			roles
		);
	}
}
