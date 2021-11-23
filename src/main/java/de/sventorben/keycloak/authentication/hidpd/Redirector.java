package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.keycloak.services.resources.IdentityBrokerService.getIdentityProviderFactory;

final class Redirector {

    private static final Logger LOG = Logger.getLogger(Redirector.class);

    private static final String PROMPT_NONE = "none";
    private static final String ACCEPTS_PROMPT_NONE = "acceptsPromptNoneForwardFromClient";

    private final AuthenticationFlowContext context;

    Redirector(AuthenticationFlowContext context) {
        this.context = context;
    }

    void redirectTo(IdentityProviderModel idp) {
        String providerAlias = idp.getAlias();
        RealmModel realm = context.getRealm();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        KeycloakSession keycloakSession = context.getSession();
        ClientSessionCode<AuthenticationSessionModel> clientSessionCode =
            new ClientSessionCode<>(keycloakSession, realm, authenticationSession);
        clientSessionCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        if (idp.isLinkOnly()) {
            LOG.warnf("Identity Provider %s is not allowed to perform a login.", providerAlias);
            return;
        }
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        if (clientSessionCode.getClientSession() != null && loginHint != null) {
            clientSessionCode.getClientSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
        }
        IdentityProviderFactory providerFactory = getIdentityProviderFactory(keycloakSession, idp);
        IdentityProvider identityProvider = providerFactory.create(keycloakSession, idp);

        Response response = identityProvider.performLogin(createAuthenticationRequest(providerAlias, clientSessionCode));
        context.forceChallenge(response);
    }

    private AuthenticationRequest createAuthenticationRequest(String providerId, ClientSessionCode<AuthenticationSessionModel> clientSessionCode) {
        AuthenticationSessionModel authSession = null;
        IdentityBrokerState encodedState = null;

        if (clientSessionCode != null) {
            authSession = clientSessionCode.getClientSession();
            String relayState = clientSessionCode.getOrGenerateCode();
            encodedState = IdentityBrokerState.decoded(relayState, authSession.getClient().getClientId(), authSession.getTabId());
        }

        KeycloakSession keycloakSession = context.getSession();
        KeycloakUriInfo keycloakUriInfo = keycloakSession.getContext().getUri();
        RealmModel realm = context.getRealm();
        String redirectUri = Urls.identityProviderAuthnResponse(keycloakUriInfo.getBaseUri(), providerId, realm.getName()).toString();
        return new AuthenticationRequest(keycloakSession, realm, authSession, context.getHttpRequest(), keycloakUriInfo, encodedState, redirectUri);
    }

    /*
    void redirectTo(IdentityProviderModel idp) {
        URI baseUri = context.getUriInfo().getBaseUri();
        String providerId = idp.getAlias();
        String realm = context.getRealm().getName();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String accessCode = new ClientSessionCode<>(context.getSession(), context.getRealm(), authenticationSession)
            .getOrGenerateCode();
        String clientId = authenticationSession.getClient().getClientId();
        String tabId = authenticationSession.getTabId();
        URI location = Urls.identityProviderAuthnRequest(baseUri, providerId, realm, accessCode, clientId, tabId);
        UriBuilder redirectUriBuilder = UriBuilder.fromUri(location);
        if (authenticationSession.getClientNote(OAuth2Constants.DISPLAY) != null) {
            redirectUriBuilder = redirectUriBuilder
                .queryParam(OAuth2Constants.DISPLAY, authenticationSession.getClientNote(OAuth2Constants.DISPLAY));
        }
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        if (idp.isLoginHint() && loginHint != null) {
            redirectUriBuilder = redirectUriBuilder
                .queryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
        }
        String uiLocales = context.getHttpRequest().getUri().getQueryParameters().getFirst(OAuth2Constants.UI_LOCALES_PARAM);
        if (uiLocales != null) {
            uiLocales = uiLocales.trim();
            if (uiLocales.length() > 0) {
                redirectUriBuilder.queryParam(OAuth2Constants.UI_LOCALES_PARAM, uiLocales);
            }
        }
        Response response = Response.seeOther(redirectUriBuilder.build()).build();
        if (PROMPT_NONE.equals(authenticationSession.getClientNote(OIDCLoginProtocol.PROMPT_PARAM)) &&
            Boolean.parseBoolean(idp.getConfig().get(ACCEPTS_PROMPT_NONE))) {
            authenticationSession.setAuthNote(AuthenticationProcessor.FORWARDED_PASSIVE_LOGIN, "true");
        }
        LOG.debugf("Redirecting user %s of reealm %s to IdP %s", context.getUser().getUsername(), realm, providerId);
        context.forceChallenge(response);
    }
    */
}
