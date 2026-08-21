package com.validacion_calculadora.steps;

import com.validacion_calculadora.interactions.SearchLoadsUntilGreenRate;
import com.validacion_calculadora.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class LoadSearch {

    private static final Setup setup = new Setup();

    @Step("{0} busca cargas, espera 15 s a que carguen y busca punto verde en Rate")
    public void buscaCargasConCiudadesVariables(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SearchLoadsUntilGreenRate.withRandomCities()
        );
    }
}
