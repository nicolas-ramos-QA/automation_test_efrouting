package com.validacion_calculadora.interactions;

import com.validacion_calculadora.userinterface.SelectorConstant;
import com.validacion_calculadora.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class SelectSubmitLogin implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        Target loginButton = Target.the(JsonTextSelector.LOGIN_INPUT_NAME)
                .located(By.xpath(SelectorConstant.LOGIN_INPUT));
        actor.attemptsTo(
                Scroll.to(loginButton),
                WaitUntil.the(loginButton, isVisible()).forNoMoreThan(10).seconds(),
                Click.on(SelectorConstant.LOGIN_INPUT)
        );
    }

    public static SelectSubmitLogin sendButtonSelected() {
        return new SelectSubmitLogin();
    }
}
