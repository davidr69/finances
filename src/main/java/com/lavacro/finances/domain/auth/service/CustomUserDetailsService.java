package com.lavacro.finances.domain.auth.service;

import com.lavacro.finances.domain.auth.permission.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDTO userDTO = permissionService.getUserPermissions(username);

		if(userDTO.locked() != null && userDTO.locked()) {
			throw new UsernameNotFoundException("User is locked: " + username);
		}

        return User.builder()
                .username(userDTO.name())
                .password(userDTO.password())
                .authorities(getAuthorities(userDTO))
                .accountLocked(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(UserDTO user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (RoleDTO userRole : user.roles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.roleName()));

            for (PermissionDTO permission : userRole.permissions()) {
                authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission.permissionName()));
            }
        }

        return authorities;
    }
}
