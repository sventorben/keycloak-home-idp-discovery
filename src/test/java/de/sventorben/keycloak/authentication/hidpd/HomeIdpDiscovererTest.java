package de.sventorben.keycloak.authentication.hidpd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeIdpDiscovererTest {

    @Mock
    AuthenticationFlowContext context;

    HomeIdpDiscoverer cut;

    @BeforeEach
    void setUp() {
        when(context.getRealm()).thenReturn(mock(RealmModel.class));
        cut = new HomeIdpDiscoverer(context, mock(Users.class));
    }

    @Test
    void doesNotThrowExceptionWhenNotConfigured() {
        when(context.getAuthenticatorConfig()).thenReturn(null);
        assertThatCode(() -> cut.discoverForUser("test"))
            .doesNotThrowAnyException();
    }

}
