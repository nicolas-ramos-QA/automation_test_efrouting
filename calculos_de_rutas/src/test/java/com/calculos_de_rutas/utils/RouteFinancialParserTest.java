package com.calculos_de_rutas.utils;

import com.calculos_de_rutas.models.LaneFinancials;
import com.calculos_de_rutas.models.RouteFinancialPayload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteFinancialParserTest {

    /** Muestra reducida del response real de user-route/{id}?features[]=markAsViewed. */
    private static final String JSON = """
            {
              "id": "2364",
              "name": "Unassigned_2026-08-08",
              "operativeCost": { "id": 3, "total": 2.56 },
              "lanes": [
                {
                  "laneId": "11675",
                  "type": "deadhead",
                  "origin": { "location": { "city_name": "Chicago", "state_code": "IL" } },
                  "destination": { "location": { "city_name": "Peoria", "state_code": "IL" } },
                  "operative": {
                    "mileage": { "estimated": 127.743, "actual": null },
                    "drivingTime": { "estimated": 2.32261, "actual": null },
                    "operationTime": { "estimated": 5.32261, "actual": null }
                  },
                  "finance": {
                    "income": { "min": 0, "max": 0, "avg": 0 },
                    "profit": { "min": -101.605, "max": -101.605, "avg": -101.605 },
                    "fuelCost": 101.605, "tollCost": 0, "customCost": 0, "totalCost": 101.605
                  }
                },
                {
                  "laneId": "11676",
                  "type": "loaded",
                  "origin": { "location": { "city_name": "Peoria", "state_code": "IL" } },
                  "destination": { "location": { "city_name": "Dallas", "state_code": "TX" } },
                  "operative": {
                    "mileage": { "estimated": 800.5, "actual": null },
                    "drivingTime": { "estimated": 12.5, "actual": null },
                    "operationTime": { "estimated": 15.0, "actual": null }
                  },
                  "finance": {
                    "income": { "min": 1500, "max": 1900, "avg": 1700 },
                    "profit": { "min": 900, "max": 1300, "avg": 1100 },
                    "fuelCost": 550, "tollCost": 40, "customCost": 10, "totalCost": 600
                  }
                }
              ]
            }
            """;

    @Test
    void parseaLanesYTarifaOperativa() {
        RouteFinancialPayload payload = RouteFinancialParser.parse(JSON);

        assertEquals("2364", payload.getRouteId());
        assertEquals(2.56, payload.getOperativeCostRate());
        assertEquals(2, payload.getLanes().size());

        LaneFinancials deadhead = payload.getLanes().get(0);
        assertEquals("Chicago, IL", deadhead.getOrigin());
        assertEquals("Peoria, IL", deadhead.getDestination());
        assertEquals(101.605, deadhead.getFuelCost());
        assertEquals(0.0, deadhead.getIncomeMax());
        assertEquals(-101.605, deadhead.getProfitAvg());
        assertEquals(5.32261, deadhead.getOperationTime());
        assertEquals(101.605, deadhead.expectedTotalCostWithoutOp(), 0.001);
        assertEquals(115.0, deadhead.expectedTotalCostWithOp(13.395), 0.01);
    }

    @Test
    void sumaLosTotalesDesdeLasLanes() {
        RouteFinancialPayload payload = RouteFinancialParser.parse(JSON);

        assertEquals(651.605, payload.sumFuel(), 0.001);
        assertEquals(40.0, payload.sumToll(), 0.001);
        assertEquals(1500.0, payload.sumIncomeMin(), 0.001);
        assertEquals(1900.0, payload.sumIncomeMax(), 0.001);
        assertEquals(701.605, payload.sumTotalCostWithoutOp(), 0.001);
        assertEquals(998.395, payload.sumProfitWithoutOp(), 0.001);
    }

    @Test
    void parseaMontosConSignoYRangos() {
        assertEquals(-101.61, MoneyParser.parseSingle("-$101.61"), 0.001);
        assertEquals(-101.61, MoneyParser.parseSingle("($101.61)"), 0.001);
        assertEquals(1234.0, MoneyParser.parseSingle("$1,234"), 0.001);

        double[] range = MoneyParser.parseRange("$1,234 - $2,456");
        assertEquals(1234, range[0], 0.01);
        assertEquals(2456, range[1], 0.01);

        assertEquals(250, MoneyParser.roundVisual(250.4));
        assertTrue(MoneyParser.matchesDisplayed(250.4, 250));
    }
}
