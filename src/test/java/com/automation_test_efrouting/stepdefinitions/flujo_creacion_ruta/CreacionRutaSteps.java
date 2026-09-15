package com.automation_test_efrouting.stepdefinitions.flujo_creacion_ruta;

import com.automation_test_efrouting.steps.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Steps;

public class CreacionRutaSteps {

    @Steps()
    LoginPage contactUsPage;
    ContactForm contactForm = new ContactForm();
    ContactSubmit contactSubmit = new ContactSubmit();
    RoutesNavigation routesNavigation = new RoutesNavigation();
    NewRoute newRoute = new NewRoute();
    TrailerType trailerType = new TrailerType();
    RouteForm routeForm = new RouteForm();
    ContinueRoute continueRoute = new ContinueRoute();
    TriHaulRoute triHaulRoute = new TriHaulRoute();
    EditLane editLane = new EditLane();
    RouteVerification routeVerification = new RouteVerification();

    @Given("^(.*) se encuentra pantalla de inicio de sesión")
    public void pantallaLogin(String actorName) {
        contactUsPage.abrirPortal(actorName);
    }

    @When("^(.*) ingresa credenciales de acceso")
    public void ingresaCredencialesAcceso(String actorName) {
        contactForm.ingresarData(actorName);
    }

    @And("^(.*) selecciona el botón \"Log in\"")
    public void seleccionaBotonLogin(String actorName) {
        contactSubmit.seleccionaBoton(actorName);
    }

    @And("^(.*) se dirige a la sección \"Routes\"")
    public void seDirigeSeccionRoutes(String actorName) {
        routesNavigation.seDirigeRoutes(actorName);
    }

    @Then("^(.*) debería visualizar la sección de rutas")
    public void deberiaVisualizarSeccionRutas(String actorName) {
        RoutesSection.visualizaSeccionRutas(actorName);
    }

    @And("^(.*) selecciona el botón \"New route\"")
    public void seleccionaBotonNewRoute(String actorName) {
        newRoute.seleccionaNewRoute(actorName);
    }

    @And("^(.*) selecciona la opción \"Trailer type only\"")
    public void seleccionaOpcionTrailerType(String actorName) {
        trailerType.seleccionaTrailerType(actorName);
    }

    @And("^(.*) completa los datos de la nueva ruta")
    public void completaDatosNuevaRuta(String actorName) {
        routeForm.completaDatosRuta(actorName);
    }

    @And("^(.*) finaliza la creación seleccionando \"Continue\"")
    public void finalizaCreacionContinue(String actorName) {
        continueRoute.seleccionaContinue(actorName);
    }

    @And("^(.*) selecciona la segunda ruta de \"Tri-hauls\"")
    public void seleccionaSegundaRutaTriHauls(String actorName) {
        triHaulRoute.seleccionaSegundaRutaTriHaul(actorName);
    }

    @And("^(.*) selecciona una ruta sugerida de tipo \"(.*)\"$")
    public void seleccionaRutaSugeridaDeTipo(String actorName, String tipoRuta) {
        triHaulRoute.seleccionaRutaSugeridaDeTipo(actorName, tipoRuta);
    }

    @And("^(.*) edita la última línea de la ruta")
    public void editaUltimaLineaRuta(String actorName) {
        editLane.editaUltimaLinea(actorName);
    }

    @Then("^(.*) verifica que las millas de la ruta sean correctas")
    public void verificaMillasRuta(String actorName) {
        routeVerification.verificaMillas(actorName);
    }
}
