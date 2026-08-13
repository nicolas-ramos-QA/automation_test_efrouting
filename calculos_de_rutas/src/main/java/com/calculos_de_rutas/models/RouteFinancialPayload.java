package com.calculos_de_rutas.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Payload financiero del endpoint user-route: lanes + tarifa de costo operativo de la ruta.
 */
public class RouteFinancialPayload {

    private final String routeId;
    private final String routeName;
    private final Double operativeCostRate;
    private final Double routeMileage;
    private final RouteTotals totals;
    private final List<LaneFinancials> lanes;
    private final String rawJson;

    private RouteFinancialPayload(Builder builder) {
        this.routeId = builder.routeId;
        this.routeName = builder.routeName;
        this.operativeCostRate = builder.operativeCostRate;
        this.routeMileage = builder.routeMileage;
        this.totals = builder.totals;
        this.lanes = Collections.unmodifiableList(new ArrayList<>(builder.lanes));
        this.rawJson = builder.rawJson;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    /** Tarifa de costo operativo de la ruta por milla (operativeCost.total). */
    public Double getOperativeCostRate() {
        return operativeCostRate;
    }

    /** Millaje total informado por el Backend a nivel de ruta (operative.mileage.estimated). */
    public Double getRouteMileage() {
        return routeMileage;
    }

    /** Acumulados globales del bloque finance de la ruta; null si el Backend no los envía. */
    public RouteTotals getTotals() {
        return totals;
    }

    public List<LaneFinancials> getLanes() {
        return lanes;
    }

    public String getRawJson() {
        return rawJson;
    }

    public double sumFuel() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getFuelCost())).sum();
    }

    public double sumToll() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getTollCost())).sum();
    }

    public double sumCustom() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getCustomCost())).sum();
    }

    public double sumTotalCostWithoutOp() {
        return lanes.stream().mapToDouble(LaneFinancials::expectedTotalCostWithoutOp).sum();
    }

    public double sumProfitWithoutOp() {
        return lanes.stream().mapToDouble(LaneFinancials::expectedProfitWithoutOp).sum();
    }

    /**
     * Profit total (rango mínimo) cuando Op cost está oculto: suma del profit de cada
     * lane sin descontar el Op (Income − Fuel − Toll − Custom por lane).
     */
    public double sumProfitMinWithoutOp() {
        return lanes.stream().mapToDouble(LaneFinancials::profitReferenceMin).sum();
    }

    /**
     * Profit total (rango máximo) cuando Op cost está oculto: suma del profit de cada
     * lane sin descontar el Op (Income − Fuel − Toll − Custom por lane).
     */
    public double sumProfitMaxWithoutOp() {
        return lanes.stream().mapToDouble(LaneFinancials::profitReferenceMax).sum();
    }

    public double sumIncomeMin() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getIncomeMin())).sum();
    }

    public double sumIncomeMax() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getIncomeMax())).sum();
    }

    public double sumOperationTime() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getOperationTime())).sum();
    }

    public double sumMileage() {
        return lanes.stream().mapToDouble(l -> l.safe(l.getMileage())).sum();
    }

    /** Op cost total de la ruta: tarifa por milla aplicada a todas las lanes. */
    public double sumOpCost() {
        return lanes.stream().mapToDouble(l -> l.opCostFrom(operativeCostRate)).sum();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String routeId;
        private String routeName;
        private Double operativeCostRate;
        private Double routeMileage;
        private RouteTotals totals;
        private final List<LaneFinancials> lanes = new ArrayList<>();
        private String rawJson;

        public Builder routeId(String routeId) {
            this.routeId = routeId;
            return this;
        }

        public Builder routeName(String routeName) {
            this.routeName = routeName;
            return this;
        }

        public Builder operativeCostRate(Double operativeCostRate) {
            this.operativeCostRate = operativeCostRate;
            return this;
        }

        public Builder routeMileage(Double routeMileage) {
            this.routeMileage = routeMileage;
            return this;
        }

        public Builder totals(RouteTotals totals) {
            this.totals = totals;
            return this;
        }

        public Builder addLane(LaneFinancials lane) {
            this.lanes.add(lane);
            return this;
        }

        public Builder rawJson(String rawJson) {
            this.rawJson = rawJson;
            return this;
        }

        public RouteFinancialPayload build() {
            return new RouteFinancialPayload(this);
        }
    }
}
