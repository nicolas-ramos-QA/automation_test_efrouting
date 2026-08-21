package com.validacion_calculadora.steps;

import com.validacion_calculadora.interactions.SelectLoadboard;
import com.validacion_calculadora.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class LoadboardNavigation {

    private static final Setup setup = new Setup();

    @Step("{0} se dirige a la sección Loadboard")
    public void seDirigeLoadboard(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectLoadboard.loadboardSectionSelected()
        );
    }
}
