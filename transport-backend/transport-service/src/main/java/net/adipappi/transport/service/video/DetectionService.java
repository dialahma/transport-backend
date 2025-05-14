package net.adipappi.transport.service.video;

import net.adipappi.transport.video.service.ObjectDetectionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class DetectionService {
    @Autowired
    private ObjectDetectionService detectionService;

    public String detectionObjects(Mat imageFile){
        Mat annotated = detectionService.detectAndAnnotate(imageFile);
        // Convertir le Mat annoté en String descriptive si nécessaire
        return "Détection effectuée - image annotée retournée";
    }


}
