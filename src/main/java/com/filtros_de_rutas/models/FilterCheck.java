package com.filtros_de_rutas.models;

public class FilterCheck {

    public enum Status { PASS, FAIL, SKIP }

    private final String filterName;
    private final String appliedValue;
    private final RouteFooterTotals baseline;
    private final RouteFooterTotals afterFilter;
    private final RouteFooterTotals afterReset;
    private final Status status;
    private final String detail;

    public FilterCheck(String filterName, String appliedValue, RouteFooterTotals baseline,
                       RouteFooterTotals afterFilter, RouteFooterTotals afterReset,
                       Status status, String detail) {
        this.filterName = filterName;
        this.appliedValue = appliedValue;
        this.baseline = baseline;
        this.afterFilter = afterFilter;
        this.afterReset = afterReset;
        this.status = status;
        this.detail = detail;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getAppliedValue() {
        return appliedValue;
    }

    public RouteFooterTotals getBaseline() {
        return baseline;
    }

    public RouteFooterTotals getAfterFilter() {
        return afterFilter;
    }

    public RouteFooterTotals getAfterReset() {
        return afterReset;
    }

    public Status getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
