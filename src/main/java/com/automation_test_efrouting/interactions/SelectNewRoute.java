package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class SelectNewRoute implements Interaction {

    public < U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
            WaitUntil.the(Target.the(JsonTextSelector.NEW_ROUTE_INPUT_NAME)
                    .located(By.xpath(SelectorConstant.NEW_ROUTE_BUTTON)),isVisible())
                    .forNoMoreThan(30).seconds(),
            Click.on(SelectorConstant.NEW_ROUTE_BUTTON)
        );
    }

    public static SelectNewRoute newRouteSelected() {
        return new SelectNewRoute();
    }
}
