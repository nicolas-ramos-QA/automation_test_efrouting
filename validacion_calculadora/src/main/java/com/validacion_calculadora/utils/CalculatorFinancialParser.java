package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae Income, Distance (sin DH) y RPM (sin DH) del modal Calculate profit.
 * Soporta texto multilínea y texto compacto (todo en pocas líneas, p.ej. con "Income per day").
 */
public final class CalculatorFinancialParser {

    private static final Pattern MONEY = Pattern.compile("\\$?\\s*([\\d,]+(?:\\.\\d+)?)");
    private static final Pattern MILES = Pattern.compile(
            "([\\d,]+(?:\\.\\d+)?)\\s*mi\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RPM_VALUE = Pattern.compile(
            "\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*/\\s*mi", Pattern.CASE_INSENSITIVE);

    /** Income / Total Income, no "Income per day". */
    private static final Pattern INCOME_COMPACT = Pattern.compile(
            "(?i)(?:total\\s+)?income(?!\\s*per\\s*day)\\s*[:\\s]*\\$?\\s*([\\d,]+(?:\\.\\d+)?)");

    /** RPM no seguido de "with DH" en el entorno cercano. */
    private static final Pattern RPM_COMPACT = Pattern.compile(
            "(?i)\\brpm\\b(?!\\s*with)\\s*[:\\s]*\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*/\\s*mi");

    /** Distance, no Total Distance ni Distance DH. */
    private static final Pattern DISTANCE_COMPACT = Pattern.compile(
            "(?i)(?<!total\\s)\\bdistance\\b(?!\\s*dh)(?!\\s*deadhead)\\s*[:\\s]*([\\d,]+(?:\\.\\d+)?)\\s*mi\\b");

    /** "Income per day: $1,733/day". */
    private static final Pattern INCOME_PER_DAY = Pattern.compile(
            "(?i)income\\s*per\\s*day\\s*[:\\s]*\\$?\\s*([\\d,]+(?:\\.\\d+)?)");

    /** "Days on Route" con el valor en la misma línea ("1.5 days on route"). */
    private static final Pattern DAYS_SAME_LINE = Pattern.compile(
            "(?i)([\\d,]+(?:\\.\\d+)?)\\s*days?\\s*on\\s*route");

    private static final Pattern BARE_NUMBER = Pattern.compile("^\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*$");

    /** "Total costs: $3,007". */
    private static final Pattern TOTAL_COSTS_SAME_LINE = Pattern.compile(
            "(?i)total\\s*costs?\\s*[:\\s]*\\$\\s*([\\d,]+(?:\\.\\d+)?)");

    /** "Current profit in $1,759" (no confundir con Cycle/Outbound profit). */
    private static final Pattern CURRENT_PROFIT_SAME_LINE = Pattern.compile(
            "(?i)current\\s*profit(?:\\s*in)?\\s*[:\\s]*\\$\\s*([\\d,]+(?:\\.\\d+)?)");

    /** "Total Distance: 852 mi" y su forma invertida "852 mi Total Distance". */
    private static final Pattern TOTAL_DISTANCE_SAME_LINE = Pattern.compile(
            "(?i)total\\s*distance\\s*[:\\s]*([\\d,]+(?:\\.\\d+)?)\\s*mi\\b");
    private static final Pattern TOTAL_DISTANCE_INVERTED = Pattern.compile(
            "(?i)([\\d,]+(?:\\.\\d+)?)\\s*mi\\s*total\\s*distance");

    /** "Profit / mile: $0.89 / mi" y su forma invertida. */
    private static final Pattern PROFIT_PER_MILE_SAME_LINE = Pattern.compile(
            "(?i)profit\\s*/\\s*mile\\s*[:\\s]*\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*/\\s*mi\\b");
    private static final Pattern PROFIT_PER_MILE_INVERTED = Pattern.compile(
            "(?i)\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*/\\s*mi\\s*profit\\s*/\\s*mile");

