package com.automation_test_efrouting.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.Alert;

public class AlertAccept implements Interaction {
    @Override
    public < U extends Actor> void performAs(U actor) {
        Alert alert = BrowseTheWeb.as(actor).getDriver().switchTo().alert();
        alert.accept();
    }

    public static AlertAccept buttonSelected() {
        return new AlertAccept();
    }
}
