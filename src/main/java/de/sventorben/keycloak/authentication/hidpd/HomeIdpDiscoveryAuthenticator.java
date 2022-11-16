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
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

final class HomeIdpDiscoveryAuthenticator extends AbstractUsernameFormAuthenticator {

    private static final Logger LOG = Logger.getLogger(HomeIdpDiscoveryAuthenticator.class);

    HomeIdpDiscoveryAuthenticator() {
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        new AuthenticationChallenge(context).challenge();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }

        String username = setUserInContext(context, formData);
        if (username == null) {
            return;
        }

        final Optional<IdentityProviderModel> homeIdp = new HomeIdpDiscoverer(context).discoverForUser(username);

        if (homeIdp.isEmpty()) {
            context.attempted();
        } else {
            new Redirector(context).redirectTo(homeIdp.get());
        }
    }

    private String setUserInContext(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        context.clearUser();

        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);

        if (username != null) {
            username = username.trim();
            if ("".equalsIgnoreCase(username))
                username = null;
        }

        if (username == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return null;
        }

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

        try {
            UserModel user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(),
                username);
            if (user != null) {
                context.setUser(user);
            }
        } catch (ModelDuplicateException ex) {
            LOG.debugf(ex, "Could not find user %s", username);
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
