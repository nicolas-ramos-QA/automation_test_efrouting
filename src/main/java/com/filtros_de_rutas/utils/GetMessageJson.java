package com.filtros_de_rutas.utils;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetMessageJson {
    private GetMessageJson() {}

    public static JSONObject getInput() {
        return child(ConstantJson.JSON_INPUT, ExceptionMessages.ERROR_INPUT);
    }

    public static JSONObject getOutput() {
        return child(ConstantJson.JSON_OUTPUT, ExceptionMessages.ERROR_OUTPUT);
    }

    public static JSONObject getEnvironments() {
        return child(ConstantJson.JSON_ENVIRONMENTS, "No se encontró la clave 'environments' en data.json");
    }

    private static JSONObject child(String key, String missingMessage) {
        try {
            String message = Files.readString(Paths.get(ConstantJson.PATH_JSON), StandardCharsets.UTF_8);
            JSONObject jsonMessage = new JSONObject(message);
            if (!jsonMessage.has(ConstantJson.JSON_DATA)) {
                throw new AssertionError(ExceptionMessages.ERROR_DATA);
            }
            JSONObject data = jsonMessage.getJSONObject(ConstantJson.JSON_DATA);
            if (!data.has(key)) {
                throw new AssertionError(missingMessage);
            }
            return data.getJSONObject(key);
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(ExceptionMessages.ERROR_JSON);
        }
    }
}
