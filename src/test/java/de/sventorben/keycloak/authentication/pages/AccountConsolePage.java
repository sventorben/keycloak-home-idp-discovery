package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AccountConsolePage {

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(css = "button[id='landingSignInButton']")
    private WebElement signInButton;

    public AccountConsolePage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
    }

    public void open() {
        webDriver.navigate().to(keycloakBaseUrl + "/realms/test-realm/account/");
    }

}
