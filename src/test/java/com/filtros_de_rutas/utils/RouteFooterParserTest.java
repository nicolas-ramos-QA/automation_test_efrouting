package com.filtros_de_rutas.utils;

import com.filtros_de_rutas.models.RouteFooterTotals;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteFooterParserTest {

    private static final String FOOTER_SAMPLE = """
            Results: 543
            Total routes
            543
            Total income
            $69,141,785-$60,587,954
            Total miles
            1,165,641 mi
            DH miles
            257,555 mi (22.1%)
            Effective RPM
            $59.32-$59.70
            Loaded RPM
            $76.14-$76.63
            """;

    @Test
    void parseaTotalesDelPie() {
        RouteFooterTotals totals = RouteFooterParser.parse(FOOTER_SAMPLE);
        assertNotNull(totals);
        assertEquals(543, totals.getResultsCount());
        assertEquals(543, totals.getTotalRoutes());
        assertTrue(totals.getTotalIncome().contains("69,141,785"));
        assertTrue(totals.getTotalMiles().contains("1,165,641"));
        assertTrue(totals.getDhMiles().contains("257,555"));
        assertTrue(totals.getEffectiveRpm().contains("59.32"));
        assertTrue(totals.getLoadedRpm().contains("76.14"));
        assertTrue(totals.isReadable());
    }

    @Test
    void detectaCambioDeTotales() {
        RouteFooterTotals baseline = RouteFooterParser.parse(FOOTER_SAMPLE);
        RouteFooterTotals filtered = RouteFooterParser.parse("""
                Results: 12
                Total routes
                12
                Total income
                $2,484-$3,073
                Total miles
                1,151 mi
                DH miles
                200 mi (17.4%)
                Effective RPM
                $2.16-$2.67
                Loaded RPM
                $2.16-$2.67
                """);
        assertTrue(filtered.differsFrom(baseline));
    }
}
