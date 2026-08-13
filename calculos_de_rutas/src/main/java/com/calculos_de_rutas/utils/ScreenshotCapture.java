package com.calculos_de_rutas.utils;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * Toma capturas de pantalla del navegador para incrustarlas (base64) en el reporte HTML,
 * de forma que el archivo final quede autocontenido y no dependa de rutas externas.
 */
public final class ScreenshotCapture {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotCapture.class);

    /**
     * JS: desplaza la tabla de lanes hasta su última celda (columna Profit, fila Total) antes de
     * capturar, tanto a la derecha (columnas Income…Profit) como hacia abajo (fila Total) cuando
     * hay más lanes de las que caben en pantalla (p.ej. Tri-hauls con 5-6 filas).
     */
    private static final String PREPARE_FULL_TABLE_VIEW_JS =
            "var footCells = document.querySelectorAll(\"table[data-cy='lanes-table'] tfoot tr td\");"
                    + "var target = footCells.length ? footCells[footCells.length - 1] : null;"
                    + "if (!target) {"
                    + "  var ths = document.querySelectorAll(\"table[data-cy='lanes-table'] thead th\");"
                    + "  target = ths.length ? ths[ths.length - 1] : null;"
                    + "}"
                    + "if (target) { target.scrollIntoView({block:'nearest', inline:'end'}); }";

    private ScreenshotCapture() {}

    /** Devuelve la captura actual como PNG codificado en base64, o {@code null} si falla. */
    public static String captureBase64(Actor actor) {
        try {
            WebDriver driver = BrowseTheWeb.as(actor).getDriver();
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            LOGGER.warn("No se pudo tomar la captura de pantalla", e);
            return null;
        }
    }

    /**
     * Maximiza la ventana (la vista centrada de 1280x800 corta columnas a la derecha) y desplaza
     * la tabla de lanes hasta el final para que Income…Profit queden dentro del cuadro visible,
     * y solo entonces toma la captura.
     */
    public static String captureFullTableBase64(Actor actor) {
        try {
            WebDriver driver = BrowseTheWeb.as(actor).getDriver();
            driver.manage().window().maximize();
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript(PREPARE_FULL_TABLE_VIEW_JS);
            Thread.sleep(500);
        } catch (Exception e) {
            LOGGER.warn("No se pudo maximizar/desplazar la tabla antes de capturar", e);
        }
        return captureBase64(actor);
    }
}
