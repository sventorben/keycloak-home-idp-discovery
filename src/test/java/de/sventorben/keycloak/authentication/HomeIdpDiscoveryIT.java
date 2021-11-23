package de.sventorben.keycloak.authentication;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.sventorben.keycloak.authentication.pages.AccountConsolePage;
import de.sventorben.keycloak.authentication.pages.TestRealmLoginPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class HomeIdpDiscoveryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeIdpDiscoveryIT.class);

    private static final int KEYCLOAK_HTTP_PORT = 8080;
    private static final String KEYCLOAK_ADMIN_PASS = "admin";
    private static final String KEYCLOAK_ADMIN_USER = "admin";
    private static final String KEYCLOAK_BASE_URL = "http://keycloak:8080";

    private static final String REALM_TEST = "test-realm";

    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = createKeycloakContainer(
        System.getProperty("keycloak.dist", "keycloak-x"), System.getProperty("keycloak.version", "latest"))
        .withProviderClassesFrom("target/classes")
        .withExposedPorts(KEYCLOAK_HTTP_PORT)
        .withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams())
        .withRealmImportFiles("/test-realm.json", "/idp-realm.json")
        .withStartupTimeout(Duration.ofSeconds(30))
        .withNetwork(NETWORK)
        .withNetworkAliases("keycloak")
        .withAdminUsername(KEYCLOAK_ADMIN_USER)
        .withAdminPassword(KEYCLOAK_ADMIN_PASS);

    private static KeycloakContainer createKeycloakContainer(String dist, String version) {
        String fullImage = "quay.io/keycloak/" + dist + ":" + version;
        LOGGER.info("Running test with Keycloak image: " + fullImage);
        return new KeycloakContainer(fullImage);
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
    public void gotoLoginPage() {
        webDriver = setupDriver();
        new AccountConsolePage(webDriver, KEYCLOAK_BASE_URL).signIn();
    }

    @Test
    public void redirectIfUserHasDomain() {
        new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn("test@example.com");
        assertRedirectedToIdp();
    }

    @Test
    public void redirectIfUserHasAlternateDomain() {
        new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn("test2@example.net");
        assertRedirectedToIdp();
    }

    @Test
    public void doNotRedirectIfUserHasNonConfiguredDomain() {
        new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn("test3@example.org");
        assertNotRedirected();
    }

    @Test
    public void doNotRedirectIfUserEmailIsNotVerified() {
        new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn("test4@example.com");
        assertNotRedirected();
    }

    @Nested
    class GivenUserHasIdpLinkConfigured {

        private String username = "test5";

        @Nested
        class ButForwardingNotEnabled {

            @BeforeEach
            public void setUp() {
                disableForwarding();
            }

            @Test
            public void willNotRedirectToIdp() {
                new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn(username);
                assertNotRedirected();
            }
        }

        @Nested
        class AndForwardingEnabled {

            @BeforeEach
            public void setUp() {
                enableForwarding();
            }

            @Test
            public void willRedirectToIdp() {
                new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn(username);
                assertRedirectedToIdp();
            }

            @Test
            public void willForwardLoginHint() {
                new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL).signIn(username);
                assertThat(webDriver.getCurrentUrl()).contains("&login_hint=idp-test5-username&");
            }
        }
    }

    private void enableForwarding() {
        setForwarding(true);
    }

    private void disableForwarding() {
        setForwarding(false);
    }

    private void setForwarding(Boolean enabled) {
        Keycloak admin = KEYCLOAK_CONTAINER.getKeycloakAdminClient();
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
        Map<String, String> config = authenticatorConfig.getConfig();
        config.put("forwardToLinkedIdp", enabled.toString());
        authenticatorConfig.setConfig(config);
        flows.updateAuthenticatorConfig(authenticationConfigId, authenticatorConfig);
    }

    private void assertRedirectedToIdp() {
        assertRedirectedTo("/realms/idp/protocol/openid-connect/auth");
    }

    private void assertNotRedirected() {
        assertRedirectedTo("/realms/test-realm/login-actions/authenticate?client_id=account-console");
    }

    private void assertRedirectedTo(String s) {
        assertThat(webDriver.getCurrentUrl()).startsWith(KEYCLOAK_BASE_URL + s);
    }

    private static RemoteWebDriver setupDriver() {
        RemoteWebDriver driver = BROWSER.getWebDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        return driver;
    }

}
