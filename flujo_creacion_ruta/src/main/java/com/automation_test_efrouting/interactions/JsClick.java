package com.automation_test_efrouting.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JsClick implements Interaction {

    private final String cssSelector;

    public JsClick(String cssSelector) {
        this.cssSelector = cssSelector;
    }

    @Override
    public < U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        boolean isXpath = cssSelector.startsWith("//") || cssSelector.startsWith("(");
        By locator = isXpath ? By.xpath(cssSelector) : By.cssSelector(cssSelector);
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
    }

    public static JsClick on(String cssSelector) {
        return new JsClick(cssSelector);
    }
}
