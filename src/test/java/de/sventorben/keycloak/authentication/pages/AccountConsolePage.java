package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
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

    public void signIn() {
        webDriver.navigate().to(keycloakBaseUrl + "/realms/test-realm/account/");
        WebElement landingSignInButton = new WebDriverWait(webDriver, Duration.ofSeconds(10))
            .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id='landingSignInButton']")));
        landingSignInButton.click();
    }

}
