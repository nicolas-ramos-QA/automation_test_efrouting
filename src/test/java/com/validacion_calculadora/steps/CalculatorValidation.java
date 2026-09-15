package com.validacion_calculadora.steps;

import com.validacion_calculadora.interactions.ValidateCalculatorOnLoads;
import com.validacion_calculadora.models.CalculatorValidationResult;
import com.validacion_calculadora.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class CalculatorValidation {

    private static final Setup setup = new Setup();

    @Step("{0} abre cargas hasta validar Current profit (ciudades y porcentaje)")
    public void validaCalculadoraEnCargas(String actorName) {
        setup.setupActor(actorName);
        theActorInTheSpotlight().attemptsTo(
                ValidateCalculatorOnLoads.untilCurrentProfitIsVisible()
        );

        CalculatorValidationResult result =
                theActorInTheSpotlight().recall(ValidateCalculatorOnLoads.REMEMBER_RESULT);
        if (result == null) {
            throw new AssertionError("No se guardó el resultado de la validación de calculadora.");
        }
        net.serenitybdd.core.Serenity.recordReportData()
                .withTitle("Current profit + fórmula Income/Distance=RPM")
                .andContents(result.toString()
                        + "\n\nReporte HTML: target/reportes/reporte-calculadora-ultimo.html");
    }
}