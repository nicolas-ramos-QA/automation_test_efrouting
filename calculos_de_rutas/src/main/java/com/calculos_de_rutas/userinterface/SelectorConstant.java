package com.calculos_de_rutas.userinterface;

import net.serenitybdd.screenplay.targets.Target;

public class SelectorConstant {

    public static final Target MAIL_INPUT = Target.the("Campo Email address")
            .locatedBy("//input[@placeholder='Type your email']");
    public static final Target PASSWORD_INPUT = Target.the("Campo Password")
            .locatedBy("//input[@name='password']");
    public static final String LOGIN_INPUT = "//button[normalize-space()='Log in']";
    public static final String ROUTES_MENU = "//a[contains(@href,'route-planner')]";
    public static final String NEW_ROUTE_BUTTON = "//button[contains(normalize-space(.),'New route')]";
    public static final String TRAILER_TYPE_TAB = "//*[normalize-space(text())='Trailer type only']";
    public static final String TRAILER_SELECT = "[data-cy='select-trailerType']";
    public static final String TRAILER_OPTION_VAN = "[data-cy='select-option-trailerType-van']";
    public static final String ORIGIN_INPUT = "#origin";
    /** Campo Destination del modal (id real: pickUpCity). */
    public static final String DESTINATION_CITY_INPUT = "[data-cy='destination-city-input']";
    public static final String DESTINATION_INPUT = DESTINATION_CITY_INPUT;
    public static final String DESTINATION_INPUT_FALLBACK =
            "//input[@data-cy='destination-city-input' or @id='pickUpCity' or @id='destination'"
                    + " or contains(@placeholder,'destination') or contains(@placeholder,'Destination')]";
    public static final String AUTOCOMPLETE_RESULT_ITEM = "[data-cy='autocomplete-result-item']";
    public static final String DAYS_TRIGGER = "[data-cy='day-range-trigger']";
    public static final String DEPARTURE_TRIGGER = "[data-cy='select-departure-date']";
    public static final String CONTINUE_BUTTON = "[data-cy='next-button-step-one']";
    /** Continue del modal de confirmación que a veces aparece tras Try this route. */
    public static final String CONTINUE_BUTTON_ANY =
            "//button[@data-cy='next-button-step-one' or @data-cy='next-button-step-two'"
                    + " or contains(@data-cy,'next-button') or normalize-space()='Continue']";
    public static final String CREATE_ROUTE_MODAL_TITLE =
            "//*[self::h1 or self::h2 or self::div][normalize-space()='Create route']";
    public static final String CREATE_ROUTE_MODAL_CLOSE =
            "//button[@aria-label='Close' or @aria-label='close' or contains(@class,'close')]"
                    + " | //*[@data-cy='close-modal' or @data-cy='close-button']"
                    + " | //button[.//*[local-name()='svg'] and ancestor::*[contains(.,'Create route')]][1]";
    /**
     * Secciones de rutas sugeridas, en orden de preferencia.
     * Hay que hacer clic en una tarjeta para expandirla; recién ahí aparece "Try this route".
     */
    public static final String[] ROUTE_SECTION_LABELS = {
            "Tri-hauls", "Bi-hauls", "Best Choice", "Best choice", "Direct Routes"
    };

    /**
     * Contenedor de la sección: el h2 con el título y su siguiente bloque de tarjetas.
     * El título puede ir en un span interno (Tri-hauls / Bi-hauls / Best Choice).
     */
    public static final String ROUTE_SECTION_HEADER =
            "//h2[.//span[normalize-space()='%1$s'] or normalize-space()='%1$s']";

    /**
     * Zona clickeable de una tarjeta para desplegarla (cursor-pointer dentro de data-index).
     * Índice 1-based: 1 = primera, 2 = segunda, etc.
     */
    public static final String ROUTE_CARD_CLICKABLE =
            "(" + ROUTE_SECTION_HEADER
                    + "/following-sibling::div[1]//div[@data-index][%2$d]"
                    + "//div[contains(@class,'cursor-pointer')])[1]"
                    + " | (" + ROUTE_SECTION_HEADER
                    + "/following-sibling::div[1]//div[@data-index][%2$d])[1]";

