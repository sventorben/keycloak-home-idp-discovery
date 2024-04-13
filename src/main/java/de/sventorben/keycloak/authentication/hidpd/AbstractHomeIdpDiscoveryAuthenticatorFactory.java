package de.sventorben.keycloak.authentication.hidpd;

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

public abstract class AbstractHomeIdpDiscoveryAuthenticatorFactory implements AuthenticatorFactory, ServerInfoAwareProviderFactory {
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = new AuthenticationExecutionModel.Requirement[]{REQUIRED, ALTERNATIVE, DISABLED};

    private final DiscovererConfig discovererConfig;

    protected AbstractHomeIdpDiscoveryAuthenticatorFactory(DiscovererConfig discovererConfig) {
        this.discovererConfig = discovererConfig;
    }

    @Override
    public final boolean isConfigurable() {
        return true;
    }

    @Override
    public final AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public final boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public final List<ProviderConfigProperty> getConfigProperties() {
        return Stream.concat(
            HomeIdpForwarderConfigProperties.CONFIG_PROPERTIES.stream(),
            discovererConfig.getProperties().stream())
            .collect(Collectors.toList());
    }

    @Override
    public final Authenticator create(KeycloakSession session) {
        return new HomeIdpDiscoveryAuthenticator(discovererConfig);
    }

    @Override
    public final void init(Config.Scope config) {
    }

    @Override
    public final void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public final void close() {
    }

    @Override
    public final Map<String, String> getOperationalInfo() {
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "dev-snapshot";
        }
        return Map.of("Version", version);
    }

    public interface DiscovererConfig {
        List<ProviderConfigProperty> getProperties();

        String getProviderId();
    }
}
