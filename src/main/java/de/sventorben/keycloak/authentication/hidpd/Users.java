package de.sventorben.keycloak.authentication.hidpd;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

final class Users {

    private static final Logger LOG = Logger.getLogger(Users.class);

    private final AuthenticationFlowContext context;

    Users(AuthenticationFlowContext context) {
        this.context = context;
    }

    UserModel lookupBy(String username) {
        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException ex) {
            LOG.warnf(ex, "Could not uniquely identify the user. Multiple users with name or email '%s' found.", username);
        }
        return user;
    }

}
