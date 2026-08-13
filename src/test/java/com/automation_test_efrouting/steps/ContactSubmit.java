package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectSubmitLogin;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactSubmit{
    private static final Setup setup = new Setup();

    @Step("{0} selecciona botón de envío")
    public void seleccionaBoton(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            SelectSubmitLogin.sendButtonSelected()
        );
    }
}