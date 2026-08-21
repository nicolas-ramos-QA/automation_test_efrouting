package com.validacion_calculadora.steps;

import com.validacion_calculadora.question.LoadboardVisible;
import com.validacion_calculadora.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.hamcrest.Matchers.is;

public class LoadboardSection {

    private static final Setup setup = new Setup();

    @Step("{0} debería visualizar la pantalla de Loadboard")
    public void visualizaLoadboard(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().should(
                seeThat("La pantalla de Loadboard está visible", LoadboardVisible.isShown(), is(true))
        );
    }
}
