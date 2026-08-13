package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.SelectTriHaulRoute;
import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.utils.FinancialReport;
import net.serenitybdd.annotations.Step;

import java.util.Locale;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class TriHaulRoute {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona una ruta sugerida (Tri-hauls → Bi-hauls → Best Choice) y Try this route")
    public void seleccionaSegundaRutaTriHaul(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectTriHaulRoute.secondTriHaulRouteSelected()
        );
    }

    /**
     * Fuerza el tipo de ruta sugerida (Tri-hauls, Bi-hauls o Best Choice) al crear la ruta.
     * El reporte de cálculos de este escenario queda identificado con el tipo elegido
     * (p.ej. {@code reporte-calculos-qa-tri-hauls-ultimo.html}).
     */
    @Step("{0} selecciona una ruta sugerida de tipo {1} y Try this route")
    public void seleccionaRutaSugeridaDeTipo(String actorName, String tipoRuta) {
        setup.setupActor(actorName);
        FinancialReport.addContext("Tipo de ruta solicitado", tipoRuta);
        FinancialReport.appendSlug(slugDe(tipoRuta));
        theActorInTheSpotlight().attemptsTo(
                SelectTriHaulRoute.ofType(tipoRuta)
        );
    }

    private String slugDe(String tipoRuta) {
        return tipoRuta.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
