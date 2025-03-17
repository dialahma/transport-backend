package net.adipappi.transport.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeolocationService {
    // Implémentez les méthodes pour interagir avec l'API de géolocalisation
    private final RestTemplate restTemplate = new RestTemplate();

    public String geocode(String address) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + address + "&format=json";
        return restTemplate.getForObject(url, String.class);
    }

    public String reverseGeoCode(double lat, double lon){
        String url = "https://nominatim.openstreetmap.org/reverse?lat=" + lat + "&lon=" + lon + "&format=json";
        return restTemplate.getForObject(url, String.class);
    }
}
