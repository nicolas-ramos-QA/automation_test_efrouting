package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.FinancialCheck;
import com.calculos_de_rutas.models.FinancialColumn;
import com.calculos_de_rutas.models.LaneFinancials;
import com.calculos_de_rutas.models.LaneUiValues;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import com.calculos_de_rutas.models.RouteTotals;

/**
 * Compara Backend (milésimas) ↔ Frontend (entero), en este orden:
 * <ol>
 *   <li>Por cada lane: Income → Fuel → Toll → Custom → Op → Total cost → Profit</li>
 *   <li>Fila Total inferior: mismas columnas</li>
 * </ol>
 *
 * <pre>
 * Total cost = Fuel + Toll + Custom [+ Op cost si está visible]
 * Profit     = Income − Total cost
 * Custom "Add +" = $0
 * </pre>
 */
public final class FinancialAssertions {

    public static final String PHASE_LANES = "1. Validación por lane";
    public static final String PHASE_TOTALS = "2. Validación del Total (fila inferior)";
    public static final String SCOPE_TOTALS = "Total de la ruta";

    private FinancialAssertions() {}

    /**
     * Valida una lane: campos del endpoint vs pantalla + fórmulas.
     *
     * <p>En cada lane el Backend entrega {@code finance.totalCost} = Fuel+Toll+Custom (sin Op).
     * Si Op está visible, la UI suma Op (tarifa × millas) al Total cost y lo resta del Profit.</p>
     */
    public static void validateLane(int rowIndex, LaneFinancials lane, LaneUiValues ui,
                                    Double opCostRate, boolean opVisible) {
        validateLane(rowIndex, lane, ui, opCostRate, opVisible,
                opVisible ? "" : "  ·  Op cost OCULTO");
    }

    public static void validateLane(int rowIndex, LaneFinancials lane, LaneUiValues ui,
                                    Double opCostRate, boolean opVisible, String scopeSuffix) {
        String scope = scopeOf(rowIndex, lane) + (scopeSuffix == null ? "" : scopeSuffix);
        double fuel = safe(lane.getFuelCost());
        double toll = safe(lane.getTollCost());
        double custom = safe(lane.getCustomCost());
        double opFull = lane.opCostFrom(opCostRate);
        // En cada lane, si Op está oculto la UI recalcula: no suma Op al Total ni lo resta del Profit.
        double opCost = opVisible ? opFull : 0.0;

        double expectedTotal = fuel + toll + custom + opCost;
        double incomeMin = safe(lane.getIncomeMin());
        double incomeMax = safe(lane.getIncomeMax());
        double expectedProfitMin = incomeMin - expectedTotal;
        double expectedProfitMax = incomeMax - expectedTotal;

        // —— Campos Income → Profit ——
        compareRange(PHASE_LANES, scope, "Income",
                lane.getIncomeMin(), lane.getIncomeMax(), ui, FinancialColumn.INCOME);

        compareAmount(PHASE_LANES, scope, "Fuel cost",
                fuel, ui, FinancialColumn.FUEL, true, null);

        compareAmount(PHASE_LANES, scope, "Toll cost",
                toll, ui, FinancialColumn.TOLL, true, null);

        compareAmount(PHASE_LANES, scope, "Custom cost",
                custom, ui, FinancialColumn.CUSTOM, true,
                ui.isPendingValue(FinancialColumn.CUSTOM)
                        ? "Botón \"Add +\" = $0 (sin valor cargado)" : null);

        if (opVisible) {
            compareAmount(PHASE_LANES, scope, "Op cost",
                    opCost, ui, FinancialColumn.OP_COST, true,
                    "tarifa × millas = " + MoneyParser.formatPrecise(safe(opCostRate))
                            + "/mi × " + MoneyParser.format(safe(lane.getMileage())) + " mi");
        } else {
            info(PHASE_LANES, scope, "Op cost",
                    MoneyParser.formatPrecise(opFull),
                    "oculto — no se resta",
                    "Op oculto en la lane → Total cost = Fuel+Toll+Custom ; Profit = Income − Total cost");
        }

        compareAmount(PHASE_LANES, scope, "Total cost",
                expectedTotal, ui, FinancialColumn.TOTAL_COST, true,
                formulaTotal(fuel, toll, custom, opCost, opVisible));

        compareRange(PHASE_LANES, scope, "Profit",
                expectedProfitMin, expectedProfitMax, ui, FinancialColumn.PROFIT);

        // —— Fórmulas explícitas ——
        recordFormulaAmount(PHASE_LANES, scope, "Cálculo Total cost",
                formulaTotal(fuel, toll, custom, opCost, opVisible),
                expectedTotal, ui.get(FinancialColumn.TOTAL_COST));

        recordFormulaRange(PHASE_LANES, scope, "Cálculo Profit",
                formulaProfit(incomeMin, incomeMax, expectedTotal, opVisible),
                expectedProfitMin, expectedProfitMax,
                ui.getRange(FinancialColumn.PROFIT));
    }

