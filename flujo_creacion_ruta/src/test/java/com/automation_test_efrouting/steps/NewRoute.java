package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectNewRoute;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class NewRoute {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona el botón New route")
    public void seleccionaNewRoute(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            SelectNewRoute.newRouteSelected()
        );
    }
}
