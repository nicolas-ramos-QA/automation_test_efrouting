package com.filtros_de_rutas.utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class JsonTextSelector {
    public static final String EMAIL_VALUE = ConstantJson.MESSAGE_INPUT.getString("email_value");
    public static final String PASSWORD_VALUE = ConstantJson.MESSAGE_INPUT.getString("password");
    public static final String LOGIN_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("login_input_name");
    public static final String ROUTES_INPUT_NAME = ConstantJson.MESSAGE_INPUT.getString("routes_input_name");
    public static final String ROUTES_VALUE = ConstantJson.MESSAGE_OUTPUT.getString("routes_value");
    public static final String FILTER_BUTTON_NAME = ConstantJson.MESSAGE_INPUT.optString("filter_button_name", "Filter");
    public static final String FILTER_RESET_NAME = ConstantJson.MESSAGE_INPUT.optString("filter_reset_name", "Reset");
    public static final String ROUTE_PLANNER_URL = ConstantJson.MESSAGE_INPUT.optString(
            "route_planner_url", "https://efdata-qa.efrouting.com/route-planner");

    private JsonTextSelector() {}

    public static List<String> filterFields() {
        JSONArray array = ConstantJson.MESSAGE_INPUT.optJSONArray("filter_fields");
        List<String> fields = new ArrayList<>();
        if (array == null) {
            return List.of(
                    "Route name", "Start date", "End date", "Origin", "Destination",
                    "Driver", "Trailer", "Unit", "Dispatcher", "Equipment type",
                    "Total miles", "Income");
        }
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "").trim();
            if (!value.isEmpty()) {
                fields.add(value);
            }
        }
        return fields;
    }
}
