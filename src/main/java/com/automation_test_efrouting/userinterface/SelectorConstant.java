package com.automation_test_efrouting.userinterface;

import net.serenitybdd.screenplay.targets.Target;

public class SelectorConstant {

    public static final Target MAIL_INPUT = Target.the("Campo Email address")
            .locatedBy("//input[@placeholder='Type your email']");
    public static final Target PASSWORD_INPUT = Target.the("Campo Password")
            .locatedBy("//input[@name='password']");
    public static final Target SUBJECT_INPUT = Target.the("Campo de asunto")
            .locatedBy("//input[@name='subject']");
    public static final Target MESSAGE_INPUT = Target.the("Campo de mensaje")
            .locatedBy("//textarea[@id='message']");
    public static final Target HOME_TEXT_INPUT = Target.the("Nombre home")
            .locatedBy("//h1[normalize-space()='Home']");
    public static final Target CAMPO_UPLOAD = Target.the("Campo de archivo")
            .locatedBy("//input[@type='file']");
    public static final String LOGIN_INPUT = "//button[normalize-space()='Log in']";
    public static final String SUCCES_INPUT = "//div[@class='status alert alert-success']";
    public static final String ROUTES_MENU = "//a[contains(@href,'route-planner')]";
    public static final Target ROUTES_SECTION = Target.the("Botón New route")
            .locatedBy("//button[contains(normalize-space(.),'New route')]");
    public static final String NEW_ROUTE_BUTTON = "//button[contains(normalize-space(.),'New route')]";
    public static final String TRAILER_TYPE_TAB = "//*[normalize-space(text())='Trailer type only']";
    public static final String TRAILER_SELECT = "[data-cy='select-trailerType']";
    public static final String TRAILER_OPTION_VAN = "[data-cy='select-option-trailerType-van']";
    public static final String ORIGIN_INPUT = "#origin";
    public static final String AUTOCOMPLETE_RESULTS = "[data-cy='autocomplete-results-container']";
    public static final String AUTOCOMPLETE_RESULT_ITEM = "[data-cy='autocomplete-result-item']";
    public static final String DAYS_TRIGGER = "[data-cy='day-range-trigger']";
    public static final String DEPARTURE_TRIGGER = "[data-cy='select-departure-date']";
    public static final String CALENDAR_PICKER = "[data-cy='calendar-picker']";
    public static final String CONTINUE_BUTTON = "[data-cy='next-button-step-one']";

    /**
     * Secciones de rutas sugeridas en Easy routes. Hay que hacer clic en una tarjeta
     * para expandirla; recién ahí aparece "Try this route".
     * Incluye variantes de mayúsculas y "Best Choice" (a veces con ★ delante).
     */
    public static final String[] ROUTE_SECTION_LABELS = {
            "Tri-hauls", "Bi-hauls", "Best Choice", "Best choice", "Loops", "Direct Routes"
    };

    /**
     * Contenedor de la sección: h2 cuyo texto contiene la etiqueta.
     * Usa contains para soportar "★ Best Choice" y textos similares.
     */
    public static final String ROUTE_SECTION_HEADER =
            "//h2[.//span[contains(normalize-space(.),'%1$s')] or contains(normalize-space(.),'%1$s')]";

    /** Zona clickeable de una tarjeta (índice 1-based). */
    public static final String ROUTE_CARD_CLICKABLE =
            "(" + ROUTE_SECTION_HEADER
                    + "/following-sibling::div[1]//div[@data-index][%2$d]"
                    + "//div[contains(@class,'cursor-pointer')])[1]"
                    + " | (" + ROUTE_SECTION_HEADER
                    + "/following-sibling::div[1]//div[@data-index][%2$d])[1]";

    public static final String ROUTE_CARDS_IN_SECTION =
            ROUTE_SECTION_HEADER + "/following-sibling::div[1]//div[@data-index]";

    public static final String TRY_THIS_ROUTE_IN_SECTION =
            "(" + ROUTE_SECTION_HEADER
                    + "/following-sibling::div[1]//button[contains(normalize-space(.),'Try this route')])[1]";

    public static final String ANY_TRY_THIS_ROUTE_BUTTON =
            "(//button[contains(normalize-space(.),'Try this route')])[1]";

    /** Compatibilidad con el paso antiguo (siempre Tri-hauls, segunda tarjeta). */
    public static final String TRI_HAULS_SECOND_ROUTE =
            String.format(ROUTE_CARD_CLICKABLE, "Tri-hauls", 2);
    public static final String TRY_THIS_ROUTE_BUTTON =
            String.format(TRY_THIS_ROUTE_IN_SECTION, "Tri-hauls");

    public static final String LANE_OPTIONS_BUTTON_LAST = "(//button[@data-cy='button-dropdown-lane-options'])[last()]";
    public static final String EDIT_LANE_OPTION = "//*[normalize-space(text())='Edit lane']";
    public static final String EDIT_ORIGIN_INPUT = "//input[@placeholder='Enter origin']";
    public static final String EDIT_ORIGIN_FIRST_SUGGESTION = "//*[@data-cy='undefined-results-item-0']";
    public static final String EDIT_START_TIME = "//input[@data-cy='input-start-time']";
    public static final String EDIT_END_TIME = "//input[@data-cy='input-end-time']";
    public static final String EDIT_SAVE_BUTTON = "//button[normalize-space()='Save changes']";
    public static final String LANE_MILEAGE_TEMPLATE = "//*[starts-with(@data-cy,'principal-table-row-')]"
            + "[.//*[@data-cy='route-detail-cell-origin-city' and normalize-space()='%s'] "
            + "and .//*[@data-cy='route-detail-cell-destination-city' and normalize-space()='%s']]"
            + "//*[@data-cy='mileage-value']";
    private SelectorConstant() {}
}
