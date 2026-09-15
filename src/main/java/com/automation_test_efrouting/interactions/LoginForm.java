package com.automation_test_efrouting.interactions;

import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.waits.WaitUntil;

import java.nio.file.Path;
import java.nio.file.Paths;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class LoginForm implements Interaction {

    Path pdfPath = Paths.get(JsonTextSelector.PATH_FILE);

    @Override
    public < U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
            WaitUntil.the(SelectorConstant.MAIL_INPUT, isVisible()).forNoMoreThan(20).seconds(),
            Enter.theValue(JsonTextSelector.EMAIL_VALUE).into(SelectorConstant.MAIL_INPUT),
            Enter.theValue(JsonTextSelector.PASSWORD_VALUE).into(SelectorConstant.PASSWORD_INPUT)
        );
    }

    public static LoginForm dataLlenarFormulario() {
        return new LoginForm();
    }
}
