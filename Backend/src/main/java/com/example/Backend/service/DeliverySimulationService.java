package com.example.Backend.service;

import com.example.Backend.entity.Delivery;
import com.example.Backend.repository.DeliveryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Service for simulating real-time delivery driver movement.
 * Broadcasts driver positions via WebSocket for live tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliverySimulationService {

    private final DeliveryRepository deliveryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Task scheduler for position updates
    private TaskScheduler taskScheduler;

    // Active simulations (deliveryId -> scheduled task)
    private final Map<UUID, ScheduledFuture<?>> activeSimulations = new ConcurrentHashMap<>();
    private final Map<UUID, SimulationState> simulationStates = new ConcurrentHashMap<>();

    // Depot location: El Menzah, Tunis (West side, avoids crossing Lac de Tunis)
    private static final double DEPOT_LAT = 36.8283;
    private static final double DEPOT_LNG = 10.1583;

    // OpenRouteService API key (full token from user)
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImJkYzNjYjZlODY5NDQ5MGU4M2RjMWEwYjk5NjM2OWE0IiwiaCI6Im11cm11cjY0In0=";
    private static final String ORS_API_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    // HTTP client for API calls
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Simulation settings
    private static final int UPDATE_INTERVAL_MS = 3000; // Update every 3 seconds
    private static final int TOTAL_STEPS = 40; // ~2 minutes simulation (40 * 3s = 120s)

    @PostConstruct
    public void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("delivery-sim-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    /**
     * Start delivery simulation from depot to customer address
     */
    public void startSimulation(UUID deliveryId) {
        // Don't start if already running
        if (activeSimulations.containsKey(deliveryId)) {
            log.info("Simulation already running for delivery {}", deliveryId);
            return;
        }

        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null) {
            log.error("Delivery not found: {}", deliveryId);
            return;
        }

        log.info("=== STARTING DELIVERY SIMULATION ===");
        log.info("Delivery ID: {}", deliveryId);
        log.info("Delivery Address: '{}'", delivery.getAddress());
        log.info("Depot Location: ({}, {})", DEPOT_LAT, DEPOT_LNG);

        // Geocode destination from address
        double[] destCoords = geocodeAddress(delivery.getAddress());
        double destLat = destCoords[0];
        double destLng = destCoords[1];

        log.info("Geocoded Destination: ({}, {})", destLat, destLng);

        // Generate route waypoints using OpenRouteService
        List<double[]> waypoints = generateRoute(DEPOT_LAT, DEPOT_LNG, destLat, destLng, TOTAL_STEPS);
        log.info("Generated {} waypoints for route", waypoints.size());

        // Create simulation state with destination and route
        SimulationState state = new SimulationState(deliveryId, waypoints, destLat, destLng);
        simulationStates.put(deliveryId, state);

        // Schedule periodic updates
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                () -> updatePosition(deliveryId),
                Instant.now().plusMillis(1000),
                java.time.Duration.ofMillis(UPDATE_INTERVAL_MS));

        activeSimulations.put(deliveryId, future);
        log.info("Started delivery simulation for {} with {} waypoints. Destination: ({}, {})",
                deliveryId, waypoints.size(), destLat, destLng);
        log.info("=== SIMULATION STARTED ===");

        // Broadcast initial position WITH FULL ROUTE WAYPOINTS
        broadcastInitialRoute(deliveryId, DEPOT_LAT, DEPOT_LNG, destLat, destLng, waypoints);
    }

    /**
     * Stop delivery simulation
     */
    public void stopSimulation(UUID deliveryId) {
        ScheduledFuture<?> future = activeSimulations.remove(deliveryId);
        if (future != null) {
            future.cancel(false);
            log.info("Stopped simulation for delivery {}", deliveryId);
        }
        simulationStates.remove(deliveryId);
    }

    /**
     * Check if simulation is running for delivery
     */
    public boolean isSimulationRunning(UUID deliveryId) {
        return activeSimulations.containsKey(deliveryId);
    }

    /**
     * Update driver position (called periodically)
     */
    private void updatePosition(UUID deliveryId) {
        SimulationState state = simulationStates.get(deliveryId);
        if (state == null) {
            stopSimulation(deliveryId);
            return;
        }

        // Get next position
        int currentStep = state.currentStep;
        if (currentStep >= state.waypoints.size()) {
            // Reached destination
            log.info("Delivery {} simulation complete - driver arrived", deliveryId);

            double[] finalPos = state.waypoints.get(state.waypoints.size() - 1);
            broadcastPositionWithRoute(deliveryId, finalPos[0], finalPos[1], 0,
                    "Driver arrived at destination",
                    state.destinationLat, state.destinationLng, state.waypoints);

            // Update delivery location
            updateDeliveryLocation(deliveryId, finalPos[0], finalPos[1]);

            // AUTO-UPDATE STATUS TO DELIVERED
            markDeliveryAsDelivered(deliveryId);

            stopSimulation(deliveryId);
            return;
        }

        double[] position = state.waypoints.get(currentStep);
        double lat = position[0];
        double lng = position[1];

        // Calculate speed (km/h simulation)
        double speed = 30 + Math.random() * 20; // 30-50 km/h

        // Update delivery in database
        updateDeliveryLocation(deliveryId, lat, lng);

        // Broadcast to WebSocket - INCLUDE ROUTE WAYPOINTS in every message
        broadcastPositionWithRoute(deliveryId, lat, lng, speed,
                "En route - " + (state.waypoints.size() - currentStep) + " points remaining",
                state.destinationLat, state.destinationLng, state.waypoints);

        // Move to next step
        state.currentStep++;
    }

    /**
     * Mark delivery as DELIVERED when simulation completes
     */
    private void markDeliveryAsDelivered(UUID deliveryId) {
        try {
            Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
            if (delivery != null && !"DELIVERED".equals(delivery.getStatus())) {
                delivery.setStatus("DELIVERED");
                delivery.setActualDelivery(java.time.LocalDateTime.now());
                deliveryRepository.save(delivery);
                log.info("Auto-marked delivery {} as DELIVERED", deliveryId);
            }
        } catch (Exception e) {
            log.error("Error marking delivery as delivered: {}", e.getMessage());
        }
    }

    /**
     * Update delivery location in database
     */
    private void updateDeliveryLocation(UUID deliveryId, double lat, double lng) {
        try {
            Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
            if (delivery != null) {
                delivery.setCurrentLatitude(lat);
                delivery.setCurrentLongitude(lng);
                delivery.setCurrentLocation(String.format("%.6f, %.6f", lat, lng));
                deliveryRepository.save(delivery);
            }
        } catch (Exception e) {
            log.error("Error updating delivery location: {}", e.getMessage());
        }
    }

    /**
     * Broadcast position update via WebSocket with route waypoints
     * Includes waypoints in EVERY message so frontend gets them regardless of when
     * user opens page
     */
    private void broadcastPositionWithRoute(UUID deliveryId, double lat, double lng, double speed, String message,
            double destLat, double destLng, List<double[]> waypoints) {

        // Convert waypoints to list of [lat, lng] arrays for JSON
        List<List<Double>> routeCoords = new ArrayList<>();
        for (double[] wp : waypoints) {
            routeCoords.add(List.of(wp[0], wp[1]));
        }

        Map<String, Object> locationUpdate = new java.util.HashMap<>();
        locationUpdate.put("deliveryId", deliveryId.toString());
        locationUpdate.put("latitude", lat);
        locationUpdate.put("longitude", lng);
        locationUpdate.put("speed", speed);
        locationUpdate.put("heading", 0);
        locationUpdate.put("driverName", "Livreur Simulé");
        locationUpdate.put("message", message);
        locationUpdate.put("timestamp", System.currentTimeMillis());
        locationUpdate.put("destinationLatitude", destLat);
        locationUpdate.put("destinationLongitude", destLng);
        locationUpdate.put("routeWaypoints", routeCoords); // ROUTE WAYPOINTS in every message!

        messagingTemplate.convertAndSend(
                "/topic/delivery/" + deliveryId + "/location",
                locationUpdate);

        log.info("Broadcast position for {} with {} waypoints: ({}, {}) -> dest ({}, {})",
                deliveryId, waypoints.size(), lat, lng, destLat, destLng);
    }

    /**
     * Broadcast initial route with ALL waypoints for frontend to draw the full road
     * path
     */
    private void broadcastInitialRoute(UUID deliveryId, double startLat, double startLng,
            double destLat, double destLng, List<double[]> waypoints) {

        // Convert waypoints to list of [lat, lng] arrays for JSON
        List<List<Double>> routeCoords = new ArrayList<>();
        for (double[] wp : waypoints) {
            routeCoords.add(List.of(wp[0], wp[1]));
        }

        Map<String, Object> routeUpdate = new java.util.HashMap<>();
        routeUpdate.put("deliveryId", deliveryId.toString());
        routeUpdate.put("latitude", startLat);
        routeUpdate.put("longitude", startLng);
        routeUpdate.put("speed", 0);
        routeUpdate.put("heading", 0);
        routeUpdate.put("driverName", "Livreur Simulé");
        routeUpdate.put("message", "Simulation started - route calculated");
        routeUpdate.put("timestamp", System.currentTimeMillis());
        routeUpdate.put("destinationLatitude", destLat);
        routeUpdate.put("destinationLongitude", destLng);
        routeUpdate.put("routeWaypoints", routeCoords); // FULL ROUTE for frontend

        messagingTemplate.convertAndSend(
                "/topic/delivery/" + deliveryId + "/location",
                routeUpdate);

        log.info("Broadcast initial route for {} with {} waypoints", deliveryId, waypoints.size());
    }

    /**
     * Geocode address to coordinates.
     * PRIORITY: 1) Known Tunisian location keywords (accurate)
     * 2) OpenRouteService Geocoding API (may be inaccurate for Tunisia)
     */
    private double[] geocodeAddress(String address) {
        log.info("Geocoding address: '{}'", address);

        if (address == null || address.trim().isEmpty()) {
            log.warn("Empty address provided, using default Tunis center");
            return new double[] { 36.8065, 10.1815 };
        }

        // STEP 1: Check for known Tunisian location keywords FIRST
        // These are more accurate than ORS for specific Tunisian neighborhoods
        double keywordLat = getDestinationLatitude(null, address);
        double keywordLng = getDestinationLongitude(null, address);

        // Check if we found a specific keyword match (not the default random)
        // Default is 36.8065 ± 0.02 for lat and 10.1815 ± 0.02 for lng
        boolean isKeywordMatch = !(keywordLat > 36.78 && keywordLat < 36.83 &&
                keywordLng > 10.16 && keywordLng < 10.20);

        if (isKeywordMatch) {
            log.info("Using keyword-based coordinates for '{}': ({}, {})", address, keywordLat, keywordLng);
            return new double[] { keywordLat, keywordLng };
        }

        // STEP 2: Try ORS Geocoding API for addresses without keyword matches
        try {
            String encodedAddress = java.net.URLEncoder.encode(address + ", Tunisia", "UTF-8");
            String geocodeUrl = "https://api.openrouteservice.org/geocode/search?api_key=" + ORS_API_KEY
                    + "&text=" + encodedAddress + "&size=1";

            log.debug("Geocoding URL: {}", geocodeUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geocodeUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Geocoding response status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode features = root.path("features");

                if (features.isArray() && features.size() > 0) {
                    JsonNode coords = features.get(0).path("geometry").path("coordinates");
                    if (coords.isArray() && coords.size() >= 2) {
                        // GeoJSON is [lng, lat]
                        double lng = coords.get(0).asDouble();
                        double lat = coords.get(1).asDouble();
                        log.info("ORS geocoded '{}' to ({}, {})", address, lat, lng);
                        return new double[] { lat, lng };
                    }
                }
                log.warn("Geocoding returned no features for '{}'", address);
            } else {
                log.error("Geocoding API returned status {}: {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Geocoding error for '{}': {}", address, e.getMessage(), e);
        }

        // STEP 3: Fallback to Tunis center with small random offset
        log.warn("Using default Tunis center for unknown address: '{}'", address);
        return new double[] { 36.8065 + (Math.random() - 0.5) * 0.01,
                10.1815 + (Math.random() - 0.5) * 0.01 };
    }

    /**
     * Fallback destination latitude based on address keywords
     * Extended with more Tunisian neighborhoods for better accuracy
     */
    private double getDestinationLatitude(Delivery delivery, String address) {
        if (address != null) {
            String addr = address.toLowerCase();

            // Ben Arous Governorate
            if (addr.contains("mourouj"))
                return 36.7405;
            if (addr.contains("mégrine") || addr.contains("megrine"))
                return 36.7678;
            if (addr.contains("radès") || addr.contains("rades"))
                return 36.7694;
            if (addr.contains("hammam lif"))
                return 36.7306;
            if (addr.contains("ezzahra") || addr.contains("zahra"))
                return 36.7469;
            if (addr.contains("fouchana"))
                return 36.7042;
            if (addr.contains("ben arous"))
                return 36.7472;
            if (addr.contains("bou mhel"))
                return 36.7267;
            if (addr.contains("nouvelle medina"))
                return 36.7578;

            // Tunis Areas
            if (addr.contains("lac") && addr.contains("1"))
                return 36.8325;
            if (addr.contains("lac") && addr.contains("2"))
                return 36.8467;
            if (addr.contains("lac"))
                return 36.8325;
            if (addr.contains("marsa"))
                return 36.8775;
            if (addr.contains("carthage"))
                return 36.8528;
            if (addr.contains("sidi bou said") || addr.contains("sidi bou"))
                return 36.8685;
            if (addr.contains("gammarth"))
                return 36.9106;
            if (addr.contains("goulette"))
                return 36.8183;
            if (addr.contains("kram"))
                return 36.8331;
            if (addr.contains("aouina"))
                return 36.8439;
            if (addr.contains("bardo"))
                return 36.8085;
            if (addr.contains("menzah"))
                return 36.8283;
            if (addr.contains("manar"))
                return 36.8300;
            if (addr.contains("ennasr"))
                return 36.8400;
            if (addr.contains("el omrane") || addr.contains("omrane"))
                return 36.8206;
            if (addr.contains("ain zaghouan"))
                return 36.8189;
            if (addr.contains("médina") || addr.contains("medina"))
                return 36.7986;

            // Ariana Governorate
            if (addr.contains("ariana ville") || addr.contains("ariana centre"))
                return 36.8620;
            if (addr.contains("ariana"))
                return 36.8667;
            if (addr.contains("raoued"))
                return 36.9458;
            if (addr.contains("soukra"))
                return 36.8903;
            if (addr.contains("mnihla"))
                return 36.8314;
            if (addr.contains("ettadhamen"))
                return 36.8272;
            if (addr.contains("ghazela"))
                return 36.8917;

            // Manouba Governorate
            if (addr.contains("manouba"))
                return 36.8081;
            if (addr.contains("denden"))
                return 36.8025;
            if (addr.contains("oued ellil"))
                return 36.7931;
            if (addr.contains("douar hicher"))
                return 36.7794;

            // Other major cities
            if (addr.contains("sousse"))
                return 35.8288;
            if (addr.contains("sfax"))
                return 34.7406;
            if (addr.contains("monastir"))
                return 35.7833;
            if (addr.contains("mahdia"))
                return 35.5047;
            if (addr.contains("nabeul"))
                return 36.4561;
            if (addr.contains("hammamet"))
                return 36.4000;
            if (addr.contains("bizerte"))
                return 37.2744;
            if (addr.contains("kairouan"))
                return 35.6781;
            if (addr.contains("gabes") || addr.contains("gabès"))
                return 33.8881;
        }

        // Default: Tunis city center with small random offset
        log.warn("No location keyword found in '{}', using Tunis center", address);
        return 36.8065 + (Math.random() - 0.5) * 0.02;
    }

    /**
     * Fallback destination longitude based on address keywords
     * Extended with more Tunisian neighborhoods for better accuracy
     */
    private double getDestinationLongitude(Delivery delivery, String address) {
        if (address != null) {
            String addr = address.toLowerCase();

            // Ben Arous Governorate
            if (addr.contains("mourouj"))
                return 10.1927;
            if (addr.contains("mégrine") || addr.contains("megrine"))
                return 10.2333;
            if (addr.contains("radès") || addr.contains("rades"))
                return 10.2756;
            if (addr.contains("hammam lif"))
                return 10.3361;
            if (addr.contains("ezzahra") || addr.contains("zahra"))
                return 10.3058;
            if (addr.contains("fouchana"))
                return 10.1433;
            if (addr.contains("ben arous"))
                return 10.2333;
            if (addr.contains("bou mhel"))
                return 10.1942;
            if (addr.contains("nouvelle medina"))
                return 10.1758;

            // Tunis Areas
            if (addr.contains("lac") && addr.contains("1"))
                return 10.2167;
            if (addr.contains("lac") && addr.contains("2"))
                return 10.2342;
            if (addr.contains("lac"))
                return 10.2167;
            if (addr.contains("marsa"))
                return 10.3242;
            if (addr.contains("carthage"))
                return 10.3306;
            if (addr.contains("sidi bou said") || addr.contains("sidi bou"))
                return 10.3472;
            if (addr.contains("gammarth"))
                return 10.2906;
            if (addr.contains("goulette"))
                return 10.3053;
            if (addr.contains("kram"))
                return 10.2917;
            if (addr.contains("aouina"))
                return 10.2353;
            if (addr.contains("bardo"))
                return 10.1358;
            if (addr.contains("menzah"))
                return 10.1583;
            if (addr.contains("manar"))
                return 10.1500;
            if (addr.contains("ennasr"))
                return 10.1700;
            if (addr.contains("el omrane") || addr.contains("omrane"))
                return 10.1453;
            if (addr.contains("ain zaghouan"))
                return 10.1553;
            if (addr.contains("médina") || addr.contains("medina"))
                return 10.1708;

            // Ariana Governorate
            if (addr.contains("ariana ville") || addr.contains("ariana centre"))
                return 10.1867;
            if (addr.contains("ariana"))
                return 10.1667;
            if (addr.contains("raoued"))
                return 10.1931;
            if (addr.contains("soukra"))
                return 10.1931;
            if (addr.contains("mnihla"))
                return 10.1206;
            if (addr.contains("ettadhamen"))
                return 10.0956;
            if (addr.contains("ghazela"))
                return 10.1833;

            // Manouba Governorate
            if (addr.contains("manouba"))
                return 10.0992;
            if (addr.contains("denden"))
                return 10.0878;
            if (addr.contains("oued ellil"))
                return 10.0586;
            if (addr.contains("douar hicher"))
                return 10.0350;

            // Other major cities
            if (addr.contains("sousse"))
                return 10.6083;
            if (addr.contains("sfax"))
                return 10.7603;
            if (addr.contains("monastir"))
                return 10.8333;
            if (addr.contains("mahdia"))
                return 11.0622;
            if (addr.contains("nabeul"))
                return 10.7350;
            if (addr.contains("hammamet"))
                return 10.6167;
            if (addr.contains("bizerte"))
                return 9.8739;
            if (addr.contains("kairouan"))
                return 10.1006;
            if (addr.contains("gabes") || addr.contains("gabès"))
                return 10.0975;
        }

        // Default: Tunis city center with small random offset
        return 10.1815 + (Math.random() - 0.5) * 0.02;
    }

    /**
     * Generate route waypoints using OpenRouteService API for real road routes
     * Falls back to linear interpolation if API fails
     */
    private List<double[]> generateRoute(double startLat, double startLng,
            double endLat, double endLng, int targetSteps) {

        // Try to get real route from OpenRouteService
        try {
            List<double[]> realRoute = fetchRouteFromORS(startLat, startLng, endLat, endLng);
            if (realRoute != null && !realRoute.isEmpty()) {
                log.info("Got {} waypoints from OpenRouteService", realRoute.size());
                // Sample route to match target steps if too many points
                return sampleRoute(realRoute, targetSteps);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch route from OpenRouteService: {}. Using fallback.", e.getMessage());
        }

        // Fallback: linear interpolation with random offsets
        return generateFallbackRoute(startLat, startLng, endLat, endLng, targetSteps);
    }

    /**
     * Fetch route from OpenRouteService Directions API
     */
    private List<double[]> fetchRouteFromORS(double startLat, double startLng,
            double endLat, double endLng) throws Exception {
        // ORS uses [longitude, latitude] order!
        String requestBody = String.format(
                "{\"coordinates\":[[%f,%f],[%f,%f]]}",
                startLng, startLat, endLng, endLat);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ORS_API_URL))
                .header("Authorization", ORS_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("ORS API error: {} - {}", response.statusCode(), response.body());
            return null;
        }

        // Parse the geometry from response
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode coordinates = root
                .path("routes")
                .path(0)
                .path("geometry")
                .path("coordinates");

        if (coordinates.isMissingNode() || !coordinates.isArray()) {
            log.error("Invalid ORS response structure");
            return null;
        }

        List<double[]> waypoints = new ArrayList<>();
        for (JsonNode coord : coordinates) {
            // ORS returns [lng, lat], we need [lat, lng]
            double lng = coord.get(0).asDouble();
            double lat = coord.get(1).asDouble();
            waypoints.add(new double[] { lat, lng });
        }

        return waypoints;
    }

    /**
     * Sample route to reduce number of waypoints to target steps
     */
    private List<double[]> sampleRoute(List<double[]> route, int targetSteps) {
        if (route.size() <= targetSteps) {
            return route;
        }

        List<double[]> sampled = new ArrayList<>();
        double step = (double) route.size() / targetSteps;

        for (int i = 0; i < targetSteps; i++) {
            int index = (int) (i * step);
            sampled.add(route.get(index));
        }

        // Always include the last point (destination)
        sampled.add(route.get(route.size() - 1));

        return sampled;
    }

    /**
     * Fallback route generation using linear interpolation
     */
    private List<double[]> generateFallbackRoute(double startLat, double startLng,
            double endLat, double endLng, int steps) {
        List<double[]> waypoints = new ArrayList<>();

        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;

            // Linear interpolation
            double lat = startLat + (endLat - startLat) * progress;
            double lng = startLng + (endLng - startLng) * progress;

            // Add slight random offset to simulate real road movement
            if (i > 0 && i < steps) {
                lat += (Math.random() - 0.5) * 0.001;
                lng += (Math.random() - 0.5) * 0.001;
            }

            waypoints.add(new double[] { lat, lng });
        }

        return waypoints;
    }

    /**
     * Calculate heading angle between two points
     */
    private double calculateHeading(double lat1, double lng1, double lat2, double lng2) {
        double dLng = Math.toRadians(lng2 - lng1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double x = Math.sin(dLng) * Math.cos(lat2Rad);
        double y = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng);

        return Math.toDegrees(Math.atan2(x, y));
    }

    /**
     * Get destination latitude from delivery address
     * For now, using predefined locations in Tunis area
     */
    private double getDestinationLatitude(Delivery delivery) {
        // Try to parse from address or use defaults
        // TODO: Implement geocoding for real addresses
        String address = delivery.getAddress();
        if (address != null) {
            address = address.toLowerCase();
            if (address.contains("bardo"))
                return 36.8085;
            if (address.contains("ariana"))
                return 36.8667;
            if (address.contains("manouba"))
                return 36.8081;
            if (address.contains("ben arous"))
                return 36.7472;
            if (address.contains("sousse"))
                return 35.8288;
            if (address.contains("sfax"))
                return 34.7406;
        }
        // Default: Tunis city center
        return 36.8065 + (Math.random() - 0.5) * 0.05;
    }

    private double getDestinationLongitude(Delivery delivery) {
        String address = delivery.getAddress();
        if (address != null) {
            address = address.toLowerCase();
            if (address.contains("bardo"))
                return 10.1358;
            if (address.contains("ariana"))
                return 10.1667;
            if (address.contains("manouba"))
                return 10.0992;
            if (address.contains("ben arous"))
                return 10.2333;
            if (address.contains("sousse"))
                return 10.6083;
            if (address.contains("sfax"))
                return 10.7603;
        }
        // Default: Tunis city center
        return 10.1815 + (Math.random() - 0.5) * 0.05;
    }

    /**
     * Internal state class for tracking simulation progress
     */
    private static class SimulationState {
        final UUID deliveryId;
        final List<double[]> waypoints;
        final double destinationLat;
        final double destinationLng;
        int currentStep = 0;

        SimulationState(UUID deliveryId, List<double[]> waypoints, double destLat, double destLng) {
            this.deliveryId = deliveryId;
            this.waypoints = waypoints;
            this.destinationLat = destLat;
            this.destinationLng = destLng;
        }
    }
}
