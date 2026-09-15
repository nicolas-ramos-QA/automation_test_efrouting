package com.automation_test_efrouting.utils;

import java.util.HashMap;
import java.util.Map;

public final class GeoDistance {

    private static final double EARTH_RADIUS_MILES = 3958.7613;

    private static final Map<String, double[]> COORDINATES = new HashMap<>();

    static {
        COORDINATES.put("Effingham, IL", new double[]{39.1200, -88.5434});
        COORDINATES.put("Tulsa, OK", new double[]{36.1540, -95.9928});
        COORDINATES.put("Chicago, IL", new double[]{41.8781, -87.6298});
    }

    private GeoDistance() {}

    public static double greatCircleMiles(String cityA, String cityB) {
        double[] a = coordinatesOf(cityA);
        double[] b = coordinatesOf(cityB);

        double lat1 = Math.toRadians(a[0]);
        double lat2 = Math.toRadians(b[0]);
        double dLat = Math.toRadians(b[0] - a[0]);
        double dLon = Math.toRadians(b[1] - a[1]);

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));

        return EARTH_RADIUS_MILES * c;
    }

    private static double[] coordinatesOf(String city) {
        double[] coordinates = COORDINATES.get(city);
        if (coordinates == null) {
            throw new IllegalArgumentException("No hay coordenadas registradas para la ciudad: " + city);
        }
        return coordinates;
    }
}
