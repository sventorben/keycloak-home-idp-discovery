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

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(css = "input[name='username']")
    private WebElement usernameInput;

    @FindBy(css = "input[id='kc-login']")
    private WebElement signInButton;

    public TestRealmLoginPage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
        assertThat(webDriver.getCurrentUrl()).startsWith(keycloakBaseUrl + "/realms/test-realm/protocol/openid-connect/auth");
    }

    public void signIn(String username) {
        new WebDriverWait(webDriver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOf(usernameInput));
        usernameInput.sendKeys(username);
        signInButton.click();
    }

}
