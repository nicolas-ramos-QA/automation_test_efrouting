package com.filtros_de_rutas.steps;

import com.filtros_de_rutas.interactions.LoginForm;
import com.filtros_de_rutas.stepdefinitions.Setup;
import com.filtros_de_rutas.utils.Environments;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactForm {

    private static final Setup setup = new Setup();

    @Step("{0} ingresa la data en el formulario de login")
    public void ingresarData(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                LoginForm.withCredentialsOf(Environments.resolved())
        );
    }
}
