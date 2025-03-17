package net.adipappi.transport.service;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RouteService {
    // Implémentez les méthodes pour calculer les itinéraires
    private final RestTemplate restTemplate = new RestTemplate();

    public String calculateRoute(double startLon, double startLat, double endLon, double endLat) {
        String url = "http://router.project-osrm.org/route/v1/driving/" + startLon + "," + startLat + ";" + endLon + "," + endLat + "?overview=false";
        return restTemplate.getForObject(url, String.class);
    }
}
