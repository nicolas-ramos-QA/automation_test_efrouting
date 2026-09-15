package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.FinancialCheck;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Acumula comparaciones Backend ↔ Frontend y genera un HTML claro:
 * primero por cada lane (Income → Profit), después la fila Total.
 * Se abre automáticamente en el navegador al finalizar cada ambiente.
 */
public final class FinancialReport {

    private static final String OUTPUT_DIR = "target/reportes";
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter HUMAN_STAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final List<FinancialCheck> CHECKS = new ArrayList<>();
    private static final Map<String, String> CONTEXT = new LinkedHashMap<>();
    private static final Map<String, String> SCREENSHOTS = new LinkedHashMap<>();
    private static String SLUG;

    private FinancialReport() {}

    public static synchronized void reset() {
        CHECKS.clear();
        CONTEXT.clear();
        SCREENSHOTS.clear();
        SLUG = null;
    }

    public static synchronized void addContext(String key, String value) {
        CONTEXT.put(key, value);
    }

    /**
     * Guarda una captura de pantalla (PNG en base64) para incrustarla al final del reporte HTML.
     * Si ya existe una captura con la misma etiqueta, se reemplaza por la más reciente.
     */
    public static synchronized void addScreenshot(String label, String base64Png) {
        if (label == null || label.isBlank() || base64Png == null || base64Png.isBlank()) {
            return;
        }
        SCREENSHOTS.put(label, base64Png);
    }

    public static synchronized void record(FinancialCheck check) {
        CHECKS.add(check);
    }

    public static synchronized List<FinancialCheck> checks() {
        return new ArrayList<>(CHECKS);
    }

    /** Copia del contexto de la corrida (ambiente, URL, ruta, etc.) para el diagnóstico. */
    public static synchronized Map<String, String> contextSnapshot() {
        return new LinkedHashMap<>(CONTEXT);
    }

    public static synchronized boolean isEmpty() {
        return CHECKS.isEmpty();
    }

    public static long countBy(FinancialCheck.Status status) {
        return checks().stream().filter(c -> c.getStatus() == status).count();
    }

    public static List<FinancialCheck> failures() {
        return checks().stream().filter(FinancialCheck::isFailure).collect(Collectors.toList());
    }

    public static synchronized void useSlug(String slug) {
        SLUG = slug;
    }

