package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.models.TestEnvironment;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class LoginForm implements Interaction {

    private final TestEnvironment environment;

    public LoginForm(TestEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                WaitUntil.the(SelectorConstant.MAIL_INPUT, isVisible()).forNoMoreThan(30).seconds(),
                Enter.theValue(environment.getEmail()).into(SelectorConstant.MAIL_INPUT),
                Enter.theValue(environment.getPassword()).into(SelectorConstant.PASSWORD_INPUT)
        );
    }

    public static LoginForm withCredentialsOf(TestEnvironment environment) {
        return new LoginForm(environment);
    }
}
