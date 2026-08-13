package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.abilities.CaptureUserRouteApi;
import com.calculos_de_rutas.interactions.CaptureRouteFinancialResponse;
import com.calculos_de_rutas.interactions.CollapseRouteMap;
import com.calculos_de_rutas.interactions.Pause;
import com.calculos_de_rutas.interactions.ToggleOpCostVisibility;
import com.calculos_de_rutas.models.FinancialCheck;
import com.calculos_de_rutas.models.LaneFinancials;
import com.calculos_de_rutas.models.LaneUiValues;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import com.calculos_de_rutas.question.LaneTableReader;
import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.utils.DiagnosticDump;
import com.calculos_de_rutas.utils.FinancialAssertions;
import com.calculos_de_rutas.utils.FinancialReport;
import com.calculos_de_rutas.utils.MoneyParser;
import com.calculos_de_rutas.utils.ScreenshotCapture;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class FinancialVerification {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinancialVerification.class);
    private static final Setup setup = new Setup();

    @Step("{0} cierra el mapa para que la tabla muestre todas las columnas")
    public void cierraElMapa(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(CollapseRouteMap.toSeeAllColumns());

        LaneTableReader tabla = LaneTableReader.from(actor);
        FinancialReport.addContext("Columnas visibles", String.join(", ", tabla.visibleLabels()));
    }

    @Step("{0} intercepta la respuesta financiera del endpoint user-route")
    public void interceptaRespuestaFinanciera(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(CaptureRouteFinancialResponse.fromNetwork());

        RouteFinancialPayload payload = rememberedPayload(actor);
        FinancialReport.addContext("Endpoint", "user-route ? features[]=markAsViewed");
        FinancialReport.addContext("URL", BrowseTheWeb.as(actor).getDriver().getCurrentUrl());
        FinancialReport.addContext("Ruta", payload.getRouteId() + " — " + payload.getRouteName());
        FinancialReport.addContext("Lanes", String.valueOf(payload.getLanes().size()));
        FinancialReport.addContext("Tarifa Op cost",
                payload.getOperativeCostRate() == null ? "—"
                        : MoneyParser.formatPrecise(payload.getOperativeCostRate()) + " / mi");
    }

    @Step("{0} valida los valores financieros de cada lane contra el Backend")
    public void validaLanesContraBackend(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();
        RouteFinancialPayload payload = rememberedPayload(actor);

        DiagnosticDump.savePage(actor, "dom-validacion-lanes.html");
        FinancialReport.addScreenshot("Tabla de la ruta — Op cost visible",
                ScreenshotCapture.captureFullTableBase64(actor));
        LaneTableReader tabla = LaneTableReader.from(actor);

        int uiRows = tabla.laneRowCount();
        int lanesToValidate = Math.min(payload.getLanes().size(), uiRows);
        FinancialReport.addContext("Op cost", "VISIBLE (incluido en Total cost y Profit)");
        FinancialReport.addContext("Filas en pantalla", String.valueOf(uiRows));

        if (uiRows != payload.getLanes().size()) {
            FinancialReport.record(FinancialCheck.builder()
                    .phase(FinancialAssertions.PHASE_LANES)
                    .scope("Estructura")
                    .field("Cantidad de lanes")
                    .backendValue(payload.getLanes().size() + " lanes")
                    .roundedValue("—")
                    .frontendValue(uiRows + " filas")
                    .status(FinancialCheck.Status.FAIL)
                    .detail("El número de filas no coincide con las lanes del Backend")
                    .build());
        }

        for (int i = 0; i < lanesToValidate; i++) {
            LaneFinancials lane = payload.getLanes().get(i);
            LaneUiValues ui = tabla.readLane(i + 1);
            FinancialAssertions.validateLane(i + 1, lane, ui, payload.getOperativeCostRate(), true);
        }
    }

    @Step("{0} valida el comportamiento del Op cost visible")
    public void validaOpCostVisible(String actorName) {
        setup.setupActor(actorName);
    }

    @Step("{0} valida el comportamiento del Op cost oculto")
    public void validaOpCostOculto(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();
        RouteFinancialPayload payload = rememberedPayload(actor);

        if (!ToggleOpCostVisibility.clickToggle(actor)) {
            FinancialReport.record(FinancialCheck.builder()
                    .phase(FinancialAssertions.PHASE_LANES)
                    .scope("Op cost oculto")
                    .field("Toggle ojo")
                    .frontendValue("no disponible")
                    .status(FinancialCheck.Status.SKIP)
                    .detail("No se encontró el icono para ocultar Op cost")
                    .build());
            return;
        }

        actor.attemptsTo(Pause.forSeconds(2));
        FinancialReport.addContext("Op cost (2.ª pasada)", "OCULTO (excluido de Total cost y Profit)");
        FinancialReport.addScreenshot("Tabla de la ruta — Op cost oculto",
                ScreenshotCapture.captureFullTableBase64(actor));

        // Releer layout: al ocultar Op las columnas pueden reacomodarse
        LaneTableReader tabla = LaneTableReader.from(actor);
        int rows = Math.min(payload.getLanes().size(), tabla.laneRowCount());
        for (int i = 0; i < rows; i++) {
            LaneFinancials lane = payload.getLanes().get(i);
            LaneUiValues ui = tabla.readLane(i + 1);
            // Misma lógica, Op no restado; el scope del reporte lo marca validateLane
            FinancialAssertions.validateLane(i + 1, lane, ui, payload.getOperativeCostRate(), false);
        }

        if (tabla.routeTotalsRowExists()) {
            FinancialAssertions.validateTotals(payload, tabla.readRouteTotals(), false);
        }

        ToggleOpCostVisibility.clickToggle(actor);
        actor.attemptsTo(Pause.forSeconds(1));
    }

    @Step("{0} valida los valores generales de la ruta contra el Backend")
    public void validaValoresGenerales(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();
        RouteFinancialPayload payload = rememberedPayload(actor);

        LaneTableReader tabla = LaneTableReader.from(actor);
        if (!tabla.routeTotalsRowExists()) {
            FinancialReport.record(FinancialCheck.builder()
                    .phase(FinancialAssertions.PHASE_TOTALS)
                    .scope(FinancialAssertions.SCOPE_TOTALS)
                    .field("Fila Total")
                    .frontendValue("no encontrada")
                    .status(FinancialCheck.Status.FAIL)
                    .build());
            return;
        }

        FinancialAssertions.validateTotals(payload, tabla.readRouteTotals(), true);
    }

    @Step("{0} genera el reporte detallado de los cálculos financieros")
    public void generaReporteDetallado(String actorName) {
        setup.setupActor(actorName);
        publicarReporte();
    }

    public static void publicarReporte() {
        if (FinancialReport.isEmpty()) {
            LOGGER.warn("No se registraron validaciones; no se genera reporte.");
            return;
        }

        String summary = FinancialReport.textSummary();
        // El HTML se escribe aquí; Hooks lo abre al cerrar el escenario (un solo open por ambiente).
        Path htmlPath = FinancialReport.writeHtml();

        LOGGER.info("\n{}", summary);
        LOGGER.info("Reporte HTML: {}", htmlPath);

        Serenity.recordReportData()
                .withTitle("Reporte Backend vs Frontend")
                .andContents(summary + "\nHTML: " + htmlPath);

        List<FinancialCheck> failures = FinancialReport.failures();
        if (!failures.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append(failures.size()).append(" discrepancia(s) Backend ↔ Frontend:\n");
            failures.forEach(f -> message.append("  - ").append(f).append('\n'));
            message.append("Reporte: ").append(htmlPath);
            throw new AssertionError(message.toString());
        }
    }

    private RouteFinancialPayload rememberedPayload(Actor actor) {
        RouteFinancialPayload payload = actor.recall(CaptureUserRouteApi.REMEMBER_PAYLOAD);
        if (payload == null) {
            throw new AssertionError("No hay payload financiero. Ejecute primero la interceptación.");
        }
        if (payload.getLanes().isEmpty()) {
            throw new AssertionError("El JSON de user-route no contiene lanes.");
        }
        return payload;
    }
}
