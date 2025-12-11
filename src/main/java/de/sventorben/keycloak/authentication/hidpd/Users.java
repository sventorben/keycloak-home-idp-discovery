package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.stream.Stream;

public final class Users {

    private static final Logger LOG = Logger.getLogger(Users.class);

    private final KeycloakSession keycloakSession;

    public Users(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    public UserModel lookupBy(String username) {
        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(keycloakSession, keycloakSession.getContext().getRealm(), username);
        } catch (ModelDuplicateException ex) {
            LOG.warnf(ex, "Could not uniquely identify the user. Multiple users with name or email '%s' found.", username);
        }
        return user;
    }

    public Stream<FederatedIdentityModel> getFederatedIdentitiesStream(RealmModel realm, UserModel user) {
        return keycloakSession.users().getFederatedIdentitiesStream(realm, user);
    }
}
