package de.sventorben.keycloak.authentication.hidpd;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticatorUtil;

final class SingleSignOn {

    private final AuthenticationFlowContext context;

    SingleSignOn(AuthenticationFlowContext context) {
        this.context = context;
    }

    /**
     * Whether the cookie authenticator has already authenticated the user for this request.
     * <p>
     * Keycloak only sets the {@code SSO_AUTH} note on the fully successful cookie-only path, right
     * after attaching the user session. Forced re-authentication, step-up and forked flows all take
     * different branches that leave the note unset, so the note is a reliable signal that nothing
     * further is required from the user.
     */
    boolean established() {
        return context.getUser() != null
            && AuthenticatorUtil.isSSOAuthentication(context.getAuthenticationSession());
    }
}
