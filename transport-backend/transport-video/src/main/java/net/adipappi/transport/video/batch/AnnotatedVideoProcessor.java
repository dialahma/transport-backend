package net.adipappi.transport.video.batch;

import net.adipappi.transport.video.service.ObjectDetectionService;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

@Component
public class AnnotatedVideoProcessor {

    private final ObjectDetectionService objectDetectionService;

    public AnnotatedVideoProcessor(ObjectDetectionService objectDetectionService) {
        this.objectDetectionService = objectDetectionService;
    }

    public Mat annotateFrame(Mat frame) {
        return objectDetectionService.detectAndAnnotate(frame);
    }
}
