package net.adipappi.transport.api.controllers.geolocation;


import net.adipappi.transport.service.geolocation.GeolocationService;
import net.adipappi.transport.service.geolocation.RouteService;

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

    @Autowired
    private RouteService routeService;

    // Endpoint pour le géocodage (adresse → coordonnées)
    @GetMapping("/geocode")
    public ResponseEntity<String> geocode(@RequestParam String address) {
        String result = geolocationService.geocode(address);
        return ResponseEntity.ok(result);
    }

    // Endpoint pour le géocodage inverse (coordonnées → adresse)
    @GetMapping("/reverse-geocode")
    public ResponseEntity<String> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {

        String result = geolocationService.reverseGeoCode(lat, lon);

        return ResponseEntity.ok(result);
    }

    // Endpoint pour le calcul d'itinéraires (OSRM)
    @GetMapping("/route")
    public ResponseEntity<String> calculateRoute(
            @RequestParam double startLat, @RequestParam double startLon,
            @RequestParam double endLat, @RequestParam double endLon) {

        String result = routeService.calculateRoute(startLon, startLat, endLon, endLat);
        return ResponseEntity.ok(result);
    }
}

