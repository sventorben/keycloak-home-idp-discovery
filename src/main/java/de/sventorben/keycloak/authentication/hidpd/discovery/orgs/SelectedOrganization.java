package de.sventorben.keycloak.authentication.hidpd.discovery.orgs;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.OrganizationModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Records the organization that discovery has narrowed down to, so that it ends up in the tokens.
 * <p>
 * Keycloak copies client notes from the authentication session onto the client session, where
 * {@code OrganizationScope.ANY} reads them. Without such a note, a {@code scope=organization}
 * request made by a user who belongs to more than one organization yields no organization claim at
 * all, because Keycloak cannot tell which of the memberships applies. The native organization flow
 * asks the user to pick one; here discovery has already determined it from the email domain.
 * <p>
 * Scopes {@code organization:*} and {@code organization:<alias>} resolve their organizations
 * without consulting this note and are unaffected.
 *
 * @implNote Internal to this extension. Not part of the public API.
 */
public final class SelectedOrganization {

    private static final Logger LOG = Logger.getLogger(SelectedOrganization.class);

    private SelectedOrganization() {
    }

    public static void remember(AuthenticationFlowContext context, OrganizationModel organization) {
        if (organization == null) {
            return;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        if (authSession == null) {
            return;
        }

        LOG.tracef("Remembering organization '%s' as the one discovery resolved", organization.getId());
        authSession.setClientNote(OrganizationModel.ORGANIZATION_ATTRIBUTE, organization.getId());
    }
}
