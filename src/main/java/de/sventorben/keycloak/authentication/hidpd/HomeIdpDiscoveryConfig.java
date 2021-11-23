package de.sventorben.keycloak.authentication.hidpd;

import org.keycloak.models.AuthenticatorConfigModel;

import java.util.Optional;

final class HomeIdpDiscoveryConfig {

    static final String FORWARD_TO_LINKED_IDP = "forwardToLinkedIdp";

    private final AuthenticatorConfigModel authenticatorConfigModel;

    HomeIdpDiscoveryConfig(AuthenticatorConfigModel authenticatorConfigModel) {
        this.authenticatorConfigModel = authenticatorConfigModel;
    }

    boolean forwardToLinkedIdp() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(FORWARD_TO_LINKED_IDP, "false")))
            .orElse(false);
    }
}
