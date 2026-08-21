package com.validacion_calculadora.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * En Load details valida el aviso de tarifa recomendada (caja con borde):
 * <ul>
 *   <li>{@code Negotiate $… for this load} → suele ir en <b>verde</b> → abrir calculadora</li>
 *   <li>{@code Charge $… for this load to break even} → suele ir en <b>rojo</b> → cerrar y otra carga</li>
 * </ul>
 * No usar el RPM verde de la métrica Income: puede estar verde aunque el aviso sea rojo.
 */
public final class LoadDetailsGreenText {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDetailsGreenText.class);

    /**
     * Busca el aviso "… for this load …" y clasifica color del texto/borde del contenedor.
     * Retorna JSON: {"text":"…","color":"green"|"red"|"other"} o null.
     */
    private static final String ADVISORY_COLOR_JS =
            "var isGreen = function(val) {"
                    + "  if (!val) return false;"
                    + "  var s = ('' + val).toLowerCase();"
                    + "  if (s.indexOf('00a065') >= 0 || s.indexOf('00c980') >= 0"
                    + "      || s.indexOf('02a168') >= 0 || s.indexOf('23a763') >= 0) return true;"
                    + "  var m = s.match(/rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)/);"
                    + "  if (!m) return false;"
                    + "  var r = +m[1], g = +m[2], b = +m[3];"
                    + "  return g > 110 && g > (r + 30) && g >= b;"
                    + "};"
                    + "var isRed = function(val) {"
                    + "  if (!val) return false;"
                    + "  var s = ('' + val).toLowerCase();"
                    + "  if (s.indexOf('ef4444') >= 0 || s.indexOf('f87171') >= 0"
                    + "      || s.indexOf('dc2626') >= 0 || s.indexOf('ff4d') >= 0"
                    + "      || s.indexOf('ff5') >= 0 || s.indexOf('red') >= 0) return true;"
                    + "  var m = s.match(/rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)/);"
                    + "  if (!m) return false;"
                    + "  var r = +m[1], g = +m[2], b = +m[3];"
                    + "  return r > 150 && r > (g + 40) && r > (b + 40);"
                    + "};"
                    + "var colorOf = function(el) {"
                    + "  var st = window.getComputedStyle(el);"
                    + "  var blob = (el.getAttribute('class')||'') + ' ' + st.color + ' '"
                    + "           + st.borderTopColor + ' ' + st.borderColor + ' ' + st.backgroundColor;"
                    + "  if (isRed(blob)) return 'red';"
                    + "  if (isGreen(blob)) return 'green';"
                    + "  return 'other';"
                    + "};"
                    + "var nodes = document.querySelectorAll('p,div,span,li,h1,h2,h3,h4');"
                    + "for (var i = 0; i < nodes.length; i++) {"
                    + "  var el = nodes[i];"
                    + "  var t = (el.textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  if (!t || t.length > 220) continue;"
                    + "  var low = t.toLowerCase();"
                    + "  var isAdvisory = (low.indexOf('for this load') >= 0)"
                    + "    && (low.indexOf('negotiate') >= 0 || low.indexOf('charge') >= 0"
                    + "        || low.indexOf('break even') >= 0);"
                    + "  if (!isAdvisory) continue;"
                    + "  var rect = el.getBoundingClientRect();"
                    + "  if (rect.width < 40 || rect.height < 8) continue;"
                    + "  var c = colorOf(el);"
                    + "  if (c === 'other') {"
                    + "    var p = el.parentElement;"
                    + "    for (var d = 0; d < 5 && p; d++) {"
                    + "      c = colorOf(p);"
                    + "      if (c !== 'other') break;"
                    + "      p = p.parentElement;"
                    + "    }"
                    + "  }"
                    + "  return JSON.stringify({ text: t.substring(0, 160), color: c });"
                    + "}"
                    + "return null;";

    private LoadDetailsGreenText() {}

    /**
     * {@code true} solo si el aviso Charge/Negotiate es <b>verde</b>.
     * Si es rojo (o no hay aviso verde) → {@code false} (cerrar Load details y seguir).
     */
    public static boolean hasGreenNegotiateOrRpm(WebDriver driver) {
        AdvisoryStatus status = waitForAdvisory(driver, 6_000L);
        if (status == null) {
            LOGGER.warn("Load details: no se encontró aviso Charge/Negotiate … for this load.");
            DiagnosticDump.saveText("load-details-sin-aviso.txt", drawerText(driver));
            return false;
        }
        String color = resolveColor(status);
        if ("green".equals(color)) {
            LOGGER.info("Aviso VERDE en Load details → abrir calculadora. Texto: {}", status.text);
            return true;
        }
        LOGGER.warn("Aviso ROJO en Load details → cerrar y otra carga. Texto: {}", status.text);
        return false;
    }

    /**
     * El texto es el criterio fiable: {@code Negotiate $… for this load} = tarifa por encima del
     * break even (verde); {@code Charge $… to break even} = la carga no cubre costos (rojo).
     * El CSS solo se usa si el texto no permite decidir.
     */
    private static String resolveColor(AdvisoryStatus status) {
        String low = status.text.toLowerCase(java.util.Locale.ROOT);
        if (low.contains("break even") || low.contains("charge")) {
            return "red";
        }
        if (low.contains("negotiate")) {
            return "green";
        }
        return status.color;
    }

    /** El aviso se pinta después de cargar el mapa/métricas: hay que esperarlo. */
    public static AdvisoryStatus waitForAdvisory(WebDriver driver, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        AdvisoryStatus last = null;
        while (System.currentTimeMillis() < deadline) {
            last = evaluateAdvisory(driver);
            if (last != null && esDecidible(last)) {
                return last;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return last;
    }

    private static boolean esDecidible(AdvisoryStatus status) {
        String low = status.text.toLowerCase(java.util.Locale.ROOT);
        return low.contains("negotiate") || low.contains("charge") || low.contains("break even")
                || !"other".equals(status.color);
    }

    /** Texto del panel Load details, para diagnóstico cuando no aparece el aviso. */
    public static String drawerText(WebDriver driver) {
        try {
            Object raw = ((JavascriptExecutor) driver).executeScript(
                    "var nodes = document.querySelectorAll('h1,h2,h3,h4,p,div,span');"
                            + "for (var i = 0; i < nodes.length; i++) {"
                            + "  if ((nodes[i].textContent || '').trim() !== 'Load details') continue;"
                            + "  var p = nodes[i].parentElement;"
                            + "  for (var d = 0; d < 6 && p; d++) {"
                            + "    var t = (p.innerText || '');"
                            + "    if (t.length > 120) return t;"
                            + "    p = p.parentElement;"
                            + "  }"
                            + "}"
                            + "return document.body.innerText;");
            return raw == null ? "" : String.valueOf(raw);
        } catch (Exception e) {
            return "(no se pudo leer el panel: " + e.getMessage() + ")";
        }
    }

    public static AdvisoryStatus evaluateAdvisory(WebDriver driver) {
        try {
            Object raw = ((JavascriptExecutor) driver).executeScript(ADVISORY_COLOR_JS);
            if (raw != null) {
                String json = String.valueOf(raw);
                String text = extractJsonString(json, "text");
                String color = extractJsonString(json, "color");
                if (text != null && color != null) {
                    return new AdvisoryStatus(text, color);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("JS aviso Load details falló: {} — Selenium fallback", e.getMessage());
        }
        return evaluateAdvisorySelenium(driver);
    }

    private static AdvisoryStatus evaluateAdvisorySelenium(WebDriver driver) {
        try {
            for (WebElement el : driver.findElements(By.xpath(
                    "//*[contains(.,'for this load') and ("
                            + "contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'negotiate')"
                            + " or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'charge')"
                            + " or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'break even')"
                            + ")]"))) {
                try {
                    if (!el.isDisplayed()) {
                        continue;
                    }
                    String t = safe(el.getText()).replaceAll("\\s+", " ").trim();
                    if (t.length() < 10 || t.length() > 220) {
                        continue;
                    }
                    String low = t.toLowerCase();
                    if (!low.contains("for this load")) {
                        continue;
                    }
                    if (!(low.contains("negotiate") || low.contains("charge") || low.contains("break even"))) {
                        continue;
                    }
                    String color = classifyCss(el);
                    if ("other".equals(color)) {
                        try {
                            WebElement box = el.findElement(By.xpath("./ancestor::*[position()<=4][1]"));
                            color = classifyCss(box);
                        } catch (Exception ignored) {
                            // keep other
                        }
                    }
                    return new AdvisoryStatus(t, color);
                } catch (Exception ignored) {
                    // next
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private static String classifyCss(WebElement el) {
        String cls = safe(el.getAttribute("class")).toLowerCase();
        String color = safe(el.getCssValue("color"));
        String border = safe(el.getCssValue("border-top-color"));
        String blob = cls + " " + color + " " + border;
        if (isRedToken(blob) || isRedRgb(color) || isRedRgb(border)) {
            return "red";
        }
        if (isGreenToken(blob) || isGreenRgb(color) || isGreenRgb(border)) {
            return "green";
        }
        return "other";
    }

    private static boolean isGreenToken(String s) {
        return s.contains("00a065") || s.contains("00c980") || s.contains("02a168")
                || s.contains("23a763") || s.contains("emerald") || s.contains("caribbean");
    }

    private static boolean isRedToken(String s) {
        return s.contains("ef4444") || s.contains("dc2626") || s.contains("f87171")
                || s.contains("text-red") || s.contains("border-red");
    }

    private static boolean isGreenRgb(String bg) {
        int[] rgb = parseRgb(bg);
        if (rgb == null) {
            return false;
        }
        return rgb[1] > 110 && rgb[1] > rgb[0] + 30 && rgb[1] >= rgb[2];
    }

    private static boolean isRedRgb(String bg) {
        int[] rgb = parseRgb(bg);
        if (rgb == null) {
            return false;
        }
        return rgb[0] > 150 && rgb[0] > rgb[1] + 40 && rgb[0] > rgb[2] + 40;
    }

    private static int[] parseRgb(String bg) {
        if (bg == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)")
                .matcher(bg);
        if (!m.find()) {
            return null;
        }
        return new int[]{
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3))
        };
    }

    private static String extractJsonString(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        if (!m.find()) {
            return null;
        }
        return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    public static final class AdvisoryStatus {
        public final String text;
        public final String color;

        public AdvisoryStatus(String text, String color) {
            this.text = text;
            this.color = color == null ? "other" : color;
        }
    }
}
