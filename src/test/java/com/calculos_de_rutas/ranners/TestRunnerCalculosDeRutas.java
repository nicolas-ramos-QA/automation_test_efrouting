package com.calculos_de_rutas.ranners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/calculos_de_rutas/")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@CalculosDeRutas")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.calculos_de_rutas.stepdefinitions.calculos_de_rutas")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.cucumber.core.plugin.SerenityReporterParallel,pretty,timeline:build/test-results/timeline")

public class TestRunnerCalculosDeRutas {
}
