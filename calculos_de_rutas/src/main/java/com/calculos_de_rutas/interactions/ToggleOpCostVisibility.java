package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Alterna la visibilidad del Op cost (icono de ojo) en la cabecera de la tabla.
 */
public class ToggleOpCostVisibility implements Interaction {

    private static final String FALLBACK_XPATH =
            "//th[contains(normalize-space(.),'Op') or contains(normalize-space(.),'Operat')]//button"
                    + " | //*[contains(normalize-space(.),'Op cost')]/ancestor::*[1]//button"
                    + " | //button[.//*[contains(@class,'eye') or contains(@data-lucide,'eye')]]";

    @Override
    public <U extends Actor> void performAs(U actor) {
        if (!clickToggle(actor)) {
            throw new AssertionError(
                    "No se encontró el toggle de visibilidad de Op cost (icono de ojo). "
                            + "Actualice SelectorConstant.OP_COST_TOGGLE con el data-cy real del frontend.");
        }
    }

    /**
     * Intenta alternar el Op cost. Devuelve false si no encuentra el control, sin abortar el escenario.
     */
    public static boolean clickToggle(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        List<WebElement> toggles = driver.findElements(By.xpath(SelectorConstant.OP_COST_TOGGLE));
        if (toggles.isEmpty()) {
            toggles = driver.findElements(By.xpath(FALLBACK_XPATH));
        }
        if (toggles.isEmpty()) {
            return false;
        }

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", toggles.get(0));
        actor.attemptsTo(Pause.forSeconds(2));
        return true;
    }

    public static ToggleOpCostVisibility toggle() {
        return new ToggleOpCostVisibility();
    }
}
