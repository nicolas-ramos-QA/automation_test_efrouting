package com.calculos_de_rutas.utils;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Guarda el DOM en momentos clave del flujo para poder ajustar selectores sin volver a correr a ciegas.
 */
public final class DiagnosticDump {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticDump.class);
    private static final Path DIR = Paths.get("target/diagnostico");

    private DiagnosticDump() {}

    public static void savePage(Actor actor, String fileName) {
        try {
            Files.createDirectories(DIR);
            Files.writeString(DIR.resolve(fileName),
                    BrowseTheWeb.as(actor).getDriver().getPageSource(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("No se pudo guardar el DOM en {}", fileName, e);
        }
    }
}
