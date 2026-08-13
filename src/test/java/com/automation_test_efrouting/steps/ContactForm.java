package com.automation_test_efrouting.steps;

import com.automation_test_efrouting.interactions.LoginForm;
import com.automation_test_efrouting.stepdefinitions.Setup;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ContactForm {

    private static final Setup setup = new Setup();

    @Step("{0} ingresa la data en el formulario de contacto")
    public void ingresarData(String actorname){
        setup.setupActor(actorname);
        theActorInTheSpotlight().attemptsTo(
            LoginForm.dataLlenarFormulario()
        );
    }
}
