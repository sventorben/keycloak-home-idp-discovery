package de.sventorben.keycloak.authentication.hidpd.discovery.orgs.email;

import de.sventorben.keycloak.authentication.hidpd.discovery.HomeIdpDiscovererConfig;
import de.sventorben.keycloak.authentication.hidpd.discovery.email.Domain;
import de.sventorben.keycloak.authentication.hidpd.discovery.email.IdentityProviders;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.*;
import org.keycloak.organization.OrganizationProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OrgsIdentityProviders implements IdentityProviders {

    private final KeycloakSession keycloakSession;

    OrgsIdentityProviders(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public List<IdentityProviderModel> candidatesForHomeIdp(RealmModel realm, UserModel user) {
        OrganizationProvider orgProvider = keycloakSession.getProvider(OrganizationProvider.class);
        if (user == null) {
            return Collections.emptyList();
        }
        if (orgProvider.isEnabled()) {
            Stream<OrganizationModel> orgs = orgProvider.getByMember(user);
            if (orgs != null) {
                return orgs
                    .filter(OrganizationModel::isEnabled)
                    .flatMap(OrganizationModel::getIdentityProviders)
                    .filter(IdentityProviderModel::isEnabled)
                    .collect(Collectors.toList());
            }
        } else {
            // TODO: Log a warning
        }
        return Collections.emptyList();
    }

    @Override
    public List<IdentityProviderModel> withMatchingDomain(HomeIdpDiscovererConfig discovererConfig, List<IdentityProviderModel> candidates, Domain domain) {
        OrganizationProvider orgProvider = keycloakSession.getProvider(OrganizationProvider.class);
        if (orgProvider.isEnabled()) {
            OrganizationModel org = orgProvider.getByDomainName(domain.getRawValue());
            if (org != null && org.isEnabled()) {
                boolean verified = org.getDomains()
                    .filter(it -> domain.getRawValue().equalsIgnoreCase(it.getName()))
                    .anyMatch(OrganizationDomainModel::isVerified);
                if (verified) {
                    return org.getIdentityProviders()
                        .filter(IdentityProviderModel::isEnabled)
                        // TODO: Filter based on domain - should only be one
                        .collect(Collectors.toList());
                }
            }
        } else {
            // TODO: Log a warning
        }
        return Collections.emptyList();
    }

}
