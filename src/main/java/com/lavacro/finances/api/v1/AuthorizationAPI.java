package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.PermissionEntity;
import com.lavacro.finances.entities.RoleEntity;
import com.lavacro.finances.entities.UserRoleEntity;
import com.lavacro.finances.model.ActionResponse;
import com.lavacro.finances.services.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERMISSION_REFRESH_VECTORS')") // Admin-only access
public class AuthorizationAPI {

    private final AuthorizationService authorizationService;

    @GetMapping("/roles")
    public List<RoleEntity> getAllRoles() {
        return authorizationService.getAllRoles();
    }

    @GetMapping("/permissions")
    public List<PermissionEntity> getAllPermissions() {
        return authorizationService.getAllPermissions();
    }

    @GetMapping("/users/{userId}/roles")
    public List<UserRoleEntity> getUserRoles(@PathVariable Integer userId) {
        return authorizationService.getUserRoles(userId);
    }

    @GetMapping("/users/{userId}/permissions")
    public Set<PermissionEntity> getUserPermissions(@PathVariable Integer userId) {
        return authorizationService.getUserPermissions(userId);
    }

    @PostMapping("/users/{userId}/roles/{roleName}")
    public ActionResponse assignRoleToUser(
            @PathVariable Integer userId,
            @PathVariable String roleName) {
        try {
            authorizationService.assignRoleToUser(userId, roleName);
            ActionResponse resp = new ActionResponse();
            resp.setCode(0);
            resp.setMessage("Role assigned successfully");
            return resp;
        } catch (Exception e) {
            log.error("Error assigning role: {}", e.getMessage());
            ActionResponse resp = new ActionResponse();
            resp.setCode(1);
            resp.setMessage("Error assigning role: " + e.getMessage());
            return resp;
        }
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ActionResponse removeRoleFromUser(
            @PathVariable Integer userId,
            @PathVariable String roleName) {
        try {
            authorizationService.removeRoleFromUser(userId, roleName);
            ActionResponse resp = new ActionResponse();
            resp.setCode(0);
            resp.setMessage("Role removed successfully");
            return resp;
        } catch (Exception e) {
            log.error("Error removing role: {}", e.getMessage());
            ActionResponse resp = new ActionResponse();
            resp.setCode(1);
            resp.setMessage("Error removing role: " + e.getMessage());
            return resp;
        }
    }

    @DeleteMapping("/users/{userId}/roles")
    public ActionResponse removeAllRolesFromUser(@PathVariable Integer userId) {
        try {
            authorizationService.removeAllRolesFromUser(userId);
            ActionResponse resp = new ActionResponse();
            resp.setCode(0);
            resp.setMessage("All roles removed successfully");
            return resp;
        } catch (Exception e) {
            log.error("Error removing roles: {}", e.getMessage());
            ActionResponse resp = new ActionResponse();
            resp.setCode(1);
            resp.setMessage("Error removing roles: " + e.getMessage());
            return resp;
        }
    }
}
