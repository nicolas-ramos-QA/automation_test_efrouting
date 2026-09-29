package com.filtros_de_rutas.utils;

import com.filtros_de_rutas.models.FilterCheck;
import com.filtros_de_rutas.models.RouteFooterTotals;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * Reporte HTML de la validación de filtros: baseline, cada filtro y captura.
 * Archivos en {@code target/reportes/}.
 */
public final class FiltersTestReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiltersTestReport.class);
    private static final String OUTPUT_DIR = "target/reportes";
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter HUMAN_STAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private FiltersTestReport() {}

    public static Path generateAndOpen(WebDriver driver, RouteFooterTotals baseline, List<FilterCheck> checks) {
        byte[] png = capturePng(driver);
        Path html = write(baseline, checks, png, true);
        LOGGER.info("Reporte de filtros: {}", html.toAbsolutePath());
        return html;
    }

    public static Path write(RouteFooterTotals baseline, List<FilterCheck> checks,
                             byte[] screenshotPng, boolean openBrowser) {
        try {
            Path dir = Paths.get(OUTPUT_DIR);
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(FILE_STAMP);
            String pngName = "captura-filtros-" + stamp + ".png";
            String b64 = "";
            if (screenshotPng != null && screenshotPng.length > 0) {
                Path pngFile = dir.resolve(pngName);
                Files.write(pngFile, screenshotPng);
                b64 = Base64.getEncoder().encodeToString(screenshotPng);
            }

            String html = buildHtml(baseline, checks, b64);
            Path stamped = dir.resolve("reporte-filtros-" + stamp + ".html");
            Path latest = dir.resolve("reporte-filtros-ultimo.html");
            Files.writeString(stamped, html, StandardCharsets.UTF_8);
            Files.writeString(latest, html, StandardCharsets.UTF_8);

            if (openBrowser) {
                openInBrowser(stamped);
            }
            return stamped;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo escribir el reporte de filtros", e);
        }
    }

    private static String buildHtml(RouteFooterTotals baseline, List<FilterCheck> checks, String b64) {
        long pass = checks.stream().filter(c -> c.getStatus() == FilterCheck.Status.PASS).count();
        long fail = checks.stream().filter(c -> c.getStatus() == FilterCheck.Status.FAIL).count();
        long skip = checks.stream().filter(c -> c.getStatus() == FilterCheck.Status.SKIP).count();
        StringBuilder rows = new StringBuilder();
        for (FilterCheck check : checks) {
            rows.append("<tr class='").append(check.getStatus().name().toLowerCase()).append("'>")
                    .append("<td>").append(esc(check.getFilterName())).append("</td>")
                    .append("<td>").append(esc(nullToDash(check.getAppliedValue()))).append("</td>")
                    .append("<td>").append(esc(check.getStatus().name())).append("</td>")
                    .append("<td>").append(esc(footer(check.getAfterFilter()))).append("</td>")
                    .append("<td>").append(esc(nullToDash(check.getDetail()))).append("</td>")
                    .append("</tr>");
        }
        String img = b64.isBlank() ? ""
                : "<h2>Captura</h2><img alt='listado de rutas' src='data:image/png;base64," + b64 + "'/>";
        return """
                <!DOCTYPE html>
                <html lang="es"><head><meta charset="UTF-8"/>
                <title>Reporte filtros de rutas</title>
                <style>
                  body{font-family:Segoe UI,sans-serif;background:#111827;color:#e5e7eb;margin:24px;}
                  h1,h2{color:#f9fafb;}
                  .meta{color:#9ca3af;margin-bottom:16px;}
                  .ok{color:#34d399;} .fail{color:#f87171;}
                  table{border-collapse:collapse;width:100%%;margin:16px 0;font-size:14px;}
                  th,td{border:1px solid #374151;padding:8px 10px;vertical-align:top;}
                  th{background:#1f2937;text-align:left;}
                  tr.pass td:nth-child(3){color:#34d399;font-weight:600;}
                  tr.fail td:nth-child(3){color:#f87171;font-weight:600;}
                  tr.skip td:nth-child(3){color:#fbbf24;font-weight:600;}
                  img{max-width:100%%;border:1px solid #374151;border-radius:8px;}
                  .box{background:#1f2937;padding:12px 16px;border-radius:8px;}
                </style></head><body>
                <h1>Filtros de rutas — totales del pie</h1>
                <p class="meta">%s · PASS %d · FAIL %d · SKIP %d</p>
                <div class="box"><strong>Baseline</strong><br/>%s</div>
                <h2>Resultado por filtro</h2>
                <table>
                  <tr><th>Filtro</th><th>Valor aplicado</th><th>Estado</th><th>Pie tras filtro</th><th>Detalle</th></tr>
                  %s
                </table>
                %s
                </body></html>
                """.formatted(
                LocalDateTime.now().format(HUMAN_STAMP),
                pass, fail, skip,
                esc(baseline == null ? "—" : baseline.snapshot()),
                rows,
                img);
    }

    private static String footer(RouteFooterTotals totals) {
        return totals == null ? "—" : totals.snapshot();
    }

    private static byte[] capturePng(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            LOGGER.warn("No se pudo capturar pantalla: {}", e.getMessage());
            return new byte[0];
        }
    }

    private static void openInBrowser(Path html) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(html.toUri());
            }
        } catch (Exception e) {
            LOGGER.warn("No se pudo abrir el reporte: {}", e.getMessage());
        }
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
