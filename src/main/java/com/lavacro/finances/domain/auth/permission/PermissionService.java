package com.lavacro.finances.domain.auth.permission;

import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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

	@Language("SQL")
	private static final String GET_PERMISSIONS_FOR_ROLE_NAMES = """
		SELECT r.id AS role_id, r.name AS role_name, p.id AS permission_id, p.name AS permission_name
		FROM rbac.roles r
		LEFT JOIN rbac.role_permissions rp ON r.id = rp.role_id
		LEFT JOIN rbac.permissions p ON rp.permission_id = p.id
		WHERE r.name IN (:roleNames)
	""";

	/**
	 * Looks up permissions by realm role name rather than by local user, for
	 * authorities sourced from a Keycloak token's realm_access.roles claim.
	 * A role with no permission rows still comes back (as a RoleDTO with an
	 * empty permission set) via the LEFT JOIN, so the ROLE_* authority is
	 * never silently dropped just because the role has no permissions yet.
	 */
	public Set<RoleDTO> getPermissionsForRoleNames(Collection<String> roleNames) {
		if (roleNames.isEmpty()) {
			return Set.of();
		}

		var rows = jdbcClient.sql(GET_PERMISSIONS_FOR_ROLE_NAMES)
			.param("roleNames", roleNames)
			.query(RolePermissionRowDTO.class)
			.list();

		Map<Integer, String> roleNameById = new LinkedHashMap<>();
		Map<Integer, Set<PermissionDTO>> permissionsByRoleId = new LinkedHashMap<>();

		for (var row : rows) {
			roleNameById.put(row.roleId(), row.roleName());
			Set<PermissionDTO> permissions = permissionsByRoleId.computeIfAbsent(row.roleId(), id -> new HashSet<>());
			if (row.permissionId() != null) {
				permissions.add(new PermissionDTO(row.permissionId(), row.permissionName()));
			}
		}

		Set<RoleDTO> result = new HashSet<>();
		for (var entry : roleNameById.entrySet()) {
			result.add(new RoleDTO(entry.getKey(), entry.getValue(), permissionsByRoleId.get(entry.getKey())));
		}
		return result;
	}

	public UserDTO getUserPermissions(String user) {
		var rows = jdbcClient.sql(GET_ALL_PERMISSIONS).param(user).query(UserPermissionsDTO.class).list();
		if (rows.isEmpty()) {
			return null;
		}

		Set<PermissionDTO> permissions = new HashSet<>();
		Set<RoleDTO> roles = new HashSet<>();

		UserPermissionsDTO lastRow = null;

		String roleName = null;

		for (var row : rows) {
			PermissionDTO permission = new PermissionDTO(row.permissionId(), row.permissionName());
			permissions.add(permission);

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
			lastRow.userId(),
			lastRow.userName(),
			lastRow.password(),
			lastRow.loginAttempts(),
			lastRow.lastLogin(),
			lastRow.locked(),
			lastRow.lockedIp(),
			roles
		);
	}
}
