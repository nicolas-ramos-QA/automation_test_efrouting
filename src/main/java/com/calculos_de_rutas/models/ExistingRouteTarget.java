package com.calculos_de_rutas.models;

import com.calculos_de_rutas.utils.Environments;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ruta ya existente que se valida sin crearla: URL completa o solo el ID,
 * leída de {@code -Druta} / {@code -Dambiente}.
 */
public final class ExistingRouteTarget {

    private static final Pattern DETAIL_IN_URL = Pattern.compile(
            "/route-planner/detail/(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");

    private final String routeId;
    private final String detailUrl;
    private final TestEnvironment environment;

    ExistingRouteTarget(String routeId, String detailUrl, TestEnvironment environment) {
        this.routeId = routeId;
        this.detailUrl = detailUrl;
        this.environment = environment;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public TestEnvironment getEnvironment() {
        return environment;
    }

    public String getEnvironmentName() {
        return environment.getName();
    }

    public String reportSuffix() {
        return "ruta-existente-" + routeId;
    }

    /**
     * Lee {@code -Druta} (URL o ID) y opcionalmente {@code -Dambiente}.
     * También acepta {@code -Droute}, {@code -Droute.url}, {@code -Droute.id} y la env {@code RUTA}.
     */
    public static ExistingRouteTarget fromSystem() {
        String raw = firstNonBlank(
                System.getProperty("ruta"),
                System.getProperty("route"),
                System.getProperty("route.url"),
                System.getProperty("route.id"),
                System.getenv("RUTA"));
        String ambiente = firstNonBlank(
                System.getProperty("ambiente"),
                System.getenv("AMBIENTE"));
        return parse(raw, ambiente);
    }

    public static ExistingRouteTarget parse(String raw, String ambienteOverride) {
        if (raw == null || raw.isBlank()) {
            throw new AssertionError(missingRouteMessage());
        }

        String trimmed = raw.trim();
        String routeId = extractId(trimmed);
        String inferred = inferEnvironmentName(trimmed, ambienteOverride);
        TestEnvironment environment = Environments.byName(inferred);
        String detailUrl = buildDetailUrl(trimmed, routeId, environment);
        return new ExistingRouteTarget(routeId, detailUrl, environment);
    }

    static String extractId(String raw) {
        Matcher inUrl = DETAIL_IN_URL.matcher(raw);
        if (inUrl.find()) {
            return inUrl.group(1);
        }
        String pathEnd = raw.contains("?") ? raw.substring(0, raw.indexOf('?')) : raw;
        if (DIGITS_ONLY.matcher(pathEnd.trim()).matches()) {
            return pathEnd.trim();
        }
        throw new AssertionError(
                "No se pudo obtener el ID de la ruta a partir de '" + raw + "'. "
                        + "Pase una URL como https://efdata-qa.efrouting.com/route-planner/detail/3417 "
                        + "o solo el número (3417).");
    }

    static String inferEnvironmentName(String raw, String ambienteOverride) {
        if (ambienteOverride != null && !ambienteOverride.isBlank()) {
            return ambienteOverride.trim();
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("efdata-qa") || lower.contains("efdata.qa") || lower.contains("-qa.")) {
            return "QA";
        }
        if (lower.contains("efdata.efrouting.com")) {
            return "PRODUCCION";
        }
        return "QA";
    }

    static String buildDetailUrl(String raw, String routeId, TestEnvironment environment) {
        Matcher inUrl = DETAIL_IN_URL.matcher(raw);
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            if (inUrl.find()) {
                int cut = raw.indexOf('?');
                return cut > 0 ? raw.substring(0, cut) : raw;
            }
        }
        String base = environment.getUrl().replaceFirst("/login/?$", "");
        return base + "/route-planner/detail/" + routeId;
    }

    public static String missingRouteMessage() {
        return """
                Falta la ruta a validar. Ejecute desde la raíz del repo:

                  mvn test "-Dtest=RunnerCalculosRutaExistente" "-Druta=https://efdata-qa.efrouting.com/route-planner/detail/3417"

                También vale solo el ID (usa QA por defecto):

                  mvn test "-Dtest=RunnerCalculosRutaExistente" "-Druta=3417"

                Para Producción: agregue "-Dambiente=PRODUCCION".
                """;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