    /** Cantidad de tarjetas data-index dentro de la sección. */
    public static final String ROUTE_CARDS_IN_SECTION =
            ROUTE_SECTION_HEADER + "/following-sibling::div[1]//div[@data-index]";

    /**
     * Botón Try this route de la sección (solo aparece cuando la tarjeta ya está expandida).
     */
    public static final String TRY_THIS_ROUTE_IN_SECTION =
            "(" + ROUTE_SECTION_HEADER
                    + "/following-sibling::div[1]//button[contains(normalize-space(.),'Try this route')])[1]";

    /** Compatibilidad con el paso antiguo. */
    public static final String TRI_HAULS_SECOND_ROUTE =
            String.format(ROUTE_CARD_CLICKABLE, "Tri-hauls", 2);
    public static final String TRY_THIS_ROUTE_BUTTON =
            String.format(TRY_THIS_ROUTE_IN_SECTION, "Tri-hauls");

    public static final String ANY_TRY_THIS_ROUTE_BUTTON =
            "(//button[contains(normalize-space(.),'Try this route')])[1]";

    /**
     * Panel de mapa del detalle de ruta y botón hamburguesa que lo abre/cierra.
     *
     * <p>Mientras el mapa ocupa un tercio del ancho, la tabla de lanes solo renderiza las columnas
     * básicas (origin, status, mileage, drivingTime, income, rpm, totalCost). Al cerrarlo aparecen
     * Fuel cost, Toll cost, Custom cost, Op cost y Profit, que son las que se validan aquí.</p>
     */
    public static final String ROUTE_MAP_CONTAINER = "//*[@data-cy='route-map-container']";
    public static final String MAP_TOGGLE_BUTTON =
            "//button[@data-cy='edit-button-route-detail']/following::button[@aria-pressed][1]";

    /**
     * Tabla de lanes del detalle de ruta (pestaña Plan).
     * Estructura real: table[data-cy=lanes-table] > thead th[data-column-id] / tbody tr[data-cy=principal-table-row-{laneId}]
     * > td[data-cy=lane-table-cell-{columnId}] / tfoot tr con la fila de valores generales.
     */
    public static final String LANES_TABLE = "//table[@data-cy='lanes-table']";
    public static final String PRINCIPAL_TABLE_ROWS =
            LANES_TABLE + "/tbody/tr[starts-with(@data-cy,'principal-table-row-')]";
    public static final String PRINCIPAL_TABLE_ROW_BY_INDEX = "(" + PRINCIPAL_TABLE_ROWS + ")[%d]";

    /** Todos los encabezados, en orden, para mapear la posición de cada columna. */
    public static final String TABLE_HEADERS = LANES_TABLE + "/thead/tr/th";
    /** Celda de una fila por el data-cy que deriva del data-column-id del encabezado. */
    public static final String LANE_CELL_BY_COLUMN = ".//td[@data-cy='lane-table-cell-%s']";
    /** Celda de una fila por la posición que ocupa la columna en el thead. */
    public static final String CELL_BY_POSITION = "./td[%d]";

    /**
     * Fila con los valores generales de la ruta (la que rotula "Total"). No es un resumen por lane:
     * son los acumulados que el Backend entrega en el bloque finance de la ruta.
     */
    public static final String ROUTE_TOTALS_ROW = LANES_TABLE + "/tfoot/tr";
    public static final String ROUTE_TOTALS_ROW_BY_LABEL =
            LANES_TABLE + "//tr[td[normalize-space(.)='Total']]";

    /** Toggle de visibilidad del Op cost: icono de ojo dentro del encabezado de esa columna. */
    public static final String OP_COST_TOGGLE =
            "//th[normalize-space(.)='Op cost']//button"
                    + " | //th[contains(normalize-space(.),'Op cost')]//*[@role='button']"
                    + " | //*[contains(@data-cy,'op-cost') or contains(@data-cy,'operative-cost')][self::button]";

    private SelectorConstant() {}
}
