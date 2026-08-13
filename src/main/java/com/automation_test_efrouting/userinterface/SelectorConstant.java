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
    public static final String TRI_HAULS_SECOND_ROUTE = "//h2[.//span[normalize-space()='Tri-hauls']]/following-sibling::div//div[@data-index='1']//div[contains(@class,'cursor-pointer')]";
    public static final String TRY_THIS_ROUTE_BUTTON = "//h2[.//span[normalize-space()='Tri-hauls']]/following-sibling::div//div[@data-index='1']//button[contains(normalize-space(.),'Try this route')]";
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
