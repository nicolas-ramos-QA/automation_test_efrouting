package com.validacion_calculadora.steps;

import com.validacion_calculadora.interactions.SelectSubmitLogin;
import com.validacion_calculadora.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactSubmit {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona el botón Log in")
    public void seleccionaBoton(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectSubmitLogin.sendButtonSelected()
        );
    }
}
