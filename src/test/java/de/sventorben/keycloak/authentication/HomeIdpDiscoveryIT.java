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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.VncRecordingContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.ws.rs.core.Response;
import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class HomeIdpDiscoveryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeIdpDiscoveryIT.class);

    private static final String KEYCLOAK_VERSION = System.getProperty("keycloak.version", "latest");
    private static final String KEYCLOAK_ADMIN_PASS = "admin";
    private static final String KEYCLOAK_ADMIN_USER = "admin";
    private static final String KEYCLOAK_BASE_URL = "http://keycloak:8080";
    private static final int KEYCLOAK_HTTP_PORT = 8080;

    private static final String REALM_TEST = "test-realm";

    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = createKeycloakContainer()
        .withProviderClassesFrom("target/classes")
        .withExposedPorts(KEYCLOAK_HTTP_PORT)
        .withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams())
        .withStartupTimeout(Duration.ofSeconds(90))
        .withNetwork(NETWORK)
        .withNetworkAliases("keycloak")
        .withAdminUsername(KEYCLOAK_ADMIN_USER)
        .withAdminPassword(KEYCLOAK_ADMIN_PASS);


    private static KeycloakContainer createKeycloakContainer() {
        String fullImage = "quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION;
        LOGGER.info("Running test with image: " + fullImage);
        return new KeycloakContainer(fullImage)
            .withRealmImportFile("/test-realm.json")
            .withRealmImportFile("/idp-realm.json");
    }

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
