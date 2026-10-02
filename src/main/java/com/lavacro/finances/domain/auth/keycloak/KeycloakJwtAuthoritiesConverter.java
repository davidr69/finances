package com.lavacro.finances.domain.auth.keycloak;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Used by .oauth2ResourceServer(oauth2 -> oauth2.jwt(...)) (the REST API /
 * preemptive Bearer auth path). Spring Security has already validated the
 * JWT's signature, expiry and issuer before this converter runs; it only
 * maps the now-trusted realm_access.roles claim to authorities, via the
 * same expansion used for browser SSO logins.
 */
@Component
@RequiredArgsConstructor
public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final RealmRoleAuthorityExpander expander;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return expander.expand(expander.realmRolesFromClaims(jwt.getClaims()));
    }
}
