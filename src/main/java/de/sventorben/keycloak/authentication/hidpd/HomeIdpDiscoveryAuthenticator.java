package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.keycloak.protocol.oidc.OIDCLoginProtocol.*;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

final class HomeIdpDiscoveryAuthenticator extends AbstractUsernameFormAuthenticator {

    private static final Logger LOG = Logger.getLogger(HomeIdpDiscoveryAuthenticator.class);

    HomeIdpDiscoveryAuthenticator() {
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());

        if (new LoginPage(context, config).shouldByPass()) {
            String loginHint = context.getAuthenticationSession().getClientNote(LOGIN_HINT_PARAM);
            String username = setUserInContext(context, loginHint);
            final Optional<IdentityProviderModel> homeIdp = new HomeIdpDiscoverer(context).discoverForUser(username);
            if (homeIdp.isPresent()) {
                new Redirector(context).redirectTo(homeIdp.get());
                return;
            }
        }
        new AuthenticationChallenge(context).challenge();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            LOG.debugf("Login canceled");
            context.cancelLogin();
            return;
        }

        String username = setUserInContext(context, formData.getFirst(AuthenticationManager.FORM_USERNAME));
        if (username == null) {
            LOG.debugf("No username in request");
            return;
        }

        final Optional<IdentityProviderModel> homeIdp = new HomeIdpDiscoverer(context).discoverForUser(username);

        if (homeIdp.isEmpty()) {
            context.attempted();
        } else {
            new Redirector(context).redirectTo(homeIdp.get());
        }
    }

    private String setUserInContext(AuthenticationFlowContext context, String username) {
        context.clearUser();

        username = trimToNull(username);

        if (username == null) {
            LOG.warn("No or empty username found in request");
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return null;
        }

        LOG.debugf("Found username '%s' in request", username);
        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
        context.getAuthenticationSession().setClientNote(LOGIN_HINT_PARAM, username);

        try {
            UserModel user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(),
                username);
            if (user != null) {
                LOG.tracef("Setting user '%s' in context", user.getId());
                context.setUser(user);
            }
        } catch (ModelDuplicateException ex) {
            LOG.warnf(ex, "Could not uniquely identify the user. Multiple users with name or email '%s' found.", username);
        }

        return username;
    }

    private String trimToNull(String username) {
        if (username != null) {
            username = username.trim();
            if ("".equalsIgnoreCase(username))
                username = null;
        }
        return username;
    }

    @Override
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsername();
    }

    @Override
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        return context.getRealm().isLoginWithEmailAllowed() ? "invalidUsernameOrEmailMessage" : "invalidUsernameMessage";
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

}
