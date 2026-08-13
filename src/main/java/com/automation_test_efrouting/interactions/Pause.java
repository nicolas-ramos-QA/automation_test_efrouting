package com.automation_test_efrouting.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;

public class Pause implements Interaction {

    private final int seconds;

    public Pause(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public < U extends Actor> void performAs(U actor) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Pause forSeconds(int seconds) {
        return new Pause(seconds);
    }
}
