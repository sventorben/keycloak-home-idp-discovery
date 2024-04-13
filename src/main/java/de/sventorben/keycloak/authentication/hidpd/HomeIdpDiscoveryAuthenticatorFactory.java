package de.sventorben.keycloak.authentication.hidpd;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig;

public final class HomeIdpDiscoveryAuthenticatorFactory extends AbstractHomeIdpDiscoveryAuthenticatorFactory {

    private static final String PROVIDER_ID = "home-idp-discovery";

    public HomeIdpDiscoveryAuthenticatorFactory() {
        super(new EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig());
    }

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
    public String getId() {
        return PROVIDER_ID;
    }
}
