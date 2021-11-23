package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;

final class HomeIdpDiscoveryRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    HomeIdpDiscoveryRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        RealmModel realm = session.getContext().getRealm();
        HomeIdpDiscoveryRealmResource resource = new HomeIdpDiscoveryRealmResource(realm, session);
        ResteasyProviderFactory.getInstance().injectProperties(resource);
        resource.init();
        return resource;
    }

    @Override
    public void close() {

    }
}
