package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;
import com.validacion_calculadora.models.CalculatorValidationResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatorTestReportTest {

    @Test
    void escribeHtmlConDatosYCapturaAlFinal() throws Exception {
        CalculatorValidationResult result = new CalculatorValidationResult(
                "Chicago, IL - Streamwood, IL - West Palm Beach, FL",
                "8%",
                "5 days",
                new CalculatorFinancialMetrics(
                        5300, 1314, 4.03, 5.0, 1060.0, 4185.0, 1115.0, 1384.0, 0.81, 21.0));

        // PNG mínimo válido (1x1)
        byte[] tinyPng = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

        Path html = CalculatorTestReport.write(
                result, "Chicago, IL", "Miami, FL", 1, tinyPng, false);

        assertTrue(Files.exists(html));
        String body = Files.readString(html);
        assertTrue(body.contains("5300") || body.contains("5,300"));
        assertTrue(body.contains("4.03"));
        assertTrue(body.contains("data:image/png;base64,"));
        assertTrue(body.contains("Captura del modal") || body.contains("Resultado exacto"));
        assertTrue(body.contains("Income / Days on Route = Income per day"));
        assertTrue(body.contains("1060"));
        assertTrue(body.contains("Income - Total costs = Current profit"));
        assertTrue(body.contains("4185"));
        assertTrue(body.contains("Current profit / Total Distance = Profit / mile"));
        assertTrue(body.contains("1384"));
        assertTrue(body.contains("Current profit / Income = Profit %"));
        assertTrue(body.contains("Profit % en UI (redondeado)"));
    }
}
