package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.SelectTriHaulRoute;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;
import org.opentest4j.TestAbortedException;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class TriHaulRoute {

    private static final Setup setup = new Setup();

    @Step("{0} selecciona la segunda ruta de Tri-hauls")
    public void seleccionaSegundaRutaTriHaul(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                SelectTriHaulRoute.secondTriHaulRouteSelected()
        );
    }

    /**
     * Fuerza el tipo de ruta sugerida (Tri-hauls, Bi-hauls, Best Choice o Loops).
     * Si ese tipo no aparece entre las sugerencias, el escenario se anula (SKIPPED), no falla.
     */
    @Step("{0} selecciona una ruta sugerida de tipo {1} y Try this route")
    public void seleccionaRutaSugeridaDeTipo(String actorName, String tipoRuta) {
        setup.setupActor(actorName);
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
}
