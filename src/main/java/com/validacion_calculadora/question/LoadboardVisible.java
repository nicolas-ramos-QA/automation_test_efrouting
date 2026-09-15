package com.validacion_calculadora.question;

import com.validacion_calculadora.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoadboardVisible implements Question<Boolean> {

    @Override
    public Boolean answeredBy(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        String url = driver.getCurrentUrl();
        boolean urlOk = url != null && url.contains("/loads");

        boolean headlineOk = false;
        for (WebElement el : driver.findElements(By.xpath(SelectorConstant.LOADBOARD_HEADLINE))) {
            try {
                if (el.isDisplayed()) {
                    headlineOk = true;
                    break;
                }
            } catch (Exception ignored) {
                // stale
            }
        }
        return urlOk || headlineOk;
    }

    public static LoadboardVisible isShown() {
        return new LoadboardVisible();
    }
}
