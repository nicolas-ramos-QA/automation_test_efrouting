package com.calculos_de_rutas.abilities;

import com.calculos_de_rutas.exceptions.CommonException;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import com.calculos_de_rutas.utils.RouteFinancialParser;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ability Screenplay: captura las respuestas de user-route interceptando fetch/XHR en el browser
 * y selecciona la que contiene el detalle financiero de la ruta.
 */
public class CaptureUserRouteApi implements Ability {

    public static final String REMEMBER_PAYLOAD = "routeFinancialPayload";
    public static final String REMEMBER_RAW_JSON = "routeFinancialRawJson";

    /** Claves que identifican al response financiero frente a otras llamadas user-route. */
    private static final String[] FINANCIAL_HINTS = {"fuelCost", "fuel_cost", "tollCost", "toll_cost", "profit"};

    private static final String INSTALL_SCRIPT = """
            (function () {
              if (window.__efRouteCaptureInstalled) { return true; }
              window.__efRouteCaptureInstalled = true;
              window.__userRouteCaptures = [];

              const shouldCapture = (url) => url && String(url).includes('user-route');
              const store = (url, body) => {
                try { window.__userRouteCaptures.push({ url: String(url), body: body }); } catch (e) {}
              };

              const originalFetch = window.fetch.bind(window);
              window.fetch = async function (...args) {
                const response = await originalFetch(...args);
                try {
                  const input = args[0];
                  const url = typeof input === 'string' ? input : (input && input.url ? input.url : '');
                  if (shouldCapture(url)) {
                    const clone = response.clone();
                    const text = await clone.text();
                    store(url, text);
                  }
                } catch (e) { /* ignore capture errors */ }
                return response;
              };

              const xhrOpen = XMLHttpRequest.prototype.open;
              const xhrSend = XMLHttpRequest.prototype.send;
              XMLHttpRequest.prototype.open = function (method, url, ...rest) {
                this.__efUrl = url;
                return xhrOpen.call(this, method, url, ...rest);
              };
              XMLHttpRequest.prototype.send = function (...args) {
                this.addEventListener('load', function () {
                  try {
                    if (shouldCapture(this.__efUrl)) { store(this.__efUrl, this.responseText); }
                  } catch (e) { /* ignore */ }
                });
                return xhrSend.apply(this, args);
              };
              return true;
            })();
            """;

    private final WebDriver driver;

    private CaptureUserRouteApi(WebDriver driver) {
        this.driver = driver;
    }

    public static CaptureUserRouteApi withDriver(WebDriver driver) {
        CaptureUserRouteApi ability = new CaptureUserRouteApi(driver);
        ability.install();
        return ability;
    }

    public static CaptureUserRouteApi as(Actor actor) {
        CaptureUserRouteApi ability = actor.abilityTo(CaptureUserRouteApi.class);
        if (ability == null) {
            ability = withDriver(BrowseTheWeb.as(actor).getDriver());
            actor.can(ability);
        }
        return ability;
    }

    public void install() {
        ((JavascriptExecutor) driver).executeScript(INSTALL_SCRIPT);
    }

    public void clear() {
        ((JavascriptExecutor) driver).executeScript("window.__userRouteCaptures = [];");
    }

    /** Todas las respuestas user-route capturadas, en orden (url -> body). */
    @SuppressWarnings("unchecked")
    public Map<String, String> allCaptures() {
        Object raw = ((JavascriptExecutor) driver).executeScript("return window.__userRouteCaptures || [];");
        Map<String, String> captures = new LinkedHashMap<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Object url = map.get("url");
                    Object body = map.get("body");
                    if (url != null && body instanceof String text) {
                        captures.put(String.valueOf(url), text);
                    }
                }
            }
        }
        return captures;
    }

    public List<String> capturedUrls() {
        return new ArrayList<>(allCaptures().keySet());
    }

    /**
     * Espera y devuelve el body de la llamada que contiene el detalle financiero.
     */
    public String waitForFinancialBody(Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        Map<String, String> lastSeen = Map.of();

        while (Instant.now().isBefore(deadline)) {
            Map<String, String> captures = allCaptures();
            lastSeen = captures;
            String best = pickFinancial(captures);
            if (best != null) {
                return best;
            }
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new CommonException(
                "No se capturó la respuesta financiera de user-route en " + timeout.getSeconds() + "s. "
                        + "Llamadas vistas: " + lastSeen.keySet());
    }

    private String pickFinancial(Map<String, String> captures) {
        String detailFallback = null;
        for (Map.Entry<String, String> entry : captures.entrySet()) {
            String body = entry.getValue();
            if (body == null || body.isBlank() || !body.trim().startsWith("{")) {
                continue;
            }
            for (String hint : FINANCIAL_HINTS) {
                if (body.contains(hint)) {
                    return body;
                }
            }
            if (entry.getKey().matches(".*/user-route/\\d+.*") && detailFallback == null) {
                detailFallback = body;
            }
        }
        return detailFallback;
    }

    public RouteFinancialPayload waitAndParse(Duration timeout) {
        return RouteFinancialParser.parse(waitForFinancialBody(timeout));
    }
}
