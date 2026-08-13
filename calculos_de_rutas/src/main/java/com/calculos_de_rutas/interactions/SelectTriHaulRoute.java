package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.abilities.CaptureUserRouteApi;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import com.calculos_de_rutas.utils.DiagnosticDump;
import com.calculos_de_rutas.utils.FinancialReport;
import com.calculos_de_rutas.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Elige una ruta sugerida: primero la <b>despliega</b> y después pulsa <b>Try this route</b>.
 * Espera la navegación a {@code /route-planner/detail/} (el cierre de mapa y los cálculos
 * siguen en los steps siguientes del feature).
 *
 * <p>Modo automático (sin tipo solicitado): Tri-hauls → Bi-hauls → Best Choice → Direct Routes,
 * tomando la primera sección que esté disponible.</p>
 *
 * <p>Modo forzado ({@link #ofType(String)}): exige una sección concreta (p.ej. "Tri-hauls",
 * "Bi-hauls" o "Best Choice") y falla con un mensaje claro si esa sección no aparece entre las
 * rutas sugeridas.</p>
 */
public class SelectTriHaulRoute implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectTriHaulRoute.class);
    private static final Duration WAIT_PAGE = Duration.ofSeconds(150);
    private static final Duration WAIT_EXPAND = Duration.ofSeconds(20);
    private static final Duration WAIT_DETAIL = Duration.ofSeconds(90);
    private static final Pattern CITY_ST = Pattern.compile(
            "\\b([A-Z][A-Za-z .'-]+,\\s*[A-Z]{2})\\b");

    /** Tipo de sección solicitado explícitamente; {@code null} = modo automático (primera disponible). */
    private final String seccionSolicitada;

    private SelectTriHaulRoute(String seccionSolicitada) {
        this.seccionSolicitada = seccionSolicitada;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait pageWait = new WebDriverWait(driver, WAIT_PAGE);

        String[] candidatos = etiquetasCandidatas();

        pageWait.until(d -> primeraSeccionDisponible(d, candidatos) != null);
        actor.attemptsTo(Pause.forSeconds(2));

        String seccion = primeraSeccionDisponible(driver, candidatos);
        if (seccion == null) {
            throw new AssertionError(seccionSolicitada != null
                    ? "No apareció la sección solicitada '" + seccionSolicitada
                            + "' entre las rutas sugeridas."
                    : "No apareció ninguna sección de rutas (Tri-hauls, Bi-hauls, Best Choice ni Direct Routes).");
        }

        LOGGER.info("Sección elegida: {}", seccion);
        FinancialReport.addContext("Sección de ruta elegida", seccion);

        String destinoSugerido = expandirTarjeta(driver, seccion);
        WebElement tryButton = esperarTryThisRoute(driver, seccion);

        CaptureUserRouteApi capture = CaptureUserRouteApi.withDriver(driver);
        actor.can(capture);
        capture.clear();

        LOGGER.info("Clic en Try this route ({})", seccion);
        clickTryThisRoute(driver, tryButton);

        if (!esperarDetalleOPasoDestino(actor, driver, destinoSugerido)) {
            DiagnosticDump.savePage(actor, "dom-tras-try-this-route.html");
            throw new AssertionError(
                    "Tras 'Try this route' no se abrió el detalle de la ruta. URL actual: "
                            + driver.getCurrentUrl());
        }

        LOGGER.info("Navegación al detalle OK: {}", driver.getCurrentUrl());
        FinancialReport.addContext("URL detalle", driver.getCurrentUrl());
    }

    /**
     * Espera el detalle. Solo si Easy routes ya desapareció y el campo Destination
     * queda interactuable (paso real de confirmación), lo completa y pulsa Continue.
     * No usa el título "Create route": ese h2 también aparece durante Easy routes.
     */
    private boolean esperarDetalleOPasoDestino(Actor actor, WebDriver driver, String destinoSugerido) {
        long deadline = System.currentTimeMillis() + WAIT_DETAIL.toMillis();
        boolean destinoManejado = false;
        long lastRetryClick = 0;

        while (System.currentTimeMillis() < deadline) {
            if (enDetalle(driver)) {
                return true;
            }

            if (!destinoManejado && pasoDestinationReal(driver)) {
                LOGGER.warn("Paso Destination real (Easy routes ya no visible); se completa y Continue.");
                DiagnosticDump.savePage(actor, "dom-paso-destination.html");
                completarPasoDestination(actor, driver, destinoSugerido);
                destinoManejado = true;
                continue;
            }

            // Reintento suave del botón si sigue visible y no hubo navegación
            if (System.currentTimeMillis() - lastRetryClick > 15_000 && botonTryVisibleGlobal(driver)) {
                LOGGER.info("Reintento de clic en Try this route (aún no hay detalle).");
                WebElement retry = primerVisible(driver, By.xpath(SelectorConstant.ANY_TRY_THIS_ROUTE_BUTTON));
                if (retry != null) {
                    clickTryThisRoute(driver, retry);
                    lastRetryClick = System.currentTimeMillis();
                }
            }

            pauseMs(500);
        }

        return enDetalle(driver);
    }

    /**
     * Destination es un paso real solo si Easy routes ya no está y el input
     * {@code destination-city-input} está visible.
     */
    private boolean pasoDestinationReal(WebDriver driver) {
        if (primeraSeccionDisponible(driver, SelectorConstant.ROUTE_SECTION_LABELS) != null) {
            return false;
        }
        if (textoVisible(driver, "Easy routes")) {
            return false;
        }
        return primerVisible(driver, By.cssSelector(SelectorConstant.DESTINATION_CITY_INPUT)) != null;
    }

    private void completarPasoDestination(Actor actor, WebDriver driver, String destinoSugerido) {
        String destino = (destinoSugerido != null && !destinoSugerido.isBlank())
                ? destinoSugerido
                : JsonTextSelector.DESTINATION_VALUE;
        LOGGER.info("Completando Destination con: {}", destino);
        FinancialReport.addContext("Destination post Try this route", destino);

        actor.attemptsTo(EnterDestinationCity.named(destino));
        actor.attemptsTo(Pause.forSeconds(1));

        WebElement continueBtn = primerVisible(driver, By.xpath(SelectorConstant.CONTINUE_BUTTON_ANY));
        if (continueBtn == null) {
            throw new AssertionError("Paso Destination visible pero no hay botón Continue.");
        }
        clickJs(driver, continueBtn);
        actor.attemptsTo(Pause.forSeconds(3));
    }

    private boolean enDetalle(WebDriver driver) {
        String url = driver.getCurrentUrl();
        return url != null && url.contains("/route-planner/detail/");
    }

    private boolean textoVisible(WebDriver driver, String texto) {
        String xpath = "//*[normalize-space()='" + texto + "']";
        for (WebElement el : driver.findElements(By.xpath(xpath))) {
            try {
                if (el.isDisplayed()) {
                    return true;
                }
            } catch (Exception ignored) {
                // stale
            }
        }
        return false;
    }

    /**
     * Etiquetas candidatas a buscar en pantalla: si se solicitó un tipo concreto, solo sus
     * variantes de mayúsculas/minúsculas; si no, todas las secciones en orden de preferencia.
     */
    private String[] etiquetasCandidatas() {
        if (seccionSolicitada == null) {
            return SelectorConstant.ROUTE_SECTION_LABELS;
        }
        return java.util.Arrays.stream(SelectorConstant.ROUTE_SECTION_LABELS)
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

    private String expandirTarjeta(WebDriver driver, String seccion) {
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
                String destino = extraerDestinoDeTarjeta(card);
                LOGGER.info("Tarjeta #{} expandida (destino inferido: {})", indice, destino);
                return destino;
            }

            clickJs(driver, card);
            pauseMs(2000);
            if (botonTryVisible(driver, seccion)) {
                return extraerDestinoDeTarjeta(card);
            }
        }

        String fallback = String.format(SelectorConstant.ROUTE_SECTION_HEADER, seccion)
                + "/following-sibling::div[1]//div[contains(@class,'cursor-pointer')]";
        for (WebElement row : driver.findElements(By.xpath(fallback))) {
            if (!row.isDisplayed()) {
                continue;
            }
            LOGGER.info("Desplegando fila cursor-pointer de fallback en '{}'", seccion);
            clickJs(driver, row);
            pauseMs(2000);
            if (botonTryVisible(driver, seccion)) {
                return extraerDestinoDeTarjeta(row);
            }
        }

        throw new AssertionError(
                "Se encontraron rutas en '" + seccion + "' pero al hacer clic no apareció "
                        + "'Try this route'. Hay que desplegar la tarjeta primero.");
    }

    private String extraerDestinoDeTarjeta(WebElement card) {
        try {
            String texto = card.getText();
            if (texto == null || texto.isBlank()) {
                WebElement parent = card;
                for (int i = 0; i < 4; i++) {
                    parent = parent.findElement(By.xpath(".."));
                    texto = parent.getText();
                    if (texto != null && texto.length() > 20) {
                        break;
                    }
                }
            }
            List<String> ciudades = new ArrayList<>();
            Matcher m = CITY_ST.matcher(texto == null ? "" : texto);
            while (m.find()) {
                ciudades.add(m.group(1).trim());
            }
            if (ciudades.isEmpty()) {
                return null;
            }
            Set<String> unicas = new LinkedHashSet<>(ciudades);
            List<String> lista = new ArrayList<>(unicas);
            String origenCity = JsonTextSelector.ORIGIN_VALUE.split(",")[0].toLowerCase(Locale.ROOT);
            for (int i = lista.size() - 1; i >= 0; i--) {
                String c = lista.get(i);
                if (!c.toLowerCase(Locale.ROOT).startsWith(origenCity)) {
                    return c;
                }
            }
            return lista.get(lista.size() - 1);
        } catch (Exception e) {
            LOGGER.warn("No se pudo inferir Destination de la tarjeta: {}", e.getMessage());
            return null;
        }
    }

    private boolean botonTryVisible(WebDriver driver, String seccion) {
        String xpath = String.format(SelectorConstant.TRY_THIS_ROUTE_IN_SECTION, seccion);
        if (primerVisible(driver, By.xpath(xpath)) != null) {
            return true;
        }
        return botonTryVisibleGlobal(driver);
    }

    private boolean botonTryVisibleGlobal(WebDriver driver) {
        return primerVisible(driver, By.xpath(SelectorConstant.ANY_TRY_THIS_ROUTE_BUTTON)) != null;
    }

    private WebElement esperarTryThisRoute(WebDriver driver, String seccion) {
        WebDriverWait wait = new WebDriverWait(driver, WAIT_EXPAND);
        String enSeccion = String.format(SelectorConstant.TRY_THIS_ROUTE_IN_SECTION, seccion);
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(enSeccion)));
        } catch (Exception e) {
            LOGGER.warn("Try this route no apareció acotado a '{}'; se busca en toda la página.", seccion);
            return wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(SelectorConstant.ANY_TRY_THIS_ROUTE_BUTTON)));
        }
    }

    private void scrollSeccion(WebDriver driver, String seccion) {
        List<WebElement> headers = headers(driver, seccion);
        if (!headers.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", headers.get(0));
            pauseMs(1000);
        }
    }

    /** Clic en Try this route: JS primero (evita overlays), luego Actions de refuerzo. */
    private void clickTryThisRoute(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
        pauseMs(300);
        clickJs(driver, element);
        pauseMs(500);
        try {
            if (botonTryVisibleGlobal(driver) && !enDetalle(driver)) {
                new Actions(driver).moveToElement(element).pause(Duration.ofMillis(150)).click().perform();
            }
        } catch (Exception e) {
            LOGGER.debug("Segundo clic nativo en Try this route omitido: {}", e.getMessage());
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

    /** Modo automático: toma la primera sección disponible (Tri-hauls → Bi-hauls → Best Choice → Direct Routes). */
    public static SelectTriHaulRoute secondTriHaulRouteSelected() {
        return new SelectTriHaulRoute(null);
    }

    /**
     * Modo forzado: exige la sección {@code tipoRuta} (p.ej. "Tri-hauls", "Bi-hauls" o
     * "Best Choice") entre las rutas sugeridas y falla si no aparece.
     */
    public static SelectTriHaulRoute ofType(String tipoRuta) {
        boolean conocida = java.util.Arrays.stream(SelectorConstant.ROUTE_SECTION_LABELS)
                .anyMatch(label -> label.equalsIgnoreCase(tipoRuta));
        if (!conocida) {
            throw new IllegalArgumentException("Tipo de ruta sugerida desconocido: '" + tipoRuta
                    + "'. Use uno de: Tri-hauls, Bi-hauls, Best Choice.");
        }
        return new SelectTriHaulRoute(tipoRuta);
    }
}
