package com.filtros_de_rutas.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;

/**
 * Maximiza para que el pie de totales y el botón Filter queden visibles.
 */
public class CenterWindow implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.manage().window().maximize();
    }

    public static CenterWindow centered() {
        return new CenterWindow();
    }
}
