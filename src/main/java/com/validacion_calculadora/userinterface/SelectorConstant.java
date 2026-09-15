package com.validacion_calculadora.userinterface;

import net.serenitybdd.screenplay.targets.Target;

public class SelectorConstant {

    public static final Target MAIL_INPUT = Target.the("Campo Email address")
            .locatedBy("//input[@placeholder='Type your email']");
    public static final Target PASSWORD_INPUT = Target.the("Campo Password")
            .locatedBy("//input[@name='password']");
    public static final String LOGIN_INPUT = "//button[normalize-space()='Log in']";

    /**
     * Enlace Loadboard del sidebar. Preferimos href /loads; fallback por texto visible.
     */
    public static final String LOADBOARD_MENU =
            "//a[contains(@href,'/loads')]"
                    + " | //*[self::a or self::button or self::div][normalize-space()='Loadboard']"
                    + " | //*[contains(normalize-space(.),'Loadboard') and (self::a or self::span or self::p)]"
                    + "/ancestor::a[1]";

    /** Título / copy de la home de Loadboard. */
    public static final String LOADBOARD_HEADLINE =
            "//*[contains(normalize-space(.),'Find the perfect load')]";

    /**
     * Disparador compacto del search-bar (visible al llegar a /loads).
     * Al hacer clic se expande el formulario completo (quita la clase {@code hidden}).
     */
    public static final String LOADBOARD_SEARCH_BAR_TRIGGER =
            "//*[@data-cy='search-bar-trigger']";

    /** Campo Origin del buscador expandido ({@code data-cy=origin-input}). */
    public static final String LOADBOARD_ORIGIN_INPUT =
            "//input[@data-cy='origin-input']"
                    + " | //input[@data-testid='place-autocomplete-input' and contains(@placeholder,'origin')]"
                    + " | //input[contains(@placeholder,'Type your origin')]";

    /**
     * Placeholder clicable de Destination antes de revelar el input
     * ({@code <p>Find the best destination</p>}).
     */
    public static final String LOADBOARD_DESTINATION_PROMPT =
            "//p[contains(normalize-space(.),'Find the best destination')]"
                    + " | //*[@data-testid='search-bar-destination' or contains(@data-cy,'destination')]"
                    + "[.//p[contains(.,'destination') or contains(.,'Destination')]]";

    /** Campo Destination una vez revelado el input. */
    public static final String LOADBOARD_DESTINATION_INPUT =
            "//input[@data-cy='destination-input']"
                    + " | //input[contains(@placeholder,'Find the best destination')"
                    + " or contains(@placeholder,'destination')]"
                    + " | //*[@data-testid='search-bar-destination']//input[@data-testid='place-autocomplete-input']";

    /** Ítems del autocomplete de origen/destino (lista bajo el input). */
    public static final String LOADBOARD_AUTOCOMPLETE_ITEM =
            "//*[@data-cy='autocomplete-result-item' or contains(@data-cy,'results-item')"
                    + " or @data-testid='place-autocomplete-option']"
                    + " | //*[@role='option' or @role='listbox']//*[normalize-space() and contains(.,',')]"
                    + " | //ul[contains(@class,'absolute') or contains(@class,'z-')]//li"
                    + " | //*[contains(@class,'absolute') and (contains(@class,'z-[') or contains(@class,'z-'))]"
                    + "//*[contains(normalize-space(.),',') and string-length(normalize-space(.)) < 40]";

    /**
     * Sugerencia exacta "City, ST" en el dropdown (clic rápido al aparecer).
     */
    public static final String LOADBOARD_AUTOCOMPLETE_EXACT =
            "//*[@data-cy='autocomplete-result-item' or @role='option'"
                    + " or @data-testid='place-autocomplete-option'][normalize-space()='%s']"
                    + " | //*[normalize-space()='%s']"
                    + "[ancestor::*[contains(@class,'absolute') or @role='listbox'"
                    + " or contains(@data-cy,'autocomplete') or contains(@data-testid,'autocomplete')]]";

    /**
     * Botón de búsqueda del search-bar expandido.
     * Con origen/destino ya elegidos es {@code data-cy=search-bar-button} ("Search loads");
     * antes de llenar suele ser el círculo verde {@code #00A065}.
     */
    public static final String LOADBOARD_SEARCH_BUTTON =
            "//*[@data-cy='search-bar-button' or @data-testid='search-bar-button']"
                    + " | //button[contains(normalize-space(.),'Search loads')]"
                    + " | //*[@data-cy='search-bar' or @data-cy='search-bar-deals']"
                    + "//div[contains(@class,'rounded-full') and contains(@class,'00A065')]"
                    + " | //div[contains(@class,'rounded-full') and contains(@class,'00A065')]";

    /** Etiqueta "Results: N" tras la búsqueda. */
    public static final String LOADBOARD_RESULTS_LABEL =
            "//*[contains(normalize-space(.),'Results:')]";

