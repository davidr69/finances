package com.lavacro.finances.security;

import com.lavacro.finances.entities.PermissionEntity;
import com.lavacro.finances.entities.RoleEntity;
import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.entities.UserRoleEntity;
import com.lavacro.finances.repositories.RbacUserRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final RbacUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RbacUsersEntity user = userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getLocked() != null && user.getLocked()) {
            throw new UsernameNotFoundException("User is locked: " + username);
        }

        return User.builder()
                .username(user.getName())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountLocked(user.getLocked() != null && user.getLocked())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(RbacUsersEntity user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (UserRoleEntity userRole : user.getUserRoles()) {
            RoleEntity role = userRole.getRole();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            for (PermissionEntity permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority("PERMISSION_" + permission.getName()));
            }
        }

        return authorities;
    }
}
