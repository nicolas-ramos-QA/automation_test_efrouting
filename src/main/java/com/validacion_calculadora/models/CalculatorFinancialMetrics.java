package com.validacion_calculadora.models;

/**
 * Métricas financieras del modal Calculate profit usadas en las fórmulas.
 */
public final class CalculatorFinancialMetrics {

    private final double income;
    private final double distanceMi;
    private final double rpm;
    private final Double daysOnRoute;
    private final Double incomePerDay;
    private final Double totalCosts;
    private final Double currentProfit;
    private final Double totalDistanceMi;
    private final Double profitPerMile;
    private final Double profitPercent;

    public CalculatorFinancialMetrics(double income, double distanceMi, double rpm) {
        this(income, distanceMi, rpm, null, null, null, null);
    }

    public CalculatorFinancialMetrics(double income, double distanceMi, double rpm,
                                      Double daysOnRoute, Double incomePerDay) {
        this(income, distanceMi, rpm, daysOnRoute, incomePerDay, null, null);
    }

    public CalculatorFinancialMetrics(double income, double distanceMi, double rpm,
                                      Double daysOnRoute, Double incomePerDay,
                                      Double totalCosts, Double currentProfit) {
        this(income, distanceMi, rpm, daysOnRoute, incomePerDay, totalCosts, currentProfit, null, null);
    }

    public CalculatorFinancialMetrics(double income, double distanceMi, double rpm,
                                      Double daysOnRoute, Double incomePerDay,
                                      Double totalCosts, Double currentProfit,
                                      Double totalDistanceMi, Double profitPerMile) {
        this(income, distanceMi, rpm, daysOnRoute, incomePerDay, totalCosts, currentProfit,
                totalDistanceMi, profitPerMile, null);
    }

    public CalculatorFinancialMetrics(double income, double distanceMi, double rpm,
                                      Double daysOnRoute, Double incomePerDay,
                                      Double totalCosts, Double currentProfit,
                                      Double totalDistanceMi, Double profitPerMile,
                                      Double profitPercent) {
        this.profitPercent = profitPercent;
        this.income = income;
        this.distanceMi = distanceMi;
        this.rpm = rpm;
        this.daysOnRoute = daysOnRoute;
        this.incomePerDay = incomePerDay;
        this.totalCosts = totalCosts;
        this.currentProfit = currentProfit;
        this.totalDistanceMi = totalDistanceMi;
        this.profitPerMile = profitPerMile;
    }

    public double getIncome() {
        return income;
    }

    public double getDistanceMi() {
        return distanceMi;
    }

    public double getRpm() {
        return rpm;
    }

    /** "Days on Route" del modal; {@code null} si no se pudo leer. */
    public Double getDaysOnRoute() {
        return daysOnRoute;
    }

    /** "Income per day" del modal; {@code null} si no se pudo leer. */
    public Double getIncomePerDay() {
        return incomePerDay;
    }

    /** "Total costs" del modal; {@code null} si no se pudo leer. */
    public Double getTotalCosts() {
        return totalCosts;
    }

    /** "Current profit" del modal; {@code null} si no se pudo leer. */
    public Double getCurrentProfit() {
        return currentProfit;
    }

    public boolean hasIncomePerDayData() {
        return daysOnRoute != null && daysOnRoute > 0 && incomePerDay != null;
    }

    /** "Total Distance" (incluye deadhead); {@code null} si no se pudo leer. */
    public Double getTotalDistanceMi() {
        return totalDistanceMi;
    }

    /** "Profit / mile" del modal; {@code null} si no se pudo leer. */
    public Double getProfitPerMile() {
        return profitPerMile;
    }

    public boolean hasProfitData() {
        return totalCosts != null && currentProfit != null;
    }

    /** "Profit: 48%" del modal, ya redondeado por la UI; {@code null} si no se pudo leer. */
    public Double getProfitPercent() {
        return profitPercent;
    }

    public boolean hasProfitPercentData() {
        return currentProfit != null && income > 0 && profitPercent != null;
    }

    public boolean hasProfitPerMileData() {
        return currentProfit != null
                && totalDistanceMi != null && totalDistanceMi > 0
                && profitPerMile != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Income=$" + plain(income)
                + " Distance=" + plain(distanceMi) + " mi"
                + " RPM=$" + plain(rpm) + "/mi");
        if (hasIncomePerDayData()) {
            sb.append(" Days=").append(plain(daysOnRoute))
                    .append(" IncomePerDay=$").append(plain(incomePerDay)).append("/day");
        }
        if (hasProfitData()) {
            sb.append(" TotalCosts=$").append(plain(totalCosts))
                    .append(" CurrentProfit=$").append(plain(currentProfit));
        }
        if (hasProfitPerMileData()) {
            sb.append(" TotalDistance=").append(plain(totalDistanceMi)).append(" mi")
                    .append(" ProfitPerMile=$").append(plain(profitPerMile)).append("/mi");
        }
        if (hasProfitPercentData()) {
            sb.append(" Profit%=").append(plain(profitPercent)).append("%");
        }
        return sb.toString();
    }

    private static String plain(double value) {
        return new java.math.BigDecimal(Double.toString(value)).stripTrailingZeros().toPlainString();
    }
}
