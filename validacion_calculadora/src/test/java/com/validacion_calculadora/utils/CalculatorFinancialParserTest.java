package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatorFinancialParserTest {

    private static final String MODAL_SAMPLE = """
            Calculate profit
            Profit to Fort Wayne, IN
            Income
            $1,050
            RPM
            $5.25 / mi
            Distance
            200 mi
            RPM with DH
            $4.44 / mi
            Distance DH-O
            36 mi
            Total costs
            $858
            Current profit in $192
            Chicago, IL - Geneva, IL - Fort Wayne, IN
            Profit: 18%, 1 days
            """;

    /** Como en la captura real: 7000 / 1431 = 4.89168… → UI $4.89/mi */
    private static final String REAL_MODAL_7000 = """
            Calculate profit
            Income
            $ 7,000
            Income per day
            $ 2,800/day
            RPM
            $ 4.89/mi
            Distance
            1,431 mi
            RPM with DH
            $ 4.88/mi
            Distance DH-O
            1 mi
            Days on Route
            2.5
            Total Distance
            1,432 mi
            Profit / mile
            $ 1.52 / mi
            Total costs
            $ 4,828
            Current profit
            $ 2,172
            Chicago, IL - Somewhere, IL - Miami, FL
            Profit: 31%, 2.5 days
            """;

    private static final String COLUMN_LAYOUT = """
            Calculate profit
            Income
            RPM
            Distance
            RPM with DH
            Distance DH-O
            $ 5,300
            $ 4.03 /mi
            1,314 mi
            $ 3.55 /mi
            70 mi
            Current profit
            Chicago, IL - Streamwood, IL - West Palm Beach, FL
            Profit: 8%, 5 days
            """;

    private static final String WITH_DH_NOISE = """
            Calculate profit
            Income
            RPM
            Distance
            RPM with DH
            $ 5,300
            $ 3.81 /mi
            1,314 mi
            $ 4.03 /mi
            Current profit
            Chicago, IL - Streamwood, IL - West Palm Beach, FL
            Profit: 8%, 5 days
            """;

    /** Ruido de Loadboard (otro rate 5.06) no debe usarse si el modal está limpio. */
    private static final String MODAL_PLUS_LOADBOARD_NOISE = REAL_MODAL_7000 + """
            
            Team drivers $ 7,000 $ 5.06 /mi 1,384 mi
            """;

    @Test
    void parseaIncomeDistanceYRpmSinConfundirConDh() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_SAMPLE);
        assertNotNull(metrics);
        assertEquals(1050.0, metrics.getIncome(), 0.001);
        assertEquals(200.0, metrics.getDistanceMi(), 0.001);
        assertEquals(5.25, metrics.getRpm(), 0.001);
    }

    @Test
    void parseaModalReal7000Sobre1431DaRpm489() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(REAL_MODAL_7000);
        assertNotNull(metrics);
        assertEquals(7000.0, metrics.getIncome(), 0.001);
        assertEquals(1431.0, metrics.getDistanceMi(), 0.001);
        assertEquals(4.89, metrics.getRpm(), 0.001);
        double exact = 7000.0 / 1431.0;
        assertTrue(Math.abs(exact - 4.891684136967156) < 1e-9);
        CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
        assertTrue(CalculatorFormulaAssertions.formatExact(exact).startsWith("4.89168"));
    }

    @Test
    void ignoraNoiseLoadboardCon506() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_PLUS_LOADBOARD_NOISE);
        assertNotNull(metrics);
        assertEquals(1431.0, metrics.getDistanceMi(), 0.001);
        assertEquals(4.89, metrics.getRpm(), 0.001);
    }

    @Test
    void noConfundeIncomeConRpmEnLayoutDeColumnas() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(COLUMN_LAYOUT);
        assertNotNull(metrics);
        assertEquals(5300.0, metrics.getIncome(), 0.001);
        assertEquals(1314.0, metrics.getDistanceMi(), 0.001);
        assertEquals(4.03, metrics.getRpm(), 0.001);
        CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
    }

    @Test
    void eligeRpmQueCumpleFormulaAunqueDhAparezcaAntes() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(WITH_DH_NOISE);
        assertNotNull(metrics);
        assertEquals(5300.0, metrics.getIncome(), 0.001);
        assertEquals(1314.0, metrics.getDistanceMi(), 0.001);
        assertEquals(4.03, metrics.getRpm(), 0.001);
        CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
    }

    @Test
    void formulaIncomeSobreDistanceIgualRpm() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_SAMPLE);
        CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
    }

    /** Texto compacto (como suele devolver Selenium): Income + Income per day en el mismo bloque. */
    private static final String COMPACT_WITH_PER_DAY = ""
            + "Calculate profit Income $ 5,300 Income per day $ 1,060/day "
            + "RPM $ 4.03/mi Distance 1,314 mi RPM with DH $ 3.81/mi Distance DH-O 70 mi "
            + "Total Distance 1,384 mi Profit / mile $ 0.85 / mi "
            + "Current profit Chicago, IL - Streamwood, IL - West Palm Beach, FL Profit: 8%, 5 days";

    @Test
    void parseaTextoCompactoConIncomePerDay() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(COMPACT_WITH_PER_DAY);
        assertNotNull(metrics);
        assertEquals(5300.0, metrics.getIncome(), 0.001);
        assertEquals(1314.0, metrics.getDistanceMi(), 0.001);
        assertEquals(4.03, metrics.getRpm(), 0.001);
        CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
    }

    /** Layout donde el valor de la tarjeta va ANTES de la etiqueta (dump real del modal). */
    private static final String MODAL_VALOR_ANTES_DE_ETIQUETA = """
            Calculate profit

            Income

            Income per day: $1,733/day

            RPM: $3.88/mi RPM with DH: $3.71/mi

            Distance: 670 mi Distance DH-O: 31 mi

            1.5

            Days on Route

            701 mi

            Total Distance

            $0.25 / mi

            Profit / mile

            Total costs: $2,428

            $ Current profit in

            $172

            Chicago, IL - Chicago Heights, IL - Atlanta, GA

            Profit: 7%, 1.5 days
            """;

    /** Income viene del input (lo inyecta CalculatorModalText); abajo está el Total income del ciclo. */
    private static final String MODAL_CON_INPUT_Y_TOTAL_INCOME = "Income: 2,600\n"
            + MODAL_VALOR_ANTES_DE_ETIQUETA + """

            Total income:$4,102 | $1,367 / day

            1,293 mi | 3 days
            """;

    @Test
    void prefiereIncomeDelInputSobreTotalIncomeDelCiclo() {
        CalculatorFinancialMetrics metrics =
                CalculatorFinancialParser.parse(MODAL_CON_INPUT_Y_TOTAL_INCOME);
        assertNotNull(metrics);
        assertEquals(2600.0, metrics.getIncome(), 0.001);
        assertEquals(670.0, metrics.getDistanceMi(), 0.001);
        assertEquals(3.88, metrics.getRpm(), 0.001);
        assertEquals(1.5, metrics.getDaysOnRoute(), 0.001);
        assertEquals(1733.0, metrics.getIncomePerDay(), 0.001);
        CalculatorFormulaAssertions.assertIncomeDividedByDistanceEqualsRpm(metrics);
        CalculatorFormulaAssertions.assertIncomeDividedByDaysEqualsIncomePerDay(metrics);
    }

    @Test
    void leeDiasEIncomePerDayConValorAntesDeLaEtiqueta() {
        CalculatorFinancialMetrics metrics =
                CalculatorFinancialParser.parse(MODAL_VALOR_ANTES_DE_ETIQUETA);
        assertNotNull(metrics);
        assertEquals(1.5, metrics.getDaysOnRoute(), 0.001);
        assertEquals(1733.0, metrics.getIncomePerDay(), 0.001);
    }

    @Test
    void leeDiasEIncomePerDayConValorDespuesDeLaEtiqueta() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(REAL_MODAL_7000);
        assertNotNull(metrics);
        assertEquals(2.5, metrics.getDaysOnRoute(), 0.001);
        assertEquals(2800.0, metrics.getIncomePerDay(), 0.001);
    }

    /** 7000 / 2.5 = 2800 exacto. */
    @Test
    void formulaIncomeSobreDiasIgualIncomePerDay() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(REAL_MODAL_7000);
        assertTrue(CalculatorFormulaAssertions.matchesIncomeDividedByDays(metrics));
        CalculatorFormulaAssertions.assertIncomeDividedByDaysEqualsIncomePerDay(metrics);
        assertEquals(2800.0, CalculatorFormulaAssertions.exactIncomeOverDays(metrics), 1e-9);
    }

    /** Como en la captura: Income $4,766 - Total costs $3,007 = Current profit $1,759. */
    private static final String MODAL_CHICAGO_NEW_YORK = """
            Income: 4,766

            Calculate profit

            Profit to New York, NY

            Income

            Income per day: $3,177/day

            RPM: $6.44/mi RPM with DH: $6.60/mi

            Distance: 736 mi Distance DH-O: 116 mi

            1.5

            Days on Route

            852 mi

            Total Distance

            $2.07 / mi

            Profit / mile

            Total costs: $3,007

            $ Current profit in

            $1,759

            Chicago, IL - Elkhart, IN - New York, NY

            Profit: 37%, 1.5 days

            Total income:$6,610 | $2,203 / day

            1,616 mi | 3 days
            """;

    @Test
    void leeTotalCostsYCurrentProfit() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_CHICAGO_NEW_YORK);
        assertNotNull(metrics);
        assertEquals(4766.0, metrics.getIncome(), 0.001);
        assertEquals(3007.0, metrics.getTotalCosts(), 0.001);
        assertEquals(1759.0, metrics.getCurrentProfit(), 0.001);
        CalculatorFormulaAssertions.assertIncomeMinusCostsEqualsProfit(metrics);
        assertEquals(1759.0, CalculatorFormulaAssertions.exactIncomeMinusCosts(metrics), 1e-9);
    }

    @Test
    void leeTotalCostsYCurrentProfitConValorEnLineaSiguiente() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(REAL_MODAL_7000);
        assertNotNull(metrics);
        assertEquals(4828.0, metrics.getTotalCosts(), 0.001);
        assertEquals(2172.0, metrics.getCurrentProfit(), 0.001);
        CalculatorFormulaAssertions.assertIncomeMinusCostsEqualsProfit(metrics);
    }

    @Test
    void noConfundeCurrentProfitConCycleProfit() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_CHICAGO_NEW_YORK);
        assertNotNull(metrics);
        assertTrue(metrics.getCurrentProfit() != 6610.0);
    }

    /** Como en la captura: Current profit $759 / Total Distance 852 mi = $0.89/mi. */
    private static final String MODAL_PROFIT_POR_MILLA = """
            Income: 3,766

            Calculate profit

            Income

            Income per day: $2,511/day

            RPM: $5.12/mi RPM with DH: $4.42/mi

            Distance: 736 mi Distance DH-O: 116 mi

            1.5

            Days on Route

            852 mi

            Total Distance

            $0.89 / mi

            Profit / mile

            Total costs: $3,007

            $ Current profit in

            $759

            Chicago, IL - Elkhart, IN - New York, NY

            Profit: 20%, 1.5 days
            """;

    @Test
    void leeTotalDistanceYProfitPorMilla() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_PROFIT_POR_MILLA);
        assertNotNull(metrics);
        assertEquals(852.0, metrics.getTotalDistanceMi(), 0.001);
        assertEquals(0.89, metrics.getProfitPerMile(), 0.001);
        // Distance sin DH sigue siendo la de la carga, no la total
        assertEquals(736.0, metrics.getDistanceMi(), 0.001);
    }

    @Test
    void formulaProfitSobreTotalDistanceIgualProfitPorMilla() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(MODAL_PROFIT_POR_MILLA);
        assertTrue(CalculatorFormulaAssertions.matchesProfitPerMile(metrics));
        CalculatorFormulaAssertions.assertProfitDividedByTotalDistanceEqualsProfitPerMile(metrics);
        assertTrue(CalculatorFormulaAssertions
                .formatExact(CalculatorFormulaAssertions.exactProfitOverTotalDistance(metrics))
                .startsWith("0.89084"));
    }

    @Test
    void leeTotalDistanceYProfitPorMillaConValorDespuesDeLaEtiqueta() {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(REAL_MODAL_7000);
        assertNotNull(metrics);
        assertEquals(1432.0, metrics.getTotalDistanceMi(), 0.001);
        assertEquals(1.52, metrics.getProfitPerMile(), 0.001);
    }

    /** Como en la captura: 8,700 - 4,562 = 4,138; 4,138 / 8,700 = 47.56% y la UI muestra 48%. */
    private static final String MODAL_PORCENTAJE_REDONDEADO = """
            Income: 8,700

            Calculate profit

            Profit to Fort Pierce, FL

            Income

            Income per day: $3,480/day

            RPM: $6.69/mi RPM with DH: $6.53/mi

            Distance: 1,301 mi Distance DH-O: 31 mi

            2.5

            Days on Route

            1,333 mi

            Total Distance

            $3.11 / mi

            Profit / mile

            Total costs: $4,562

            $ Current profit in

            $4,138

            Chicago, IL - Romeoville, IL - Fort Pierce, FL

            Profit: 48%, 2.5 days
            """;

    @Test
    void leeProfitPorcentajeRedondeadoDeLaUi() {
        CalculatorFinancialMetrics metrics =
                CalculatorFinancialParser.parse(MODAL_PORCENTAJE_REDONDEADO);
        assertNotNull(metrics);
        assertEquals(8700.0, metrics.getIncome(), 0.001);
        assertEquals(4562.0, metrics.getTotalCosts(), 0.001);
        assertEquals(4138.0, metrics.getCurrentProfit(), 0.001);
        assertEquals(48.0, metrics.getProfitPercent(), 0.001);
    }

    /** 4138 / 8700 = 0.475632183908046 → 47.5632% que la UI redondea a 48%. */
    @Test
    void formulaProfitSobreIncomeIgualProfitPorcentajeConRedondeo() {
        CalculatorFinancialMetrics metrics =
                CalculatorFinancialParser.parse(MODAL_PORCENTAJE_REDONDEADO);
        assertTrue(CalculatorFormulaAssertions.matchesProfitPercent(metrics));
        CalculatorFormulaAssertions.assertProfitOverIncomeEqualsProfitPercent(metrics);
        assertTrue(CalculatorFormulaAssertions
                .formatExact(metrics.getCurrentProfit() / metrics.getIncome())
                .startsWith("0.4756321839"));
        assertEquals(47.5632, CalculatorFormulaAssertions.exactProfitPercent(metrics), 0.0001);
    }

    /** 172 / 2600 = 6.61% y la UI muestra 7%: el redondeo hacia arriba también vale. */
    @Test
    void aceptaRedondeoHaciaArribaDelPorcentaje() {
        CalculatorFinancialMetrics metrics =
                new CalculatorFinancialMetrics(2600, 670, 3.88, 1.5, 1733.0, 2428.0, 172.0,
                        701.0, 0.25, 7.0);
        assertTrue(CalculatorFormulaAssertions.matchesProfitPercent(metrics));
        CalculatorFormulaAssertions.assertProfitOverIncomeEqualsProfitPercent(metrics);
    }

    @Test
    void detectaProfitPorcentajeQueNoCuadra() {
        CalculatorFinancialMetrics metrics =
                new CalculatorFinancialMetrics(8700, 1301, 6.69, 2.5, 3480.0, 4562.0, 4138.0,
                        1333.0, 3.11, 61.0);
        assertTrue(!CalculatorFormulaAssertions.matchesProfitPercent(metrics));
        assertThrows(AssertionError.class,
                () -> CalculatorFormulaAssertions.assertProfitOverIncomeEqualsProfitPercent(metrics));
    }

    @Test
    void detectaProfitPorMillaQueNoCuadra() {
        CalculatorFinancialMetrics metrics = new CalculatorFinancialMetrics(
                3766, 736, 5.12, 1.5, 2511.0, 3007.0, 759.0, 852.0, 1.40);
        assertTrue(!CalculatorFormulaAssertions.matchesProfitPerMile(metrics));
        assertThrows(AssertionError.class, () -> CalculatorFormulaAssertions
                .assertProfitDividedByTotalDistanceEqualsProfitPerMile(metrics));
    }

    @Test
    void detectaCurrentProfitQueNoCuadra() {
        CalculatorFinancialMetrics metrics =
                new CalculatorFinancialMetrics(4766, 736, 6.47, 1.5, 3177.0, 3007.0, 1200.0);
        assertTrue(!CalculatorFormulaAssertions.matchesIncomeMinusCosts(metrics));
        assertThrows(AssertionError.class,
                () -> CalculatorFormulaAssertions.assertIncomeMinusCostsEqualsProfit(metrics));
    }

    @Test
    void detectaIncomePerDayQueNoCuadra() {
        CalculatorFinancialMetrics metrics =
                new CalculatorFinancialMetrics(7000, 1431, 4.89, 2.5, 1900.0);
        assertTrue(!CalculatorFormulaAssertions.matchesIncomeDividedByDays(metrics));
        assertThrows(AssertionError.class,
                () -> CalculatorFormulaAssertions.assertIncomeDividedByDaysEqualsIncomePerDay(metrics));
    }
}
