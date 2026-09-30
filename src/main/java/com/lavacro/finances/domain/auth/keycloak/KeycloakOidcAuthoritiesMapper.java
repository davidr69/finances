package com.lavacro.finances.domain.auth.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Used by .oauth2Login(...) (the browser SSO / redirect flow). Extracts
 * realm_access.roles from the ID token's claims and expands them via
 * RealmRoleAuthorityExpander.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakOidcAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private final RealmRoleAuthorityExpander expander;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {

		log.info("******************** mapAuthorities");
        Set<GrantedAuthority> mapped = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
			log.info("******************** authority: {}", authority.getAuthority());
            Map<String, Object> claims = extractClaims(authority);
            if (claims != null) {
                mapped.addAll(expander.expand(expander.realmRolesFromClaims(claims)));
            }
        }
        return mapped;
    }

    private Map<String, Object> extractClaims(GrantedAuthority authority) {
		log.info("******************** extractClaims");
        if (authority instanceof OidcUserAuthority oidcAuthority) {
			log.info("******************** oidcAuthority");
            return oidcAuthority.getIdToken().getClaims();
        }
        if (authority instanceof OAuth2UserAuthority oauth2Authority) {
			log.info("******************** oauth2Authority");
            return oauth2Authority.getAttributes();
        }
        return null;
    }
}