    /**
     * Añade un sufijo al slug actual (p.ej. el tipo de ruta elegido: "qa" → "qa-tri-hauls").
     * Permite que cada combinación ambiente + tipo de ruta genere su propio archivo "-ultimo".
     */
    public static synchronized void appendSlug(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return;
        }
        String base = SLUG == null ? "general" : SLUG;
        SLUG = base + "-" + suffix;
    }

    public static String textSummary() {
        List<FinancialCheck> all = checks();
        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("  REPORTE Backend vs Frontend (redondeo por milésimas → UI)\n");
        sb.append("=============================================================\n");
        sb.append("Fecha: ").append(LocalDateTime.now().format(HUMAN_STAMP)).append('\n');
        CONTEXT.forEach((k, v) -> sb.append(k).append(": ").append(v).append('\n'));
        sb.append("-------------------------------------------------------------\n");
        sb.append("Fórmulas:  Total cost = Fuel + Toll + Custom [+ Op si visible]\n");
        sb.append("            Profit     = Income − Total cost\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("OK: ").append(countBy(FinancialCheck.Status.PASS))
                .append("   FALLÓ: ").append(countBy(FinancialCheck.Status.FAIL))
                .append("   AVISO: ").append(countBy(FinancialCheck.Status.WARN))
                .append("   OMITIDO: ").append(countBy(FinancialCheck.Status.SKIP))
                .append("   Total: ").append(all.size()).append('\n');
        sb.append("-------------------------------------------------------------\n\n");

        String currentScope = null;
        for (FinancialCheck check : all) {
            if (!check.getScope().equals(currentScope)) {
                currentScope = check.getScope();
                sb.append("\n▸ ").append(currentScope).append('\n');
            }
            sb.append(String.format("  [%s] %-22s  Backend: %-28s  → UI: %-18s  Frontend: %s%n",
                    check.getStatus().getLabel(),
                    check.getField(),
                    check.getBackendValue(),
                    check.getRoundedValue(),
                    check.getFrontendValue()));
            if (!check.getFormula().isBlank()) {
                sb.append("           fórmula: ").append(check.getFormula()).append('\n');
            }
        }

        sb.append('\n');
        if (failures().isEmpty()) {
            sb.append("RESULTADO: Backend y Frontend coinciden (con redondeo por milésimas).\n");
        } else {
            sb.append("RESULTADO: ").append(failures().size()).append(" discrepancia(s):\n");
            failures().forEach(f -> sb.append("  - ").append(f).append('\n'));
            sb.append(FailureDiagnosis.buildText());
        }
        return sb.toString();
    }

    public static Path writeHtml() {
        try {
            Path dir = Paths.get(OUTPUT_DIR);
            Files.createDirectories(dir);
            String prefix = "reporte-calculos-" + reportSlug();
            String html = buildHtml();
            Path file = dir.resolve(prefix + "-" + LocalDateTime.now().format(FILE_STAMP) + ".html");
            Files.writeString(file, html, StandardCharsets.UTF_8);
            Files.writeString(dir.resolve(prefix + "-ultimo.html"), html, StandardCharsets.UTF_8);
            return file.toAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir el reporte de cálculos", e);
        }
    }

    /** Escribe el HTML y lo abre en el navegador predeterminado del sistema. */
    public static Path writeHtmlAndOpen() {
        Path file = writeHtml();
        openInBrowser(file);
        return file;
    }

    public static void openInBrowser(Path file) {
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
            // No tumbar el test si el SO no permite abrir el navegador
            System.err.println("No se pudo abrir el reporte en el navegador: " + e.getMessage());
        }
    }

    private static synchronized String reportSlug() {
        return SLUG == null ? "general" : SLUG;
    }

    private static String buildHtml() {
        long ok = countBy(FinancialCheck.Status.PASS);
        long fail = countBy(FinancialCheck.Status.FAIL);
        boolean success = fail == 0;

        Map<String, List<FinancialCheck>> lanes = new LinkedHashMap<>();
        Map<String, List<FinancialCheck>> totals = new LinkedHashMap<>();
        for (FinancialCheck check : checks()) {
            String scope = check.getScope();
            if (scope.startsWith("Total") || check.getPhase().contains("2.")) {
                totals.computeIfAbsent(scope, k -> new ArrayList<>()).add(check);
            } else {
                lanes.computeIfAbsent(scope, k -> new ArrayList<>()).add(check);
            }
        }

        String ambiente = CONTEXT.getOrDefault("Ambiente", reportSlug().toUpperCase(Locale.ROOT));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>Cálculos financieros — ").append(escape(ambiente)).append("</title>")
                .append("<style>").append(css()).append("</style></head><body>");

        html.append("<header>")
                .append("<div class=\"brand\">efRouting · validación financiera</div>")
                .append("<h1>Backend vs Frontend</h1>")
                .append("<p class=\"subtitle\">Redondeo por milésimas → entero de la UI &nbsp;·&nbsp; Ambiente: <b>")
                .append(escape(ambiente)).append("</b></p>")
                .append("</header>");

        html.append("<section class=\"summary\">");
        CONTEXT.forEach((k, v) -> html.append("<div><span>").append(escape(k))
                .append("</span><b>").append(escape(v)).append("</b></div>"));
        html.append("</section>");

        html.append("<section class=\"cards\">")
                .append(card("ok", String.valueOf(ok), "Coinciden"))
                .append(card(fail == 0 ? "ok" : "bad", String.valueOf(fail), "No coinciden"))
                .append(card("", String.valueOf(checks().size()), "Comparaciones"))
                .append("</section>");

        html.append("<div class=\"banner ").append(success ? "ok" : "bad").append("\">")
                .append(success
                        ? "Todos los valores del Backend coinciden con el Frontend (aplicando redondeo por milésimas)."
                        : fail + " valor(es) no coinciden. Revise las filas en rojo abajo.")
                .append("</div>");

        html.append("<section class=\"formulas\">")
                .append("<h2 class=\"inline\">Cómo se valida</h2>")
                .append("<ul>")
                .append("<li><b>Por lane y en Total:</b> cada campo del endpoint (Income → Profit) se redondea por milésimas y se compara con la pantalla.</li>")
                .append("<li><code>Total cost</code> = Fuel + Toll + Custom + Op cost &nbsp;")
                .append("<em>(Op = tarifa × millas en lanes; en Total el Backend ya lo trae en <code>finance.totalCost</code>)</em></li>")
                .append("<li><code>Profit</code> = Income − Total cost &nbsp;")
                .append("<em>(en la fila Total se compara el <code>finance.profit</code> del endpoint)</em></li>")
                .append("<li>Custom cost con botón <b>Add +</b> = <b>$0</b></li>")
                .append("<li>Ejemplo de redondeo: Backend <code>312.782</code> → UI <code>$313</code></li>")
                .append("<li>El icono del ojo <b>oculta la columna</b> Op cost; ahí Total cost y Profit se recalculan como la suma de las lanes SIN Op (ya no coinciden con el finance del Backend, que sí lo incluye).</li>")
                .append("</ul>")
                .append("<div class=\"known-issue\"><b>⚠ Problema conocido (fila Total · Op cost oculto):</b> "
                        + "el Profit en pantalla puede subir exactamente el Σ Op cost de la ruta, como si la app "
                        + "lo sumara por error al ocultar la columna (el Profit del Backend nunca restaba Op, "
                        + "ni con Op visible). Se deja marcado como FALLÓ a propósito para seguir reportando "
                        + "esta inconsistencia; revise la fila \"Profit\" de esa sección para el detalle numérico "
                        + "de cada corrida.</div>")
                .append("</section>");

        html.append("<h2>1. Por cada lane &nbsp;<small>Income → Profit</small></h2>");
        html.append("<p class=\"hint\">Comparación campo a campo del endpoint <code>user-route</code> ")
                .append("contra lo renderizado en cada fila de la tabla Plan.</p>");
        if (lanes.isEmpty()) {
            html.append("<p class=\"empty\">Sin lanes validadas.</p>");
        } else {
            int n = 1;
            for (Map.Entry<String, List<FinancialCheck>> e : lanes.entrySet()) {
                appendScopeTable(html, e.getKey(), e.getValue(), "lane-" + n++);
            }
        }

        html.append("<h2>2. Total (fila inferior) &nbsp;<small>Income → Profit</small></h2>");
        html.append("<p class=\"hint\">Valores generales de la ruta: bloque <code>finance</code> del Backend ")
                .append("vs la fila <b>Total</b> al pie de la tabla.</p>");
        if (totals.isEmpty()) {
            html.append("<p class=\"empty\">Sin fila Total validada.</p>");
        } else {
            totals.forEach((scope, rows) -> appendScopeTable(html, scope, rows, "total"));
        }

        if (!SCREENSHOTS.isEmpty()) {
            html.append("<h2>3. Capturas de pantalla &nbsp;<small>Op cost visible / oculto</small></h2>");
            html.append("<p class=\"hint\">Estado real de la tabla en el navegador al momento de validar cada escenario.</p>");
            html.append("<section class=\"screenshots\">");
            SCREENSHOTS.forEach((label, base64) -> html.append("<figure class=\"shot\">")
                    .append("<figcaption>").append(escape(label)).append("</figcaption>")
                    .append("<img src=\"data:image/png;base64,").append(base64).append("\" alt=\"")
                    .append(escape(label)).append("\"/>")
                    .append("</figure>"));
            html.append("</section>");
        }

        html.append(FailureDiagnosis.buildHtmlSection());

        html.append("<footer>Generado automáticamente el ")
                .append(escape(LocalDateTime.now().format(HUMAN_STAMP)))
                .append(" · archivo: reporte-calculos-").append(escape(reportSlug())).append("-ultimo.html")
                .append("</footer></body></html>");
        return html.toString();
    }

    private static void appendScopeTable(StringBuilder html, String scope,
                                         List<FinancialCheck> rows, String id) {
        boolean anyFail = rows.stream().anyMatch(FinancialCheck::isFailure);
        html.append("<article class=\"block").append(anyFail ? " has-fail" : "").append("\" id=\"")
                .append(escape(id)).append("\">")
                .append("<h3>").append(escape(scope)).append("</h3>")
                .append("<table><thead><tr>")
                .append("<th>Estado</th>")
                .append("<th>Campo</th>")
                .append("<th>Backend <small>(milésimas del endpoint)</small></th>")
                .append("<th>→ Redondeo UI</th>")
                .append("<th>Frontend <small>(pantalla)</small></th>")
                .append("<th>Dif.</th>")
                .append("</tr></thead><tbody>");

        for (FinancialCheck check : rows) {
            boolean isFormula = check.getField().startsWith("Cálculo");
            html.append("<tr class=\"").append(check.getStatus().name())
                    .append(isFormula ? " formula-row" : "").append("\">")
                    .append("<td><span class=\"tag ").append(check.getStatus().name()).append("\">")
                    .append(check.getStatus().getLabel()).append("</span></td>")
                    .append("<td class=\"field\">").append(escape(check.getField()));
            if (!check.getFormula().isBlank()) {
                html.append("<div class=\"formula\">").append(escape(check.getFormula())).append("</div>");
            }
            if (!check.getDetail().isBlank() && !check.getDetail().equals(check.getFormula())) {
                html.append("<div class=\"note\">").append(escape(check.getDetail())).append("</div>");
            }
            html.append("</td>")
                    .append("<td class=\"num be\">").append(escape(check.getBackendValue())).append("</td>")
                    .append("<td class=\"num rounded\">").append(escape(check.getRoundedValue())).append("</td>")
                    .append("<td class=\"num fe\">").append(escape(check.getFrontendValue())).append("</td>")
                    .append("<td class=\"num\">").append(escape(check.getDifference())).append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table></article>");
    }

    private static String css() {
        return """
                *{box-sizing:border-box}
                body{margin:0;padding:28px 36px 56px;font-family:'Segoe UI',system-ui,sans-serif;
                     background:#f4f6f9;color:#15202b;line-height:1.45}
                header{margin-bottom:22px}
                .brand{font-size:12px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;color:#0d9488;margin-bottom:6px}
                h1{margin:0;font-size:28px;font-weight:750;letter-spacing:-.03em}
                .subtitle{margin:6px 0 0;color:#5b6575;font-size:14px}
                h2{margin:40px 0 8px;font-size:20px;color:#0f172a;border-bottom:2px solid #dbe3ee;padding-bottom:8px}
                h2 small{font-size:13px;font-weight:500;color:#64748b}
                h2.inline{border:none;margin:0 0 8px;font-size:15px;padding:0}
                h3{margin:0 0 12px;font-size:15px;font-weight:650;color:#1e293b}
                .hint{margin:0 0 14px;font-size:13px;color:#64748b}
                .summary{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:8px 18px;
                         background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:14px 18px;margin-bottom:16px;font-size:13px}
                .summary span{color:#64748b;display:block;font-size:11px;text-transform:uppercase;letter-spacing:.04em}
                .summary b{font-weight:600;word-break:break-word}
                .cards{display:flex;gap:12px;flex-wrap:wrap;margin-bottom:16px}
                .card{background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:14px 22px;min-width:120px}
                .card .n{font-size:30px;font-weight:750;line-height:1}
                .card .l{font-size:11px;color:#64748b;text-transform:uppercase;margin-top:4px;letter-spacing:.04em}
                .card.ok .n{color:#15803d}.card.bad .n{color:#b91c1c}
                .banner{padding:12px 16px;border-radius:10px;font-weight:650;font-size:14px;margin-bottom:14px}
                .banner.ok{background:#dcfce7;color:#166534;border:1px solid #86efac}
                .banner.bad{background:#fee2e2;color:#991b1b;border:1px solid #fca5a5}
                .formulas{background:#fff;border:1px solid #e2e8f0;border-radius:12px;padding:14px 18px;margin-bottom:8px}
                .formulas ul{margin:0;padding-left:18px;font-size:13px;color:#334155}
                .formulas li{margin:4px 0}
                .formulas code{background:#f1f5f9;padding:1px 6px;border-radius:4px;font-size:12px}
                .known-issue{margin-top:10px;padding:10px 14px;background:#fffbeb;border:1px solid #fde68a;
                             border-radius:8px;font-size:12.5px;color:#92400e;line-height:1.5}
                .block{background:#fff;border:1px solid #e2e8f0;border-radius:14px;padding:16px 18px;margin-bottom:18px;
                       box-shadow:0 1px 2px rgba(15,23,42,.04)}
                .block.has-fail{border-color:#fca5a5}
                table{width:100%;border-collapse:collapse;font-size:13px}
                th{text-align:left;padding:9px 10px;background:#f1f5f9;color:#475569;font-size:11px;
                   text-transform:uppercase;letter-spacing:.04em;border-bottom:1px solid #e2e8f0}
                th small{display:block;text-transform:none;letter-spacing:0;color:#94a3b8;font-weight:400;margin-top:2px}
                td{padding:10px;border-top:1px solid #f1f5f9;vertical-align:top}
                td.num{font-family:Consolas,'Cascadia Mono','SF Mono',monospace;white-space:nowrap;font-size:12.5px}
                td.be{color:#0f172a}
                td.rounded{color:#0369a1;font-weight:700}
                td.fe{color:#0f766e;font-weight:600}
                td.field{font-weight:600;min-width:150px}
                .formula{font-family:Consolas,monospace;font-size:11px;color:#4f46e5;font-weight:400;margin-top:4px;line-height:1.35}
                .note{font-size:11px;color:#64748b;margin-top:2px;font-weight:400}
                .tag{display:inline-block;padding:2px 9px;border-radius:999px;font-size:11px;font-weight:700}
                .tag.PASS{background:#dcfce7;color:#15803d}
                .tag.FAIL{background:#fee2e2;color:#b91c1c}
                .tag.WARN{background:#fef3c7;color:#b45309}
                .tag.SKIP{background:#f1f5f9;color:#64748b}
                tr.FAIL{background:#fef2f2}
                tr.formula-row{background:#f8fafc}
                tr.formula-row td.field{color:#4338ca}
                .empty{color:#94a3b8;font-size:13px}
                .screenshots{display:flex;flex-wrap:wrap;gap:20px;margin-bottom:20px}
                .shot{background:#fff;border:1px solid #e2e8f0;border-radius:14px;padding:14px;
                      flex:1 1 460px;max-width:100%;box-shadow:0 1px 2px rgba(15,23,42,.04)}
                .shot figcaption{font-weight:650;font-size:13px;color:#1e293b;margin-bottom:10px}
                .shot img{width:100%;border-radius:8px;border:1px solid #e2e8f0;display:block}
                footer{margin-top:36px;font-size:12px;color:#94a3b8}
                .diagnosis-intro,.diagnosis-close{background:#fff7ed;border:1px solid #fdba74;border-radius:12px;
                    padding:14px 18px;margin:0 0 16px;font-size:13.5px;color:#7c2d12;line-height:1.55}
                .diagnosis-close{background:#f8fafc;border-color:#cbd5e1;color:#334155}
                .diagnosis{background:#fff;border:1px solid #fca5a5;border-radius:14px;padding:16px 18px 8px;
                    margin-bottom:16px;box-shadow:0 1px 2px rgba(185,28,28,.06)}
                .diagnosis h3{margin:0 0 10px;font-size:16px;color:#991b1b}
                .diagnosis h4{margin:14px 0 4px;font-size:13px;text-transform:uppercase;letter-spacing:.04em;color:#9a3412}
                .diagnosis p{margin:0 0 8px;font-size:13.5px;color:#1e293b;line-height:1.55}
                """;
    }

    private static String card(String modifier, String number, String label) {
        return "<div class=\"card " + modifier + "\"><div class=\"n\">" + number
                + "</div><div class=\"l\">" + label + "</div></div>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
