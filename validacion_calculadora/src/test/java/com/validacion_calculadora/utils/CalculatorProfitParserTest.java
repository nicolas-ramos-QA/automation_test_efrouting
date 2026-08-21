package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CalculatorProfitParserTest {

    @Test
    void parseaCurrentProfitConCiudadesYPorcentaje() {
        String text = """
                Calculate profit
                Profit to Fort Wayne, IN
                Income $ 1,050
                Total costs $ 858
                $ Current profit in $192
                Chicago, IL - Geneva, IL - Fort Wayne, IN
                Profit: 18%, 1 days
                Cycle profit
                """;

        CalculatorValidationResult result = CalculatorProfitParser.parseIfValid(text);
        assertNotNull(result);
        assertEquals("Chicago, IL - Geneva, IL - Fort Wayne, IN", result.getCitiesLine());
        assertEquals("18%", result.getProfitPercent());
        assertEquals("1 days", result.getDaysText());
    }

    @Test
    void sinCurrentProfitNoSirve() {
        assertNull(CalculatorProfitParser.parseIfValid("Calculate profit\nIncome $100"));
    }

    @Test
    void sinPorcentajeNoSirve() {
        String text = """
                Current profit in $192
                Chicago, IL - Geneva, IL - Fort Wayne, IN
                """;
        assertNull(CalculatorProfitParser.parseIfValid(text));
    }
}
