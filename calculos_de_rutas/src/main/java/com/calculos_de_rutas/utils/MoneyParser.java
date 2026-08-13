package com.calculos_de_rutas.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parseo de montos mostrados en UI ($1,234.56, rangos, redondeos).
 */
public final class MoneyParser {

    /** Monto con signo: -$1,234.56, ($1,234.56), $1,234.56 */
    private static final Pattern MONEY = Pattern.compile(
            "(?<sign>[-−(])?\\s*\\$?\\s*(?<amount>[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]+)?|[0-9]+(?:\\.[0-9]+)?)");
    /**
     * Rango "A - B". El separador exige un espacio a la izquierda para no confundirlo con el signo
     * del primer importe, pero no a la derecha: efRouting pinta el Profit como "$4 -$374" y los
     * rangos negativos como "-$3,257 --$2,387".
     */
    private static final Pattern RANGE = Pattern.compile(
            "([-−(]?\\s*\\$?\\s*[0-9,.]+\\)?)\\s+[-–—]\\s*([-−(]?\\s*\\$?\\s*[0-9,.]+\\)?)");

    private MoneyParser() {}

    public static Double parseSingle(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String cleaned = text.replace("\u00A0", " ").trim();
        // La tabla usa "-" o "--" para los tramos sin valor aplicable (por ejemplo el RPM de un deadhead).
        if (cleaned.matches("[-−–—\\s]+") || cleaned.equalsIgnoreCase("n/a")) {
            return 0.0;
        }
        Matcher matcher = MONEY.matcher(cleaned);
        if (matcher.find()) {
            double amount = Double.parseDouble(matcher.group("amount").replace(",", ""));
            return matcher.group("sign") == null ? amount : -amount;
        }
        return null;
    }

    public static double[] parseRange(String text) {
        if (text == null || text.isBlank()) {
            return new double[]{0, 0};
        }
        Matcher range = RANGE.matcher(text.replace("\u00A0", " "));
        if (range.find()) {
            Double min = parseSingle(range.group(1));
            Double max = parseSingle(range.group(2));
            return new double[]{
                    min == null ? 0 : min,
                    max == null ? 0 : max
            };
        }
        Double single = parseSingle(text);
        double value = single == null ? 0 : single;
        return new double[]{value, value};
    }

    public static List<Double> parseAll(String text) {
        List<Double> values = new ArrayList<>();
        if (text == null) {
            return values;
        }
        Matcher matcher = MONEY.matcher(text.replace("\u00A0", " "));
        while (matcher.find()) {
            double amount = Double.parseDouble(matcher.group("amount").replace(",", ""));
            values.add(matcher.group("sign") == null ? amount : -amount);
        }
        return values;
    }

    /**
     * Redondeo visual de la UI: el Backend trae precisiones hasta milésimas (p. ej. 312.628)
     * y la pantalla muestra el entero más cercano ($313).
     */
    public static long roundVisual(double value) {
        return Math.round(value);
    }

    public static boolean nearlyEqual(double expected, double actual, double tolerance) {
        return Math.abs(expected - actual) <= tolerance;
    }

    /**
     * Compara Backend vs UI: el valor redondeado del Backend debe coincidir con lo mostrado.
     */
    public static boolean matchesDisplayed(double backendValue, double uiValue) {
        return matchesDisplayed(backendValue, uiValue, 0.51);
    }

    public static boolean matchesDisplayed(double backendValue, double uiValue, double tolerance) {
        // Tolerancia ≥ 2 para absorber ±1/±2 por redondeo de frontera en totales (finance vs UI).
        double effective = Math.max(tolerance, 2.01);
        return nearlyEqual(roundVisual(backendValue), uiValue, effective);
    }

    /**
     * Compara importes de costo ignorando el signo: efRouting muestra los costos por lane en
     * negativo (salida de dinero) aunque el Backend los expone en positivo.
     */
    public static boolean matchesDisplayedMagnitude(double backendValue, double uiValue) {
        return matchesDisplayedMagnitude(backendValue, uiValue, 0.51);
    }

    public static boolean matchesDisplayedMagnitude(double backendValue, double uiValue, double tolerance) {
        return matchesDisplayed(Math.abs(backendValue), Math.abs(uiValue), tolerance);
    }

    /** Backend con milésimas, para el reporte. */
    public static String formatPrecise(double value) {
        return String.format(Locale.US, "$%,.3f", value);
    }

    public static String formatPreciseRange(double min, double max) {
        return formatPrecise(min) + " – " + formatPrecise(max);
    }

    /** Valor ya redondeado como lo pinta la UI. */
    public static String formatRounded(double value) {
        return String.format(Locale.US, "$%,d", roundVisual(value));
    }

    public static String formatRoundedRange(double min, double max) {
        return formatRounded(min) + " – " + formatRounded(max);
    }

    /** Lo que se lee del Frontend (puede ser negativo). */
    public static String formatUi(double value) {
        if (value < 0) {
            return String.format(Locale.US, "-$%,.0f", Math.abs(value));
        }
        return String.format(Locale.US, "$%,.0f", value);
    }

    public static String formatUiRange(double min, double max) {
        if (Math.abs(min - max) < 0.5) {
            return formatUi(min);
        }
        return formatUi(min) + " – " + formatUi(max);
    }

    public static String format(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    public static String formatRange(double min, double max) {
        return String.format(Locale.US, "%.3f - %.3f", min, max);
    }

    public static String formatRangeAsMoney(double min, double max) {
        return formatPreciseRange(min, max);
    }
}
