package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Escribe la ciudad de origen y selecciona la primera sugerencia del autocompletado.
 *
 * <p>El buscador aplica debounce y consulta al servidor, así que a veces no alcanza a responder
 * con un sendKeys de golpe. Se escribe carácter por carácter y se reintenta antes de fallar.</p>
 */
public class EnterOriginCity implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnterOriginCity.class);
    private static final int MAX_INTENTOS = 3;
    private static final Duration ESPERA_SUGERENCIAS = Duration.ofSeconds(20);

    private final String city;

    public EnterOriginCity(String city) {
        this.city = city;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, ESPERA_SUGERENCIAS);
        By suggestions = By.cssSelector(SelectorConstant.AUTOCOMPLETE_RESULT_ITEM);

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(SelectorConstant.ORIGIN_INPUT)));

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            escribir(input);
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(suggestions));
                driver.findElements(suggestions).get(0).click();
                return;
            } catch (TimeoutException e) {
                LOGGER.warn("El autocompletado de origen no respondió en el intento {} de {}",
                        intento, MAX_INTENTOS);
            }
        }

        throw new AssertionError("El autocompletado no ofreció sugerencias para el origen '"
                + city + "' tras " + MAX_INTENTOS + " intentos.");
    }

    private void escribir(WebElement input) {
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        for (char letra : city.toCharArray()) {
            input.sendKeys(String.valueOf(letra));
        }
    }

    public static EnterOriginCity named(String city) {
        return new EnterOriginCity(city);
    }
}
