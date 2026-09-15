package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.FinancialCheck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Explicación muy detallada de cada cálculo que FALLÓ, para el final del reporte.
 * Reconstruye la fórmula, el redondeo Backend → UI y las causas más probables.
 */
public final class FailureDiagnosis {

    private FailureDiagnosis() {}

    public static boolean hasFailures() {
        return !FinancialReport.failures().isEmpty();
    }

    public static String buildText() {
        List<FinancialCheck> failures = FinancialReport.failures();
        if (failures.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("=============================================================\n");
        sb.append("  DIAGNÓSTICO DETALLADO — por qué falló cada cálculo\n");
        sb.append("=============================================================\n");
        sb.append(intro(failures.size()));
        sb.append('\n');

        int n = 1;
        for (FinancialCheck fail : failures) {
            sb.append("----- Fallo ").append(n++).append(" de ").append(failures.size()).append(" -----\n");
            sb.append(explain(fail).text());
            sb.append('\n');
        }
        sb.append(closing(failures));
        return sb.toString();
    }

    public static String buildHtmlSection() {
        List<FinancialCheck> failures = FinancialReport.failures();
        if (failures.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>4. Diagnóstico detallado &nbsp;<small>por qué falló cada cálculo</small></h2>");
        html.append("<p class=\"hint\">Esta sección solo aparece cuando hay discrepancias. ")
                .append("Explica, cálculo por cálculo, qué se esperaba, cómo se redondeó, ")
                .append("qué pintó la pantalla y cuáles son las causas más probables.</p>");

        html.append("<section class=\"diagnosis-intro\">")
                .append(escape(intro(failures.size())).replace("\n", "<br>"))
                .append("</section>");

        int n = 1;
        for (FinancialCheck fail : failures) {
            Explanation exp = explain(fail);
            html.append("<article class=\"diagnosis\">")
                    .append("<h3>Fallo ").append(n++).append(" de ").append(failures.size())
                    .append(" · ").append(escape(fail.getScope())).append(" · ")
                    .append(escape(fail.getField())).append("</h3>");
            for (Block block : exp.blocks) {
                html.append("<h4>").append(escape(block.title)).append("</h4>");
                html.append("<p>").append(escape(block.body).replace("\n", "<br>")).append("</p>");
            }
            html.append("</article>");
        }

        html.append("<section class=\"diagnosis-close\">")
                .append(escape(closing(failures)).replace("\n", "<br>"))
                .append("</section>");
        return html.toString();
    }

    static Explanation explain(FinancialCheck fail) {
        String field = fail.getField() == null ? "" : fail.getField();
        String scope = fail.getScope() == null ? "" : fail.getScope();
        boolean hiddenOp = scope.toLowerCase(Locale.ROOT).contains("oculto");
        boolean isTotal = scope.toLowerCase(Locale.ROOT).startsWith("total")
                || fail.getPhase().contains("2.");
        boolean isFormula = field.toLowerCase(Locale.ROOT).startsWith("cálculo")
                || field.toLowerCase(Locale.ROOT).startsWith("calculo");

        List<Block> blocks = new ArrayList<>();
        blocks.add(new Block("Qué se estaba validando", where(fail, isTotal, hiddenOp)));
        blocks.add(new Block("Números confrontados", numbers(fail)));
        blocks.add(new Block("Regla de redondeo", roundingRule(fail)));
        blocks.add(new Block("Fórmula aplicada", formulaBlock(fail, field, hiddenOp, isTotal)));
        blocks.add(new Block("Por qué se marcó FALLÓ", whyFailed(fail)));
        blocks.add(new Block("Causas más probables", causes(fail, field, hiddenOp, isTotal, isFormula)));
        blocks.add(new Block("Cómo comprobarlo a mano", howToCheck(fail, field, hiddenOp, isTotal)));
        return new Explanation(blocks);
    }

    private static String intro(int count) {
        Map<String, String> ctx = new LinkedHashMap<>(FinancialReport.contextSnapshot());
        StringBuilder sb = new StringBuilder();
        sb.append("Se encontraron ").append(count)
                .append(count == 1 ? " cálculo que no cuadra" : " cálculos que no cuadran")
                .append(" entre el JSON de user-route (Backend) y lo pintado en la tabla Plan (Frontend).\n");
        sb.append("La automatización no se detiene en el primer error: recorre Income → Profit ")
                .append("por cada lane y después la fila Total, con Op cost visible y oculto, ")
                .append("para dejar el panorama completo.\n");
        if (!ctx.isEmpty()) {
            sb.append("Contexto de esta corrida: ");
            List<String> bits = new ArrayList<>();
            ctx.forEach((k, v) -> bits.add(k + " = " + v));
            sb.append(String.join("; ", bits)).append(".\n");
        }
        sb.append("Recuerde: el Backend habla en milésimas (ej. 312.782) y la UI muestra el entero ")
                .append("más cercano ($313). Un desfase de ±1 o ±2 dólares suele ser redondeo de frontera ")
                .append("y la automatización ya lo tolera; si un renglón está en rojo, la diferencia ")
                .append("superó esa tolerancia y hay que explicar de dónde salió.\n");
        return sb.toString();
    }

    private static String where(FinancialCheck fail, boolean isTotal, boolean hiddenOp) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fase: ").append(empty(fail.getPhase())).append(".\n");
        sb.append("Ámbito: ").append(empty(fail.getScope())).append(".\n");
        sb.append("Campo: ").append(empty(fail.getField())).append(".\n");
        if (isTotal) {
            sb.append("Esto no es un tramo suelto: es la fila Total al pie de la tabla, ")
                    .append("que debe cuadrar con el bloque finance de la ruta (y, si Op está oculto, ")
                    .append("con la suma de las lanes sin Op).\n");
        } else {
            sb.append("Es un valor de un tramo (lane) concreto. El origen/destino y el tipo ")
                    .append("[loaded / deadhead] aparecen en el ámbito si el Backend los mandó.\n");
        }
        if (hiddenOp) {
            sb.append("En esta pasada el icono del ojo HABÍA OCULTADO la columna Op cost. ")
                    .append("La UI debe recalcular Total cost y Profit SIN sumar ni restar Op. ")
                    .append("El finance del Backend sí incluye Op: por eso no se usa como referencia directa.\n");
        } else {
            sb.append("En esta pasada el Op cost estaba VISIBLE: entra en el Total cost de la lane ")
                    .append("(tarifa × millas) y se resta del Profit de la lane. ")
                    .append("En la fila Total, finance.totalCost ya trae el Op; finance.profit del ")
                    .append("endpoint NO lo resta (asimetría conocida del producto).\n");
        }
        return sb.toString();
    }

    private static String numbers(FinancialCheck fail) {
        StringBuilder sb = new StringBuilder();
        sb.append("Backend (milésimas del endpoint): ").append(fail.getBackendValue()).append(".\n");
        sb.append("Ese mismo valor redondeado como debería verse en pantalla: ")
                .append(fail.getRoundedValue()).append(".\n");
        sb.append("Frontend (texto leído de la celda): ").append(fail.getFrontendValue()).append(".\n");
        sb.append("Diferencia (UI − redondeo Backend): ").append(fail.getDifference()).append(".\n");
        if (!fail.getDetail().isBlank()) {
            sb.append("Nota registrada en el momento de comparar: ").append(fail.getDetail()).append(".\n");
        }
        sb.append("Si el Frontend dice «no leído» o «columna no visible», el problema no es la fórmula ")
                .append("sino que no se pudo extraer el número de la tabla (mapa abierto, selector o DOM distinto).\n");
        return sb.toString();
    }

    private static String roundingRule(FinancialCheck fail) {
        return "efRouting pinta enteros. La regla de la automatización es Math.round sobre el valor "
                + "del Backend (312.782 → $313; 1125.540 → $1,126). Luego se compara con lo leído "
                + "en pantalla con una tolerancia de unos ±2 dólares para absorber fronteras de "
                + "redondeo al agregar lanes. Los costos por lane en UI suelen ir en negativo "
                + "(salida de dinero) aunque el JSON los mande positivos: se compara la magnitud, "
                + "no el signo. Valor Backend=" + fail.getBackendValue()
                + " → redondeo esperado=" + fail.getRoundedValue()
                + " → UI=" + fail.getFrontendValue() + ".\n";
    }

    private static String formulaBlock(FinancialCheck fail, String field, boolean hiddenOp, boolean isTotal) {
        StringBuilder sb = new StringBuilder();
        if (!fail.getFormula().isBlank()) {
            sb.append("Fórmula anotada en esta comparación:\n").append(fail.getFormula()).append("\n");
        }
        String key = normalizeField(field);
        switch (key) {
            case "income" -> sb.append("Income no se calcula aquí: se toma finance.income.min/max ")
                    .append("del Backend y se redondea cada extremo. La UI muestra un rango ")
                    .append("\"mín – máx\". Si un extremo falla, suele ser pintado distinto o un ")
                    .append("rango colapsado a un solo número.\n");
            case "fuel", "toll", "custom" -> sb.append("Este costo sale directo de finance.")
                    .append(key.equals("fuel") ? "fuelCost" : key.equals("toll") ? "tollCost" : "customCost")
                    .append(". No hay suma intermedia. Custom con botón \"Add +\" se trata como $0 ")
                    .append("porque nadie cargó un importe manual.\n");
            case "op" -> sb.append("Op cost de una lane = tarifa (operativeCost.total, $ / mi) × millaje ")
                    .append("estimado de esa lane. En la fila Total la celda muestra la Σ de esos Op ")
                    .append("(tarifa × millaje total), no la tarifa suelta.\n");
            case "total" -> {
                if (hiddenOp) {
                    sb.append("Total cost esperado (Op oculto) = Fuel + Toll + Custom, ")
                            .append(isTotal ? "sumado across todas las lanes." : "de esta lane.")
                            .append(" El finance.totalCost del Backend SÍ incluye Op y por eso no se usa.\n");
                } else {
                    sb.append("Total cost esperado (Op visible) = Fuel + Toll + Custom + Op. ")
                            .append(isTotal
                                    ? "En Total, finance.totalCost del Backend ya trae el Op incluido.\n"
                                    : "En la lane, finance.totalCost del Backend NO trae Op; la UI lo suma.\n");
                }
            }
            case "profit" -> {
                if (hiddenOp) {
                    sb.append("Profit esperado (Op oculto) = Income − (Fuel + Toll + Custom). ")
                            .append(isTotal ? "En Total se suma el Profit de cada lane sin Op (mín y máx por separado).\n"
                                    : "En la lane: cada extremo de Income menos el Total cost sin Op.\n");
                } else {
                    sb.append("Profit esperado (Op visible) = Income − Total cost (con Op). ")
                            .append("Ojo: finance.profit del endpoint, a nivel ruta, NO resta el Op; ")
                            .append("la automatización recalcula Income − Total cost para no dar un falso OK.\n");
                }
            }
            default -> sb.append("Comparación directa Backend redondeado vs celda de pantalla.\n");
        }
        return sb.toString();
    }

    private static String whyFailed(FinancialCheck fail) {
        String fe = fail.getFrontendValue() == null ? "" : fail.getFrontendValue().toLowerCase(Locale.ROOT);
        if (fe.contains("no leído") || fe.contains("no leido")) {
            return "No hubo número de Frontend contra el que comparar. La aserción falla porque "
                    + "la celda no devolvió un importe parseable. Sin ese dato no se puede decir "
                    + "si la fórmula del producto está mal: primero hay que lograr leer la tabla.\n";
        }
        if (fe.contains("no visible") || fe.contains("no disponible")) {
            return "La columna no estaba en el DOM. Con el mapa abierto efRouting comprime la tabla "
                    + "y deja de renderizar Fuel, Toll, Custom, Op cost y Profit. Hay que cerrar el "
                    + "mapa (hamburguesa junto a Edit) antes de validar.\n";
        }
        String detail = fail.getDetail();
        if (detail != null && detail.contains("Posible bug de la app")) {
            return detail + "\nEn criollo: al tachar el ojo, el Profit de la fila Total subió "
                    + "exactamente el Σ Op cost, como si la app lo sumara en vez de dejarlo quieto "
                    + "o recalcularlo sin Op. El Backend nunca restaba Op del profit de ruta, "
                    + "así que ese salto no tiene respaldo en el JSON.\n";
        }
        return "El entero (o el rango) que se ve en pantalla no coincide con el Backend "
                + "después de redondear por milésimas, ni cae dentro de la tolerancia de ±2. "
                + "Diferencia reportada: " + fail.getDifference() + ". "
                + (detail == null || detail.isBlank() ? "" : "Detalle del check: " + detail + " ")
                + "Eso implica que o el producto pintó otro número, o usó otra fórmula "
                + "(por ejemplo metió/sacó el Op cost donde no debía), o el JSON y la UI "
                + "están hablando de agregaciones distintas.\n";
    }

    private static String causes(FinancialCheck fail, String field, boolean hiddenOp,
                                 boolean isTotal, boolean isFormula) {
        StringBuilder sb = new StringBuilder();
        String key = normalizeField(field);
        String diff = fail.getDifference();

        if (fail.getFrontendValue() != null && fail.getFrontendValue().toLowerCase(Locale.ROOT).contains("no le")) {
            sb.append("1) El mapa sigue abierto o la tabla aún no terminó de hidratar las columnas.\n");
            sb.append("2) Cambió el data-column-id / data-cy de la celda (ver FinancialColumn y el DOM en target/diagnostico).\n");
            sb.append("3) La ruta no tiene esa columna para este tipo de lane (menos frecuente).\n");
            return sb.toString();
        }

        int i = 1;
        if ("income".equals(key)) {
            sb.append(i++).append(") La UI está mostrando un solo número en vez del rango min–max, o al revés.\n");
            sb.append(i++).append(") Se pintó finance.income.avg en lugar de min/max.\n");
            sb.append(i++).append(") En Total, el Backend agregó mal las lanes (entonces fallará también la suma de Incomes por tramo).\n");
        }
        if ("fuel".equals(key) || "toll".equals(key) || "custom".equals(key)) {
            sb.append(i++).append(") El Frontend muestra un costo distinto al finance de esa lane: o se recargó otro valor, o se está leyendo la celda de al lado.\n");
            sb.append(i++).append(") Custom cost: si se ve \"Add +\" debe ser $0; si alguien cargó un extra y el JSON no lo trae (o viceversa), no van a coincidir.\n");
            sb.append(i++).append(") En Total, si las lanes individuales están OK pero el Total no, el Backend está agregando mal ese costo.\n");
        }
        if ("op".equals(key)) {
            sb.append(i++).append(") La tarifa ($/mi) × millas de la lane no da lo que pinta la celda: o cambió el millaje en pantalla, o la tarifa no es operativeCost.total.\n");
            sb.append(i++).append(") En Total, la UI a veces muestra la tarifa suelta y a veces la Σ; la automatización espera la Σ (tarifa × millaje total).\n");
            sb.append(i++).append(") Deadhead vs loaded: si una lane no debería cobrar Op y la UI igual lo pone (o al revés), la diferencia será exactamente tarifa × millas de esa lane.\n");
        }
        if ("total".equals(key) || isFormula && field.toLowerCase(Locale.ROOT).contains("total")) {
            sb.append(i++).append(") Algún componente (Fuel, Toll, Custom u Op) ya está mal: el Total hereda el error. Revise primero esas filas del mismo ámbito.\n");
            if (hiddenOp) {
                sb.append(i++).append(") Op oculto: si el Total de pantalla sigue incluyendo el Op, la diferencia será ≈ Σ Op cost. La app no está recalculando al tachar el ojo.\n");
            } else {
                sb.append(i++).append(") Op visible: si el Total de pantalla es Fuel+Toll+Custom SIN Op, la diferencia será ≈ Op cost. La UI no lo está sumando.\n");
            }
            sb.append(i++).append(") Redondeo por componente vs redondeo de la suma: la UI puede redondear cada costo y luego sumar, en vez de sumar milésimas y redondear una vez.\n");
        }
        if ("profit".equals(key) || isFormula && field.toLowerCase(Locale.ROOT).contains("profit")) {
            sb.append(i++).append(") Profit = Income − Total cost. Si el Total cost ya falló, el Profit fallará por la misma diferencia (con signo contrario).\n");
            if (hiddenOp) {
                sb.append(i++).append(") Bug conocido (fila Total, Op oculto): el Profit sube exactamente el Σ Op cost. Si la diferencia coincide con ese importe, no es un error de la automatización: es la app sumando Op al Profit al ocultarlo.\n");
            } else {
                sb.append(i++).append(") Si se comparara contra finance.profit de la ruta (que no resta Op) daría otro número; aquí se usa Income − Total cost a propósito.\n");
            }
            sb.append(i++).append(") Un extremo del rango (mín o máx) puede estar bien y el otro no: mire la diferencia \"mín / máx\".\n");
        }
        if (field.toLowerCase(Locale.ROOT).contains("cantidad")) {
            sb.append(i++).append(") El JSON trae más/menos lanes de las filas de la tabla: lanes colapsadas, deadheads ocultos o un response viejo.\n");
        }
        sb.append(i++).append(") El endpoint interceptado no es el del detalle (se eligió otra llamada user-route). Revise target/diagnostico/user-route.json vs user-route-todas.txt.\n");
        sb.append(i++).append(") Condición de carrera: la tabla se leyó antes de que React terminara de pintar los importes. Menos probable si hay captura de pantalla coherente.\n");
        if (diff != null && !diff.isBlank() && !diff.equals("—") && !diff.equals("0")) {
            sb.append("La diferencia numérica (").append(diff)
                    .append(") es la pista más útil: compárela con Fuel, Toll, Custom, Op y con Σ Op cost del contexto.\n");
        }
        return sb.toString();
    }

    private static String howToCheck(FinancialCheck fail, String field, boolean hiddenOp, boolean isTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("1) Abra target/diagnostico/user-route.json y localice ");
        if (isTotal) {
            sb.append("el bloque finance de la ruta (income, fuelCost, tollCost, customCost, totalCost, profit, operativeCost).\n");
        } else {
            sb.append("la lane de este ámbito (finance + operative.mileage.estimated).\n");
        }
        sb.append("2) Anote los importes con milésimas y aplique Math.round a cada uno; eso es lo que debería verse.\n");
        sb.append("3) En la captura del reporte (sección 3) lea la misma celda y contraste.\n");
        String key = normalizeField(field);
        if ("total".equals(key) || "profit".equals(key) || field.toLowerCase(Locale.ROOT).contains("cálculo")
                || field.toLowerCase(Locale.ROOT).contains("calculo")) {
            sb.append("4) Recalcule a mano: Fuel + Toll + Custom");
            sb.append(hiddenOp ? " (sin Op)" : " + (tarifa × millas)");
            sb.append(" = Total cost. Luego Income − Total cost = Profit.\n");
        } else if ("op".equals(key)) {
            sb.append("4) Multiplique operativeCost.total por el millaje de la lane (o la suma de millajes en Total).\n");
        } else {
            sb.append("4) No hay suma: el campo del JSON redondeado debe ser idéntico a la celda.\n");
        }
        sb.append("5) Si solo falla con Op oculto, pulse el ojo usted mismo y mire si Total/Profit cambian exactamente el Op cost.\n");
        sb.append("6) Si el JSON y la pantalla coinciden al ojo pero el reporte dice FALLÓ, revise el parseo ")
                .append("(signo, rango \"$4 -$374\", miles con coma) en MoneyParser y el DOM de ")
                .append("target/diagnostico/dom-validacion-lanes.html.\n");
        sb.append("Valores de este fallo: Backend ").append(fail.getBackendValue())
                .append(" → UI esperada ").append(fail.getRoundedValue())
                .append(" vs UI leída ").append(fail.getFrontendValue()).append(".\n");
        return sb.toString();
    }

    private static String closing(List<FinancialCheck> failures) {
        Map<String, Long> byField = new LinkedHashMap<>();
        for (FinancialCheck fail : failures) {
            String field = fail.getField() == null ? "(sin campo)" : fail.getField();
            byField.merge(field, 1L, Long::sum);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Resumen de fallos por campo: ");
        List<String> bits = new ArrayList<>();
        byField.forEach((k, v) -> bits.add(k + " × " + v));
        sb.append(String.join(", ", bits)).append(".\n");
        sb.append("Lectura recomendada: si fallan Fuel/Toll/Custom y también Total y Profit del mismo tramo, ")
                .append("arregle el componente; el resto son consecuencias. Si los componentes están OK ")
                .append("y solo fallan Total o Profit, la app está armando mal la fórmula (casi siempre Op cost).\n");
        sb.append("Archivos útiles: target/reportes/reporte-calculos-*-ultimo.html (esta página), ")
                .append("target/diagnostico/user-route.json, user-route-todas.txt, ")
                .append("dom-validacion-lanes.html y las capturas embebidas más arriba.\n");
        return sb.toString();
    }

    private static String normalizeField(String field) {
        if (field == null) {
            return "";
        }
        String f = field.toLowerCase(Locale.ROOT);
        if (f.contains("income")) {
            return "income";
        }
        if (f.contains("fuel")) {
            return "fuel";
        }
        if (f.contains("toll")) {
            return "toll";
        }
        if (f.contains("custom")) {
            return "custom";
        }
        if (f.contains("op cost") || f.equals("op cost") || f.startsWith("op ")) {
            return "op";
        }
        if (f.contains("total")) {
            return "total";
        }
        if (f.contains("profit")) {
            return "profit";
        }
        return f;
    }

    private static String empty(String value) {
        return value == null || value.isBlank() ? "—" : value;
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

    static final class Explanation {
        final List<Block> blocks;

        Explanation(List<Block> blocks) {
            this.blocks = blocks;
        }

        String text() {
            StringBuilder sb = new StringBuilder();
            for (Block block : blocks) {
                sb.append(block.title).append('\n');
                sb.append(block.body);
                if (!block.body.endsWith("\n")) {
                    sb.append('\n');
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    static final class Block {
        final String title;
        final String body;

        Block(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}
