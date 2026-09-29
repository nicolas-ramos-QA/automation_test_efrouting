package com.filtros_de_rutas.utils;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DiagnosticDump {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticDump.class);
    private static final Path DIR = Paths.get("target/diagnostico");

    private DiagnosticDump() {}

    public static void savePage(Actor actor, String fileName) {
        try {
            Files.createDirectories(DIR);
            Files.writeString(DIR.resolve(fileName),
                    BrowseTheWeb.as(actor).getDriver().getPageSource(), StandardCharsets.UTF_8);
            LOGGER.info("DOM guardado en {}", DIR.resolve(fileName).toAbsolutePath());
        } catch (Exception e) {
            LOGGER.warn("No se pudo guardar el DOM en {}", fileName, e);
        }
    }

    public static void saveText(String fileName, String content) {
        try {
            Files.createDirectories(DIR);
            Files.writeString(DIR.resolve(fileName),
                    content == null ? "" : content, StandardCharsets.UTF_8);
            LOGGER.info("Texto guardado en {}", DIR.resolve(fileName).toAbsolutePath());
        } catch (Exception e) {
            LOGGER.warn("No se pudo guardar texto en {}", fileName, e);
        }
    }
}