    /** "Profit: 48%, 2.5 days" — el porcentaje que muestra la UI ya redondeado. */
    private static final Pattern PROFIT_PERCENT = Pattern.compile(
            "(?i)profit\\s*:\\s*([\\d,]+(?:\\.\\d+)?)\\s*%");

    /** Línea que es solo millas ("852 mi") o solo importe por milla ("$0.89 / mi"). */
    private static final Pattern ONLY_MILES = Pattern.compile(
            "(?i)^\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*mi$");
    private static final Pattern ONLY_PER_MILE = Pattern.compile(
            "(?i)^\\$?\\s*([\\d,]+(?:\\.\\d+)?)\\s*/\\s*mi$");

    private static final double MAX_PLAUSIBLE_RPM = 200.0;
    private static final double MAX_PLAUSIBLE_DAYS = 120.0;

    private CalculatorFinancialParser() {}

    public static CalculatorFinancialMetrics parse(String modalText) {
        if (modalText == null || modalText.isBlank()) {
            return null;
        }
        String normalized = modalText.replace('\u00A0', ' ');

        Double income = findIncomeCompact(normalized);
        if (income == null) {
            income = findIncome(normalized);
        }
        if (income == null) {
            income = findTotalIncome(normalized);
        }
        Double distance = findDistanceCompact(normalized);
        if (distance == null) {
            distance = findPlainDistance(normalized);
        }
        Double rpm = findRpmCompact(normalized);
        if (rpm == null) {
            rpm = findLabeledPlainRpm(normalized);
        }

        if (income == null || distance == null || rpm == null || distance <= 0) {
            return null;
        }

        double exact = income / distance;
        if (Math.abs(rpm - exact) > 0.05) {
            Double byFormula = closestRpmTo(normalized, exact);
            if (byFormula != null && Math.abs(byFormula - exact) < Math.abs(rpm - exact)) {
                rpm = byFormula;
            }
        }
        return new CalculatorFinancialMetrics(
                income, distance, rpm,
                findDaysOnRoute(normalized), findIncomePerDay(normalized),
                findTotalCosts(normalized), findCurrentProfit(normalized),
                findTotalDistance(normalized), findProfitPerMile(normalized),
                findProfitPercent(normalized));
    }

    static Double findProfitPercent(String text) {
        return firstGroup(PROFIT_PERCENT, text);
    }

    static Double findTotalDistance(String text) {
        Double v = firstGroup(TOTAL_DISTANCE_SAME_LINE, text);
        if (v == null) {
            v = firstGroup(TOTAL_DISTANCE_INVERTED, text);
        }
        if (v == null) {
            v = findNearLabel(text, "totaldistance", ONLY_MILES);
        }
        return v != null && v > 0 ? v : null;
    }

    static Double findProfitPerMile(String text) {
        Double v = firstGroup(PROFIT_PER_MILE_SAME_LINE, text);
        if (v == null) {
            v = firstGroup(PROFIT_PER_MILE_INVERTED, text);
        }
        if (v == null) {
            v = findNearLabel(text, "profitmile", ONLY_PER_MILE);
        }
        return v;
    }

