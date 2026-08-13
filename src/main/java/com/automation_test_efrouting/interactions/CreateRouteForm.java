package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class CreateRouteForm implements Interaction {

    public < U extends Actor> void performAs(U actor) {
        String daysOption = "//div[@data-cy='day-range-options-container']//button[normalize-space()='"
                + JsonTextSelector.DAYS_ON_ROUTE_VALUE + "']";

        actor.attemptsTo(
            WaitUntil.the(Target.the("Selector de tráiler").locatedBy(SelectorConstant.TRAILER_SELECT), isVisible())
                    .forNoMoreThan(20).seconds(),
            Click.on(SelectorConstant.TRAILER_SELECT),
            Click.on(SelectorConstant.TRAILER_OPTION_VAN),
            Enter.theValue(JsonTextSelector.ORIGIN_VALUE).into(SelectorConstant.ORIGIN_INPUT),
            WaitUntil.the(Target.the("Sugerencias de origen").locatedBy(SelectorConstant.AUTOCOMPLETE_RESULT_ITEM), isVisible())
                    .forNoMoreThan(15).seconds(),
            Click.on(SelectorConstant.AUTOCOMPLETE_RESULT_ITEM),
            Pause.forSeconds(1),
            JsClick.on(SelectorConstant.DEPARTURE_TRIGGER),
            Pause.forSeconds(1),
            SelectDepartureDate.futureDate(),
            Pause.forSeconds(1),
            JsClick.on(SelectorConstant.DAYS_TRIGGER),
            Pause.forSeconds(1),
            JsClick.on(daysOption)
        );
    }

    public static CreateRouteForm routeDataFilled() {
        return new CreateRouteForm();
    }
}
