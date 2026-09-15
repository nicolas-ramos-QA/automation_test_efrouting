package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorValidationResult;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detecta en el texto del modal Calculate profit si hay Current profit usable:
 * línea de ciudades (City, ST - City, ST ...) y "Profit: N%".
 */
public final class CalculatorProfitParser {

    /**
     * Ej: Chicago, IL - Geneva, IL - Fort Wayne, IN
     * (al menos 2 ciudades con estado de 2 letras).
     */
    private static final Pattern CITIES_LINE = Pattern.compile(
            "([A-Za-z][A-Za-z .'-]+,\\s*[A-Z]{2}"
                    + "(?:\\s*[-–—]\\s*[A-Za-z][A-Za-z .'-]+,\\s*[A-Z]{2})+)");

    /** Ej: Profit: 18%  ó  Profit:18%, 1 days */
    private static final Pattern PROFIT_PCT = Pattern.compile(
            "Profit\\s*:\\s*(\\d+(?:\\.\\d+)?)\\s*%", Pattern.CASE_INSENSITIVE);

    private static final Pattern DAYS = Pattern.compile(
            "(\\d+)\\s*days?", Pattern.CASE_INSENSITIVE);

    private CalculatorProfitParser() {}

    /**
     * @return resultado parseado si el texto contiene Current profit + ciudades + %,
     *         o {@code null} si no sirve para validar la calculadora.
     */
    public static CalculatorValidationResult parseIfValid(String modalText) {
        if (modalText == null || modalText.isBlank()) {
            return null;
        }
        String normalized = modalText.replace('\u00A0', ' ').trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (!lower.contains("current profit")) {
            return null;
        }

        Matcher cities = CITIES_LINE.matcher(normalized);
        if (!cities.find()) {
            return null;
        }
        String citiesLine = cities.group(1).replaceAll("\\s+", " ").trim();

        Matcher pct = PROFIT_PCT.matcher(normalized);
        if (!pct.find()) {
            return null;
        }
        String profitPercent = pct.group(1) + "%";

        String daysText = null;
        Matcher days = DAYS.matcher(normalized);
        if (days.find()) {
            daysText = days.group(1) + " days";
        }

        return new CalculatorValidationResult(citiesLine, profitPercent, daysText);
    }
}
