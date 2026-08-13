package com.automation_test_efrouting.utils;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetMessageJson {
    private GetMessageJson() {}

    public static JSONObject getInput() {
        try {
            JSONObject input = null;
            String message = Files.readString(
                                Paths.get(ConstantJson.PATH_JSON),
                                    StandardCharsets.UTF_8
                                );
            JSONObject jsonMessage = new JSONObject(message);
            if(jsonMessage.has(ConstantJson.JSON_DATA)) {
                JSONObject dataInput = jsonMessage.getJSONObject(ConstantJson.JSON_DATA);
                if(dataInput.has(ConstantJson.JSON_INPUT)) {
                    input = dataInput.getJSONObject(ConstantJson.JSON_INPUT);
                }else  {
                    throw new AssertionError(ExceptionMessages.ERROR_INPUT);
                }
            } else {
                throw new AssertionError(ExceptionMessages.ERROR_DATA);
            }
            return input;
        }catch (Exception e){
            throw new AssertionError(ExceptionMessages.ERROR_JSON);
        }
    }

    public static JSONObject getOutput() {
        try {
            JSONObject output = null;
            String message = Files.readString(
                                Paths.get(ConstantJson.PATH_JSON),
                                    StandardCharsets.UTF_8
                                );
            JSONObject jsonMessage = new JSONObject(message);
            if(jsonMessage.has(ConstantJson.JSON_DATA)) {
                JSONObject dataOutput = jsonMessage.getJSONObject(ConstantJson.JSON_DATA);
                if(dataOutput.has(ConstantJson.JSON_OUTPUT)) {
                    output = dataOutput.getJSONObject(ConstantJson.JSON_OUTPUT);
                }else  {
                    throw new AssertionError(ExceptionMessages.ERROR_OUTPUT);
                }
            } else {
                throw new AssertionError(ExceptionMessages.ERROR_DATA);
            }
            return output;
        }catch (Exception e){
            throw new AssertionError(ExceptionMessages.ERROR_JSON);
        }
    }
}
