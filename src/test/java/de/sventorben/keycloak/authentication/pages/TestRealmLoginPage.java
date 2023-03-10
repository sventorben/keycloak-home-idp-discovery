package de.sventorben.keycloak.authentication.pages;

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

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(css = "input[name='username']")
    private WebElement usernameInput;

    @FindBy(css = "input[id='kc-login']")
    private WebElement signInButton;

    @FindBy(css = "a[id='try-another-way']")
    private WebElement tryAnotherWayLink;

    public TestRealmLoginPage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
        assertThat(webDriver.getCurrentUrl()).startsWith(keycloakBaseUrl + OIDC_AUTH_PATH);
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

    public void assertLoginForClient(String clientId) {
        assertThat(webDriver.getCurrentUrl()).contains("client_id=" + clientId);
    }

}
