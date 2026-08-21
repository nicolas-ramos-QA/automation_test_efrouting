package com.calculos_de_rutas.steps;

import com.calculos_de_rutas.interactions.SelectTriHaulRoute;
import com.calculos_de_rutas.stepdefinitions.Setup;
import com.calculos_de_rutas.utils.FinancialReport;
import net.serenitybdd.annotations.Step;
import org.opentest4j.TestAbortedException;

import java.util.Locale;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class TriHaulRoute {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona una ruta sugerida (Tri-hauls → Bi-hauls → Best Choice → Loops) y Try this route")
    public void seleccionaSegundaRutaTriHaul(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectTriHaulRoute.secondTriHaulRouteSelected()
        );
    }

    /**
     * Fuerza el tipo de ruta sugerida (Tri-hauls, Bi-hauls, Best Choice o Loops).
     * Si ese tipo no aparece entre las sugerencias, el escenario se anula (SKIPPED), no falla.
     * El reporte de cálculos queda identificado con el tipo elegido
     * (p.ej. {@code reporte-calculos-qa-loops-ultimo.html}).
     */
    @Step("{0} selecciona una ruta sugerida de tipo {1} y Try this route")
    public void seleccionaRutaSugeridaDeTipo(String actorName, String tipoRuta) {
        setup.setupActor(actorName);
        FinancialReport.addContext("Tipo de ruta solicitado", tipoRuta);
        FinancialReport.appendSlug(slugDe(tipoRuta));
        try {
            theActorInTheSpotlight().attemptsTo(
                    SelectTriHaulRoute.ofType(tipoRuta)
            );
        } catch (Throwable t) {
            // Serenity a veces envuelve TestAbortedException; la re-lanzamos limpia
            // para que Cucumber/JUnit la reporten como omitido, no como fallido.
            TestAbortedException aborted = unwrapAborted(t);
            if (aborted != null) {
                throw aborted;
            }
            if (t instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new RuntimeException(t);
        }
    }

    private static TestAbortedException unwrapAborted(Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof TestAbortedException aborted) {
                return aborted;
            }
            current = current.getCause();
        }
        return null;
    }

    private String slugDe(String tipoRuta) {
        return tipoRuta.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }
}
