package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class SelectTriHaulRoute implements Interaction {

    public < U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
            WaitUntil.the(Target.the("Sección Tri-hauls").locatedBy("//*[normalize-space(text())='Tri-hauls']"), isVisible())
                    .forNoMoreThan(40).seconds(),
            Pause.forSeconds(2),
            WaitUntil.the(Target.the("Segunda ruta Tri-hauls").locatedBy(SelectorConstant.TRI_HAULS_SECOND_ROUTE), isVisible())
                    .forNoMoreThan(20).seconds(),
            JsClick.on(SelectorConstant.TRI_HAULS_SECOND_ROUTE),
            Pause.forSeconds(2),
            WaitUntil.the(Target.the("Botón Try this route").locatedBy(SelectorConstant.TRY_THIS_ROUTE_BUTTON), isVisible())
                    .forNoMoreThan(20).seconds(),
            JsClick.on(SelectorConstant.TRY_THIS_ROUTE_BUTTON),
            Pause.forSeconds(10)
        );
    }

    public static SelectTriHaulRoute secondTriHaulRouteSelected() {
        return new SelectTriHaulRoute();
    }
}
