package com.validacion_calculadora.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Elige pares origen/destino desde {@code data.json → city_pairs}.
 */
public final class LoadSearchCities {

    private final String origin;
    private final String destination;

    private LoadSearchCities(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String key() {
        return (origin + "|" + destination).toLowerCase(Locale.ROOT);
    }

    public static LoadSearchCities randomPair() {
        return randomPairExcluding(Set.of());
    }

    public static LoadSearchCities randomPairExcluding(Set<String> usedKeys) {
        List<LoadSearchCities> options = new ArrayList<>();
        for (LoadSearchCities p : allPairs()) {
            if (!usedKeys.contains(p.key())) {
                options.add(p);
            }
        }
        if (options.isEmpty()) {
            options = allPairs();
        }
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }

    public static List<LoadSearchCities> allPairs() {
        JSONArray pairs = ConstantJson.MESSAGE_INPUT.getJSONArray("city_pairs");
        if (pairs == null || pairs.isEmpty()) {
            throw new AssertionError("data.json no tiene 'city_pairs' con orígenes/destinos.");
        }
        List<LoadSearchCities> options = new ArrayList<>();
        for (int i = 0; i < pairs.length(); i++) {
            JSONObject pair = pairs.getJSONObject(i);
            String origin = pair.getString("origin");
            String destination = pair.getString("destination");
            if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
                continue;
            }
            if (origin.equalsIgnoreCase(destination)) {
                continue;
            }
            options.add(new LoadSearchCities(origin.trim(), destination.trim()));
        }
        if (options.isEmpty()) {
            throw new AssertionError("Ningún city_pair válido en data.json.");
        }
        return options;
    }
}
