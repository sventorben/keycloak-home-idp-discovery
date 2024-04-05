package de.sventorben.keycloak.authentication.hidpd;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.HomeIdpDiscoveryConfigProperties;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.*;

public final class HomeIdpDiscoveryAuthenticatorFactory implements AuthenticatorFactory, ServerInfoAwareProviderFactory {

    private static final Logger LOG = Logger.getLogger(HomeIdpDiscoveryAuthenticatorFactory.class);

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = new AuthenticationExecutionModel.Requirement[]{REQUIRED, ALTERNATIVE, DISABLED};

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
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their home identity provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Stream.concat(
            HomeIdpForwarderConfigProperties.CONFIG_PROPERTIES.stream(),
            HomeIdpDiscoveryConfigProperties.CONFIG_PROPERTIES.stream())
            .collect(Collectors.toList());
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new HomeIdpDiscoveryAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "dev-snapshot";
        }
        return Map.of("Version", version);
    }
}
