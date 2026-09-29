package com.filtros_de_rutas.steps;

import com.filtros_de_rutas.interactions.ValidateFiltersOnRoutes;
import com.filtros_de_rutas.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class FiltersValidation {

    private static final Setup setup = new Setup();

    @Step("{0} aplica cada filtro y verifica que los totales del pie cambian")
    public void validaFiltros(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                ValidateFiltersOnRoutes.untilFooterChanges()
        );
    }
}
