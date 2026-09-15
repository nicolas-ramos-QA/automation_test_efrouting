package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Fórmula: Income ÷ Distance = RPM.
 * Se compara el valor exacto de la división con el RPM de la UI (±0.01, por los 2 decimales de pantalla).
 * En logs/reporte se muestra el cociente exacto, sin redondear.
 */
public final class CalculatorFormulaAssertions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorFormulaAssertions.class);

    /** Tolerancia vs RPM mostrado en UI (2 decimales). */
    private static final double TOLERANCE = 0.01;

    /** Income per day se muestra en dólares enteros: el redondeo puede desviar hasta ~1. */
    private static final double TOLERANCE_PER_DAY = 1.0;

    /** Income, Total costs y Current profit se muestran redondeados a dólares enteros. */
    private static final double TOLERANCE_PROFIT = 1.0;

    /**
     * La UI muestra el Profit % redondeado al entero más cercano (47.56% → 48%), así que
     * la diferencia admisible es medio punto porcentual más el margen de los importes
     * que ya vienen redondeados a dólares.
     */
    private static final double TOLERANCE_PERCENT = 0.51;

    private CalculatorFormulaAssertions() {}

    public static double exactIncomeOverDays(CalculatorFinancialMetrics metrics) {
        return metrics.getIncome() / metrics.getDaysOnRoute();
    }

    public static boolean matchesIncomeDividedByDays(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasIncomePerDayData()) {
            return false;
        }
        return Math.abs(exactIncomeOverDays(metrics) - metrics.getIncomePerDay()) <= TOLERANCE_PER_DAY;
    }

    /** Fórmula: Income ÷ Days on Route = Income per day. */
    public static void assertIncomeDividedByDaysEqualsIncomePerDay(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasIncomePerDayData()) {
            throw new AssertionError(
                    "No se pudieron leer Days on Route / Income per day del modal Calculate profit.");
        }
        double exact = exactIncomeOverDays(metrics);
        double uiPerDay = metrics.getIncomePerDay();
        double delta = Math.abs(exact - uiPerDay);

        String detail = String.format(Locale.US,
                "Income (%s) / Days on Route (%s) = %s  |  Income per day UI = %s  |  Δ=%s",
                formatExact(metrics.getIncome()),
                formatExact(metrics.getDaysOnRoute()),
                formatExact(exact),
                formatExact(uiPerDay),
                formatExact(delta));

        LOGGER.info("Validación Income per day (sin redondear el cociente): {}", detail);

        if (delta > TOLERANCE_PER_DAY) {
            throw new AssertionError(
                    "Fórmula fallida: Income / Days on Route ≠ Income per day. " + detail);
        }
    }

    public static double exactIncomeMinusCosts(CalculatorFinancialMetrics metrics) {
        return metrics.getIncome() - metrics.getTotalCosts();
    }

    public static boolean matchesIncomeMinusCosts(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasProfitData()) {
            return false;
        }
        return Math.abs(exactIncomeMinusCosts(metrics) - metrics.getCurrentProfit()) <= TOLERANCE_PROFIT;
    }

    /** Fórmula: Income − Total costs = Current profit. */
    public static void assertIncomeMinusCostsEqualsProfit(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasProfitData()) {
            throw new AssertionError(
                    "No se pudieron leer Total costs / Current profit del modal Calculate profit.");
        }
        double exact = exactIncomeMinusCosts(metrics);
        double uiProfit = metrics.getCurrentProfit();
        double delta = Math.abs(exact - uiProfit);

        String detail = String.format(Locale.US,
                "Income (%s) - Total costs (%s) = %s  |  Current profit UI = %s  |  Δ=%s",
                formatExact(metrics.getIncome()),
                formatExact(metrics.getTotalCosts()),
                formatExact(exact),
                formatExact(uiProfit),
                formatExact(delta));

        LOGGER.info("Validación Current profit: {}", detail);

        if (delta > TOLERANCE_PROFIT) {
            throw new AssertionError(
                    "Fórmula fallida: Income - Total costs ≠ Current profit. " + detail);
        }
    }

    public static double exactProfitOverTotalDistance(CalculatorFinancialMetrics metrics) {
        return metrics.getCurrentProfit() / metrics.getTotalDistanceMi();
    }

    public static boolean matchesProfitPerMile(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasProfitPerMileData()) {
            return false;
        }
        return Math.abs(exactProfitOverTotalDistance(metrics) - metrics.getProfitPerMile()) <= TOLERANCE;
    }

    /** Fórmula: Current profit ÷ Total Distance = Profit / mile. */
    public static void assertProfitDividedByTotalDistanceEqualsProfitPerMile(
            CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasProfitPerMileData()) {
            throw new AssertionError(
                    "No se pudieron leer Total Distance / Profit per mile del modal Calculate profit.");
        }
        double exact = exactProfitOverTotalDistance(metrics);
        double uiPerMile = metrics.getProfitPerMile();
        double delta = Math.abs(exact - uiPerMile);

        String detail = String.format(Locale.US,
                "Current profit (%s) / Total Distance (%s mi) = %s  |  Profit per mile UI = %s  |  Δ=%s",
                formatExact(metrics.getCurrentProfit()),
                formatExact(metrics.getTotalDistanceMi()),
                formatExact(exact),
                formatExact(uiPerMile),
                formatExact(delta));

        LOGGER.info("Validación Profit per mile (sin redondear el cociente): {}", detail);

        if (delta > TOLERANCE) {
            throw new AssertionError(
                    "Fórmula fallida: Current profit / Total Distance ≠ Profit per mile. " + detail);
        }
    }

    /** (Income − Total costs) ÷ Income × 100, sin redondear. */
    public static double exactProfitPercent(CalculatorFinancialMetrics metrics) {
        return metrics.getCurrentProfit() / metrics.getIncome() * 100.0;
    }

    public static boolean matchesProfitPercent(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasProfitPercentData()) {
            return false;
        }
        return Math.abs(exactProfitPercent(metrics) - metrics.getProfitPercent()) <= TOLERANCE_PERCENT;
    }

    /** Fórmula: Income − Total costs = Profit, y Profit ÷ Income = Profit %. */
    public static void assertProfitOverIncomeEqualsProfitPercent(CalculatorFinancialMetrics metrics) {
        if (metrics == null || !metrics.hasProfitPercentData()) {
            throw new AssertionError(
                    "No se pudieron leer Current profit / Profit % del modal Calculate profit.");
        }
        double exact = exactProfitPercent(metrics);
        double uiPercent = metrics.getProfitPercent();
        double delta = Math.abs(exact - uiPercent);

        String detail = String.format(Locale.US,
                "Current profit (%s) / Income (%s) = %s → %s%%  |  Profit %% UI = %s%%  |  Δ=%s",
                formatExact(metrics.getCurrentProfit()),
                formatExact(metrics.getIncome()),
                formatExact(metrics.getCurrentProfit() / metrics.getIncome()),
                formatExact(exact),
                formatExact(uiPercent),
                formatExact(delta));

        LOGGER.info("Validación Profit % (la UI redondea al entero): {}", detail);

        if (delta > TOLERANCE_PERCENT) {
            throw new AssertionError(
                    "Fórmula fallida: Current profit / Income ≠ Profit %. " + detail);
        }
    }

    public static double exactIncomeOverDistance(CalculatorFinancialMetrics metrics) {
        return metrics.getIncome() / metrics.getDistanceMi();
    }

    public static boolean matchesIncomeDividedByDistance(CalculatorFinancialMetrics metrics) {
        if (metrics == null || metrics.getDistanceMi() <= 0) {
            return false;
        }
        double exact = exactIncomeOverDistance(metrics);
        return Math.abs(exact - metrics.getRpm()) <= TOLERANCE;
    }

    public static void assertIncomeDividedByDistanceEqualsRpm(CalculatorFinancialMetrics metrics) {
        if (metrics == null) {
            throw new AssertionError(
                    "No se pudieron leer Income, Distance y RPM del modal Calculate profit.");
        }
        double exact = exactIncomeOverDistance(metrics);
        double uiRpm = metrics.getRpm();
        double delta = Math.abs(exact - uiRpm);

        String detail = String.format(Locale.US,
                "Income (%s) / Distance (%s mi) = %s  |  RPM UI = %s  |  Δ=%s",
                formatExact(metrics.getIncome()),
                formatExact(metrics.getDistanceMi()),
                formatExact(exact),
                formatExact(uiRpm),
                formatExact(delta));

        LOGGER.info("Validación RPM (sin redondear el cociente): {}", detail);

        if (delta > TOLERANCE) {
            throw new AssertionError(
                    "Fórmula fallida: Income / Distance ≠ RPM. " + detail);
        }
    }

    public static CalculatorFinancialMetrics parseAndAssertRpm(String modalText) {
        CalculatorFinancialMetrics metrics = CalculatorFinancialParser.parse(modalText);
        assertIncomeDividedByDistanceEqualsRpm(metrics);
        return metrics;
    }

    /** Formato exacto: quita ceros basura del double sin forzar 2 decimales. */
    public static String formatExact(double value) {
        return new java.math.BigDecimal(Double.toString(value))
                .stripTrailingZeros()
                .toPlainString();
    }
}
