package net.adipappi.transport.service.video;

import net.adipappi.transport.video.service.ObjectDetectionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class DetectionService {
    @Autowired
    private ObjectDetectionService detectionService;
    private Mat lastAnnotatedImage;
    private static final Logger logger = LoggerFactory.getLogger(DetectionService.class);

    public String detectionObjects(Mat imageFile){
        logger.debug("Début de la détection sur image {}x{}",
                imageFile.cols(), imageFile.rows());
        lastAnnotatedImage = detectionService.detectAndAnnotate(imageFile);

        // Comptage des véhicules détectés
        int vehicleCount = countDetectedVehicles(lastAnnotatedImage);
        logger.info("Détection terminée - Véhicules trouvés: {}", vehicleCount);

        return String.format("%d véhicules détectés", vehicleCount);
    }

    public Mat getLastAnnotatedImage() {
        return lastAnnotatedImage != null ? lastAnnotatedImage.clone() : new Mat();
    }

    private int countDetectedVehicles(Mat annotatedImage) {
        // Implémentation simplifiée - à adapter
        return (int) annotatedImage.total() / 1000; // Exemple
    }
}
