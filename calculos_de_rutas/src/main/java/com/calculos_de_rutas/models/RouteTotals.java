package com.calculos_de_rutas.models;

/**
 * Bloque {@code finance} a nivel de ruta que devuelve el Backend, con los acumulados globales.
 *
 * <p>A diferencia del {@code finance.totalCost} de cada lane, el {@code totalCost} de la ruta
 * ya incluye el costo operativo (tarifa por milla x millaje total).</p>
 */
public class RouteTotals {

    private final Double incomeMin;
    private final Double incomeMax;
    private final Double incomeAvg;
    private final Double profitMin;
    private final Double profitMax;
    private final Double profitAvg;
    private final Double netProfitMin;
    private final Double netProfitMax;
    private final Double netProfitAvg;
    private final Double fuelCost;
    private final Double tollCost;
    private final Double customCost;
    private final Double operativeCost;
    private final Double totalCost;

    private RouteTotals(Builder builder) {
        this.incomeMin = builder.incomeMin;
        this.incomeMax = builder.incomeMax;
        this.incomeAvg = builder.incomeAvg;
        this.profitMin = builder.profitMin;
        this.profitMax = builder.profitMax;
        this.profitAvg = builder.profitAvg;
        this.netProfitMin = builder.netProfitMin;
        this.netProfitMax = builder.netProfitMax;
        this.netProfitAvg = builder.netProfitAvg;
        this.fuelCost = builder.fuelCost;
        this.tollCost = builder.tollCost;
        this.customCost = builder.customCost;
        this.operativeCost = builder.operativeCost;
        this.totalCost = builder.totalCost;
    }

    public Double getIncomeMin() {
        return incomeMin;
    }

    public Double getIncomeMax() {
        return incomeMax;
    }

    public Double getIncomeAvg() {
        return incomeAvg;
    }

    public Double getProfitMin() {
        return profitMin;
    }

    public Double getProfitMax() {
        return profitMax;
    }

    public Double getProfitAvg() {
        return profitAvg;
    }

    public Double getNetProfitMin() {
        return netProfitMin;
    }

    public Double getNetProfitMax() {
        return netProfitMax;
    }

    public Double getNetProfitAvg() {
        return netProfitAvg;
    }

    public Double getFuelCost() {
        return fuelCost;
    }

    public Double getTollCost() {
        return tollCost;
    }

    public Double getCustomCost() {
        return customCost;
    }

    /** Tarifa operativa por milla informada dentro del bloque finance. */
    public Double getOperativeCost() {
        return operativeCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double incomeMin;
        private Double incomeMax;
        private Double incomeAvg;
        private Double profitMin;
        private Double profitMax;
        private Double profitAvg;
        private Double netProfitMin;
        private Double netProfitMax;
        private Double netProfitAvg;
        private Double fuelCost;
        private Double tollCost;
        private Double customCost;
        private Double operativeCost;
        private Double totalCost;

        public Builder incomeMin(Double value) {
            this.incomeMin = value;
            return this;
        }

        public Builder incomeMax(Double value) {
            this.incomeMax = value;
            return this;
        }

        public Builder incomeAvg(Double value) {
            this.incomeAvg = value;
            return this;
        }

        public Builder profitMin(Double value) {
            this.profitMin = value;
            return this;
        }

        public Builder profitMax(Double value) {
            this.profitMax = value;
            return this;
        }

        public Builder profitAvg(Double value) {
            this.profitAvg = value;
            return this;
        }

        public Builder netProfitMin(Double value) {
            this.netProfitMin = value;
            return this;
        }

        public Builder netProfitMax(Double value) {
            this.netProfitMax = value;
            return this;
        }

        public Builder netProfitAvg(Double value) {
            this.netProfitAvg = value;
            return this;
        }

        public Builder fuelCost(Double value) {
            this.fuelCost = value;
            return this;
        }

        public Builder tollCost(Double value) {
            this.tollCost = value;
            return this;
        }

        public Builder customCost(Double value) {
            this.customCost = value;
            return this;
        }

        public Builder operativeCost(Double value) {
            this.operativeCost = value;
            return this;
        }

        public Builder totalCost(Double value) {
            this.totalCost = value;
            return this;
        }

        public RouteTotals build() {
            return new RouteTotals(this);
        }
    }
}
