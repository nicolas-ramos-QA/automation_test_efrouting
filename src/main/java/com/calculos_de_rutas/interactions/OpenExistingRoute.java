package com.calculos_de_rutas.interactions;

import com.calculos_de_rutas.abilities.CaptureUserRouteApi;
import com.calculos_de_rutas.models.ExistingRouteTarget;
import com.calculos_de_rutas.userinterface.SelectorConstant;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Abre el detalle de una ruta ya creada e instala la captura de {@code user-route}
 * antes (y después) de navegar, para no perder el JSON financiero.
 */
public class OpenExistingRoute implements Interaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenExistingRoute.class);

    private final ExistingRouteTarget target;

    public OpenExistingRoute(ExistingRouteTarget target) {
        this.target = target;
    }

    @Override
    public <U extends Actor> void performAs(U actor) {
        CaptureUserRouteApi.persistOn(actor);
        LOGGER.info("Abriendo ruta existente {} → {}", target.getRouteId(), target.getDetailUrl());

        actor.attemptsTo(
                Open.url(target.getDetailUrl()),
                CenterWindow.centered(),
                Pause.forSeconds(2)
        );

        esperarDetalle(BrowseTheWeb.as(actor).getDriver());
        CaptureUserRouteApi capture = CaptureUserRouteApi.as(actor);
        capture.install();
        actor.attemptsTo(Pause.forSeconds(2));
        if (capture.allCaptures().isEmpty()) {
            LOGGER.info("Sin user-route aún; se recarga el detalle (el hook CDP debe recapturarlo).");
            BrowseTheWeb.as(actor).getDriver().navigate().refresh();
            esperarDetalle(BrowseTheWeb.as(actor).getDriver());
            capture.install();
        }

        actor.attemptsTo(
                WaitUntil.the(Target.the("Tabla de lanes")
                                .locatedBy(SelectorConstant.LANES_TABLE), isVisible())
                        .forNoMoreThan(120).seconds(),
                Pause.forSeconds(2)
        );
    }

    private void esperarDetalle(WebDriver driver) {
        long deadline = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < deadline) {
            String url = driver.getCurrentUrl();
            if (url != null && url.contains("/route-planner/detail/" + target.getRouteId())) {
                return;
            }
            if (url != null && url.contains("/login")) {
                throw new AssertionError(
                        "Al abrir la ruta existente el navegador volvió a login. "
                                + "Revise credenciales de " + target.getEnvironmentName()
                                + ". URL: " + url);
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new AssertionError(
                "No se llegó al detalle de la ruta " + target.getRouteId()
                        + ". Se esperaba " + target.getDetailUrl()
                        + " y la URL actual es " + driver.getCurrentUrl());
    }

    public static OpenExistingRoute of(ExistingRouteTarget target) {
        return new OpenExistingRoute(target);
    }
}
