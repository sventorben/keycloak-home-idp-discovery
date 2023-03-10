package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import javax.ws.rs.core.MultivaluedMap;

import java.util.Set;

import static org.keycloak.protocol.oidc.OIDCLoginProtocol.PROMPT_PARAM;
import static org.keycloak.protocol.oidc.OIDCLoginProtocol.PROMPT_VALUE_CONSENT;
import static org.keycloak.protocol.oidc.OIDCLoginProtocol.PROMPT_VALUE_LOGIN;
import static org.keycloak.protocol.oidc.OIDCLoginProtocol.PROMPT_VALUE_SELECT_ACCOUNT;
import static org.keycloak.protocol.saml.SamlProtocol.SAML_FORCEAUTHN_REQUIREMENT;
import static org.keycloak.protocol.saml.SamlProtocol.SAML_LOGIN_REQUEST_FORCEAUTHN;

class LoginPage {

    private static final Logger LOG = Logger.getLogger(LoginPage.class);
    private static final Set<String> OIDC_PROMPT_NO_BYPASS =
        Set.of(PROMPT_VALUE_LOGIN, PROMPT_VALUE_CONSENT, PROMPT_VALUE_SELECT_ACCOUNT);

    private final AuthenticationFlowContext context;
    private final HomeIdpDiscoveryConfig config;

    LoginPage(AuthenticationFlowContext context, HomeIdpDiscoveryConfig config) {
        this.context = context;
        this.config = config;
    }

    boolean shouldByPass() {
        boolean bypassLoginPage = config.bypassLoginPage();
        if (bypassLoginPage) {
            AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
            String prompt = authenticationSession.getClientNote(PROMPT_PARAM);
            if (OIDC_PROMPT_NO_BYPASS.stream().anyMatch(it -> TokenUtil.hasPrompt(prompt, it))) {
                LOG.debugf("OIDC: Forced by prompt=%s", prompt);
                return false;
            }
            if (SAML_FORCEAUTHN_REQUIREMENT.equalsIgnoreCase(
                authenticationSession.getAuthNote(SAML_LOGIN_REQUEST_FORCEAUTHN))) {
                LOG.debugf("SAML: Forced authentication");
                return false;
            }
        }
        return bypassLoginPage;
    }
}
