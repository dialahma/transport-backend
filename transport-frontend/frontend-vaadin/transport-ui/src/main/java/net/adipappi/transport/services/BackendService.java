package net.adipappi.transport.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BackendService {
    
    private final String backendUrl = "http://localhost:8086";
    private final HttpClient httpClient;

    public BackendService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String startVideoStream(String rtspUrl, String streamName) {
        try {
            String url = backendUrl + "/api/video/stream?rtspUrl=" + rtspUrl + "&streamName=" + streamName;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la connexion au backend: " + e.getMessage();
        }
    }
}
