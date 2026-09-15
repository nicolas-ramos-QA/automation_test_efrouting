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

public class SelectRoutes implements Interaction {

    public < U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
            Pause.forSeconds(3),
            WaitUntil.the(Target.the(JsonTextSelector.ROUTES_INPUT_NAME)
                    .located(By.xpath(SelectorConstant.ROUTES_MENU)),isVisible())
                    .forNoMoreThan(30).seconds(),
            Click.on(SelectorConstant.ROUTES_MENU)
        );
    }

    public static SelectRoutes routesSectionSelected() {
        return new SelectRoutes();
    }
}
