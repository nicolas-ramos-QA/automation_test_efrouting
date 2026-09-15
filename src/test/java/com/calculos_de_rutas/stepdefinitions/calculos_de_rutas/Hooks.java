package com.calculos_de_rutas.stepdefinitions.calculos_de_rutas;

import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.utils.FinancialReport;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Hooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void prepareStage(Scenario scenario) {
        Setup.reset();
        OnStage.setTheStage(new OnlineCast());
        FinancialReport.reset();
        FinancialReport.addContext("Escenario", scenario.getName());
    }

    /**
     * Escribe y abre el reporte HTML al terminar cada ambiente (aunque falle un paso).
     * Evita doble apertura si el último step ya lo publicó.
     */
    @After
    public void publishReport(Scenario scenario) {
        try {
            if (!FinancialReport.isEmpty()) {
                Path html = FinancialReport.writeHtml();
                // Abrir siempre al cerrar el escenario de cada ambiente
                FinancialReport.openInBrowser(html);
                LOGGER.info("\n{}", FinancialReport.textSummary());
                LOGGER.info("Reporte de cálculos financieros abierto: {}", html);
                scenario.attach(FinancialReport.textSummary().getBytes(StandardCharsets.UTF_8),
                        "text/plain", "Reporte de cálculos financieros");
            }
        } catch (Exception e) {
            LOGGER.warn("No se pudo generar/abrir el reporte de cálculos financieros", e);
        } finally {
            try {
                OnStage.drawTheCurtain();
            } catch (Exception e) {
                LOGGER.warn("No se pudo cerrar el navegador al finalizar el escenario", e);
            }
            Setup.reset();
        }
    }
}
