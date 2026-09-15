package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.SelectNewRoute;
import com.calculos_de_rutas.stepdefinitions.Setup;
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
