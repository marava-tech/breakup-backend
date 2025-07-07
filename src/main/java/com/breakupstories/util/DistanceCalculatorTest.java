package com.breakupstories.util;

/**
 * Simple test class for DistanceCalculator utility
 * This can be used to verify distance calculations work correctly
 */
public class DistanceCalculatorTest {
    
    public static void main(String[] args) {
        // Test coordinates (Mumbai to Delhi)
        double mumbaiLat = 19.0760;
        double mumbaiLon = 72.8777;
        double delhiLat = 28.7041;
        double delhiLon = 77.1025;
        
        double distance = DistanceCalculator.calculateDistance(mumbaiLat, mumbaiLon, delhiLat, delhiLon);
        System.out.println("Distance between Mumbai and Delhi: " + distance + " km");
        
        // Test 100km radius check
        boolean withinRadius = DistanceCalculator.isWithinRadius(mumbaiLat, mumbaiLon, delhiLat, delhiLon, 100.0);
        System.out.println("Within 100km radius: " + withinRadius);
        
        // Test nearby coordinates (Mumbai to Pune - should be within 100km)
        double puneLat = 18.5204;
        double puneLon = 73.8567;
        
        double distanceToPune = DistanceCalculator.calculateDistance(mumbaiLat, mumbaiLon, puneLat, puneLon);
        System.out.println("Distance between Mumbai and Pune: " + distanceToPune + " km");
        
        boolean puneWithinRadius = DistanceCalculator.isWithinRadius(mumbaiLat, mumbaiLon, puneLat, puneLon, 100.0);
        System.out.println("Pune within 100km radius: " + puneWithinRadius);
    }
} 