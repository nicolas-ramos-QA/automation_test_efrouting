package com.filtros_de_rutas.utils;

import com.filtros_de_rutas.models.TestEnvironment;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.Locale;

public final class Environments {

    private Environments() {}

    /** Este flujo corre solo contra QA. */
    public static TestEnvironment resolved() {
        return byName("QA");
    }

    public static TestEnvironment byName(String name) {
        JSONObject environments = GetMessageJson.getEnvironments();
        String key = normalize(name);

        for (String declared : environments.keySet()) {
            if (normalize(declared).equals(key)) {
                JSONObject config = environments.getJSONObject(declared);
                return new TestEnvironment(
                        declared,
                        config.getString("url"),
                        config.getString("email"),
                        config.getString("password"));
            }
        }

        throw new AssertionError("El ambiente '" + name + "' no está definido en data.json. "
                + "Disponibles: " + environments.keySet());
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }
}
