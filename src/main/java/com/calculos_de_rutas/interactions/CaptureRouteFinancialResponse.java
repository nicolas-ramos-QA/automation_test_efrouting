package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.abilities.CaptureUserRouteApi;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

/**
 * Espera la respuesta financiera interceptada de user-route y la guarda en memoria del Actor.
 */
public class CaptureRouteFinancialResponse implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureRouteFinancialResponse.class);
    private static final Path DIAGNOSTIC_DIR = Paths.get("target/diagnostico");

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                WaitUntil.the(Target.the("Tabla de lanes")
                                .locatedBy(SelectorConstant.LANES_TABLE), isVisible())
                        .forNoMoreThan(60).seconds(),
                Pause.forSeconds(3)
        );

        CaptureUserRouteApi capture = CaptureUserRouteApi.as(actor);
        RouteFinancialPayload payload = capture.waitAndParse(Duration.ofSeconds(30));

        actor.remember(CaptureUserRouteApi.REMEMBER_PAYLOAD, payload);
        actor.remember(CaptureUserRouteApi.REMEMBER_RAW_JSON, payload.getRawJson());

        volcarDiagnostico(actor, capture, payload.getRawJson());

        Serenity.recordReportData()
                .withTitle("API user-route interceptada")
                .andContents("Llamadas capturadas: " + capture.capturedUrls()
                        + "\nLanes parseadas: " + payload.getLanes().size()
                        + "\nRaw JSON (truncado):\n" + truncate(payload.getRawJson(), 4000));
    }

    /**
     * Guarda el JSON del Backend y el DOM del detalle de ruta para depurar selectores y parseo.
     */
    private void volcarDiagnostico(Actor actor, CaptureUserRouteApi capture, String rawJson) {
        try {
            Files.createDirectories(DIAGNOSTIC_DIR);
            Files.writeString(DIAGNOSTIC_DIR.resolve("user-route.json"), rawJson, StandardCharsets.UTF_8);
            Files.writeString(DIAGNOSTIC_DIR.resolve("dom-detalle-ruta.html"),
                    BrowseTheWeb.as(actor).getDriver().getPageSource(), StandardCharsets.UTF_8);

            StringBuilder todas = new StringBuilder();
            for (Map.Entry<String, String> entry : capture.allCaptures().entrySet()) {
                todas.append("=== ").append(entry.getKey()).append(" ===\n")
                        .append(entry.getValue()).append("\n\n");
            }
            Files.writeString(DIAGNOSTIC_DIR.resolve("user-route-todas.txt"), todas.toString(),
                    StandardCharsets.UTF_8);

            LOGGER.info("Diagnóstico guardado en {}", DIAGNOSTIC_DIR.toAbsolutePath());
        } catch (Exception e) {
            LOGGER.warn("No se pudo guardar el diagnóstico de user-route", e);
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    public static CaptureRouteFinancialResponse fromNetwork() {
        return new CaptureRouteFinancialResponse();
    }
}
