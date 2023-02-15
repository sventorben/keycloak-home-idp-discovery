package de.sventorben.keycloak.authentication.hidpd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.mockito.InjectMocks;
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
        when(context.getUser()).thenReturn(mock(UserModel.class));
        cut = new HomeIdpDiscoverer(context);
    }

    @Test
    void doesNotThrowExceptionWhenNotConfigured() {
        when(context.getAuthenticatorConfig()).thenReturn(null);
        assertThatCode(() -> cut.discoverForUser("test"))
            .doesNotThrowAnyException();
    }

}
