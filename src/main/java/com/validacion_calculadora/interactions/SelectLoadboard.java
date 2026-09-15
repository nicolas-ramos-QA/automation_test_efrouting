package com.validacion_calculadora.interactions;

import com.validacion_calculadora.userinterface.SelectorConstant;
import com.validacion_calculadora.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Navega al Loadboard desde el menú lateral (equivalente a Routes en los otros flujos).
 */
public class SelectLoadboard implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                Pause.forSeconds(3),
                WaitUntil.the(Target.the(JsonTextSelector.LOADBOARD_INPUT_NAME)
                                .located(By.xpath(SelectorConstant.LOADBOARD_MENU)), isVisible())
                        .forNoMoreThan(40).seconds(),
                JsClick.on(SelectorConstant.LOADBOARD_MENU),
                Pause.forSeconds(2)
        );

        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        long deadline = System.currentTimeMillis() + 40_000L;
        while (System.currentTimeMillis() < deadline) {
            String url = driver.getCurrentUrl();
            boolean onLoads = url != null && url.contains("/loads");
            boolean hasHeadline = !driver.findElements(
                    By.xpath(SelectorConstant.LOADBOARD_HEADLINE)).isEmpty();
            // También espera a que exista al menos un input usable en el buscador
            boolean hasSearchInput = driver.findElements(
                    By.cssSelector("input:not([type='hidden']), textarea")).stream()
                    .anyMatch(el -> {
                        try {
                            return el.isDisplayed();
                        } catch (Exception e) {
                            return false;
                        }
                    });
            if (onLoads && (hasHeadline || hasSearchInput)) {
                actor.attemptsTo(Pause.forSeconds(2));
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new AssertionError(
                "Tras hacer clic en Loadboard no se estabilizó /loads. URL actual: "
                        + driver.getCurrentUrl());
    }

    public static SelectLoadboard loadboardSectionSelected() {
        return new SelectLoadboard();
    }
}
