package com.validacion_calculadora.utils;

import com.validacion_calculadora.models.CalculatorFinancialMetrics;
import com.validacion_calculadora.models.CalculatorValidationResult;
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
import java.util.Locale;

/**
 * Reporte HTML de la validación de la calculadora: datos leídos del modal + captura al final.
 * Archivos en {@code target/reportes/}.
 */
public final class CalculatorTestReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorTestReport.class);

    private static final String OUTPUT_DIR = "target/reportes";
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter HUMAN_STAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private CalculatorTestReport() {}

    /**
     * Captura el modal, escribe HTML (+ PNG) y abre el reporte en el navegador.
     *
     * @return ruta del HTML generado
     */
    public static Path generateAndOpen(
            WebDriver driver,
            CalculatorValidationResult result,
            String origin,
            String destination,
            int loadAttempt) {
        byte[] png = capturePng(driver);
        Path html = write(result, origin, destination, loadAttempt, png, true);
        LOGGER.info("Reporte de calculadora: {}", html.toAbsolutePath());
        return html;
    }

    public static Path write(
            CalculatorValidationResult result,
            String origin,
            String destination,
            int loadAttempt,
            byte[] screenshotPng,
            boolean openBrowser) {
        try {
            Path dir = Paths.get(OUTPUT_DIR);
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(FILE_STAMP);
            String pngName = "captura-calculadora-" + stamp + ".png";
            String b64 = "";
            if (screenshotPng != null && screenshotPng.length > 0) {
                Path pngFile = dir.resolve(pngName);
                Files.write(pngFile, screenshotPng);
                b64 = Base64.getEncoder().encodeToString(screenshotPng);
                LOGGER.info("Captura guardada: {}", pngFile.toAbsolutePath());
            }

            String html = buildHtml(result, origin, destination, loadAttempt, b64, pngName);
            Path stamped = dir.resolve("reporte-calculadora-" + stamp + ".html");
            Path latest = dir.resolve("reporte-calculadora-ultimo.html");
            Files.writeString(stamped, html, StandardCharsets.UTF_8);
            Files.writeString(latest, html, StandardCharsets.UTF_8);

            if (openBrowser) {
                openInBrowser(stamped);
            }
            return stamped.toAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el reporte de calculadora", e);
        }
    }

    public static byte[] capturePng(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            LOGGER.warn("No se pudo capturar pantalla para el reporte: {}", e.getMessage());
            return new byte[0];
        }
    }

    private static String buildHtml(
            CalculatorValidationResult result,
            String origin,
            String destination,
            int loadAttempt,
            String screenshotBase64,
            String pngFileName) {
        CalculatorFinancialMetrics m = result.getFinancialMetrics();
        boolean formulaOk = false;
        double exactRpm = 0;
        double uiRpm = 0;
        double delta = 0;
        if (m != null && m.getDistanceMi() > 0) {
            exactRpm = m.getIncome() / m.getDistanceMi();
            uiRpm = m.getRpm();
            delta = Math.abs(exactRpm - uiRpm);
            formulaOk = delta <= 0.01;
        }

        boolean perDayOk = CalculatorFormulaAssertions.matchesIncomeDividedByDays(m);
        double exactPerDay = 0;
        double uiPerDay = 0;
        double deltaPerDay = 0;
        if (m != null && m.hasIncomePerDayData()) {
            exactPerDay = CalculatorFormulaAssertions.exactIncomeOverDays(m);
            uiPerDay = m.getIncomePerDay();
            deltaPerDay = Math.abs(exactPerDay - uiPerDay);
        }
        boolean profitOk = CalculatorFormulaAssertions.matchesIncomeMinusCosts(m);
        double exactProfit = 0;
        double uiProfit = 0;
        double deltaProfit = 0;
        if (m != null && m.hasProfitData()) {
            exactProfit = CalculatorFormulaAssertions.exactIncomeMinusCosts(m);
            uiProfit = m.getCurrentProfit();
            deltaProfit = Math.abs(exactProfit - uiProfit);
        }
        boolean perMileOk = CalculatorFormulaAssertions.matchesProfitPerMile(m);
        double exactPerMile = 0;
        double uiPerMile = 0;
        double deltaPerMile = 0;
        if (m != null && m.hasProfitPerMileData()) {
            exactPerMile = CalculatorFormulaAssertions.exactProfitOverTotalDistance(m);
            uiPerMile = m.getProfitPerMile();
            deltaPerMile = Math.abs(exactPerMile - uiPerMile);
        }
        boolean percentOk = CalculatorFormulaAssertions.matchesProfitPercent(m);
        double exactPercent = 0;
        double uiPercent = 0;
        double deltaPercent = 0;
        if (m != null && m.hasProfitPercentData()) {
            exactPercent = CalculatorFormulaAssertions.exactProfitPercent(m);
            uiPercent = m.getProfitPercent();
            deltaPercent = Math.abs(exactPercent - uiPercent);
        }
        boolean todoOk = formulaOk && perDayOk && profitOk && perMileOk && percentOk;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>Reporte validación calculadora</title>")
                .append("<style>").append(css()).append("</style></head><body>");

        html.append("<header>")
                .append("<div class=\"brand\">efRouting · validacion_calculadora</div>")
                .append("<h1>Reporte de prueba — Calculadora</h1>")
                .append("<p class=\"subtitle\">Datos del modal <b>Calculate profit</b> (valores exactos, sin redondear el cociente) &nbsp;·&nbsp; ")
                .append(escape(LocalDateTime.now().format(HUMAN_STAMP))).append("</p>")
                .append("</header>");

        html.append("<div class=\"banner ").append(todoOk ? "ok" : "bad").append("\">")
                .append(todoOk
                        ? "Validación OK — las 5 fórmulas: Income / Distance = RPM, "
                                + "Income / Días = Income per day, Income − Total costs = Current profit, "
                                + "Current profit / Total Distance = Profit / mile "
                                + "y Current profit / Income = Profit %"
                        : "Revisar — faltan métricas o alguna fórmula no cuadra")
                .append("</div>");

        html.append("<section class=\"summary\">")
                .append(row("Origen búsqueda", nullToDash(origin)))
                .append(row("Destino búsqueda", nullToDash(destination)))
                .append(row("Carga validada #", String.valueOf(loadAttempt)))
                .append("</section>");

        html.append("<h2>1. Validación de fórmula — RPM</h2>")
                .append("<p class=\"hint\">Income ÷ Distance = RPM — cociente <b>exacto</b> (sin redondear). ")
                .append("Tolerancia ±0.01 solo al comparar con el RPM de la UI.</p>")
                .append("<section class=\"card-block\"><table>")
                .append("<tr><th>Concepto</th><th>Valor</th></tr>")
                .append("<tr><td>Fórmula</td><td><code>Income / Distance = RPM</code></td></tr>");
        if (m != null) {
            html.append("<tr><td>Operación</td><td><code>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getIncome())))
                    .append(" ÷ ")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getDistanceMi())))
                    .append("</code></td></tr>")
                    .append("<tr><td>Resultado exacto (calculadora)</td><td><b>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(exactRpm)))
                    .append("</b></td></tr>")
                    .append("<tr><td>RPM en UI</td><td>$")
                    .append(escape(CalculatorFormulaAssertions.formatExact(uiRpm)))
                    .append("/mi</td></tr>")
                    .append("<tr><td>Δ</td><td>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(delta)))
                    .append("</td></tr>")
                    .append("<tr><td>Resultado</td><td><span class=\"tag ")
                    .append(formulaOk ? "PASS" : "FAIL").append("\">")
                    .append(formulaOk ? "OK" : "FALLÓ")
                    .append("</span></td></tr>");
        }
        html.append("</table></section>");

        html.append("<h2>2. Validación de fórmula — Income per day</h2>")
                .append("<p class=\"hint\">Income ÷ Days on Route = Income per day — cociente <b>exacto</b>. ")
                .append("Tolerancia ±1 porque la UI muestra el importe redondeado a dólares enteros.</p>");
        if (m == null || !m.hasIncomePerDayData()) {
            html.append("<p class=\"empty\">No se leyeron Days on Route / Income per day del modal.</p>");
        } else {
            html.append("<section class=\"card-block\"><table>")
                    .append("<tr><th>Concepto</th><th>Valor</th></tr>")
                    .append("<tr><td>Fórmula</td><td><code>Income / Days on Route = Income per day</code></td></tr>")
                    .append("<tr><td>Operación</td><td><code>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getIncome())))
                    .append(" ÷ ")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getDaysOnRoute())))
                    .append("</code></td></tr>")
                    .append("<tr><td>Resultado exacto (calculadora)</td><td><b>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(exactPerDay)))
                    .append("</b></td></tr>")
                    .append("<tr><td>Income per day en UI</td><td>$")
                    .append(escape(CalculatorFormulaAssertions.formatExact(uiPerDay)))
                    .append("/day</td></tr>")
                    .append("<tr><td>Δ</td><td>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(deltaPerDay)))
                    .append("</td></tr>")
                    .append("<tr><td>Resultado</td><td><span class=\"tag ")
                    .append(perDayOk ? "PASS" : "FAIL").append("\">")
                    .append(perDayOk ? "OK" : "FALLÓ")
                    .append("</span></td></tr>")
                    .append("</table></section>");
        }

        html.append("<h2>3. Validación de fórmula — Current profit</h2>")
                .append("<p class=\"hint\">Income − Total costs = Current profit. ")
                .append("Tolerancia ±1 porque la UI muestra los importes redondeados a dólares enteros.</p>");
        if (m == null || !m.hasProfitData()) {
            html.append("<p class=\"empty\">No se leyeron Total costs / Current profit del modal.</p>");
        } else {
            html.append("<section class=\"card-block\"><table>")
                    .append("<tr><th>Concepto</th><th>Valor</th></tr>")
                    .append("<tr><td>Fórmula</td><td><code>Income - Total costs = Current profit</code></td></tr>")
                    .append("<tr><td>Operación</td><td><code>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getIncome())))
                    .append(" − ")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getTotalCosts())))
                    .append("</code></td></tr>")
                    .append("<tr><td>Resultado exacto (calculadora)</td><td><b>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(exactProfit)))
                    .append("</b></td></tr>")
                    .append("<tr><td>Current profit en UI</td><td>$")
                    .append(escape(CalculatorFormulaAssertions.formatExact(uiProfit)))
                    .append("</td></tr>")
                    .append("<tr><td>Δ</td><td>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(deltaProfit)))
                    .append("</td></tr>")
                    .append("<tr><td>Resultado</td><td><span class=\"tag ")
                    .append(profitOk ? "PASS" : "FAIL").append("\">")
                    .append(profitOk ? "OK" : "FALLÓ")
                    .append("</span></td></tr>")
                    .append("</table></section>");
        }

        html.append("<h2>4. Validación de fórmula — Profit / mile</h2>")
                .append("<p class=\"hint\">Current profit ÷ Total Distance = Profit / mile — cociente ")
                .append("<b>exacto</b> (sin redondear). Tolerancia ±0.01 solo al comparar con la UI.</p>");
        if (m == null || !m.hasProfitPerMileData()) {
            html.append("<p class=\"empty\">No se leyeron Total Distance / Profit per mile del modal.</p>");
        } else {
            html.append("<section class=\"card-block\"><table>")
                    .append("<tr><th>Concepto</th><th>Valor</th></tr>")
                    .append("<tr><td>Fórmula</td><td><code>Current profit / Total Distance = Profit / mile")
                    .append("</code></td></tr>")
                    .append("<tr><td>Operación</td><td><code>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getCurrentProfit())))
                    .append(" ÷ ")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getTotalDistanceMi())))
                    .append("</code></td></tr>")
                    .append("<tr><td>Resultado exacto (calculadora)</td><td><b>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(exactPerMile)))
                    .append("</b></td></tr>")
                    .append("<tr><td>Profit / mile en UI</td><td>$")
                    .append(escape(CalculatorFormulaAssertions.formatExact(uiPerMile)))
                    .append("/mi</td></tr>")
                    .append("<tr><td>Δ</td><td>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(deltaPerMile)))
                    .append("</td></tr>")
                    .append("<tr><td>Resultado</td><td><span class=\"tag ")
                    .append(perMileOk ? "PASS" : "FAIL").append("\">")
                    .append(perMileOk ? "OK" : "FALLÓ")
                    .append("</span></td></tr>")
                    .append("</table></section>");
        }

        html.append("<h2>5. Validación de fórmula — Profit %</h2>")
                .append("<p class=\"hint\">Income − Total costs = Current profit, y ")
                .append("Current profit ÷ Income = Profit %. El cociente se calcula <b>exacto</b>; ")
                .append("la UI muestra el porcentaje redondeado al entero (47.56% → 48%), ")
                .append("por eso se admite hasta medio punto de diferencia.</p>");
        if (m == null || !m.hasProfitPercentData()) {
            html.append("<p class=\"empty\">No se leyeron Current profit / Profit % del modal.</p>");
        } else {
            html.append("<section class=\"card-block\"><table>")
                    .append("<tr><th>Concepto</th><th>Valor</th></tr>")
                    .append("<tr><td>Fórmula</td><td><code>Current profit / Income = Profit %")
                    .append("</code></td></tr>")
                    .append("<tr><td>Operación</td><td><code>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getCurrentProfit())))
                    .append(" ÷ ")
                    .append(escape(CalculatorFormulaAssertions.formatExact(m.getIncome())))
                    .append("</code></td></tr>")
                    .append("<tr><td>Cociente exacto (calculadora)</td><td><b>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(
                            m.getCurrentProfit() / m.getIncome())))
                    .append("</b></td></tr>")
                    .append("<tr><td>Porcentaje exacto</td><td><b>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(exactPercent)))
                    .append("%</b></td></tr>")
                    .append("<tr><td>Profit % en UI (redondeado)</td><td>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(uiPercent)))
                    .append("%</td></tr>")
                    .append("<tr><td>Δ</td><td>")
                    .append(escape(CalculatorFormulaAssertions.formatExact(deltaPercent)))
                    .append("</td></tr>")
                    .append("<tr><td>Resultado</td><td><span class=\"tag ")
                    .append(percentOk ? "PASS" : "FAIL").append("\">")
                    .append(percentOk ? "OK" : "FALLÓ")
                    .append("</span></td></tr>")
                    .append("</table></section>");
        }

        html.append("<h2>6. Captura del modal (comparación visual)</h2>")
                .append("<p class=\"hint\">Estado de la pantalla al validar Calculate profit. ")
                .append("También en archivo: <code>").append(escape(pngFileName)).append("</code></p>");
        if (screenshotBase64 == null || screenshotBase64.isBlank()) {
            html.append("<p class=\"empty\">No hay captura disponible.</p>");
        } else {
            html.append("<figure class=\"shot\">")
                    .append("<figcaption>Modal Calculate profit — captura al finalizar la validación</figcaption>")
                    .append("<img src=\"data:image/png;base64,").append(screenshotBase64)
                    .append("\" alt=\"Captura Calculate profit\"/>")
                    .append("</figure>");
        }

        html.append("<footer>Generado automáticamente · ")
                .append(escape(LocalDateTime.now().format(HUMAN_STAMP)))
                .append(" · target/reportes/reporte-calculadora-ultimo.html</footer>")
                .append("</body></html>");
        return html.toString();
    }

    private static String row(String label, String value) {
        return "<div><span>" + escape(label) + "</span><b>" + escape(value) + "</b></div>";
    }

    private static String nullToDash(String v) {
        return v == null || v.isBlank() ? "—" : v;
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static void openInBrowser(Path file) {
        try {
            Path absolute = file.toAbsolutePath();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(absolute.toUri());
                return;
            }
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "", absolute.toString());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", absolute.toString());
            } else {
                pb = new ProcessBuilder("xdg-open", absolute.toString());
            }
            pb.start();
        } catch (Exception e) {
            LOGGER.warn("No se pudo abrir el reporte en el navegador: {}", e.getMessage());
        }
    }

    private static String css() {
        return """
                *{box-sizing:border-box}
                body{margin:0;padding:28px 36px 56px;font-family:'Segoe UI',system-ui,sans-serif;
                     background:#0f1419;color:#e7ecf3;line-height:1.45}
                header{margin-bottom:20px}
                .brand{font-size:12px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;
                       color:#00c980;margin-bottom:6px}
                h1{margin:0;font-size:26px;font-weight:750;letter-spacing:-.02em;color:#fff}
                .subtitle{margin:8px 0 0;color:#9aa7b5;font-size:14px}
                h2{margin:32px 0 10px;font-size:18px;color:#fff;border-bottom:1px solid #243041;padding-bottom:8px}
                .hint{margin:0 0 12px;font-size:13px;color:#8b9aab}
                .hint code{background:#1a2330;padding:2px 6px;border-radius:4px;font-size:12px}
                .banner{padding:12px 16px;border-radius:10px;font-weight:650;font-size:14px;margin-bottom:16px}
                .banner.ok{background:#0f2e1f;color:#86efac;border:1px solid #166534}
                .banner.bad{background:#3b1212;color:#fecaca;border:1px solid #991b1b}
                .summary{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:10px 16px;
                         background:#151c26;border:1px solid #243041;border-radius:12px;padding:14px 18px;margin-bottom:8px}
                .summary span{display:block;font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#8b9aab}
                .summary b{font-weight:600;color:#fff}
                .card-block{background:#151c26;border:1px solid #243041;border-radius:12px;padding:4px 0;overflow:auto}
                table{width:100%;border-collapse:collapse;font-size:14px}
                th,td{padding:10px 16px;text-align:left;border-bottom:1px solid #243041;vertical-align:top}
                th{color:#8b9aab;font-size:11px;text-transform:uppercase;letter-spacing:.04em;font-weight:600}
                td code{background:#1a2330;padding:2px 6px;border-radius:4px;font-size:12px;color:#94d1d6}
                .tag{display:inline-block;padding:2px 10px;border-radius:999px;font-size:12px;font-weight:700}
                .tag.PASS{background:#14532d;color:#86efac}
                .tag.FAIL{background:#7f1d1d;color:#fecaca}
                .shot{margin:12px 0 0;background:#151c26;border:1px solid #243041;border-radius:12px;padding:12px}
                .shot figcaption{font-size:13px;color:#8b9aab;margin:0 0 10px}
                .shot img{display:block;width:100%;max-width:1100px;height:auto;border-radius:8px;
                          border:1px solid #243041;background:#0b0f14}
                .empty{color:#8b9aab;font-size:14px}
                footer{margin-top:36px;font-size:12px;color:#6b7a8c}
                """;
    }
}
