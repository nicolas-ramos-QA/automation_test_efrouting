package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectTrailerType;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class TrailerType {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona la opción Trailer type only")
    public void seleccionaTrailerType(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            SelectTrailerType.trailerTypeSelected()
        );
    }
}
