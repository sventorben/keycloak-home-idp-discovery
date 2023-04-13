package de.sventorben.keycloak.authentication.hidpd;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

import static de.sventorben.keycloak.authentication.hidpd.HomeIdpDiscoveryConfig.BYPASS_LOGIN_PAGE;
import static de.sventorben.keycloak.authentication.hidpd.HomeIdpDiscoveryConfig.FORWARD_TO_LINKED_IDP;
import static de.sventorben.keycloak.authentication.hidpd.HomeIdpDiscoveryConfig.FORWARD_TO_FIRST_MATCH;
import static de.sventorben.keycloak.authentication.hidpd.HomeIdpDiscoveryConfig.USER_ATTRIBUTE;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

final class HomeIdpDiscoveryConfigProperties {

    private static final ProviderConfigProperty FORWARD_TO_LINKED_IDP_PROPERTY = new ProviderConfigProperty(
        FORWARD_TO_LINKED_IDP,
        "Forward to linked IdP",
        "Whether to forward existing user to a linked identity provider or not.",
        BOOLEAN_TYPE,
        false,
        false);

    private static final ProviderConfigProperty BYPASS_LOGIN_PAGE_PROPERTY = new ProviderConfigProperty(
        BYPASS_LOGIN_PAGE,
        "Bypass login page",
        "If OIDC login_hint parameter is present, whether to bypass the login page for managed domains or not.",
        BOOLEAN_TYPE,
        false,
        false);

    private static final ProviderConfigProperty FORWARD_TO_FIRST_MATCH_PROPERTY = new ProviderConfigProperty(
        FORWARD_TO_FIRST_MATCH,
        "Forward to first matched IdP",
        "When multiple IdPs match the domain, whether to forward to the first IdP found or let the user choose.",
        BOOLEAN_TYPE,
        true,
        false);

    private static final ProviderConfigProperty USER_ATTRIBUTE_PROPERTY = new ProviderConfigProperty(
        USER_ATTRIBUTE,
        "User attribute",
        "The user attribute used to lookup the email address of the user.",
        STRING_TYPE,
        "email",
        false);

    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property(USER_ATTRIBUTE_PROPERTY)
        .property(BYPASS_LOGIN_PAGE_PROPERTY)
        .property(FORWARD_TO_LINKED_IDP_PROPERTY)
        .property(FORWARD_TO_FIRST_MATCH_PROPERTY)
        .build();

}