    private static Double firstGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? parseNumber(m.group(1)) : null;
    }

    /**
     * Valor de una tarjeta cuya etiqueta ocupa su propia línea: el importe puede quedar
     * en la línea anterior o en la siguiente según cómo se serialice el modal.
     */
    private static Double findNearLabel(String text, String compactLabel, Pattern valueOnly) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String compact = lines[i].toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            if (!compact.equals(compactLabel)) {
                continue;
            }
            Double before = matchNear(lines, i, -1, valueOnly);
            if (before != null) {
                return before;
            }
            Double after = matchNear(lines, i, 1, valueOnly);
            if (after != null) {
                return after;
            }
        }
        return null;
    }

    private static Double matchNear(String[] lines, int from, int step, Pattern valueOnly) {
        for (int k = 1; k <= 6; k++) {
            int idx = from + step * k;
            if (idx < 0 || idx >= lines.length) {
                return null;
            }
            String candidate = lines[idx].trim();
            if (candidate.isEmpty()) {
                continue;
            }
            Matcher m = valueOnly.matcher(candidate);
            return m.matches() ? parseNumber(m.group(1)) : null;
        }
        return null;
    }

    static Double findTotalCosts(String text) {
        Matcher m = TOTAL_COSTS_SAME_LINE.matcher(text);
        if (m.find()) {
            return parseNumber(m.group(1));
        }
        return moneyAfterLabel(text, "totalcosts");
    }

    /** "Current profit" de la carga; ignora "Cycle profit" y "Outbound profit". */
    static Double findCurrentProfit(String text) {
        Matcher m = CURRENT_PROFIT_SAME_LINE.matcher(text);
        if (m.find()) {
            return parseNumber(m.group(1));
        }
        return moneyAfterLabel(text, "currentprofit");
    }

    /**
     * Valor de una tarjeta cuyo importe queda en una línea propia debajo de la etiqueta,
     * p.ej. "$ Current profit in" seguido de "$172".
     */
    private static Double moneyAfterLabel(String text, String compactLabel) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String lower = lines[i].toLowerCase(Locale.ROOT);
            if (lower.contains("cycle") || lower.contains("outbound")) {
                continue;
            }
            if (!lower.replaceAll("[^a-z]", "").contains(compactLabel)) {
                continue;
            }
            for (int j = i + 1; j < Math.min(i + 5, lines.length); j++) {
                String next = lines[j].trim();
                if (next.isEmpty()) {
                    continue;
                }
                Matcher money = BARE_NUMBER.matcher(next);
                return money.matches() ? parseNumber(money.group(1)) : null;
            }
        }
        return null;
    }

    static Double findIncomePerDay(String text) {
        Matcher m = INCOME_PER_DAY.matcher(text);
        if (m.find()) {
            double v = parseNumber(m.group(1));
            if (v > 0) {
                return v;
            }
        }
        return null;
    }

    /**
     * "Days on Route" es una tarjeta y el valor puede quedar en la línea <b>anterior</b>
     * (como "Total Distance") o en la <b>siguiente</b>, según cómo se serialice el modal.
     */
    static Double findDaysOnRoute(String text) {
        Matcher same = DAYS_SAME_LINE.matcher(text);
        if (same.find()) {
            double v = parseNumber(same.group(1));
            if (v > 0 && v <= MAX_PLAUSIBLE_DAYS) {
                return v;
            }
        }

        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String compact = lines[i].toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            if (!compact.equals("daysonroute")) {
                continue;
            }
            Double dias = plausibleDays(matchNear(lines, i, -1, BARE_NUMBER));
            if (dias == null) {
                dias = plausibleDays(matchNear(lines, i, 1, BARE_NUMBER));
            }
            if (dias != null) {
                return dias;
            }
        }
        return null;
    }

    private static Double plausibleDays(Double v) {
        return v != null && v > 0 && v <= MAX_PLAUSIBLE_DAYS ? v : null;
    }

    static Double findIncomeCompact(String text) {
        Matcher m = INCOME_COMPACT.matcher(text);
        while (m.find()) {
            if (esTotalIncome(text, m)) {
                continue;
            }
            double v = parseNumber(m.group(1));
            // Evitar Income per day si el lookahead falló por formato raro
            int end = m.end();
            String after = text.substring(end, Math.min(text.length(), end + 12)).toLowerCase(Locale.ROOT);
            if (after.contains("/day") || after.contains("per day")) {
                continue;
            }
            if (v >= 50) {
                return v;
            }
        }
        return null;
    }

    /**
     * "Total income" es el acumulado del ciclo/ruta, no el Income de la carga: solo sirve
     * si no se pudo leer el campo Income del modal.
     */
    static Double findTotalIncome(String text) {
        Matcher m = INCOME_COMPACT.matcher(text);
        while (m.find()) {
            if (!esTotalIncome(text, m)) {
                continue;
            }
            double v = parseNumber(m.group(1));
            if (v >= 50) {
                return v;
            }
        }
        return null;
    }

    private static boolean esTotalIncome(String text, Matcher m) {
        if (m.group().toLowerCase(Locale.ROOT).startsWith("total")) {
            return true;
        }
        String before = text.substring(Math.max(0, m.start() - 8), m.start())
                .toLowerCase(Locale.ROOT);
        return before.trim().endsWith("total");
    }

    static Double findRpmCompact(String text) {
        Matcher m = RPM_COMPACT.matcher(text);
        if (m.find()) {
            double v = parseNumber(m.group(1));
            if (v > 0 && v <= MAX_PLAUSIBLE_RPM) {
                return v;
            }
        }
        return null;
    }

    static Double findDistanceCompact(String text) {
        Matcher m = DISTANCE_COMPACT.matcher(text);
        while (m.find()) {
            // Verificar que no sea "Total Distance" mirando 12 chars antes
            int start = m.start();
            String before = text.substring(Math.max(0, start - 12), start).toLowerCase(Locale.ROOT);
            if (before.contains("total")) {
                continue;
            }
            double v = parseNumber(m.group(1));
            if (v >= 10) {
                return v;
            }
        }
        return null;
    }

    static Double findIncome(String text) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String lower = lines[i].trim().toLowerCase(Locale.ROOT);
            if (!lower.contains("income")) {
                continue;
            }
            // "Total income" es del ciclo completo, no de la carga
            if (!lower.replace("total income", "").contains("income")) {
                continue;
            }
            // Línea solo de "Income per day" → saltar; si mezcla Income + per day, usar compact
            if (lower.contains("per day") || lower.contains("/day")) {
                if (!lower.matches("(?s).*\\bincome\\b(?!\\s*per\\s*day).*")) {
                    continue;
                }
            }
            Double same = moneyPreferringIncome(lines[i]);
            if (same != null) {
                return same;
            }
            Double ahead = lookAheadIncome(lines, i + 1);
            if (ahead != null) {
                return ahead;
            }
        }
        return null;
    }

    private static Double moneyPreferringIncome(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        Matcher m = MONEY.matcher(line);
        Double best = null;
        while (m.find()) {
            double v = parseNumber(m.group(1));
            String after = line.substring(m.end()).toLowerCase(Locale.ROOT).trim();
            if (after.startsWith("/day")) {
                continue;
            }
            if (v > MAX_PLAUSIBLE_RPM) {
                return v;
            }
            if (!lower.contains("/mi") && v >= 1) {
                best = v;
            }
        }
        return best;
    }

    private static Double lookAheadIncome(String[] lines, int from) {
        for (int j = from; j < Math.min(from + 10, lines.length); j++) {
            String next = lines[j].trim();
            if (next.isEmpty() || isMetricLabel(next)) {
                continue;
            }
            String lower = next.toLowerCase(Locale.ROOT);
            if (lower.contains("/day") || lower.contains("per day")) {
                continue;
            }
            if (lower.contains("/mi") && !hasLargeMoney(next)) {
                continue;
            }
            if (MILES.matcher(next).find() && !hasLargeMoney(next) && !lower.contains("$")) {
                continue;
            }
            Matcher money = MONEY.matcher(next);
            if (money.find()) {
                double v = parseNumber(money.group(1));
                if (v > MAX_PLAUSIBLE_RPM || (v >= 50 && !lower.contains("/mi"))) {
                    return v;
                }
            }
        }
        return null;
    }

    private static boolean hasLargeMoney(String line) {
        Matcher m = MONEY.matcher(line);
        while (m.find()) {
            if (parseNumber(m.group(1)) > MAX_PLAUSIBLE_RPM) {
                return true;
            }
        }
        return false;
    }

    static Double findPlainDistance(String text) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String lower = lines[i].trim().toLowerCase(Locale.ROOT);
            if (!lower.contains("distance")) {
                continue;
            }
            if (lower.contains("dh") || lower.contains("deadhead") || lower.contains("total")) {
                continue;
            }
            String compact = lower.replaceAll("[^a-z]", "");
            if (compact.startsWith("distancedh") || compact.contains("totaldistance")) {
                continue;
            }
            Matcher same = MILES.matcher(lines[i]);
            if (same.find()) {
                double v = parseNumber(same.group(1));
                if (v >= 10) {
                    return v;
                }
            }
            for (int j = i + 1; j < Math.min(i + 8, lines.length); j++) {
                String next = lines[j].trim();
                String nextLower = next.toLowerCase(Locale.ROOT);
                if (isMetricLabel(next)) {
                    if (nextLower.contains("dh") || nextLower.contains("total")
                            || nextLower.contains("income") || nextLower.contains("rpm")) {
                        continue;
                    }
                    if (nextLower.contains("distance")) {
                        break;
                    }
                    continue;
                }
                if (nextLower.contains("dh") || nextLower.contains("deadhead") || nextLower.contains("/mi")) {
                    continue;
                }
                Matcher nextM = MILES.matcher(next);
                if (nextM.find()) {
                    double v = parseNumber(nextM.group(1));
                    if (v >= 10) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    static Double findLabeledPlainRpm(String text) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String lineLower = lines[i].trim().toLowerCase(Locale.ROOT);
            if (!lineLower.contains("rpm")) {
                continue;
            }
            if (isRpmWithDh(lineLower) || lineLower.contains("profit") || lineLower.contains("cumulative")) {
                continue;
            }
            Double fromLine = firstPlausibleRpm(lines[i]);
            if (fromLine != null) {
                return fromLine;
            }
            for (int j = i + 1; j < Math.min(i + 8, lines.length); j++) {
                String next = lines[j].trim();
                String nextLower = next.toLowerCase(Locale.ROOT);
                if (isRpmWithDh(nextLower)) {
                    if (firstPlausibleRpm(next) != null) {
                        break;
                    }
                    continue;
                }
                if (isMetricLabel(next)) {
                    continue;
                }
                Double fromNext = firstPlausibleRpm(next);
                if (fromNext != null) {
                    return fromNext;
                }
            }
        }
        return null;
    }

    static Double findPlainRpm(String text) {
        return findLabeledPlainRpm(text);
    }

    private static Double closestRpmTo(String text, double expected) {
        Double best = null;
        double bestDelta = Double.MAX_VALUE;
        Matcher m = RPM_VALUE.matcher(text);
        while (m.find()) {
            double val = parseNumber(m.group(1));
            if (val <= 0 || val > MAX_PLAUSIBLE_RPM) {
                continue;
            }
            String before = text.substring(Math.max(0, m.start() - 24), m.start()).toLowerCase(Locale.ROOT);
            if (before.contains("with dh") || before.contains("profit") || before.contains("cumulative")) {
                continue;
            }
            double delta = Math.abs(val - expected);
            if (delta < bestDelta) {
                bestDelta = delta;
                best = val;
            }
        }
        return best;
    }

    private static Double firstPlausibleRpm(String chunk) {
        Matcher m = RPM_VALUE.matcher(chunk);
        while (m.find()) {
            double val = parseNumber(m.group(1));
            if (val > 0 && val <= MAX_PLAUSIBLE_RPM) {
                return val;
            }
        }
        return null;
    }

    private static boolean isRpmWithDh(String lower) {
        return lower.contains("with dh")
                || lower.contains("with deadhead")
                || lower.contains("rpm with")
                || lower.contains("dh-o");
    }

    private static boolean isMetricLabel(String line) {
        String compact = line.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        return compact.equals("rpm")
                || compact.equals("income")
                || compact.equals("totalincome")
                || compact.equals("distance")
                || compact.equals("incomeperday")
                || compact.startsWith("rpmwith")
                || compact.startsWith("distancedh")
                || compact.equals("totalcosts")
                || compact.equals("totaldistance")
                || compact.equals("currentprofit")
                || compact.equals("profitmile")
                || compact.equals("daysonroute");
    }

    static double parseNumber(String raw) {
        return Double.parseDouble(raw.replace(",", "").trim());
    }
}
