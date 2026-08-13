package com.calculos_de_rutas.question;

import com.calculos_de_rutas.models.FinancialColumn;
import com.calculos_de_rutas.models.LaneUiValues;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import com.calculos_de_rutas.utils.MoneyParser;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lee la tabla de lanes del detalle de ruta.
 *
 * <p>Las columnas se ubican por la posición que ocupan en el thead (reconocidas por
 * {@code data-column-id} o por su rótulo), porque las celdas de la fila de valores generales no
 * tienen {@code data-cy} y porque el frontend agrega y quita columnas según el ancho disponible.</p>
 */
public final class LaneTableReader {

    /** Texto del botón que efRouting muestra cuando una celda de costo aún no tiene importe. */
    private static final String ADD_BUTTON_TEXT = "add";

    private final WebDriver driver;
    private final Map<FinancialColumn, Integer> positions = new EnumMap<>(FinancialColumn.class);
    private final Map<FinancialColumn, String> columnIds = new EnumMap<>(FinancialColumn.class);

    private LaneTableReader(WebDriver driver) {
        this.driver = driver;
        resolveLayout();
    }

    public static LaneTableReader from(Actor actor) {
        return new LaneTableReader(BrowseTheWeb.as(actor).getDriver());
    }

    private void resolveLayout() {
        List<WebElement> headers = driver.findElements(By.xpath(SelectorConstant.TABLE_HEADERS));
        for (int i = 0; i < headers.size(); i++) {
            String columnId = headers.get(i).getAttribute("data-column-id");
            String text = headers.get(i).getText();
            for (FinancialColumn column : FinancialColumn.values()) {
                if (!positions.containsKey(column) && column.matchesHeader(columnId, text)) {
                    positions.put(column, i + 1);
                    columnIds.put(column, columnId == null ? column.columnId() : columnId);
                }
            }
        }
    }

    public boolean isVisible(FinancialColumn column) {
        return positions.containsKey(column);
    }

    public List<String> visibleLabels() {
        List<String> labels = new ArrayList<>();
        for (FinancialColumn column : FinancialColumn.values()) {
            if (isVisible(column)) {
                labels.add(column.label());
            }
        }
        return labels;
    }

    public List<String> missingLabels() {
        List<String> labels = new ArrayList<>();
        for (FinancialColumn column : FinancialColumn.values()) {
            if (!isVisible(column)) {
                labels.add(column.label());
            }
        }
        return labels;
    }

    public int laneRowCount() {
        return driver.findElements(By.xpath(SelectorConstant.PRINCIPAL_TABLE_ROWS)).size();
    }

    /** Lee todas las columnas financieras de una lane (índice 1-based). */
    public LaneUiValues readLane(int rowIndex) {
        List<WebElement> rows = driver.findElements(
                By.xpath(String.format(SelectorConstant.PRINCIPAL_TABLE_ROW_BY_INDEX, rowIndex)));
        return rows.isEmpty() ? new LaneUiValues() : readRow(rows.get(0), true);
    }

    /**
     * Lee la fila de valores generales de la ruta (rotulada "Total"). No es un resumen de la
     * tabla: el Backend la entrega en el bloque finance de la ruta.
     */
    public LaneUiValues readRouteTotals() {
        WebElement row = totalsRow();
        return row == null ? new LaneUiValues() : readRow(row, false);
    }

    public boolean routeTotalsRowExists() {
        return totalsRow() != null;
    }

    private WebElement totalsRow() {
        List<WebElement> rows = driver.findElements(By.xpath(SelectorConstant.ROUTE_TOTALS_ROW));
        if (rows.isEmpty()) {
            rows = driver.findElements(By.xpath(SelectorConstant.ROUTE_TOTALS_ROW_BY_LABEL));
        }
        for (WebElement row : rows) {
            if (row.getText().toLowerCase(Locale.ROOT).contains("total")) {
                return row;
            }
        }
        return rows.isEmpty() ? null : rows.get(0);
    }

    private LaneUiValues readRow(WebElement row, boolean isLane) {
        LaneUiValues ui = new LaneUiValues();
        List<WebElement> cells = row.findElements(By.xpath("./td"));

        for (FinancialColumn column : FinancialColumn.values()) {
            if (!isVisible(column)) {
                continue;
            }
            String text = cellText(row, cells, column, isLane);
            Double value;
            double[] range;
            if (isAddButton(text)) {
                ui.markAsPending(column);
                value = Double.valueOf(0);
                range = new double[]{0, 0};
            } else {
                value = MoneyParser.parseSingle(text);
                range = MoneyParser.parseRange(text);
            }
            ui.put(column, text, value, range, true);
        }
        return ui;
    }

    private String cellText(WebElement row, List<WebElement> cells, FinancialColumn column, boolean isLane) {
        if (isLane) {
            List<WebElement> byDataCy = row.findElements(
                    By.xpath(String.format(SelectorConstant.LANE_CELL_BY_COLUMN, columnIds.get(column))));
            if (!byDataCy.isEmpty()) {
                return byDataCy.get(0).getText().trim();
            }
        }
        int position = positions.get(column);
        return position <= cells.size() ? cells.get(position - 1).getText().trim() : "";
    }

    /**
     * "Add +" significa que la columna existe y vale cero, no que falte el dato: el usuario
     * todavía no cargó un importe manual (caso habitual de Custom cost).
     */
    private boolean isAddButton(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.replace("+", "").replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith(ADD_BUTTON_TEXT) && !normalized.matches(".*\\d.*");
    }
}
