package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.LoginForm;
import com.calculos_de_rutas.models.TestEnvironment;
import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.utils.Environments;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactForm {

    private static final Setup setup = new Setup();

    @Step("{0} ingresa las credenciales del ambiente {1}")
    public void ingresarData(String actorName, String environmentName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();

        TestEnvironment environment = actor.recall(LoginPage.REMEMBER_ENVIRONMENT);
        if (environment == null) {
            environment = Environments.byName(environmentName);
            actor.remember(LoginPage.REMEMBER_ENVIRONMENT, environment);
        }

        actor.attemptsTo(LoginForm.withCredentialsOf(environment));
    }
}
