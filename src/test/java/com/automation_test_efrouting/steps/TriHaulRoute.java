package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectTriHaulRoute;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class TriHaulRoute {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona la segunda ruta de Tri-hauls")
    public void seleccionaSegundaRutaTriHaul(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
            SelectTriHaulRoute.secondTriHaulRouteSelected()
        );
    }
}
