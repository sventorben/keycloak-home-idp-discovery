package de.sventorben.keycloak.authentication;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.sventorben.keycloak.authentication.pages.AccountConsolePage;
import de.sventorben.keycloak.authentication.pages.SelectIdpPage;
import de.sventorben.keycloak.authentication.pages.TestRealmLoginPage;
import de.sventorben.keycloak.authentication.pages.UpstreamIdpMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class HomeIdpDiscoveryIT {

    private static final String KEYCLOAK_BASE_URL = "http://keycloak:8080";
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
    private AuthenticatorConfig authenticatorConfig;

    @BeforeEach
    public void setUp() {
        webDriver = setupDriver();
        authenticatorConfig = new AuthenticatorConfig(
            KEYCLOAK_CONTAINER.getAuthServerUrl(),
            KEYCLOAK_CONTAINER.getAdminUsername(),
            KEYCLOAK_CONTAINER.getAdminPassword()
        );
        authenticatorConfig.resetAuthenticatorConfig();
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
        authenticatorConfig.setUserAttribute("UPN");
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
            authenticatorConfig.enableBypassLoginPage();
        }

        @Test
        @DisplayName("GH-199 - Given no login hint, should not dispaly error message")
        public void gh199NoErrorMessage() {
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", null);
            testRealmLoginPage().assertLoginForClient("test");
            testRealmLoginPage().assertNoInvalidUserMessage();
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
            authenticatorConfig.setUserAttribute("UPN");
            upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test4@example.com");
            assertRedirectedToIdp();
        }

        @Nested
        @DisplayName("and given forwarding to first match is disabled")
        class GivenForwardingToFirstMatchIsDisabled {

            @BeforeEach
            void setUp() {
                authenticatorConfig.disableForwardToFirstMatch();
            }

            @Test
            @DisplayName("Given only one matched IdP, redirect")
            public void redirectIfOnlyOneIdPMatchesDomain() {
                upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test2@example.net");
                assertRedirectedToIdp();
            }

            @Test
            @DisplayName("Given multiple IdPs match, show selection")
            public void showSelectionIfMultipleIdpsMatch() {
                upstreamIdpMock().redirectToDownstreamWithLoginHint("test", "test@example.com");
                selectIdpPage().assertPageTitle();
            }
        }

    }

    @Nested
    @DisplayName("Given forwarding to first match is disabled")
    class GivenForwardingToFirstMatchIsDisabled {

        @BeforeEach
        void setUp() {
            authenticatorConfig.disableForwardToFirstMatch();
        }

        @Test
        @DisplayName("Given only one matched IdP, redirect")
        public void redirectIfOnlyOneIdPMatchesDomain() {
            accountConsolePage().signIn();
            testRealmLoginPage().signIn("test2@example.net");
            assertRedirectedToIdp();
        }

        @Test
        @DisplayName("Given multiple IdPs match, show selection")
        public void showSelectionIfMultipleIdpsMatch() {
            accountConsolePage().signIn();
            testRealmLoginPage().signIn("test@example.com");
            selectIdpPage().assertOnPage();
        }

        @Test
        @DisplayName("Given multiple IdPs match, when selecting one, redirects")
        public void redirectToIdpAfterSelection() {
            String idpAlias = "keycloak-oidc2";
            accountConsolePage().signIn();
            testRealmLoginPage().signIn("test@example.com");
            selectIdpPage().selectIdp(idpAlias);
            assertRedirectedToIdp(idpAlias);
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
                authenticatorConfig.disableForwarding();
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
                authenticatorConfig.enableForwarding();
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

    private SelectIdpPage selectIdpPage() {
        return new SelectIdpPage(webDriver, KEYCLOAK_BASE_URL);
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

    private static Keycloak getKeycloakAdminClient() {
        return Keycloak.getInstance(KEYCLOAK_CONTAINER.getAuthServerUrl(), "master",
            KEYCLOAK_CONTAINER.getAdminUsername(), KEYCLOAK_CONTAINER.getAdminPassword(), "admin-cli");
    }

    private void assertRedirectedToIdp() {
        assertRedirectedToIdp("keycloak-oidc");
    }

    private void assertRedirectedToIdp(String idpAlias) {
        assertRedirectedTo(KEYCLOAK_BASE_URL + "/realms/idp/protocol/openid-connect/auth");
        assertThat(webDriver.getCurrentUrl()).contains("broker%2F" + idpAlias + "%2F");
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
