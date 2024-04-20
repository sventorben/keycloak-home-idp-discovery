package de.sventorben.keycloak.authentication.hidpd.discovery.orgs.domainhint;

import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscoverer;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.organization.OrganizationProvider;

import java.util.Collections;
import java.util.List;

final class OrgsDomainDiscoverer implements HomeIdpDiscoverer {

    private final KeycloakSession keycloakSession;

    OrgsDomainDiscoverer(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public List<IdentityProviderModel> discoverForUser(AuthenticationFlowContext context, String username) {
        String domain = username;
        OrganizationProvider orgProvider = keycloakSession.getProvider(OrganizationProvider.class);

        if (!orgProvider.isEnabled()) {
            return Collections.emptyList();
        }

        OrganizationModel org = orgProvider.getByDomainName(domain);
        if (org != null) {
            IdentityProviderModel idp = org.getIdentityProvider();
            if (idp != null) {
                return Collections.singletonList(idp);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void close() {

    }
}
