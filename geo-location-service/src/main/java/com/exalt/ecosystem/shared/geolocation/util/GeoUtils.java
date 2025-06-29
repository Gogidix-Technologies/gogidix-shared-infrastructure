package com.exalt.ecosystem.shared.geolocation.util;

/**
 * Utility class for geo-location calculations.
 */
public class GeoUtils {
    private static final double EARTH_RADIUS_METERS = 6371000; // Earth's radius in meters
    
    private GeoUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Calculate the Haversine distance between two coordinates in meters.
     * @param lat1 The latitude of the first point
     * @param lng1 The longitude of the first point
     * @param lat2 The latitude of the second point
     * @param lng2 The longitude of the second point
     * @return The distance in meters
     */
    public static double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return EARTH_RADIUS_METERS * c;
    }
    
    /**
     * Calculate a destination point given a starting point, bearing, and distance.
     * @param lat The starting latitude
     * @param lng The starting longitude
     * @param bearingDegrees The bearing in degrees (0 = north, 90 = east, etc.)
     * @param distanceMeters The distance in meters
     * @return An array with [latitude, longitude] of the destination point
     */
    public static double[] calculateDestination(double lat, double lng, double bearingDegrees, double distanceMeters) {
        double angularDistance = distanceMeters / EARTH_RADIUS_METERS;
        double bearingRadians = Math.toRadians(bearingDegrees);
        
        double latRadians = Math.toRadians(lat);
        double lngRadians = Math.toRadians(lng);
        
        double sinLatRadians = Math.sin(latRadians);
        double cosLatRadians = Math.cos(latRadians);
        double sinAngularDistance = Math.sin(angularDistance);
        double cosAngularDistance = Math.cos(angularDistance);
        double sinBearingRadians = Math.sin(bearingRadians);
        double cosBearingRadians = Math.cos(bearingRadians);
        
        double sinDestLatRadians = sinLatRadians * cosAngularDistance + 
                                  cosLatRadians * sinAngularDistance * cosBearingRadians;
        double destLatRadians = Math.asin(sinDestLatRadians);
        
        double y = sinBearingRadians * sinAngularDistance * cosLatRadians;
        double x = cosAngularDistance - sinLatRadians * sinDestLatRadians;
        double destLngRadians = lngRadians + Math.atan2(y, x);
        
        // Normalize longitude to -180 to +180
        destLngRadians = (destLngRadians + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        
        double[] result = new double[2];
        result[0] = Math.toDegrees(destLatRadians);
        result[1] = Math.toDegrees(destLngRadians);
        
        return result;
    }
    
    /**
     * Generate points forming a circle around a center point.
     * @param centerLat The center latitude
     * @param centerLng The center longitude
     * @param radiusMeters The radius in meters
     * @param numPoints The number of points to generate
     * @return An array of arrays with [latitude, longitude] for each point
     */
    public static double[][] generateCirclePoints(double centerLat, double centerLng, double radiusMeters, int numPoints) {
        double[][] circle = new double[numPoints][2];
        
        for (int i = 0; i < numPoints; i++) {
            double bearing = (360.0 / numPoints) * i;
            double[] point = calculateDestination(centerLat, centerLng, bearing, radiusMeters);
            circle[i][0] = point[0];
            circle[i][1] = point[1];
        }
        
        return circle;
    }
    
    /**
     * Check if a point is within a polygon defined by an array of points.
     * Uses the ray casting algorithm.
     * @param pointLat The latitude of the point to check
     * @param pointLng The longitude of the point to check
     * @param polygonPoints Array of [latitude, longitude] points forming the polygon
     * @return true if the point is within the polygon, false otherwise
     */
    public static boolean isPointInPolygon(double pointLat, double pointLng, double[][] polygonPoints) {
        boolean inside = false;
        int numPoints = polygonPoints.length;
        
        double[] lastPoint = polygonPoints[numPoints - 1];
        double lastLat = lastPoint[0];
        double lastLng = lastPoint[1];
        
        for (double[] currentPoint : polygonPoints) {
            double currentLat = currentPoint[0];
            double currentLng = currentPoint[1];
            
            boolean intersect = ((currentLat > pointLat) != (lastLat > pointLat)) &&
                               (pointLng < (lastLng - currentLng) * (pointLat - currentLat) / 
                                          (lastLat - currentLat) + currentLng);
            
            if (intersect) {
                inside = !inside;
            }
            
            lastLat = currentLat;
            lastLng = currentLng;
        }
        
        return inside;
    }
}
