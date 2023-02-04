package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AlternativeClientPage {

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(css = "button[id='landingSignInButton']")
    private WebElement signInButton;

    public AlternativeClientPage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
    }

    public void gotoLoginPage() {
        webDriver.navigate().to(keycloakBaseUrl + "/realms/test-realm/protocol/openid-connect/auth?client_id=test-alternatives&response_mode=fragment&response_type=code&scope=openid&redirect_uri=http://localhost");
    }

}
