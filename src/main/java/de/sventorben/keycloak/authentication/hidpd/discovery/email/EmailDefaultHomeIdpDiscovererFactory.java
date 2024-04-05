package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.Users;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscoverer;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscovererFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public final class EmailDefaultHomeIdpDiscovererFactory implements HomeIdpDiscovererFactory {

    private static final String PROVIDER_ID = "email";

    @Override
    public HomeIdpDiscoverer create(KeycloakSession keycloakSession) {
        return new EmailHomeIdpDiscoverer(new Users(keycloakSession));
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
}
