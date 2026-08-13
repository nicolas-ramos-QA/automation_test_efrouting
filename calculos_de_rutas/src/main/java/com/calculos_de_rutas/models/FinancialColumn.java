package com.calculos_de_rutas.models;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Columnas financieras de la tabla de lanes de efRouting.
 *
 * <p>Se reconocen por el {@code data-column-id} del encabezado o, si el frontend cambia ese
 * identificador, por el rótulo visible. Fuel, Toll, Custom, Op cost y Profit solo se renderizan
 * cuando el panel de mapa está cerrado.</p>
 */
public enum FinancialColumn {

    INCOME("Income", "income"),
    RPM("RPM", "rpm"),
    FUEL("Fuel cost", "fuelCost", "fuel"),
    TOLL("Toll cost", "tollCost", "toll"),
    CUSTOM("Custom cost", "customCost", "custom"),
    OP_COST("Op cost", "operativeCost", "opCost", "operative", "operationalCost"),
    TOTAL_COST("Total cost", "totalCost"),
    PROFIT("Profit", "profit");

    private final String label;
    private final List<String> columnIds;

    FinancialColumn(String label, String... columnIds) {
        this.label = label;
        this.columnIds = Arrays.asList(columnIds);
    }

    /** Identificador principal, el que usa el frontend en data-column-id y data-cy. */
    public String columnId() {
        return columnIds.get(0);
    }

    public List<String> columnIds() {
        return columnIds;
    }

    public String label() {
        return label;
    }

    /** Determina si un encabezado corresponde a esta columna. */
    public boolean matchesHeader(String columnId, String headerText) {
        if (columnId != null) {
            for (String candidate : columnIds) {
                if (candidate.equalsIgnoreCase(columnId.trim())) {
                    return true;
                }
            }
        }
        return normalize(headerText).equals(normalize(label));
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }
}
