package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.EditLastLane;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class EditLane {

    private static final Setup setup = new Setup();

    @Step("{0} edita la última línea de la ruta")
    public void editaUltimaLinea(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            EditLastLane.lastLaneEdited()
        );
    }
}
