package com.filtros_de_rutas.steps;

import com.filtros_de_rutas.interactions.SelectSubmitLogin;
import com.filtros_de_rutas.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactSubmit {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona botón de envío")
    public void seleccionaBoton(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectSubmitLogin.sendButtonSelected()
        );
    }
}
