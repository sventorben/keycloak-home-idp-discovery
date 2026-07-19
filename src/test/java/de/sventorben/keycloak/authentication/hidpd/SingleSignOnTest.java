package de.sventorben.keycloak.authentication.hidpd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SingleSignOnTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    AuthenticationFlowContext context;

    @Mock
    UserModel user;

    @InjectMocks
    SingleSignOn cut;

    @Test
    @DisplayName("Given a user and the SSO auth note, then SSO is established")
    void givenUserAndSsoAuthNoteThenEstablished() {
        given(context.getUser()).willReturn(user);
        given(context.getAuthenticationSession().getAuthNote(AuthenticationManager.SSO_AUTH))
            .willReturn("true");

        assertThat(cut.established()).isTrue();
    }

    @Test
    @DisplayName("Given no user, then SSO is not established")
    void givenNoUserThenNotEstablished() {
        given(context.getUser()).willReturn(null);

        assertThat(cut.established()).isFalse();
    }

    @Test
    @DisplayName("Given a user but no SSO auth note, then SSO is not established")
    void givenNoSsoAuthNoteThenNotEstablished() {
        // Forced re-authentication, step-up and forked flows all leave the note unset, so the user
        // must still be challenged.
        given(context.getUser()).willReturn(user);
        given(context.getAuthenticationSession().getAuthNote(AuthenticationManager.SSO_AUTH))
            .willReturn(null);

        assertThat(cut.established()).isFalse();
    }
}
