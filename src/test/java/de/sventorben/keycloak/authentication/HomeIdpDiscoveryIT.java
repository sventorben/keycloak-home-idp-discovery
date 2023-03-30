package de.sventorben.keycloak.authentication;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.sventorben.keycloak.authentication.pages.AccountConsolePage;
import de.sventorben.keycloak.authentication.pages.TestRealmLoginPage;
import de.sventorben.keycloak.authentication.pages.UpstreamIdpMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.ws.rs.core.Response;
import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class HomeIdpDiscoveryIT {

    private static final String KEYCLOAK_BASE_URL = "http://keycloak:8080";
    private static final String REALM_TEST = "test-realm";
    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = KeycloakDockerContainer.create(NETWORK);

    @Container
    private static final BrowserWebDriverContainer BROWSER = new BrowserWebDriverContainer<>()
        .withCapabilities(new ChromeOptions().addArguments("--no-sandbox", "--disable-dev-shm-usage"))
        .dependsOn(KEYCLOAK_CONTAINER)
        .withRecordingMode(
            VncRecordingMode.RECORD_ALL,
            new File("target"),
            VncRecordingContainer.VncRecordingFormat.MP4)
        .withNetwork(NETWORK);

    @AfterAll
    static void tearDown() {
        NETWORK.close();
    }

    private RemoteWebDriver webDriver;

    @BeforeEach
    public void setUp() {
        webDriver = setupDriver();
        resetAuthenticatorConfig();
    }

    @Test
    @DisplayName("Given user's email is has a primary managed domain, redirect")
    public void redirectIfUserHasDomain() {
        accountConsolePage().signIn();
        testRealmLoginPage().signIn("test@example.com");
        assertRedirectedToIdp();
    }

    @Test
    @DisplayName("Given user's email is has an alternate managed domain, redirect")
    public void redirectIfUserHasAlternateDomain() {
        accountConsolePage().signIn();
        testRealmLoginPage().signIn("test2@example.net");
        assertRedirectedToIdp();
    }

    @Test
    @DisplayName("Given user's email has non managed domain, do not redirect")
    public void doNotRedirectIfUserHasNonManagedDomain() {
        accountConsolePage().signIn();
        testRealmLoginPage().signIn("test3@example.org");
        assertNotRedirected();
    }

    @Test
    @DisplayName("Given user's email is not verified, do not redirect")
    public void doNotRedirectIfUserEmailIsNotVerified() {
        accountConsolePage().signIn();
        testRealmLoginPage().signIn("test4@example.com");
        assertNotRedirected();
    }

    @Test
    @DisplayName("Given the user has a matching domain in custom user attribute, redirect")
    public void redirectIfUserHasDomainAsPartOfCustomUserAttribute() {
        setUserAttribute("UPN");
        accountConsolePage().signIn();
        testRealmLoginPage().signIn("test4@example.com");
        assertRedirectedToIdp();
    }

    @Nested
    @DisplayName("Remember Me")
    class RememberMe {

        private static final String COOKIE_NAME_REMEMBER_ME = "KEYCLOAK_REMEMBER_ME";

        @Test
        @DisplayName("Given the user has checked 'remember me' feature, remember user in cookie")
        public void testUserGetsRemembered() {
            accountConsolePage().signIn();

            TestRealmLoginPage testRealmLoginPage = testRealmLoginPage();
            testRealmLoginPage.enableRememberMe();
            testRealmLoginPage.signIn("test@example.com");
            assertRedirectedToIdp();

            getKeycloakAdminClient().realm("test-realm")
                .users()
                .get("139020a3-4459-43b1-a92f-d90e5cf093a1")
                .logout();

            accountConsolePage().signIn();

            Set<Cookie> cookies = webDriver.manage().getCookies();
            assertThat(cookies).contains(new Cookie(COOKIE_NAME_REMEMBER_ME, "username:test%40example.com"));
        }

        @Test
        @DisplayName("Given the user has unchecked 'remember me' feature, cookie will be removed")
        public void testUserWillBeForgotten() {
            testUserGetsRemembered();

            TestRealmLoginPage testRealmLoginPage = testRealmLoginPage();
            testRealmLoginPage.disableRememberMe();
            testRealmLoginPage.signIn("test@example.com");
            assertRedirectedToIdp();

            getKeycloakAdminClient().realm("test-realm")
                .users()
                .get("139020a3-4459-43b1-a92f-d90e5cf093a1")
                .logout();

            accountConsolePage().signIn();

            Set<Cookie> cookies = webDriver.manage().getCookies();
            assertThat(cookies.stream().map(Cookie::getName).collect(Collectors.toList()))
                .doesNotContain(COOKIE_NAME_REMEMBER_ME);
        }
    }

    @Nested
    @DisplayName("Given login page should be bypassed")
    class GivenLoginPageShouldBeBypassed {

        @BeforeEach
        public void setUp() {
            enableBypassLoginPage();
        }

        @Test
        @DisplayName("Given user's email is has a primary managed domain, redirect")
        public void redirectIfUserHasDomain() {
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test@example.com");
            assertRedirectedToIdp();
        }

        @Test
        @DisplayName("Given user's email is has an alternate managed domain, redirect")
        public void redirectIfUserHasAlternateDomain() {
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test2@example.net");
            assertRedirectedToIdp();
        }

        @Test
        @DisplayName("Given user's email has non managed domain, do not redirect")
        public void doNotRedirectIfUserHasNonManagedDomain() {
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test3@example.org");
            testRealmLoginPage().assertLoginForClient("test");
        }

        @Test
        @DisplayName("Given user's email is not verified, do not redirect")
        public void doNotRedirectIfUserEmailIsNotVerified() {
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test4@example.com");
            testRealmLoginPage().assertLoginForClient("test");
        }

        @Test
        @DisplayName("Given the user has a matching domain in custom user attribute, redirect")
        public void redirectIfUserHasDomainAsPartOfCustomUserAttribute() {
            setUserAttribute("UPN");
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test4@example.com");
            assertRedirectedToIdp();
        }

    }

    @Nested
    @DisplayName("Given user is linked to an IdP already")
    class GivenUserHasIdpLinkConfigured {

        private String username = "test5";

        @Nested
        @DisplayName("and given forwarding to linked IdPs is disabled")
        class ButForwardingNotEnabled {

            @BeforeEach
            public void setUp() {
                disableForwarding();
                accountConsolePage().signIn();
            }

            @Test
            @DisplayName("then do not redirect")
            public void willNotRedirectToIdp() {
                testRealmLoginPage().signIn(username);
                assertNotRedirected();
            }
        }

        @Nested
        @DisplayName("and given forwarding to linked IdPs is enabled")
        class AndForwardingEnabled {

            @BeforeEach
            public void setUp() {
                enableForwarding();
                accountConsolePage().signIn();
            }

            @Test
            @DisplayName("then redirect")
            public void willRedirectToIdp() {
                testRealmLoginPage().signIn(username);
                assertRedirectedToIdp();
            }

            @Test
            @DisplayName("then pass login_hint parameter to downstream IdP")
            public void willForwardLoginHint() {
                testRealmLoginPage().signIn(username);
                assertThat(webDriver.getCurrentUrl()).contains("&login_hint=idp-test5-username&");
            }
        }
    }

    private AccountConsolePage accountConsolePage() {
        return new AccountConsolePage(webDriver, KEYCLOAK_BASE_URL);
    }

    private TestRealmLoginPage testRealmLoginPage() {
        return new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL);
    }

    private UpstreamIdpMock upstreamIdpMock() {
        return new UpstreamIdpMock(webDriver, KEYCLOAK_BASE_URL);
    }

    private void setUserAttribute(String userAttribute) {
        updateAuthenticatorConfig(authenticatorConfig -> {
            Map<String, String> config = authenticatorConfig.getConfig();
            config.put("userAttribute", userAttribute);
            authenticatorConfig.setConfig(config);
        });
    }

    private void enableForwarding() {
        setForwarding(true);
    }

    private void disableForwarding() {
        setForwarding(false);
    }

    private void setForwarding(Boolean enabled) {
        updateAuthenticatorConfig(authenticatorConfig -> {
            Map<String, String> config = authenticatorConfig.getConfig();
            config.put("forwardToLinkedIdp", enabled.toString());
            authenticatorConfig.setConfig(config);
        });
    }

    private void enableBypassLoginPage() {
        setBypassLoginPage(true);
    }

    private void disableBypassLoginPage() {
        setBypassLoginPage(false);
    }

    private void setBypassLoginPage(Boolean enabled) {
        updateAuthenticatorConfig(authenticatorConfig -> {
            Map<String, String> config = authenticatorConfig.getConfig();
            config.put("bypassLoginPage", enabled.toString());
            authenticatorConfig.setConfig(config);
        });
    }

    private void resetAuthenticatorConfig() {
        disableForwarding();
        disableBypassLoginPage();
        setUserAttribute("email");
    }

    private void updateAuthenticatorConfig(Consumer<AuthenticatorConfigRepresentation> configurer) {
        try (Keycloak admin = getKeycloakAdminClient()) {
            AuthenticationManagementResource flows = admin.realm(REALM_TEST).flows();
            AuthenticationExecutionInfoRepresentation execution = flows
                .getExecutions("discover home idp").stream()
                .filter(it -> it.getProviderId().equalsIgnoreCase("home-idp-discovery"))
                .findFirst()
                .get();
            String authenticationConfigId = execution.getAuthenticationConfig();
            AuthenticatorConfigRepresentation authenticatorConfig;
            String authenticatorConfigAlias = "home-idp-discovery-flow-config";
            if (authenticationConfigId == null) {
                authenticatorConfig = new AuthenticatorConfigRepresentation();
                authenticatorConfig.setAlias(authenticatorConfigAlias);
                Response response = flows.newExecutionConfig(execution.getId(), authenticatorConfig);
                String location = response.getHeaderString("Location");
                authenticationConfigId = location.substring(location.lastIndexOf("/") + 1);
            } else {
                authenticatorConfig = flows.getAuthenticatorConfig(authenticationConfigId);
            }
            configurer.accept(authenticatorConfig);
            flows.updateAuthenticatorConfig(authenticationConfigId, authenticatorConfig);
        }
    }

    private static Keycloak getKeycloakAdminClient() {
        return Keycloak.getInstance(KEYCLOAK_CONTAINER.getAuthServerUrl(), "master",
            KEYCLOAK_CONTAINER.getAdminUsername(), KEYCLOAK_CONTAINER.getAdminPassword(), "admin-cli");
    }

    private void assertRedirectedToIdp() {
        assertRedirectedTo(KEYCLOAK_BASE_URL + "/realms/idp/protocol/openid-connect/auth");
    }

    private void assertNotRedirected() {
        assertRedirectedTo(
            KEYCLOAK_BASE_URL + "/realms/test-realm/login-actions/authenticate?client_id=account-console");
    }

    private void assertRedirectedTo(String url) {
        assertThat(webDriver.getCurrentUrl()).startsWith(url);
    }

    private static RemoteWebDriver setupDriver() {
        RemoteWebDriver driver = BROWSER.getWebDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        return driver;
    }

}