    /**
     * Valida la fila Total (Income → Profit) contra el bloque {@code finance} de la ruta.
     *
     * <p>En Total, la celda Op cost muestra la <b>Σ Op cost</b> de todas las lanes (tarifa ×
     * millaje total), no la tarifa/mi. El Total cost del Backend ({@code finance.totalCost}) ya
     * incluye esa suma cuando Op está visible.</p>
     *
     * <p>{@code finance.profit} del endpoint <b>nunca resta Op cost</b> (ni con Op visible), así
     * que no sirve de referencia directa para el Profit: se recalcula igual que en cada lane,
     * como Income − Total cost (que sí incluye Op cuando está visible).</p>
     *
     * <p>Cuando Op cost se OCULTA, Total cost y Profit se recalculan como la <b>suma de las
     * lanes sin Op</b> (Fuel+Toll+Custom por lane, e Income − esos costos por lane, tanto para el
     * mínimo como para el máximo).</p>
     */
    public static void validateTotals(RouteFinancialPayload payload, LaneUiValues ui,
                                      boolean opVisible) {
        String scope = opVisible ? SCOPE_TOTALS : SCOPE_TOTALS + "  ·  Op cost OCULTO";
        RouteTotals totals = payload.getTotals();
        if (totals == null) {
            FinancialReport.record(FinancialCheck.builder()
                    .phase(PHASE_TOTALS).scope(scope).field("finance de la ruta")
                    .status(FinancialCheck.Status.FAIL)
                    .detail("El Backend no devolvió el bloque finance a nivel de ruta")
                    .build());
            return;
        }

        double fuel = safe(totals.getFuelCost());
        double toll = safe(totals.getTollCost());
        double custom = safe(totals.getCustomCost());
        double opRate = safe(totals.getOperativeCost() != null
                ? totals.getOperativeCost() : payload.getOperativeCostRate());
        double opSum = payload.sumOpCost();

        double beTotalCost = safe(totals.getTotalCost() != null
                ? totals.getTotalCost() : fuel + toll + custom + opSum);

        // Evidencia real (QA, 13/ago/2026): finance.profit del endpoint NUNCA resta Op (ni con Op
        // visible), así que no sirve de referencia para el Profit visible. Se recalcula igual que
        // en cada lane: Income − Total cost (que sí incluye Op cuando está visible).
        double expectedProfitMin = safe(totals.getIncomeMin()) - beTotalCost;
        double expectedProfitMax = safe(totals.getIncomeMax()) - beTotalCost;

        compareRange(PHASE_TOTALS, scope, "Income",
                totals.getIncomeMin(), totals.getIncomeMax(), ui, FinancialColumn.INCOME);

        compareAmount(PHASE_TOTALS, scope, "Fuel cost",
                fuel, ui, FinancialColumn.FUEL, true, null);

        compareAmount(PHASE_TOTALS, scope, "Toll cost",
                toll, ui, FinancialColumn.TOLL, true, null);

        compareAmount(PHASE_TOTALS, scope, "Custom cost",
                custom, ui, FinancialColumn.CUSTOM, true,
                ui.isPendingValue(FinancialColumn.CUSTOM) ? "Add + = $0" : null);

        if (opVisible) {
            // Evidencia real (QA, 3 corridas): la celda Op cost de Total muestra la SUMA de
            // Op cost de todas las lanes (tarifa × millaje total), no la tarifa/mi.
            compareAmount(PHASE_TOTALS, scope, "Op cost",
                    opSum, ui, FinancialColumn.OP_COST, true,
                    "En Total se muestra la Σ Op cost de las lanes (tarifa " + MoneyParser.formatPrecise(opRate)
                            + "/mi × millaje total) = " + MoneyParser.formatPrecise(opSum));

            compareAmount(PHASE_TOTALS, scope, "Total cost",
                    beTotalCost, ui, FinancialColumn.TOTAL_COST, true,
                    formulaTotal(fuel, toll, custom, opSum, true)
                            + "  ← Op incluido = tarifa × millaje (" + MoneyParser.formatPrecise(opSum) + ")");

            compareRange(PHASE_TOTALS, scope, "Profit",
                    expectedProfitMin, expectedProfitMax, ui, FinancialColumn.PROFIT);

            recordFormulaAmount(PHASE_TOTALS, scope, "Cálculo Total cost",
                    formulaTotal(fuel, toll, custom, opSum, true),
                    beTotalCost, ui.get(FinancialColumn.TOTAL_COST));

            recordFormulaRange(PHASE_TOTALS, scope, "Cálculo Profit",
                    formulaProfit(safe(totals.getIncomeMin()), safe(totals.getIncomeMax()), beTotalCost, true),
                    expectedProfitMin, expectedProfitMax,
                    ui.getRange(FinancialColumn.PROFIT));
        } else {
            // Al ocultar Op, el finance de la ruta ya no es la referencia (incluye Op sumado).
            // La UI recalcula Total cost y Profit como la suma de cada lane SIN Op.
            double recalcTotalCost = payload.sumTotalCostWithoutOp();
            double recalcProfitMin = payload.sumProfitMinWithoutOp();
            double recalcProfitMax = payload.sumProfitMaxWithoutOp();

            info(PHASE_TOTALS, scope, "Op cost",
                    MoneyParser.formatPrecise(opRate),
                    "columna oculta (ojo tachado)",
                    "En Total el ojo oculta Op; Total cost y Profit se recalculan como Σ lanes sin Op");

            compareAmount(PHASE_TOTALS, scope, "Total cost",
                    recalcTotalCost, ui, FinancialColumn.TOTAL_COST, true,
                    "Σ lanes (Fuel+Toll+Custom, sin Op) = " + MoneyParser.formatPrecise(recalcTotalCost)
                            + "  (finance.totalCost del Backend incluye Op, no aplica aquí)");

            // Si falla, se documenta la causa probable directamente en el reporte (ver nota dinámica).
            String notaProfit = notaProfitOcultoInflado(ui.getRange(FinancialColumn.PROFIT),
                    recalcProfitMin, recalcProfitMax, opSum);

            compareRange(PHASE_TOTALS, scope, "Profit",
                    recalcProfitMin, recalcProfitMax, ui, FinancialColumn.PROFIT, notaProfit);

            recordFormulaAmount(PHASE_TOTALS, scope, "Cálculo Total cost",
                    "Σ lanes (Fuel+Toll+Custom, sin Op)",
                    recalcTotalCost, ui.get(FinancialColumn.TOTAL_COST));

            recordFormulaRange(PHASE_TOTALS, scope, "Cálculo Profit",
                    "Σ Profit de cada lane sin Op (mín/máx) = "
                            + MoneyParser.formatPreciseRange(recalcProfitMin, recalcProfitMax),
                    recalcProfitMin, recalcProfitMax,
                    ui.getRange(FinancialColumn.PROFIT), notaProfit);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void compareAmount(String phase, String scope, String field,
                                      double backend, LaneUiValues ui, FinancialColumn column,
                                      boolean ignoreSign, String note) {
        if (!ui.isAvailable(column)) {
            skip(phase, scope, field, backend);
            return;
        }
        Double frontend = ui.get(column);
        boolean ok = frontend != null
                && (ignoreSign
                ? MoneyParser.matchesDisplayedMagnitude(backend, frontend)
                : MoneyParser.matchesDisplayed(backend, frontend));

        FinancialReport.record(FinancialCheck.builder()
                .phase(phase).scope(scope).field(field)
                .backendValue(MoneyParser.formatPrecise(backend))
                .roundedValue(MoneyParser.formatRounded(backend))
                .frontendValue(frontend == null ? "no leído"
                        : MoneyParser.formatUi(frontend) + displayHint(ui.getText(column)))
                .difference(frontend == null ? "—"
                        : diff(Math.abs(frontend) - MoneyParser.roundVisual(Math.abs(backend))))
                .status(ok ? FinancialCheck.Status.PASS : FinancialCheck.Status.FAIL)
                .detail(ok ? nullToEmpty(note)
                        : (frontend == null ? "No se pudo leer el valor en pantalla"
                        : "Backend redondeado ≠ Frontend"))
                .formula(nullToEmpty(note))
                .build());
    }

    private static void compareRange(String phase, String scope, String field,
                                     Double backendMin, Double backendMax,
                                     LaneUiValues ui, FinancialColumn column) {
        compareRange(phase, scope, field, backendMin, backendMax, ui, column, null);
    }

    /**
     * Igual que {@link #compareRange}, pero si el check FALLA agrega {@code failNote} al detalle
     * (p.ej. una explicación de la causa probable, para que el reporte sea autoexplicativo).
     */
    private static void compareRange(String phase, String scope, String field,
                                     Double backendMin, Double backendMax,
                                     LaneUiValues ui, FinancialColumn column, String failNote) {
        if (!ui.isAvailable(column)) {
            skip(phase, scope, field, backendMax);
            return;
        }
        double min = safe(backendMin);
        double max = backendMax == null ? min : backendMax;
        double[] uiRange = ui.getRange(column);
        boolean ok = MoneyParser.matchesDisplayed(min, uiRange[0])
                && MoneyParser.matchesDisplayed(max, uiRange[1]);

        FinancialReport.record(FinancialCheck.builder()
                .phase(phase).scope(scope).field(field)
                .backendValue(MoneyParser.formatPreciseRange(min, max))
                .roundedValue(MoneyParser.formatRoundedRange(min, max))
                .frontendValue(MoneyParser.formatUiRange(uiRange[0], uiRange[1])
                        + displayHint(ui.getText(column)))
                .difference(String.format("mín %s / máx %s",
                        diff(uiRange[0] - MoneyParser.roundVisual(min)),
                        diff(uiRange[1] - MoneyParser.roundVisual(max))))
                .status(ok ? FinancialCheck.Status.PASS : FinancialCheck.Status.FAIL)
                .detail(ok ? "" : "El rango en pantalla no coincide con el Backend redondeado"
                        + (failNote == null || failNote.isBlank() ? "" : "  ·  " + failNote))
                .build());
    }

    private static void recordFormulaAmount(String phase, String scope, String field,
                                            String formula, double expected, Double frontend) {
        boolean ok = frontend != null && MoneyParser.matchesDisplayedMagnitude(expected, frontend);
        FinancialReport.record(FinancialCheck.builder()
                .phase(phase).scope(scope).field(field)
                .formula(formula)
                .backendValue(MoneyParser.formatPrecise(expected))
                .roundedValue(MoneyParser.formatRounded(expected))
                .frontendValue(frontend == null ? "no leído" : MoneyParser.formatUi(frontend))
                .difference(frontend == null ? "—"
                        : diff(Math.abs(frontend) - MoneyParser.roundVisual(expected)))
                .status(ok ? FinancialCheck.Status.PASS : FinancialCheck.Status.FAIL)
                .detail(ok ? "Fórmula OK" : "Fuel + Toll + Custom [+ Op] ≠ Total cost en pantalla")
                .build());
    }

    private static void recordFormulaRange(String phase, String scope, String field, String formula,
                                           double expectedMin, double expectedMax, double[] uiRange) {
        recordFormulaRange(phase, scope, field, formula, expectedMin, expectedMax, uiRange, null);
    }

    /** Igual que {@link #recordFormulaRange}, agregando {@code failNote} al detalle si FALLA. */
    private static void recordFormulaRange(String phase, String scope, String field, String formula,
                                           double expectedMin, double expectedMax, double[] uiRange,
                                           String failNote) {
        boolean ok = MoneyParser.matchesDisplayed(expectedMin, uiRange[0])
                && MoneyParser.matchesDisplayed(expectedMax, uiRange[1]);
        FinancialReport.record(FinancialCheck.builder()
                .phase(phase).scope(scope).field(field)
                .formula(formula)
                .backendValue(MoneyParser.formatPreciseRange(expectedMin, expectedMax))
                .roundedValue(MoneyParser.formatRoundedRange(expectedMin, expectedMax))
                .frontendValue(MoneyParser.formatUiRange(uiRange[0], uiRange[1]))
                .difference(String.format("mín %s / máx %s",
                        diff(uiRange[0] - MoneyParser.roundVisual(expectedMin)),
                        diff(uiRange[1] - MoneyParser.roundVisual(expectedMax))))
                .status(ok ? FinancialCheck.Status.PASS : FinancialCheck.Status.FAIL)
                .detail(ok ? "Fórmula OK" : "Income − Total cost ≠ Profit en pantalla"
                        + (failNote == null || failNote.isBlank() ? "" : "  ·  " + failNote))
                .build());
    }

    /**
     * Detecta el patrón de bug encontrado en QA (13/ago/2026): al ocultar Op cost, el Profit en
     * pantalla sube exactamente el Σ Op cost de la ruta, como si la app lo sumara por error
     * (el Profit del Backend nunca lo restaba, ni con Op visible). Si la diferencia real coincide
     * con ese patrón, lo documenta explícitamente en el reporte; si no, deja una nota genérica.
     */
    private static String notaProfitOcultoInflado(double[] uiRange, double recalcMin, double recalcMax,
                                                   double opSum) {
        if (opSum <= 0.0) {
            return null;
        }
        double diffMin = Math.abs(uiRange[0] - recalcMin);
        double diffMax = Math.abs(uiRange[1] - recalcMax);
        double tolerancia = 2.0; // margen por redondeo de milésimas → entero en ambos extremos
        boolean coincideConOpSum = Math.abs(diffMin - opSum) < tolerancia
                && Math.abs(diffMax - opSum) < tolerancia;

        if (coincideConOpSum) {
            return "Posible bug de la app: el Profit en pantalla sube ~" + MoneyParser.formatPrecise(opSum)
                    + " al ocultar Op cost — exactamente el Σ Op cost de la ruta — como si lo sumara "
                    + "por error en vez de dejarlo sin cambios (el Profit del Backend nunca restaba Op, "
                    + "ni con Op visible). Ver decisión del equipo: se deja como FALLÓ para reportarlo.";
        }
        return "La diferencia no coincide con el patrón conocido (Σ Op cost = "
                + MoneyParser.formatPrecise(opSum) + "); revisar manualmente.";
    }

    private static void info(String phase, String scope, String field,
                             String backend, String frontend, String formula) {
        FinancialReport.record(FinancialCheck.builder()
                .phase(phase).scope(scope).field(field)
                .backendValue(backend)
                .roundedValue("no aplica")
                .frontendValue(frontend)
                .status(FinancialCheck.Status.PASS)
                .formula(formula)
                .build());
    }

    private static void skip(String phase, String scope, String field, Double backend) {
        FinancialReport.record(FinancialCheck.builder()
                .phase(phase).scope(scope).field(field)
                .backendValue(backend == null ? "—" : MoneyParser.formatPrecise(backend))
                .frontendValue("columna no visible")
                .status(FinancialCheck.Status.SKIP)
                .detail("Cierre el mapa (hamburguesa) para renderizar todas las columnas")
                .build());
    }

    public static String scopeOf(int rowIndex, LaneFinancials lane) {
        StringBuilder sb = new StringBuilder("Lane ").append(rowIndex);
        if (lane.getOrigin() != null && lane.getDestination() != null) {
            sb.append(" — ").append(lane.getOrigin()).append(" → ").append(lane.getDestination());
        }
        if (lane.getType() != null) {
            sb.append(" [").append(lane.getType()).append(']');
        }
        return sb.toString();
    }

    private static String formulaTotal(double fuel, double toll, double custom,
                                       double op, boolean opVisible) {
        if (opVisible) {
            return String.format("Fuel %s + Toll %s + Custom %s + Op %s = %s",
                    MoneyParser.formatPrecise(fuel), MoneyParser.formatPrecise(toll),
                    MoneyParser.formatPrecise(custom), MoneyParser.formatPrecise(op),
                    MoneyParser.formatPrecise(fuel + toll + custom + op));
        }
        return String.format("Fuel %s + Toll %s + Custom %s = %s  (Op cost oculto)",
                MoneyParser.formatPrecise(fuel), MoneyParser.formatPrecise(toll),
                MoneyParser.formatPrecise(custom),
                MoneyParser.formatPrecise(fuel + toll + custom));
    }

    private static String formulaProfit(double incomeMin, double incomeMax,
                                        double totalCost, boolean opVisible) {
        String base = String.format("Income (%s – %s) − Total cost %s = Profit (%s – %s)",
                MoneyParser.formatPrecise(incomeMin), MoneyParser.formatPrecise(incomeMax),
                MoneyParser.formatPrecise(totalCost),
                MoneyParser.formatPrecise(incomeMin - totalCost),
                MoneyParser.formatPrecise(incomeMax - totalCost));
        return opVisible ? base : base + "  · Op no restado";
    }

    private static String displayHint(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return "  («" + raw.replace('\n', ' ').trim() + "»)";
    }

    private static String diff(double value) {
        if (Math.abs(value) < 0.005) {
            return "0";
        }
        return (value > 0 ? "+" : "") + String.format(java.util.Locale.US, "%.0f", value);
    }

    private static double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static void checkLaneAgainstUi(int rowIndex, LaneFinancials lane, LaneUiValues ui,
                                          Double opCostRate) {
        validateLane(rowIndex, lane, ui, opCostRate, true);
    }

    public static void checkRouteTotals(RouteFinancialPayload payload, LaneUiValues ui) {
        validateTotals(payload, ui, true);
    }
}
