package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.ExistingRouteTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExistingRouteTargetTest {

    @Test
    void parseaUrlDeQaYExtraeId() {
        ExistingRouteTarget target = ExistingRouteTarget.parse(
                "https://efdata-qa.efrouting.com/route-planner/detail/3417", null);

        assertEquals("3417", target.getRouteId());
        assertEquals("https://efdata-qa.efrouting.com/route-planner/detail/3417", target.getDetailUrl());
        assertEquals("QA", target.getEnvironmentName());
        assertEquals("ruta-existente-3417", target.reportSuffix());
    }

    @Test
    void parseaSoloIdYUsaQaPorDefecto() {
        ExistingRouteTarget target = ExistingRouteTarget.parse("3417", null);

        assertEquals("3417", target.getRouteId());
        assertEquals("https://efdata-qa.efrouting.com/route-planner/detail/3417", target.getDetailUrl());
        assertEquals("QA", target.getEnvironmentName());
    }

    @Test
    void parseaIdConAmbienteDeProduccion() {
        ExistingRouteTarget target = ExistingRouteTarget.parse("3417", "PRODUCCION");

        assertEquals("3417", target.getRouteId());
        assertEquals("https://efdata.efrouting.com/route-planner/detail/3417", target.getDetailUrl());
        assertEquals("PRODUCCION", target.getEnvironmentName());
    }

    @Test
    void infiereProduccionDesdeLaUrl() {
        ExistingRouteTarget target = ExistingRouteTarget.parse(
                "https://efdata.efrouting.com/route-planner/detail/99", null);

        assertEquals("99", target.getRouteId());
        assertEquals("PRODUCCION", target.getEnvironmentName());
    }

    @Test
    void recortaQueryStringDeLaUrl() {
        ExistingRouteTarget target = ExistingRouteTarget.parse(
                "https://efdata-qa.efrouting.com/route-planner/detail/3417?tab=plan", null);

        assertEquals("https://efdata-qa.efrouting.com/route-planner/detail/3417", target.getDetailUrl());
    }

    @Test
    void fallaSiNoHayRuta() {
        AssertionError error = assertThrows(AssertionError.class,
                () -> ExistingRouteTarget.parse("   ", null));
        assertTrue(error.getMessage().contains("RunnerCalculosRutaExistente"));
        assertTrue(error.getMessage().contains("-Druta"));
    }

    @Test
    void fallaSiLaUrlNoTraeId() {
        assertThrows(AssertionError.class,
                () -> ExistingRouteTarget.parse("https://efdata-qa.efrouting.com/routes", null));
    }
}
