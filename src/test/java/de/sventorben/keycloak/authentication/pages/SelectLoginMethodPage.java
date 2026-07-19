package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectLoginMethodPage {

    private static final String LOGIN_ACTIONS_PATH = "/realms/test-realm/login-actions/authenticate";

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    @FindBy(css = "h1[id='kc-page-title']")
    private WebElement pageTitle;

    @FindBy(css = "ul.select-auth-container")
    private WebElement selectAuthContainer;

    public SelectLoginMethodPage(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
        Navigation.awaitUrlStartingWith(webDriver, keycloakBaseUrl + LOGIN_ACTIONS_PATH);
        assertThat(webDriver.getCurrentUrl()).startsWith(keycloakBaseUrl + LOGIN_ACTIONS_PATH);
        assertThat(pageTitle.getText()).isEqualTo("Select login method");
    }

    public void assertThatHasAlternativeWithTitle(String title) {
        assertThat(selectAuthContainer.isDisplayed()).isTrue();
        assertThat(getTexts("select-auth-box-headline")).contains(title);
    }

    public void assertThatHasAlternativeWithHelpText(String helpText) {
        assertThat(selectAuthContainer.isDisplayed()).isTrue();
        assertThat(getTexts("select-auth-box-desc")).contains(helpText);
    }

    private List<String> getTexts(String className) {
        return selectAuthContainer.findElements(By.className(className))
            .stream().map(it -> it.getText().trim()).collect(Collectors.toList());
    }

}
