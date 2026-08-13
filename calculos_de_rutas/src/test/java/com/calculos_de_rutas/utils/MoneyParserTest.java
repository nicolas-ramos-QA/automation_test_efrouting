package com.calculos_de_rutas.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Formatos reales tomados de la tabla de lanes de efRouting.
 */
class MoneyParserTest {

    @Test
    void interpretaImportesSimples() {
        assertEquals(2652.0, MoneyParser.parseSingle("$2,652"));
        assertEquals(-105.0, MoneyParser.parseSingle("-$105"));
        assertEquals(0.0, MoneyParser.parseSingle("$0"));
        assertEquals(0.0, MoneyParser.parseSingle("--"));
        assertNull(MoneyParser.parseSingle(""));
    }

    @Test
    void redondeoDeMilesimasAlEnteroDeLaUi() {
        assertEquals(313, MoneyParser.roundVisual(312.628));
        assertEquals(1126, MoneyParser.roundVisual(1125.54));
        assertEquals(1622, MoneyParser.roundVisual(1622.4));
        assertTrue(MoneyParser.matchesDisplayed(312.628, 313));
        assertTrue(MoneyParser.matchesDisplayedMagnitude(312.628, -313));
    }

    @Test
    void interpretaRangosConEspacioAAmbosLados() {
        assertArrayEquals(new double[]{4600, 6442}, MoneyParser.parseRange("$4,600 - $6,442"));
        assertArrayEquals(new double[]{750, 1120}, MoneyParser.parseRange("$750 - $1,120"));
    }

    /** El Profit se pinta sin espacio después del guion separador. */
    @Test
    void interpretaRangosSinEspacioDespuesDelSeparador() {
        assertArrayEquals(new double[]{4, 374}, MoneyParser.parseRange("$4 -$374"));
        assertArrayEquals(new double[]{535, 904}, MoneyParser.parseRange("$535 -$904"));
        assertArrayEquals(new double[]{878, 1748}, MoneyParser.parseRange("$878 -$1,748"));
    }

    @Test
    void interpretaRangosConExtremosNegativos() {
        assertArrayEquals(new double[]{-368, 234}, MoneyParser.parseRange("-$368 -$234"));
        assertArrayEquals(new double[]{-3257, -2387}, MoneyParser.parseRange("-$3,257 --$2,387"));
    }

    /** Un importe negativo suelto no debe confundirse con un rango. */
    @Test
    void unImporteNegativoNoEsUnRango() {
        assertArrayEquals(new double[]{-560, -560}, MoneyParser.parseRange("-$560"));
        assertArrayEquals(new double[]{0, 0}, MoneyParser.parseRange("$0"));
    }
}
