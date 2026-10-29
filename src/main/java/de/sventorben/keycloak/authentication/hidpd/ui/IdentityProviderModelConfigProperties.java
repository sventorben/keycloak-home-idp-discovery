package de.sventorben.keycloak.authentication.hidpd.ui;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.MULTIVALUED_STRING_TYPE;

final class IdentityProviderModelConfigProperties {

    private static final String KEY_DOMAINS = "domains";
    private static final String KEY_MATCH_SUBDOMAINS = "matchSubDomains";

    private static final ProviderConfigProperty DOMAINS_PROPERTY = new ProviderConfigProperty(
        KEY_DOMAINS,
        "Domains",
        "List of domains supported by this identity provider",
        MULTIVALUED_STRING_TYPE,
        null,
        false);

    private static final ProviderConfigProperty MATCH_SUBDOMAINS_PROPERTY = new ProviderConfigProperty(
        KEY_MATCH_SUBDOMAINS,
        "Match sub-domains",
        "Whether sub-domain matching should be enabled or not",
        BOOLEAN_TYPE,
        "false",
        false);

    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property(DOMAINS_PROPERTY)
        .property(MATCH_SUBDOMAINS_PROPERTY)
        .build();

}
