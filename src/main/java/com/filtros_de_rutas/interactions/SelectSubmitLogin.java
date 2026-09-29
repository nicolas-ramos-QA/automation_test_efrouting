package com.filtros_de_rutas.interactions;

import com.filtros_de_rutas.userinterface.SelectorConstant;
import com.filtros_de_rutas.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class SelectSubmitLogin implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                WaitUntil.the(Target.the(JsonTextSelector.LOGIN_INPUT_NAME)
                                .located(By.xpath(SelectorConstant.LOGIN_INPUT)), isVisible())
                        .forNoMoreThan(15).seconds(),
                JsClick.on(SelectorConstant.LOGIN_INPUT)
        );
    }

    public static SelectSubmitLogin sendButtonSelected() {
        return new SelectSubmitLogin();
    }
}
