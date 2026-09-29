package com.filtros_de_rutas.interactions;

import com.filtros_de_rutas.models.TestEnvironment;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Open;

public class OpenLoginPage implements Interaction {

    private final TestEnvironment environment;

    public OpenLoginPage(TestEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                Open.url(environment.getUrl()),
                CenterWindow.centered(),
                Pause.forSeconds(2)
        );
    }

    public static OpenLoginPage of(TestEnvironment environment) {
        return new OpenLoginPage(environment);
    }
}
