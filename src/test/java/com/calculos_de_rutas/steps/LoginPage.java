package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.OpenLoginPage;
import com.calculos_de_rutas.models.TestEnvironment;
import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.utils.Environments;
import com.calculos_de_rutas.utils.FinancialReport;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class LoginPage {

    /** Clave con la que el Actor recuerda el ambiente durante todo el escenario. */
    public static final String REMEMBER_ENVIRONMENT = "ambiente";

    private static final Setup setup = new Setup();

    @Step("{0} ingresa al sitio web del ambiente {1}")
    public void abrirPortal(String actorName, String environmentName) {
        setup.setupActor(actorName);

        TestEnvironment environment = Environments.byName(environmentName);
        theActorInTheSpotlight().remember(REMEMBER_ENVIRONMENT, environment);

        FinancialReport.useSlug(environment.getSlug());
        FinancialReport.addContext("Ambiente", environment.getName());
        FinancialReport.addContext("URL de login", environment.getUrl());
        FinancialReport.addContext("Usuario", environment.getEmail());

        theActorInTheSpotlight().attemptsTo(OpenLoginPage.of(environment));
    }
}
