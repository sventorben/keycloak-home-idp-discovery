package de.sventorben.keycloak.authentication.hidpd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.keycloak.protocol.oidc.OIDCLoginProtocol.*;
import static org.keycloak.protocol.saml.SamlProtocol.SAML_FORCEAUTHN_REQUIREMENT;
import static org.keycloak.protocol.saml.SamlProtocol.SAML_LOGIN_REQUEST_FORCEAUTHN;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LoginPageTest {

    @Mock
    HomeIdpForwarderConfig config;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    AuthenticationFlowContext context;

    @Mock
    Reauthentication reauthentication;

    @InjectMocks
    LoginPage cut;

    @Test
    @DisplayName("Given the config switch is disabled, then should not bypass login")
    void givenConfigIsDisabledThenShouldNotBypassLogin() {
        given(config.bypassLoginPage()).willReturn(false);
        boolean shouldByPass = cut.shouldByPass();
        assertThat(shouldByPass).isFalse();
    }

    @Nested
    @DisplayName("Given the config switch is enabled")
    class GivenConfigIsEnabled {

        @BeforeEach
        void setUp() {
            given(config.bypassLoginPage()).willReturn(true);
        }

        @ParameterizedTest
        @ValueSource(strings = { PROMPT_VALUE_LOGIN, PROMPT_VALUE_CONSENT, PROMPT_VALUE_SELECT_ACCOUNT})
        @DisplayName("and given OIDC prompt is given and not none, then should not bypass login")
        void givenOidcPromptAndNotNoneThenDoNotBypassLogin(String prompt) {
            given(context.getAuthenticationSession().getClientNote(PROMPT_PARAM))
                .willReturn(prompt);
            boolean shouldByPass = cut.shouldByPass();
            assertThat(shouldByPass).isFalse();
        }

        @Test
        @DisplayName("and given SAML authentication is forced, then should not bypass login")
        void givenSamlAuthnIsForcedThenDoNotBypassLogin() {
            given(context.getAuthenticationSession().getAuthNote(SAML_LOGIN_REQUEST_FORCEAUTHN))
                .willReturn(SAML_FORCEAUTHN_REQUIREMENT);
            boolean shouldByPass = cut.shouldByPass();
            assertThat(shouldByPass).isFalse();
        }

        @Test
        @DisplayName("and given reauthentication is required, then should not bypass login")
        void givenReauthenticationIsRequiredThenDoNotBypassLogin() {
            given(reauthentication.required()).willReturn(true);
            boolean shouldByPass = cut.shouldByPass();
            assertThat(shouldByPass).isFalse();
        }

        @Test
        @DisplayName("then should bypass login")
        void thenShouldBypassLogin() {
            boolean shouldByPass = cut.shouldByPass();
            assertThat(shouldByPass).isTrue();
        }
    }
}
