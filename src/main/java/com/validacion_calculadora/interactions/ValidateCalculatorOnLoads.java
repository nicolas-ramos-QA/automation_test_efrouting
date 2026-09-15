package com.validacion_calculadora.interactions;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;
import com.validacion_calculadora.models.CalculatorValidationResult;
import com.validacion_calculadora.userinterface.SelectorConstant;
import com.validacion_calculadora.utils.CalculatorFinancialParser;
import com.validacion_calculadora.utils.CalculatorFormulaAssertions;
import com.validacion_calculadora.utils.CalculatorModalText;
import com.validacion_calculadora.utils.CalculatorProfitParser;
import com.validacion_calculadora.utils.CalculatorTestReport;
import com.validacion_calculadora.utils.DiagnosticDump;
import com.validacion_calculadora.utils.GreenRateIndicator;
import com.validacion_calculadora.utils.LoadDetailsGreenText;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Flujo por carga (después de la espera de 15 s y resultados con punto verde).
 * Tope duro: {@link #OVERALL_TIMEOUT_MS}. Si no valida a tiempo, falla el escenario.
 */
public class ValidateCalculatorOnLoads implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateCalculatorOnLoads.class);

    public static final String REMEMBER_RESULT = "calculator.validation.result";

    /**
     * Tiempo máximo total del paso de abrir cargas / validar calculadora.
     * Ajustable con {@code -Dcalculadora.timeout.min}: hay días en que casi todas las
     * cargas verdes traen aviso rojo y hacen falta varias re-búsquedas.
     */
    private static final long OVERALL_TIMEOUT_MS =
            Long.getLong("calculadora.timeout.min", 10L) * 60_000L;

    private static final int MAX_LOADS_TO_TRY = 40;
    private static final int MAX_SCROLL_SIN_NUEVA = 3;
    private static final int MAX_SCROLLS_LISTA = 6;
    /** Cargas verdes a revisar por par de ciudades antes de cambiar origen/destino. */
    private static final int VERDES_POR_BUSQUEDA = 5;
    private static final int MAX_RE_BUSQUEDAS = 6;
    private static final long WAIT_DETAILS_MS = 6_000L;
    private static final long WAIT_CALC_MS = 8_000L;

    private int reBusquedas;

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        long deadline = System.currentTimeMillis() + OVERALL_TIMEOUT_MS;
        actor.attemptsTo(Pause.forSeconds(1));

        Set<String> yaProbadas = new LinkedHashSet<>();
        int intentosUtiles = 0;
        int scrollsSinNueva = 0;
        int rojasEnEsteParDeCiudades = 0;

        try {
            while (intentosUtiles < MAX_LOADS_TO_TRY) {
                failIfTimedOut(deadline, intentosUtiles, scrollsSinNueva);

                cerrarModalesHastaQueDesaparezcan(driver);

                if (hayAlgunModal(driver)) {
                    LOGGER.warn("Modal residual; se fuerza Escape/backdrop antes de seguir.");
                    forzarCierreTotal(driver);
                }

                // 5 verdes seguidas con aviso rojo: este par de ciudades no sirve
                if (rojasEnEsteParDeCiudades >= VERDES_POR_BUSQUEDA) {
                    if (!buscarConOtrasCiudades(actor, yaProbadas, deadline)) {
                        break;
                    }
                    rojasEnEsteParDeCiudades = 0;
                }

                // Buscar la fila verde y hacer clic en la MISMA llamada JS (lista virtualizada)
                String huella = GreenRateIndicator.clickNextGreenRow(driver, yaProbadas);
                if (huella == null) {
                    huella = buscarMasVerdes(actor, driver, yaProbadas, deadline, intentosUtiles);
                    if (huella == null) {
                        scrollsSinNueva++;
                        if (scrollsSinNueva >= MAX_SCROLL_SIN_NUEVA) {
                            throw new AssertionError(
                                    "No quedan cargas con punto verde por probar. Cargas abiertas="
                                            + intentosUtiles + ". Estado: "
                                            + GreenRateIndicator.diagnose(driver));
                        }
                        continue;
                    }
                }
                scrollsSinNueva = 0;
                yaProbadas.add(huella);
                intentosUtiles++;
                LOGGER.info("=== Carga #{} / {} (punto verde) === {} ({} s restantes)",
                        intentosUtiles, MAX_LOADS_TO_TRY, acortar(huella),
                        Math.max(0, (deadline - System.currentTimeMillis()) / 1000));
                pauseMs(700);

                if (!esperarVisible(driver, SelectorConstant.LOAD_DETAILS_TITLE, WAIT_DETAILS_MS)) {
                    LOGGER.warn("Carga #{}: no abrió Load details → descartar.", intentosUtiles);
                    descartarYScrollear(driver);
                    continue;
                }
                LOGGER.info("Load details abierto.");
                pauseMs(500);

                if (!LoadDetailsGreenText.hasGreenNegotiateOrRpm(driver)) {
                    rojasEnEsteParDeCiudades++;
                    LOGGER.warn(
                            "Carga #{}: texto ROJO en Load details ({}/{} de este par de ciudades)"
                                    + " → cerrar y otra con punto verde.",
                            intentosUtiles, rojasEnEsteParDeCiudades, VERDES_POR_BUSQUEDA);
                    descartarYScrollear(driver);
                    continue;
                }
                rojasEnEsteParDeCiudades = 0;
                LOGGER.info("Aviso VERDE en Load details; se abre Calculate operation.");

                WebElement calcOp = esperarBoton(driver, SelectorConstant.CALCULATE_OPERATION_BUTTON, 5_000L);
                if (calcOp == null) {
                    LOGGER.warn("Carga #{}: sin Calculate operation → cerrar + scroll.", intentosUtiles);
                    descartarYScrollear(driver);
                    continue;
                }

                LOGGER.info("Clic en Calculate operation…");
                clickSeguro(driver, calcOp);
                pauseMs(900);

                if (!esperarCualquiera(driver, WAIT_CALC_MS,
                        SelectorConstant.CALCULATE_PROFIT_TITLE,
                        SelectorConstant.CURRENT_PROFIT_LABEL)) {
                    LOGGER.warn("Carga #{}: no abrió Calculate profit → cerrar + scroll.", intentosUtiles);
                    descartarYScrollear(driver);
                    continue;
                }
                LOGGER.info("Modal Calculate profit abierto; pausa visual…");
                pauseMs(1_500L);
                LOGGER.info("Validando Current profit…");

                String modalText = CalculatorModalText.extract(driver);
                if (modalText.isBlank()) {
                    modalText = textoModalCalculadora(driver);
                }
                CalculatorValidationResult result = CalculatorProfitParser.parseIfValid(modalText);
                if (result == null) {
                    result = CalculatorProfitParser.parseIfValid(
                            driver.findElement(By.tagName("body")).getText());
                }

                if (result == null) {
                    LOGGER.warn(
                            "Carga #{}: sin ciudades/% → cerrar y scrollear.",
                            intentosUtiles);
                    descartarYScrollear(driver);
                    continue;
                }

                LOGGER.info("Current profit OK: {}", result);
                CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(modalText);
                if (metrics == null) {
                    LOGGER.warn(
                            "Parse financiero falló. Primeros 400 chars del modal: {}",
                            modalText.length() > 400 ? modalText.substring(0, 400) : modalText);
                    DiagnosticDump.saveText("modal-calculate-profit-sin-metricas.txt", modalText);
                    String alt = textoModalCalculadora(driver);
                    if (!alt.isBlank() && !alt.equals(modalText)) {
                        metrics = CalculatorFinancialParser.parse(alt);
                    }
                }
                if (metrics == null) {
                    throw new AssertionError(
                            "Current profit OK pero no se pudieron leer Income/Distance/RPM del modal. "
                                    + "Texto en target/diagnostico/modal-calculate-profit-sin-metricas.txt");
                }

                if (!CalculatorFormulaAssertions.matchesIncomeDividedByDistance(metrics)) {
                    LOGGER.warn(
                            "Carga #{}: fórmula RPM no cuadra ({} ) → cerrar y otra carga.",
                            intentosUtiles, metrics);
                    DiagnosticDump.saveText(
                            "modal-formula-no-cuadra-" + intentosUtiles + ".txt", modalText);
                    descartarYScrollear(driver);
                    continue;
                }
                if (!CalculatorFormulaAssertions.matchesIncomeDividedByDays(metrics)) {
                    LOGGER.warn(
                            "Carga #{}: fórmula Income per day no cuadra ({} ) → cerrar y otra carga.",
                            intentosUtiles, metrics);
                    DiagnosticDump.saveText(
                            "modal-income-per-day-no-cuadra-" + intentosUtiles + ".txt", modalText);
                    descartarYScrollear(driver);
                    continue;
                }
                if (!CalculatorFormulaAssertions.matchesIncomeMinusCosts(metrics)) {
                    LOGGER.warn(
                            "Carga #{}: fórmula Current profit no cuadra ({} ) → cerrar y otra carga.",
                            intentosUtiles, metrics);
                    DiagnosticDump.saveText(
                            "modal-current-profit-no-cuadra-" + intentosUtiles + ".txt", modalText);
                    descartarYScrollear(driver);
                    continue;
                }
                if (!CalculatorFormulaAssertions.matchesProfitPerMile(metrics)) {
                    LOGGER.warn(
                            "Carga #{}: fórmula Profit per mile no cuadra ({} ) → cerrar y otra carga.",
                            intentosUtiles, metrics);
                    DiagnosticDump.saveText(
                            "modal-profit-per-mile-no-cuadra-" + intentosUtiles + ".txt", modalText);
                    descartarYScrollear(driver);
                    continue;
                }
                if (!CalculatorFormulaAssertions.matchesProfitPercent(metrics)) {
                    LOGGER.warn(
                            "Carga #{}: fórmula Profit % no cuadra ({} ) → cerrar y otra carga.",
                            intentosUtiles, metrics);
                    DiagnosticDump.saveText(
                            "modal-profit-percent-no-cuadra-" + intentosUtiles + ".txt", modalText);
                    descartarYScrollear(driver);
                    continue;
                }
                CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
                CalculatorFormulaAssertions.assertIncomeDividedByDaysEqualsIncomePerDay(metrics);
                CalculatorFormulaAssertions.assertIncomeMinusCostsEqualsProfit(metrics);
                CalculatorFormulaAssertions.assertProfitDividedByTotalDistanceEqualsProfitPerMile(metrics);
                CalculatorFormulaAssertions.assertProfitOverIncomeEqualsProfitPercent(metrics);
                result = result.withFinancialMetrics(metrics);

                LOGGER.info("Validación completa en carga #{}: {}", intentosUtiles, result);
                actor.remember(REMEMBER_RESULT, result);

                String origin = safeRecall(actor, "load.search.origin");
                String destination = safeRecall(actor, "load.search.destination");
                try {
                    CalculatorTestReport.generateAndOpen(
                            driver, result, origin, destination, intentosUtiles);
                } catch (Exception e) {
                    LOGGER.warn("No se pudo generar/abrir el reporte: {}", e.getMessage());
                }
                return;
            }
        } catch (org.openqa.selenium.WebDriverException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase(Locale.ROOT).contains("invalid session")) {
                throw new AssertionError(
                        "Chrome cerró la sesión antes de validar la calculadora "
                                + "(límite " + (OVERALL_TIMEOUT_MS / 1000) + " s / scrolls sin verdes). "
                                + "Último intento útil=" + intentosUtiles + ". Causa: " + e.getMessage(),
                        e);
            }
            throw e;
        }

        throw new AssertionError(
                "Tras probar " + intentosUtiles
                        + " cargas en ≤" + (OVERALL_TIMEOUT_MS / 1000)
                        + " s, ninguna cumplió Current profit + fórmula Income/Distance=RPM.");
    }

    private void failIfTimedOut(long deadline, int intentosUtiles, int scrollsSinNueva) {
        long left = deadline - System.currentTimeMillis();
        if (left <= 0) {
            throw new AssertionError(
                    "Automatización fallida: pasaron "
                            + (OVERALL_TIMEOUT_MS / 1000)
                            + " s sin validar una carga con Current profit + fórmula OK. "
                            + "Cargas intentadas=" + intentosUtiles
                            + ", scrolls sin verde nueva=" + scrollsSinNueva + ".");
        }
    }

    /** Cierra modales y deja la lista lista para la siguiente carga verde (sin perder posición). */
    private void descartarYScrollear(WebDriver driver) {
        cerrarModalesHastaQueDesaparezcan(driver);
        if (hayAlgunModal(driver)) {
            forzarCierreTotal(driver);
        }
        GreenRateIndicator.dismissOverlaysIfAny(driver);
    }

    /**
     * No hay verde nueva en lo renderizado: scrollea la lista virtualizada para traer más cargas y,
     * si aun así se agotan, repite la búsqueda con otras ciudades.
     */
    private String buscarMasVerdes(Actor actor, WebDriver driver, Set<String> yaProbadas,
                                   long deadline, int intentosUtiles) {
        for (int s = 1; s <= MAX_SCROLLS_LISTA; s++) {
            failIfTimedOut(deadline, intentosUtiles, s);
            GreenRateIndicator.scrollListDown(driver);
            String huella = GreenRateIndicator.clickNextGreenRow(driver, yaProbadas);
            if (huella != null) {
                LOGGER.info("Carga verde nueva encontrada tras scroll #{}", s);
                return huella;
            }
        }

        GreenRateIndicator.scrollListToTop(driver);
        String huella = GreenRateIndicator.clickNextGreenRow(driver, yaProbadas);
        if (huella != null) {
            return huella;
        }

        LOGGER.info("Sin más cargas con punto verde ({} probadas).", yaProbadas.size());
        if (!buscarConOtrasCiudades(actor, yaProbadas, deadline)) {
            return null;
        }
        return GreenRateIndicator.clickNextGreenRow(driver, yaProbadas);
    }

    /**
     * Repite la búsqueda con otro origen/destino y reinicia las cargas probadas.
     *
     * @return {@code false} si ya no quedan reintentos o se acabó el tiempo.
     */
    private boolean buscarConOtrasCiudades(Actor actor, Set<String> yaProbadas, long deadline) {
        if (reBusquedas >= MAX_RE_BUSQUEDAS || System.currentTimeMillis() > deadline) {
            return false;
        }
        reBusquedas++;
        LOGGER.info("Cambiando origen/destino (búsqueda #{} de {})…", reBusquedas, MAX_RE_BUSQUEDAS);
        actor.attemptsTo(SearchLoadsUntilGreenRate.withRandomCities());
        yaProbadas.clear();
        return true;
    }

    private boolean cerrarModalesHastaQueDesaparezcan(WebDriver driver) {
        if (!hayAlgunModal(driver)) {
            return true;
        }
        LOGGER.info("Cerrando modales…");

        if (hayCalculateProfit(driver)) {
            WebElement goBack = primerVisible(driver, By.xpath(SelectorConstant.CALCULATE_GO_BACK));
            if (goBack != null) {
                LOGGER.info("Calculate profit → Go back");
                clickSeguro(driver, goBack);
                pauseMs(700);
            } else {
                clicXLoadDetails(driver);
                pauseMs(400);
            }
            esperarQueDesaparezcaTitulo(driver, "Calculate profit", 2_500L);
        }

        // Escape primero: el drawer es un dialog y responde a ESC sin buscar la X
        for (int i = 0; i < 3 && hayLoadDetails(driver); i++) {
            LOGGER.info("Load details → cerrar (intento {})", i + 1);
            enviarEscape(driver);
            if (!hayLoadDetails(driver)) {
                break;
            }
            clicXLoadDetails(driver);
            pauseMs(350);
        }

        if (hayAlgunModal(driver)) {
            forzarCierreTotal(driver);
        }

        boolean limpio = !hayAlgunModal(driver);
        LOGGER.info(limpio ? "Lista libre de modales." : "Aún hay modal visible.");
        return limpio;
    }

    private void forzarCierreTotal(WebDriver driver) {
        for (int i = 0; i < 2 && hayAlgunModal(driver); i++) {
            enviarEscape(driver);
            if (!hayAlgunModal(driver)) {
                return;
            }
            clicXLoadDetails(driver);
            clicBackdrop(driver);
            pauseMs(300);
        }
    }

    /**
     * Clic en la X del header de Load details (esquina superior derecha).
     * Preferimos el último botón del contenedor del título; evita íconos del mapa/footer.
     */
    private boolean clicXLoadDetails(WebDriver driver) {
        if (clicXPorJs(driver)) {
            return true;
        }
        return clicXPorSelenium(driver);
    }

    private boolean clicXPorSelenium(WebDriver driver) {
        List<By> candidatos = List.of(
                By.xpath("//*[normalize-space()='Load details']/following-sibling::button[1]"),
                By.xpath("//*[normalize-space()='Load details']/parent::*/*[last()]"
                        + "[self::button or @role='button']"),
                By.xpath("//*[normalize-space()='Load details']/parent::*"
                        + "//button[@aria-label='Close' or @aria-label='close' or contains(@aria-label,'Close')]"),
                By.xpath("//*[normalize-space()='Load details']/ancestor::*[position()<=3]"
                        + "//button[@aria-label='Close' or @aria-label='close']"),
                By.xpath("//*[normalize-space()='Load details']/ancestor::*[position()<=3]"
                        + "//button[.//*[local-name()='svg']][last()]"),
                By.cssSelector("button[aria-label='Close'], button[aria-label='close']"),
                By.xpath("//*[normalize-space()='Calculate profit']/following-sibling::button[1]")
        );

        for (By by : candidatos) {
            WebElement close = primerVisible(driver, by);
            if (close == null) {
                continue;
            }
            String txt = safe(close.getText()).toLowerCase(Locale.ROOT);
            if (txt.contains("book") || txt.contains("calculate") || txt.contains("go back")) {
                continue;
            }
            LOGGER.info("X Selenium (aria={}, text='{}')",
                    safe(close.getAttribute("aria-label")), txt);
            if (clickSeguro(driver, close)) {
                return true;
            }
        }
        return false;
    }

    /**
     * JS: la X real del drawer es el icono pequeño y sin texto más a la derecha de la
     * franja superior del panel. Buscarla por "último botón del header" clicaba Hide Map.
     */
    private boolean clicXPorJs(WebDriver driver) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var titles = document.querySelectorAll('h1,h2,h3,h4,p,div,span');"
                            + "var title = null;"
                            + "for (var i = 0; i < titles.length; i++) {"
                            + "  var t = (titles[i].textContent || '').trim();"
                            + "  if (t !== 'Load details' && t !== 'Calculate profit') continue;"
                            + "  var r = titles[i].getBoundingClientRect();"
                            + "  if (r.width > 0 && r.height > 0 && r.height < 80) { title = titles[i]; break; }"
                            + "}"
                            + "if (!title) return 'no-title';"
                            + "var drawer = title;"
                            + "while (drawer && drawer.getBoundingClientRect().height < 300) {"
                            + "  drawer = drawer.parentElement;"
                            + "}"
                            + "if (!drawer) drawer = document.body;"
                            + "var dr = drawer.getBoundingClientRect();"
                            + "var tr = title.getBoundingClientRect();"
                            + "var cands = drawer.querySelectorAll('button, [role=\"button\"], svg');"
                            + "var best = null, bestRight = -1;"
                            + "for (var b = 0; b < cands.length; b++) {"
                            + "  var el = cands[b];"
                            + "  var r = el.getBoundingClientRect();"
                            + "  if (r.width <= 0 || r.height <= 0 || r.width > 60 || r.height > 60) continue;"
                            + "  if ((el.textContent || '').trim().length > 2) continue;"
                            + "  if (r.top > tr.top + 60 || r.bottom < dr.top) continue;"
                            + "  if (r.right > bestRight) { bestRight = r.right; best = el; }"
                            + "}"
                            + "if (!best) return 'no-btn';"
                            + "var clickable = best.closest ? (best.closest('button') || best) : best;"
                            + "clickable.click();"
                            + "return 'clicked';"
            );
            LOGGER.info("X JS drawer -> {}", result);
            return "clicked".equals(String.valueOf(result));
        } catch (Exception e) {
            LOGGER.warn("X JS fallo: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clic en el backdrop del drawer. Solo si el punto es realmente un overlay:
     * clicar a ciegas puede caer en el menú lateral y navegar fuera de Loadboard.
     */
    private void clicBackdrop(WebDriver driver) {
        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                    "var el = document.elementFromPoint(20, Math.floor(window.innerHeight / 2));"
                            + "if (!el) return 'none';"
                            + "var n = el, overlay = null;"
                            + "for (var d = 0; d < 4 && n; d++) {"
                            + "  var st = window.getComputedStyle(n);"
                            + "  if (st.position === 'fixed' && (n.className || '').toString().indexOf('inset-0') >= 0) {"
                            + "    overlay = n; break;"
                            + "  }"
                            + "  n = n.parentElement;"
                            + "}"
                            + "if (!overlay) return 'no-overlay';"
                            + "overlay.click();"
                            + "return 'clicked';"
            );
            LOGGER.info("Backdrop -> {}", clicked);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private List<WebElement> filasCarga(WebDriver driver) {
        Set<WebElement> unique = new LinkedHashSet<>();
        for (WebElement el : driver.findElements(By.xpath(SelectorConstant.LOAD_RESULT_ROWS))) {
            try {
                if (el.isDisplayed() && el.getSize().getHeight() > 24) {
                    unique.add(el);
                }
            } catch (StaleElementReferenceException ignored) {
                // skip
            }
        }
        return new ArrayList<>(unique);
    }

    private boolean scrollResultados(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<WebElement> rows = filasCarga(driver);
        if (!rows.isEmpty()) {
            try {
                js.executeScript(
                        "arguments[0].scrollIntoView({block:'end'});", rows.get(rows.size() - 1));
                pauseMs(200);
            } catch (StaleElementReferenceException ignored) {
                // ignore
            }
        }
        js.executeScript(
                "var pick=document.querySelector('table tbody')"
                        + "||document.querySelector('[class*=\"results\"]')"
                        + "||document.querySelector('main');"
                        + "var n=pick;"
                        + "while(n&&n!==document.body){"
                        + "  if(n.scrollHeight>n.clientHeight+40){"
                        + "    n.scrollTop+=Math.max(320,Math.floor(n.clientHeight*0.85));"
                        + "    return true;"
                        + "  }"
                        + "  n=n.parentElement;"
                        + "}"
                        + "window.scrollBy(0, Math.floor(window.innerHeight*0.8));"
                        + "return true;");
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.PAGE_DOWN);
        } catch (Exception ignored) {
            // ignore
        }
        pauseMs(600);
        return true;
    }

    private String textoModalCalculadora(WebDriver driver) {
        for (String xpath : List.of(
                "//*[normalize-space()='Calculate profit']/ancestor::*[.//*[contains(.,'Income')]][1]",
                "//*[contains(normalize-space(.),'Current profit')]/ancestor::*[contains(.,'Calculate profit')][1]",
                "//body")) {
            for (WebElement el : driver.findElements(By.xpath(xpath))) {
                try {
                    if (el.isDisplayed()) {
                        String text = el.getText();
                        if (text != null && text.toLowerCase(Locale.ROOT).contains("profit")) {
                            return text;
                        }
                    }
                } catch (StaleElementReferenceException ignored) {
                    // next
                }
            }
        }
        return driver.findElement(By.tagName("body")).getText();
    }

    private boolean clickSeguro(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", element);
            pauseMs(100);
            try {
                element.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
            return true;
        } catch (StaleElementReferenceException e) {
            LOGGER.warn("clickSeguro: elemento stale");
            return false;
        } catch (Exception e) {
            LOGGER.warn("clickSeguro fallo: {}", e.getMessage());
            return false;
        }
    }

    private void enviarEscape(WebDriver driver) {
        try {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            pauseMs(250);
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            pauseMs(200);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private boolean hayAlgunModal(WebDriver driver) {
        return hayLoadDetails(driver) || hayCalculateProfit(driver);
    }

    private boolean hayLoadDetails(WebDriver driver) {
        return hayTituloExactoVisible(driver, "Load details");
    }

    private boolean hayCalculateProfit(WebDriver driver) {
        return hayTituloExactoVisible(driver, "Calculate profit");
    }

    /**
     * Detección por JS: el XPath {@code //*[normalize-space()='…']} recorre todo el DOM y
     * cuesta segundos en esta pantalla; aquí es una sola llamada de milisegundos.
     */
    private boolean hayTituloExactoVisible(WebDriver driver, String title) {
        try {
            Object visible = ((JavascriptExecutor) driver).executeScript(
                    "var title = arguments[0];"
                            + "var nodes = document.querySelectorAll('h1,h2,h3,h4,p,span,div');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  if ((nodes[i].textContent || '').trim() !== title) continue;"
                            + "  var r = nodes[i].getBoundingClientRect();"
                            + "  if (r.width > 0 && r.height > 0 && r.height < 100) return true;"
                            + "}"
                            + "return false;",
                    title);
            return Boolean.TRUE.equals(visible);
        } catch (org.openqa.selenium.NoSuchSessionException e) {
            throw e;
        } catch (Exception e) {
            return hayTituloExactoVisibleSelenium(driver, title);
        }
    }

    private boolean hayTituloExactoVisibleSelenium(WebDriver driver, String title) {
        for (WebElement el : driver.findElements(By.xpath("//*[normalize-space()='" + title + "']"))) {
            try {
                if (!el.isDisplayed()) {
                    continue;
                }
                int h = el.getSize().getHeight();
                if (h > 0 && h < 100) {
                    return true;
                }
            } catch (StaleElementReferenceException ignored) {
                // next
            }
        }
        return false;
    }

    private void esperarQueDesaparezcaTitulo(WebDriver driver, String title, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (!hayTituloExactoVisible(driver, title)) {
                return;
            }
            pauseMs(200);
        }
    }

    private boolean esperarVisible(WebDriver driver, String xpath, long timeoutMs) {
        return esperarCualquiera(driver, timeoutMs, xpath);
    }

    private boolean esperarCualquiera(WebDriver driver, long timeoutMs, String... xpaths) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            for (String xp : xpaths) {
                if (primerVisible(driver, By.xpath(xp)) != null) {
                    return true;
                }
            }
            pauseMs(250);
        }
        return false;
    }

    private WebElement esperarBoton(WebDriver driver, String xpath, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            WebElement el = primerVisible(driver, By.xpath(xpath));
            if (el != null) {
                return el;
            }
            pauseMs(200);
        }
        return null;
    }

    private WebElement primerVisible(WebDriver driver, By by) {
        for (WebElement el : driver.findElements(by)) {
            try {
                if (el.isDisplayed() && el.isEnabled()) {
                    return el;
                }
            } catch (StaleElementReferenceException ignored) {
                // next
            }
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeRecall(Actor actor, String key) {
        try {
            Object v = actor.recall(key);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception e) {
            return "";
        }
    }

    private static String acortar(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace('|', ' ').trim();
        return t.length() <= 80 ? t : t.substring(0, 80) + "…";
    }

    private void pauseMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ValidateCalculatorOnLoads untilCurrentProfitIsVisible() {
        return new ValidateCalculatorOnLoads();
    }
}
