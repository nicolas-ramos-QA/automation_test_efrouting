package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectRoutes;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class RoutesNavigation {

    private static final Setup setup = new Setup();

    @Step("{0} se dirige a la sección de Routes")
    public void seDirigeRoutes(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            SelectRoutes.routesSectionSelected()
        );
    }
}
