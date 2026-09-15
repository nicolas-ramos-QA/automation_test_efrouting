package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.CreateRouteForm;
import com.calculos_de_rutas.stepdefinitions.Setup;
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
