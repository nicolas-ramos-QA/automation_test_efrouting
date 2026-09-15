package com.calculos_de_rutas.ranners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Valida los cálculos de una ruta YA CREADA (URL o ID en {@code -Druta}).
 *
 * <p>No empieza por {@code TestRunner} a propósito: {@code mvn test} no lo toma.
 * Solo corre con su comando:</p>
 *
 * <pre>
 * mvn test "-Dtest=RunnerCalculosRutaExistente" "-Druta=https://efdata-qa.efrouting.com/route-planner/detail/3417"
 * </pre>
 *
 * <p>Se ejecuta desde la raíz del repositorio (un solo {@code src} para los tres flujos).</p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/calculos_de_rutas/")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@RutaExistente")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.calculos_de_rutas.stepdefinitions.calculos_de_rutas")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.cucumber.core.plugin.SerenityReporterParallel,pretty,timeline:build/test-results/timeline")

public class RunnerCalculosRutaExistente {
}
