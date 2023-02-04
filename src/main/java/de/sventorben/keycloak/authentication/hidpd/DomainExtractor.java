package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.util.Optional;

final class DomainExtractor {

    private static final Logger LOG = Logger.getLogger(DomainExtractor.class);
    private static final String EMAIL_ATTRIBUTE = "email";

    private HomeIdpDiscoveryConfig config;

    DomainExtractor(HomeIdpDiscoveryConfig config) {
        this.config = config;
    }

    Optional<String> extractFrom(UserModel user) {
        if (!user.isEnabled()) {
            LOG.warnf("User '%s' not enabled", user.getId());
            return Optional.empty();
        }
        String userAttribute = user.getFirstAttribute(config.userAttribute());
        if (userAttribute == null) {
            LOG.warnf("Could not find user attribute '%s' for user '%s'", config.userAttribute(), user.getId());
            return Optional.empty();
        }
        if (EMAIL_ATTRIBUTE.equalsIgnoreCase(config.userAttribute()) && !user.isEmailVerified()) {
            LOG.warnf("Email address of user '%s' is not verified", user.getId());
            return Optional.empty();
        }
        return extractFrom(userAttribute);
    }

    Optional<String> extractFrom(String usernameOrEmail) {
        String domain = null;
        if (usernameOrEmail != null) {
            int atIndex = usernameOrEmail.trim().lastIndexOf("@");
            if (atIndex >= 0) {
                String email = usernameOrEmail;
                domain = email.substring(atIndex + 1).trim();
                if (domain.length() == 0) {
                    domain = null;
                }
            }
        }
        return Optional.ofNullable(domain);
    }

}
