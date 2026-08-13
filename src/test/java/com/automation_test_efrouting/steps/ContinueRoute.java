package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectContinueRoute;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContinueRoute {

    private static final Setup setup = new Setup();

    @Step("{0} finaliza la creación seleccionando Continue")
    public void seleccionaContinue(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            SelectContinueRoute.continueSelected()
        );
    }
}
