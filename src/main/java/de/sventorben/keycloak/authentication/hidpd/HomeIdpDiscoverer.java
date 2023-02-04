package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class HomeIdpDiscoverer {

    private static final Logger LOG = Logger.getLogger(HomeIdpDiscoverer.class);

    private final DomainExtractor domainExtractor;
    private final AuthenticationFlowContext context;

    HomeIdpDiscoverer(AuthenticationFlowContext context) {
        this(new DomainExtractor(new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig())), context);
    }

    HomeIdpDiscoverer(DomainExtractor domainExtractor, AuthenticationFlowContext context) {
        this.domainExtractor = domainExtractor;
        this.context = context;
    }

    public Optional<IdentityProviderModel> discoverForUser(String username) {

        String realmName = context.getRealm().getName();
        LOG.tracef(
            "Trying to discover home IdP for username '%s' in realm '%s' with authenticator config '%s'",
            username, realmName, context.getAuthenticatorConfig().getAlias());

        Optional<IdentityProviderModel> homeIdp = Optional.empty();

        final Optional<String> emailDomain;
        UserModel user = context.getUser();
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
            String domain = emailDomain.get();
            homeIdp = discoverHomeIdp(domain, user, username);
            if (homeIdp.isEmpty()) {
                LOG.infof("Could not find home IdP for domain '%s' and user '%s' in realm '%s'", domain, username, realmName);
            }
        } else {
            LOG.warnf("Could not extract domain from email address '%s'", username);
        }

        return homeIdp;
    }

    private Optional<IdentityProviderModel> discoverHomeIdp(String domain, UserModel user, String username) {
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

        List<IdentityProviderModel> enabledIdps = determineEnabledIdps();
        List<IdentityProviderModel> enabledIdpsWithMatchingDomain = filterIdpsWithMatchingDomainFrom(enabledIdps,
            domain,
            config);

        // Prefer linked IdP with matching domain first
        Optional<IdentityProviderModel> homeIdp = getLinkedIdpFrom(enabledIdpsWithMatchingDomain, linkedIdps);

        if (homeIdp.isEmpty()) {
            if (!linkedIdps.isEmpty()) {
                // Prefer linked and enabled IdPs without matching domain in favor of not linked IdPs with matching domain
                homeIdp = getLinkedIdpFrom(enabledIdps, linkedIdps);
            }
            if (homeIdp.isEmpty()) {
                // Fallback to not linked IdP with matching domain (general case if user logs in for the first time)
                homeIdp = enabledIdpsWithMatchingDomain.stream().findFirst();
                homeIdp.ifPresent(idp -> LOG.tracef(
                    "Found non-linked IdP '%s' with matching domain '%s' for user '%s'",
                    idp.getAlias(), domain, username));
            } else {
                LOG.tracef("Found linked IdP '%s' without matching domain '%s' for user '%s'", homeIdp.get().getAlias(),
                    domain, username);
            }
        } else {
            LOG.tracef("Found linked IdP '%s' with matching domain '%s' for user '%s'", homeIdp.get().getAlias(),
                domain, username);
        }

        homeIdp.ifPresent(idp -> {
            String loginHint = linkedIdps.getOrDefault(idp.getAlias(), username);
            context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
        });

        return homeIdp;
    }

    private Optional<IdentityProviderModel> getLinkedIdpFrom(List<IdentityProviderModel> enabledIdpsWithMatchingDomain, Map<String, String> linkedIdps) {
        return enabledIdpsWithMatchingDomain.stream()
            .filter(it -> linkedIdps.containsKey(it.getAlias()))
            .findFirst();
    }

    private List<IdentityProviderModel> filterIdpsWithMatchingDomainFrom(List<IdentityProviderModel> enabledIdps, String domain, HomeIdpDiscoveryConfig config) {
        String userAttributeName = config.userAttribute();
        List<IdentityProviderModel> idpsWithMatchingDomain = enabledIdps.stream()
            .filter(it -> new IdentityProviderModelConfig(it).hasDomain(userAttributeName, domain))
            .collect(Collectors.toList());
        LOG.tracef("IdPs with matching domain '%s' for attribute '%s': %s", domain, userAttributeName,
            idpsWithMatchingDomain.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return idpsWithMatchingDomain;
    }

    private List<IdentityProviderModel> determineEnabledIdps() {
        RealmModel realm = context.getRealm();
        List<IdentityProviderModel> enabledIdps = realm.getIdentityProvidersStream()
            .filter(IdentityProviderModel::isEnabled)
            .collect(Collectors.toList());
        LOG.tracef("Enabled IdPs in realm '%s': %s",
            realm.getName(),
            enabledIdps.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return enabledIdps;
    }

}
