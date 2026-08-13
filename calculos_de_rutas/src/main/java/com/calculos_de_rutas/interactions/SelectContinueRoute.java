package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.userinterface.SelectorConstant;
import com.calculos_de_rutas.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Pulsa Continue del paso 1 (origen/tráiler) y espera a que carguen las rutas sugeridas
 * (Easy routes: Tri-hauls / Bi-hauls / …), no solo un sleep fijo.
 */
public class SelectContinueRoute implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectContinueRoute.class);
    private static final Duration WAIT_EASY_ROUTES = Duration.ofSeconds(120);

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                WaitUntil.the(Target.the(JsonTextSelector.CONTINUE_INPUT_NAME)
                                .locatedBy(SelectorConstant.CONTINUE_BUTTON), isVisible())
                        .forNoMoreThan(20).seconds(),
                JsClick.on(SelectorConstant.CONTINUE_BUTTON),
                Pause.forSeconds(3)
        );

        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, WAIT_EASY_ROUTES);
        wait.until(d -> seccionRutasVisible(d));
        LOGGER.info("Easy routes cargado tras Continue.");
        actor.attemptsTo(Pause.forSeconds(2));
    }

    private boolean seccionRutasVisible(WebDriver driver) {
        for (String label : SelectorConstant.ROUTE_SECTION_LABELS) {
            String xpath = String.format(SelectorConstant.ROUTE_SECTION_HEADER, label);
            for (WebElement el : driver.findElements(By.xpath(xpath))) {
                try {
                    if (el.isDisplayed()) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // stale
                }
            }
        }
        return false;
    }

    public static SelectContinueRoute continueSelected() {
        return new SelectContinueRoute();
    }
}
