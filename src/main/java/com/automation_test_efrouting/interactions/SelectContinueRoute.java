package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class SelectContinueRoute implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                WaitUntil.the(Target.the(JsonTextSelector.CONTINUE_INPUT_NAME)
                                .locatedBy(SelectorConstant.CONTINUE_BUTTON), isVisible())
                        .forNoMoreThan(20).seconds(),
                JsClick.on(SelectorConstant.CONTINUE_BUTTON),
                Pause.forSeconds(10)
        );
    }

    public static SelectContinueRoute continueSelected() {
        return new SelectContinueRoute();
    }
}
