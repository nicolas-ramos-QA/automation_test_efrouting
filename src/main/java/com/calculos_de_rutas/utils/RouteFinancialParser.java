package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.LaneFinancials;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import com.calculos_de_rutas.models.RouteTotals;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parser del response de user-route de efRouting.
 *
 * <p>Estructura esperada:
 * <pre>
 * { "id", "name", "operativeCost": { "total" },
 *   "lanes": [ { "laneId", "type",
 *                "origin": { "location": { "city_name", "state_code" } },
 *                "destination": { "location": { ... } },
 *                "operative": { "mileage": {"estimated"}, "drivingTime": {...}, "operationTime": {...} },
 *                "finance": { "income": {"min","max","avg"}, "profit": {"min","max","avg"},
 *                             "fuelCost", "tollCost", "customCost", "totalCost" } } ] }
 * </pre>
 */
public final class RouteFinancialParser {

    private RouteFinancialParser() {}

    public static RouteFinancialPayload parse(String rawJson) {
        JSONObject root = new JSONObject(rawJson);
        JSONObject data = root.has("data") && root.get("data") instanceof JSONObject
                ? root.getJSONObject("data")
                : root;

        JSONObject finance = optObject(data, "finance");
        Double operativeCostRate = nestedDouble(data, "operativeCost", "total");
        if (operativeCostRate == null && finance != null) {
            operativeCostRate = optDouble(finance, "operativeCost");
        }

        RouteFinancialPayload.Builder builder = RouteFinancialPayload.builder()
                .rawJson(rawJson)
                .routeId(optString(data, "id"))
                .routeName(optString(data, "name"))
                .operativeCostRate(operativeCostRate)
                .routeMileage(estimated(optObject(data, "operative"), "mileage"))
                .totals(parseTotals(finance));

        JSONArray lanes = optArray(data, "lanes");
        if (lanes != null) {
            for (int i = 0; i < lanes.length(); i++) {
                builder.addLane(parseLane(lanes.getJSONObject(i)));
            }
        }
        return builder.build();
    }

    /**
     * Acumulados globales de la ruta. El {@code totalCost} de este bloque ya incluye el Op cost,
     * a diferencia del de cada lane.
     */
    private static RouteTotals parseTotals(JSONObject finance) {
        if (finance == null) {
            return null;
        }
        JSONObject income = optObject(finance, "income");
        JSONObject profit = optObject(finance, "profit");
        JSONObject netProfit = optObject(finance, "netProfit");

        return RouteTotals.builder()
                .incomeMin(income == null ? null : optDouble(income, "min"))
                .incomeMax(income == null ? null : optDouble(income, "max"))
                .incomeAvg(income == null ? null : optDouble(income, "avg"))
                .profitMin(profit == null ? null : optDouble(profit, "min"))
                .profitMax(profit == null ? null : optDouble(profit, "max"))
                .profitAvg(profit == null ? null : optDouble(profit, "avg"))
                .netProfitMin(netProfit == null ? null : optDouble(netProfit, "min"))
                .netProfitMax(netProfit == null ? null : optDouble(netProfit, "max"))
                .netProfitAvg(netProfit == null ? null : optDouble(netProfit, "avg"))
                .fuelCost(optDouble(finance, "fuelCost"))
                .tollCost(optDouble(finance, "tollCost"))
                .customCost(optDouble(finance, "customCost"))
                .operativeCost(optDouble(finance, "operativeCost"))
                .totalCost(optDouble(finance, "totalCost"))
                .build();
    }

    private static LaneFinancials parseLane(JSONObject lane) {
        JSONObject finance = optObject(lane, "finance");
        JSONObject operative = optObject(lane, "operative");
        JSONObject income = finance == null ? null : optObject(finance, "income");
        JSONObject profit = finance == null ? null : optObject(finance, "profit");

        return LaneFinancials.builder()
                .laneId(optString(lane, "laneId", "id"))
                .type(optString(lane, "type"))
                .origin(locationName(lane, "origin"))
                .destination(locationName(lane, "destination"))
                .incomeMin(income == null ? null : optDouble(income, "min"))
                .incomeMax(income == null ? null : optDouble(income, "max"))
                .incomeAvg(income == null ? null : optDouble(income, "avg"))
                .profitMin(profit == null ? null : optDouble(profit, "min"))
                .profitMax(profit == null ? null : optDouble(profit, "max"))
                .profitAvg(profit == null ? null : optDouble(profit, "avg"))
                .fuelCost(finance == null ? null : optDouble(finance, "fuelCost"))
                .tollCost(finance == null ? null : optDouble(finance, "tollCost"))
                .customCost(finance == null ? null : optDouble(finance, "customCost"))
                .totalCost(finance == null ? null : optDouble(finance, "totalCost"))
                .mileage(estimated(operative, "mileage"))
                .drivingTime(estimated(operative, "drivingTime"))
                .operationTime(estimated(operative, "operationTime"))
                .build();
    }

    /** Nombre legible "Ciudad, ST" desde origin/destination.location. */
    private static String locationName(JSONObject lane, String node) {
        JSONObject wrapper = optObject(lane, node);
        if (wrapper == null) {
            return null;
        }
        JSONObject location = optObject(wrapper, "location");
        JSONObject source = location != null ? location : wrapper;
        String city = optString(source, "city_name", "cityName", "hub_name");
        String state = optString(source, "state_code", "stateCode");
        if (city == null) {
            return null;
        }
        return state == null ? city : city + ", " + state;
    }

    /** Lee el valor "estimated" de un bloque operativo. */
    private static Double estimated(JSONObject operative, String key) {
        if (operative == null) {
            return null;
        }
        JSONObject node = optObject(operative, key);
        if (node != null) {
            return optDouble(node, "estimated", "actual");
        }
        return optDouble(operative, key);
    }

    private static JSONObject optObject(JSONObject parent, String key) {
        return parent.has(key) && parent.get(key) instanceof JSONObject
                ? parent.getJSONObject(key)
                : null;
    }

    private static JSONArray optArray(JSONObject parent, String key) {
        return parent.has(key) && parent.get(key) instanceof JSONArray
                ? parent.getJSONArray(key)
                : null;
    }

    private static Double nestedDouble(JSONObject parent, String node, String key) {
        JSONObject child = optObject(parent, node);
        return child == null ? null : optDouble(child, key);
    }

    private static Double optDouble(JSONObject parent, String... keys) {
        for (String key : keys) {
            if (parent.has(key) && !parent.isNull(key)) {
                Object value = parent.get(key);
                if (value instanceof Number number) {
                    return number.doubleValue();
                }
                if (value instanceof String text && !text.isBlank()) {
                    try {
                        return Double.parseDouble(text.replace(",", "").replace("$", "").trim());
                    } catch (NumberFormatException ignored) {
                        // siguiente clave
                    }
                }
            }
        }
        return null;
    }

    private static String optString(JSONObject parent, String... keys) {
        for (String key : keys) {
            if (parent.has(key) && !parent.isNull(key)) {
                Object value = parent.get(key);
                if (value instanceof String text && !text.isBlank()) {
                    return text.trim();
                }
                if (value instanceof Number number) {
                    return String.valueOf(number);
                }
            }
        }
        return null;
    }
}
