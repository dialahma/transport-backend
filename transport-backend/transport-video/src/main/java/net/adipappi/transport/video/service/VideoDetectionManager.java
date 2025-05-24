package net.adipappi.transport.video.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoDetectionManager {

    @Autowired
    private List<VideoDetectionService> detectionServices;

    private Map<String, VideoDetectionService> serviceByName;

    @PostConstruct
    public void init() {
        serviceByName = detectionServices.stream()
                .collect(Collectors.toMap(VideoDetectionService::getName, s -> s));

        System.out.println("🧠 Available detection services:");
        serviceByName.forEach((name, service) -> log.info(" -  {} ", name));
    }

    /**
     * Applique un service de détection donné à une image.
     *
     * @param detectionType ex: "vehicle", "face", "plate"
     * @param frame         image OpenCV
     * @return l’image annotée si le service existe, sinon l’image originale
     */
    public Mat applyDetection(String detectionType, Mat frame) {
        VideoDetectionService service = serviceByName.get(detectionType);
        if (service == null) {
            System.err.println("⚠️ No detection service found for: " + detectionType);
            return frame.clone();
        }
        try {
            return service.detectAndAnnotate(frame);
        } catch (Exception e) {
            System.err.println("❌ Error during detection [" + detectionType + "]: " + e.getMessage());
            return frame.clone();
        }
    }

    /**
     * Liste les types de détections disponibles.
     */
    public List<String> listAvailableDetections() {
        return List.copyOf(serviceByName.keySet());
    }
}
