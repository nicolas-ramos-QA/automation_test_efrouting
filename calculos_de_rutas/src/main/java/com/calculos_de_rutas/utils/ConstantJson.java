package com.calculos_de_rutas.utils;

import org.json.JSONObject;

public class ConstantJson {

    private ConstantJson() {}

    public static final String PATH_JSON = "src/test/resources/data.json";
    public static final String JSON_DATA = "data";
    public static final String JSON_INPUT = "input";
    public static final String JSON_OUTPUT = "output";
    public static final String JSON_ENVIRONMENTS = "environments";

    public static final JSONObject MESSAGE_INPUT = GetMessageJson.getInput();
    public static final JSONObject MESSAGE_OUTPUT = GetMessageJson.getOutput();
}
