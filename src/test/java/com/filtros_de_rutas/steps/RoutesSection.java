package com.filtros_de_rutas.steps;

import com.filtros_de_rutas.interactions.Pause;
import com.filtros_de_rutas.question.RoutesListVisible;
import com.filtros_de_rutas.stepdefinitions.Setup;
import com.filtros_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static org.hamcrest.Matchers.is;

public class RoutesSection {

    private static final Setup setup = new Setup();

    @Step("{0} visualiza el listado de rutas")
    public void visualizaListado(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                WaitUntil.the(Target.the("Botón New route")
                                .locatedBy(SelectorConstant.NEW_ROUTE_BUTTON), isVisible())
                        .forNoMoreThan(40).seconds(),
                Pause.forSeconds(2)
        );
        theActorInTheSpotlight().should(
                seeThat("El listado de rutas está visible", RoutesListVisible.isShown(), is(true))
        );
    }
}
