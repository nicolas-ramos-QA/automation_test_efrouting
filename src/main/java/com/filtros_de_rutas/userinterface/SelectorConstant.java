package com.filtros_de_rutas.userinterface;

import net.serenitybdd.screenplay.targets.Target;

public class SelectorConstant {

    public static final Target MAIL_INPUT = Target.the("Campo Email address")
            .locatedBy("//input[@placeholder='Type your email']");
    public static final Target PASSWORD_INPUT = Target.the("Campo Password")
            .locatedBy("//input[@name='password']");
    public static final String LOGIN_INPUT = "//button[normalize-space()='Log in']";

    public static final String ROUTES_MENU = "//a[contains(@href,'route-planner')]";
    public static final String NEW_ROUTE_BUTTON = "//button[contains(normalize-space(.),'New route')]";

    public static final String FILTER_BUTTON =
            "//button[contains(normalize-space(.),'Filter')]"
                    + " | //*[@role='button'][contains(normalize-space(.),'Filter')]"
                    + " | //*[self::button or self::div][normalize-space()='Filter']";

    public static final String RESET_BUTTON =
            "//button[normalize-space()='Reset']"
                    + " | //*[@role='button'][normalize-space()='Reset']"
                    + " | //button[contains(normalize-space(.),'Reset')]";

    public static final String FILTER_FIELD_TEMPLATE =
            "//*[self::button or self::div or self::span or self::label]"
                    + "[normalize-space()='%s']";

    public static final String RESULTS_LABEL =
            "//*[contains(normalize-space(.),'Results')]";

    public static final String FOOTER_TOTAL_ROUTES =
            "//*[contains(normalize-space(.),'Total routes')]";

    private SelectorConstant() {}
}
