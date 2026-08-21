package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.question.SuccessMessageRender;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class SuccessMessage {

    private static final Setup setup = new Setup();
    private static Actor actor = theActorInTheSpotlight();

    @Step("{0} visualiza Texto Home")
    public static  void visualizaMensajeExito(String actorName) {
        OnStage.theActorInTheSpotlight().should(
                SuccessMessageRender.successMessageRendered().answeredBy(actor)
        );
    }

}
