package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.userinterface.SelectorConstant;
import com.calculos_de_rutas.utils.JsonTextSelector;
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
        actor.attemptsTo(
                Scroll.to(Target.the(JsonTextSelector.LOGIN_INPUT_NAME).located(By.xpath(SelectorConstant.LOGIN_INPUT))),
                WaitUntil.the(Target.the(JsonTextSelector.LOGIN_INPUT_NAME)
                                .located(By.xpath(SelectorConstant.LOGIN_INPUT)), isVisible())
                        .forNoMoreThan(10).seconds(),
                Click.on(SelectorConstant.LOGIN_INPUT)
        );
    }

    public static SelectSubmitLogin sendButtonSelected() {
        return new SelectSubmitLogin();
    }
}
