package de.sventorben.keycloak.authentication.hidpd;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

import static de.sventorben.keycloak.authentication.hidpd.HomeIdpDiscoveryConfig.FORWARD_TO_LINKED_IDP;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

final class HomeIdpDiscoveryConfigProperties {

    private static final ProviderConfigProperty FORWARD_TO_LINKED_IDP_PROPERTY = new ProviderConfigProperty(
        FORWARD_TO_LINKED_IDP,
        "Forward to linked IdP",
        "Whether to forward existing user to a linked identity provider or not.",
        BOOLEAN_TYPE,
        false,
        false);


    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property(FORWARD_TO_LINKED_IDP_PROPERTY)
        .build();

}
