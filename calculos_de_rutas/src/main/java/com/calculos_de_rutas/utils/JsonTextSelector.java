package com.calculos_de_rutas.utils;

public class JsonTextSelector {
    public static final String PATH_FILE = ConstantJson.MESSAGE_INPUT.getString("path_file");
    public static final String EMAIL_VALUE = ConstantJson.MESSAGE_INPUT.getString("email_value");
    public static final String PASSWORD_VALUE = ConstantJson.MESSAGE_INPUT.getString("password");
    public static final String LOGIN_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("login_input_name");
    public static final String ROUTES_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("routes_input_name");
    public static final String NEW_ROUTE_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("new_route_input_name");
    public static final String TRAILER_TYPE_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("trailer_type_input_name");
    public static final String ORIGIN_VALUE = ConstantJson.MESSAGE_INPUT.getString("origin_value");
    public static final String DESTINATION_VALUE = ConstantJson.MESSAGE_INPUT.getString("destination_value");
    public static final String DAYS_ON_ROUTE_VALUE = ConstantJson.MESSAGE_INPUT.getString("days_on_route_value");
    public static final String CONTINUE_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("continue_input_name");

    private JsonTextSelector() {}
}
