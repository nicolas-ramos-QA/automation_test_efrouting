package com.filtros_de_rutas.interactions;

import com.filtros_de_rutas.userinterface.SelectorConstant;
import com.filtros_de_rutas.utils.JsonTextSelector;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Open;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Abre el listado de rutas de QA: https://efdata-qa.efrouting.com/route-planner
 */
public class SelectRoutes implements Interaction {

    @Override
    public <U extends Actor> void performAs(U actor) {
        actor.attemptsTo(
                Open.url(JsonTextSelector.ROUTE_PLANNER_URL),
                CenterWindow.centered(),
                Pause.forSeconds(2)
        );

        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        long deadline = System.currentTimeMillis() + 45_000L;
        while (System.currentTimeMillis() < deadline) {
            String url = driver.getCurrentUrl();
            boolean onPlanner = url != null
                    && url.contains("efdata-qa.efrouting.com/route-planner")
                    && !url.contains("/detail/");
            boolean hasNewRoute = !driver.findElements(By.xpath(SelectorConstant.NEW_ROUTE_BUTTON)).isEmpty();
            boolean hasFooter = !driver.findElements(By.xpath(SelectorConstant.FOOTER_TOTAL_ROUTES)).isEmpty();
            if (onPlanner && (hasNewRoute || hasFooter)) {
                actor.attemptsTo(Pause.forSeconds(2));
                return;
            }
            if (url != null && url.contains("/login")) {
                throw new AssertionError(
                        "Al abrir el listado de rutas QA el navegador volvió a login. URL: " + url);
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new AssertionError(
                "No se estabilizó el listado QA. Se esperaba "
                        + JsonTextSelector.ROUTE_PLANNER_URL
                        + " y la URL actual es " + driver.getCurrentUrl());
    }

    public static SelectRoutes routesSectionSelected() {
        return new SelectRoutes();
    }
}
