package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.FinancialCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FailureDiagnosisTest {

    @BeforeEach
    void limpiar() {
        FinancialReport.reset();
    }

    @Test
    void sinFallosNoGeneraSeccion() {
        FinancialReport.record(FinancialCheck.builder()
                .scope("Lane 1")
                .field("Fuel cost")
                .status(FinancialCheck.Status.PASS)
                .backendValue("$100.000")
                .roundedValue("$100")
                .frontendValue("-$100")
                .build());

        assertFalse(FailureDiagnosis.hasFailures());
        assertTrue(FailureDiagnosis.buildText().isEmpty());
        assertTrue(FailureDiagnosis.buildHtmlSection().isEmpty());
    }

    @Test
    void explicaProfitOcultoConMuchoDetalle() {
        FinancialReport.addContext("Ruta", "3417");
        FinancialReport.addContext("URL de la ruta",
                "https://efdata-qa.efrouting.com/route-planner/detail/3417");
        FinancialReport.record(FinancialCheck.builder()
                .phase("2. Validación del Total (fila inferior)")
                .scope("Total de la ruta  ·  Op cost OCULTO")
                .field("Profit")
                .backendValue("$550.000 – $750.000")
                .roundedValue("$550 – $750")
                .frontendValue("$650 – $850")
                .difference("mín +100 / máx +100")
                .status(FinancialCheck.Status.FAIL)
                .detail("Posible bug de la app: el Profit en pantalla sube ~$100.000 al ocultar Op cost")
                .formula("Σ Profit de cada lane sin Op")
                .build());

        String text = FailureDiagnosis.buildText();
        assertTrue(text.contains("DIAGNÓSTICO DETALLADO"));
        assertTrue(text.contains("Profit"));
        assertTrue(text.contains("Op cost"));
        assertTrue(text.contains("qué se esperaba") || text.contains("Números confrontados"));
        assertTrue(text.contains("Causas más probables"));
        assertTrue(text.contains("Cómo comprobarlo a mano"));
        assertTrue(text.contains("3417"));

        String html = FailureDiagnosis.buildHtmlSection();
        assertTrue(html.contains("Diagnóstico detallado"));
        assertTrue(html.contains("Fallo 1 de 1"));
        assertTrue(html.contains("Posible bug") || html.contains("Profit"));
    }

    @Test
    void explicaTotalCostVisibleConFormula() {
        FinancialReport.record(FinancialCheck.builder()
                .phase("1. Validación por lane")
                .scope("Lane 2 — Dallas → Chicago [deadhead]")
                .field("Cálculo Total cost")
                .backendValue("$220.400")
                .roundedValue("$220")
                .frontendValue("-$180")
                .difference("+40")
                .status(FinancialCheck.Status.FAIL)
                .formula("Fuel 120.000 + Toll 0.000 + Custom 0.000 + Op 100.400 = 220.400")
                .detail("Fuel + Toll + Custom [+ Op] ≠ Total cost en pantalla")
                .build());

        String text = FailureDiagnosis.buildText();
        assertTrue(text.contains("Fuel 120.000"));
        assertTrue(text.contains("deadhead"));
        assertTrue(text.contains("Total cost"));
        assertTrue(text.contains("cómo se redondeó") || text.contains("Redondeo"));
    }
}
