package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.models.FinancialColumn;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Cierra el panel de mapa del detalle de ruta con el botón hamburguesa.
 *
 * <p>Con el mapa abierto la tabla se comprime a un tercio del ancho y efRouting deja de renderizar
 * las columnas de costos (Fuel, Toll, Custom, Op cost, Profit). Sin cerrarlo no hay nada que
 * comparar contra el Backend.</p>
 */
public class CollapseRouteMap implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollapseRouteMap.class);
    private static final int MAX_INTENTOS = 3;

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        esperarDetalleDeRuta(driver);
        actor.attemptsTo(
                WaitUntil.the(Target.the("Tabla de lanes").locatedBy(SelectorConstant.LANES_TABLE), isVisible())
                        .forNoMoreThan(120).seconds(),
                Pause.forSeconds(2)
        );

        if (columnasDeCostoVisibles(driver)) {
            LOGGER.info("Las columnas de costos ya están visibles; no hace falta cerrar el mapa.");
            return;
        }

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            if (!click(driver, SelectorConstant.MAP_TOGGLE_BUTTON)) {
                throw new AssertionError(
                        "No se encontró el botón hamburguesa que cierra el mapa. Se buscó con: "
                                + SelectorConstant.MAP_TOGGLE_BUTTON);
            }
            actor.attemptsTo(Pause.forSeconds(2));

            if (columnasDeCostoVisibles(driver)) {
                LOGGER.info("Mapa cerrado en el intento {}; columnas de costos renderizadas.", intento);
                return;
            }
        }

        throw new AssertionError(
                "Se pulsó el botón del mapa " + MAX_INTENTOS + " veces y la tabla sigue sin mostrar "
                        + "las columnas de costos (Fuel cost / Op cost / Profit).");
    }

    /**
     * Confirma que ya estamos en el detalle (SelectTriHaulRoute debe haber navegado antes).
     * Espera corta por si la URL aún está estabilizando.
     */
    private void esperarDetalleDeRuta(WebDriver driver) {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            String url = driver.getCurrentUrl();
            if (url != null && url.contains("/route-planner/detail/")) {
                LOGGER.info("Detalle de ruta cargado: {}", url);
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
                "Se esperaba estar en /route-planner/detail/ antes de cerrar el mapa. URL actual: "
                        + driver.getCurrentUrl());
    }

    /** El mapa cerrado se reconoce porque aparecen las columnas de costos, no por el ancho del panel. */
    private boolean columnasDeCostoVisibles(WebDriver driver) {
        List<WebElement> headers = driver.findElements(By.xpath(SelectorConstant.TABLE_HEADERS));
        boolean fuel = false;
        boolean profit = false;
        for (WebElement header : headers) {
            String columnId = header.getAttribute("data-column-id");
            String text = header.getText();
            fuel = fuel || FinancialColumn.FUEL.matchesHeader(columnId, text);
            profit = profit || FinancialColumn.PROFIT.matchesHeader(columnId, text);
        }
        return fuel && profit;
    }

    private boolean click(WebDriver driver, String xpath) {
        List<WebElement> elements = driver.findElements(By.xpath(xpath));
        if (elements.isEmpty()) {
            return false;
        }
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", elements.get(0));
        return true;
    }

    public static CollapseRouteMap toSeeAllColumns() {
        return new CollapseRouteMap();
    }
}
