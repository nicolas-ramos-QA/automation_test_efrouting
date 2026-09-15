package com.validacion_calculadora.steps;

import com.validacion_calculadora.interactions.LoginForm;
import com.validacion_calculadora.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactForm {

    private static final Setup setup = new Setup();

    @Step("{0} ingresa las credenciales de acceso")
    public void ingresarData(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                LoginForm.dataLlenarFormulario()
        );
    }
}
