package com.filtros_de_rutas.interactions;

import com.filtros_de_rutas.models.FilterCheck;
import com.filtros_de_rutas.models.RouteFooterTotals;
import com.filtros_de_rutas.models.RouteRowSample;
import com.filtros_de_rutas.userinterface.SelectorConstant;
import com.filtros_de_rutas.utils.DiagnosticDump;
import com.filtros_de_rutas.utils.FiltersTestReport;
import com.filtros_de_rutas.utils.JsonTextSelector;
import com.filtros_de_rutas.utils.RouteFooterParser;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Aplica uno a uno los filtros del panel Filter y comprueba que el pie de
 * totales cambia. Tras cada filtro hace Reset y espera a que vuelvan los valores.
 */
public class ValidateFiltersOnRoutes implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateFiltersOnRoutes.class);

    public static final String REMEMBER_CHECKS = "filtros.checks";
    public static final String REMEMBER_BASELINE = "filtros.baseline";

    private static final long WAIT_FOOTER_MS = 20_000L;
    private static final long WAIT_RESTORE_MS = 20_000L;

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        esperarListado(driver);

        RouteFooterTotals baseline = esperarFooterLegible(driver, 25_000L);
        if (baseline == null || !baseline.isReadable()) {
            DiagnosticDump.savePage(actor, "filtros-sin-footer.html");
            throw new AssertionError(
                    "No se pudieron leer los totales del pie (Total routes / Results). "
                            + "DOM en target/diagnostico/filtros-sin-footer.html");
        }
        LOGGER.info("Totales iniciales: {}", baseline.snapshot());
        actor.remember(REMEMBER_BASELINE, baseline);

        RouteRowSample sample = extraerPrimeraFila(driver);
        LOGGER.info("Valores de la primera fila: {}", sample.asMap());

        List<FilterCheck> checks = new ArrayList<>();
        List<String> fields = JsonTextSelector.filterFields();

        for (String field : fields) {
            checks.add(validarFiltro(actor, driver, field, sample, baseline));
        }

        actor.remember(REMEMBER_CHECKS, checks);
        try {
            FiltersTestReport.generateAndOpen(driver, baseline, checks);
        } catch (Exception e) {
            LOGGER.warn("No se pudo generar/abrir el reporte: {}", e.getMessage());
        }

        long fallos = checks.stream().filter(c -> c.getStatus() == FilterCheck.Status.FAIL).count();
        if (fallos > 0) {
            throw new AssertionError(
                    fallos + " filtro(s) no cambiaron los totales del pie. "
                            + "Detalle en target/reportes/reporte-filtros-ultimo.html");
        }
    }

    private FilterCheck validarFiltro(Actor actor, WebDriver driver, String field,
                                      RouteRowSample sample, RouteFooterTotals baseline) {
        String value = sample.get(field);
        LOGGER.info("=== Filtro '{}' con valor '{}' ===", field, value);
        try {
            resetearFiltros(driver);
            esperarTotalesComo(driver, baseline, WAIT_RESTORE_MS);

            boolean applied = aplicarFiltro(driver, field, value);
            if (!applied) {
                DiagnosticDump.savePage(actor, slug(field) + "-no-aplicado.html");
                return new FilterCheck(field, value, baseline, null, null,
                        FilterCheck.Status.FAIL,
                        "No se pudo abrir/rellenar el filtro '" + field + "'");
            }

            RouteFooterTotals after = esperarCambio(driver, baseline, WAIT_FOOTER_MS);
            if (after == null || !after.differsFrom(baseline)) {
                DiagnosticDump.saveText(slug(field) + "-sin-cambio.txt",
                        after == null ? "footer no leído" : after.snapshot());
                DiagnosticDump.savePage(actor, slug(field) + "-sin-cambio.html");
                resetearFiltros(driver);
                return new FilterCheck(field, value, baseline, after, null,
                        FilterCheck.Status.FAIL,
                        "El pie no cambió tras aplicar '" + field + "'"
                                + (value == null ? "" : " = " + value)
                                + ". Baseline: " + baseline.snapshot()
                                + (after == null ? "" : " | Tras filtro: " + after.snapshot()));
            }

            LOGGER.info("Pie cambió con '{}': {}", field, after.snapshot());
            resetearFiltros(driver);
            RouteFooterTotals restored = esperarTotalesComo(driver, baseline, WAIT_RESTORE_MS);
            boolean restoredOk = restored != null && !restored.differsFrom(baseline);
            String detail = "Totales cambiaron. Antes: " + baseline.snapshot()
                    + " | Después: " + after.snapshot()
                    + (restoredOk ? " | Reset restauró el pie." : " | Reset no restauró del todo.");
            return new FilterCheck(field, value, baseline, after, restored,
                    FilterCheck.Status.PASS, detail);
        } catch (RuntimeException e) {
            LOGGER.warn("Filtro '{}' falló: {}", field, e.getMessage());
            DiagnosticDump.savePage(actor, slug(field) + "-error.html");
            try {
                resetearFiltros(driver);
            } catch (Exception ignored) {
                // continue
            }
            return new FilterCheck(field, value, baseline, null, null,
                    FilterCheck.Status.FAIL, "Error al aplicar '" + field + "': " + e.getMessage());
        }
    }

    private boolean aplicarFiltro(WebDriver driver, String field, String value) {
        abrirPanelFiltro(driver);
        if (!clickCampoFiltro(driver, field)) {
            LOGGER.warn("No se encontró el campo de filtro '{}'", field);
            return false;
        }
        pauseMs(600);

        if (esFecha(field)) {
            if (rellenarFecha(driver, value)) {
                cerrarPanel(driver);
                return true;
            }
        }

        if (esNumerico(field)) {
            if (rellenarNumerico(driver, value)) {
                cerrarPanel(driver);
                return true;
            }
        }

        if (value != null && !value.isBlank()) {
            if (elegirOpcion(driver, value) || escribirValor(driver, value)) {
                cerrarPanel(driver);
                return true;
            }
        }

        if (elegirPrimeraOpcion(driver)) {
            cerrarPanel(driver);
            return true;
        }

        LOGGER.warn("Sin input/opción usable para '{}'", field);
        return false;
    }

    private void abrirPanelFiltro(WebDriver driver) {
        if (panelAbierto(driver)) {
            return;
        }
        WebElement button = primerVisible(driver, By.xpath(SelectorConstant.FILTER_BUTTON));
        if (button == null) {
            clickPorTexto(driver, List.of("Filter", "Filters"));
        } else {
            clickSeguro(driver, button);
        }
        pauseMs(500);
        if (!panelAbierto(driver)) {
            clickPorTexto(driver, List.of("Filter", "Filters"));
            pauseMs(400);
        }
    }

    private boolean panelAbierto(WebDriver driver) {
        return hayTextoExacto(driver, "Route name") && hayTextoExacto(driver, JsonTextSelector.FILTER_RESET_NAME);
    }

    private boolean clickCampoFiltro(WebDriver driver, String field) {
        for (int i = 0; i < 10; i++) {
            String xpath = String.format(SelectorConstant.FILTER_FIELD_TEMPLATE, field);
            WebElement el = primerVisible(driver, By.xpath(xpath));
            if (el != null) {
                return clickSeguro(driver, el);
            }
            if (clickPorTexto(driver, List.of(field))) {
                return true;
            }
            if (!scrollPanelFiltro(driver, 120)) {
                break;
            }
            pauseMs(200);
        }
        scrollPanelFiltroArriba(driver);
        return clickPorTexto(driver, List.of(field));
    }

    private boolean scrollPanelFiltro(WebDriver driver, int delta) {
        Object moved = js(driver).executeScript(
                "var reset = arguments[0];"
                        + "var nodes = document.querySelectorAll('button,div,span');"
                        + "var start = null;"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  if ((nodes[i].textContent || '').replace(/\\s+/g,' ').trim() !== reset) continue;"
                        + "  var r = nodes[i].getBoundingClientRect();"
                        + "  if (r.width > 0 && r.height > 0 && r.height < 50) { start = nodes[i]; break; }"
                        + "}"
                        + "if (!start) return false;"
                        + "var panel = start;"
                        + "while (panel && panel !== document.body) {"
                        + "  if (panel.scrollHeight > panel.clientHeight + 20) {"
                        + "    var before = panel.scrollTop;"
                        + "    panel.scrollTop += arguments[1];"
                        + "    return panel.scrollTop !== before;"
                        + "  }"
                        + "  panel = panel.parentElement;"
                        + "}"
                        + "return false;",
                JsonTextSelector.FILTER_RESET_NAME, delta);
        return Boolean.TRUE.equals(moved);
    }

    private void scrollPanelFiltroArriba(WebDriver driver) {
        js(driver).executeScript(
                "var reset = arguments[0];"
                        + "var nodes = document.querySelectorAll('button,div,span');"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  if ((nodes[i].textContent || '').replace(/\\s+/g,' ').trim() !== reset) continue;"
                        + "  var panel = nodes[i];"
                        + "  while (panel && panel !== document.body) {"
                        + "    if (panel.scrollHeight > panel.clientHeight + 20) { panel.scrollTop = 0; return; }"
                        + "    panel = panel.parentElement;"
                        + "  }"
                        + "}",
                JsonTextSelector.FILTER_RESET_NAME);
    }

    private void resetearFiltros(WebDriver driver) {
        abrirPanelFiltro(driver);
        WebElement reset = primerVisible(driver, By.xpath(SelectorConstant.RESET_BUTTON));
        if (reset != null) {
            clickSeguro(driver, reset);
            pauseMs(800);
        } else {
            clickPorTexto(driver, List.of(JsonTextSelector.FILTER_RESET_NAME, "Reset"));
            pauseMs(800);
        }
        cerrarPanel(driver);
    }

    private void cerrarPanel(WebDriver driver) {
        if (!panelAbierto(driver)) {
            return;
        }
        try {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            pauseMs(250);
        } catch (Exception ignored) {
            // ignore
        }
        if (panelAbierto(driver)) {
            WebElement button = primerVisible(driver, By.xpath(SelectorConstant.FILTER_BUTTON));
            if (button != null) {
                clickSeguro(driver, button);
                pauseMs(300);
            }
        }
    }

    private boolean escribirValor(WebDriver driver, String value) {
        WebElement input = primerInputVisible(driver);
        if (input == null) {
            return false;
        }
        try {
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);
            input.sendKeys(value);
            pauseMs(400);
            if (!elegirOpcion(driver, value)) {
                input.sendKeys(Keys.ENTER);
            }
            pauseMs(500);
            return true;
        } catch (Exception e) {
            LOGGER.warn("No se pudo escribir '{}': {}", value, e.getMessage());
            return false;
        }
    }

    private boolean rellenarFecha(WebDriver driver, String value) {
        if (value == null || value.isBlank()) {
            return elegirPrimeraOpcion(driver);
        }
        List<String> formatos = formatosFecha(value);
        WebElement input = primerInputVisible(driver);
        if (input != null) {
            for (String formato : formatos) {
                try {
                    input.click();
                    input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                    input.sendKeys(Keys.DELETE);
                    input.sendKeys(formato);
                    pauseMs(300);
                    input.sendKeys(Keys.ENTER);
                    pauseMs(400);
                    return true;
                } catch (Exception e) {
                    LOGGER.debug("Formato de fecha '{}' no aplicó: {}", formato, e.getMessage());
                }
            }
        }
        return elegirOpcion(driver, value) || elegirPrimeraOpcion(driver);
    }

    private boolean elegirOpcion(WebDriver driver, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String needle = value.toLowerCase(Locale.ROOT);
        String script =
                "var needle = arguments[0];"
                        + "var nodes = document.querySelectorAll("
                        + "  'label, [role=\"option\"], [role=\"menuitem\"], li, button, span, div');"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  var el = nodes[i];"
                        + "  var t = (el.textContent || '').replace(/\\s+/g,' ').trim();"
                        + "  if (!t || t.length > 80) continue;"
                        + "  var r = el.getBoundingClientRect();"
                        + "  if (r.width <= 0 || r.height <= 0 || r.height > 80) continue;"
                        + "  if (t.toLowerCase().indexOf(needle) < 0) continue;"
                        + "  if (t.toLowerCase() === 'filter' || t.toLowerCase() === 'reset') continue;"
                        + "  var clickable = el.closest('label,button,[role=\"option\"],[role=\"menuitem\"],li') || el;"
                        + "  clickable.click();"
                        + "  return t;"
                        + "}"
                        + "return null;";
        Object clicked = js(driver).executeScript(script, needle);
        if (clicked != null) {
            LOGGER.info("Opción clicada: {}", clicked);
            pauseMs(400);
            return true;
        }
        return false;
    }

    private boolean elegirPrimeraOpcion(WebDriver driver) {
        String script =
                "var nodes = document.querySelectorAll("
                        + "  'label, [role=\"option\"], [role=\"menuitem\"], li button, [type=\"checkbox\"]');"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  var el = nodes[i];"
                        + "  var r = el.getBoundingClientRect();"
                        + "  if (r.width <= 0 || r.height <= 0) continue;"
                        + "  var t = (el.textContent || '').replace(/\\s+/g,' ').trim().toLowerCase();"
                        + "  if (t === 'filter' || t === 'reset' || t === 'all' || t === 'any') continue;"
                        + "  var clickable = el.closest('label,button,[role=\"option\"],li') || el;"
                        + "  clickable.click();"
                        + "  return (el.textContent || '').trim() || 'opción';"
                        + "}"
                        + "return null;";
        Object clicked = js(driver).executeScript(script);
        if (clicked != null) {
            LOGGER.info("Primera opción clicada: {}", clicked);
            pauseMs(400);
            return true;
        }
        return false;
    }

    private RouteRowSample extraerPrimeraFila(WebDriver driver) {
        RouteRowSample sample = new RouteRowSample();
        Object raw = js(driver).executeScript(
                "var result = {};"
                        + "function txt(el){ return el ? (el.textContent||'').replace(/\\s+/g,' ').trim() : ''; }"
                        + "var headers = [];"
                        + "document.querySelectorAll('table thead th, [role=\"columnheader\"]').forEach(function(h){"
                        + "  var t = txt(h); if (t) headers.push(t);"
                        + "});"
                        + "var row = document.querySelector('table tbody tr')"
                        + "  || document.querySelector('[role=\"rowgroup\"] [role=\"row\"]')"
                        + "  || document.querySelector('[class*=\"row\"]:not(header)');"
                        + "var cells = [];"
                        + "if (row) {"
                        + "  row.querySelectorAll('td, [role=\"cell\"], [role=\"gridcell\"]').forEach(function(c){"
                        + "    var t = txt(c); if (t) cells.push(t);"
                        + "  });"
                        + "  if (cells.length === 0) { var all = txt(row); if (all) cells.push(all); }"
                        + "}"
                        + "result.headers = headers;"
                        + "result.cells = cells;"
                        + "result.rowText = row ? txt(row) : '';"
                        + "return result;"
        );
        if (!(raw instanceof java.util.Map)) {
            return sample;
        }
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) raw;
        List<String> headers = strings(map.get("headers"));
        List<String> cells = strings(map.get("cells"));
        String rowText = String.valueOf(map.getOrDefault("rowText", ""));

        for (int i = 0; i < headers.size() && i < cells.size(); i++) {
            mapHeaderToFilter(sample, headers.get(i), cells.get(i));
        }
        completarDesdeTextoFila(sample, rowText);
        completarDesdeCeldas(sample, cells);
        return sample;
    }

    private void mapHeaderToFilter(RouteRowSample sample, String header, String cell) {
        String h = header.toLowerCase(Locale.ROOT);
        if (h.contains("route name") || h.equals("route") || h.contains("route #")) {
            sample.put("Route name", primeraLinea(cell));
        } else if (h.contains("driver")) {
            sample.put("Driver", primeraLinea(cell));
        } else if (h.contains("unit")) {
            sample.put("Unit", primeraLinea(cell));
        } else if (h.contains("trailer") && !h.contains("type")) {
            sample.put("Trailer", primeraLinea(cell));
        } else if (h.contains("dispatcher")) {
            sample.put("Dispatcher", primeraLinea(cell));
        } else if (h.contains("equipment")) {
            sample.put("Equipment type", primeraLinea(cell));
        } else if (h.contains("origin")) {
            sample.put("Origin", primeraLinea(cell));
        } else if (h.contains("destination")) {
            sample.put("Destination", primeraLinea(cell));
        } else if (h.contains("income")) {
            sample.put("Income", primeraMonto(cell));
        } else if (h.contains("total mile") || h.equals("total miles")) {
            sample.put("Total miles", primeraMillas(cell));
        } else if (h.contains("route plan") || (h.contains("lane") && !h.contains("mileage"))) {
            extraerOrigenDestinoFechas(sample, cell);
        }
    }

    private void completarDesdeTextoFila(RouteRowSample sample, String rowText) {
        extraerOrigenDestinoFechas(sample, rowText);
        if (!sample.has("Route name")) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d{4,}_[A-Za-z0-9._-]+)").matcher(rowText);
            if (m.find()) {
                sample.put("Route name", m.group(1));
            }
        }
        if (!sample.has("Unit")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(\\d{4,6})\\b").matcher(rowText);
            if (m.find()) {
                sample.put("Unit", m.group(1));
            }
        }
        if (!sample.has("Total miles")) {
            String miles = primeraMillas(rowText);
            if (!miles.isBlank()) {
                sample.put("Total miles", miles);
            }
        }
        if (!sample.has("Income")) {
            String income = primeraMonto(rowText);
            if (!income.isBlank()) {
                sample.put("Income", income);
            }
        }
    }

    private void completarDesdeCeldas(RouteRowSample sample, List<String> cells) {
        for (String cell : cells) {
            extraerOrigenDestinoFechas(sample, cell);
            String low = cell.toLowerCase(Locale.ROOT);
            if (!sample.has("Equipment type")
                    && (low.equals("van") || low.equals("reefer") || low.contains("flatbed")
                    || low.contains("tanker") || low.contains("step"))) {
                sample.put("Equipment type", primeraLinea(cell));
            }
            if (!sample.has("Total miles")) {
                String miles = primeraMillas(cell);
                if (!miles.isBlank()) {
                    sample.put("Total miles", miles);
                }
            }
            if (!sample.has("Income") && cell.contains("$")) {
                String income = primeraMonto(cell);
                if (!income.isBlank()) {
                    sample.put("Income", income);
                }
            }
        }
    }

    private void extraerOrigenDestinoFechas(RouteRowSample sample, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        java.util.regex.Matcher arrow = java.util.regex.Pattern
                .compile("([A-Za-z .'-]+?)\\s*(?:→|->|—|-|to)\\s*([A-Za-z .'-]+)")
                .matcher(text);
        if (arrow.find()) {
            if (!sample.has("Origin")) {
                sample.put("Origin", limpiarCiudad(arrow.group(1)));
            }
            if (!sample.has("Destination")) {
                sample.put("Destination", limpiarCiudad(arrow.group(2)));
            }
        }
        java.util.regex.Matcher dates = java.util.regex.Pattern
                .compile("((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{1,2},?\\s*\\d{4})")
                .matcher(text);
        List<String> found = new ArrayList<>();
        while (dates.find()) {
            found.add(dates.group(1));
        }
        if (!found.isEmpty() && !sample.has("Start date")) {
            sample.put("Start date", found.get(0));
        }
        if (found.size() > 1 && !sample.has("End date")) {
            sample.put("End date", found.get(1));
        }
    }

    private String limpiarCiudad(String raw) {
        String city = raw.replaceAll("\\d", " ").replaceAll("\\s+", " ").trim();
        if (city.contains(",")) {
            return city;
        }
        return city.replaceAll("(?i)\\b(planned|unbooked|in progress|completed|assigned)\\b", "").trim();
    }

    private RouteFooterTotals leerFooter(WebDriver driver) {
        String body = "";
        try {
            body = driver.findElement(By.tagName("body")).getText();
        } catch (Exception e) {
            LOGGER.warn("No se pudo leer body: {}", e.getMessage());
        }
        RouteFooterTotals parsed = RouteFooterParser.parse(body);
        if (parsed.isReadable()) {
            return parsed;
        }
        Object jsText = js(driver).executeScript(
                "var labels=['Total routes','Total income','Total miles','DH miles','Effective RPM','Loaded RPM','Results'];"
                        + "var out=[];"
                        + "var nodes=document.querySelectorAll('footer, [class*=\"footer\"], [class*=\"total\"], body *');"
                        + "for (var i=0;i<nodes.length && out.length<20;i++){"
                        + "  var t=(nodes[i].textContent||'').replace(/\\s+/g,' ').trim();"
                        + "  if (!t || t.length>180) continue;"
                        + "  for (var k=0;k<labels.length;k++){"
                        + "    if (t.indexOf(labels[k])>=0){ out.push(t); break; }"
                        + "  }"
                        + "}"
                        + "return out.join('\\n');"
        );
        return RouteFooterParser.parse(body + "\n" + (jsText == null ? "" : jsText));
    }

    private RouteFooterTotals esperarFooterLegible(WebDriver driver, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        RouteFooterTotals last = null;
        while (System.currentTimeMillis() < deadline) {
            last = leerFooter(driver);
            if (last != null && last.isReadable()) {
                return last;
            }
            pauseMs(400);
        }
        return last;
    }

    private RouteFooterTotals esperarCambio(WebDriver driver, RouteFooterTotals baseline, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        RouteFooterTotals last = baseline;
        while (System.currentTimeMillis() < deadline) {
            last = leerFooter(driver);
            if (last != null && last.differsFrom(baseline)) {
                return last;
            }
            pauseMs(400);
        }
        return last;
    }

    private RouteFooterTotals esperarTotalesComo(WebDriver driver, RouteFooterTotals expected, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        RouteFooterTotals last = null;
        while (System.currentTimeMillis() < deadline) {
            last = leerFooter(driver);
            if (last != null && !last.differsFrom(expected)) {
                return last;
            }
            pauseMs(400);
        }
        return last;
    }

    private void esperarListado(WebDriver driver) {
        long deadline = System.currentTimeMillis() + 40_000L;
        while (System.currentTimeMillis() < deadline) {
            boolean hasNew = primerVisible(driver, By.xpath(SelectorConstant.NEW_ROUTE_BUTTON)) != null;
            boolean hasFooter = primerVisible(driver, By.xpath(SelectorConstant.FOOTER_TOTAL_ROUTES)) != null;
            if (hasNew || hasFooter) {
                pauseMs(1_200);
                return;
            }
            pauseMs(400);
        }
        throw new AssertionError("El listado de rutas no mostró New route ni el pie de totales.");
    }

    private WebElement primerInputVisible(WebDriver driver) {
        for (WebElement el : driver.findElements(By.cssSelector(
                "input:not([type='hidden']):not([type='checkbox']):not([type='radio']), textarea"))) {
            try {
                if (el.isDisplayed() && el.isEnabled()) {
                    return el;
                }
            } catch (Exception ignored) {
                // next
            }
        }
        return null;
    }

    private WebElement primerVisible(WebDriver driver, By by) {
        for (WebElement el : driver.findElements(by)) {
            try {
                if (el.isDisplayed() && el.isEnabled()) {
                    return el;
                }
            } catch (Exception ignored) {
                // next
            }
        }
        return null;
    }

    private boolean clickSeguro(WebDriver driver, WebElement element) {
        try {
            js(driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
            pauseMs(80);
            try {
                element.click();
            } catch (Exception e) {
                js(driver).executeScript("arguments[0].click();", element);
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn("clickSeguro: {}", e.getMessage());
            return false;
        }
    }

    private boolean clickPorTexto(WebDriver driver, List<String> texts) {
        String script =
                "var wanted = arguments[0];"
                        + "var nodes = document.querySelectorAll('button,[role=\"button\"],div,span,label,p');"
                        + "for (var i = 0; i < nodes.length; i++) {"
                        + "  var t = (nodes[i].textContent || '').replace(/\\s+/g,' ').trim();"
                        + "  if (wanted.indexOf(t) < 0) continue;"
                        + "  var r = nodes[i].getBoundingClientRect();"
                        + "  if (r.width <= 0 || r.height <= 0 || r.height > 70) continue;"
                        + "  var clickable = nodes[i].closest('button,[role=\"button\"]') || nodes[i];"
                        + "  clickable.click();"
                        + "  return t;"
                        + "}"
                        + "return null;";
        Object clicked = js(driver).executeScript(script, texts);
        return clicked != null;
    }

    private boolean hayTextoExacto(WebDriver driver, String title) {
        try {
            Object visible = js(driver).executeScript(
                    "var title = arguments[0];"
                            + "var nodes = document.querySelectorAll('button,span,div,p,label,h1,h2,h3');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  if ((nodes[i].textContent || '').replace(/\\s+/g,' ').trim() !== title) continue;"
                            + "  var r = nodes[i].getBoundingClientRect();"
                            + "  if (r.width > 0 && r.height > 0 && r.height < 80) return true;"
                            + "}"
                            + "return false;",
                    title);
            return Boolean.TRUE.equals(visible);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean esFecha(String field) {
        String n = field.toLowerCase(Locale.ROOT);
        return n.contains("date") || n.contains("fecha");
    }

    private boolean esNumerico(String field) {
        String n = field.toLowerCase(Locale.ROOT);
        return n.contains("mile") || n.contains("income") || n.contains("ingreso");
    }

    private boolean rellenarNumerico(WebDriver driver, String value) {
        String numero = extraerNumero(value);
        if (numero.isBlank()) {
            return elegirPrimeraOpcion(driver);
        }
        List<WebElement> inputs = driver.findElements(By.cssSelector(
                "input:not([type='hidden']):not([type='checkbox']):not([type='radio']), textarea"));
        int escritos = 0;
        for (WebElement input : inputs) {
            try {
                if (!input.isDisplayed() || !input.isEnabled()) {
                    continue;
                }
                input.click();
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(Keys.DELETE);
                input.sendKeys(numero);
                escritos++;
                pauseMs(200);
            } catch (Exception ignored) {
                // next
            }
        }
        if (escritos > 0) {
            try {
                new Actions(driver).sendKeys(Keys.ENTER).perform();
            } catch (Exception ignored) {
                // ignore
            }
            pauseMs(400);
            return true;
        }
        return elegirOpcion(driver, numero) || elegirPrimeraOpcion(driver);
    }

    private String extraerNumero(String value) {
        if (value == null) {
            return "";
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\$?\\s*([\\d,.]+)").matcher(value);
        return m.find() ? m.group(1).replace(",", "") : "";
    }

    private String primeraMillas(String text) {
        if (text == null) {
            return "";
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("([\\d,.]+)\\s*mi\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        return m.find() ? m.group(1).replace(",", "") : "";
    }

    private String primeraMonto(String text) {
        if (text == null) {
            return "";
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\$\\s*([\\d,.]+)").matcher(text);
        return m.find() ? m.group(1).replace(",", "") : "";
    }

    private List<String> formatosFecha(String value) {
        List<String> out = new ArrayList<>();
        out.add(value);
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?i)(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{1,2}),?\\s*(\\d{4})")
                .matcher(value);
        if (m.find()) {
            int month = mes(m.group(1));
            String day = String.format("%02d", Integer.parseInt(m.group(2)));
            String year = m.group(3);
            String mm = String.format("%02d", month);
            out.add(mm + "/" + day + "/" + year);
            out.add(year + "-" + mm + "-" + day);
            out.add(day + "/" + mm + "/" + year);
        }
        return out;
    }

    private int mes(String name) {
        String n = name.substring(0, 3).toLowerCase(Locale.ROOT);
        return switch (n) {
            case "jan" -> 1;
            case "feb" -> 2;
            case "mar" -> 3;
            case "apr" -> 4;
            case "may" -> 5;
            case "jun" -> 6;
            case "jul" -> 7;
            case "aug" -> 8;
            case "sep" -> 9;
            case "oct" -> 10;
            case "nov" -> 11;
            case "dec" -> 12;
            default -> 1;
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> strings(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o != null && !String.valueOf(o).isBlank()) {
                    out.add(String.valueOf(o).trim());
                }
            }
        }
        return out;
    }

    private String primeraLinea(String text) {
        if (text == null) {
            return "";
        }
        String[] lines = text.split("\\r?\\n");
        return lines[0].replaceAll("\\s+", " ").trim();
    }

    private String slug(String field) {
        return "filtro-" + field.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    private JavascriptExecutor js(WebDriver driver) {
        return (JavascriptExecutor) driver;
    }

    private void pauseMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ValidateFiltersOnRoutes untilFooterChanges() {
        return new ValidateFiltersOnRoutes();
    }
}
