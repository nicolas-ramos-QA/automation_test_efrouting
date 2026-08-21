package com.validacion_calculadora.interactions;

import com.validacion_calculadora.userinterface.SelectorConstant;
import com.validacion_calculadora.utils.DiagnosticDump;
import com.validacion_calculadora.utils.LoadSearchCities;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Completa origen y destino en el buscador de Loadboard (ciudades variables) y lanza la búsqueda.
 * <p>
 * En /loads el formulario completo llega con clase {@code hidden}; primero hay que expandir
 * {@code data-cy=search-bar-trigger}. Destination arranca como un {@code <p>} clicable y
 * el botón lupa es un {@code div} verde, no un {@code button}.
 */
public class SearchLoadsForm implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchLoadsForm.class);

    private final String origin;
    private final String destination;

    private SearchLoadsForm(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        LOGGER.info("Buscando cargas: {} → {}", origin, destination);
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        expandirBarraBusqueda(driver);

        WebElement originField = esperarOrigin(driver, 45_000L);
        if (originField == null) {
            DiagnosticDump.savePage(actor, "dom-loadboard-sin-origin.html");
            throw new AssertionError(
                    "No se encontró el campo Origin en Loadboard. DOM en target/diagnostico/dom-loadboard-sin-origin.html. URL="
                            + driver.getCurrentUrl());
        }

        escribirYElegir(actor, driver, originField, origin);
        if (!campoTieneCiudad(driver, true, origin)) {
            LOGGER.warn("Origin no quedó con '{}'; reintento.", origin);
            originField = esperarOrigin(driver, 10_000L);
            escribirYElegir(actor, driver, originField, origin);
        }

        completarDestinationConVerificacion(actor, driver);

        lanzarBusquedaHastaResultados(actor, driver);
    }

    /**
     * Destination a menudo vuelve al placeholder si el clic de sugerencia no prende.
     * Se reintenta hasta confirmar que ya no dice "Find the best destination".
     */
    private void completarDestinationConVerificacion(Actor actor, WebDriver driver) {
        for (int intento = 1; intento <= 3; intento++) {
            WebElement destinationField = revelarYEsperarDestination(driver, 15_000L);
            if (destinationField == null) {
                DiagnosticDump.savePage(actor, "dom-loadboard-sin-destination.html");
                throw new AssertionError(
                        "No se encontró el campo Destination en Loadboard. DOM en target/diagnostico/dom-loadboard-sin-destination.html.");
            }
            LOGGER.info("Destination intento {} — escribiendo {}", intento, destination);
            escribirYElegir(actor, driver, destinationField, destination);
            pauseMs(400);

            if (campoTieneCiudad(driver, false, destination)) {
                LOGGER.info("Destination confirmado: {}", destination);
                return;
            }
            LOGGER.warn("Destination no quedó seleccionado (sigue el placeholder); reintento.");
            // Volver a abrir el prompt
            WebElement prompt = primerVisible(driver, By.xpath(SelectorConstant.LOADBOARD_DESTINATION_PROMPT));
            if (prompt != null) {
                clickJs(driver, prompt);
                pauseMs(500);
            }
        }
        DiagnosticDump.savePage(actor, "dom-loadboard-destination-no-queda.html");
        throw new AssertionError(
                "No se pudo fijar Destination='" + destination
                        + "'. DOM en target/diagnostico/dom-loadboard-destination-no-queda.html");
    }

    private boolean campoTieneCiudad(WebDriver driver, boolean origin, String cityState) {
        String needle = shortQuery(cityState).toLowerCase(Locale.ROOT);
        String state = cityState.contains(",")
                ? cityState.substring(cityState.indexOf(',') + 1).trim().toLowerCase(Locale.ROOT)
                : "";

        if (origin) {
            WebElement input = primerVisible(driver, By.cssSelector("input[data-cy='origin-input']"));
            if (input != null) {
                String val = safe(input.getAttribute("value")).toLowerCase(Locale.ROOT);
                if (val.contains(needle)) {
                    return true;
                }
            }
        } else {
            // Destination OK si el placeholder "Find the best destination" ya no está visible
            // y hay texto de la ciudad en el área del search-bar
            WebElement prompt = primerVisible(driver, By.xpath(SelectorConstant.LOADBOARD_DESTINATION_PROMPT));
            if (prompt != null) {
                return false;
            }
            WebElement destInput = encontrarDestinationInput(driver);
            if (destInput != null) {
                String val = safe(destInput.getAttribute("value")).toLowerCase(Locale.ROOT);
                if (val.contains(needle)) {
                    return true;
                }
            }
            // Chip / texto ya seleccionado en la barra
            for (WebElement el : driver.findElements(By.cssSelector(
                    "[data-cy='search-bar'] p, [data-cy='search-bar'] span, [data-testid='search-bar-destination']"))) {
                try {
                    if (!el.isDisplayed()) {
                        continue;
                    }
                    String t = safe(el.getText()).toLowerCase(Locale.ROOT);
                    if (t.contains(needle) && (state.isBlank() || t.contains(state))) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // stale
                }
            }
        }
        return false;
    }

    /**
     * Clic en Search loads. Si ya estamos en /loads/results (re-búsqueda con otras ciudades),
     * hay que hacer clic igual: no salir solo porque la URL ya tiene results.
     */
    private void lanzarBusquedaHastaResultados(Actor actor, WebDriver driver) {
        long deadline = System.currentTimeMillis() + 50_000L;
        boolean alreadyOnResults = llegoAResultados(driver);
        String urlBefore = safe(driver.getCurrentUrl());
        int clicks = 0;

        while (System.currentTimeMillis() < deadline) {
            // En re-búsqueda: exigir al menos 1 clic a Search antes de dar por bueno
            if (llegoAResultados(driver) && (!alreadyOnResults || clicks >= 1)) {
                if (alreadyOnResults && clicks == 1) {
                    // Dar tiempo a que la lista se refresque tras el Search
                    pauseMs(2_000L);
                }
                LOGGER.info("Resultados de cargas cargados: {}", driver.getCurrentUrl());
                return;
            }
            if (clicks < 6) {
                WebElement searchBtn = encontrarBotonBuscarExpandido(driver);
                if (searchBtn != null) {
                    LOGGER.info("Clic búsqueda #{} tag={} data-cy={} text='{}'{}",
                            clicks + 1,
                            searchBtn.getTagName(),
                            safe(searchBtn.getAttribute("data-cy")),
                            safe(searchBtn.getText()).replaceAll("\\s+", " ").trim(),
                            alreadyOnResults ? " (re-búsqueda)" : "");
                    try {
                        searchBtn.click();
                    } catch (Exception e) {
                        clickJs(driver, searchBtn);
                    }
                    clicks++;
                    pauseMs(alreadyOnResults ? 2_000L : 1_500L);
                    // Si la URL cambió de query, ya refrescaron
                    String urlNow = safe(driver.getCurrentUrl());
                    if (alreadyOnResults && !urlNow.equals(urlBefore) && urlNow.contains("/loads/results")) {
                        pauseMs(1_000L);
                        LOGGER.info("Resultados de cargas cargados: {}", urlNow);
                        return;
                    }
                    continue;
                }
                LOGGER.info("Sin botón Search loads visible; se envía ENTER");
                new Actions(driver).sendKeys(Keys.ENTER).perform();
                clicks++;
                pauseMs(1_200L);
                continue;
            }
            pauseMs(500);
        }
        DiagnosticDump.savePage(actor, "dom-loadboard-sin-resultados.html");
        throw new AssertionError(
                "Tras buscar cargas no se llegó a resultados. URL: " + driver.getCurrentUrl());
    }

    private boolean llegoAResultados(WebDriver driver) {
        String url = driver.getCurrentUrl();
        if (url != null && url.contains("/loads/results")) {
            return true;
        }
        return primerVisible(driver, By.xpath(SelectorConstant.LOADBOARD_RESULTS_LABEL)) != null;
    }

    /** Botón Search loads / lupa del formulario expandido. */
    private WebElement encontrarBotonBuscarExpandido(WebDriver driver) {
        List<By> candidates = List.of(
                By.cssSelector("[data-cy='search-bar-button'], [data-testid='search-bar-button']"),
                By.xpath("//button[contains(normalize-space(.),'Search loads')]"),
                By.xpath(SelectorConstant.LOADBOARD_SEARCH_BUTTON),
                By.xpath("//*[@data-cy='search-bar']"
                        + "//div[contains(@class,'rounded-full') and contains(@class,'00A065')]"),
                By.xpath("//div[contains(@class,'rounded-full') and contains(@class,'00A065')]")
        );
        for (By by : candidates) {
            WebElement el = primerVisible(driver, by);
            if (el != null) {
                return el;
            }
        }
        return null;
    }

    /**
     * El search-bar expandido llega con {@code hidden}. Clic en el trigger compacto lo revela.
     */
    private void expandirBarraBusqueda(WebDriver driver) {
        if (primerVisible(driver, By.cssSelector("input[data-cy='origin-input']")) != null) {
            LOGGER.info("Campo Origin ya visible; no se expande el trigger.");
            return;
        }

        WebElement trigger = primerVisible(driver, By.cssSelector("[data-cy='search-bar-trigger']"));
        if (trigger == null) {
            trigger = primerVisible(driver, By.xpath(SelectorConstant.LOADBOARD_SEARCH_BAR_TRIGGER));
        }
        if (trigger != null) {
            LOGGER.info("Expandiendo search-bar vía data-cy=search-bar-trigger");
            clickJs(driver, trigger);
            pauseMs(900);
            return;
        }

        // Fallback: clic en la etiqueta "Origin" del trigger visual
        WebElement originLabel = primerVisible(driver,
                By.xpath("//*[@data-cy='search-bar-trigger']//p[normalize-space()='Origin']"
                        + " | //p[normalize-space()='Origin']/ancestor::div[contains(@class,'cursor-pointer')][1]"));
        if (originLabel != null) {
            LOGGER.info("Expandiendo search-bar vía etiqueta Origin");
            clickJs(driver, originLabel);
            pauseMs(900);
        }
    }

    private WebElement esperarOrigin(WebDriver driver, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int intento = 0;
        while (System.currentTimeMillis() < deadline) {
            WebElement found = encontrarOrigin(driver);
            if (found != null) {
                return found;
            }
            // Reintentar expandir cada ~2.5 s por si el primer clic no aplicó
            if (intento % 5 == 0) {
                expandirBarraBusqueda(driver);
            }
            intento++;
            pauseMs(500);
        }
        return encontrarOrigin(driver);
    }

    private WebElement encontrarOrigin(WebDriver driver) {
        WebElement byCy = primerVisible(driver, By.cssSelector("input[data-cy='origin-input']"));
        if (byCy != null) {
            LOGGER.info("Campo origin localizado por data-cy=origin-input");
            return byCy;
        }
        WebElement byXp = primerVisible(driver, By.xpath(SelectorConstant.LOADBOARD_ORIGIN_INPUT));
        if (byXp != null) {
            LOGGER.info("Campo origin localizado por xpath: {}", summarize(byXp));
            return byXp;
        }
        return encontrarCampoFlexible(driver, true);
    }

    private WebElement revelarYEsperarDestination(WebDriver driver, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int clicksPrompt = 0;

        while (System.currentTimeMillis() < deadline) {
            WebElement input = encontrarDestinationInput(driver);
            if (input != null) {
                // No devolver el origin-input por error
                String cy = safe(input.getAttribute("data-cy")).toLowerCase(Locale.ROOT);
                String ph = safe(input.getAttribute("placeholder")).toLowerCase(Locale.ROOT);
                if (cy.contains("origin") || ph.contains("origin")) {
                    LOGGER.warn("Se encontró origin al buscar destination; se ignora.");
                } else {
                    return input;
                }
            }
            WebElement prompt = primerVisible(driver, By.xpath(SelectorConstant.LOADBOARD_DESTINATION_PROMPT));
            if (prompt != null && clicksPrompt < 5) {
                LOGGER.info("Revelando input Destination con clic en prompt");
                clickJs(driver, prompt);
                clicksPrompt++;
                pauseMs(500);
                continue;
            }
            pauseMs(300);
        }
        return encontrarDestinationInput(driver);
    }

    private WebElement encontrarDestinationInput(WebDriver driver) {
        WebElement byCy = primerVisible(driver, By.cssSelector("input[data-cy='destination-input']"));
        if (byCy != null) {
            return byCy;
        }
        // Input visible cuyo placeholder habla de destination (nunca origin)
        for (WebElement el : driver.findElements(By.cssSelector(
                "input[data-testid='place-autocomplete-input'], input[placeholder*='destination'], input[placeholder*='Destination']"))) {
            try {
                if (!el.isDisplayed() || !el.isEnabled()) {
                    continue;
                }
                String cy = safe(el.getAttribute("data-cy")).toLowerCase(Locale.ROOT);
                String ph = safe(el.getAttribute("placeholder")).toLowerCase(Locale.ROOT);
                if (cy.contains("origin") || ph.contains("origin")) {
                    continue;
                }
                if (cy.contains("destination") || ph.contains("destination") || ph.contains("best")) {
                    return el;
                }
            } catch (Exception ignored) {
                // stale
            }
        }
        List<WebElement> autos = new ArrayList<>();
        for (WebElement el : driver.findElements(By.cssSelector("input[data-testid='place-autocomplete-input']"))) {
            try {
                if (el.isDisplayed() && el.isEnabled()) {
                    String cy = safe(el.getAttribute("data-cy")).toLowerCase(Locale.ROOT);
                    if (cy.contains("origin")) {
                        continue;
                    }
                    autos.add(el);
                }
            } catch (Exception ignored) {
                // stale
            }
        }
        if (autos.size() >= 1) {
            // Si hay 2+, el de destination suele ser el último
            WebElement pick = autos.get(autos.size() - 1);
            String cy = safe(pick.getAttribute("data-cy")).toLowerCase(Locale.ROOT);
            if (!cy.contains("origin")) {
                LOGGER.info("Destination = place-autocomplete #{}", autos.size());
                return pick;
            }
        }
        return null;
    }

    private void escribirYElegir(Actor actor, WebDriver driver, WebElement field, String cityState) {
        String query = shortQuery(cityState);
        try {
            clickJs(driver, field);
            pauseMs(150);
            clearField(driver, field);
            field.sendKeys(query);
        } catch (StaleElementReferenceException e) {
            // El search-bar se re-monta al repetir la búsqueda; el llamador reintenta con el campo nuevo
            LOGGER.warn("Campo de búsqueda stale al escribir '{}'; se reintenta.", cityState);
            return;
        }
        pauseMs(350);
        seleccionarSugerenciaRapida(driver, cityState);
        // Cerrar dropdown residual
        pauseMs(200);
    }

    private boolean clicSugerencia(WebDriver driver, WebElement item) {
        try {
            item.click();
            return true;
        } catch (StaleElementReferenceException e) {
            return false;
        } catch (Exception e) {
            return clickJs(driver, item);
        }
    }

    /**
     * Espera corta y clic inmediato en la sugerencia exacta (ej. Atlanta, GA).
     */
    private void seleccionarSugerenciaRapida(WebDriver driver, String expectedCity) {
        String exact = expectedCity.trim();
        String query = shortQuery(expectedCity).toLowerCase(Locale.ROOT);
        String state = estadoDe(exact);
        long deadline = System.currentTimeMillis() + 4_000L;
        String exactXp = String.format(SelectorConstant.LOADBOARD_AUTOCOMPLETE_EXACT, exact, exact);

        while (System.currentTimeMillis() < deadline) {
            WebElement exactMatch = primerVisible(driver, By.xpath(exactXp));
            if (exactMatch != null) {
                LOGGER.info("Sugerencia exacta (rápida): {}", exact);
                if (clicSugerencia(driver, exactMatch)) {
                    pauseMs(350);
                    return;
                }
                pauseMs(200);
                continue;
            }

            WebElement best = null;
            for (WebElement item : driver.findElements(By.xpath(SelectorConstant.LOADBOARD_AUTOCOMPLETE_ITEM))) {
                try {
                    if (!item.isDisplayed()) {
                        continue;
                    }
                    String text = item.getText() == null ? "" : item.getText().replace('\n', ' ').trim();
                    if (text.isBlank() || text.length() > 60 || !text.contains(",")) {
                        continue;
                    }
                    // Evitar nodos padre que agrupan varias ciudades
                    if (text.toLowerCase(Locale.ROOT).chars().filter(c -> c == ',').count() > 1
                            && text.split(",").length > 2) {
                        continue;
                    }
                    if (text.equalsIgnoreCase(exact)) {
                        best = item;
                        break;
                    }
                    String lower = text.toLowerCase(Locale.ROOT);
                    if (!lower.startsWith(query)) {
                        continue;
                    }
                    if (state != null && lower.endsWith(", " + state.toLowerCase(Locale.ROOT))) {
                        best = item;
                        break;
                    }
                    if (best == null) {
                        best = item;
                    }
                } catch (Exception ignored) {
                    // stale
                }
            }
            if (best != null) {
                LOGGER.info("Sugerencia elegida (rápida): {}", safe(best.getText()).replace('\n', ' ').trim());
                if (clicSugerencia(driver, best)) {
                    pauseMs(350);
                    return;
                }
                pauseMs(150);
                continue;
            }
            pauseMs(80);
        }

        LOGGER.warn("Sin clic en sugerencia para '{}'; ARROW_DOWN+ENTER.", expectedCity);
        new Actions(driver).sendKeys(Keys.ARROW_DOWN)
                .pause(java.time.Duration.ofMillis(80))
                .sendKeys(Keys.ENTER)
                .perform();
        pauseMs(250);
    }

    private static String estadoDe(String cityState) {
        int comma = cityState.lastIndexOf(',');
        if (comma < 0 || comma >= cityState.length() - 1) {
            return null;
        }
        return cityState.substring(comma + 1).trim();
    }

    /**
     * Fallback genérico por atributos / posición (por si cambian data-cy).
     */
    private WebElement encontrarCampoFlexible(WebDriver driver, boolean origin) {
        String key = origin ? "origin" : "destination";
        List<String> hints = origin
                ? List.of("type your origin", "your origin", "origin")
                : List.of("find the best destination", "best destination", "destination");

        for (WebElement el : driver.findElements(By.cssSelector("input, textarea, [contenteditable='true']"))) {
            try {
                if (!el.isDisplayed() || !el.isEnabled()) {
                    continue;
                }
                String blob = (safe(el.getAttribute("placeholder")) + " "
                        + safe(el.getAttribute("aria-label")) + " "
                        + safe(el.getAttribute("name")) + " "
                        + safe(el.getAttribute("id")) + " "
                        + safe(el.getAttribute("data-cy")) + " "
                        + safe(el.getAttribute("data-testid"))).toLowerCase(Locale.ROOT);
                for (String hint : hints) {
                    if (blob.contains(hint)) {
                        LOGGER.info("Campo {} localizado por atributo ('{}'): {}", key, hint, summarize(el));
                        return el;
                    }
                }
            } catch (Exception ignored) {
                // stale
            }
        }
        return null;
    }

    private WebElement encontrarBotonBuscar(WebDriver driver) {
        List<By> candidates = List.of(
                By.xpath(SelectorConstant.LOADBOARD_SEARCH_BUTTON),
                By.cssSelector("[data-cy='search-bar'] div.rounded-full"),
                By.xpath("//div[contains(@class,'rounded-full') and contains(@class,'00A065')]"),
                By.xpath("//button[contains(@aria-label,'Search') or contains(@aria-label,'search')]")
        );
        for (By by : candidates) {
            WebElement btn = primerVisible(driver, by);
            if (btn != null) {
                LOGGER.info("Botón búsqueda localizado: tag={}", btn.getTagName());
                return btn;
            }
        }
        return null;
    }

    private void esperarResultados(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        long deadline = System.currentTimeMillis() + 45_000L;
        while (System.currentTimeMillis() < deadline) {
            String url = driver.getCurrentUrl();
            if (url != null && url.contains("/loads/results")) {
                LOGGER.info("Resultados de cargas cargados: {}", url);
                return;
            }
            for (WebElement el : driver.findElements(By.xpath(SelectorConstant.LOADBOARD_RESULTS_LABEL))) {
                try {
                    if (el.isDisplayed()) {
                        LOGGER.info("Etiqueta de resultados visible: {}", el.getText());
                        return;
                    }
                } catch (Exception ignored) {
                    // stale
                }
            }
            pauseMs(500);
        }
        DiagnosticDump.savePage(actor, "dom-loadboard-sin-resultados.html");
        throw new AssertionError(
                "Tras buscar cargas no se llegó a resultados. URL: " + driver.getCurrentUrl());
    }

    private static String shortQuery(String cityState) {
        int comma = cityState.indexOf(',');
        return comma > 0 ? cityState.substring(0, comma).trim() : cityState.trim();
    }

    private void clearField(WebDriver driver, WebElement field) {
        try {
            field.clear();
        } catch (Exception ignored) {
            // ignore
        }
        try {
            field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    field);
        }
    }

    /** El dropdown de sugerencias se re-renderiza mientras se escribe: el stale no debe romper el test. */
    private boolean clickJs(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", element);
            return true;
        } catch (StaleElementReferenceException e) {
            LOGGER.warn("clickJs: elemento stale (el dropdown se refrescó); se reintenta.");
            return false;
        }
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String summarize(WebElement el) {
        return "tag=" + el.getTagName()
                + " ph='" + safe(el.getAttribute("placeholder")) + "'"
                + " data-cy='" + safe(el.getAttribute("data-cy")) + "'";
    }

    private void pauseMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static SearchLoadsForm withRandomCities() {
        LoadSearchCities pair = LoadSearchCities.randomPair();
        return new SearchLoadsForm(pair.getOrigin(), pair.getDestination());
    }

    public static SearchLoadsForm withCities(String origin, String destination) {
        return new SearchLoadsForm(origin, destination);
    }
}
