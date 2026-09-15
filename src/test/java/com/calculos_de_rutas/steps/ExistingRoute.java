package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.OpenExistingRoute;
import com.calculos_de_rutas.models.ExistingRouteTarget;
import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import com.calculos_de_rutas.utils.FinancialReport;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class ExistingRoute {

    private static final Setup setup = new Setup();

    @Step("{0} inicia sesión para validar una ruta ya existente")
    public void iniciaSesion(String actorName) {
        ExistingRouteTarget target = ExistingRouteTarget.fromSystem();
        LoginPage loginPage = new LoginPage();
        loginPage.abrirPortal(actorName, target.getEnvironmentName());

        new ContactForm().ingresarData(actorName, target.getEnvironmentName());
        new ContactSubmit().seleccionaBoton(actorName);

        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(
                WaitUntil.the(Target.the("Menú Routes")
                                .locatedBy(SelectorConstant.ROUTES_MENU), isVisible())
                        .forNoMoreThan(45).seconds()
        );
        actor.remember("existingRouteTarget", target);

        FinancialReport.appendSlug(target.reportSuffix());
        FinancialReport.addContext("Modo", "Ruta existente (no se crea una nueva)");
        FinancialReport.addContext("Ruta pedida", target.getRouteId());
        FinancialReport.addContext("URL de la ruta", target.getDetailUrl());
    }

    @Step("{0} abre la ruta existente indicada")
    public void abrir(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();
        ExistingRouteTarget target = actor.recall("existingRouteTarget");
        if (target == null) {
            target = ExistingRouteTarget.fromSystem();
            actor.remember("existingRouteTarget", target);
        }
        actor.attemptsTo(OpenExistingRoute.of(target));
    }
}
