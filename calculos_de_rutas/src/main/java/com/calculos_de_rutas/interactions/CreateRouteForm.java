package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.userinterface.SelectorConstant;
import com.calculos_de_rutas.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class CreateRouteForm implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        String daysOption = "//div[@data-cy='day-range-options-container']//button[normalize-space()='"
                + JsonTextSelector.DAYS_ON_ROUTE_VALUE + "']";

        actor.attemptsTo(
                WaitUntil.the(Target.the("Selector de tráiler").locatedBy(SelectorConstant.TRAILER_SELECT), isVisible())
                        .forNoMoreThan(20).seconds(),
                Click.on(SelectorConstant.TRAILER_SELECT),
                Click.on(SelectorConstant.TRAILER_OPTION_VAN),
                EnterOriginCity.named(JsonTextSelector.ORIGIN_VALUE),
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
