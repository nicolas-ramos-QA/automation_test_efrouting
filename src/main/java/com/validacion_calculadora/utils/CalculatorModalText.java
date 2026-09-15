package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extrae solo el texto del modal Calculate profit (sin la tabla Loadboard de fondo).
 */
public final class CalculatorModalText {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorModalText.class);

    /**
     * Busca el contenedor más pequeño que tenga Calculate profit + Income + RPM + Distance + Current profit.
     */
    private static final String EXTRACT_JS =
            "var isTitle = function(el) {"
                    + "  var t = (el.textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "  return t === 'Calculate profit' || t.indexOf('Calculate profit') === 0 && t.length < 48;"
                    + "};"
                    + "var nodes = document.querySelectorAll('h1,h2,h3,h4,div,span,p,header,section');"
                    + "var title = null;"
                    + "for (var i = 0; i < nodes.length; i++) {"
                    + "  if (isTitle(nodes[i])) { title = nodes[i]; break; }"
                    + "}"
                    + "if (!title) return '';"
                    + "var el = title;"
                    + "var box = null;"
                    + "for (var k = 0; k < 14 && el; k++) {"
                    + "  var txt = el.innerText || '';"
                    + "  var ok = /Income/i.test(txt) && /\\bRPM\\b/i.test(txt)"
                    + "      && /Distance/i.test(txt) && /Current profit/i.test(txt);"
                    + "  if (ok) { box = el; break; }"
                    + "  el = el.parentElement;"
                    + "}"
                    + "if (!box) { box = title.closest('[role=\"dialog\"], [data-state=\"open\"], .fixed'); }"
                    + "if (!box) return '';"
                    // innerText no incluye el valor de los <input>: Income es editable.
                    + "var extra = [];"
                    + "var fields = box.querySelectorAll('input, textarea');"
                    + "for (var f = 0; f < fields.length; f++) {"
                    + "  var val = (fields[f].value || '').trim();"
                    + "  if (!val || !/\\d/.test(val)) continue;"
                    + "  var scope = '';"
                    + "  var p = fields[f];"
                    + "  for (var d = 0; d < 6 && p; d++) {"
                    + "    p = p.parentElement;"
                    + "    if (!p) break;"
                    + "    var t = (p.innerText || '').replace(/\\s+/g, ' ').trim();"
                    + "    if (t) { scope = t; break; }"
                    + "  }"
                    + "  if (!scope) {"
                    + "    scope = fields[f].getAttribute('aria-label')"
                    + "        || fields[f].getAttribute('name')"
                    + "        || fields[f].getAttribute('placeholder') || '';"
                    + "  }"
                    + "  var cleaned = scope.replace(/income\\s*per\\s*day/ig, '');"
                    + "  if (/\\bincome\\b/i.test(cleaned)) {"
                    + "    extra.push('Income: ' + val);"
                    + "  } else if (/days\\s*on\\s*route/i.test(cleaned)) {"
                    + "    extra.push(val + ' Days on Route');"
                    + "  }"
                    + "}"
                    + "return (extra.length ? extra.join('\\n') + '\\n' : '') + (box.innerText || '');";

    private CalculatorModalText() {}

    public static String extract(WebDriver driver) {
        try {
            Object raw = ((JavascriptExecutor) driver).executeScript(EXTRACT_JS);
            String text = raw == null ? "" : String.valueOf(raw).trim();
            if (text.length() > 40
                    && text.toLowerCase().contains("income")
                    && text.toLowerCase().contains("rpm")) {
                LOGGER.info("Texto modal Calculate profit aislado ({} chars).", text.length());
                return text;
            }
        } catch (Exception e) {
            LOGGER.warn("No se pudo aislar el modal por JS: {}", e.getMessage());
        }
        return "";
    }
}
