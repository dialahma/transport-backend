package net.adipappi.transport.service.video;

import lombok.extern.slf4j.Slf4j;
import net.adipappi.transport.video.service.ObjectDetectionService;
import net.adipappi.transport.video.service.VideoDetectionManager;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;

@Service
@Slf4j
public class DetectionService {
    @Autowired
    private ObjectDetectionService detectionService;
    @Autowired
    private VideoDetectionManager manager;

    private Mat lastAnnotatedImage;
    //private static final Logger logger = LoggerFactory.getLogger(DetectionService.class);

    public String detectionObjects(Mat imageFile){
        log.debug("Début de la détection sur image {}x{}",
                imageFile.cols(), imageFile.rows());
        lastAnnotatedImage = detectionService.detectAndAnnotate(imageFile);

        // Comptage des véhicules détectés
        int vehicleCount = countDetectedVehicles(lastAnnotatedImage);
        log.info("Détection terminée - Véhicules trouvés: {}", vehicleCount);

        return String.format("%d véhicules détectés", vehicleCount);
    }

    public Mat getLastAnnotatedImage() {
        return lastAnnotatedImage != null ? lastAnnotatedImage.clone() : new Mat();
    }

    public Mat detect(String detectionType, Mat frame){
        return manager.applyDetection(detectionType, frame);
    }

    public List<String> available(){
        return manager.listAvailableDetections();
    }
    private int countDetectedVehicles(Mat annotatedImage) {
        // Implémentation simplifiée - à adapter
        return (int) annotatedImage.total() / 1000; // Exemple
    }


}
