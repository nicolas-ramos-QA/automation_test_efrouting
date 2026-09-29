package com.filtros_de_rutas.stepdefinitions.filtros_de_rutas;

import com.filtros_de_rutas.steps.ContactForm;
import com.filtros_de_rutas.steps.ContactSubmit;
import com.filtros_de_rutas.steps.FiltersValidation;
import com.filtros_de_rutas.steps.LoginPage;
import com.filtros_de_rutas.steps.RoutesNavigation;
import com.filtros_de_rutas.steps.RoutesSection;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class FiltrosDeRutasSteps {

    LoginPage loginPage = new LoginPage();
    ContactForm contactForm = new ContactForm();
    ContactSubmit contactSubmit = new ContactSubmit();
    RoutesNavigation routesNavigation = new RoutesNavigation();
    RoutesSection routesSection = new RoutesSection();
    FiltersValidation filtersValidation = new FiltersValidation();

    @Given("^(.*) se encuentra pantalla de inicio de sesión")
    public void pantallaLogin(String actorName) {
        loginPage.abrirPortal(actorName);
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

    @And("^(.*) debería visualizar el listado de rutas")
    public void deberiaVisualizarListado(String actorName) {
        routesSection.visualizaListado(actorName);
    }

    @Then("^(.*) aplica cada filtro y verifica que los totales del pie cambian")
    public void aplicaFiltrosYVerificaTotales(String actorName) {
        filtersValidation.validaFiltros(actorName);
    }
}
