package com.calculos_de_rutas.models;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lo que se ve en pantalla para una fila de la tabla de lanes: el texto tal cual, el importe
 * interpretado, el rango cuando la celda muestra "min - max" y qué columnas existen realmente.
 */
public class LaneUiValues {

    private final Map<FinancialColumn, String> texts = new EnumMap<>(FinancialColumn.class);
    private final Map<FinancialColumn, Double> values = new EnumMap<>(FinancialColumn.class);
    private final Map<FinancialColumn, double[]> ranges = new EnumMap<>(FinancialColumn.class);
    private final Set<FinancialColumn> available = EnumSet.noneOf(FinancialColumn.class);
    private final Set<FinancialColumn> pendingValue = EnumSet.noneOf(FinancialColumn.class);

    public LaneUiValues put(FinancialColumn column, String rawText, Double value,
                            double[] range, boolean columnAvailable) {
        if (columnAvailable) {
            available.add(column);
        }
        if (rawText != null) {
            texts.put(column, rawText);
        }
        if (value != null) {
            values.put(column, value);
        }
        if (range != null) {
            ranges.put(column, range);
        }
        return this;
    }

    /** Marca una celda que muestra el botón "Add" porque todavía no tiene importe cargado. */
    public LaneUiValues markAsPending(FinancialColumn column) {
        pendingValue.add(column);
        return this;
    }

    /**
     * Celda sin importe cargado: efRouting dibuja un botón "Add +" en lugar de $0, típicamente en
     * Custom cost. Equivale a cero, no a un valor faltante.
     */
    public boolean isPendingValue(FinancialColumn column) {
        return pendingValue.contains(column);
    }

    public String getText(FinancialColumn column) {
        return texts.get(column);
    }

    public Double get(FinancialColumn column) {
        return values.get(column);
    }

    public double getOrZero(FinancialColumn column) {
        Double value = values.get(column);
        return value == null ? 0.0 : value;
    }

    public double[] getRange(FinancialColumn column) {
        double[] range = ranges.get(column);
        if (range != null) {
            return range;
        }
        double value = getOrZero(column);
        return new double[]{value, value};
    }

    public double[] getIncomeRange() {
        return getRange(FinancialColumn.INCOME);
    }

    public boolean isAvailable(FinancialColumn column) {
        return available.contains(column);
    }

    public Set<FinancialColumn> availableColumns() {
        return EnumSet.copyOf(available.isEmpty() ? EnumSet.noneOf(FinancialColumn.class) : available);
    }
}
