package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Completa Destination ({@code data-cy=destination-city-input}) con autocompletado.
 */
public class EnterDestinationCity implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnterDestinationCity.class);
    private static final int MAX_INTENTOS = 3;
    private static final Duration ESPERA = Duration.ofSeconds(25);

    private final String city;

    public EnterDestinationCity(String city) {
        this.city = city;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, ESPERA);
        By suggestions = By.cssSelector(SelectorConstant.AUTOCOMPLETE_RESULT_ITEM);

        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(SelectorConstant.DESTINATION_CITY_INPUT)));
        LOGGER.info("Destination input: id={}, placeholder={}",
                input.getAttribute("id"), input.getAttribute("placeholder"));

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            escribirConJs(driver, input);
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(suggestions));
                driver.findElements(suggestions).get(0).click();
                LOGGER.info("Destination seleccionado: {}", city);
                return;
            } catch (TimeoutException e) {
                LOGGER.warn("Autocompletado de destination no respondió (intento {}/{})",
                        intento, MAX_INTENTOS);
            }
        }

        throw new AssertionError("No hubo sugerencias de autocompletado para destination '"
                + city + "'.");
    }

    private void escribirConJs(WebDriver driver, WebElement input) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].focus();", input);
        try {
            input.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        }
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        for (char letra : city.toCharArray()) {
            input.sendKeys(String.valueOf(letra));
        }
    }

    public static EnterDestinationCity named(String city) {
        return new EnterDestinationCity(city);
    }
}
