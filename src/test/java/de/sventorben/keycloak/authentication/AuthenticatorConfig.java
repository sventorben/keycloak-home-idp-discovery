package de.sventorben.keycloak.authentication;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Consumer;

final class AuthenticatorConfig {

    private static final String REALM_TEST = "test-realm";
    private final String authServerUrl;
    private final String user;
    private final String password;

    AuthenticatorConfig(String authServerUrl, String user, String password) {
        this.authServerUrl = authServerUrl;
        this.user = user;
        this.password = password;
    }

    void setUserAttribute(String userAttribute) {
        updateProperty("userAttribute", userAttribute);
    }

    void enableForwarding() {
        setForwarding(true);
    }

    void disableForwarding() {
        setForwarding(false);
    }

    private void setForwarding(Boolean enabled) {
        updateProperty("forwardToLinkedIdp",enabled);
    }

    void enableBypassLoginPage() {
        setBypassLoginPage(true);
    }

    void disableBypassLoginPage() {
        setBypassLoginPage(false);
    }

    private void setBypassLoginPage(Boolean enabled) {
        updateProperty("bypassLoginPage",enabled);
    }

    void enableForwardToFirstMatch() {
        setForwardToFirstMatch(true);
    }

    void disableForwardToFirstMatch() {
        setForwardToFirstMatch(false);
    }

    private void setForwardToFirstMatch(Boolean enabled) {
        updateProperty("forwardToFirstMatch", enabled);
    }

    void resetAuthenticatorConfig() {
        disableForwarding();
        disableBypassLoginPage();
        setUserAttribute("email");
        enableForwardToFirstMatch();
    }

    private void updateProperty(String propertyName, Boolean enabled) {
        updateProperty(propertyName, enabled.toString());
    }

    private void updateProperty(String propertyName, String value) {
        updateAuthenticatorConfig(authenticatorConfig -> {
            Map<String, String> config = authenticatorConfig.getConfig();
            config.put(propertyName, value);
            authenticatorConfig.setConfig(config);
        });
    }

    private void updateAuthenticatorConfig(Consumer<AuthenticatorConfigRepresentation> configurer) {
        try (Keycloak admin = getKeycloakAdminClient()) {
            AuthenticationManagementResource flows = admin.realm(REALM_TEST).flows();
            AuthenticationExecutionInfoRepresentation execution = flows
                .getExecutions("discover home idp").stream()
                .filter(it -> it.getProviderId().equalsIgnoreCase("home-idp-discovery"))
                .findFirst()
                .get();
            String authenticationConfigId = execution.getAuthenticationConfig();
            AuthenticatorConfigRepresentation authenticatorConfig;
            String authenticatorConfigAlias = "home-idp-discovery-flow-config";
            if (authenticationConfigId == null) {
                authenticatorConfig = new AuthenticatorConfigRepresentation();
                authenticatorConfig.setAlias(authenticatorConfigAlias);
                Response response = flows.newExecutionConfig(execution.getId(), authenticatorConfig);
                String location = response.getHeaderString("Location");
                authenticationConfigId = location.substring(location.lastIndexOf("/") + 1);
            } else {
                authenticatorConfig = flows.getAuthenticatorConfig(authenticationConfigId);
            }
            configurer.accept(authenticatorConfig);
            flows.updateAuthenticatorConfig(authenticationConfigId, authenticatorConfig);
        }
    }

    private Keycloak getKeycloakAdminClient() {
        return Keycloak.getInstance(authServerUrl, "master", user, password, "admin-cli");
    }

}
