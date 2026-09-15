package com.automation_test_efrouting.stepdefinitions;

import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

public class Setup {

    public void setupActor(String actorName) {
        OnStage.setTheStage(new OnlineCast());
        OnStage.theActorCalled(actorName);
    }
}
