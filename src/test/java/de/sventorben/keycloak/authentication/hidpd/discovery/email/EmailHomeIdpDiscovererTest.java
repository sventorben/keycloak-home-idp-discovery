package de.sventorben.keycloak.authentication.hidpd.discovery.email;

import de.sventorben.keycloak.authentication.hidpd.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class EmailHomeIdpDiscovererTest {

    @Mock
    AuthenticationFlowContext context;

    @Mock
    KeycloakSession session;
    
    @Mock
    Users users;
    
    @Mock
    UserModel user;
    
    @Mock
    RealmModel realm;
    
    @Mock
    IdentityProviders identityProviders;
    
    @Mock
    UserProvider userProvider;

    EmailHomeIdpDiscoverer cut;

    @BeforeEach
    void setUp() {
        when(context.getRealm()).thenReturn(mock(RealmModel.class));
        cut = new EmailHomeIdpDiscoverer(users, identityProviders);
    }

    @Test
    void doesNotThrowExceptionWhenNotConfigured() {
        when(context.getAuthenticatorConfig()).thenReturn(null);
        assertThatCode(() -> cut.discoverForUser(context, "test"))
            .doesNotThrowAnyException();
    }

    @Test
    void onlyLinkedProvidersForUserWithoutEmail() {
        when(context.getRealm()).thenReturn(realm);
        when(context.getSession()).thenReturn(session);
        when(session.users()).thenReturn(userProvider);
        
        AuthenticatorConfigModel config = new AuthenticatorConfigModel();
        config.setConfig(Map.of(
            "forwardToLinkedIdp", "true",
            "forwardNoEmail", "true"
        ));
        when(context.getAuthenticatorConfig()).thenReturn(config);

        when(users.lookupBy("test")).thenReturn(user);

        when(user.getFirstAttribute("email")).thenReturn("");
        when(user.getUsername()).thenReturn("test");
        when(user.getId()).thenReturn("id");

        IdentityProviderModel google = new IdentityProviderModel();
        google.setAlias("google");
        google.setEnabled(true);

        IdentityProviderModel github = new IdentityProviderModel();
        github.setAlias("github");
        github.setEnabled(true);

        when(identityProviders.candidatesForHomeIdp(context, user))
            .thenReturn(List.of(google, github));

        when(userProvider.getFederatedIdentitiesStream(realm, user))
            .thenReturn(Stream.of(
                new FederatedIdentityModel("github", "github-user", "123")
            ));

        List<IdentityProviderModel> result = cut.discoverForUser(context, "test");

        assertThat(result)
            .extracting(IdentityProviderModel::getAlias)
            .containsExactly("github");
    }

    @Test
    void noProvidersForUserNoEmailAndNoLink() {
        when(context.getRealm()).thenReturn(realm);
        when(context.getSession()).thenReturn(session);
        when(session.users()).thenReturn(userProvider);

        AuthenticatorConfigModel config = new AuthenticatorConfigModel();
        config.setConfig(Map.of(
            "forwardToLinkedIdp", "true",
            "forwardNoEmail", "true"
        ));
        when(context.getAuthenticatorConfig()).thenReturn(config);

        when(users.lookupBy("test")).thenReturn(user);

        when(user.getFirstAttribute("email")).thenReturn("");
        when(user.getId()).thenReturn("id");

        // No linked IdPs
        when(userProvider.getFederatedIdentitiesStream(realm, user))
            .thenReturn(Stream.empty());

        // Realm has candidates, but none are linked to the user
        IdentityProviderModel github = new IdentityProviderModel();
        github.setInternalId("github");
        github.setAlias("github");
        github.setEnabled(true);

        when(identityProviders.candidatesForHomeIdp(context, user))
            .thenReturn(List.of(github));

        List<IdentityProviderModel> result = cut.discoverForUser(context, "test");

        assertThat(result).isEmpty();
    }

    @Test
    void noForwardWithEmailWhenNoLink() {
        when(context.getRealm()).thenReturn(realm);
        when(context.getSession()).thenReturn(session);
        when(session.users()).thenReturn(userProvider);
        
        AuthenticatorConfigModel config = new AuthenticatorConfigModel();
        config.setConfig(Map.of(
            "forwardToLinkedIdp", "true",
            "forwardNoEmail", "true"
        ));
        when(context.getAuthenticatorConfig()).thenReturn(config);

        when(users.lookupBy("test")).thenReturn(user);

        when(user.getFirstAttribute("email")).thenReturn("test@example.com");
        when(user.getId()).thenReturn("id");
        when(user.isEmailVerified()).thenReturn(true);
        when(userProvider.getFederatedIdentitiesStream(realm, user))
            .thenReturn(Stream.empty());

        // Domain discovery finds no matching IdP
        when(identityProviders.candidatesForHomeIdp(context, user))
            .thenReturn(List.of());

        List<IdentityProviderModel> result = cut.discoverForUser(context, "test");

        assertThat(result).isEmpty();
    }

    @Test
    void noForwardUnverifiedEmailWhenForwardingUnverifiedDisabled() {
        when(context.getRealm()).thenReturn(realm);

        AuthenticatorConfigModel config = new AuthenticatorConfigModel();
        config.setConfig(Map.of(
            "forwardToLinkedIdp", "true",
            "forwardNoEmail", "true",
            "forwardUnverifiedEmail", "false"
        ));
        when(context.getAuthenticatorConfig()).thenReturn(config);

        when(users.lookupBy("test@example.com")).thenReturn(user);

        when(user.getId()).thenReturn("id");
        when(user.isEmailVerified()).thenReturn(false);
        when(user.getFirstAttribute("email")).thenReturn("test@example.com");


        List<IdentityProviderModel> result =
            cut.discoverForUser(context, "test@example.com");

        assertThat(result).isEmpty();
    }
}
