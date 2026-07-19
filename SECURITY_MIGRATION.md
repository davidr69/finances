# Spring Security Authorization Migration

This document describes the Spring Security authorization implementation added to the finances application.

## Overview

The application has been migrated from binary authorization (authenticated = full access) to role-based access control (RBAC) using Spring Security.

## Database Changes

### New Tables (in rbac schema)

- **roles** - Defines user roles (ADMIN, USER, VIEWER)
- **permissions** - Defines granular permissions (UPLOAD_STATEMENT, MERGE_STATEMENT, etc.)
- **role_permissions** - Many-to-many relationship between roles and permissions
- **user_roles** - Many-to-many relationship between users and roles

### Migration Script

Run the SQL migration script:
```bash
psql -U your_user -d your_database -f src/main/resources/db/migration/V1__create_authorization_tables.sql
```

## Code Changes

### Dependencies Added
- `spring-boot-starter-security`
- `spring-security-test`

### New Components

**Entities:**
- `RoleEntity.java` - Role entity with permissions mapping
- `PermissionEntity.java` - Permission entity
- `UserRoleEntity.java` - User-role join entity

**Repositories:**
- `RoleRepository.java` - Role data access
- `PermissionRepository.java` - Permission data access
- `UserRoleRepository.java` - User-role relationship data access

**Security:**
- `SecurityConfig.java` - Spring Security configuration with Redis session integration
- `CustomUserDetailsService.java` - Loads user roles and permissions for Spring Security

**Services:**
- `AuthorizationService.java` - Service for managing user roles and permissions

**API:**
- `AuthorizationAPI.java` - Admin API for role management (admin-only access)

### Modified Components

**Controllers:**
- `Upload.java` - Added `@PreAuthorize("hasAuthority('PERMISSION_UPLOAD_STATEMENT')")`
- `Statements.java` - Added `@PreAuthorize("hasAuthority('PERMISSION_MERGE_STATEMENT')")`

**API Controllers:**
- `StatementsAPI.java` - Added authorization annotations for upload, merge, and refresh vectors endpoints
- `Authenticate.java` - Updated to use Spring Security AuthenticationManager

**Configuration:**
- `UrlFilter.java` - Disabled SessionFilter (Spring Security now handles authentication)
- `RbacUserRepository.java` - Added `findByName()` method

## Permissions

### Available Permissions

- `PERMISSION_UPLOAD_STATEMENT` - Can upload bank statements
- `PERMISSION_MERGE_STATEMENT` - Can merge bank statements
- `PERMISSION_REFRESH_VECTORS` - Can refresh entity vectors (admin only)
- `PERMISSION_MANAGE_ENTITIES` - Can manage entities
- `PERMISSION_MANAGE_TRANSACTIONS` - Can manage transactions
- `PERMISSION_VIEW_REPORTS` - Can view reports

### Default Roles

**ADMIN** - All permissions
**USER** - UPLOAD_STATEMENT, MERGE_STATEMENT, MANAGE_ENTITIES, MANAGE_TRANSACTIONS, VIEW_REPORTS
**VIEWER** - VIEW_REPORTS only

## Redis Session Integration

The application uses Spring Session with Redis for distributed session management across Kubernetes pods. This is already configured in `SessionConfig.java` and is preserved in the Spring Security configuration.

## Protected Endpoints

The following endpoints now require specific permissions:

- `/upload`, `/api/v1/upload_statement` - `PERMISSION_UPLOAD_STATEMENT`
- `/merge_statement`, `/api/v1/statement_merge` - `PERMISSION_MERGE_STATEMENT`
- `/api/v1/refresh_vectors` - `PERMISSION_REFRESH_VECTORS`

## Admin API

Role management is available via `/api/v1/admin/*` endpoints (requires `PERMISSION_REFRESH_VECTORS`):

- `GET /api/v1/admin/roles` - List all roles
- `GET /api/v1/admin/permissions` - List all permissions
- `GET /api/v1/admin/users/{userId}/roles` - Get user's roles
- `GET /api/v1/admin/users/{userId}/permissions` - Get user's permissions
- `POST /api/v1/admin/users/{userId}/roles/{roleName}` - Assign role to user
- `DELETE /api/v1/admin/users/{userId}/roles/{roleName}` - Remove role from user
- `DELETE /api/v1/admin/users/{userId}/roles` - Remove all roles from user

## Initial User Setup

After running the migration, assign roles to existing users:

```sql
-- Assign ADMIN role to user ID 1
INSERT INTO rbac.user_roles (user_id, role_id)
SELECT 1, id FROM rbac.roles WHERE name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Assign USER role to other users
INSERT INTO rbac.user_roles (user_id, role_id)
SELECT user_id, (SELECT id FROM rbac.roles WHERE name = 'USER')
FROM rbac.users
WHERE id != 1
ON CONFLICT DO NOTHING;
```

Or use the Admin API:
```bash
curl -X POST http://localhost:8443/api/v1/admin/users/1/roles/ADMIN \
  -H "Cookie: JSESSIONID=<your_session_cookie>"
```

## Testing

Test authorization by:
1. Creating users with different roles
2. Attempting to access protected endpoints without proper permissions (should return 403)
3. Verifying that authorized users can access their permitted endpoints

## Important Notes

- The UI (nav.html) remains unchanged - all options are visible to authenticated users
- Backend authorization checks prevent unauthorized access even if users manually invoke protected URIs
- Existing authentication flow is preserved - users still authenticate via `/authenticate`
- Session management continues to use Redis for Kubernetes deployment
- Passwords should be hashed with BCrypt (update existing passwords if not already hashed)
