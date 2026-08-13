package com.calculos_de_rutas.models;

/**
 * Valores financieros y operativos de una lane (tramo), tal como vienen del Backend.
 *
 * <p>Nota sobre el Backend de efRouting: {@code finance.totalCost} agrupa fuel + toll + custom,
 * sin incluir el costo operativo. El Op cost se deriva de la tarifa global
 * {@code operativeCost.total} y del tiempo de operación de la lane.</p>
 */
public class LaneFinancials {

    private final String laneId;
    private final String type;
    private final String origin;
    private final String destination;

    private final Double incomeMin;
    private final Double incomeMax;
    private final Double incomeAvg;
    private final Double profitMin;
    private final Double profitMax;
    private final Double profitAvg;
    private final Double fuelCost;
    private final Double tollCost;
    private final Double customCost;
    private final Double totalCost;

    private final Double mileage;
    private final Double drivingTime;
    private final Double operationTime;

    private LaneFinancials(Builder builder) {
        this.laneId = builder.laneId;
        this.type = builder.type;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.incomeMin = builder.incomeMin;
        this.incomeMax = builder.incomeMax;
        this.incomeAvg = builder.incomeAvg;
        this.profitMin = builder.profitMin;
        this.profitMax = builder.profitMax;
        this.profitAvg = builder.profitAvg;
        this.fuelCost = builder.fuelCost;
        this.tollCost = builder.tollCost;
        this.customCost = builder.customCost;
        this.totalCost = builder.totalCost;
        this.mileage = builder.mileage;
        this.drivingTime = builder.drivingTime;
        this.operationTime = builder.operationTime;
    }

    public String getLaneId() {
        return laneId;
    }

    public String getType() {
        return type;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
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

    public Double getFuelCost() {
        return fuelCost;
    }

    public Double getTollCost() {
        return tollCost;
    }

    public Double getCustomCost() {
        return customCost;
    }

    /** Costo total del Backend: fuel + toll + custom (sin Op cost). */
    public Double getTotalCost() {
        return totalCost;
    }

    public Double getMileage() {
        return mileage;
    }

    public Double getDrivingTime() {
        return drivingTime;
    }

    public Double getOperationTime() {
        return operationTime;
    }

    public double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    public double incomeReference() {
        if (incomeAvg != null) {
            return incomeAvg;
        }
        if (incomeMin != null && incomeMax != null) {
            return (incomeMin + incomeMax) / 2.0;
        }
        return safe(incomeMin != null ? incomeMin : incomeMax);
    }

    /** Suma explícita de los componentes reportados por el Backend, sin Op cost. */
    public double sumOfCostsWithoutOp() {
        return safe(fuelCost) + safe(tollCost) + safe(customCost);
    }

    /**
     * Op cost de la lane: la tarifa operativa de la ruta se cobra por milla recorrida.
     */
    public double opCostFrom(Double ratePerMile) {
        if (ratePerMile == null || mileage == null) {
            return 0.0;
        }
        return ratePerMile * mileage;
    }

    /** Costo total esperado cuando el Op cost está visible en la UI. */
    public double expectedTotalCostWithOp(double opCost) {
        return expectedTotalCostWithoutOp() + opCost;
    }

    /** Costo total esperado cuando el Op cost está oculto. */
    public double expectedTotalCostWithoutOp() {
        return totalCost != null ? totalCost : sumOfCostsWithoutOp();
    }

    /**
     * Profit que muestra la UI por lane con el Op cost visible: el {@code profit} del Backend ya
     * descuenta fuel + toll + custom, y la pantalla le resta además el costo operativo.
     */
    public double expectedProfitMinWithOp(double opCost) {
        return profitReferenceMin() - opCost;
    }

    public double expectedProfitMaxWithOp(double opCost) {
        return profitReferenceMax() - opCost;
    }

    /** Profit del Backend sin descontar Op cost (rango mínimo). */
    public double profitReferenceMin() {
        if (profitMin != null) {
            return profitMin;
        }
        return safe(incomeMin) - expectedTotalCostWithoutOp();
    }

    /** Profit del Backend sin descontar Op cost (rango máximo). */
    public double profitReferenceMax() {
        if (profitMax != null) {
            return profitMax;
        }
        return safe(incomeMax) - expectedTotalCostWithoutOp();
    }

    public double expectedProfitWithOp(double opCost) {
        return incomeReference() - expectedTotalCostWithOp(opCost);
    }

    public double expectedProfitWithoutOp() {
        if (profitAvg != null) {
            return profitAvg;
        }
        return incomeReference() - expectedTotalCostWithoutOp();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String laneId;
        private String type;
        private String origin;
        private String destination;
        private Double incomeMin;
        private Double incomeMax;
        private Double incomeAvg;
        private Double profitMin;
        private Double profitMax;
        private Double profitAvg;
        private Double fuelCost;
        private Double tollCost;
        private Double customCost;
        private Double totalCost;
        private Double mileage;
        private Double drivingTime;
        private Double operationTime;

        public Builder laneId(String laneId) {
            this.laneId = laneId;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder incomeMin(Double incomeMin) {
            this.incomeMin = incomeMin;
            return this;
        }

        public Builder incomeMax(Double incomeMax) {
            this.incomeMax = incomeMax;
            return this;
        }

        public Builder incomeAvg(Double incomeAvg) {
            this.incomeAvg = incomeAvg;
            return this;
        }

        public Builder profitMin(Double profitMin) {
            this.profitMin = profitMin;
            return this;
        }

        public Builder profitMax(Double profitMax) {
            this.profitMax = profitMax;
            return this;
        }

        public Builder profitAvg(Double profitAvg) {
            this.profitAvg = profitAvg;
            return this;
        }

        public Builder fuelCost(Double fuelCost) {
            this.fuelCost = fuelCost;
            return this;
        }

        public Builder tollCost(Double tollCost) {
            this.tollCost = tollCost;
            return this;
        }

        public Builder customCost(Double customCost) {
            this.customCost = customCost;
            return this;
        }

        public Builder totalCost(Double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder mileage(Double mileage) {
            this.mileage = mileage;
            return this;
        }

        public Builder drivingTime(Double drivingTime) {
            this.drivingTime = drivingTime;
            return this;
        }

        public Builder operationTime(Double operationTime) {
            this.operationTime = operationTime;
            return this;
        }

        public LaneFinancials build() {
            return new LaneFinancials(this);
        }
    }

    @Override
    public String toString() {
        return "Lane " + laneId + " " + origin + " -> " + destination
                + " {income=" + incomeMin + "-" + incomeMax
                + ", fuel=" + fuelCost + ", toll=" + tollCost + ", custom=" + customCost
                + ", totalCost=" + totalCost + ", profit=" + profitAvg + "}";
    }
}
