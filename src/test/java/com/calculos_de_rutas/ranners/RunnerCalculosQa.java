package com.calculos_de_rutas.ranners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Ejecuta la validación financiera únicamente contra QA.
 *
 * <p>No empieza por {@code TestRunner} a propósito: así {@code mvn test} no lo toma y solo corre
 * cuando se pide explícitamente con {@code -Dtest=RunnerCalculosQa}.</p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/calculos_de_rutas/")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@CalculosDeRutas and @QA")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.calculos_de_rutas.stepdefinitions.calculos_de_rutas")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.cucumber.core.plugin.SerenityReporterParallel,pretty,timeline:build/test-results/timeline")

public class RunnerCalculosQa {
}
