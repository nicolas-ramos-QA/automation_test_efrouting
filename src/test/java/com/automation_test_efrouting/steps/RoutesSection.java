package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.question.RoutesSectionRender;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class RoutesSection {

    private static final Setup setup = new Setup();
    private static Actor actor = theActorInTheSpotlight();

    @Step("{0} visualiza la sección de rutas")
    public static  void visualizaSeccionRutas(String actorName) {
        OnStage.theActorInTheSpotlight().should(
                RoutesSectionRender.routesSectionRendered().answeredBy(actor)
        );
    }

}
