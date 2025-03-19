package net.adipappi.transport.service.video;

import net.adipappi.transport.video.service.ObjectDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DetectionService {
    @Autowired
    private ObjectDetectionService detectionService;

    public String detectionObjects(byte[] imageFile){
        return detectionService.detectObjects(imageFile);
    }
}
