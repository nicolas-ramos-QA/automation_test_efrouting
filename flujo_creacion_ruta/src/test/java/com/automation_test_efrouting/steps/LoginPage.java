package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.CenterWindow;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.actions.Open;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class LoginPage {

    private static final Setup setup = new Setup();

    @Step("{0} ingresa al sitio web")
    public void abrirPortal(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            Open.browserOn().thePageNamed("pages.exercise"),
            CenterWindow.centered()
        );
    }
}
