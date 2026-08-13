package com.automation_test_efrouting.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SetReactInput implements Interaction {

    private final String selector;
    private final String value;

    public SetReactInput(String selector, String value) {
        this.selector = selector;
        this.value = value;
    }

    @Override
    public < U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        boolean isXpath = selector.startsWith("//") || selector.startsWith("(");
        By locator = isXpath ? By.xpath(selector) : By.cssSelector(selector);
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript(
            "var el = arguments[0], val = arguments[1];"
                + "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                + "setter.call(el, val);"
                + "el.dispatchEvent(new Event('input', {bubbles:true}));"
                + "el.dispatchEvent(new Event('change', {bubbles:true}));"
                + "el.dispatchEvent(new Event('blur', {bubbles:true}));",
            element, value);
    }

    public static SetReactInput on(String selector, String value) {
        return new SetReactInput(selector, value);
    }
}
