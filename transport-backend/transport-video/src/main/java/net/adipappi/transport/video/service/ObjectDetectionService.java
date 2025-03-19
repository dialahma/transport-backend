package net.adipappi.transport.video.service;

import org.springframework.stereotype.Service;

@Service
public class ObjectDetectionService {

    public String detectObjects(byte[] image) {
        // Appeler un modèle de détection d'objets (YOLO, SSD, etc.)
        return "Véhicule détecté";
    }
}
