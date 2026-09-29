package com.filtros_de_rutas.models;

import java.util.Objects;

/**
 * Totales del pie del listado de rutas (y el contador Results de la cabecera).
 */
public class RouteFooterTotals {

    private final Integer resultsCount;
    private final Integer totalRoutes;
    private final String totalIncome;
    private final String totalMiles;
    private final String dhMiles;
    private final String effectiveRpm;
    private final String loadedRpm;
    private final String rawText;

    public RouteFooterTotals(Integer resultsCount, Integer totalRoutes, String totalIncome,
                             String totalMiles, String dhMiles, String effectiveRpm,
                             String loadedRpm, String rawText) {
        this.resultsCount = resultsCount;
        this.totalRoutes = totalRoutes;
        this.totalIncome = totalIncome;
        this.totalMiles = totalMiles;
        this.dhMiles = dhMiles;
        this.effectiveRpm = effectiveRpm;
        this.loadedRpm = loadedRpm;
        this.rawText = rawText;
    }

    public Integer getResultsCount() {
        return resultsCount;
    }

    public Integer getTotalRoutes() {
        return totalRoutes;
    }

    public String getTotalIncome() {
        return totalIncome;
    }

    public String getTotalMiles() {
        return totalMiles;
    }

    public String getDhMiles() {
        return dhMiles;
    }

    public String getEffectiveRpm() {
        return effectiveRpm;
    }

    public String getLoadedRpm() {
        return loadedRpm;
    }

    public String getRawText() {
        return rawText;
    }

    public boolean isReadable() {
        return totalRoutes != null || resultsCount != null
                || notBlank(totalIncome) || notBlank(totalMiles);
    }

    /** True si al menos un total del pie (o Results) cambió respecto a {@code other}. */
    public boolean differsFrom(RouteFooterTotals other) {
        if (other == null) {
            return true;
        }
        return !Objects.equals(totalRoutes, other.totalRoutes)
                || !Objects.equals(resultsCount, other.resultsCount)
                || !eq(totalIncome, other.totalIncome)
                || !eq(totalMiles, other.totalMiles)
                || !eq(dhMiles, other.dhMiles)
                || !eq(effectiveRpm, other.effectiveRpm)
                || !eq(loadedRpm, other.loadedRpm);
    }

    public String snapshot() {
        return "Results=" + value(resultsCount)
                + " | Total routes=" + value(totalRoutes)
                + " | Total income=" + value(totalIncome)
                + " | Total miles=" + value(totalMiles)
                + " | DH miles=" + value(dhMiles)
                + " | Effective RPM=" + value(effectiveRpm)
                + " | Loaded RPM=" + value(loadedRpm);
    }

    private static boolean eq(String a, String b) {
        return Objects.equals(norm(a), norm(b));
    }

    private static String norm(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String value(Object value) {
        return value == null ? "—" : String.valueOf(value);
    }
}
