package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.Pause;
import com.automation_test_efrouting.question.LaneMileage;
import com.automation_test_efrouting.stepdefinitions.Setup;
import com.automation_test_efrouting.userinterface.SelectorConstant;
import com.automation_test_efrouting.utils.GeoDistance;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class RouteVerification {

    private static final Setup setup = new Setup();

    private static final double LOWER_FACTOR = 0.85;
    private static final double UPPER_FACTOR = 1.40;

    @Step("{0} verifica que las millas de la ruta sean correctas")
    public void verificaMillas(String actorName) {
        setup.setupActor(actorName);
        Actor actor = theActorInTheSpotlight();

        String dhXpath = String.format(SelectorConstant.LANE_MILEAGE_TEMPLATE, "Effingham, IL", "Tulsa, OK");

        actor.attemptsTo(
            Pause.forSeconds(2),
            WaitUntil.the(Target.the("Millas del tramo Deadhead Effingham->Tulsa").locatedBy(dhXpath), isVisible())
                    .forNoMoreThan(30).seconds()
        );

        verificaTramo(actor, "Effingham, IL", "Tulsa, OK");
        verificaTramo(actor, "Tulsa, OK", "Chicago, IL");
    }

    private void verificaTramo(Actor actor, String origin, String destination) {
        int displayed = LaneMileage.between(origin, destination).answeredBy(actor);
        double reference = GeoDistance.greatCircleMiles(origin, destination);
        int lower = (int) Math.floor(reference * LOWER_FACTOR);
        int upper = (int) Math.ceil(reference * UPPER_FACTOR);

        Serenity.recordReportData()
                .withTitle("Verificación de millas: " + origin + " -> " + destination)
                .andContents(String.format(
                        "Millas mostradas por la app: %d mi%n"
                                + "Distancia de referencia (gran círculo): %.1f mi%n"
                                + "Rango aceptado (factor carretera): [%d, %d] mi",
                        displayed, reference, lower, upper));

        actor.should(
            seeThat("Las millas mostradas de " + origin + " a " + destination
                            + " (" + displayed + " mi) coinciden con la distancia real esperada",
                    LaneMileage.between(origin, destination),
                    is(both(greaterThanOrEqualTo(lower)).and(lessThanOrEqualTo(upper))))
        );
    }
}
