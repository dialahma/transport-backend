
// src/main/java/net/adipappi/transport/integration/controller/GeolocationController.java
package net.adipappi.transport.api.controllers;

import net.adipappi.transport.service.GeolocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/geolocation")
public class GeolocationController {

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private GeolocationService geolocationService;
    // Endpoint pour le géocodage (adresse → coordonnées)
    @GetMapping("/geocode")
    public ResponseEntity<String> geocode(@RequestParam String address) {
        String result = geolocationService.geocode(address);
        result = "Geocoding result for: " + address;
        return ResponseEntity.ok(result);
    }

    // Endpoint pour le géocodage inverse (coordonnées → adresse)
    @GetMapping("/reverse-geocode")
    public ResponseEntity<String> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        String url = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lon + "&format=json";
        String result = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(result);
    }

    // Endpoint pour le calcul d'itinéraires (OSRM)
    @GetMapping("/route")
    public ResponseEntity<String> calculateRoute(
            @RequestParam double startLat, @RequestParam double startLon,
            @RequestParam double endLat, @RequestParam double endLon) {
        String url = "http://router.project-osrm.org/route/v1/driving/" + startLon + "," + startLat + ";" + endLon + "," + endLat + "?overview=false";
        String result = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(result);
    }
}