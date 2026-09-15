package com.validacion_calculadora.models;

/**
 * Resultado de una validación exitosa de la calculadora (valores variables por carga).
 */
public final class CalculatorValidationResult {

    private final String citiesLine;
    private final String profitPercent;
    private final String daysText;
    private final CalculatorFinancialMetrics financialMetrics;

    public CalculatorValidationResult(String citiesLine, String profitPercent, String daysText) {
        this(citiesLine, profitPercent, daysText, null);
    }

    public CalculatorValidationResult(
            String citiesLine,
            String profitPercent,
            String daysText,
            CalculatorFinancialMetrics financialMetrics) {
        this.citiesLine = citiesLine;
        this.profitPercent = profitPercent;
        this.daysText = daysText;
        this.financialMetrics = financialMetrics;
    }

    public String getCitiesLine() {
        return citiesLine;
    }

    public String getProfitPercent() {
        return profitPercent;
    }

    public String getDaysText() {
        return daysText;
    }

    public CalculatorFinancialMetrics getFinancialMetrics() {
        return financialMetrics;
    }

    public CalculatorValidationResult withFinancialMetrics(CalculatorFinancialMetrics metrics) {
        return new CalculatorValidationResult(citiesLine, profitPercent, daysText, metrics);
    }

    @Override
    public String toString() {
        String base = "ciudades=[" + citiesLine + "] profit=" + profitPercent
                + (daysText == null || daysText.isBlank() ? "" : ", " + daysText);
        if (financialMetrics != null) {
            base += " | " + financialMetrics
                    + " | fórmulas OK: Income/Distance=RPM";
            if (financialMetrics.hasIncomePerDayData()) {
                base += ", Income/Días=Income per day";
            }
            if (financialMetrics.hasProfitData()) {
                base += ", Income-Total costs=Current profit";
            }
            if (financialMetrics.hasProfitPerMileData()) {
                base += ", Current profit/Total Distance=Profit per mile";
            }
            if (financialMetrics.hasProfitPercentData()) {
                base += ", Current profit/Income=Profit %";
            }
        }
        return base;
    }
}
