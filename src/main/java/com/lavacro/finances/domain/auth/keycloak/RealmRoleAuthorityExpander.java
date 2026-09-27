package com.lavacro.finances.domain.auth.keycloak;

import com.lavacro.finances.domain.auth.permission.PermissionDTO;
import com.lavacro.finances.domain.auth.permission.PermissionService;
import com.lavacro.finances.domain.auth.permission.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Expands Keycloak realm-role names (as found in a token's realm_access.roles
 * claim) into the ROLE_* and PERMISSION_* GrantedAuthority strings the app's
 * @PreAuthorize annotations and SecurityConfig.hasAuthority(...) checks
 * already expect, via the existing rbac.roles / rbac.role_permissions tables.
 * <br/>
 * Shared by both authentication paths:
 *  - browser SSO (OIDC login), where roles come from the ID token
 *  - REST API bearer auth (resource server), where roles come from the
 *    access token presented directly by the caller (preemptive auth)
 */
@Component
@RequiredArgsConstructor
public class RealmRoleAuthorityExpander {

    private final PermissionService permissionService;

    public Set<GrantedAuthority> expand(Collection<String> roleNames) {
        Set<RoleDTO> roles = permissionService.getPermissionsForRoleNames(roleNames);

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (RoleDTO role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.roleName()));
            for (PermissionDTO permission : role.permissions()) {
                authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission.permissionName()));
            }
        }
        return authorities;
    }

    @SuppressWarnings("unchecked")
    public List<String> realmRolesFromClaims(Map<String, Object> claims) {
        Object realmAccess = claims.get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }
        Object roles = realmAccessMap.get("roles");
        if (roles instanceof List<?> roleList) {
            return roleList.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}
