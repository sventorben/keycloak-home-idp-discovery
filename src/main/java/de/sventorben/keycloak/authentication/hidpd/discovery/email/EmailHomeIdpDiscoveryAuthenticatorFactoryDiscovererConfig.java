package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public final class EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig implements AbstractHomeIdpDiscoveryAuthenticatorFactory.DiscovererConfig {
    @Override
    public List<ProviderConfigProperty> getProperties() {
        return EmailHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
    }

    @Override
    public String getProviderId() {
        return EmailHomeIdpDiscovererFactory.PROVIDER_ID;
    }

}
