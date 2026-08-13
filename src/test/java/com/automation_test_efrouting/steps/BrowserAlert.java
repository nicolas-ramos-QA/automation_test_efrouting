package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.AlertAccept;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class BrowserAlert {

    private static final Setup setup = new Setup();

    @Step("{0} Redirige a módulo de my loads")
    public void redirigeModuloMyLoads(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            AlertAccept.buttonSelected()
        );
    }
}
