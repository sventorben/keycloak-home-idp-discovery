package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class UpstreamIdpMock {

    private final WebDriver webDriver;
    private final String keycloakBaseUrl;

    public UpstreamIdpMock(WebDriver webDriver, String keycloakBaseUrl) {
        this.webDriver = webDriver;
        this.keycloakBaseUrl = keycloakBaseUrl;
        PageFactory.initElements(webDriver, this);
    }

    public void redirectToDownstreamWithLoginHint(String clientId, String loginHint) {
        String url = "/realms/test-realm/protocol/openid-connect/auth?client_id=" + clientId + "&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Frealms%2Ftest-realm%2Faccount%2F&response_mode=fragment&response_type=code&scope=openid";
        if (loginHint != null) {
            url += "&login_hint=" + loginHint;
        }
        webDriver.navigate().to(keycloakBaseUrl + url);
    }

    public void redirectToDownstreamWithPromptLogin(String clientId) {
        String url = "/realms/test-realm/protocol/openid-connect/auth?client_id=" + clientId + "&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Frealms%2Ftest-realm%2Faccount%2F&response_mode=fragment&response_type=code&scope=openid";
        url += "&prompt=login";
        webDriver.navigate().to(keycloakBaseUrl + url);
    }

}
