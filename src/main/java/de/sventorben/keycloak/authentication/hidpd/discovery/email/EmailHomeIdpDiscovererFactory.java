package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.OperationalInfo;
import de.sventorben.keycloak.authentication.hidpd.Users;
import de.sventorben.keycloak.authentication.hidpd.discovery.HomeIdpDiscovererConfig;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscoverer;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscovererFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.Map;

public final class EmailHomeIdpDiscovererFactory implements HomeIdpDiscovererFactory, ServerInfoAwareProviderFactory {

    static final String PROVIDER_ID = "email";

    @Override
    public HomeIdpDiscoverer<? extends HomeIdpDiscovererConfig> create(KeycloakSession keycloakSession) {
        return new EmailHomeIdpDiscoverer(new Users(keycloakSession), new DefaultIdentityProviders());
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

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
        return OperationalInfo.get();
    }
}
