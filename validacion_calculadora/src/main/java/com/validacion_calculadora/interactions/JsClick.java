package com.validacion_calculadora.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JsClick implements Interaction {

    private final String selector;

    public JsClick(String selector) {
        this.selector = selector;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        boolean isXpath = selector.startsWith("//") || selector.startsWith("(");
        By locator = isXpath ? By.xpath(selector) : By.cssSelector(selector);
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
    }

    public static JsClick on(String selector) {
        return new JsClick(selector);
    }
}
