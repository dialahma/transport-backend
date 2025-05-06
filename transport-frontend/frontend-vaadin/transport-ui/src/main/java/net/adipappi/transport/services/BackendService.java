package net.adipappi.transport.services;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
@Service
public class BackendService {
    
    private final String backendUrl = "http://localhost:8085";
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

    public String startHlsStreaming(String rtspUrl, String streamName) {
        try {
            String encodedUrl = java.net.URLEncoder.encode(rtspUrl, "UTF-8");
            String url = String.format("%s/api/video/stream?rtspUrl=%s&streamName=%s&hls=true",
                    backendUrl, encodedUrl, streamName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return "Flux HLS démarré. URL: " +
                        backendUrl + "/api/video/hls/" + streamName + ".m3u8";
            } else {
                return "Erreur serveur: " + response.body();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de connexion au backend", e);
        }
    }
}
