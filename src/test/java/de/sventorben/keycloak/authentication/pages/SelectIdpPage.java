package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectIdpPage {

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(id = "hidpd-select-provider-title")
    private WebElement pageTitle;

    public SelectIdpPage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
    }

    public void assertOnPage() {
        assertThat(webDriver.getCurrentUrl()).startsWith(
            keycloakBaseUrl + "/realms/test-realm/login-actions/authenticate");
        assertPageTitle();
    }

    public void assertPageTitle() {
        assertThat(pageTitle.getText()).isEqualTo("Select your home identity provider");
    }

    public void selectIdp(String idpAlias) {
        WebElement idpLoginLink = webDriver.findElements(By.id("social-" + idpAlias)).get(0);
        idpLoginLink.click();
    }
}
