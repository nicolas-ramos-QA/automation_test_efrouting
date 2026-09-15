package com.validacion_calculadora.stepdefinitions.validacion_calculadora;

import com.validacion_calculadora.steps.CalculatorValidation;
import com.validacion_calculadora.steps.ContactForm;
import com.validacion_calculadora.steps.ContactSubmit;
import com.validacion_calculadora.steps.LoadSearch;
import com.validacion_calculadora.steps.LoadboardNavigation;
import com.validacion_calculadora.steps.LoadboardSection;
import com.validacion_calculadora.steps.LoginPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ValidacionCalculadoraSteps {

    LoginPage loginPage = new LoginPage();
    ContactForm contactForm = new ContactForm();
    ContactSubmit contactSubmit = new ContactSubmit();
    LoadboardNavigation loadboardNavigation = new LoadboardNavigation();
    LoadboardSection loadboardSection = new LoadboardSection();
    LoadSearch loadSearch = new LoadSearch();
    CalculatorValidation calculatorValidation = new CalculatorValidation();

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

    @And("^(.*) se dirige a la sección \"Loadboard\"")
    public void seDirigeSeccionLoadboard(String actorName) {
        loadboardNavigation.seDirigeLoadboard(actorName);
    }

    @And("^(.*) debería visualizar la pantalla de Loadboard")
    public void deberiaVisualizarLoadboard(String actorName) {
        loadboardSection.visualizaLoadboard(actorName);
    }

    @And("^(.*) busca cargas con origen y destino variables")
    public void buscaCargasConOrigenYDestinoVariables(String actorName) {
        loadSearch.buscaCargasConCiudadesVariables(actorName);
    }

    @Then("^(.*) abre cargas hasta validar Current profit con ciudades y porcentaje")
    public void abreCargasHastaValidarCurrentProfit(String actorName) {
        calculatorValidation.validaCalculadoraEnCargas(actorName);
    }
}
