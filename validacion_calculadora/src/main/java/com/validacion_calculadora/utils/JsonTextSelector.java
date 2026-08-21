package com.validacion_calculadora.utils;

public class JsonTextSelector {
    public static final String EMAIL_VALUE = ConstantJson.MESSAGE_INPUT.getString("email_value");
    public static final String PASSWORD_VALUE = ConstantJson.MESSAGE_INPUT.getString("password");
    public static final String LOGIN_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("login_input_name");
    public static final String LOADBOARD_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("loadboard_input_name");
    public static final String LOADBOARD_HEADLINE = ConstantJson.MESSAGE_OUTPUT.getString("loadboard_headline");

    private JsonTextSelector() {}
}
