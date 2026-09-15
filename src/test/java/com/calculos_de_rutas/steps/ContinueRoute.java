package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.SelectContinueRoute;
import com.calculos_de_rutas.stepdefinitions.Setup;
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
