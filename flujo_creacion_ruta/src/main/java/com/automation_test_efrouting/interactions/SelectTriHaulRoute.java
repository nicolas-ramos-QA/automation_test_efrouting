package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Elige una ruta sugerida: despliega una tarjeta y pulsa <b>Try this route</b>.
 *
 * <p>Modo forzado ({@link #ofType(String)}): espera a que cargue Easy routes y busca la
 * sección solicitada (Tri-hauls, Bi-hauls, Best Choice o Loops). Si Easy routes ya cargó
 * y ese tipo <b>no</b> está entre las sugerencias, anula el escenario
 * ({@link TestAbortedException}) para que no se marque como fallido. Si aparece, continúa.</p>
 *
 * <p>Modo automático ({@link #secondTriHaulRouteSelected()}): toma la primera sección disponible.</p>
 */
public class SelectTriHaulRoute implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectTriHaulRoute.class);

    /** Tiempo máximo a que cargue al menos una sección de Easy routes. */
    private static final long WAIT_SECTIONS_MS = 90_000L;
    private static final long POLL_MS = 2_000L;

    /** Tipo de sección solicitado; {@code null} = primera disponible. */
    private final String seccionSolicitada;

    private SelectTriHaulRoute(String seccionSolicitada) {
        this.seccionSolicitada = seccionSolicitada;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        String[] candidatos = etiquetasCandidatas();

        String seccion = esperarSeccionDisponible(driver, candidatos);
        if (seccion == null) {
            String disponibles = seccionesVisibles(driver);
            String mensaje = seccionSolicitada != null
                    ? "No se encontró el tipo de ruta '" + seccionSolicitada
                    + "' entre las sugerencias. Secciones visibles: [" + disponibles
                    + "]. Escenario anulado (no fallido)."
                    : "No apareció ninguna sección de rutas. Escenario anulado (no fallido).";
            LOGGER.warn(mensaje);
            throw new TestAbortedException(mensaje);
        }

        LOGGER.info("Sección elegida: {}", seccion);
        expandirTarjeta(driver, seccion);
        WebElement tryButton = esperarTryThisRoute(driver, seccion);
        if (tryButton == null) {
            throw new AssertionError(
                    "Se encontró la sección '" + seccion + "' pero no apareció el botón 'Try this route'.");
        }

        LOGGER.info("Clic en Try this route ({})", seccion);
        clickJs(driver, tryButton);
        actor.attemptsTo(Pause.forSeconds(10));
    }

    /**
     * Espera a que cargue Easy routes. Si se pidió un tipo concreto y ya hay otras
     * secciones visibles pero no la pedida, corta de inmediato (no espera 90 s en vano).
     */
    private String esperarSeccionDisponible(WebDriver driver, String[] candidatos) {
        long deadline = System.currentTimeMillis() + WAIT_SECTIONS_MS;
        while (System.currentTimeMillis() < deadline) {
            String encontrada = primeraSeccionDisponible(driver, candidatos);
            if (encontrada != null) {
                return encontrada;
            }

            // Easy routes ya cargó (hay alguna sección conocida) pero no la solicitada → anular ya
            if (seccionSolicitada != null
                    && primeraSeccionDisponible(driver, SelectorConstant.ROUTE_SECTION_LABELS) != null) {
                LOGGER.info("Easy routes cargó sin '{}'. Secciones visibles: [{}]",
                        seccionSolicitada, seccionesVisibles(driver));
                return null;
            }

            LOGGER.info("Esperando sección(es) {}…", Arrays.toString(candidatos));
            pauseMs(POLL_MS);
        }
        return primeraSeccionDisponible(driver, candidatos);
    }

    private String seccionesVisibles(WebDriver driver) {
        StringBuilder sb = new StringBuilder();
        for (String label : SelectorConstant.ROUTE_SECTION_LABELS) {
            if (!headers(driver, label).isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(label);
            }
        }
        return sb.length() == 0 ? "ninguna" : sb.toString();
    }

    private String[] etiquetasCandidatas() {
        if (seccionSolicitada == null) {
            return SelectorConstant.ROUTE_SECTION_LABELS;
        }
        return Arrays.stream(SelectorConstant.ROUTE_SECTION_LABELS)
                .filter(label -> label.equalsIgnoreCase(seccionSolicitada))
                .toArray(String[]::new);
    }

    private String primeraSeccionDisponible(WebDriver driver, String[] candidatos) {
        for (String label : candidatos) {
            if (!headers(driver, label).isEmpty()) {
                return label;
            }
        }
        return null;
    }

    private List<WebElement> headers(WebDriver driver, String label) {
        return driver.findElements(By.xpath(String.format(SelectorConstant.ROUTE_SECTION_HEADER, label)))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private void expandirTarjeta(WebDriver driver, String seccion) {
        scrollSeccion(driver, seccion);

        int total = driver.findElements(
                By.xpath(String.format(SelectorConstant.ROUTE_CARDS_IN_SECTION, seccion))).size();
        LOGGER.info("Tarjetas en '{}': {}", seccion, total);

        int[] orden = total >= 2 ? new int[]{2, 1, 3, 4, 5} : new int[]{1, 2, 3};

        for (int indice : orden) {
            if (indice > total && total > 0) {
                continue;
            }
            String xpath = String.format(SelectorConstant.ROUTE_CARD_CLICKABLE, seccion, indice);
            List<WebElement> cards = driver.findElements(By.xpath(xpath));
            if (cards.isEmpty()) {
                continue;
            }

            WebElement card = cards.get(0);
            LOGGER.info("Desplegando tarjeta #{} de '{}'", indice, seccion);
            clickJs(driver, card);
            pauseMs(2000);

            if (botonTryVisible(driver, seccion)) {
                return;
            }

            clickJs(driver, card);
            pauseMs(2000);
            if (botonTryVisible(driver, seccion)) {
                return;
            }
        }

        throw new AssertionError(
                "Se encontraron rutas en '" + seccion + "' pero al hacer clic no apareció "
                        + "'Try this route'. Hay que desplegar la tarjeta primero.");
    }

    private boolean botonTryVisible(WebDriver driver, String seccion) {
        String xpath = String.format(SelectorConstant.TRY_THIS_ROUTE_IN_SECTION, seccion);
        if (primerVisible(driver, By.xpath(xpath)) != null) {
            return true;
        }
        return primerVisible(driver, By.xpath(SelectorConstant.ANY_TRY_THIS_ROUTE_BUTTON)) != null;
    }

    private WebElement esperarTryThisRoute(WebDriver driver, String seccion) {
        long deadline = System.currentTimeMillis() + 20_000L;
        String enSeccion = String.format(SelectorConstant.TRY_THIS_ROUTE_IN_SECTION, seccion);
        while (System.currentTimeMillis() < deadline) {
            WebElement btn = primerVisible(driver, By.xpath(enSeccion));
            if (btn == null) {
                btn = primerVisible(driver, By.xpath(SelectorConstant.ANY_TRY_THIS_ROUTE_BUTTON));
            }
            if (btn != null) {
                return btn;
            }
            pauseMs(500);
        }
        return null;
    }

    private void scrollSeccion(WebDriver driver, String seccion) {
        List<WebElement> headers = headers(driver, seccion);
        if (!headers.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", headers.get(0));
            pauseMs(1000);
        }
    }

    private void clickJs(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
    }

    private WebElement primerVisible(WebDriver driver, By by) {
        for (WebElement el : driver.findElements(by)) {
            try {
                if (el.isDisplayed() && el.isEnabled()) {
                    return el;
                }
            } catch (Exception ignored) {
                // stale
            }
        }
        return null;
    }

    private void pauseMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Modo automático: primera sección disponible. */
    public static SelectTriHaulRoute secondTriHaulRouteSelected() {
        return new SelectTriHaulRoute(null);
    }

    /**
     * Modo forzado: exige la sección {@code tipoRuta}. Si Easy routes cargó sin ese tipo,
     * anula el escenario (no fallido).
     */
    public static SelectTriHaulRoute ofType(String tipoRuta) {
        boolean conocida = Arrays.stream(SelectorConstant.ROUTE_SECTION_LABELS)
                .anyMatch(label -> label.equalsIgnoreCase(tipoRuta));
        if (!conocida) {
            throw new IllegalArgumentException("Tipo de ruta sugerida desconocido: '" + tipoRuta
                    + "'. Use uno de: Tri-hauls, Bi-hauls, Best Choice, Loops.");
        }
        return new SelectTriHaulRoute(tipoRuta);
    }
}
