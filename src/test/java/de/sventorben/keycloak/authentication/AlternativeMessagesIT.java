package de.sventorben.keycloak.authentication;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import de.sventorben.keycloak.authentication.pages.AlternativeClientPage;
import de.sventorben.keycloak.authentication.pages.SelectLoginMethodPage;
import de.sventorben.keycloak.authentication.pages.TestRealmLoginPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@Testcontainers
class AlternativeMessagesIT {

    private static final String KEYCLOAK_BASE_URL = "http://keycloak:8080";
    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = KeycloakDockerContainer.create(NETWORK);

    @Container
    private static final BrowserWebDriverContainer BROWSER = new BrowserWebDriverContainer<>()
        .withCapabilities(new ChromeOptions().addArguments("--no-sandbox", "--disable-dev-shm-usage"))
        .withEnv("SE_ENABLE_TRACING", "false")
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
        alternativeClientPage().gotoLoginPage();
    }

    @Test
    public void checkCorrectMessagesForAnotherWayTitle() {
        testRealmLoginPage().tryAnotherWay();
        selectLoginMethodPage().assertThatHasAlternativeWithTitle("Home identity provider");
    }

    @Test
    public void checkCorrectMessagesForAnotherWayHelpText() {
        testRealmLoginPage().tryAnotherWay();
        selectLoginMethodPage().assertThatHasAlternativeWithHelpText("Sign in via your home identity provider which will be automatically determined based on your provided email address.");
    }

    private AlternativeClientPage alternativeClientPage() {
        return new AlternativeClientPage(webDriver, KEYCLOAK_BASE_URL);
    }

    private TestRealmLoginPage testRealmLoginPage() {
        return new TestRealmLoginPage(webDriver, KEYCLOAK_BASE_URL);
    }

    private SelectLoginMethodPage selectLoginMethodPage() {
        return new SelectLoginMethodPage(webDriver, KEYCLOAK_BASE_URL);
    }

    private static RemoteWebDriver setupDriver() {
        RemoteWebDriver driver = BROWSER.getWebDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        return driver;
    }

}
