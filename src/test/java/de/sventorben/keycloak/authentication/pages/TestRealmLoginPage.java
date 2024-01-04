package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class TestRealmLoginPage {

    private static final String OIDC_AUTH_PATH = "/realms/test-realm/protocol/openid-connect/auth";
    private static final String LOGIN_ACTIONS_PATH = "/realms/test-realm/login-actions/authenticate";

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(css = "input[id='username']")
    private WebElement usernameInput;

    @FindBy(css = "input[id='kc-login']")
    private WebElement signInButton;

    @FindBy(css = "a[id='try-another-way']")
    private WebElement tryAnotherWayLink;

    @FindBy(css = "input[id='rememberMe']")
    private WebElement rememberMe;

    public TestRealmLoginPage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
        waitForLoginPage();
        assertThat(webDriver.getCurrentUrl()).satisfiesAnyOf(
            it -> assertThat(it).startsWith(keycloakBaseUrl + OIDC_AUTH_PATH),
            it -> assertThat(it).startsWith(keycloakBaseUrl + LOGIN_ACTIONS_PATH)
        );
    }

    private void waitForLoginPage() {
        new WebDriverWait(webDriver, Duration.ofSeconds(5))
            .until((driver) -> {
                String currentUrl = driver.getCurrentUrl();
                return currentUrl.startsWith(keycloakBaseUrl + OIDC_AUTH_PATH)
                    || currentUrl.startsWith(keycloakBaseUrl + LOGIN_ACTIONS_PATH);
            }
        );
    }

    public void signIn(String username) {
        new WebDriverWait(webDriver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOf(usernameInput));
        usernameInput.sendKeys(username);
        signInButton.click();
    }

    public void tryAnotherWay() {
        new WebDriverWait(webDriver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOf(tryAnotherWayLink));
        tryAnotherWayLink.click();
    }

    public void enableRememberMe() {
        if (!rememberMe.isSelected()) {
            rememberMe.click();
        }
        assertThat(rememberMe.isSelected()).isTrue();
    }

    public void disableRememberMe() {
        if (rememberMe.isSelected()) {
            rememberMe.click();
        }
        assertThat(rememberMe.isSelected()).isFalse();
    }

    public void assertLoginForClient(String clientId) {
        assertThat(webDriver.getCurrentUrl()).contains("client_id=" + clientId);
    }

    public void assertNoInvalidUserMessage() {
        Duration implicitWaitTimeout = webDriver.manage().timeouts().getImplicitWaitTimeout();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        try {
            assertThat(webDriver.findElements(By.id("input-error-username"))).isEmpty();
        } finally {
            webDriver.manage().timeouts().implicitlyWait(implicitWaitTimeout);
        }
    }

    public void assertUsernameFieldIsDisplayed() {
        assertThat(usernameInput.isDisplayed()).isTrue();
    }

    public void assertUsernameFieldIsPrefilledWith(String username) {
        assertThat(usernameInput.getAttribute("value")).isEqualTo(username);
    }

    public void assertPasswordFieldIsDisplayed() {
        assertThat(webDriver.findElement(By.id("password")).isDisplayed()).isTrue();
    }

    public void assertPasswordFieldIsNotDisplayed() {
        assertThat(webDriver.findElements(By.id("password")).isEmpty()).isTrue();
    }
}
