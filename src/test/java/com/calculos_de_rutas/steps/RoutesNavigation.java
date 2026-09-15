package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.SelectRoutes;
import com.calculos_de_rutas.stepdefinitions.Setup;
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
