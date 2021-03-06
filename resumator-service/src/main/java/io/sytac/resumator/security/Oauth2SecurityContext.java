package io.sytac.resumator.security;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Optional;

/**
 * Security context for an Oauth2 scheme
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
class Oauth2SecurityContext implements SecurityContext {

    private final Identity principal;

    public Oauth2SecurityContext(final Optional<Identity> maybeUser) {
        this.principal = maybeUser.orElse(Identity.ANONYMOUS);
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return principal.hasRole(role);
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
