package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.SelectTrailerType;
import com.calculos_de_rutas.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class TrailerType {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona la opción Trailer type only")
    public void seleccionaTrailerType(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectTrailerType.trailerTypeSelected()
        );
    }
}
