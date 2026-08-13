package com.calculos_de_rutas.stepdefinitions;

import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

public class Setup {

    private static volatile boolean initialized = false;

    public synchronized void setupActor(String actorName) {
        if (!initialized) {
            OnStage.setTheStage(new OnlineCast());
            OnStage.theActorCalled(actorName);
            initialized = true;
            return;
        }
        try {
            OnStage.theActorInTheSpotlight();
        } catch (Throwable t) {
            OnStage.theActorCalled(actorName);
        }
    }

    public static synchronized void reset() {
        initialized = false;
    }
}
