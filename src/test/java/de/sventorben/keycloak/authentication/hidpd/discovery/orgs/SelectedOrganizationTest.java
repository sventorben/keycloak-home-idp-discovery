package de.sventorben.keycloak.authentication.hidpd.discovery.orgs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.OrganizationModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SelectedOrganizationTest {

    @Mock
    AuthenticationFlowContext context;

    @Mock
    AuthenticationSessionModel authSession;

    @Mock
    OrganizationModel organization;

    @Test
    @DisplayName("Given an organization, then it is remembered as a client note")
    void givenOrganizationThenRememberedAsClientNote() {
        given(context.getAuthenticationSession()).willReturn(authSession);
        given(organization.getId()).willReturn("org-id");

        SelectedOrganization.remember(context, organization);

        verify(authSession).setClientNote(OrganizationModel.ORGANIZATION_ATTRIBUTE, "org-id");
    }

    @Test
    @DisplayName("Given no organization, then nothing is remembered")
    void givenNoOrganizationThenNothingRemembered() {
        SelectedOrganization.remember(context, null);

        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Given no authentication session, then nothing is remembered")
    void givenNoAuthenticationSessionThenNothingRemembered() {
        given(context.getAuthenticationSession()).willReturn(null);

        SelectedOrganization.remember(context, organization);

        verify(authSession, never()).setClientNote(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString());
    }
}