    /**
     * Filas/cards de cargas en resultados. Preferir {@code tr} de tabla;
     * evitar chips genéricos del search-bar.
     */
    public static final String LOAD_RESULT_ROWS =
            "//table//tbody/tr[td and not(contains(@class,'hidden'))]"
                    + " | //*[@data-cy='load-item']"
                    + " | //*[@data-cy and (contains(@data-cy,'load-row') or contains(@data-cy,'load-card')"
                    + " or contains(@data-cy,'load-item') or @data-cy='load')]"
                    + " | //*[@role='row' and .//text()[contains(.,',')]]"
                    + " | //main//*[contains(@class,'cursor-pointer')][.//*[contains(text(),',')]"
                    + " and string-length(normalize-space(.)) > 20"
                    + " and not(ancestor::*[@data-cy='search-bar' or @data-cy='search-bar-trigger'])]";

    /**
     * Celda Rate de una carga ({@code data-cy=load-item-ratePerMile}).
     * El punto verde vive aquí (no en Connectivity).
     */
    public static final String LOAD_RATE_CELL =
            "//*[@data-cy='load-item-ratePerMile']";

    /**
     * Círculo / badge {@code $} dentro de la celda Rate (preferir sobre selectores globales).
     */
    public static final String LOAD_GREEN_RATE_DOT =
            "//*[@data-cy='load-item-ratePerMile']//*[normalize-space()='$']"
                    + " | //*[@data-cy='load-item-ratePerMile']"
                    + "//*[contains(@class,'rounded-full') and ("
                    + "contains(@class,'00A065') or contains(@class,'00C980')"
                    + " or contains(@class,'02A168') or contains(@class,'00a065')"
                    + " or contains(@class,'00c980'))]";

    /** Filas load-item con badge {@code $} en Rate. */
    public static final String LOAD_ROWS_WITH_GREEN_RATE =
            "//*[@data-cy='load-item'][.//*[@data-cy='load-item-ratePerMile']"
                    + "[.//*[normalize-space()='$'] or .//*[contains(@class,'rounded-full')"
                    + " and (contains(@class,'00A065') or contains(@class,'00C980')"
                    + " or contains(@class,'02A168'))]]]";

    /** Modal / panel Load details. */
    public static final String LOAD_DETAILS_TITLE =
            "//*[normalize-space()='Load details' or contains(normalize-space(.),'Load details')]";

    /**
     * Contenedor del panel Load details (ancestro del título con el botón X).
     */
    public static final String LOAD_DETAILS_PANEL =
            "//*[normalize-space()='Load details']/ancestor::*[.//button or .//*[@role='button']][1]"
                    + " | //*[@role='dialog'][.//*[normalize-space()='Load details']]"
                    + " | //*[contains(@class,'fixed') or contains(@class,'drawer') or @data-state='open']"
                    + "[.//*[normalize-space()='Load details']]";

    /** Botón Calculate operation en el footer del modal de detalle. */
    public static final String CALCULATE_OPERATION_BUTTON =
            "//button[contains(normalize-space(.),'Calculate operation')"
                    + " or contains(normalize-space(.),'Calculate Operation')]"
                    + " | //*[self::a or self::span][contains(normalize-space(.),'Calculate operation')]";

    /** Modal Calculate profit. */
    public static final String CALCULATE_PROFIT_TITLE =
            "//*[contains(normalize-space(.),'Calculate profit')]";

    /** Bloque / texto de Current profit. */
    public static final String CURRENT_PROFIT_LABEL =
            "//*[contains(normalize-space(.),'Current profit')]";

    /**
     * Botón X para cerrar Load details / Calculate profit (esquina superior derecha del panel).
     * Cubre button, div/role=button y SVG clicable.
     */
    public static final String MODAL_CLOSE_BUTTON =
            "//*[@role='dialog' or @data-state='open']"
                    + "//button[@aria-label='Close' or @aria-label='close' or contains(@aria-label,'Close')]"
                    + " | //*[normalize-space()='Load details']/ancestor::*[position()<=6]"
                    + "//button[@aria-label='Close' or @aria-label='close' or contains(@aria-label,'Close')"
                    + " or contains(@class,'close')]"
                    + " | //*[normalize-space()='Load details']/ancestor::*[position()<=6]"
                    + "//*[@role='button'][@aria-label='Close' or @aria-label='close']"
                    + " | //*[normalize-space()='Load details']/ancestor::*[position()<=4]"
                    + "//button[.//*[local-name()='svg']][not(contains(normalize-space(.),'Book'))]"
                    + "[not(contains(normalize-space(.),'Calculate'))][last()]"
                    + " | //*[normalize-space()='Calculate profit']/ancestor::*[position()<=6]"
                    + "//button[@aria-label='Close' or @aria-label='close' or contains(@aria-label,'Close')]"
                    + " | //button[normalize-space()='×' or normalize-space()='✕' or normalize-space()='x']";

    /** Botón Go back del modal de calculadora (vuelve a Load details). */
    public static final String CALCULATE_GO_BACK =
            "//button[contains(normalize-space(.),'Go back')]"
                    + " | //*[self::a or self::span][contains(normalize-space(.),'Go back')]"
                    + "/ancestor::button[1]";

    private SelectorConstant() {}
}
