-- Authorization tables for RBAC
-- These tables will be used with Spring Security for role-based access control

-- Add primary key constraint to existing users table if not present
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_pkey') THEN
        ALTER TABLE rbac.users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
    END IF;
END $$;

-- Create roles table
CREATE TABLE IF NOT EXISTS rbac.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create permissions table
CREATE TABLE IF NOT EXISTS rbac.permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create role_permissions join table (many-to-many)
CREATE TABLE IF NOT EXISTS rbac.role_permissions (
    role_id INTEGER NOT NULL REFERENCES rbac.roles(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES rbac.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Create user_roles join table (many-to-many)
CREATE TABLE IF NOT EXISTS rbac.user_roles (
    user_id INTEGER NOT NULL REFERENCES rbac.users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES rbac.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Insert default roles
INSERT INTO rbac.roles (name, description) VALUES
    ('ADMIN', 'Full system access'),
    ('USER', 'Standard user access'),
    ('VIEWER', 'Read-only access')
ON CONFLICT (name) DO NOTHING;

-- Insert default permissions
INSERT INTO rbac.permissions (name, description) VALUES
    ('UPLOAD_STATEMENT', 'Can upload bank statements'),
    ('MERGE_STATEMENT', 'Can merge bank statements'),
    ('REFRESH_VECTORS', 'Can refresh entity vectors (admin only)'),
    ('MANAGE_ENTITIES', 'Can manage entities'),
    ('MANAGE_TRANSACTIONS', 'Can manage transactions'),
    ('VIEW_REPORTS', 'Can view reports')
ON CONFLICT (name) DO NOTHING;

-- Assign permissions to roles
-- ADMIN gets all permissions
INSERT INTO rbac.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM rbac.roles r, rbac.permissions p WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- USER gets standard permissions (upload, merge, manage entities/transactions, view reports)
INSERT INTO rbac.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM rbac.roles r, rbac.permissions p
WHERE r.name = 'USER' AND p.name IN ('UPLOAD_STATEMENT', 'MERGE_STATEMENT', 'MANAGE_ENTITIES', 'MANAGE_TRANSACTIONS', 'VIEW_REPORTS')
ON CONFLICT DO NOTHING;

-- VIEWER gets read-only permissions
INSERT INTO rbac.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM rbac.roles r, rbac.permissions p
WHERE r.name = 'VIEWER' AND p.name IN ('VIEW_REPORTS')
ON CONFLICT DO NOTHING;
