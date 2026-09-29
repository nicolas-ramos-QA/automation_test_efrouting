package com.filtros_de_rutas.steps;

import com.filtros_de_rutas.interactions.OpenLoginPage;
import com.filtros_de_rutas.models.TestEnvironment;
import com.filtros_de_rutas.stepdefinitions.Setup;
import com.filtros_de_rutas.utils.Environments;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class LoginPage {

    private static final Setup setup = new Setup();

    @Step("{0} ingresa al sitio web")
    public void abrirPortal(String actorName) {
        setup.setupActor(actorName);
        TestEnvironment environment = Environments.resolved();
        theActorInTheSpotlight().attemptsTo(OpenLoginPage.of(environment));
    }
}
