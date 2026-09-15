package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Clear;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class EditLastLane implements Interaction {

    public < U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
            WaitUntil.the(Target.the("Detalle de ruta").locatedBy("//*[normalize-space(text())='Report']"), isVisible())
                    .forNoMoreThan(45).seconds(),
            Pause.forSeconds(4),
            JsClick.on(SelectorConstant.LANE_OPTIONS_BUTTON_LAST),
            WaitUntil.the(Target.the("Opción Edit lane").locatedBy(SelectorConstant.EDIT_LANE_OPTION), isVisible())
                    .forNoMoreThan(15).seconds(),
            JsClick.on(SelectorConstant.EDIT_LANE_OPTION),
            WaitUntil.the(Target.the("Campo Origin del modal").locatedBy(SelectorConstant.EDIT_ORIGIN_INPUT), isVisible())
                    .forNoMoreThan(15).seconds(),
            Clear.field(Target.the("Campo Origin del modal").locatedBy(SelectorConstant.EDIT_ORIGIN_INPUT)),
            Enter.theValue(JsonTextSelector.EDIT_ORIGIN_TYPE).into(SelectorConstant.EDIT_ORIGIN_INPUT),
            WaitUntil.the(Target.the("Sugerencia de origen").locatedBy(SelectorConstant.EDIT_ORIGIN_FIRST_SUGGESTION), isVisible())
                    .forNoMoreThan(15).seconds(),
            JsClick.on(SelectorConstant.EDIT_ORIGIN_FIRST_SUGGESTION),
            Pause.forSeconds(1),
            SetReactInput.on(SelectorConstant.EDIT_START_TIME, JsonTextSelector.EDIT_TIME_VALUE),
            SetReactInput.on(SelectorConstant.EDIT_END_TIME, JsonTextSelector.EDIT_TIME_VALUE),
            Pause.forSeconds(1),
            JsClick.on(SelectorConstant.EDIT_SAVE_BUTTON),
            Pause.forSeconds(6)
        );
    }

    public static EditLastLane lastLaneEdited() {
        return new EditLastLane();
    }
}
