package com.filtros_de_rutas.models;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Valores de la primera fila visible, usados para rellenar cada filtro.
 */
public class RouteRowSample {

    private final Map<String, String> values = new LinkedHashMap<>();

    public void put(String filterName, String value) {
        if (filterName == null || filterName.isBlank() || value == null) {
            return;
        }
        String trimmed = value.replaceAll("\\s+", " ").trim();
        if (!trimmed.isEmpty()) {
            values.put(normalize(filterName), trimmed);
        }
    }

    public String get(String filterName) {
        return values.get(normalize(filterName));
    }

    public boolean has(String filterName) {
        String value = get(filterName);
        return value != null && !value.isBlank();
    }

    public Map<String, String> asMap() {
        return Map.copyOf(values);
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
