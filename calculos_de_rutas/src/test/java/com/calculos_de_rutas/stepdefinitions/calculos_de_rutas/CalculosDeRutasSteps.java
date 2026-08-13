package com.calculos_de_rutas.stepdefinitions.calculos_de_rutas;

import com.calculos_de_rutas.steps.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CalculosDeRutasSteps {

    LoginPage loginPage = new LoginPage();
    ContactForm contactForm = new ContactForm();
    ContactSubmit contactSubmit = new ContactSubmit();
    RoutesNavigation routesNavigation = new RoutesNavigation();
    NewRoute newRoute = new NewRoute();
    TrailerType trailerType = new TrailerType();
    RouteForm routeForm = new RouteForm();
    ContinueRoute continueRoute = new ContinueRoute();
    TriHaulRoute triHaulRoute = new TriHaulRoute();
    FinancialVerification financialVerification = new FinancialVerification();

    @Given("^(.*) se encuentra en la pantalla de inicio de sesión de \"(.*)\"")
    public void pantallaLogin(String actorName, String ambiente) {
        loginPage.abrirPortal(actorName, ambiente);
    }

    @When("^(.*) ingresa las credenciales de \"(.*)\"")
    public void ingresaCredencialesAcceso(String actorName, String ambiente) {
        contactForm.ingresarData(actorName, ambiente);
    }

    @And("^(.*) selecciona el botón \"Log in\"")
    public void seleccionaBotonLogin(String actorName) {
        contactSubmit.seleccionaBoton(actorName);
    }

    @And("^(.*) se dirige a la sección \"Routes\"")
    public void seDirigeSeccionRoutes(String actorName) {
        routesNavigation.seDirigeRoutes(actorName);
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

    @And("^(.*) selecciona una ruta sugerida \\(Tri-hauls, Bi-hauls o Best Choice\\)")
    public void seleccionaRutaSugerida(String actorName) {
        triHaulRoute.seleccionaSegundaRutaTriHaul(actorName);
    }

    @And("^(.*) selecciona una ruta sugerida de tipo \"(.*)\"")
    public void seleccionaRutaSugeridaDeTipo(String actorName, String tipoRuta) {
        triHaulRoute.seleccionaRutaSugeridaDeTipo(actorName, tipoRuta);
    }

    @And("^(.*) cierra el mapa para ver todas las columnas de la tabla")
    public void cierraElMapa(String actorName) {
        financialVerification.cierraElMapa(actorName);
    }

    @And("^(.*) intercepta la respuesta financiera del endpoint user-route")
    public void interceptaRespuestaFinanciera(String actorName) {
        financialVerification.interceptaRespuestaFinanciera(actorName);
    }

    @And("^(.*) valida los valores financieros de cada lane contra el Backend")
    public void validaLanes(String actorName) {
        financialVerification.validaLanesContraBackend(actorName);
    }

    @And("^(.*) valida el comportamiento del Op cost visible")
    public void validaOpCostVisible(String actorName) {
        financialVerification.validaOpCostVisible(actorName);
    }

    @And("^(.*) valida el comportamiento del Op cost oculto")
    public void validaOpCostOculto(String actorName) {
        financialVerification.validaOpCostOculto(actorName);
    }

    @And("^(.*) valida los valores generales de la ruta contra el Backend")
    public void validaValoresGenerales(String actorName) {
        financialVerification.validaValoresGenerales(actorName);
    }

    @Then("^(.*) genera el reporte detallado de los cálculos financieros")
    public void generaReporteDetallado(String actorName) {
        financialVerification.generaReporteDetallado(actorName);
    }
}
