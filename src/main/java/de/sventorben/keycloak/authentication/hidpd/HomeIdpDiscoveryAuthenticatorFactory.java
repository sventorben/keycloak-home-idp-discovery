package de.sventorben.keycloak.authentication.hidpd;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig;
import org.keycloak.Config;

public final class HomeIdpDiscoveryAuthenticatorFactory extends AbstractHomeIdpDiscoveryAuthenticatorFactory {

    private static final String PROVIDER_ID = "home-idp-discovery";

    private Config.Scope config;

    @Override
    public String getDisplayType() {
        return "Home IdP Discovery";
    }

    @Override
    public String getReferenceCategory() {
        return "Authorization";
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their home identity provider";
    }

    @Override
    DiscovererConfig getDiscovererConfig() {
        return new EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
