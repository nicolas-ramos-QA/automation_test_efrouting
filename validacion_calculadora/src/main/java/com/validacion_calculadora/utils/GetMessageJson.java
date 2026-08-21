package com.validacion_calculadora.utils;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetMessageJson {
    private GetMessageJson() {}

    public static JSONObject getInput() {
        try {
            String message = Files.readString(Paths.get(ConstantJson.PATH_JSON), StandardCharsets.UTF_8);
            JSONObject jsonMessage = new JSONObject(message);
            if (!jsonMessage.has(ConstantJson.JSON_DATA)) {
                throw new AssertionError(ExceptionMessages.ERROR_DATA);
            }
            JSONObject dataInput = jsonMessage.getJSONObject(ConstantJson.JSON_DATA);
            if (!dataInput.has(ConstantJson.JSON_INPUT)) {
                throw new AssertionError(ExceptionMessages.ERROR_INPUT);
            }
            return dataInput.getJSONObject(ConstantJson.JSON_INPUT);
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(ExceptionMessages.ERROR_JSON);
        }
    }

    public static JSONObject getOutput() {
        try {
            String message = Files.readString(Paths.get(ConstantJson.PATH_JSON), StandardCharsets.UTF_8);
            JSONObject jsonMessage = new JSONObject(message);
            if (!jsonMessage.has(ConstantJson.JSON_DATA)) {
                throw new AssertionError(ExceptionMessages.ERROR_DATA);
            }
            JSONObject dataOutput = jsonMessage.getJSONObject(ConstantJson.JSON_DATA);
            if (!dataOutput.has(ConstantJson.JSON_OUTPUT)) {
                throw new AssertionError(ExceptionMessages.ERROR_OUTPUT);
            }
            return dataOutput.getJSONObject(ConstantJson.JSON_OUTPUT);
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(ExceptionMessages.ERROR_JSON);
        }
    }
}
