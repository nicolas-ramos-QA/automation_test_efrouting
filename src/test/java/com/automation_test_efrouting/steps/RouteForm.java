package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.CreateRouteForm;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class RouteForm {

    private static final Setup setup = new Setup();

    @Step("{0} completa los datos de la nueva ruta")
    public void completaDatosRuta(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            CreateRouteForm.routeDataFilled()
        );
    }
}
