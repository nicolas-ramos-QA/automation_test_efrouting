package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.FinancialCheck;
import com.calculos_de_rutas.models.FinancialColumn;
import com.calculos_de_rutas.models.LaneUiValues;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialReportTest {

    private static final String JSON = """
            {
              "id": "2364",
              "name": "Ruta de prueba",
              "operativeCost": { "id": 64, "total": 2 },
              "operative": { "mileage": { "estimated": 150 } },
              "finance": {
                "income": { "min": 1000.400, "max": 1200.600, "avg": 1100.500 },
                "profit": { "min": 579.800, "max": 780.000, "avg": 679.900 },
                "netProfit": { "min": 279.800, "max": 480.000, "avg": 379.900 },
                "fuelCost": 370.400, "tollCost": 40.200, "customCost": 10.000,
                "operativeCost": 2, "totalCost": 720.600
              },
              "lanes": [
                {
                  "laneId": "11281", "type": "loaded",
                  "origin": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                  "destination": { "location": { "city_name": "Effingham", "state_code": "IL" } },
                  "operative": { "mileage": { "estimated": 100 } },
                  "finance": {
                    "income": { "min": 1000.400, "max": 1200.600, "avg": 1100.500 },
                    "profit": { "min": 699.800, "max": 900.000, "avg": 799.900 },
                    "fuelCost": 250.400, "tollCost": 40.200, "customCost": 10.000, "totalCost": 300.600
                  }
                },
                {
                  "laneId": "11282", "type": "deadhead",
                  "origin": { "location": { "city_name": "Effingham", "state_code": "IL" } },
                  "destination": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                  "operative": { "mileage": { "estimated": 50 } },
                  "finance": {
                    "income": { "min": 0, "max": 0, "avg": 0 },
                    "profit": { "min": -120.000, "max": -120.000, "avg": -120.000 },
                    "fuelCost": 120.000, "tollCost": 0, "customCost": 0, "totalCost": 120.000
                  }
                }
              ]
            }
            """;

    @BeforeEach
    void limpiar() {
        FinancialReport.reset();
    }

    @Test
    void reporteLimpioPorLaneYTotal() {
        RouteFinancialPayload payload = RouteFinancialParser.parse(JSON);
        FinancialReport.addContext("Ambiente", "QA");
        FinancialReport.addContext("Ruta", "2364");

        // Lane 1 Op visible: total = 250.4+40.2+10+200 = 500.6 → $501
        // Profit = 1000.4-500.6=499.8, 1200.6-500.6=700
        FinancialAssertions.validateLane(1, payload.getLanes().get(0),
                lane(new double[]{1000, 1201}, -250, -40, -10, -200, -501, new double[]{500, 700}),
                2.0, true);

        // Lane 2 con fuel incorrecto a propósito
        FinancialAssertions.validateLane(2, payload.getLanes().get(1),
                lane(new double[]{0, 0}, -999, 0, 0, -100, -220, new double[]{-220, -220}),
                2.0, true);

        // Totales: BE totalCost 720.6 → $721; BE profit 579.8–780 → UI
        FinancialAssertions.validateTotals(payload,
                lane(new double[]{1000, 1201}, -370, -40, -10, -2, -721, new double[]{580, 780}),
                true);

        assertTrue(FinancialReport.failures().size() >= 1, "Debe detectar el fuel incorrecto de lane 2");
        Path html = FinancialReport.writeHtml();
        assertTrue(Files.exists(html));
        String content = Files.exists(html) ? FinancialReport.textSummary() : "";
        assertTrue(content.contains("Lane 1"));
        assertTrue(content.contains("Fuel cost") || content.contains("Backend"));

        String htmlBody = "";
        try {
            htmlBody = Files.readString(html);
        } catch (Exception ignored) {
            // ignore
        }
        assertTrue(htmlBody.contains("Por cada lane") || htmlBody.contains("lane"));
        assertTrue(htmlBody.contains("Total") || htmlBody.contains("fila inferior"));
    }

    @Test
    void botonAddEsCero() {
        RouteFinancialPayload payload = RouteFinancialParser.parse(JSON);
        LaneUiValues ui = lane(new double[]{0, 0}, -120, 0, 0, -100, -220, new double[]{-220, -220});
        ui.markAsPending(FinancialColumn.CUSTOM);

        FinancialAssertions.validateLane(2, payload.getLanes().get(1), ui, 2.0, true);

        boolean customOk = FinancialReport.checks().stream()
                .anyMatch(c -> "Custom cost".equals(c.getField())
                        && c.getStatus() == FinancialCheck.Status.PASS
                        && (c.getDetail().contains("Add") || c.getFormula().contains("Add")));
        assertTrue(customOk, "Add + debe validarse como $0");
    }

    @Test
    void totalesConOpCostOcultoSeRecalculanSumandoLanesSinOp() {
        // Ruta con Op cost visible: finance.totalCost/profit YA incluyen el Op (100 = 5/mi x 10mi x 2 lanes).
        // finance.totalCost = 150 (sin Op) + 100 (Op) = 250
        // finance.profit (con Op) = income - 250 → min 700-250=450, max 900-250=650
        String json = """
                {
                  "id": "9001",
                  "name": "Ruta Op oculto",
                  "operativeCost": { "id": 1, "total": 5 },
                  "finance": {
                    "income": { "min": 700, "max": 900, "avg": 800 },
                    "profit": { "min": 450, "max": 650, "avg": 550 },
                    "fuelCost": 150, "tollCost": 0, "customCost": 0,
                    "operativeCost": 5, "totalCost": 250
                  },
                  "lanes": [
                    {
                      "laneId": "A", "type": "loaded",
                      "origin": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                      "destination": { "location": { "city_name": "Peoria", "state_code": "IL" } },
                      "operative": { "mileage": { "estimated": 10 } },
                      "finance": {
                        "income": { "min": 500, "max": 600, "avg": 550 },
                        "profit": { "min": 400, "max": 500, "avg": 450 },
                        "fuelCost": 100, "tollCost": 0, "customCost": 0, "totalCost": 100
                      }
                    },
                    {
                      "laneId": "B", "type": "deadhead",
                      "origin": { "location": { "city_name": "Peoria", "state_code": "IL" } },
                      "destination": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                      "operative": { "mileage": { "estimated": 10 } },
                      "finance": {
                        "income": { "min": 200, "max": 300, "avg": 250 },
                        "profit": { "min": 150, "max": 250, "avg": 200 },
                        "fuelCost": 50, "tollCost": 0, "customCost": 0, "totalCost": 50
                      }
                    }
                  ]
                }
                """;
        RouteFinancialPayload payload = RouteFinancialParser.parse(json);
        FinancialReport.addContext("Ambiente", "QA");

        // Σ Total cost sin Op = 100+50 = 150 ; Σ Profit sin Op = (400+150) a (500+250) = 550-750
        assertEquals(150.0, payload.sumTotalCostWithoutOp(), 0.001);
        assertEquals(550.0, payload.sumProfitMinWithoutOp(), 0.001);
        assertEquals(750.0, payload.sumProfitMaxWithoutOp(), 0.001);

        // La UI (Op oculto) muestra exactamente esos recalculados, NO el finance.totalCost/profit (con Op).
        LaneUiValues ui = lane(new double[]{700, 900}, -150, 0, 0, 0, -150, new double[]{550, 750});
        FinancialAssertions.validateTotals(payload, ui, false);

        boolean totalCostOk = FinancialReport.checks().stream()
                .anyMatch(c -> "Total cost".equals(c.getField()) && c.getStatus() == FinancialCheck.Status.PASS);
        boolean profitOk = FinancialReport.checks().stream()
                .anyMatch(c -> "Profit".equals(c.getField()) && c.getStatus() == FinancialCheck.Status.PASS);
        assertTrue(totalCostOk, "Total cost recalculado (sin Op) debe coincidir con la UI");
        assertTrue(profitOk, "Profit recalculado (sin Op, min/máx) debe coincidir con la UI");

        boolean noFailures = FinancialReport.failures().isEmpty();
        assertTrue(noFailures, "No debe haber discrepancias al recalcular Total cost/Profit sin Op: "
                + FinancialReport.failures());
    }

    @Test
    void profitOcultoInfladoPorOpSumSeDocumentaComoBugConocido() {
        // Mismos datos del caso anterior (Σ lanes sin Op = 150 ; Profit sin Op = 550-750),
        // pero ahora la UI reproduce el bug real de QA: Profit oculto = Profit esperado + Σ Op cost (100).
        String json = """
                {
                  "id": "9002",
                  "name": "Ruta Op oculto inflado",
                  "operativeCost": { "id": 1, "total": 5 },
                  "finance": {
                    "income": { "min": 700, "max": 900, "avg": 800 },
                    "profit": { "min": 450, "max": 650, "avg": 550 },
                    "fuelCost": 150, "tollCost": 0, "customCost": 0,
                    "operativeCost": 5, "totalCost": 250
                  },
                  "lanes": [
                    {
                      "laneId": "A", "type": "loaded",
                      "origin": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                      "destination": { "location": { "city_name": "Peoria", "state_code": "IL" } },
                      "operative": { "mileage": { "estimated": 10 } },
                      "finance": {
                        "income": { "min": 500, "max": 600, "avg": 550 },
                        "profit": { "min": 400, "max": 500, "avg": 450 },
                        "fuelCost": 100, "tollCost": 0, "customCost": 0, "totalCost": 100
                      }
                    },
                    {
                      "laneId": "B", "type": "deadhead",
                      "origin": { "location": { "city_name": "Peoria", "state_code": "IL" } },
                      "destination": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                      "operative": { "mileage": { "estimated": 10 } },
                      "finance": {
                        "income": { "min": 200, "max": 300, "avg": 250 },
                        "profit": { "min": 150, "max": 250, "avg": 200 },
                        "fuelCost": 50, "tollCost": 0, "customCost": 0, "totalCost": 50
                      }
                    }
                  ]
                }
                """;
        RouteFinancialPayload payload = RouteFinancialParser.parse(json);
        FinancialReport.addContext("Ambiente", "QA");

        // Σ Op cost = 5/mi * (10+10)mi = 100. Profit esperado sin Op: 550-750.
        // UI reproduce el bug: 550+100=650 .. 750+100=850, en vez de 550-750.
        LaneUiValues ui = lane(new double[]{700, 900}, -150, 0, 0, 0, -150, new double[]{650, 850});
        FinancialAssertions.validateTotals(payload, ui, false);

        FinancialCheck profitCheck = FinancialReport.checks().stream()
                .filter(c -> "Profit".equals(c.getField()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se registró el check de Profit"));

        assertTrue(profitCheck.getStatus() == FinancialCheck.Status.FAIL,
                "El Profit inflado por Op cost debe marcarse como FALLÓ");
        assertTrue(profitCheck.getDetail().contains("Posible bug de la app"),
                "El detalle debe documentar el patrón de bug conocido: " + profitCheck.getDetail());
        assertTrue(profitCheck.getDetail().contains("Σ Op cost"),
                "El detalle debe mencionar el Σ Op cost como causa: " + profitCheck.getDetail());
    }

    @Test
    void redondeoPorMilesimasComoEnImagen() {
        // 312.782 → $313 ; income 1125.54 → $1,126
        assertTrue(MoneyParser.matchesDisplayed(312.782, 313));
        assertTrue(MoneyParser.matchesDisplayed(1125.54, 1126));
        assertTrue(MoneyParser.matchesDisplayed(1622.4, 1622));
        assertTrue(MoneyParser.matchesDisplayed(8667.867699999999, 8668));
        assertTrue(MoneyParser.matchesDisplayed(2665.2657, 2665));
    }

    private static LaneUiValues lane(double[] income, double fuel, double toll, double custom,
                                     double op, double total, double[] profit) {
        LaneUiValues ui = new LaneUiValues();
        ui.put(FinancialColumn.INCOME, "$" + Math.round(income[0]) + " - $" + Math.round(income[1]),
                income[1], income, true);
        ui.put(FinancialColumn.FUEL, money(fuel), fuel, null, true);
        ui.put(FinancialColumn.TOLL, money(toll), toll, null, true);
        ui.put(FinancialColumn.CUSTOM, money(custom), custom, null, true);
        ui.put(FinancialColumn.OP_COST, money(op), op, null, true);
        ui.put(FinancialColumn.TOTAL_COST, money(total), total, null, true);
        ui.put(FinancialColumn.PROFIT, money(profit[0]) + " - " + money(profit[1]),
                profit[0], profit, true);
        return ui;
    }

    private static String money(double value) {
        return (value < 0 ? "-$" : "$") + Math.round(Math.abs(value));
    }
}
