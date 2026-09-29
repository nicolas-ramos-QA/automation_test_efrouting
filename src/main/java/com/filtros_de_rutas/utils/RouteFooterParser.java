package com.filtros_de_rutas.utils;

import com.filtros_de_rutas.models.RouteFooterTotals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RouteFooterParser {

    private static final Pattern RESULTS = Pattern.compile(
            "(?i)Results\\s*:?\\s*([\\d,.]+)");
    private static final Pattern TOTAL_ROUTES = Pattern.compile(
            "(?is)Total\\s*routes\\s+([\\d,.]+)");
    private static final Pattern TOTAL_INCOME = Pattern.compile(
            "(?is)Total\\s*income\\s+(\\$[^\\r\\n]+?)(?=\\s*(?:Total\\s*miles|DH\\s*miles|Effective|Loaded|$))");
    private static final Pattern TOTAL_MILES = Pattern.compile(
            "(?is)Total\\s*miles\\s+([^\\r\\n]+?)(?=\\s*(?:DH\\s*miles|Effective|Loaded|$))");
    private static final Pattern DH_MILES = Pattern.compile(
            "(?is)DH\\s*miles\\s+([^\\r\\n]+?)(?=\\s*(?:Effective|Loaded|$))");
    private static final Pattern EFFECTIVE_RPM = Pattern.compile(
            "(?is)Effective\\s*RPM\\s+([^\\r\\n]+?)(?=\\s*(?:Loaded|$))");
    private static final Pattern LOADED_RPM = Pattern.compile(
            "(?is)Loaded\\s*RPM\\s+([^\\r\\n]+)");

    private RouteFooterParser() {}

    public static RouteFooterTotals parse(String pageText) {
        String text = pageText == null ? "" : pageText;
        return new RouteFooterTotals(
                parseInt(first(RESULTS, text)),
                parseInt(first(TOTAL_ROUTES, text)),
                clean(first(TOTAL_INCOME, text)),
                clean(first(TOTAL_MILES, text)),
                clean(first(DH_MILES, text)),
                clean(first(EFFECTIVE_RPM, text)),
                clean(first(LOADED_RPM, text)),
                text);
    }

    private static String first(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static Integer parseInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String clean(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
        return value.isEmpty() ? null : value;
    }
}
