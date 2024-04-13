package de.sventorben.keycloak.authentication.hidpd.discovery.spi;

import de.sventorben.keycloak.authentication.hidpd.PublicAPI;
import org.keycloak.provider.ProviderFactory;

@PublicAPI(unstable = true)
public interface HomeIdpDiscovererFactory extends ProviderFactory<HomeIdpDiscoverer> {
}
