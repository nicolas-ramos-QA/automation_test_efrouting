package com.validacion_calculadora.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

public class CenterWindow implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Number availWidth = (Number) js.executeScript("return window.screen.availWidth;");
        Number availHeight = (Number) js.executeScript("return window.screen.availHeight;");

        int screenWidth = availWidth.intValue();
        int screenHeight = availHeight.intValue();

        int width = Math.min(1280, screenWidth - 40);
        int height = Math.min(800, screenHeight - 60);

        driver.manage().window().setSize(new Dimension(width, height));

        int x = Math.max(0, (screenWidth - width) / 2);
        int y = Math.max(0, (screenHeight - height) / 2);

        driver.manage().window().setPosition(new Point(x, y));
    }

    public static CenterWindow centered() {
        return new CenterWindow();
    }
}
