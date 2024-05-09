package de.sventorben.keycloak.authentication.hidpd.discovery.orgs.email;

import de.sventorben.keycloak.authentication.hidpd.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public final class OrgsEmailHomeIdpDiscoveryAuthenticatorFactory extends AbstractHomeIdpDiscoveryAuthenticatorFactory implements EnvironmentDependentProviderFactory {

    private static final String PROVIDER_ID = "orgs-email";

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION);
    }

    public OrgsEmailHomeIdpDiscoveryAuthenticatorFactory() {
        super(new DiscovererConfig() {
            @Override
            public List<ProviderConfigProperty> getProperties() {
                return OrgsEmailHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
            }

            @Override
            public String getProviderId() {
                return PROVIDER_ID;
            }
        });
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Home IdP Discovery - Organization via Email";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their organization's identity provider which will be discovered based on the domain of the user's email address.";
    }

}
