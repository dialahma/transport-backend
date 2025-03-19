package net.adipappi.transport.integration.client;

import org.springframework.stereotype.Component;

@Component
public class FaceRecognitionClient {

    public String recognizeFace(byte[] image) {
        // Appeler un service externe de reconnaissance faciale
        return "Visage reconnu";
    }
}
