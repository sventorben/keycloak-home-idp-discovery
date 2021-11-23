package de.sventorben.keycloak.authentication.hidpd;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public final class HomeIdpDiscoveryRealmResourceProviderFactory implements RealmResourceProviderFactory {

    private static final String PROVIDER_ID = "home-idp-discovery";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new HomeIdpDiscoveryRealmResourceProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

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
}
