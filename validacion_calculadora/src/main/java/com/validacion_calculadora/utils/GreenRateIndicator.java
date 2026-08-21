package com.validacion_calculadora.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Detecta el indicador verde de Rate en {@code data-cy=load-item-ratePerMile}:
 * círculo verde (a menudo con {@code $}) a la izquierda del monto.
 * No confundir con el punto verde de Connectivity ni con badges naranja/ámbar.
 */
public final class GreenRateIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreenRateIndicator.class);

    /**
     * Cuenta filas {@code load-item} cuyo Rate tiene badge verde (no Call broker / no naranja).
     * ASCII-safe para el bridge de Selenium.
     */
    private static final String COUNT_GREEN_JS =
            "var isGreen = function(v) {"
                    + "  if (!v) return false;"
                    + "  var s = ('' + v).toLowerCase();"
                    + "  if (s.indexOf('00a065') >= 0 || s.indexOf('00c980') >= 0"
                    + "      || s.indexOf('02a168') >= 0 || s.indexOf('02a1') >= 0) return true;"
                    + "  var m = s.match(/rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)/);"
                    + "  if (!m) return false;"
                    + "  var r = +m[1], g = +m[2], b = +m[3];"
                    + "  return g > 110 && g > (r + 25) && g >= b;"
                    + "};"
                    + "var isOrange = function(v) {"
                    + "  if (!v) return false;"
                    + "  var s = ('' + v).toLowerCase();"
                    + "  if (s.indexOf('ff8038') >= 0 || s.indexOf('ffcb46') >= 0"
                    + "      || s.indexOf('ff7426') >= 0 || s.indexOf('f59e') >= 0"
                    + "      || s.indexOf('orange') >= 0 || s.indexOf('amber') >= 0) return true;"
                    + "  var m = s.match(/rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)/);"
                    + "  if (!m) return false;"
                    + "  var r = +m[1], g = +m[2], b = +m[3];"
                    + "  return r > 180 && g > 80 && g < 200 && b < 120 && r > g;"
                    + "};"
                    // SVG usa SVGAnimatedString: className.toString() NO da las clases reales
                    + "var clsOf = function(el) {"
                    + "  try {"
                    + "    if (!el) return '';"
                    + "    var a = el.getAttribute('class');"
                    + "    if (a) return a;"
                    + "    if (el.className && el.className.baseVal != null) return '' + el.className.baseVal;"
                    + "    return '';"
                    + "  } catch (e) { return ''; }"
                    + "};"
                    + "var cellHasGreenBadge = function(cell) {"
                    + "  var txt = (cell.innerText || cell.textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (/call\\s*broker/i.test(txt) && !/\\$[\\d,]/.test(txt)) return false;"
                    // Icono Rate verde: class con 00c980 / 00a065 (p.ej. svg text-[#00c980])
                    + "  var greenNodes = cell.querySelectorAll("
                    + "    '[class*=\"00c980\"], [class*=\"00C980\"],"
                    + "     [class*=\"00a065\"], [class*=\"00A065\"], [class*=\"02a168\"]');"
                    + "  if (greenNodes && greenNodes.length) {"
                    + "    for (var g = 0; g < greenNodes.length; g++) {"
                    + "      var gc = clsOf(greenNodes[g]);"
                    + "      if (/ff8038|orange|amber|ffcb46/i.test(gc)) continue;"
                    + "      return true;"
                    + "    }"
                    + "  }"
                    + "  var nodes = cell.querySelectorAll('span, div, p, i, button, svg, path');"
                    + "  for (var i = 0; i < nodes.length; i++) {"
                    + "    var el = nodes[i];"
                    + "    var cls = clsOf(el);"
                    + "    var st = window.getComputedStyle(el);"
                    + "    var blob = cls + ' ' + (st.backgroundColor || '') + ' ' + (st.color || '')"
                    + "             + ' ' + (st.fill || '') + ' ' + (el.getAttribute('fill') || '');"
                    + "    if (isOrange(blob)) continue;"
                    + "    if (!isGreen(blob) && !/caribbean|bg-green|emerald|00c980|00a065|02a168/i.test(cls)) continue;"
                    + "    var raw = (el.textContent || '').replace(/\\s+/g, '').trim();"
                    + "    var rect = el.getBoundingClientRect();"
                    + "    var small = rect.width > 0 && rect.width <= 36 && rect.height > 0 && rect.height <= 36;"
                    + "    var dollarOnly = raw === '$' || raw === '\\uFF04';"
                    + "    var rounded = /rounded-full|rounded-\\[/.test(cls);"
                    + "    var tag = (el.tagName || '').toLowerCase();"
                    + "    if (dollarOnly || (small && rounded) || (small && /\\$/.test(txt))"
                    + "        || (small && isGreen(blob)) || (tag === 'svg' && /00c980|00a065/i.test(cls)))"
                    + "      return true;"
                    + "  }"
                    + "  return false;"
                    + "};"
                    + "var rows = document.querySelectorAll('[data-cy=\"load-item\"]');"
                    + "var n = 0;"
                    + "for (var r = 0; r < rows.length; r++) {"
                    + "  var cell = rows[r].querySelector('[data-cy=\"load-item-ratePerMile\"]');"
                    + "  if (!cell) continue;"
                    + "  if (cellHasGreenBadge(cell)) n++;"
                    + "}"
                    + "return n;";

    /** Resumen para logs: filas, Call broker, montos $, verdes SVG Rate. */
    private static final String DIAG_JS =
            "var rows = document.querySelectorAll('[data-cy=\"load-item\"]');"
                    + "var callB = 0, withDollar = 0, greenSvg = 0, samples = [];"
                    + "for (var i = 0; i < rows.length; i++) {"
                    + "  var cell = rows[i].querySelector('[data-cy=\"load-item-ratePerMile\"]');"
                    + "  if (!cell) continue;"
                    + "  var t = (cell.innerText || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (/call\\s*broker/i.test(t)) callB++;"
                    + "  if (/\\$[\\d,]/.test(t) || t.indexOf('$') >= 0) withDollar++;"
                    + "  if (cell.querySelector('[class*=\"00c980\"], [class*=\"00a065\"], [class*=\"00A065\"]')) greenSvg++;"
                    + "  if (samples.length < 5) samples.push(t.substring(0, 60));"
                    + "}"
                    + "return JSON.stringify({"
                    + "  rows: rows.length,"
                    + "  callBroker: callB,"
                    + "  withDollar: withDollar,"
                    + "  greenSvg: greenSvg,"
                    + "  samples: samples,"
                    + "  nextLane: !!document.querySelector(\"button\") && document.body.innerText.indexOf('Next Lane') >= 0,"
                    + "  results: document.body.innerText.indexOf('Results:') >= 0"
                    + "});";

    private GreenRateIndicator() {}

    public static int waitForAtLeastOne(WebDriver driver, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int last = 0;
        while (System.currentTimeMillis() < deadline) {
            dismissOverlaysIfAny(driver);
            last = countGreen(driver);
            if (last > 0) {
                LOGGER.info("Indicador(es) Rate verde detectados: {}", last);
                return last;
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        LOGGER.warn("Tras {} ms no se detectó punto verde en Rate. {}", timeoutMs, diagnose(driver));
        return 0;
    }

    public static int countGreen(WebDriver driver) {
        try {
            Object jsCount = ((JavascriptExecutor) driver).executeScript(COUNT_GREEN_JS);
            int count = jsCount instanceof Number ? ((Number) jsCount).intValue() : 0;
            if (count > 0) {
                return count;
            }
        } catch (Exception e) {
            LOGGER.warn("countGreen JS falló: {} — fallback Selenium", e.getMessage());
        }
        return countGreenSelenium(driver);
    }

    public static String diagnose(WebDriver driver) {
        try {
            Object raw = ((JavascriptExecutor) driver).executeScript(DIAG_JS);
            return raw == null ? "(sin diag)" : String.valueOf(raw);
        } catch (Exception e) {
            return "(diag error: " + e.getMessage() + ")";
        }
    }

    /**
     * Tras la espera fija: cierra overlays, scrollea la lista virtualizada y re-cuenta verdes.
     */
    public static int findGreenAfterLoad(WebDriver driver, int maxScrolls) {
        dismissOverlaysIfAny(driver);
        int verdes = countGreen(driver);
        if (verdes > 0) {
            return verdes;
        }
        LOGGER.info("Sin verde aún. Estado: {}", diagnose(driver));
        for (int s = 1; s <= maxScrolls; s++) {
            dismissOverlaysIfAny(driver);
            scrollLoadList(driver);
            pause(450);
            verdes = countGreen(driver);
            LOGGER.info("Scroll #{} → verdes={} | {}", s, verdes, diagnose(driver));
            if (verdes > 0) {
                return verdes;
            }
        }
        return 0;
    }

    public static boolean rowHasGreen(WebElement row) {
        try {
            WebDriver driver = ((org.openqa.selenium.WrapsDriver) row).getWrappedDriver();
            Object r = ((JavascriptExecutor) driver).executeScript(
                    "var row = arguments[0];"
                            + "var cell = row.querySelector('[data-cy=\"load-item-ratePerMile\"]');"
                            + "if (!cell) return false;"
                            + "var txt = (cell.innerText || '').replace(/\\s+/g, ' ');"
                            + "if (/call\\s*broker/i.test(txt) && !/\\$[\\d,]/.test(txt)) return false;"
                            + "var nodes = cell.querySelectorAll('[class*=\"00c980\"], [class*=\"00C980\"],"
                            + "  [class*=\"00a065\"], [class*=\"00A065\"], [class*=\"02a168\"]');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  var cls = nodes[i].getAttribute('class') || '';"
                            + "  if (/ff8038|orange|amber|ffcb46/i.test(cls)) continue;"
                            + "  return true;"
                            + "}"
                            + "return false;",
                    row);
            return Boolean.TRUE.equals(r);
        } catch (Exception e) {
            try {
                WebElement cell = row.findElement(By.cssSelector("[data-cy='load-item-ratePerMile']"));
                String txt = safe(cell.getText());
                if (txt.toLowerCase().contains("call broker") && !txt.contains("$")) {
                    return false;
                }
                // SVG / span con color verde de Rate
                return !cell.findElements(By.cssSelector(
                        "[class*='00c980'], [class*='00C980'], [class*='00a065'], [class*='00A065']"))
                        .isEmpty()
                        || !cell.findElements(By.xpath(".//*[normalize-space()='$']")).isEmpty()
                        || countGreenInRowFallback(cell);
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    private static boolean countGreenInRowFallback(WebElement cell) {
        for (WebElement el : cell.findElements(By.cssSelector("span, div, svg"))) {
            try {
                String cls = safe(el.getAttribute("class")).toLowerCase();
                if (isOrangeToken(cls)) {
                    continue;
                }
                String bg = el.getCssValue("background-color");
                String color = el.getCssValue("color");
                if (isGreenRgb(bg) || isGreenRgb(color) || cls.contains("00c980") || cls.contains("00a065")) {
                    org.openqa.selenium.Dimension size = el.getSize();
                    if (size.getWidth() > 0 && size.getWidth() <= 40 && size.getHeight() <= 40) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // next
            }
        }
        return false;
    }

    /**
     * Busca la siguiente fila con punto verde en Rate (excluyendo las ya probadas) y le hace clic
     * en la <b>misma</b> ejecución JS. Evita {@code StaleElementReferenceException}: la lista de
     * resultados es virtualizada y recicla los nodos entre el findElement y el click de Selenium.
     *
     * @return clave estable de la carga abierta, o {@code null} si no hay verde nueva visible.
     */
    public static String clickNextGreenRow(WebDriver driver, Collection<String> triedKeys) {
        try {
            Object key = ((JavascriptExecutor) driver).executeScript(
                    CLICK_NEXT_GREEN_JS, new ArrayList<>(triedKeys));
            if (key == null) {
                return null;
            }
            String k = String.valueOf(key);
            return k.isBlank() || "null".equals(k) ? null : k;
        } catch (Exception e) {
            LOGGER.warn("clickNextGreenRow falló: {}", e.getMessage());
            return null;
        }
    }

    /** Cuántas filas con punto verde hay que aún no se han probado. */
    public static int countGreenNotTried(WebDriver driver, Collection<String> triedKeys) {
        try {
            Object n = ((JavascriptExecutor) driver).executeScript(
                    COUNT_GREEN_NOT_TRIED_JS, new ArrayList<>(triedKeys));
            return n instanceof Number ? ((Number) n).intValue() : 0;
        } catch (Exception e) {
            LOGGER.warn("countGreenNotTried falló: {}", e.getMessage());
            return 0;
        }
    }

    /** Helpers JS compartidos: clave estable por carga + detección de verde en Rate. */
    private static final String GREEN_ROW_HELPERS_JS =
            "var tried = arguments[0] || [];"
                    + "var triedMap = {};"
                    + "for (var t = 0; t < tried.length; t++) { triedMap[tried[t]] = true; }"
                    + "var clsOf = function(el) {"
                    + "  try { var a = el.getAttribute('class'); return a || ''; } catch (e) { return ''; }"
                    + "};"
                    // La clave viene del texto de la fila: sobrevive al reciclaje de nodos
                    + "var keyOf = function(row) {"
                    + "  var t = (row.innerText || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                    + "  return t.substring(0, 110);"
                    + "};"
                    + "var hasGreen = function(row) {"
                    + "  var cell = row.querySelector('[data-cy=\"load-item-ratePerMile\"]');"
                    + "  if (!cell) return false;"
                    + "  var txt = (cell.innerText || '').replace(/\\s+/g, ' ');"
                    + "  if (/call\\s*broker/i.test(txt) && !/\\$[\\d,]/.test(txt)) return false;"
                    + "  var nodes = cell.querySelectorAll('[class*=\"00c980\"], [class*=\"00C980\"],"
                    + "    [class*=\"00a065\"], [class*=\"00A065\"], [class*=\"02a168\"]');"
                    + "  for (var i = 0; i < nodes.length; i++) {"
                    + "    if (/ff8038|orange|amber|ffcb46/i.test(clsOf(nodes[i]))) continue;"
                    + "    return true;"
                    + "  }"
                    + "  return false;"
                    + "};";

    private static final String CLICK_NEXT_GREEN_JS =
            GREEN_ROW_HELPERS_JS
                    + "var rows = document.querySelectorAll('[data-cy=\"load-item\"]');"
                    + "for (var r = 0; r < rows.length; r++) {"
                    + "  var row = rows[r];"
                    + "  var k = keyOf(row);"
                    + "  if (!k || k.length < 8) continue;"
                    + "  if (triedMap[k]) continue;"
                    + "  if (!hasGreen(row)) continue;"
                    + "  row.scrollIntoView({ block: 'center' });"
                    + "  row.click();"
                    + "  return k;"
                    + "}"
                    + "return null;";

    private static final String COUNT_GREEN_NOT_TRIED_JS =
            GREEN_ROW_HELPERS_JS
                    + "var rows = document.querySelectorAll('[data-cy=\"load-item\"]');"
                    + "var n = 0;"
                    + "for (var r = 0; r < rows.length; r++) {"
                    + "  var k = keyOf(rows[r]);"
                    + "  if (!k || k.length < 8 || triedMap[k]) continue;"
                    + "  if (hasGreen(rows[r])) n++;"
                    + "}"
                    + "return n;";

    /** Vuelve al inicio de la lista de resultados (cargas verdes suelen estar arriba). */
    public static void scrollListToTop(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var row = document.querySelector('[data-cy=\"load-item\"]');"
                            + "var sc = row && row.closest('[style*=\"overflow\"], .overflow-auto, .overflow-y-auto, [data-radix-scroll-area-viewport]');"
                            + "if (sc) { sc.scrollTop = 0; }"
                            + "window.scrollTo(0, 0);"
                            + "if (row) {"
                            + "  var first = document.querySelector('[data-cy=\"load-item\"][data-index=\"0\"], [data-cy=\"load-item\"]');"
                            + "  if (first) first.scrollIntoView({block:'start'});"
                            + "}");
            pause(400);
        } catch (Exception e) {
            LOGGER.warn("scrollListToTop: {}", e.getMessage());
        }
    }

    public static void dismissOverlaysIfAny(WebDriver driver) {
        try {
            // Solo modales/tourours reales — NO el ítem de sort "Recommended"
            boolean overlay = !driver.findElements(By.xpath(
                    "//button[contains(normalize-space(.),'Next Lane')]"
                            + " | //*[contains(normalize-space(.),'Profit potential')]"
                            + " | //*[contains(normalize-space(.),'Ease of booking')]"
                            + " | //*[contains(normalize-space(.),'Lane connectivity')]"
                            + " | //*[@role='dialog'][.//*[contains(.,'Next Lane') or contains(.,'Recommended')]]"
                            + " | //*[contains(@class,'fixed') and contains(@class,'inset-0')]"
                            + "     [.//button[contains(.,'Next Lane')]]")).isEmpty();
            if (!overlay) {
                return;
            }
            LOGGER.info("Overlay sobre resultados (tour/modal); se cierra.");
            for (WebElement close : driver.findElements(By.cssSelector(
                    "button[aria-label='Close'], button[aria-label='close'],"
                            + " button[aria-label='Close modal'], [data-cy*='close']"))) {
                try {
                    if (close.isDisplayed()) {
                        close.click();
                        pause(350);
                        break;
                    }
                } catch (Exception ignored) {
                    // next
                }
            }
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            pause(250);
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "var b=document.querySelector('[data-state=open][class*=fixed], .fixed.inset-0');"
                                + "if(b){b.click();}");
            } catch (Exception ignored) {
                // ignore
            }
            pause(300);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private static int countGreenSelenium(WebDriver driver) {
        int n = 0;
        try {
            for (WebElement row : driver.findElements(By.cssSelector("[data-cy='load-item']"))) {
                try {
                    if (rowHasGreen(row)) {
                        n++;
                    }
                } catch (Exception ignored) {
                    // next
                }
            }
        } catch (Exception e) {
            LOGGER.warn("countGreen Selenium falló: {}", e.getMessage());
        }
        return n;
    }

    /** Avanza la lista virtualizada para renderizar más cargas. */
    public static void scrollListDown(WebDriver driver) {
        scrollLoadList(driver);
        pause(500);
    }

    private static void scrollLoadList(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var sc = document.querySelector('[data-cy=\"load-item\"]')"
                            + "  && document.querySelector('[data-cy=\"load-item\"]').closest('[style*=\"overflow\"], .overflow-auto, .overflow-y-auto, [data-radix-scroll-area-viewport]');"
                            + "if (!sc) {"
                            + "  var rows = document.querySelectorAll('[data-cy=\"load-item\"]');"
                            + "  if (rows.length) rows[rows.length-1].scrollIntoView({block:'end'});"
                            + "  window.scrollBy(0, 420);"
                            + "} else { sc.scrollTop = sc.scrollTop + 420; }");
        } catch (Exception e) {
            try {
                new Actions(driver).sendKeys(Keys.PAGE_DOWN).perform();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private static boolean isOrangeToken(String cls) {
        return cls.contains("ff8038") || cls.contains("ffcb46") || cls.contains("ff7426")
                || cls.contains("orange") || cls.contains("amber") || cls.contains("f59e");
    }

    private static boolean isGreenRgb(String bg) {
        if (bg == null) {
            return false;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)")
                .matcher(bg);
        if (!m.find()) {
            return false;
        }
        int r = Integer.parseInt(m.group(1));
        int g = Integer.parseInt(m.group(2));
        int b = Integer.parseInt(m.group(3));
        return g > 110 && g > r + 25 && g >= b;
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
