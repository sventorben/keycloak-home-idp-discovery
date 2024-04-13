package de.sventorben.keycloak.authentication.hidpd.discovery.spi;

import de.sventorben.keycloak.authentication.hidpd.PublicAPI;
import org.keycloak.provider.ProviderFactory;

/**
 * @apiNote This interface is part of the public API, but is currently unstable and may change in future releases.
 */
@PublicAPI(unstable = true)
public interface HomeIdpDiscovererFactory extends ProviderFactory<HomeIdpDiscoverer> {
}
