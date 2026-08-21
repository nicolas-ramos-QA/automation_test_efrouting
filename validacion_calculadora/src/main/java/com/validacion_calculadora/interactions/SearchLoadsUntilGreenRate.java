package com.validacion_calculadora.interactions;

import com.validacion_calculadora.utils.DiagnosticDump;
import com.validacion_calculadora.utils.GreenRateIndicator;
import com.validacion_calculadora.utils.LoadSearchCities;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Tras Search loads: espera 15 s a que carguen resultados, cierra overlays,
 * scrollea la lista y valida que exista al menos un punto verde en Rate
 * ({@code load-item-ratePerMile}). Si no hay, cambia origen/destino.
 */
public class SearchLoadsUntilGreenRate implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchLoadsUntilGreenRate.class);

    private static final int MAX_CITY_PAIR_ATTEMPTS = 4;
    private static final long WAIT_RESULTS_LOAD_MS = 15_000L;
    private static final int MAX_SCROLLS_LOOKING_GREEN = 4;
    /** Tope total de este paso (búsquedas + esperas). */
    private static final long OVERALL_TIMEOUT_MS = 3 * 60_000L;

    /** Compartido entre invocaciones: al repetir la búsqueda no se vuelven a usar los mismos pares. */
    private static final Set<String> USED_CITY_PAIRS = new HashSet<>();

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        long overallDeadline = System.currentTimeMillis() + OVERALL_TIMEOUT_MS;
        Set<String> used = USED_CITY_PAIRS;

        for (int intento = 1; intento <= MAX_CITY_PAIR_ATTEMPTS; intento++) {
            if (System.currentTimeMillis() > overallDeadline) {
                DiagnosticDump.savePage(actor, "dom-loadboard-sin-punto-verde.html");
                throw new AssertionError(
                        "Automatización fallida: pasaron " + (OVERALL_TIMEOUT_MS / 1000)
                                + " s buscando cargas con punto verde en Rate sin éxito.");
            }
            LoadSearchCities pair = LoadSearchCities.randomPairExcluding(used);
            used.add(pair.key());
            LOGGER.info(
                    "Búsqueda #{} / {} → {} → {} (espera {} s + scroll buscando Rate verde)",
                    intento, MAX_CITY_PAIR_ATTEMPTS, pair.getOrigin(), pair.getDestination(),
                    WAIT_RESULTS_LOAD_MS / 1000);

            actor.attemptsTo(SearchLoadsForm.withCities(pair.getOrigin(), pair.getDestination()));

            LOGGER.info(
                    "Resultados visibles. Esperando {} s a que carguen las cargas…",
                    WAIT_RESULTS_LOAD_MS / 1000);
            GreenRateIndicator.dismissOverlaysIfAny(driver);
            // Durante la espera: cerrar overlay periódicamente y mirar verdes temprano
            long deadline = System.currentTimeMillis() + WAIT_RESULTS_LOAD_MS;
            int verdes = 0;
            while (System.currentTimeMillis() < deadline) {
                GreenRateIndicator.dismissOverlaysIfAny(driver);
                verdes = GreenRateIndicator.countGreen(driver);
                if (verdes > 0) {
                    break;
                }
                pauseMs(500);
            }

            if (verdes == 0) {
                LOGGER.info("Tras espera, sin verde aún. Se scrollea la lista virtualizada…");
                verdes = GreenRateIndicator.findGreenAfterLoad(driver, MAX_SCROLLS_LOOKING_GREEN);
            }

            if (verdes > 0) {
                LOGGER.info(
                        "OK: {} carga(s) con punto verde en Rate. Ciudades {} → {}. {}",
                        verdes, pair.getOrigin(), pair.getDestination(),
                        GreenRateIndicator.diagnose(driver));
                actor.remember("load.search.origin", pair.getOrigin());
                actor.remember("load.search.destination", pair.getDestination());
                return;
            }

            LOGGER.warn(
                    "Sin punto verde en Rate ({} → {}). Estado: {}. Se cambian ciudades.",
                    pair.getOrigin(), pair.getDestination(), GreenRateIndicator.diagnose(driver));
        }

        DiagnosticDump.savePage(actor, "dom-loadboard-sin-punto-verde.html");
        throw new AssertionError(
                "Tras " + MAX_CITY_PAIR_ATTEMPTS
                        + " búsquedas no se detectó el punto verde en la columna Rate"
                        + " (data-cy=load-item-ratePerMile)."
                        + " Último estado: " + GreenRateIndicator.diagnose(driver)
                        + ". DOM en target/diagnostico/dom-loadboard-sin-punto-verde.html");
    }

    private void pauseMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static SearchLoadsUntilGreenRate withRandomCities() {
        return new SearchLoadsUntilGreenRate();
    }
}
