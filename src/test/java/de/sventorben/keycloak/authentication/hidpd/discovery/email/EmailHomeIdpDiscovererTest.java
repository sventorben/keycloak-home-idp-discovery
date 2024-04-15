package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.RealmModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailHomeIdpDiscovererTest {

    @Mock
    AuthenticationFlowContext context;

    EmailHomeIdpDiscoverer cut;

    @BeforeEach
    void setUp() {
        when(context.getRealm()).thenReturn(mock(RealmModel.class));
        cut = new EmailHomeIdpDiscoverer(mock(Users.class), mock(IdentityProviders.class));
    }

    @Test
    void doesNotThrowExceptionWhenNotConfigured() {
        when(context.getAuthenticatorConfig()).thenReturn(null);
        assertThatCode(() -> cut.discoverForUser(context, "test"))
            .doesNotThrowAnyException();
    }

}
