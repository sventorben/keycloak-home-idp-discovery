package de.sventorben.keycloak.authentication.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

/**
 * Synchronisation helpers for page navigation.
 * <p>
 * Selenium returns from a click as soon as the command has been dispatched, not once the resulting
 * page has loaded, and an implicit wait only covers element lookups -- it never applies to
 * {@code getCurrentUrl()}. Anything that inspects the URL straight after an action therefore has to
 * wait for the navigation explicitly, or it observes the page the browser is about to leave.
 */
public final class Navigation {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private Navigation() {
    }

    public static void awaitUrlStartingWith(WebDriver webDriver, String prefix) {
        await(webDriver).until(driver -> {
            String currentUrl = driver.getCurrentUrl();
            return currentUrl != null && currentUrl.startsWith(prefix);
        });
    }

    public static void awaitUrlContaining(WebDriver webDriver, String fragment) {
        await(webDriver).until(ExpectedConditions.urlContains(fragment));
    }

    /**
     * Clicks an element and waits until the browser has actually left the current URL, so that
     * callers reading the URL afterwards observe the page the click led to.
     * <p>
     * Note that {@link ExpectedConditions#stalenessOf} is unsuitable here: elements injected by
     * {@code PageFactory} are proxies that re-locate themselves on every access and so never go
     * stale.
     */
    public static void clickAndAwaitNavigation(WebDriver webDriver, WebElement element) {
        String urlBefore = webDriver.getCurrentUrl();
        element.click();
        await(webDriver).until(driver -> !Objects.equals(driver.getCurrentUrl(), urlBefore));
    }

    private static WebDriverWait await(WebDriver webDriver) {
        return new WebDriverWait(webDriver, TIMEOUT);
    }
}
