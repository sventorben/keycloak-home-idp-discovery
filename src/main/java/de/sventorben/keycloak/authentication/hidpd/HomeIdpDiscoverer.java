package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
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
        Optional<IdentityProviderModel> homeIdp = Optional.empty();

        final Optional<String> emailDomain;
        UserModel user = context.getUser();
        if (user == null) {
            emailDomain = domainExtractor.extractFrom(username);
        } else {
            emailDomain = domainExtractor.extractFrom(user);
        }

        if (emailDomain.isPresent()) {
            String domain = emailDomain.get();
            homeIdp = discoverHomeIdp(domain, user, username);
            if (homeIdp.isEmpty()) {
                LOG.tracef("Could not find home IdP for domain %s and user %s", domain, username);
            }
        } else {
            LOG.warnf("Could not extract domain from email address %s", username);
        }

        return homeIdp;
    }

    private Optional<IdentityProviderModel> discoverHomeIdp(String domain, UserModel user, String username) {
        final Map<String, String> linkedIdps;

        HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());
        if (user == null || !config.forwardToLinkedIdp()) {
            linkedIdps = Collections.emptyMap();
        } else {
            linkedIdps = context.getSession().users()
                .getFederatedIdentitiesStream(context.getRealm(), user)
                .collect(
                    Collectors.toMap(FederatedIdentityModel::getIdentityProvider, FederatedIdentityModel::getUserName));
        }

        // enabled IdPs with domain
        List<IdentityProviderModel> idpsWithDomain = context.getRealm().getIdentityProvidersStream()
            .filter(IdentityProviderModel::isEnabled)
            .filter(it -> new IdentityProviderModelConfig(it).hasDomain(config.userAttribute(), domain))
            .collect(Collectors.toList());

        // Linked IdPs with matching domain
        Optional<IdentityProviderModel> homeIdp = idpsWithDomain.stream()
            .filter(it -> linkedIdps.containsKey(it.getAlias()))
            .findFirst();

        // linked and enabled IdPs
        if (homeIdp.isEmpty() && !linkedIdps.isEmpty()) {
            homeIdp = context.getRealm().getIdentityProvidersStream()
                .filter(IdentityProviderModel::isEnabled)
                .filter(it -> linkedIdps.containsKey(it.getAlias()))
                .findFirst();
        }

        // Matching domain
        if (homeIdp.isEmpty()) {
            homeIdp = idpsWithDomain.stream().findFirst();
        }

        homeIdp.ifPresent(it -> {
            if (linkedIdps.containsKey(it.getAlias()) && config.forwardToLinkedIdp()) {
                String idpUsername = linkedIdps.get(it.getAlias());
                context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, idpUsername);
            } else {
                context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);
            }
        });

        return homeIdp;
    }

}
