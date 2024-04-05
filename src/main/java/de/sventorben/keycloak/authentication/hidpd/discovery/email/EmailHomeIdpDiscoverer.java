package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.Users;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscoverer;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.*;

import java.util.*;
import java.util.stream.Collectors;

final class EmailHomeIdpDiscoverer implements HomeIdpDiscoverer {

    private static final Logger LOG = Logger.getLogger(EmailHomeIdpDiscoverer.class);
    private final Users users;

    EmailHomeIdpDiscoverer(Users users) {
        this.users = users;
    }

    @Override
    public List<IdentityProviderModel> discoverForUser(AuthenticationFlowContext context, String username) {

        DomainExtractor domainExtractor = new DomainExtractor(new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig()));

        String realmName = context.getRealm().getName();
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        LOG.tracef("Trying to discover home IdP for username '%s' in realm '%s' with authenticator config '%s'",
            username, realmName, authenticatorConfig == null ? "<unconfigured>" : authenticatorConfig.getAlias());

        List<IdentityProviderModel> homeIdps = new ArrayList<>();

        final Optional<Domain> emailDomain;
        UserModel user = users.lookupBy(username);
        if (user == null) {
            LOG.tracef("No user found in AuthenticationFlowContext. Extracting domain from provided username '%s'.",
                username);
            emailDomain = domainExtractor.extractFrom(username);
        } else {
            LOG.tracef("User found in AuthenticationFlowContext. Extracting domain from stored user '%s'.",
                user.getId());
            emailDomain = domainExtractor.extractFrom(user);
        }

        if (emailDomain.isPresent()) {
            Domain domain = emailDomain.get();
            homeIdps = discoverHomeIdps(context, domain, user, username);
            if (homeIdps.isEmpty()) {
                LOG.infof("Could not find home IdP for domain '%s' and user '%s' in realm '%s'",
                    domain, username, realmName);
            }
        } else {
            LOG.warnf("Could not extract domain from email address '%s'", username);
        }

        return homeIdps;
    }

    private List<IdentityProviderModel> discoverHomeIdps(AuthenticationFlowContext context, Domain domain, UserModel user, String username) {
        final Map<String, String> linkedIdps;

        HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());
        if (user == null || !config.forwardToLinkedIdp()) {
            linkedIdps = Collections.emptyMap();
            LOG.tracef(
                "User '%s' is not stored locally or forwarding to linked IdP is disabled. Skipping discovery of linked IdPs.",
                username);
        } else {
            LOG.tracef(
                "Found local user '%s' and forwarding to linked IdP is enabled. Discovering linked IdPs.",
                username);
            linkedIdps = context.getSession().users()
                .getFederatedIdentitiesStream(context.getRealm(), user)
                .collect(
                    Collectors.toMap(FederatedIdentityModel::getIdentityProvider, FederatedIdentityModel::getUserName));
        }

        List<IdentityProviderModel> enabledIdps = determineEnabledIdps(context);
        List<IdentityProviderModel> enabledIdpsWithMatchingDomain = filterIdpsWithMatchingDomainFrom(enabledIdps,
            domain,
            config);

        // Prefer linked IdP with matching domain first
        List<IdentityProviderModel> homeIdps = getLinkedIdpsFrom(enabledIdpsWithMatchingDomain, linkedIdps);

        if (homeIdps.isEmpty()) {
            if (!linkedIdps.isEmpty()) {
                // Prefer linked and enabled IdPs without matching domain in favor of not linked IdPs with matching domain
                homeIdps = getLinkedIdpsFrom(enabledIdps, linkedIdps);
            }
            if (homeIdps.isEmpty()) {
                // Fallback to not linked IdPs with matching domain (general case if user logs in for the first time)
                homeIdps = enabledIdpsWithMatchingDomain;
                logFoundIdps("non-linked", "matching", homeIdps, domain, username);
            } else {
                logFoundIdps("non-linked", "non-matching", homeIdps, domain, username);
            }
        } else {
            logFoundIdps("linked", "matching", homeIdps, domain, username);
        }

        return homeIdps;
    }

    private void logFoundIdps(String idpQualifier, String domainQualifier, List<IdentityProviderModel> homeIdps, Domain domain, String username) {
        String homeIdpsString = homeIdps.stream()
            .map(IdentityProviderModel::getAlias)
            .collect(Collectors.joining(","));
        LOG.tracef("Found %s IdPs [%s] with %s domain '%s' for user '%s'",
            idpQualifier, homeIdpsString, domainQualifier, domain, username);
    }

    private List<IdentityProviderModel> getLinkedIdpsFrom(List<IdentityProviderModel> enabledIdpsWithMatchingDomain, Map<String, String> linkedIdps) {
        return enabledIdpsWithMatchingDomain.stream()
            .filter(it -> linkedIdps.containsKey(it.getAlias()))
            .collect(Collectors.toList());
    }

    private List<IdentityProviderModel> filterIdpsWithMatchingDomainFrom(List<IdentityProviderModel> enabledIdps, Domain domain, HomeIdpDiscoveryConfig config) {
        String userAttributeName = config.userAttribute();
        List<IdentityProviderModel> idpsWithMatchingDomain = enabledIdps.stream()
            .filter(it -> new IdentityProviderModelConfig(it).supportsDomain(userAttributeName, domain))
            .collect(Collectors.toList());
        LOG.tracef("IdPs with matching domain '%s' for attribute '%s': %s", domain, userAttributeName,
            idpsWithMatchingDomain.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return idpsWithMatchingDomain;
    }

    private List<IdentityProviderModel> determineEnabledIdps(AuthenticationFlowContext context) {
        RealmModel realm = context.getRealm();
        List<IdentityProviderModel> enabledIdps = realm.getIdentityProvidersStream()
            .filter(IdentityProviderModel::isEnabled)
            .collect(Collectors.toList());
        LOG.tracef("Enabled IdPs in realm '%s': %s",
            realm.getName(),
            enabledIdps.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return enabledIdps;
    }

    @Override
    public void close() {
    }
}
